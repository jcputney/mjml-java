package dev.jcputney.mjml.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps HTML5 named entities to their numeric character references. This allows MJML content with
 * HTML entities to be parsed by the strict JDK XML parser which only supports XML entities
 * (&amp;amp;, &amp;lt;, etc).
 */
public final class EntityTable {

  // Lookup map keyed by entity name (without & and ;) for single-pass scanning
  private static final Map<String, String> ENTITY_BY_NAME = new HashMap<>();

  static {
    // Most common HTML entities
    ENTITY_BY_NAME.put("nbsp", "&#160;");
    ENTITY_BY_NAME.put("iexcl", "&#161;");
    ENTITY_BY_NAME.put("cent", "&#162;");
    ENTITY_BY_NAME.put("pound", "&#163;");
    ENTITY_BY_NAME.put("curren", "&#164;");
    ENTITY_BY_NAME.put("yen", "&#165;");
    ENTITY_BY_NAME.put("brvbar", "&#166;");
    ENTITY_BY_NAME.put("sect", "&#167;");
    ENTITY_BY_NAME.put("uml", "&#168;");
    ENTITY_BY_NAME.put("copy", "&#169;");
    ENTITY_BY_NAME.put("ordf", "&#170;");
    ENTITY_BY_NAME.put("laquo", "&#171;");
    ENTITY_BY_NAME.put("not", "&#172;");
    ENTITY_BY_NAME.put("shy", "&#173;");
    ENTITY_BY_NAME.put("reg", "&#174;");
    ENTITY_BY_NAME.put("macr", "&#175;");
    ENTITY_BY_NAME.put("deg", "&#176;");
    ENTITY_BY_NAME.put("plusmn", "&#177;");
    ENTITY_BY_NAME.put("sup2", "&#178;");
    ENTITY_BY_NAME.put("sup3", "&#179;");
    ENTITY_BY_NAME.put("acute", "&#180;");
    ENTITY_BY_NAME.put("micro", "&#181;");
    ENTITY_BY_NAME.put("para", "&#182;");
    ENTITY_BY_NAME.put("middot", "&#183;");
    ENTITY_BY_NAME.put("cedil", "&#184;");
    ENTITY_BY_NAME.put("sup1", "&#185;");
    ENTITY_BY_NAME.put("ordm", "&#186;");
    ENTITY_BY_NAME.put("raquo", "&#187;");
    ENTITY_BY_NAME.put("frac14", "&#188;");
    ENTITY_BY_NAME.put("frac12", "&#189;");
    ENTITY_BY_NAME.put("frac34", "&#190;");
    ENTITY_BY_NAME.put("iquest", "&#191;");
    ENTITY_BY_NAME.put("Agrave", "&#192;");
    ENTITY_BY_NAME.put("Aacute", "&#193;");
    ENTITY_BY_NAME.put("Acirc", "&#194;");
    ENTITY_BY_NAME.put("Atilde", "&#195;");
    ENTITY_BY_NAME.put("Auml", "&#196;");
    ENTITY_BY_NAME.put("Aring", "&#197;");
    ENTITY_BY_NAME.put("AElig", "&#198;");
    ENTITY_BY_NAME.put("Ccedil", "&#199;");
    ENTITY_BY_NAME.put("Egrave", "&#200;");
    ENTITY_BY_NAME.put("Eacute", "&#201;");
    ENTITY_BY_NAME.put("Ecirc", "&#202;");
    ENTITY_BY_NAME.put("Euml", "&#203;");
    ENTITY_BY_NAME.put("Igrave", "&#204;");
    ENTITY_BY_NAME.put("Iacute", "&#205;");
    ENTITY_BY_NAME.put("Icirc", "&#206;");
    ENTITY_BY_NAME.put("Iuml", "&#207;");
    ENTITY_BY_NAME.put("ETH", "&#208;");
    ENTITY_BY_NAME.put("Ntilde", "&#209;");
    ENTITY_BY_NAME.put("Ograve", "&#210;");
    ENTITY_BY_NAME.put("Oacute", "&#211;");
    ENTITY_BY_NAME.put("Ocirc", "&#212;");
    ENTITY_BY_NAME.put("Otilde", "&#213;");
    ENTITY_BY_NAME.put("Ouml", "&#214;");
    ENTITY_BY_NAME.put("times", "&#215;");
    ENTITY_BY_NAME.put("Oslash", "&#216;");
    ENTITY_BY_NAME.put("Ugrave", "&#217;");
    ENTITY_BY_NAME.put("Uacute", "&#218;");
    ENTITY_BY_NAME.put("Ucirc", "&#219;");
    ENTITY_BY_NAME.put("Uuml", "&#220;");
    ENTITY_BY_NAME.put("Yacute", "&#221;");
    ENTITY_BY_NAME.put("THORN", "&#222;");
    ENTITY_BY_NAME.put("szlig", "&#223;");
    ENTITY_BY_NAME.put("agrave", "&#224;");
    ENTITY_BY_NAME.put("aacute", "&#225;");
    ENTITY_BY_NAME.put("acirc", "&#226;");
    ENTITY_BY_NAME.put("atilde", "&#227;");
    ENTITY_BY_NAME.put("auml", "&#228;");
    ENTITY_BY_NAME.put("aring", "&#229;");
    ENTITY_BY_NAME.put("aelig", "&#230;");
    ENTITY_BY_NAME.put("ccedil", "&#231;");
    ENTITY_BY_NAME.put("egrave", "&#232;");
    ENTITY_BY_NAME.put("eacute", "&#233;");
    ENTITY_BY_NAME.put("ecirc", "&#234;");
    ENTITY_BY_NAME.put("euml", "&#235;");
    ENTITY_BY_NAME.put("igrave", "&#236;");
    ENTITY_BY_NAME.put("iacute", "&#237;");
    ENTITY_BY_NAME.put("icirc", "&#238;");
    ENTITY_BY_NAME.put("iuml", "&#239;");
    ENTITY_BY_NAME.put("eth", "&#240;");
    ENTITY_BY_NAME.put("ntilde", "&#241;");
    ENTITY_BY_NAME.put("ograve", "&#242;");
    ENTITY_BY_NAME.put("oacute", "&#243;");
    ENTITY_BY_NAME.put("ocirc", "&#244;");
    ENTITY_BY_NAME.put("otilde", "&#245;");
    ENTITY_BY_NAME.put("ouml", "&#246;");
    ENTITY_BY_NAME.put("divide", "&#247;");
    ENTITY_BY_NAME.put("oslash", "&#248;");
    ENTITY_BY_NAME.put("ugrave", "&#249;");
    ENTITY_BY_NAME.put("uacute", "&#250;");
    ENTITY_BY_NAME.put("ucirc", "&#251;");
    ENTITY_BY_NAME.put("uuml", "&#252;");
    ENTITY_BY_NAME.put("yacute", "&#253;");
    ENTITY_BY_NAME.put("thorn", "&#254;");
    ENTITY_BY_NAME.put("yuml", "&#255;");
    // Typographic
    ENTITY_BY_NAME.put("ndash", "&#8211;");
    ENTITY_BY_NAME.put("mdash", "&#8212;");
    ENTITY_BY_NAME.put("lsquo", "&#8216;");
    ENTITY_BY_NAME.put("rsquo", "&#8217;");
    ENTITY_BY_NAME.put("sbquo", "&#8218;");
    ENTITY_BY_NAME.put("ldquo", "&#8220;");
    ENTITY_BY_NAME.put("rdquo", "&#8221;");
    ENTITY_BY_NAME.put("bdquo", "&#8222;");
    ENTITY_BY_NAME.put("dagger", "&#8224;");
    ENTITY_BY_NAME.put("Dagger", "&#8225;");
    ENTITY_BY_NAME.put("bull", "&#8226;");
    ENTITY_BY_NAME.put("hellip", "&#8230;");
    ENTITY_BY_NAME.put("permil", "&#8240;");
    ENTITY_BY_NAME.put("prime", "&#8242;");
    ENTITY_BY_NAME.put("Prime", "&#8243;");
    ENTITY_BY_NAME.put("lsaquo", "&#8249;");
    ENTITY_BY_NAME.put("rsaquo", "&#8250;");
    ENTITY_BY_NAME.put("oline", "&#8254;");
    ENTITY_BY_NAME.put("euro", "&#8364;");
    ENTITY_BY_NAME.put("trade", "&#8482;");
    // Math/symbols
    ENTITY_BY_NAME.put("larr", "&#8592;");
    ENTITY_BY_NAME.put("uarr", "&#8593;");
    ENTITY_BY_NAME.put("rarr", "&#8594;");
    ENTITY_BY_NAME.put("darr", "&#8595;");
    ENTITY_BY_NAME.put("harr", "&#8596;");
    ENTITY_BY_NAME.put("fnof", "&#402;");
    ENTITY_BY_NAME.put("circ", "&#710;");
    ENTITY_BY_NAME.put("tilde", "&#732;");
    ENTITY_BY_NAME.put("ensp", "&#8194;");
    ENTITY_BY_NAME.put("emsp", "&#8195;");
    ENTITY_BY_NAME.put("thinsp", "&#8201;");
    ENTITY_BY_NAME.put("zwnj", "&#8204;");
    ENTITY_BY_NAME.put("zwj", "&#8205;");
    ENTITY_BY_NAME.put("lrm", "&#8206;");
    ENTITY_BY_NAME.put("rlm", "&#8207;");
  }

