package dev.jcputney.mjml.css;

/**
 * CSS specificity as an (a, b, c) tuple where:
 * <ul>
 *   <li>a = number of ID selectors</li>
 *   <li>b = number of class, attribute, and pseudo-class selectors</li>
 *   <li>c = number of type and pseudo-element selectors</li>
 * </ul>
 * Inline styles have a virtual specificity of (1, 0, 0, 0) which always wins,
 * handled separately by the inliner.
 */
public record CssSpecificity(int a, int b, int c) implements Comparable<CssSpecificity> {

  public static final CssSpecificity ZERO = new CssSpecificity(0, 0, 0);

  /**
   * Adds two specificity values together (used for compound selectors).
   */
  public CssSpecificity add(CssSpecificity other) {
    return new CssSpecificity(a + other.a, b + other.b, c + other.c);
  }

  @Override
  public int compareTo(CssSpecificity other) {
    if (this.a != other.a) {
      return Integer.compare(this.a, other.a);
    }
    if (this.b != other.b) {
      return Integer.compare(this.b, other.b);
    }
    return Integer.compare(this.c, other.c);
  }

  @Override
  public String toString() {
    return "(" + a + "," + b + "," + c + ")";
  }
}
