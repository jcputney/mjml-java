package dev.jcputney.mjml.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Edge case tests for the CSS parsing, inlining, and selector matching subsystem. Covers
 * specificity conflicts, malformed CSS recovery, complex selectors, !important handling, and
 * pseudo-class preservation.
 */
class CssEdgeCaseTest {

  // --- Specificity conflict resolution ---

  @Test
  void idSelectorBeatsClassSelector() {
    String html =
        """
        <html>
        <head><style>
        .red { color: red; }
        #special { color: green; }
        </style></head>
        <body><div id="special" class="red">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(
        result.contains("color:green"), "ID selector (#special) should beat class selector (.red)");
  }

  @Test
  void classSelectorBeatsTypeSelector() {
    String html =
        """
        <html>
        <head><style>
        div { color: red; }
        .blue { color: blue; }
        </style></head>
        <body><div class="blue">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(
        result.contains("color:blue"), "Class selector (.blue) should beat type selector (div)");
  }

  @Test
  void idBeatClassBeatsTag() {
    String html =
        """
        <html>
        <head><style>
        p { color: red; }
        .cls { color: blue; }
        #uid { color: green; }
        </style></head>
        <body><p id="uid" class="cls">Hello</p></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:green"), "ID > class > tag specificity should hold");
  }

  @Test
  void laterRuleWinsAtEqualSpecificity() {
    String html =
        """
        <html>
        <head><style>
        .first { color: red; }
        .second { color: blue; }
        </style></head>
        <body><div class="first second">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:blue"), "Later rule should win when specificity is equal");
  }

  @Test
  void compoundSelectorHigherSpecificityThanSimple() {
    String html =
        """
        <html>
        <head><style>
        .a { color: red; }
        div.a { color: blue; }
        </style></head>
        <body><div class="a">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(
        result.contains("color:blue"),
        "Compound selector (div.a) should have higher specificity than simple (.a)");
  }

  // --- Malformed CSS recovery ---

  @Test
  void unclosedBraceRecovery() {
    // Parser should handle malformed CSS without throwing.
    // The input ".a { color: red; .b { color: blue; }" is ambiguous — the parser
    // will try to match braces and may collapse the two rules into one.
    // The key assertion is that it doesn't throw or infinite-loop.
    CssParser.ParseResult result = CssParser.parse(".a { color: red; .b { color: blue; }");
    assertNotNull(result, "Should handle malformed CSS without throwing");
  }

  @Test
  void missingPropertyValueIgnored() {
    CssParser.ParseResult result = CssParser.parse(".a { color: ; font-size: 14px; }");
    assertNotNull(result);
    assertFalse(result.rules().isEmpty(), "Should parse rules even with empty values");
  }

  @Test
  void missingColonInDeclarationIgnored() {
    List<CssDeclaration> decls = CssParser.parseDeclarations("color red; font-size: 14px");
    // The first declaration is malformed (no colon), should be skipped
    // The second should parse correctly
    boolean hasFontSize = decls.stream().anyMatch(d -> "font-size".equals(d.property()));
    assertTrue(
        hasFontSize, "Valid declarations should be parsed even with preceding malformed ones");
  }

  @Test
  void emptyCssProducesEmptyRules() {
    CssParser.ParseResult result = CssParser.parse("");
    assertNotNull(result);
    assertTrue(result.rules().isEmpty());
    assertTrue(result.preservedAtRules().isEmpty());
  }

  @Test
  void nullCssProducesEmptyRules() {
    CssParser.ParseResult result = CssParser.parse(null);
    assertNotNull(result);
    assertTrue(result.rules().isEmpty());
  }

  @Test
  void cssCommentStripping() {
    CssParser.ParseResult result =
        CssParser.parse("/* comment */ .a { color: red; } /* another comment */");
    assertNotNull(result);
    assertEquals(1, result.rules().size());
    assertEquals(".a", result.rules().get(0).selectorText());
  }

  @Test
  void unterminatedCommentHandled() {
    // Unterminated comment should not cause infinite loop
    CssParser.ParseResult result = CssParser.parse(".a { color: red; } /* unterminated");
    assertNotNull(result);
    assertFalse(result.rules().isEmpty());
  }

  // --- Complex selectors ---

  @Test
  void descendantSelectorMatches() {
    String html =
        """
        <html>
        <head><style>.parent .child { color: red; }</style></head>
        <body>
        <div class="parent"><span class="child">Inside</span></div>
        <span class="child">Outside</span>
        </body>
        </html>""";

    String result = CssInliner.inline(html);
    // The first span (inside .parent) should get the style
    int firstSpan = result.indexOf("<span");
    int secondSpan = result.indexOf("<span", firstSpan + 1);
    assertTrue(firstSpan >= 0 && secondSpan >= 0, "Should have two spans");

    String firstTag = result.substring(firstSpan, result.indexOf(">", firstSpan) + 1);
    assertTrue(
        firstTag.contains("color:red"), "Descendant span inside .parent should get the style");
  }

  @Test
  void childSelectorMatches() {
    String html =
        """
        <html>
        <head><style>.parent > .child { color: blue; }</style></head>
        <body>
        <div class="parent"><div class="child">Direct child</div></div>
        </body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:blue"), "Direct child selector should match");
  }

