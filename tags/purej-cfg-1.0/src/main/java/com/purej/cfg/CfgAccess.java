// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Provides static access (load and store) of key/value property files/resources from and to {@link Cfg} instances.
 * The data to be loaded must be in flat java properties-file format. See the {@link Properties} javadoc for more details.
 *
 * @author Stefan Mueller
 */
public final class CfgAccess {

  /**
   * Private as only static methods.
   */
  private CfgAccess() {
  }

  /**
   * Loads config key/values from the given resource or file into a newly created {@link Cfg} instance.
   *
   * @param resourceOrFile the  key/values resource or file
   * @return the newly created {@link Cfg} instance
   * @throws IOException if the file or resource could not be found or an I/O error occurred
   */
  public static Cfg load(String resourceOrFile) throws IOException {
    InputStream stream = createInputStream(resourceOrFile);
    try {
      return load(stream);
    }
    finally {
      stream.close();
    }
  }

  /**
   * Loads config key/values from the given file into a newly created {@link Cfg} instance.
   *
   * @param file the key/values file
   * @return the newly created {@link Cfg} instance
   * @throws IOException if the file could not be found or an I/O error occurred
   */
  public static Cfg load(File file) throws IOException {
    InputStream stream = new FileInputStream(file);
    try {
      return load(stream);
    }
    finally {
      stream.close();
    }
  }

  /**
   * Loads config key/values from the given stream into a newly created {@link Cfg} instance.
   *
   * @param stream the input stream to load from, will remain open after this method returns
   * @return the newly created {@link Cfg} instance
   * @throws IOException if an I/O error occurred
   */
  public static Cfg load(InputStream stream) throws IOException {
    Properties properties = new Properties();
    properties.load(stream);
    return new Cfg(properties);
  }

  /**
   * Stores config key/values to the given file. The file will be overwritten if it already exists.
   *
   * @param cfg the key/values to be written
   * @param file the file to be written to
   * @throws IOException if the file could not be written
   */
  public static void store(Cfg cfg, File file) throws IOException {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    Properties properties = new Properties();
    for (Map.Entry<String, String> entry : cfg.toMap().entrySet()) {
      properties.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
    }
    OutputStream stream = new FileOutputStream(file);
    try {
      properties.store(stream, "Saved at " + new Date());
    }
    finally {
      stream.close();
    }
  }

  private static InputStream createInputStream(String properties) throws IOException {
    // Try to get it as resource from the context class loader (most specific class loader):
    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(properties);
    if (stream != null) {
      return stream;
    }

    // If not found, try to load over this class's class loader:
    stream = CfgAccess.class.getResourceAsStream(properties);
    if (stream != null) {
      return stream;
    }

    // Try to load it as file:
    File file = new File(properties);
    if (!file.exists()) {
      throw new IOException("The file or resource '" + properties + "' does not exist!");
    }
    return new FileInputStream(file);
  }
}
