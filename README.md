
<h1><img src="https://cloud.githubusercontent.com/assets/13910123/9427485/e1fecaf8-4980-11e5-86eb-905b762092b8.png"/> PureJ Cfg</h1>

## Features

  * Checked and type-safe access to configuration key/value pairs
  * Automatic substitution (resolve) of expressions of the form ${my.key} inside config values
  * Convenience functionality: Subsets, merge, range-checks

In contrary to the *Apache Commons Configuration 1 and 2* library which aims for the same goals, this library is extremly small (just 9k), has much better performance (see below) and does not require additional dependencies.

## Requisites

  * Java 1.8 or higher (tested with java 8, 11, 17)

Maven users just need to add the following dependency:

```
  <dependency>
    <groupId>com.purej</groupId>
    <artifactId>purej-cfg</artifactId>
    <version>1.3</version>
  </dependency>
```

## Usage

Create a _Cfg_ instance from various sources:
```
  Cfg cfg = new Cfg(); // New empty config
  Cfg cfg = new Cfg("myCfg.properties"); // Load from java properties resource or file
  Cfg cfg = new Cfg(System.getenv()); // Load from system environment
```

Access type-safe *mandatory* config values (throws a *CfgException* if a key or value is missing or if conversion failed):
```
  boolean myBool = cfg.getBoolean("my.mandatory.boolean.key");
  int myInt = cfg.getInt("my.mandatory.int.key");
  String myString = cfg.getString("my.mandatory.string.key");
  TimeUnit myEnum = cfg.getEnum("my.mandatory.enum.key", TimeUnit.class);
```

Access type-safe *optional* config values by providing a default for missing keys/values:
```
  boolean myBool = cfg.getBoolean("my.optional.boolean.key", Boolean.TRUE);
  int myInt = cfg.getInt("my.optional.int.key", 42);
  String myString = cfg.getString("my.optional.string.key", null);
  TimeUnit myEnum = cfg.getEnum("my.optional.enum.key", TimeUnit.class, TimeUnit.DAY);
```

Change some config values and store to a properties file:
```
  cfg.put("my.key1", 42);
  cfg.put("my.key2", "my.value2");
  cfg.store(new File("myCfg.properties"));
```

## Performance

Performance comparison to the *Apache Commons Configuration* library for a common use-case:

  * Read a properties file (which contains 20 key/value entries) from disk
  * Access properties with different types (string/bool/int/long/decimal)
  * Access the same properties over a config-subset

The times are measured as the average of 1 million tries. The test-program can be found under _src/test/java_.

|| PureJ Cfg | Apache Commons Configuration | Apache Commons Configuration |
|----|----|----|----|
|Read property file | 60 micros | 440 micros | 450 micros |
| Access single property | 0.03 micros | 0.1 micros | 0.2 micros |
| Access single property on a subset | 0.06 micros | 0.6 micros | 0.6 micros |

