// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.io.File;

/**
 * Performance tests for Cfg usage.
 *
 * @author Stefan Mueller
 */
public class PerformanceCfgTest extends AbstractPerformanceTest<Cfg> {

  @Override
  protected Cfg load(File file) {
    return new Cfg(file);
  }

  @Override
  protected void readProperties(Cfg cfg) {
    cfg.getString("my.path1.string.value");
    cfg.getBoolean("my.path1.bool.value");
    cfg.getInt("my.path1.int.value");
    cfg.getLong("my.path1.long.value");
    cfg.getBigDecimal("my.path1.decimal.value");
  }

  @Override
  protected void readPropertiesOnSubset(Cfg cfg) {
    Cfg sub = cfg.subset("my.path1");
    sub.getString("string.value");
    sub.getBoolean("bool.value");
    sub.getInt("int.value");
    sub.getLong("long.value");
    sub.getBigDecimal("decimal.value");
  }
}
