package dev.jcputney.javamjml.parser;

/**
 * Parsed MJML document model containing the root node and
 * convenience accessors for head and body sections.
 */
public class MjmlDocument {

  private final MjmlNode root;

  public MjmlDocument(MjmlNode root) {
    this.root = root;
  }

  public MjmlNode getRoot() {
    return root;
  }

  /**
   * Returns the mj-head node, or null if not present.
   */
  public MjmlNode getHead() {
    return root.getFirstChildByTag("mj-head");
  }

  /**
   * Returns the mj-body node.
   */
  public MjmlNode getBody() {
    return root.getFirstChildByTag("mj-body");
  }
}
