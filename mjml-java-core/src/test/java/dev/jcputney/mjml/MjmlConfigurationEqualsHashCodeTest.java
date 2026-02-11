package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests for MjmlConfiguration equals/hashCode. */
class MjmlConfigurationEqualsHashCodeTest {

  @Test
  void identicalBuildersProduceEqualConfigs() {
    MjmlConfiguration a =
        MjmlConfiguration.builder()
            .language("en")
            .direction(Direction.LTR)
            .maxInputSize(500_000)
            .maxNestingDepth(50)
            .maxIncludeDepth(10)
            .sanitizeOutput(true)
            .build();

    MjmlConfiguration b =
        MjmlConfiguration.builder()
            .language("en")
            .direction(Direction.LTR)
            .maxInputSize(500_000)
            .maxNestingDepth(50)
            .maxIncludeDepth(10)
            .sanitizeOutput(true)
            .build();

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  void identicalConfigsShareHashMapEntry() {
    MjmlConfiguration a = MjmlConfiguration.builder().language("en").build();
    MjmlConfiguration b = MjmlConfiguration.builder().language("en").build();

    Map<MjmlConfiguration, String> map = new HashMap<>();
    map.put(a, "first");
    map.put(b, "second");

    // With proper equals/hashCode, b should overwrite a's entry
    assertEquals(1, map.size());
    assertEquals("second", map.get(a));
  }

  @Test
  void differentLanguageNotEqual() {
    MjmlConfiguration a = MjmlConfiguration.builder().language("en").build();
    MjmlConfiguration b = MjmlConfiguration.builder().language("fr").build();

    assertNotEquals(a, b);
  }

  @Test
  void differentDirectionNotEqual() {
    MjmlConfiguration a = MjmlConfiguration.builder().direction(Direction.LTR).build();
    MjmlConfiguration b = MjmlConfiguration.builder().direction(Direction.RTL).build();

    assertNotEquals(a, b);
  }

  @Test
  void differentMaxInputSizeNotEqual() {
    MjmlConfiguration a = MjmlConfiguration.builder().maxInputSize(100).build();
    MjmlConfiguration b = MjmlConfiguration.builder().maxInputSize(200).build();

    assertNotEquals(a, b);
  }

  @Test
  void sameIncludeResolverInstanceEqual() {
    IncludeResolver resolver = (path, ctx) -> "";
    MjmlConfiguration a = MjmlConfiguration.builder().includeResolver(resolver).build();
    MjmlConfiguration b = MjmlConfiguration.builder().includeResolver(resolver).build();

    assertEquals(a, b);
  }

  @Test
  void differentIncludeResolverInstancesNotEqual() {
    IncludeResolver resolverA = (path, ctx) -> "";
    IncludeResolver resolverB = (path, ctx) -> "";
    MjmlConfiguration a = MjmlConfiguration.builder().includeResolver(resolverA).build();
    MjmlConfiguration b = MjmlConfiguration.builder().includeResolver(resolverB).build();

    assertNotEquals(a, b);
  }

  @Test
  void nullNotEqual() {
    MjmlConfiguration a = MjmlConfiguration.defaults();
    assertNotEquals(null, a);
  }

  @Test
  void reflexiveEquality() {
    MjmlConfiguration a = MjmlConfiguration.defaults();
    assertEquals(a, a);
  }

  @Test
  void defaultsAreEqual() {
    MjmlConfiguration a = MjmlConfiguration.defaults();
    MjmlConfiguration b = MjmlConfiguration.defaults();
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }
}
