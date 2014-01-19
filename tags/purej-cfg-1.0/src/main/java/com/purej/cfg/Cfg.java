// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class holds some configuration stored as string key/value pairs, provides typed access and supports automatic substitution
 * of expressions of the form ${lookup.key} inside config values
 * <p/>
 * Typically, this class is used in conjunction with a java properties file. See the {@link CfgAccess} helper methods to load and
 * store a {@link Cfg} from and to a java properties file.
 * <p/>
 * <strong>Note that this implementation is not synchronized.</strong> If multiple threads access this config concurrently,
 * and at least one of the threads adds or deletes config keys, it <i>must</i> be synchronized externally!
 *
 * @author Stefan Mueller
 */
public class Cfg {
  private final Map<String, String> _map;
  private final String _subsetPrefix;

  /**
   * Creates a new instance with the given state - only for internal usage.
   */
  private Cfg(Map<String, String> map, String subsetPrefix) {
    _map = map;
    _subsetPrefix = subsetPrefix;
  }

  /**
   * Creates a new instance of this class using an empty key/value map.
   */
  public Cfg() {
    _map = new HashMap<String, String>();
    _subsetPrefix = null;
  }

  /**
   * Creates a new instance of this class using the provided property map.
   * <p/>
   * Note: The keys and values of the given map will both be converted to strings.
   * An empty value (eg. string with length 0) is interpreted as null value.
   *
   * @param keyValues the map of key/value pairs
   */
  public Cfg(Map<?, ?> keyValues) {
    _map = new HashMap<String, String>((int) (keyValues.size() / 0.7) + 1);
    _subsetPrefix = null;
    for (Map.Entry<?, ?> e : keyValues.entrySet()) {
      if (e.getKey() == null) {
        throw new CfgException("Key must not be null!");
      }
      _map.put(e.getKey().toString(), e.getValue() != null ? e.getValue().toString() : null);
    }
  }

  /**
   * Checks the given value to be not smaller then the specified min-value.
   *
   * @param value the value to be checked
   * @param minValue the min-value to check against
   * @return the passed value
   * @throws CfgException if the given value is smaller then the specified min-value.
   */
  public static <T extends Comparable<T>> T checkMin(T value, T minValue) throws CfgException {
    if (value != null && minValue.compareTo(value) > 0) {
      throw new CfgException("Value '" + value + "' is smaller then allowed min-value '" + minValue + "'");
    }
    return value;
  }

  /**
   * Checks the given value to be not bigger then the specified max-value.
   *
   * @param value the value to be checked
   * @param maxValue the max-value to check against
   * @return the passed value
   * @throws CfgException if the given value is bigger then the specified max-value.
   */
  public static <T extends Comparable<T>> T checkMax(T value, T maxValue) throws CfgException {
    if (value != null && maxValue.compareTo(value) < 0) {
      throw new CfgException("Value '" + value + "' is bigger then allowed max-value '" + maxValue + "'");
    }
    return value;
  }

  /**
   * Checks the given value to be not smaller and not bigger then the specified min/max-values.
   *
   * @param value the value to be checked
   * @param minValue the min-value to check against
   * @param maxValue the max-value to check against
   * @return the passed value
   * @throws CfgException if the given value is smaller or bigger then the specified min/max-values.
   */
  public static <T extends Comparable<T>> T checkMinMax(T value, T minValue, T maxValue) throws CfgException {
    return checkMax(checkMin(value, minValue), maxValue);
  }

  /**
   * Returns the subset-prefix of this config instance.
   * @return the subset prefix or null if this config is no subset (eg. root)
   */
  public String getSubsetPrefix() {
    return _subsetPrefix;
  }

