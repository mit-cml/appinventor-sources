// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Provides a checked exception for exceptions that occur during the workspace
 * saving process.
 *
 *
 */
public class SaveException extends CodeblocksException {

  public SaveException(String message) {
    super(message);
  }

  public SaveException(String message, Throwable cause) {
    super(message, cause);
  }
}
