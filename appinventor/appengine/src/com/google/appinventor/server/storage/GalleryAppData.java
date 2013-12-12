// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;
import java.util.Date;

import javax.persistence.Id;

/**
 * this class modelled after those in StoredData.java
 *
 * @author wolberd@gmail.com (David Wolber)
 *
 */
 
public class GalleryAppData {
  // The Google Account userid
  @Id Long id;
  String title;
  String description;

  // Date app published
  @Indexed public long dateCreated;

  // Date app last updated
  @Indexed public long dateModified;

  @Indexed public int numDownloads;
  long projectId;

  // need fields for image and source, or can we build from ids?

}