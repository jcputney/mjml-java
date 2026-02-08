package dev.jcputney.mjml.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps HTML5 named entities to their numeric character references.
 * This allows MJML content with HTML entities to be parsed by the
 * strict JDK XML parser which only supports XML entities (&amp;amp;, &amp;lt;, etc).
 */
public final class EntityTable {

  private static final Map<String, String> ENTITIES = new HashMap<>();

  static {
    // Most common HTML entities
    ENTITIES.put("&nbsp;", "&#160;");
    ENTITIES.put("&iexcl;", "&#161;");
    ENTITIES.put("&cent;", "&#162;");
    ENTITIES.put("&pound;", "&#163;");
    ENTITIES.put("&curren;", "&#164;");
    ENTITIES.put("&yen;", "&#165;");
    ENTITIES.put("&brvbar;", "&#166;");
    ENTITIES.put("&sect;", "&#167;");
    ENTITIES.put("&uml;", "&#168;");
    ENTITIES.put("&copy;", "&#169;");
    ENTITIES.put("&ordf;", "&#170;");
    ENTITIES.put("&laquo;", "&#171;");
    ENTITIES.put("&not;", "&#172;");
    ENTITIES.put("&shy;", "&#173;");
    ENTITIES.put("&reg;", "&#174;");
    ENTITIES.put("&macr;", "&#175;");
    ENTITIES.put("&deg;", "&#176;");
    ENTITIES.put("&plusmn;", "&#177;");
    ENTITIES.put("&sup2;", "&#178;");
    ENTITIES.put("&sup3;", "&#179;");
    ENTITIES.put("&acute;", "&#180;");
    ENTITIES.put("&micro;", "&#181;");
    ENTITIES.put("&para;", "&#182;");
    ENTITIES.put("&middot;", "&#183;");
    ENTITIES.put("&cedil;", "&#184;");
    ENTITIES.put("&sup1;", "&#185;");
    ENTITIES.put("&ordm;", "&#186;");
    ENTITIES.put("&raquo;", "&#187;");
    ENTITIES.put("&frac14;", "&#188;");
    ENTITIES.put("&frac12;", "&#189;");
    ENTITIES.put("&frac34;", "&#190;");
    ENTITIES.put("&iquest;", "&#191;");
    ENTITIES.put("&Agrave;", "&#192;");
    ENTITIES.put("&Aacute;", "&#193;");
    ENTITIES.put("&Acirc;", "&#194;");
    ENTITIES.put("&Atilde;", "&#195;");
    ENTITIES.put("&Auml;", "&#196;");
    ENTITIES.put("&Aring;", "&#197;");
    ENTITIES.put("&AElig;", "&#198;");
    ENTITIES.put("&Ccedil;", "&#199;");
    ENTITIES.put("&Egrave;", "&#200;");
    ENTITIES.put("&Eacute;", "&#201;");
    ENTITIES.put("&Ecirc;", "&#202;");
    ENTITIES.put("&Euml;", "&#203;");
    ENTITIES.put("&Igrave;", "&#204;");
    ENTITIES.put("&Iacute;", "&#205;");
    ENTITIES.put("&Icirc;", "&#206;");
    ENTITIES.put("&Iuml;", "&#207;");
    ENTITIES.put("&ETH;", "&#208;");
    ENTITIES.put("&Ntilde;", "&#209;");
    ENTITIES.put("&Ograve;", "&#210;");
    ENTITIES.put("&Oacute;", "&#211;");
    ENTITIES.put("&Ocirc;", "&#212;");
    ENTITIES.put("&Otilde;", "&#213;");
    ENTITIES.put("&Ouml;", "&#214;");
    ENTITIES.put("&times;", "&#215;");
    ENTITIES.put("&Oslash;", "&#216;");
    ENTITIES.put("&Ugrave;", "&#217;");
    ENTITIES.put("&Uacute;", "&#218;");
    ENTITIES.put("&Ucirc;", "&#219;");
    ENTITIES.put("&Uuml;", "&#220;");
    ENTITIES.put("&Yacute;", "&#221;");
    ENTITIES.put("&THORN;", "&#222;");
    ENTITIES.put("&szlig;", "&#223;");
    ENTITIES.put("&agrave;", "&#224;");
    ENTITIES.put("&aacute;", "&#225;");
    ENTITIES.put("&acirc;", "&#226;");
    ENTITIES.put("&atilde;", "&#227;");
    ENTITIES.put("&auml;", "&#228;");
    ENTITIES.put("&aring;", "&#229;");
    ENTITIES.put("&aelig;", "&#230;");
    ENTITIES.put("&ccedil;", "&#231;");
    ENTITIES.put("&egrave;", "&#232;");
    ENTITIES.put("&eacute;", "&#233;");
    ENTITIES.put("&ecirc;", "&#234;");
    ENTITIES.put("&euml;", "&#235;");
    ENTITIES.put("&igrave;", "&#236;");
    ENTITIES.put("&iacute;", "&#237;");
    ENTITIES.put("&icirc;", "&#238;");
    ENTITIES.put("&iuml;", "&#239;");
    ENTITIES.put("&eth;", "&#240;");
    ENTITIES.put("&ntilde;", "&#241;");
    ENTITIES.put("&ograve;", "&#242;");
    ENTITIES.put("&oacute;", "&#243;");
    ENTITIES.put("&ocirc;", "&#244;");
    ENTITIES.put("&otilde;", "&#245;");
    ENTITIES.put("&ouml;", "&#246;");
    ENTITIES.put("&divide;", "&#247;");
    ENTITIES.put("&oslash;", "&#248;");
    ENTITIES.put("&ugrave;", "&#249;");
    ENTITIES.put("&uacute;", "&#250;");
    ENTITIES.put("&ucirc;", "&#251;");
    ENTITIES.put("&uuml;", "&#252;");
    ENTITIES.put("&yacute;", "&#253;");
    ENTITIES.put("&thorn;", "&#254;");
    ENTITIES.put("&yuml;", "&#255;");
    // Typographic
    ENTITIES.put("&ndash;", "&#8211;");
    ENTITIES.put("&mdash;", "&#8212;");
    ENTITIES.put("&lsquo;", "&#8216;");
    ENTITIES.put("&rsquo;", "&#8217;");
    ENTITIES.put("&sbquo;", "&#8218;");
    ENTITIES.put("&ldquo;", "&#8220;");
    ENTITIES.put("&rdquo;", "&#8221;");
    ENTITIES.put("&bdquo;", "&#8222;");
    ENTITIES.put("&dagger;", "&#8224;");
    ENTITIES.put("&Dagger;", "&#8225;");
    ENTITIES.put("&bull;", "&#8226;");
    ENTITIES.put("&hellip;", "&#8230;");
    ENTITIES.put("&permil;", "&#8240;");
    ENTITIES.put("&prime;", "&#8242;");
    ENTITIES.put("&Prime;", "&#8243;");
    ENTITIES.put("&lsaquo;", "&#8249;");
    ENTITIES.put("&rsaquo;", "&#8250;");
    ENTITIES.put("&oline;", "&#8254;");
    ENTITIES.put("&euro;", "&#8364;");
    ENTITIES.put("&trade;", "&#8482;");
    // Math/symbols
    ENTITIES.put("&larr;", "&#8592;");
    ENTITIES.put("&uarr;", "&#8593;");
    ENTITIES.put("&rarr;", "&#8594;");
    ENTITIES.put("&darr;", "&#8595;");
    ENTITIES.put("&harr;", "&#8596;");
    ENTITIES.put("&fnof;", "&#402;");
    ENTITIES.put("&circ;", "&#710;");
    ENTITIES.put("&tilde;", "&#732;");
    ENTITIES.put("&ensp;", "&#8194;");
    ENTITIES.put("&emsp;", "&#8195;");
    ENTITIES.put("&thinsp;", "&#8201;");
    ENTITIES.put("&zwnj;", "&#8204;");
    ENTITIES.put("&zwj;", "&#8205;");
    ENTITIES.put("&lrm;", "&#8206;");
    ENTITIES.put("&rlm;", "&#8207;");
  }

  private EntityTable() {
  }

  /**
   * Replaces all known HTML named entities in the input string with
   * their numeric character references. XML entities (&amp;amp;, &amp;lt;, &amp;gt;,
   * &amp;apos;, &amp;quot;) are left as-is since the XML parser handles them natively.
   */
  public static String replaceEntities(String input) {
    if (input == null || !input.contains("&")) {
      return input;
    }
    String result = input;
    for (Map.Entry<String, String> entry : ENTITIES.entrySet()) {
      if (result.contains(entry.getKey())) {
        result = result.replace(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
}
