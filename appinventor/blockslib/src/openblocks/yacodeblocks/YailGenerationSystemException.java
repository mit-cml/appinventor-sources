// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
