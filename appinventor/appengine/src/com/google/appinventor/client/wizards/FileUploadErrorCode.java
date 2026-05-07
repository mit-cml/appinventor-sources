// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

/**
 * Enumeration used to indicate different types of upload errors.
 */
enum FileUploadErrorCode {
  /**
   * Error raised when the user attempts to upload a project as an asset.
   */
  AIA_MEDIA_ASSET,

  /**
   * Error raised when the user fails to select a file.
   */
  NO_FILE_SELECTED,

  /**
   * Error raised when the filename includes invalid characters.
   */
  MALFORMED_FILENAME,

  /**
   * Error raised when the filename is too large for the server.
   */
  FILENAME_BAD_SIZE
}
