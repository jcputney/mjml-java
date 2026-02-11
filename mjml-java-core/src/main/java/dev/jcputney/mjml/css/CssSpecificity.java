package dev.jcputney.mjml.css;

/**
 * CSS specificity as an (a, b, c) tuple where:
 *
 * <ul>
 *   <li>a = number of ID selectors
 *   <li>b = number of class, attribute, and pseudo-class selectors
 *   <li>c = number of type and pseudo-element selectors
 * </ul>
 *
 * Inline styles have a virtual specificity of (1, 0, 0, 0) which always wins, handled separately by
 * the inliner.
 *
 * @param a the number of ID selectors
 * @param b the number of class, attribute, and pseudo-class selectors
 * @param c the number of type and pseudo-element selectors
 */
public record CssSpecificity(int a, int b, int c) implements Comparable<CssSpecificity> {

  /**
   * Represents CSS specificity of zero, defined as (0, 0, 0). This is typically used to indicate
   * the absence of any selector specificity.
   */
  public static final CssSpecificity ZERO = new CssSpecificity(0, 0, 0);

  /**
   * Adds two specificity values together (used for compound selectors).
   *
   * @param other the specificity to add to this one
   * @return new specificity with each component summed
   */
  public CssSpecificity add(CssSpecificity other) {
    return new CssSpecificity(a + other.a, b + other.b, c + other.c);
  }

  /**
   * Compares this CSS specificity with another based on the specificity values (a, b, c). The
   * comparison is performed in order of significance: first by `a` (ID selectors), then by `b`
   * (class, attribute, and pseudo-class selectors), and finally by `c` (type and pseudo-element
   * selectors).
   *
   * @param other the other CssSpecificity object to compare with this instance
   * @return a negative integer, zero, or a positive integer as this specificity is less than, equal
   *     to, or greater than the specified specificity
   */
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

  /**
   * Returns a string representation of the CSS specificity as a tuple (a, b, c).
   *
   * @return a string in the format "(a,b,c)" where: `a` represents the number of ID selectors, `b`
   *     represents the number of class, attribute, and pseudo-class selectors, and `c` represents
   *     the number of type and pseudo-element selectors.
   */
  @Override
  public String toString() {
    return "(" + a + "," + b + "," + c + ")";
  }
}
