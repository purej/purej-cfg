// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Performance tests for Cfg usage.
 *
 * @author Stefan Mueller
 */
public class PerformanceApacheConfigurationTest extends AbstractPerformanceTest<Configuration> {

  @Override
  protected Configuration load(File file) throws Exception {
    return new PropertiesConfiguration(file);
  }

  @Override
  protected void readProperties(Configuration cfg) {
    cfg.getString("my.path1.string.value");
    cfg.getBoolean("my.path1.bool.value");
    cfg.getInt("my.path1.int.value");
    cfg.getLong("my.path1.long.value");
    cfg.getBigDecimal("my.path1.decimal.value");
  }

  @Override
  protected void readPropertiesOnSubset(Configuration cfg) {
    Configuration sub = cfg.subset("my.path1");
    sub.getString("string.value");
    sub.getBoolean("bool.value");
    sub.getInt("int.value");
    sub.getLong("long.value");
    sub.getBigDecimal("decimal.value");
  }
}