  /**
   * Returns a decorator config instance that contains all key/value pairs from this configuration where the key starts with the given subset-prefix.
   * The given subset-prefix must match to config key parts separated by dots.
   * <p/>
   * Note: When accessing the returned subset config, the subset-prefix does NOT need to be specified anymore to lookup values.
   * <p/>
   * Note: The returned subset contains just a reference (and not a copy) of this config's key/value pairs. So modifications to the subset are
   * reflected in this config and the other way round.
   *
   * @param subsetPrefix the subset-prefix, must match to a dot-separatable part
   * @return the config subset with an underlying reference to this config
   */
  public Cfg subset(String subsetPrefix) {
    String sub = subsetPrefix.endsWith(".") ? subsetPrefix : subsetPrefix + ".";
    return new Cfg(_map, toKey(sub));
  }

  /**
   * Returns the list of keys of this config instance.
   */
  public Set<String> getKeys() {
    if (_subsetPrefix == null) {
      return new HashSet<String>(_map.keySet());
    }
    Set<String> result = new HashSet<String>();
    for (String key : _map.keySet()) {
      if (key.startsWith(_subsetPrefix)) {
        result.add(key.substring(_subsetPrefix.length()));
      }
    }
    return result;
  }

  /**
   * Merges all key/value pairs from the specified config to this config
   * and overwrites eventually existing key/value pairs.
   * <p/>
   * Note that only config instances without a subset (eg. root configs) can be merged this way.
   * @throws CfgException if this or the specified config instance is a subset
   */
  public void merge(Cfg cfg) throws CfgException {
    if (_subsetPrefix != null || cfg._subsetPrefix != null) {
      throw new CfgException("Only root level configs can be merged (no subsets)!");
    }
    _map.putAll(cfg._map);
  }

  /**
   * Returns whether or not this config contains the given key.
   * @param key the key
   * @return true, if the key exists, false otherwise
   */
  public boolean containsKey(String key) {
    return _map.containsKey(toKey(key));
  }

  /**
   * Returns whether or not this config has an associated non-null and non-empty value for the given key.
   * @param key the key
   * @return true, if the key exists and there is a value that is not null and not empty, false otherwise
   */
  public boolean containsValue(String key) {
    String value = resolve(toKey(key));
    return value != null && value.length() > 0;
  }

  /**
   * Returns a newly created map with all key/value pairs of this config instance.
   */
  public Map<String, String> toMap() {
    return new HashMap<String, String>(_map);
  }

  /**
   * Returns the mandatory config value for the given key as boolean.
   * If the config value is not mandatory, use the {@link #getBoolean(String, Boolean)} method.
   *
   * @param key the config key
   * @return true if the configured value is equal, ignoring case, to the string {@code "true"} - false otherwise
   * @throws CfgException if no value for the given key exists
   */
  public boolean getBoolean(String key) throws CfgException {
    return checkNotNull(key, getBoolean(key, null)).booleanValue();
  }

  /**
   * Returns the optional config value for the given key as {@link Boolean}.
   * If the given key does not map to an existing config value, the specified default value is returned instead.
   *
   * @param key the config key
   * @param defaultValue the default value in case of a missing a config value
   * @return the configured value or the specified default value
   */
  public Boolean getBoolean(String key, Boolean defaultValue) throws CfgException {
    String value = getString(key, null);
    return value != null ? Boolean.valueOf(value) : defaultValue;
  }

  /**
   * Returns the mandatory config value for the given key as int.
   * If the config value is not mandatory, use the {@link #getInt(String, Integer)} method.
   *
   * @param key the config key
   * @return the configured int value
   * @throws CfgException if no value for the given key exists or the value could not be converted to an int
   */
  public int getInt(String key) throws CfgException {
    return checkNotNull(key, getInt(key, null)).intValue();
  }

  /**
   * Returns the optional config value for the given key as {@link Integer}.
   * If the given key does not map to an existing config value, the specified default value is returned instead.
   *
   * @param key the config key
   * @param defaultValue the default value in case of a missing a config value
   * @return the configured value or the specified default value
   * @throws CfgException if the value could not be converted to an {@link Integer}
   */
  public Integer getInt(String key, Integer defaultValue) throws CfgException {
    String value = getString(key, null);
    try {
      return value != null ? Integer.valueOf(value) : defaultValue;
    }
    catch (NumberFormatException e) {
      throw new CfgException("Value '" + value + "' for key '" + key + "' is no valid int!");
    }
  }

