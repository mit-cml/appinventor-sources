// Copyright 2010 Google Inc. All rights reserved.

package openblocks.yacodeblocks;

/**
 * @author kerr@google.com (Debby Wallach)
 */
public class ExternalStorageException extends Exception {
  // COV_NF_BEGIN
  public ExternalStorageException() {
  }

  public ExternalStorageException(String message) {
    super(message);
  }

  public ExternalStorageException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExternalStorageException(Throwable cause) {
    super(cause);
  }
  // COV_NF_END
}
