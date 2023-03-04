// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.io.File;
import org.junit.Test;

/**
 * Base performance test.
 *
 * @author Stefan Mueller
 */
public abstract class AbstractPerformanceTest<T> {

  /**
   * Tests the named functionality.
   */
  @Test
  public void testGetPerformance() throws Exception {
    File file = new File("src/test/resources/perf-test.properties");

    // Two calls before measure:
    T cfg = load(file);
    cfg = load(file);

    int runs = 10000;
    long tick = System.nanoTime();
    for (int i = 0; i < runs; i++) {
      cfg = load(file);
    }
    double nanos = System.nanoTime() - tick;
    System.out.println("Cfg - Time per load: " + (nanos / runs) / 1000 + "micros");

    // Two calls before measure:
    readProperties(cfg);
    readProperties(cfg);

    runs = 1000000;
    tick = System.nanoTime();
    for (int i = 0; i < runs; i++) {
      readProperties(cfg);
    }
    nanos = System.nanoTime() - tick;
    System.out.println("Cfg - Time per get: " + (nanos / (runs * 5)) / 1000 + "micros");

    // Two calls before measure:
    readPropertiesOnSubset(cfg);
    readPropertiesOnSubset(cfg);

    tick = System.nanoTime();
    for (int i = 0; i < runs; i++) {
      readPropertiesOnSubset(cfg);
    }
    nanos = System.nanoTime() - tick;
    System.out.println("Cfg - Time per get on subset: " + (nanos / (runs * 5)) / 1000 + "micros");
  }

  /**
   * Loads the configuration from file.
   */
  protected abstract T load(File file) throws Exception;

  /**
   * Reads properties.
   */
  protected abstract void readProperties(T cfg);

  /**
   * Reads properties on subset.
   */
  protected abstract void readPropertiesOnSubset(T cfg);
}
