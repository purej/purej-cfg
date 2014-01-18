// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the named functionality.
 *
 * @author Stefan Mueller
 */
public class CfgTest {

  @Test
  public void testBasics() throws Exception {
    Cfg cfg = new Cfg();
    cfg.put("a.b.c", "x1");
    cfg.put("a.b.d", "x2");
    cfg.put("a.b.d.null", (String) null);
    cfg.put("a.b.d.empty", "");
    cfg.put("a.b.e", "x3");
    cfg.put("a.c.x", "x4");
    System.out.println("Cfg.toString() --> " + cfg.toString());
    System.out.println("Cfg.subset('a.b').toString() --> " + cfg.subset("a.b").toString());

    // Test some basic methods:
    Assert.assertEquals(null, cfg.getSubsetPrefix());
    Assert.assertEquals(6, cfg.getKeys().size());
    Assert.assertEquals(false, cfg.containsKey(""));
    Assert.assertEquals(false, cfg.containsKey("xy"));
    Assert.assertEquals(true, cfg.containsKey("a.b.d.null"));
    Assert.assertEquals(true, cfg.containsKey("a.b.d.empty"));
    Assert.assertEquals(true, cfg.containsKey("a.b.c"));
    Assert.assertEquals(false, cfg.containsValue(""));
    Assert.assertEquals(false, cfg.containsValue("xy"));
    Assert.assertEquals(false, cfg.containsValue("a.b.d.null"));
    Assert.assertEquals(false, cfg.containsValue("a.b.d.empty"));
    Assert.assertEquals(true, cfg.containsValue("a.b.c"));

    // Test subset:
    Assert.assertEquals(6, cfg.subset("a").getKeys().size());
    Assert.assertEquals(5, cfg.subset("a.b").getKeys().size());
    Assert.assertEquals(2, cfg.subset("a.b.d").getKeys().size());
    Assert.assertEquals(1, cfg.subset("a.c").getKeys().size());
    Assert.assertEquals(0, cfg.subset("blub").getKeys().size());

    // Test getting a values on subset:
    Assert.assertEquals(null, cfg.getSubsetPrefix());
    Assert.assertEquals("a.b.", cfg.subset("a.b").getSubsetPrefix());
    Assert.assertEquals("x2", cfg.subset("a.b.").getString("d"));
    Assert.assertEquals("bla.bli.", cfg.subset("bla").subset("bli").getSubsetPrefix());
    Assert.assertEquals("myDefault", cfg.subset("blub").getString("y", "myDefault"));
  }

  @Test
  public void testSubstitution() throws Exception {
    // Test some direct substitutions:
    testDirectSubstitution("${my.key.1}", "Value1");
    testDirectSubstitution("${my.key.1} ${my.key.1}${my.key.1}", "Value1 Value1Value1");
    testDirectSubstitution("${my.key.1", "${my.key.1"); // Incorrect expressions are not replaced...
    testDirectSubstitution("}${my.key.1", "}${my.key.1"); // Incorrect expressions are not replaced...
    testDirectSubstitution("}${my.key.1}", "}Value1");
    testDirectSubstitution("$}${my.key.1}", "$}Value1");

    // Multi level substitution:
    Cfg cfg = new Cfg();
    cfg.put("my.key.1", "Value1");
    cfg.put("my.key.2", "${my.key.1}");
    cfg.put("my.key.3", "${my.key.2}");
    Assert.assertEquals("Value1", cfg.getString("my.key.3"));

    // Test substitution with null:
    cfg = new Cfg();
    cfg.put("my.key.null", (String) null);
    cfg.put("my.key.empty", "");
    cfg.put("my.key.x", "${my.key.null}");
    cfg.put("my.key.y", "${my.key.empty}");
    Assert.assertEquals(null, cfg.getString("my.key.x", null));
    Assert.assertEquals(null, cfg.getString("my.key.y", null));

    // Test missing substitution:
    try {
      cfg = new Cfg();
      cfg.put("my.key.1", "${my.key.missing}");
      cfg.getString("my.key.1");
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for missing subst: " + e.toString());
    }

    // Test endless loopt:
    try {
      cfg = new Cfg();
      cfg.put("my.key.1", "${my.key.2}");
      cfg.put("my.key.2", "${my.key.3}");
      cfg.put("my.key.3", "${my.key.1}");
      cfg.getString("my.key.3");
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for endlesse loop config: " + e.toString());
    }
  }

