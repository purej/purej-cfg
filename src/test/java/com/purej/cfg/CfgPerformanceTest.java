// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.io.File;
import org.junit.Test;

/**
 * Performance tests for Cfg usage.
 *
 * @author Stefan Mueller
 */
public class CfgPerformanceTest {

  /**
   * Tests the named functionality.
   */
  @Test
  public void testGetPerformance() throws Exception {
    File file = new File("src/test/resources/perf-test.properties");

    // Two calls before measure:
    Cfg cfg = new Cfg(file);
    cfg = new Cfg(file);

    int runs = 10000;
    long tick = System.nanoTime();
    for (int i = 0; i < runs; i++) {
      cfg = new Cfg(file);
    }
    double nanos = System.nanoTime() - tick;
    System.out.println("Cfg - Time per load: " + (nanos / runs) + "nanos");

    // Two calls before measure:
    doTestGetPerformance(cfg);
    doTestGetPerformance(cfg);

    runs = 1000000;
    tick = System.nanoTime();
    for (int i = 0; i < runs; i++) {
      doTestGetPerformance(cfg);
    }
    nanos = System.nanoTime() - tick;
    System.out.println("Cfg - Time per get: " + (nanos / (runs * 5)) + "nanos");

    // Two calls before measure:
    doTestGetPerformanceOnSubset(cfg.subset("my.path1"));
    doTestGetPerformanceOnSubset(cfg.subset("my.path1"));

    tick = System.nanoTime();
    for (int i = 0; i < runs; i++) {
      doTestGetPerformanceOnSubset(cfg.subset("my.path1"));
    }
    nanos = System.nanoTime() - tick;
    System.out.println("Cfg - Time per get on subset: " + (nanos / (runs * 5)) + "nanos");
  }

  private static void doTestGetPerformance(Cfg cfg) {
    cfg.getString("my.path1.string.value");
    cfg.getBoolean("my.path1.bool.value");
    cfg.getInt("my.path1.int.value");
    cfg.getLong("my.path1.long.value");
    cfg.getBigDecimal("my.path1.decimal.value");
  }

  private static void doTestGetPerformanceOnSubset(Cfg cfg) {
    cfg.getString("string.value");
    cfg.getBoolean("bool.value");
    cfg.getInt("int.value");
    cfg.getLong("long.value");
    cfg.getBigDecimal("decimal.value");
  }
}
