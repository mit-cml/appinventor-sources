//Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;


/**
 * Provides a checked exception for exceptions that occur during the workspace
 * loading process.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */

public class LoadException extends CodeblocksException {
  public LoadException(String message) {
    super(message);
  }

  public LoadException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public LoadException(Throwable cause) {
    super(cause);
  }
}