  private static void testDirectSubstitution(String value2, String resolvedValue) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("my.key.1", "Value1");
    map.put("my.key.2", value2);
    Cfg cfg = new Cfg(map);
    Assert.assertEquals(resolvedValue, cfg.getString("my.key.2"));
    Assert.assertEquals(resolvedValue, cfg.subset("my.key").getString("2"));
  }

  @Test
  public void testMerge() throws Exception {
    Cfg cfg = new Cfg();
    cfg.put("my.key.1", "Value1");
    cfg.put("my.key.2", "Value2");
    Cfg cfg2 = new Cfg();
    cfg2.put("my.key.2", "X2");
    cfg2.put("my.key.3", "X3");
    cfg.merge(cfg2);
    Assert.assertEquals("Value1", cfg.getString("my.key.1"));
    Assert.assertEquals("X2", cfg.getString("my.key.2"));
    Assert.assertEquals("X3", cfg.getString("my.key.3"));
  }

  @Test
  public void testGetTypeValuesRoot() throws Exception {
    Cfg cfg = new Cfg();
    cfg.put("string-value", " abc ");
    cfg.put("string-value-null", (String) null);
    cfg.put("string-value-empty", "");
    cfg.put("string-value-spaces", "   ");
    cfg.put("boolean-value", true);
    cfg.put("int-value", 1234);
    cfg.put("long-value", 12345678901L);
    cfg.put("decimal-value", new BigDecimal("1234.5678"));
    cfg.put("timeunit-value", TimeUnit.HOURS);
    doTestGetTypeValues(cfg);
  }

  @Test
  public void testGetTypeValuesSubset() throws Exception {
    Cfg cfg = new Cfg();
    cfg.put("my.sub.string-value", " abc ");
    cfg.put("my.sub.string-value-null", (String) null);
    cfg.put("my.sub.string-value-empty", "");
    cfg.put("my.sub.string-value-spaces", "   ");
    cfg.put("my.sub.boolean-value", "true");
    cfg.put("my.sub.int-value", "1234");
    cfg.put("my.sub.long-value", "12345678901");
    cfg.put("my.sub.decimal-value", "1234.5678");
    cfg.put("my.sub.timeunit-value", "HOURS");
    doTestGetTypeValues(cfg.subset("my.sub"));
  }

  private static void doTestGetTypeValues(Cfg c) throws Exception {
    // Test containsKey/hasValue:
    Assert.assertEquals(false, c.containsKey("not.existing"));
    Assert.assertEquals(true, c.containsKey("string-value-empty"));
    Assert.assertEquals(true, c.containsKey("string-value"));
    Assert.assertEquals(false, c.containsValue("not.existing"));
    Assert.assertEquals(false, c.containsValue("string-value-empty"));
    Assert.assertEquals(true, c.containsValue("string-value-spaces"));
    Assert.assertEquals(true, c.containsValue("string-value"));

    // Test mandatory properties:
    Assert.assertEquals(" abc ", c.getString("string-value"));
    Assert.assertEquals("   ", c.getString("string-value-spaces"));
    Assert.assertEquals(true, c.getBoolean("boolean-value"));
    Assert.assertEquals(1234, c.getInt("int-value"));
    Assert.assertEquals(12345678901L, c.getLong("long-value"));
    Assert.assertEquals(new BigDecimal("1234.5678"), c.getBigDecimal("decimal-value"));
    Assert.assertEquals(TimeUnit.HOURS, c.getEnum("timeunit-value", TimeUnit.class));

    // Test optional properties (existing - primitive):
    Assert.assertEquals(true, c.getBoolean("boolean-value", false));
    Assert.assertEquals(Integer.valueOf(1234), c.getInt("int-value", 5678));
    Assert.assertEquals(Long.valueOf(12345678901L), c.getLong("long-value", 5678956789L));

    // Test optional properties (existing - object):
    Assert.assertEquals(" abc ", c.getString("string-value", "xyz"));
    Assert.assertEquals(Boolean.TRUE, c.getBoolean("boolean-value", Boolean.FALSE));
    Assert.assertEquals(Integer.valueOf(1234), c.getInt("int-value", Integer.valueOf(5678)));
    Assert.assertEquals(Long.valueOf(12345678901L), c.getLong("long-value", Long.valueOf(5678956789L)));
    Assert.assertEquals(new BigDecimal("1234.5678"), c.getBigDecimal("decimal-value", new BigDecimal("5678.1234")));
    Assert.assertEquals(TimeUnit.HOURS, c.getEnum("timeunit-value", TimeUnit.class, TimeUnit.DAYS));

    // Test optional properties (not existing - primitive):
    Assert.assertEquals(false, c.getBoolean("boolean-x", false));
    Assert.assertEquals(Integer.valueOf(5678), c.getInt("int-x", 5678));
    Assert.assertEquals(Long.valueOf(5678956789L), c.getLong("long-x", 5678956789L));

    // Test optional properties (not existing - object):
    Assert.assertEquals("xyz", c.getString("string-x", "xyz"));
    Assert.assertEquals(Boolean.FALSE, c.getBoolean("boolean-x", Boolean.FALSE));
    Assert.assertEquals(Integer.valueOf(5678), c.getInt("int-x", Integer.valueOf(5678)));
    Assert.assertEquals(Long.valueOf(5678956789L), c.getLong("long-x", Long.valueOf(5678956789L)));
    Assert.assertEquals(new BigDecimal("5678.1234"), c.getBigDecimal("decimal-x", new BigDecimal("5678.1234")));
    Assert.assertEquals(TimeUnit.DAYS, c.getEnum("timeunit-x", TimeUnit.class, TimeUnit.DAYS));

    // Test wrong property type:
    try {
      c.getInt("string-value");
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for wrong type: " + e.toString());
    }

    // Test wrong property type:
    try {
      c.getLong("string-value");
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for wrong type: " + e.toString());
    }

    // Test wrong property type:
    try {
      c.getBigDecimal("string-value");
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for wrong type: " + e.toString());
    }

    // Test wrong property type:
    try {
      c.getEnum("string-value", TimeUnit.class);
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for wrong type: " + e.toString());
    }
  }

  @Test
  public void testErrors() throws Exception {
    try {
      new Cfg().getString("not-existing");
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for not existing string property: " + e.toString());
    }
  }

  @Test
  public void testRangeCheck() throws Exception {
    // Test min:
    Assert.assertEquals(null, Cfg.checkMin(null, 12));
    Assert.assertEquals(Integer.valueOf(12), Cfg.checkMin(12, 12));
    Assert.assertEquals(Integer.valueOf(13), Cfg.checkMin(13, 12));

    // Test max:
    Assert.assertEquals(null, Cfg.checkMax(null, 12));
    Assert.assertEquals(Integer.valueOf(12), Cfg.checkMax(12, 12));
    Assert.assertEquals(Integer.valueOf(11), Cfg.checkMax(11, 12));

    // Test minMax:
    Assert.assertEquals(null, Cfg.checkMinMax(null, -15, 15));
    Assert.assertEquals(Integer.valueOf(-15), Cfg.checkMinMax(-15, -15, 15));
    Assert.assertEquals(Integer.valueOf(-14), Cfg.checkMinMax(-14, -15, 15));
    Assert.assertEquals(Integer.valueOf(14), Cfg.checkMinMax(14, -15, 15));
    Assert.assertEquals(Integer.valueOf(14), Cfg.checkMinMax(14, -15, 15));

    try {
      Cfg.checkMin(11, 12);
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for too small value: " + e.toString());
    }

    try {
      Cfg.checkMax(13, 12);
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for too big value: " + e.toString());
    }

    try {
      Cfg.checkMinMax(13, 10, 12);
      Assert.fail();
    }
    catch (Exception e) {
      System.out.println("Expected exception for too big value: " + e.toString());
    }
  }
}
