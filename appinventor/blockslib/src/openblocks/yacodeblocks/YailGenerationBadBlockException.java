//Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Provides a checked exception for bad block exceptions that occur during yail
 * code generation - use this subclass for system errors (not the user's fault)
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */


public class YailGenerationBadBlockException extends YailGenerationException {
    public YailGenerationBadBlockException(String message) {
      super(message);
    }

    public YailGenerationBadBlockException(String message, Throwable cause) {
      super(message, cause);
    }

}
