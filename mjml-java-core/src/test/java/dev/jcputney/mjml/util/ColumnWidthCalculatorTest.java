package dev.jcputney.mjml.util;

import static org.junit.jupiter.api.Assertions.*;

import dev.jcputney.mjml.parser.MjmlNode;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for ColumnWidthCalculator, including the zero-width sentinel fix.
 */
class ColumnWidthCalculatorTest {

  @Test
  void explicitZeroPxWidthIsNotTreatedAsAuto() {
    // Column with explicit width="0px" should get 0, not auto-width
    MjmlNode col1 = new MjmlNode("mj-column");
    col1.setAttribute("width", "0px");

    MjmlNode col2 = new MjmlNode("mj-column");
    col2.setAttribute("width", "300px");

    double[] widths = ColumnWidthCalculator.calculatePixelWidths(
        List.of(col1, col2), 600, false);

    assertEquals(0.0, widths[0], "width='0px' column should be 0, not auto");
    assertEquals(300.0, widths[1]);
  }

  @Test
  void autoColumnsShareRemainingSpace() {
    MjmlNode col1 = new MjmlNode("mj-column");
    col1.setAttribute("width", "200px");

    MjmlNode col2 = new MjmlNode("mj-column");
    // No width -> auto

    double[] widths = ColumnWidthCalculator.calculatePixelWidths(
        List.of(col1, col2), 600, false);

    assertEquals(200.0, widths[0]);
    assertEquals(400.0, widths[1]);
  }

  @Test
  void allAutoColumnsEqualShare() {
    MjmlNode col1 = new MjmlNode("mj-column");
    MjmlNode col2 = new MjmlNode("mj-column");
    MjmlNode col3 = new MjmlNode("mj-column");

    double[] widths = ColumnWidthCalculator.calculatePixelWidths(
        List.of(col1, col2, col3), 600, true);

    // With usePercentAuto: autoPct = 100/3, autoWidth = autoPct * 600 / 100
    double expected = (100.0 / 3.0) * 600 / 100.0;
    assertEquals(expected, widths[0], 0.001);
    assertEquals(expected, widths[1], 0.001);
    assertEquals(expected, widths[2], 0.001);
  }

  @Test
  void percentageWidthCalculated() {
    MjmlNode col = new MjmlNode("mj-column");
    col.setAttribute("width", "50%");

    double[] widths = ColumnWidthCalculator.calculatePixelWidths(
        List.of(col), 600, false);

    assertEquals(300.0, widths[0]);
  }

  @Test
  void widthSpecsForAutoColumns() {
    MjmlNode col1 = new MjmlNode("mj-column");
    MjmlNode col2 = new MjmlNode("mj-column");

    String[] specs = ColumnWidthCalculator.calculateWidthSpecs(List.of(col1, col2));

    assertEquals("50", specs[0]);
    assertEquals("50", specs[1]);
  }

  @Test
  void widthSpecsForExplicitColumns() {
    MjmlNode col1 = new MjmlNode("mj-column");
    col1.setAttribute("width", "75%");

    MjmlNode col2 = new MjmlNode("mj-column");
    col2.setAttribute("width", "150px");

    String[] specs = ColumnWidthCalculator.calculateWidthSpecs(List.of(col1, col2));

    assertEquals("75", specs[0]);
    assertEquals("150px", specs[1]);
  }
}
