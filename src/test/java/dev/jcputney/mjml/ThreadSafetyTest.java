package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * Tests concurrent rendering to verify thread safety of MjmlRenderer.
 */
class ThreadSafetyTest {

  @Test
  void concurrentRendersProduceCorrectOutput() throws InterruptedException {
    int threadCount = 8;
    int iterationsPerThread = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    List<Throwable> errors = new CopyOnWriteArrayList<>();
    List<String> results = new CopyOnWriteArrayList<>();

    for (int t = 0; t < threadCount; t++) {
      final int threadId = t;
      executor.submit(() -> {
        try {
          startLatch.await(); // All threads start at once
          for (int i = 0; i < iterationsPerThread; i++) {
            String mjml = """
                <mjml>
                  <mj-head>
                    <mj-title>Thread %d Iter %d</mj-title>
                  </mj-head>
                  <mj-body>
                    <mj-section>
                      <mj-column>
                        <mj-text>Thread %d content iteration %d</mj-text>
                      </mj-column>
                    </mj-section>
                  </mj-body>
                </mjml>
                """.formatted(threadId, i, threadId, i);

            MjmlRenderResult result = MjmlRenderer.render(mjml, MjmlConfiguration.defaults());
            assertNotNull(result.html());
            assertTrue(result.html().contains("Thread " + threadId + " content iteration " + i));
            assertTrue(result.title().contains("Thread " + threadId + " Iter " + i));
            results.add(result.html());
          }
        } catch (Throwable e) {
          errors.add(e);
        } finally {
          doneLatch.countDown();
        }
      });
    }

    startLatch.countDown(); // Release all threads
    assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All threads should finish within 30s");
    executor.shutdown();

    assertEquals(0, errors.size(),
        "No errors should occur during concurrent rendering. First error: "
            + (errors.isEmpty() ? "none" : errors.get(0).getMessage()));
    assertEquals(threadCount * iterationsPerThread, results.size(),
        "All renders should produce results");
  }

  @Test
  void concurrentRendersWithDifferentConfigurations() throws InterruptedException {
    int threadCount = 4;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    List<Throwable> errors = new CopyOnWriteArrayList<>();

    String[] languages = {"en", "fr", "de", "es"};

    for (int t = 0; t < threadCount; t++) {
      final String lang = languages[t];
      executor.submit(() -> {
        try {
          startLatch.await();
          MjmlConfiguration config = MjmlConfiguration.builder()
              .language(lang)
              .build();

          for (int i = 0; i < 20; i++) {
            String mjml = """
                <mjml>
                  <mj-body>
                    <mj-section>
                      <mj-column>
                        <mj-text>Content in %s</mj-text>
                      </mj-column>
                    </mj-section>
                  </mj-body>
                </mjml>
                """.formatted(lang);

            String html = MjmlRenderer.render(mjml, config).html();
            assertTrue(html.contains("lang=\"" + lang + "\""),
                "Each thread should get its configured language");
          }
        } catch (Throwable e) {
          errors.add(e);
        } finally {
          doneLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    assertTrue(doneLatch.await(30, TimeUnit.SECONDS));
    executor.shutdown();

    assertEquals(0, errors.size(),
        "No errors during concurrent rendering with different configs");
  }
}
