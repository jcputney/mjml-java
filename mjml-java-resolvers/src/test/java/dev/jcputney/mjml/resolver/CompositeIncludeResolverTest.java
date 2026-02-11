package dev.jcputney.mjml.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompositeIncludeResolverTest {

  private static final ResolverContext CTX = ResolverContext.root("mjml");

  @Test
  void firstResolverSucceeds() {
    var r1 = MapIncludeResolver.of("a.mjml", "from-r1");
    var r2 = MapIncludeResolver.of("a.mjml", "from-r2");

    var composite = CompositeIncludeResolver.of(r1, r2);
    assertEquals("from-r1", composite.resolve("a.mjml", CTX));
  }

  @Test
  void fallbackToSecondResolver() {
    var r1 = MapIncludeResolver.of("a.mjml", "from-r1");
    var r2 = MapIncludeResolver.of("b.mjml", "from-r2");

    var composite = CompositeIncludeResolver.of(r1, r2);
    assertEquals("from-r2", composite.resolve("b.mjml", CTX));
  }

  @Test
  void allFailRethrowsLastException() {
    var r1 = MapIncludeResolver.of("a.mjml", "x");
    var r2 = MapIncludeResolver.of("b.mjml", "y");

    var composite = CompositeIncludeResolver.of(r1, r2);
    var ex = assertThrows(MjmlIncludeException.class, () -> composite.resolve("missing.mjml", CTX));
    // Last exception should mention "missing.mjml"
    assertEquals("Template not found in map: missing.mjml", ex.getMessage());
  }

  @Test
  void singleResolverWorks() {
    var r1 = MapIncludeResolver.of("x.mjml", "content");
    var composite = CompositeIncludeResolver.of(r1);
    assertEquals("content", composite.resolve("x.mjml", CTX));
  }

  @Test
  void emptyListThrows() {
    assertThrows(IllegalArgumentException.class, () -> new CompositeIncludeResolver(List.of()));
  }

  @Test
  void constructorDefensiveCopy() {
    var r1 = MapIncludeResolver.of("a.mjml", "content");
    var list = new java.util.ArrayList<dev.jcputney.mjml.IncludeResolver>();
    list.add(r1);
    var composite = new CompositeIncludeResolver(list);
    list.clear();
    // Should still work because we made a defensive copy
    assertEquals("content", composite.resolve("a.mjml", CTX));
  }

  @Test
  void threeResolversChained() {
    var r1 = MapIncludeResolver.of("a.mjml", "A");
    var r2 = MapIncludeResolver.of("b.mjml", "B");
    var r3 = MapIncludeResolver.of("c.mjml", "C");

    var composite = CompositeIncludeResolver.of(r1, r2, r3);
    assertEquals("A", composite.resolve("a.mjml", CTX));
    assertEquals("B", composite.resolve("b.mjml", CTX));
    assertEquals("C", composite.resolve("c.mjml", CTX));
  }
}
