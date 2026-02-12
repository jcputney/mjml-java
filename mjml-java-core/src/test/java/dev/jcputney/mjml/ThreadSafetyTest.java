package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * Tests concurrent rendering to verify thread safety of a shared {@link MjmlRenderer} instance.
 *
 * <p>A single renderer created via {@link MjmlRenderer#create()} is shared across 32 threads, each
 * calling {@link MjmlRenderer#renderTemplate(String)} concurrently. All results are verified for
 * correctness.
 */
class ThreadSafetyTest {

  private static final String SIMPLE_TEMPLATE =
      // language=MJML
      """
      <mjml>
        <mj-body>
          <mj-section>
            <mj-column>
              <mj-text>Hello World</mj-text>
            </mj-column>
          </mj-section>
        </mj-body>
      </mjml>
      """;

  private static final int THREAD_COUNT = 32;

  @Test
  void sharedRendererProducesCorrectOutputAcross32Threads() throws InterruptedException {
    MjmlRenderer sharedRenderer = MjmlRenderer.create();

    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
    List<String> results = new CopyOnWriteArrayList<>();
    List<Throwable> errors = new CopyOnWriteArrayList<>();

    for (int t = 0; t < THREAD_COUNT; t++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              MjmlRenderResult result = sharedRenderer.renderTemplate(SIMPLE_TEMPLATE);
              String html = result.html();
              assertNotNull(html, "Rendered HTML must not be null");
              assertFalse(html.isEmpty(), "Rendered HTML must not be empty");
              assertTrue(
                  html.contains("<!doctype html>"), "Rendered HTML must contain <!doctype html>");
              assertTrue(
                  html.contains("Hello World"), "Rendered HTML must contain the template text");
              results.add(html);
            } catch (Throwable e) {
              errors.add(e);
            } finally {
              doneLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    assertTrue(
        doneLatch.await(60, TimeUnit.SECONDS), "All 32 threads should finish within 60 seconds");
    executor.shutdown();

    if (!errors.isEmpty()) {
      Throwable first = errors.get(0);
      fail(
          "Expected no errors across 32 concurrent renders but got "
              + errors.size()
              + ". First: "
              + first.getMessage(),
          first);
    }
    assertEquals(THREAD_COUNT, results.size(), "All 32 threads should produce a result");
  }
}
