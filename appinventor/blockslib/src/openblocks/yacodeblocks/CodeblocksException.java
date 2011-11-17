// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Provides a checked exception class for Codeblocks exceptions.
 *
 *
 */
public class CodeblocksException extends Exception {
  public CodeblocksException(String message){
    super(message);
  }

  public CodeblocksException(String message, Throwable cause) {
    super(message, cause);
  }

  public CodeblocksException(Throwable cause) {
    super(cause);
  }
}
