// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.codeblockutil;

import java.io.IOException;

/**
 * Thrown when the App Inventor directory cannot be found
 *
 * @author halabelson@google.com (Hal Abelson)
 */
public class NoAIDirectoryException extends IOException {

// This is commented out because it seems to destroy Java 5 compatibility
//   public NoAIDirectoryException(String message, Throwable cause) {
//     super(message, cause);
//   }

  public NoAIDirectoryException(String message) {
    super(message);
  }

}