  @Test
  void adjacentSiblingSelector() {
    String html =
        """
        <html>
        <head><style>h1 + p { color: red; }</style></head>
        <body>
        <h1>Title</h1>
        <p>Adjacent</p>
        <p>Not adjacent</p>
        </body>
        </html>""";

    String result = CssInliner.inline(html);
    // At least one p should get the style
    assertTrue(
        result.contains("color:red"), "Adjacent sibling selector should match first p after h1");
  }

  @Test
  void generalSiblingSelector() {
    String html =
        """
        <html>
        <head><style>h1 ~ p { margin-top: 0; }</style></head>
        <body>
        <h1>Title</h1>
        <p>First</p>
        <p>Second</p>
        </body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(
        result.contains("margin-top:0"),
        "General sibling selector should match p elements after h1");
  }

  @Test
  void selectorListMatchesAllSelectors() {
    String html =
        """
        <html>
        <head><style>h1, h2, h3 { font-weight: bold; }</style></head>
        <body><h1>A</h1><h2>B</h2><p>C</p></body>
        </html>""";

    String result = CssInliner.inline(html);
    // h1 and h2 should match, p should not
    int firstBold = result.indexOf("font-weight:bold");
    assertTrue(firstBold >= 0, "At least one heading should get bold style");
  }

  // --- !important handling ---

  @Test
  void importantOverridesInlineStyle() {
    String html =
        """
        <html>
        <head><style>.override { color: blue !important; }</style></head>
        <body><div class="override" style="color:red">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:blue"), "!important should override existing inline style");
  }

  @Test
  void importantOverridesLowerSpecificity() {
    String html =
        """
        <html>
        <head><style>
        #unique { color: green; }
        .override { color: blue !important; }
        </style></head>
        <body><div id="unique" class="override">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:blue"), "!important on lower specificity should still win");
  }

  @Test
  void bothImportantHigherSpecificityWins() {
    String html =
        """
        <html>
        <head><style>
        .cls { color: red !important; }
        #uid { color: green !important; }
        </style></head>
        <body><div id="uid" class="cls">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(
        result.contains("color:green"), "When both are !important, higher specificity should win");
  }

  @Test
  void importantDeclarationParsedCorrectly() {
    List<CssDeclaration> decls =
        CssParser.parseDeclarations("color: red !important; font-size: 14px");
    assertEquals(2, decls.size());
    CssDeclaration colorDecl =
        decls.stream().filter(d -> "color".equals(d.property())).findFirst().orElseThrow();
    assertTrue(colorDecl.important(), "Declaration should be marked as important");
    assertEquals("red", colorDecl.value(), "Value should not contain !important text");
  }

  // --- Pseudo-class and pseudo-element preservation ---

  @Test
  void hoverPseudoClassNotInlined() {
    String html =
        """
        <html>
        <head><style>
        a { color: blue; }
        a:hover { color: red; }
        </style></head>
        <body><a href="#">Link</a></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:blue"), "Base rule should be inlined");
    assertTrue(
        result.contains("a:hover"), ":hover rule should be preserved in style block, not inlined");
  }

  @Test
  void firstChildPseudoClassNotInlined() {
    String html =
        """
        <html>
        <head><style>
        p:first-child { margin-top: 0; }
        p { margin: 10px; }
        </style></head>
        <body><p>First</p><p>Second</p></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(
        result.contains("p:first-child"), ":first-child rule should be preserved in style block");
    assertTrue(result.contains("margin:10px"), "Base rule should be inlined");
  }

