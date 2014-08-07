// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

/**
 * this class modelled after those in StoredData.java
 *
 * @author blu2@dons.usfca.edu (Bin Lu)
 *
 */

public class GalleryAppLikeData {
  @Id Long id;
  String userId;    // user id
  @Parent Key<GalleryAppData> galleryKey;
}