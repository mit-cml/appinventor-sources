// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.codeblockutil;

import java.io.IOException;

/**
 * Thrown when the ADB command cannot be found
 *
 * @author halabelson@google.com (Hal Abelson)
 */
public class NoAdbException extends IOException {

// This is commented out because it seems to destroy Java 5 compatibility
//   public NoAdbException(String message, Throwable cause) {
//     super(message, cause);
//   }

  public NoAdbException(String message) {
    super(message);
  }

}
