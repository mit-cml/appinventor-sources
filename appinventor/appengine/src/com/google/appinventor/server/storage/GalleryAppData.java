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
  @Id Long id;
  String title;   // user entered, can have spaces
  String projectName;  // we keep this as the .aia file name
  String description;

  // Date app published
  @Indexed public long dateCreated;
  // Date app last updated
  @Indexed public long dateModified;
  @Indexed public int numDownloads;
  @Indexed public int unreadLikes;
  @Indexed public int unreadDownloads;
  long projectId;
  int status;

  @Indexed
  String userId;

  boolean active; //if false, app will be hided.

}