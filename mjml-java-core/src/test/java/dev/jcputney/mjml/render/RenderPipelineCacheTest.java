package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderer;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RenderPipelineCacheTest {

  private static final String MINIMAL_MJML = """
      <mjml>
        <mj-body>
          <mj-section>
            <mj-column>
              <mj-text>Cache test</mj-text>
            </mj-column>
          </mj-section>
        </mj-body>
      </mjml>
      """;

  @Test
  void registryCacheIsBounded() throws Exception {
    int maxSize = readCacheMaxSize();

    for (int i = 0; i < maxSize + 50; i++) {
      MjmlConfiguration config = MjmlConfiguration.builder()
          .language("lang-" + i)
          .build();
      MjmlRenderer.render(MINIMAL_MJML, config);
    }

    Map<?, ?> cache = readRegistryCache();
    assertTrue(cache.size() <= maxSize,
        "Registry cache should stay bounded at " + maxSize + " entries");
  }

  private static int readCacheMaxSize() throws Exception {
    Field maxSizeField = RenderPipeline.class.getDeclaredField("REGISTRY_CACHE_MAX_SIZE");
    maxSizeField.setAccessible(true);
    return maxSizeField.getInt(null);
  }

  private static Map<?, ?> readRegistryCache() throws Exception {
    Field cacheField = RenderPipeline.class.getDeclaredField("REGISTRY_CACHE");
    cacheField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<?, ?> cache = (Map<?, ?>) cacheField.get(null);
    return cache;
  }
}