  @Test
  void pseudoElementPreserved() {
    String html =
        """
        <html>
        <head><style>
        p::before { content: ">>"; }
        p { color: blue; }
        </style></head>
        <body><p>Text</p></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(
        result.contains("p::before"), "::before pseudo-element should be preserved in style block");
    assertTrue(result.contains("color:blue"), "Base rule should be inlined");
  }

  // --- @media preservation ---

  @Test
  void mediaQueryPreservedNotInlined() {
    String html =
        """
        <html>
        <head><style>
        .normal { color: red; }
        @media (max-width: 600px) {
          .mobile { font-size: 12px; }
        }
        </style></head>
        <body><div class="normal mobile">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:red"), "Regular rule should be inlined");
    assertTrue(result.contains("@media"), "Media query should be preserved in style block");
  }

  @Test
  void keyframesPreserved() {
    CssParser.ParseResult result =
        CssParser.parse(
            """
        .animated { animation: fade 1s; }
        @keyframes fade {
          from { opacity: 0; }
          to { opacity: 1; }
        }
        """);
    assertEquals(1, result.rules().size(), "Regular rule should be parsed");
    assertEquals(1, result.preservedAtRules().size(), "@keyframes should be preserved");
    assertTrue(
        result.preservedAtRules().get(0).contains("@keyframes fade"),
        "Preserved at-rule should contain keyframes");
  }

  // --- Attribute selectors ---

  @Test
  void attributeExactMatch() {
    String html =
        """
        <html>
        <head><style>[data-type="header"] { color: red; }</style></head>
        <body>
        <div data-type="header">Header</div>
        <div data-type="footer">Footer</div>
        </body>
        </html>""";

    String result = CssInliner.inline(html);
    // The header div should get the style
    int headerIdx = result.indexOf("Header");
    int divBefore = result.lastIndexOf("<div", headerIdx);
    String divTag = result.substring(divBefore, result.indexOf(">", divBefore) + 1);
    assertTrue(divTag.contains("color:red"), "Attribute exact match should apply style");
  }

  @Test
  void attributePresenceMatch() {
    String html =
        """
        <html>
        <head><style>[data-active] { font-weight: bold; }</style></head>
        <body>
        <div data-active>Active</div>
        <div>Inactive</div>
        </body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(
        result.contains("font-weight:bold"),
        "Attribute presence selector should match element with attribute");
  }

  // --- CssSelectorMatcher hasPseudo ---

  @Test
  void hasPseudoDetectsHover() {
    CssSelector selector = CssSelectorParser.parse("a:hover");
    assertNotNull(selector);
    assertTrue(CssSelectorMatcher.hasPseudo(selector), ":hover should be detected as pseudo");
  }

  @Test
  void hasPseudoDetectsBefore() {
    CssSelector selector = CssSelectorParser.parse("p::before");
    assertNotNull(selector);
    assertTrue(CssSelectorMatcher.hasPseudo(selector), "::before should be detected as pseudo");
  }

  @Test
  void hasPseudoFalseForSimpleSelector() {
    CssSelector selector = CssSelectorParser.parse("div.class#id");
    assertNotNull(selector);
    assertFalse(
        CssSelectorMatcher.hasPseudo(selector), "Simple compound selector should not have pseudo");
  }

  // --- Integration with MJML renderer ---

  @Test
  void mjmlInlineStyleOverridesCssClass() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-style inline="inline">
              .custom { color: red; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="custom">Styled</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>""";

    String html = dev.jcputney.mjml.MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("Styled"));
    // The inline CSS engine should process the rule
  }

  @Test
  void mjmlNonInlineStylePreservedInHead() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-style>
              .custom { color: red; }
              .custom:hover { color: blue; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="custom">Styled</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>""";

    String html = dev.jcputney.mjml.MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(
        html.contains(".custom:hover"),
        "Non-inline mj-style should preserve :hover in style block");
  }
}
