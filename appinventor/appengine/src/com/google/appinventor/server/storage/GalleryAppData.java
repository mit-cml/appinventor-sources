// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Indexed;

/**
 * this class modelled after those in StoredData.java
 *
 * @author wolberd@gmail.com (David Wolber)
 *
 */
public class GalleryAppData {
  @Id Long id;
  String title;        // user entered, can have spaces
  String projectName;  // we keep this as the .aia file name
  String description;  // user entered
  String moreInfo;     // user entered
  String credit;       // user entered

  // Date app published
  @Indexed public long dateCreated;
  // Date app last updated
  @Indexed public long dateModified;
  @Indexed public int numLikes;
  @Indexed public int numDownloads;
  @Indexed public int unreadLikes;
  @Indexed public int unreadDownloads;
  long projectId;
  int status;
  long lastEmailNotificationTimeStamp;

  @Indexed
  String userId;

  boolean active; //if false, app will be hided.

}