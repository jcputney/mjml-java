package dev.jcputney.javamjml.parser;

import dev.jcputney.javamjml.MjmlException;
import java.io.StringReader;
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

/**
 * Parses preprocessed MJML source into an MjmlDocument.
 * Uses the JDK DOM parser to build a DOM tree, then converts
 * it to our lightweight MjmlNode tree.
 */
public final class MjmlParser {

  private MjmlParser() {
  }

  /**
   * Parses raw MJML source into an MjmlDocument.
   * The source is preprocessed (CDATA wrapping, entity replacement)
   * before XML parsing.
   */
  public static MjmlDocument parse(String mjmlSource) {
    if (mjmlSource == null || mjmlSource.isBlank()) {
      throw new MjmlException("MJML source cannot be null or empty");
    }

    String preprocessed = MjmlPreprocessor.preprocess(mjmlSource);
    return parseXml(preprocessed);
  }

  private static MjmlDocument parseXml(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setValidating(false);
      // Disable external entities for security
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xml)));

      Element root = document.getDocumentElement();
      if (!"mjml".equals(root.getTagName())) {
        throw new MjmlException(
            "Root element must be <mjml>, found <" + root.getTagName() + ">");
      }

      MjmlNode rootNode = convertElement(root);
      return new MjmlDocument(rootNode);
    } catch (MjmlException e) {
      throw e;
    } catch (Exception e) {
      throw new MjmlException("Failed to parse MJML: " + e.getMessage(), e);
    }
  }

  private static MjmlNode convertElement(Element element) {
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
        case Node.ELEMENT_NODE -> node.addChild(convertElement((Element) child));
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
