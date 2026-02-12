package dev.jcputney.mjml.parser;

import dev.jcputney.mjml.MjmlException;
import dev.jcputney.mjml.MjmlParseException;
import dev.jcputney.mjml.MjmlValidationException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * Parses preprocessed MJML source into an MjmlDocument. Uses the JDK DOM parser to build a DOM
 * tree, then converts it to our lightweight MjmlNode tree.
 */
public final class MjmlParser {

  private static final DocumentBuilderFactory FACTORY;
  private static final ThreadLocal<DocumentBuilder> BUILDER_TL;
  private static final int DEFAULT_MAX_DEPTH = 100;

  static {
    try {
      FACTORY = DocumentBuilderFactory.newInstance();
      FACTORY.setNamespaceAware(false);
      FACTORY.setValidating(false);
      FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      FACTORY.setFeature("http://xml.org/sax/features/external-general-entities", false);
      FACTORY.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
    BUILDER_TL =
        ThreadLocal.withInitial(
            () -> {
              try {
                return FACTORY.newDocumentBuilder();
              } catch (Exception e) {
                throw new IllegalStateException("Failed to create DocumentBuilder", e);
              }
            });
  }

  private MjmlParser() {}

  /**
   * Parses raw MJML source into an MjmlDocument using the default maximum nesting depth. The source
   * is preprocessed (CDATA wrapping, entity replacement) before XML parsing.
   *
   * @param mjmlSource the raw MJML markup to parse
   * @return the parsed {@link MjmlDocument}
   */
  public static MjmlDocument parse(String mjmlSource) {
    return parse(mjmlSource, DEFAULT_MAX_DEPTH);
  }

  /**
   * Parses raw MJML source into an MjmlDocument with a configurable maximum nesting depth. The
   * source is preprocessed (CDATA wrapping, entity replacement) before XML parsing.
   *
   * @param mjmlSource the raw MJML markup
   * @param maxNestingDepth maximum allowed element nesting depth
   * @return the parsed {@link MjmlDocument}
   */
  public static MjmlDocument parse(String mjmlSource, int maxNestingDepth) {
    if (mjmlSource == null || mjmlSource.isBlank()) {
      throw new MjmlParseException("MJML source cannot be null or empty");
    }

    String preprocessed = MjmlPreprocessor.preprocess(mjmlSource);
    return parseXml(preprocessed, maxNestingDepth);
  }

  private static MjmlDocument parseXml(String xml, int maxNestingDepth) {
    try {
      DocumentBuilder builder = BUILDER_TL.get();
      builder.reset();
      Document document = builder.parse(new InputSource(new StringReader(xml)));

      Element root = document.getDocumentElement();
      if (!"mjml".equals(root.getTagName())) {
        throw new MjmlParseException(
            "Root element must be <mjml>, found <" + root.getTagName() + ">");
      }

      MjmlNode rootNode = convertElement(root, 0, maxNestingDepth);
      return new MjmlDocument(rootNode);
    } catch (MjmlException e) {
      throw e;
    } catch (SAXParseException spe) {
      throw buildParseException(spe, spe);
    } catch (Exception e) {
      Throwable cause = e.getCause();
      if (cause instanceof SAXParseException spe) {
        throw buildParseException(spe, e);
      }
      throw new MjmlParseException("Failed to parse MJML: " + e.getMessage(), e);
    }
  }

  private static MjmlParseException buildParseException(SAXParseException spe, Exception cause) {
    int line = spe.getLineNumber();
    int col = spe.getColumnNumber();
    String location = (line >= 0 ? " at line " + line : "") + (col >= 0 ? ", column " + col : "");
    return new MjmlParseException(
        "Failed to parse MJML" + location + ": " + spe.getMessage(), cause);
  }

  private static MjmlNode convertElement(Element element, int depth, int maxDepth) {
    if (depth > maxDepth) {
      throw new MjmlValidationException("Maximum nesting depth exceeded (" + maxDepth + ")");
    }
    MjmlNode node = new MjmlNode(element.getTagName());

    // Copy attributes
    NamedNodeMap attrs = element.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node attr = attrs.item(i);
      node.setAttribute(attr.getNodeName(), attr.getNodeValue());
    }

    // Process child nodes
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node child = childNodes.item(i);

      switch (child.getNodeType()) {
        case Node.ELEMENT_NODE ->
            node.addChild(convertElement((Element) child, depth + 1, maxDepth));
        case Node.TEXT_NODE -> {
          String text = ((Text) child).getWholeText();
          if (!text.isBlank()) {
            MjmlNode textNode = new MjmlNode("#text");
            textNode.setTextContent(text);
            node.addChild(textNode);
          }
        }
        case Node.CDATA_SECTION_NODE -> {
          String cdataContent = ((CDATASection) child).getData();
          // CDATA content is the raw HTML that was wrapped during preprocessing
          MjmlNode cdataNode = new MjmlNode("#cdata-section");
          cdataNode.setTextContent(cdataContent);
          node.addChild(cdataNode);
        }
        case Node.COMMENT_NODE -> {
          String commentText = ((Comment) child).getData();
          MjmlNode commentNode = new MjmlNode("#comment");
          commentNode.setTextContent(commentText);
          node.addChild(commentNode);
        }
      }
    }

    return node;
  }
}
