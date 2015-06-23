// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * GalleryModerationAction is the shared class holding moderation action info. It is used by server to
 * hold moderation action returned from database
 */
public class GalleryModerationAction implements IsSerializable {
  public final static long NOTAVAILABLE = -1L;

  private long emailId;
  private long reportId;
  private long galleryId;
  private String moderatorId;
  private int actionType;
  private long date;

  private String moderatorName;
  private String emailPreview;

  public final static int SENDEMAIL = 1;
  public final static int DEACTIVATEAPP = 2;
  public final static int REACTIVATEAPP = 3;
  public final static int MARKASRESOLVED = 4;
  public final static int MARKASUNRESOLVED = 5;

  /**
   * Default constructor. This constructor is required by GWT.
   */
  public GalleryModerationAction() {

  }

  /**
   * Constructor of GalleryModerationAction
   * @param reportId report id
   * @param galleryId gallery id
   * @param emailId email id
   * @param moderatorId moderator id
   * @param actionType action type
   * @param moderatorName moderator name
   * @param emailPreview email preview
   * @param date creation date
   */
  public GalleryModerationAction(long reportId, long galleryId, long emailId, String moderatorId,
      int actionType, String moderatorName, String emailPreview, long date) {
    this.reportId = reportId;
    this.galleryId = galleryId;
    this.emailId = emailId;
    this.moderatorId = moderatorId;
    this.actionType = actionType;
    this.moderatorName = moderatorName;
    this.emailPreview = emailPreview;
    this.date = date;
  }

  /**
   * set the date as given date
   * @param date creation date
   */
  public void setDate(long date){
    this.date = date;
  }

  /**
   * @return date creation date
   */
  public long getDate(){
    return date;
  }

  /**
   * set report id as given id
   * @param reportId report id
   */
  public void setReportId(long reportId){
    this.reportId = reportId;
  }

  /**
   * @return reportId report id
   */
  public long getReportId(){
    return reportId;
  }

  /**
   * set gallery id as given id
   * @param galleryId gallery id
   */
  public void setGalleryId(long galleryId){
    this.galleryId = galleryId;
  }

  /**
   * @return galleryId gallery id
   */
  public long getGalleryId(){
    return galleryId;
  }

  /**
   * set email id as given id
   * @param emailId email id
   */
  public void setEmailId(long emailId){
    this.emailId = emailId;
  }

  /**
   * @return emailId email id
   */
  public long getMesaageId(){
    return emailId;
  }

  /**
   * set moderator id as given id
   * @param moderatorId moderator id
   */
  public void setModeratorId(String moderatorId){
    this.moderatorId = moderatorId;
  }

  /**
   * @return get moderator id as given id
   */
  public String getModeratorId(){
    return moderatorId;
  }

  /**
   * set action type as given type
   * @param actionType action type
   */
  public void setActionType(int actionType){
    this.actionType = actionType;
  }

  /**
   * @return actionType action type
   */
  public int getActonType(){
    return actionType;
  }

  /**
   * set moderator name as given name
   * @param moderatorName moderator name
   */
  public void setModeratorName(String moderatorName){
    this.moderatorName = moderatorName;
  }

  /**
   * @return moderatorName moderator name
   */
  public String getModeratorName(){
    return moderatorName;
  }

  /**
   * set email preview as given preview
   * @param emailPreview email preview
   */
  public void setMessagePreview(String emailPreview){
    this.emailPreview = emailPreview;
  }

  /**
   * @return emailPreview email preview
   */
  public String getEmailPreview(){
    return emailPreview;
  }
}