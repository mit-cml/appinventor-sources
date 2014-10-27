// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GalleryCommentReport implements IsSerializable{

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private GalleryCommentReport() {

  }

  /**
   * Constructor of GalleryCommentReport
   * @param commentId comment id
   * @param userId user id
   * @param report report body
   * @param timeStamp time stamp
   */
  public GalleryCommentReport(long commentId, String userId, String report,long timeStamp ) {
    super();
    this.commentId = commentId;
    this.timeStamp = timeStamp;
    this.report = report;
    this.userId = userId;
  }

  /**
   * @return commentId comment id
   */
  public long getCommentId() {
    return commentId;
  }

  /**
   * set commentid as given commentId
   * @param commentId comment id
   */
  public void setCommentID(long commentId) {
    this.commentId = commentId;
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
   * @return report comment report
   */
  public String getReport() {
    return report;
  }

  /**
   * set comment report as given report
   * @param report comment report
   */
  public void setReport(String report) {
    this.report = report;
  }

  /**
   * @return userId user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * set use id as given userId
   * @param userId user id
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
   * set user name as given name
   * @param name username
   */
  public void setUserName(String name) {
    this.userName = name;
  }

  // Here are the data members
  private long commentId;
  private long timeStamp;
  private String report;
  private String userId;
  private String userName;

  @Override
  public String toString() {
    return report;
  }

}
