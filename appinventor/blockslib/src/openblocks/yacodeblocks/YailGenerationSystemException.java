//Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Provides a checked exception for exceptions that occur during yail
 * code generation - use this subclass for system errors (not the user's fault)
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */

public class YailGenerationSystemException extends YailGenerationException {
  public YailGenerationSystemException(String message) {
    super(message);
  }

  public YailGenerationSystemException(String message, Throwable cause) {
    super(message, cause);
  }

}
