// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
