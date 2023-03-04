// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.io.File;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;

/**
 * Performance tests for Cfg usage.
 *
 * @author Stefan Mueller
 */
public class PerformanceApacheConfiguration2Test extends AbstractPerformanceTest<Configuration> {

  @Override
  protected Configuration load(File file) throws Exception {
    // Wtf how complex to initialize! Taken from https://commons.apache.org/proper/commons-configuration/userguide/upgradeto2_0.html
    FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
        PropertiesConfiguration.class)
            .configure(new Parameters().properties().setFile(file).setThrowExceptionOnMissing(true)
                .setListDelimiterHandler(new DefaultListDelimiterHandler(';')).setIncludesAllowed(false));
    return builder.getConfiguration();
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
