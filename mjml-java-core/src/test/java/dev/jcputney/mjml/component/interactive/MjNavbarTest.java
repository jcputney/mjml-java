package dev.jcputney.mjml.component.interactive;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-navbar component rendering. */
class MjNavbarTest {

    private String render(String mjml) {
        String html = MjmlRenderer.render(mjml).html();
        assertNotNull(html);
        assertFalse(html.isEmpty());
        return html;
    }

    @Test
    void hamburgerModeRendersCheckbox() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar hamburger="hamburger">
                  <mj-navbar-link href="https://example.com/home">Home</mj-navbar-link>
                  <mj-navbar-link href="https://example.com/about">About</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("type=\"checkbox\""), "Hamburger mode should render a checkbox input");
        assertTrue(html.contains("mj-menu-trigger"), "Should contain menu trigger div for hamburger");
        assertTrue(html.contains("&#9776;"), "Should contain hamburger icon character");
    }

    @Test
    void linksRendered() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar>
                  <mj-navbar-link href="https://example.com/home">Home</mj-navbar-link>
                  <mj-navbar-link href="https://example.com/about">About</mj-navbar-link>
                  <mj-navbar-link href="https://example.com/contact">Contact</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("Home"), "Should render Home link text");
        assertTrue(html.contains("About"), "Should render About link text");
        assertTrue(html.contains("Contact"), "Should render Contact link text");
        assertTrue(html.contains("mj-inline-links"), "Should contain mj-inline-links class");
    }

    @Test
    void hamburgerCssIncludesMediaQuery() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar hamburger="hamburger">
                  <mj-navbar-link href="#">Link</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("mj-menu-checkbox"), "Should include hamburger checkbox CSS");
    }

    @Test
    void nonHamburgerModeNoCheckbox() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar>
                  <mj-navbar-link href="#">Simple Link</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertFalse(html.contains("mj-menu-trigger"), "Non-hamburger mode should not contain menu trigger");
        assertTrue(html.contains("Simple Link"), "Should still render the link text");
    }

    @Test
    void hamburgerRendersWithLinks() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar hamburger="hamburger">
                  <mj-navbar-link href="https://example.com/a">Link A</mj-navbar-link>
                  <mj-navbar-link href="https://example.com/b">Link B</mj-navbar-link>
                  <mj-navbar-link href="https://example.com/c">Link C</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("Link A"), "Should render first link");
        assertTrue(html.contains("Link B"), "Should render second link");
        assertTrue(html.contains("Link C"), "Should render third link");
        assertTrue(html.contains("mj-menu-trigger"), "Should render hamburger trigger");
        assertTrue(html.contains("mj-menu-icon-open"), "Should render open icon span");
        assertTrue(html.contains("mj-menu-icon-close"), "Should render close icon span");
        assertTrue(html.contains("mj-menu-checkbox"), "Should inject hamburger CSS for checkbox toggle");
    }

    @Test
    void hamburgerWithCustomIcoAttributes() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar
                  hamburger="hamburger"
                  ico-color="ff0000"
                  ico-font-size="40px"
                  ico-padding="20px"
                  ico-align="left">
                  <mj-navbar-link href="https://example.com">Link</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("#ff0000"), "Should include custom ico color with # prefix");
        assertTrue(html.contains("font-size:40px"), "Should use custom ico font size");
        assertTrue(html.contains("padding:20px"), "Should use custom ico padding");
        assertTrue(html.contains("align=\"left\""), "Should use custom ico alignment");
    }

    @Test
    void linkWithBaseUrl() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar base-url="https://example.com">
                  <mj-navbar-link href="/about">About</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("https://example.com/about"), "Should prepend base-url to relative href");
        assertTrue(html.contains("About"), "Should render link text");
    }

    @Test
    void linkWithRelAttribute() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar>
                  <mj-navbar-link href="https://example.com" rel="noopener">External</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("rel=\"noopener\""), "Should include rel attribute when set");
        assertTrue(html.contains("External"), "Should render link text");
    }
}