  /**
   * Returns the mandatory config value for the given key as long.
   * If the config value is not mandatory, use the {@link #getLong(String, Long)} method.
   *
   * @param key the config key
   * @return the configured long value
   * @throws CfgException if no value for the given key exists or the value could not be converted to a long
   */
  public long getLong(String key) throws CfgException {
    return checkNotNull(key, getLong(key, null)).longValue();
  }

  /**
   * Returns the optional config value for the given key as {@link Long}.
   * If the given key does not map to an existing config value, the specified default value is returned instead.
   *
   * @param key the config key
   * @param defaultValue the default value in case of a missing a config value
   * @return the configured value or the specified default value
   * @throws CfgException if the value could not be converted to a {@link Long}
   */
  public Long getLong(String key, Long defaultValue) throws CfgException {
    String value = getString(key, null);
    try {
      return value != null ? Long.valueOf(value) : defaultValue;
    }
    catch (NumberFormatException e) {
      throw new CfgException("Value '" + value + "' for key '" + key + "' is no valid long!");
    }
  }

  /**
   * Returns the mandatory config value for the given key as {@link BigDecimal}.
   * If the config value is not mandatory, use the {@link #getBigDecimal(String, BigDecimal)} method.
   *
   * @param key the config key
   * @return the configured value, never null
   * @throws CfgException if no value for the given key exists or the value could not be converted to a {@link BigDecimal}
   */
  public BigDecimal getBigDecimal(String key) throws CfgException {
    return checkNotNull(key, getBigDecimal(key, null));
  }

