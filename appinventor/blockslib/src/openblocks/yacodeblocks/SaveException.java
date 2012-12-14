// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
