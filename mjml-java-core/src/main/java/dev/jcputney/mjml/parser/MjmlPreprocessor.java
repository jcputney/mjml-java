package dev.jcputney.mjml.parser;

import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocesses MJML source before XML parsing.
 * <p>
 * MJML ending tags (mj-text, mj-button, mj-table, mj-raw, mj-navbar-link, etc.)
 * can contain arbitrary HTML which is not valid XML. This preprocessor wraps the
 * content of these tags in CDATA sections so the JDK XML parser can handle them.
 * <p>
 * Also replaces HTML named entities with numeric character references since the
 * XML parser only supports the 5 XML entities.
 */
public final class MjmlPreprocessor {

  private static final Logger LOG = Logger.getLogger(MjmlPreprocessor.class.getName());

  /**
   * Tags whose content may contain raw HTML and needs CDATA wrapping.
   */
  private static final Set<String> ENDING_TAGS = Set.of(
      "mj-text",
      "mj-button",
      "mj-table",
      "mj-raw",
      "mj-navbar-link",
      "mj-accordion-title",
      "mj-accordion-text",
      "mj-style",
      "mj-html-attribute"
  );

  /**
   * Single pre-compiled pattern that matches all ending tags in one pass.
   * Group 1: full opening tag, Group 2: tag name, Group 3: tag attributes,
   * Group 4: content between tags, Group 5: closing tag.
   */
  private static final Pattern COMBINED_TAG_PATTERN;

  static {
    String tagAlternation = String.join("|", ENDING_TAGS);
    COMBINED_TAG_PATTERN = Pattern.compile(
        "(<(" + tagAlternation + ")(\\s[^>]*)?(?<!/)>)" +
            "(.*?)" +
            "(</\\2\\s*>)",
        Pattern.DOTALL
    );
  }

  private MjmlPreprocessor() {
  }

  /**
   * Preprocesses MJML source for XML parsing.
   * Wraps ending tag content in CDATA sections and replaces HTML entities.
   */
  public static String preprocess(String mjml) {
    if (mjml == null) {
      throw new IllegalArgumentException("MJML source cannot be null");
    }
    if (mjml.isEmpty()) {
      return mjml;
    }

    // Wrap content of all ending tags in CDATA in a single regex pass
    String result = wrapTagContent(mjml);

    // Then replace HTML entities only OUTSIDE of CDATA sections
    result = replaceEntitiesOutsideCdata(result);

    return result;
  }

  /**
   * Replaces HTML named entities with numeric character references,
   * but only outside of CDATA sections (to preserve original entities in content).
   */
  private static String replaceEntitiesOutsideCdata(String input) {
    StringBuilder sb = new StringBuilder();
    int pos = 0;
    while (pos < input.length()) {
      int cdataStart = input.indexOf("<![CDATA[", pos);
      if (cdataStart < 0) {
        // No more CDATA — replace entities in the remaining text
        sb.append(EntityTable.replaceEntities(input.substring(pos)));
        break;
      }
      // Replace entities in the text before the CDATA section
      sb.append(EntityTable.replaceEntities(input.substring(pos, cdataStart)));
      // Find end of CDATA
      int cdataEnd = input.indexOf("]]>", cdataStart);
      if (cdataEnd < 0) {
        // Unterminated CDATA — keep rest as-is
        sb.append(input.substring(cdataStart));
        break;
      }
      cdataEnd += 3; // Include ]]>
      // Append CDATA section as-is (no entity replacement)
      sb.append(input, cdataStart, cdataEnd);
      pos = cdataEnd;
    }
    return sb.toString();
  }

  /**
   * Wraps the content between opening and closing tags in CDATA sections using a single
   * combined regex pass over all ending tags.
   * Self-closing tags are left as-is. Already-wrapped CDATA content is not double-wrapped.
   */
  private static String wrapTagContent(String input) {
    Matcher matcher = COMBINED_TAG_PATTERN.matcher(input);
    StringBuilder sb = new StringBuilder();

    while (matcher.find()) {
      String openTag = matcher.group(1);
      String tagName = matcher.group(2);
      String content = matcher.group(4);
      String closeTag = matcher.group(5);

      // Skip if content is empty or already wrapped in CDATA
      if (content.isEmpty() || content.trim().startsWith("<![CDATA[")) {
        matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
        continue;
      }

      // Escape any existing ]]> in content to prevent CDATA injection
      String safeContent = content.replace("]]>", "]]]]><![CDATA[>");
      // Wrap content in CDATA
      String replacement = openTag + "<![CDATA[" + safeContent + "]]>" + closeTag;
      LOG.fine(() -> "CDATA-wrapped content of <" + tagName + "> tag");
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }
}
