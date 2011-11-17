// Copyright 2010 Google Inc. All rights reserved.

package openblocks.yacodeblocks;

/**
 * @author kerr@google.com (Debby Wallach)
 */
public class AndroidControllerException extends Exception {
  // COV_NF_BEGIN
  public AndroidControllerException() {
  }

  public AndroidControllerException(String message) {
    super(message);
  }

  public AndroidControllerException(String message, Throwable cause) {
    super(message, cause);
  }

  public AndroidControllerException(Throwable cause) {
    super(cause);
  }
  // COV_NF_END
}