  private EntityTable() {}

  /**
   * Replaces all known HTML named entities in the input string with their numeric character
   * references. XML entities (&amp;amp;, &amp;lt;, &amp;gt;, &amp;apos;, &amp;quot;) are left as-is
   * since the XML parser handles them natively.
   *
   * <p>This is a single-pass O(n) implementation that scans for {@code &} characters and looks up
   * the entity name in a hash map.
   *
   * @param input the input string potentially containing HTML named entities
   * @return the string with HTML named entities replaced by numeric character references
   */
  public static String replaceEntities(String input) {
    if (input == null || !input.contains("&")) {
      return input;
    }
    StringBuilder sb = new StringBuilder(input.length());
    int pos = 0;
    int len = input.length();
    while (pos < len) {
      int ampIdx = input.indexOf('&', pos);
      if (ampIdx < 0) {
        sb.append(input, pos, len);
        break;
      }
      // Copy text before &
      sb.append(input, pos, ampIdx);
      // Look for ; within a reasonable range (max entity name ~10 chars)
      int semiIdx = input.indexOf(';', ampIdx + 1);
      if (semiIdx < 0 || semiIdx - ampIdx > 12) {
        // Not a valid entity reference, copy the & and move on
        sb.append('&');
        pos = ampIdx + 1;
        continue;
      }
      String name = input.substring(ampIdx + 1, semiIdx);
      String replacement = ENTITY_BY_NAME.get(name);
      if (replacement != null) {
        sb.append(replacement);
        pos = semiIdx + 1;
      } else {
        // Not a known entity (may be an XML entity like &amp;), keep as-is
        sb.append('&');
        pos = ampIdx + 1;
      }
    }
    return sb.toString();
  }
}