  /**
   * Returns the optional config value for the given key as {@link BigDecimal}.
   * If the given key does not map to an existing config value, the specified default value is returned instead.
   *
   * @param key the config key
   * @param defaultValue the default value in case of a missing a config value
   * @return the configured value or the specified default value
   * @throws CfgException if the value could not be converted to a {@link BigDecimal}
   */
  public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) throws CfgException {
    String value = getString(key, null);
    try {
      return value != null ? new BigDecimal(value) : defaultValue;
    }
    catch (NumberFormatException e) {
      throw new CfgException("Value '" + value + "' for key '" + key + "' is no valid BigDecimal!");
    }
  }

  /**
   * Returns the mandatory config value for the given key as enum.
   * If the config value is not mandatory, use the {@link #getEnum(String, Class, Enum)} method.
   *
   * @param key the config key
   * @return the configured value, never null
   * @throws CfgException if no value for the given key exists or the value could not be converted to the specified enum type
   */
  public <T extends Enum<T>> T getEnum(String key, Class<T> type) throws CfgException {
    return checkNotNull(key, getEnum(key, type, null));
  }

  /**
   * Returns the optional config value for the given key as enum.
   * If the given key does not map to an existing config value, the specified default value is returned instead.
   *
   * @param key the config key
   * @param defaultValue the default value in case of a missing a config value
   * @return the configured value or the specified default value
   * @throws CfgException if the value could not be converted to the specified enum type
   */
  public <T extends Enum<T>> T getEnum(String key, Class<T> type, T defaultValue) throws CfgException {
    String value = getString(key, null);
    try {
      return value != null ? Enum.valueOf(type, value) : defaultValue;
    }
    catch (Exception e) {
      throw new CfgException("Value '" + value + "' for key '" + key + "' is no valid Enum for '" + type + "'!");
    }
  }

  /**
   * Returns the mandatory config value for the given key as string.
   * If the config value is not mandatory, use the {@link #getString(String, String)} method.
   *
   * @param key the config key
   * @return the configured value, never null
   * @throws CfgException if no value for the given key exists
   */
  public String getString(String key) throws CfgException {
    return checkNotNull(key, getString(key, null));
  }

  /**
   * Returns the optional config value for the given key as string.
   * If the given key does not map to an existing config value, the specified default value is returned instead.
   *
   * @param key the config key
   * @param defaultValue the default value in case of a missing a config value
   * @return the configured value or the specified default value
   */
  public String getString(String key, String defaultValue) {
    String value = resolve(toKey(key));
    return value != null && value.length() > 0 ? value : defaultValue;
  }

  /**
   * Sets the key/value pair to this config and overwrites an eventually already existing pair.
   *
   * @param key the key to be set, must not be null
   * @param value the value to be set
   */
  public void put(String key, boolean value) {
    put(key, String.valueOf(value));
  }

  /**
   * Sets the key/value pair to this config and overwrites an eventually already existing pair.
   *
   * @param key the key to be set, must not be null
   * @param value the value to be set
   */
  public void put(String key, int value) {
    put(key, String.valueOf(value));
  }

  /**
   * Sets the key/value pair to this config and overwrites an eventually already existing pair.
   *
   * @param key the key to be set, must not be null
   * @param value the value to be set
   */
  public void put(String key, long value) {
    put(key, String.valueOf(value));
  }

  /**
   * Sets the key/value pair to this config and overwrites an eventually already existing pair.
   *
   * @param key the key to be set, must not be null
   * @param value the value to be set, might be null
   */
  public void put(String key, BigDecimal value) {
    put(key, value != null ? value.toPlainString() : null);
  }

  /**
   * Sets the key/value pair to this config and overwrites an eventually already existing pair.
   *
   * @param key the key to be set, must not be null
   * @param value the value to be set, might be null
   */
  public void put(String key, Enum<?> value) {
    put(key, value != null ? value.name() : null);
  }

  /**
   * Sets the key/value pair to this config and overwrites an eventually already existing pair.
   *
   * @param key the key to be set, must not be null
   * @param value the value to be set, might be null
   */
  public void put(String key, String value) {
    _map.put(toKey(key), value);
  }

  /**
   * Removes the given key from this config.
   *
   * @param key the key to be removed
   */
  public void remove(String key) {
    _map.remove(toKey(key));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Cfg");
    if (_subsetPrefix != null) {
      builder.append("(subset=").append(_subsetPrefix).append(")");
    }
    builder.append("[");
    boolean comma = false;
    List<String> keys = new ArrayList<String>(getKeys());
    Collections.sort(keys);
    for (String key : keys) {
      if (comma) {
        builder.append(", ");
      }
      builder.append(key).append('=').append(_map.get(toKey(key)));
      comma = true;
    }
    builder.append("]");
    return builder.toString();
  }

  private String toKey(String key) {
    if (key == null) {
      throw new CfgException("Key must not be null!");
    }
    return _subsetPrefix != null ? _subsetPrefix + key : key;
  }

  private <T> T checkNotNull(String key, T value) {
    if (value == null) {
      String suffix = _subsetPrefix != null ? " in subset '" + _subsetPrefix + "'!" : "!";
      throw new CfgException("No value configured for key '" + key + "'" + suffix);
    }
    return value;
  }

  /**
   * Internal lookup that automatically replaces expressions of the form ${lookup.key} inside config values.
   */
  private String resolve(String key) {
    String value = _map.get(key);
    if (value != null) {
      // We replace values till nothing is found anymore (allows for transitive replaces):
      Set<String> uniqueSet = new HashSet<String>();
      while (value.length() > 0) {
        int preIdx = value.indexOf("${");
        if (preIdx == -1) {
          break;
        }
        int postIdx = value.indexOf('}', preIdx);
        if (postIdx == -1) {
          break;
        }
        String substKey = value.substring(preIdx + 2, postIdx);
        if (!_map.containsKey(substKey)) {
          throw new CfgException("The substitution key '" + substKey + "' does not exist!");
        }
        String substValue = _map.get(substKey);
        value = value.substring(0, preIdx) + (substValue == null ? "" : substValue) + value.substring(postIdx + 1);
        if (!uniqueSet.add(value)) {
          throw new CfgException("Key '" + key + "' leads to a circular, non-resolvable substitution!");
        }
      }
    }
    return value;
  }
}
