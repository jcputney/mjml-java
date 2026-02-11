package dev.jcputney.mjml.parser;

/**
 * Represents an MJML document's structure, containing a root node and methods to access key
 * elements such as the head and body nodes. This class encapsulates the MJML document's logical
 * model, allowing for the retrieval of specific parts of its structure.
 *
 * @param root the root {@code <mjml>} node of the document
 */
public record MjmlDocument(MjmlNode root) {

  /**
   * Creates a new MJML document with the given root node.
   *
   * @param root the root {@code <mjml>} node of the document
   */
  public MjmlDocument {}

  /**
   * Returns the root node of this document.
   *
   * @return the root {@code <mjml>} node
   */
  @Override
  public MjmlNode root() {
    return root;
  }

  /**
   * Returns the mj-head node, or {@code null} if not present.
   *
   * @return the {@code <mj-head>} node, or {@code null}
   */
  public MjmlNode getHead() {
    return root.getFirstChildByTag("mj-head");
  }

  /**
   * Returns the mj-body node, or {@code null} if not present.
   *
   * @return the {@code <mj-body>} node, or {@code null}
   */
  public MjmlNode getBody() {
    return root.getFirstChildByTag("mj-body");
  }
}
