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

  @Test
  void concurrentRendersWithDifferentTemplates() throws InterruptedException {
    int threadCount = 4;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    List<Throwable> errors = new CopyOnWriteArrayList<>();

    String[] templates = {
        // Hero template
        """
        <mjml><mj-body>
          <mj-hero background-url="https://example.com/bg.jpg">
            <mj-text>Hero content</mj-text>
            <mj-button href="https://example.com">Click</mj-button>
          </mj-hero>
        </mj-body></mjml>""",
        // Social template
        """
        <mjml><mj-body>
          <mj-section><mj-column>
            <mj-social>
              <mj-social-element name="facebook" href="https://facebook.com">FB</mj-social-element>
            </mj-social>
          </mj-column></mj-section>
        </mj-body></mjml>""",
        // Accordion template
        """
        <mjml><mj-body>
          <mj-section><mj-column>
            <mj-accordion>
              <mj-accordion-element>
                <mj-accordion-title>Title</mj-accordion-title>
                <mj-accordion-text>Content</mj-accordion-text>
              </mj-accordion-element>
            </mj-accordion>
          </mj-column></mj-section>
        </mj-body></mjml>""",
        // Simple text template
        """
        <mjml><mj-body>
          <mj-section><mj-column>
            <mj-text>Simple text</mj-text>
            <mj-image src="https://example.com/img.jpg" />
          </mj-column></mj-section>
        </mj-body></mjml>"""
    };

    for (int t = 0; t < threadCount; t++) {
      final int idx = t;
      executor.submit(() -> {
        try {
          startLatch.await();
          for (int i = 0; i < 10; i++) {
            MjmlRenderResult result = MjmlRenderer.render(templates[idx]);
            assertNotNull(result.html());
            assertTrue(result.html().contains("<!doctype html>")
                || result.html().contains("<!DOCTYPE html>"));
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
        "No errors during concurrent rendering with different templates: "
            + (errors.isEmpty() ? "none" : errors.get(0).getMessage()));
  }

  @Test
  void stressTest32Threads() throws InterruptedException {
    int threadCount = 32;
    int iterationsPerThread = 5;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    List<Throwable> errors = new CopyOnWriteArrayList<>();
    List<String> results = new CopyOnWriteArrayList<>();

    for (int t = 0; t < threadCount; t++) {
      final int threadId = t;
      executor.submit(() -> {
        try {
          startLatch.await();
          for (int i = 0; i < iterationsPerThread; i++) {
            String mjml = """
                <mjml>
                  <mj-head><mj-title>Stress %d-%d</mj-title></mj-head>
                  <mj-body>
                    <mj-section>
                      <mj-column>
                        <mj-text>Thread %d iteration %d</mj-text>
                      </mj-column>
                    </mj-section>
                  </mj-body>
                </mjml>
                """.formatted(threadId, i, threadId, i);

            MjmlRenderResult result = MjmlRenderer.render(mjml);
            assertNotNull(result.html());
            assertTrue(result.html().contains("Thread " + threadId + " iteration " + i));
            results.add(result.html());
          }
        } catch (Throwable e) {
          errors.add(e);
        } finally {
          doneLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    assertTrue(doneLatch.await(60, TimeUnit.SECONDS),
        "All 32 threads should finish within 60s");
    executor.shutdown();

    assertEquals(0, errors.size(),
        "No errors in 32-thread stress test: "
            + (errors.isEmpty() ? "none" : errors.get(0).getMessage()));
    assertEquals(threadCount * iterationsPerThread, results.size(),
        "All renders should produce results");
  }

  @Test
  void noSharedStateCorruption() throws InterruptedException {
    // Render with different configs and verify no cross-contamination
    int threadCount = 8;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    List<Throwable> errors = new CopyOnWriteArrayList<>();

    for (int t = 0; t < threadCount; t++) {
      final int threadId = t;
      final String uniqueTitle = "UniqueTitle_" + threadId;
      final String uniqueContent = "UniqueContent_" + threadId;
      executor.submit(() -> {
        try {
          startLatch.await();
          MjmlConfiguration config = MjmlConfiguration.builder()
              .language("lang" + threadId)
              .build();

          for (int i = 0; i < 15; i++) {
            String mjml = """
                <mjml>
                  <mj-head><mj-title>%s</mj-title></mj-head>
                  <mj-body>
                    <mj-section>
                      <mj-column>
                        <mj-text>%s</mj-text>
                      </mj-column>
                    </mj-section>
                  </mj-body>
                </mjml>
                """.formatted(uniqueTitle, uniqueContent);

            MjmlRenderResult result = MjmlRenderer.render(mjml, config);
            String html = result.html();

            // Verify our own content is present
            assertTrue(html.contains(uniqueContent),
                "Thread " + threadId + " should find its own content");
            assertTrue(result.title().contains(uniqueTitle),
                "Thread " + threadId + " should find its own title");
            assertTrue(html.contains("lang=\"lang" + threadId + "\""),
                "Thread " + threadId + " should find its own language");

            // Verify no OTHER thread's content leaked in
            for (int other = 0; other < threadCount; other++) {
              if (other != threadId) {
                assertTrue(!html.contains("UniqueContent_" + other),
                    "Thread " + threadId + " should NOT contain thread " + other + "'s content");
              }
            }
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
        "No shared state corruption detected: "
            + (errors.isEmpty() ? "none" : errors.get(0).getMessage()));
  }
}
