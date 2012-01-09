//Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Provides a checked exception for exceptions that occur during yail
 * code generation
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */

public class YailGenerationException extends CodeblocksException {
  public YailGenerationException(String message) {
    super(message);
  }

  public YailGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
}
