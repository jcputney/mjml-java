package dev.jcputney.javamjml.css;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CssInlinerTest {

  @Test
  void inlinesSimpleClassRule() {
    String html = """
        <html>
        <head><style>.red { color: red; }</style></head>
        <body><div class="red">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:red"), "Should inline color:red");
    assertTrue(result.contains("class=\"red\""), "Should keep class attribute");
  }

  @Test
  void inlinesTypeSelector() {
    String html = """
        <html>
        <head><style>p { font-size: 14px; }</style></head>
        <body><p>Hello</p><div>World</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("font-size:14px"), "Should inline font-size on p");
    // The div should NOT have a style attribute with font-size
    int divIdx = result.indexOf("<div");
    assertTrue(divIdx >= 0, "Should still contain div");
    String divTag = result.substring(divIdx, result.indexOf(">", divIdx) + 1);
    assertFalse(divTag.contains("font-size"), "div should not get p's font-size style");
  }

  @Test
  void mergesWithExistingInlineStyle() {
    String html = """
        <html>
        <head><style>.bold { font-weight: bold; }</style></head>
        <body><div class="bold" style="color:red">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:red"), "Should keep existing inline style");
    assertTrue(result.contains("font-weight:bold"), "Should add new style");
  }

  @Test
  void respectsImportant() {
    String html = """
        <html>
        <head><style>.override { color: blue !important; }</style></head>
        <body><div class="override" style="color:red">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:blue"), "!important should override inline");
  }

  @Test
  void preservesMediaQueries() {
    String html = """
        <html>
        <head><style>
        .red { color: red; }
        @media (max-width: 600px) { .red { color: blue; } }
        </style></head>
        <body><div class="red">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:red"), "Should inline the regular rule");
    assertTrue(result.contains("@media"), "Should preserve media query");
  }

  @Test
  void preservesPseudoClassRules() {
    String html = """
        <html>
        <head><style>
        a { color: blue; }
        a:hover { color: red; }
        </style></head>
        <body><a href="#">Link</a></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("color:blue"), "Should inline regular rule");
    assertTrue(result.contains("a:hover"), "Should preserve :hover rule in style block");
  }

  @Test
  void inlinesDescendantSelector() {
    String html = """
        <html>
        <head><style>.container p { margin: 0; }</style></head>
        <body>
        <div class="container"><p>Inside</p></div>
        <p>Outside</p>
        </body>
        </html>""";

    String result = CssInliner.inline(html);
    // The p inside .container should get the style
    assertTrue(result.contains("margin:0"), "Should inline on descendant p");
  }

  @Test
  void inlinesAdditionalCss() {
    String html = """
        <html>
        <head></head>
        <body><div class="custom">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html, ".custom { background: yellow; }");
    assertTrue(result.contains("background:yellow"), "Should inline additional CSS");
  }

  @Test
  void handlesIdSelector() {
    String html = """
        <html>
        <head><style>#header { padding: 10px; }</style></head>
        <body><div id="header">Title</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    assertTrue(result.contains("padding:10px"), "Should inline ID-selected rule");
  }

  @Test
  void respectsSpecificityOrder() {
    String html = """
        <html>
        <head><style>
        div { color: red; }
        .special { color: blue; }
        #unique { color: green; }
        </style></head>
        <body><div id="unique" class="special">Hello</div></body>
        </html>""";

    String result = CssInliner.inline(html);
    // ID should win (highest specificity)
    assertTrue(result.contains("color:green"),
        "ID selector should win due to higher specificity");
  }

  @Test
  void handlesNullHtml() {
    assertNull(CssInliner.inline(null));
  }

  @Test
  void handlesEmptyHtml() {
    String result = CssInliner.inline("");
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void handlesNoStyleBlocks() {
    String html = "<html><body><div>Hello</div></body></html>";
    String result = CssInliner.inline(html);
    assertTrue(result.contains("<div>Hello</div>"),
        "Should pass through HTML unchanged when no styles");
  }

  @Test
  void inlinesMultipleSelectorsInList() {
    String html = """
        <html>
        <head><style>h1, h2, h3 { font-weight: bold; }</style></head>
        <body><h1>A</h1><h2>B</h2><p>C</p></body>
        </html>""";

    String result = CssInliner.inline(html);
    // h1 and h2 should get the style, but not p
    int firstBold = result.indexOf("font-weight:bold");
    assertTrue(firstBold >= 0, "Should inline on at least one heading");
  }

  @Test
  void integrationWithMjmlRenderer() {
    // Test that CSS inlining works with MJML-rendered HTML
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style inline="inline">
              .red-text { color: red; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="red-text">Styled text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>""";

    String html = dev.jcputney.javamjml.MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertTrue(html.contains("Styled text"));
    // The inline CSS engine should process mj-style inline="inline" styles
  }
}
