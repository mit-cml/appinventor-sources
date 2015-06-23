// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Parent;

/**
 * this class modelled after those in StoredData.java
 *
 * @author wolberd@gmail.com (David Wolber)
 *
 */

public class GalleryAppReportData {
  @Id Long id;
  String reporterId;    // user who reported
  String offenderId;    // user being reported
  @Parent Key<GalleryAppData> galleryKey;
  String reportText;   // the comment

  // timestamp
  @Indexed public long dateCreated;

  Boolean resolved;
}