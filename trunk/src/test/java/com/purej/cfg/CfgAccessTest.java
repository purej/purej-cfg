// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Tests the named functionality.
 *
 * @author Stefan Mueller
 */
public class CfgAccessTest {

  /**
   * Tests the named functionality.
   */
  @Test
  public void testLoadFromFile() throws Exception {
    Cfg cfg = CfgAccess.load(new File("src/test/resources/test-01.properties"));
    Assert.assertEquals("value-01", cfg.getString("mykey"));

    // Load not existing:
    try {
      CfgAccess.load(new File("src/test/resources/xxx"));
    }
    catch (IOException e) {
      System.out.println("Expected exception: " + e.toString());
    }
  }

  /**
   * Tests the named functionality.
   */
  @Test
  public void testLoadFromStream() throws Exception {
    Cfg cfg = CfgAccess.load(CfgAccessTest.class.getResourceAsStream("/test-01.properties"));
    Assert.assertEquals("value-01", cfg.getString("mykey"));

    cfg = CfgAccess.load(CfgAccessTest.class.getResourceAsStream("test-03.properties"));
    Assert.assertEquals("value-03", cfg.getString("mykey"));
  }

  /**
   * Tests the named functionality.
   */
  @Test
  public void testLoadFromResource() throws Exception {
    Cfg cfg = CfgAccess.load("/test-01.properties");
    Assert.assertEquals("value-01", cfg.getString("mykey"));

    cfg = CfgAccess.load("src/test/resources/test-02.properties");
    Assert.assertEquals("value-02", cfg.getString("mykey"));

    cfg = CfgAccess.load("test-03.properties");
    Assert.assertEquals("value-03", cfg.getString("mykey"));

    // Load not existing:
    try {
      CfgAccess.load("src/test/resources/xxx");
    }
    catch (IOException e) {
      System.out.println("Expected exception: " + e.toString());
    }
  }

  /**
   * Tests the named functionality.
   */
  @Test
  public void testStore() throws Exception {
    // Create cfg & store:
    Cfg cfg = new Cfg();
    cfg.put("k1", 12);
    cfg.put("k2", true);
    cfg.put("k3", "");
    cfg.put("k4", "     ");
    cfg.put("k5", "xxx  ");
    CfgAccess.store(cfg, new File("target/tmp/tmp.properties"));

    // Read again:
    cfg = CfgAccess.load("target/tmp/tmp.properties");
    Assert.assertEquals(5, cfg.getKeys().size());
    Assert.assertEquals(12, cfg.getInt("k1"));
    Assert.assertEquals(true, cfg.getBoolean("k2"));
    Assert.assertEquals(null, cfg.getString("k3", null));
    Assert.assertEquals("     ", cfg.getString("k4"));
    Assert.assertEquals("xxx  ", cfg.getString("k5"));
  }

  /**
   * Tests the named functionality.
   */
  @Test
  public void testLoadEnv() throws Exception {
    Cfg cfg = new Cfg(System.getenv());
    System.out.println("Cfg loaded from env: " + cfg);
  }

  /**
   * Tests the named functionality.
   */
  @Test
  public void testLoadSystemProperties() throws Exception {
    Cfg cfg = new Cfg(System.getProperties());
    System.out.println("Cfg loaded from sys-properties: " + cfg);
  }
}
