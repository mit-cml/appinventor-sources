// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
