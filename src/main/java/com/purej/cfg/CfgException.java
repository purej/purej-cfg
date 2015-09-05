// Copyright (c), 2009, adopus consulting GmbH Switzerland, all rights reserved.
package com.purej.cfg;

/**
 * This exception will be thrown if a mandatory configuration value is missing or conversion
 * into the specified type failed.
 *
 * @author Stefan Mueller
 */
public final class CfgException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new instance of this class with a specified exception message.
   */
  public CfgException(String message) {
    super(message);
  }

  /**
   * Creates a new instance of this class with the specified exception message and cause.
   */
  public CfgException(String message, Exception cause) {
    super(message, cause);
  }
}
