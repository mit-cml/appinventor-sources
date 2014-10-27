// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;
/**
 * GalleryComment is a shared object. The db puts comments in it and slings it back
 * to the view
 */
public class GalleryComment implements IsSerializable{

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private GalleryComment() {
  }

  /**
   * This constructor was used in the old system, in conjunction with
   * the old gallery api. I believe we can abolish this.
   */
  public GalleryComment(long appId, long timeStamp, String comment,
      int numCurFlags, String userId, String treeId, int numChildren) {
    super();
    this.appId = appId;
    this.timeStamp = timeStamp;
    this.comment = comment;
    this.numCurFlags = numCurFlags;
    this.userId = userId;
    this.treeId = treeId;
    this.numChildren = numChildren;
  }

  /**
   * Constructor of GalleryComment
   * @param appId galleryapp id
   * @param userId user id
   * @param comment comment body
   * @param timeStamp time stamp
   */
  public GalleryComment(long appId, String userId, String comment,long timeStamp ) {
    super();
    this.appId = appId;
    this.timeStamp = timeStamp;
    this.comment = comment;
    this.userId = userId;
    this.numCurFlags = 0;
    this.treeId = "treeId";
    this.numChildren = 0;
  }

  /**
   * @return appId app id
   */
  public long getAppId() {
    return appId;
  }

  /**
   * set app id as given appId
   * @param appId app id
   */
  public void setAppID(long appId) {
    this.appId = appId;
  }

  /**
   * @return timeStamp time stamp
   */
  public long getTimeStamp() {
    return timeStamp;
  }

  /**
   * set time stamp as given timeStamp
   * @param timeStamp time stamp
   */
  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  /**
   * @return comment comment body
   */
  public String getComment() {
    return comment;
  }

  /**
   * set comment as given comment
   * @param comment comment body
   */
  public void setText(String comment) {
    this.comment = comment;
  }

  /**
   * @return userId get user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * set user id as given userId
   * @param userId
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return userName user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * set user name as given username
   * @param name
   */
  public void setUserName(String name) {
    this.userName = name;
  }

  /**
   * @return numCurFlags num of curFlags
   */
  public int getNumCurFlags() {
    return numCurFlags;
  }

  /**
   * set num of curflags as given num
   * @param numCurFlags
   */
  public void setNumCurFlags(int numCurFlags) {
    this.numCurFlags = numCurFlags;
  }

// we are implementing a flat comment list to start. If we add replies we may want
// some tree structure as below (this came from old gallery api client)

  public String getTreeId() {
    return treeId;
  }

  public void setTreeId(String treeId) {
    this.treeId = treeId;
  }

  public int getNumChildren() {
    return numChildren;
  }

  public void setNumChildren(int numChildren) {
    this.numChildren = numChildren;
  }

  // Here are the data members
  private long appId;
  private long timeStamp;
  private String comment;
  private String userId;
  private String userName;
  private int numCurFlags;

  private String treeId;
  private int numChildren;

  @Override
  public String toString() {
    return comment;
  }

}
