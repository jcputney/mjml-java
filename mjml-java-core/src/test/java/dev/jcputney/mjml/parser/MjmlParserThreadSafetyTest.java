package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

/**
 * Tests that MjmlParser.parse() is thread-safe when called concurrently. The DocumentBuilderFactory
 * is shared and requires synchronized access.
 */
class MjmlParserThreadSafetyTest {

  @Test
  void concurrentParseDoesNotCorrupt() throws Exception {
    String mjml =
        "<mjml><mj-body><mj-section><mj-column><mj-text>hello</mj-text></mj-column></mj-section></mj-body></mjml>";

    int threadCount = 8;
    int iterationsPerThread = 50;
    ExecutorService pool = Executors.newFixedThreadPool(threadCount);

    List<Callable<Void>> tasks = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      tasks.add(
          () -> {
            for (int j = 0; j < iterationsPerThread; j++) {
              MjmlDocument doc = MjmlParser.parse(mjml);
              assertNotNull(doc);
              assertNotNull(doc.root());
              assertEquals("mjml", doc.root().getTagName());
            }
            return null;
          });
    }

    List<Future<Void>> futures = pool.invokeAll(tasks);
    pool.shutdown();

    for (Future<Void> f : futures) {
      // get() will throw if the task threw
      f.get();
    }
  }
}
