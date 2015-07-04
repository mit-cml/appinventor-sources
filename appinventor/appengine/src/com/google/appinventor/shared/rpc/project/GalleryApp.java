// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;
import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * GalleryApp is the shared class holding app info. It is used by server to
 * hold apps returned from database, and its also used to hold default info
 * for an app before its published.
 */
public class GalleryApp implements IsSerializable {
  public final static long FROMSCRATCH = -1L;

  /**
   * Default constructor. This constructor is required by GWT.
   * note: don't think this is being used, can remove code within?
   */
  public GalleryApp() {
    this.title = "no title";
    this.downloads = 0;
    this.views = 0;
    this.likes = 0;
    this.unreadLikes = 0;
    this.comments = 0;
    this.projectId = FROMSCRATCH;
    this.galleryAppId = FROMSCRATCH;
    this.attributionId = FROMSCRATCH;
    this.developerId = "fred";
    this.creationDate = 0L;
    this.updateDate = 0L;
    this.active = true;
    this.moreInfo = "no info";
    this.credit = "no credit";
  }

  public static final String DEFAULTGALLERYIMAGE="images/genericApp.png";
  public static final String DEFAULTUSERIMAGE="images/android_icon_.png";

  public GalleryApp(String title, String developerId, String description,
      long creationDate, long updateDate, String imageURL, String projectName,
      int downloads, int views, int likes, int unreadLikes,
      int unreadDownloads, int comments,
      String imageBlobId, String sourceBlobId, long galleryAppId,
      ArrayList<String> tags) {
    super();
    this.title = title;
    this.developerId = developerId;
    this.description = description;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.imageURL = imageURL;
    this.attributionId = FROMSCRATCH;
    // the source file name we get from gallery can have some bad characters in it...
    // e.g., name  (2).zip. We need to cleanse this and probably deal with url as
    //   well.
    // NOTE: i think the following was for old gallery and we can eliminate? Maybe
    // even this entire constructor?
    if (projectName.contains(".")) {
      String[] splitName = projectName.split("\\.");
      projectName = splitName[0];
    }
    this.downloads = downloads;
    this.views = views;
    this.likes = likes;
    this.unreadLikes = unreadLikes;
    this.unreadDownloads = unreadDownloads;
    this.comments = comments;
    this.imageBlobId = imageBlobId;
    this.sourceBlobId = sourceBlobId;
    this.galleryAppId = galleryAppId;
    this.tags = tags;
  }

  /**
   *  this constructor is called when we are creating a new gallery app but don't have
   *  the stuff yet
   */
  public GalleryApp(String title, long projectId, String projectName, long galleryAppId, long attributionId) {
  super();
    this.title = title;
    this.description = projectName +" description";
    this.downloads = 0;
    this.views = 0;
    this.likes = 0;
    this.unreadLikes = 0;
    this.unreadDownloads = 0;
    this.comments = 0;
    this.projectId = projectId;
    this.attributionId = attributionId;
    this.projectName = projectName;
    this.galleryAppId = galleryAppId;
    this.developerId = "none";
    this.developerName = "none";
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.imageBlobId = "SFKJF";
  }


  private String title;
  private String developerId;
  private String developerName;
  private String description;
  private long creationDate;
  private long updateDate;

  private String projectName;
  private String imageURL;
  private int downloads;
  private int views;
  private int likes;
  private int unreadLikes;
  private int unreadDownloads;
  private int comments;
  private String imageBlobId;
  private String sourceBlobId;
  private long galleryAppId;
  private ArrayList<String> tags;
  private long projectId;  // when we edit a newly published app, we need the ai proj id.
  private long attributionId;  // the gallery id which this app inherits from.
                               // it takes from the project which inherits from a gellery app.
  private boolean active;
  private String moreInfo;
  private String credit;

  /**
   * @return attributionId attribution id
   */
  public long getProjectAttributionId() {
    return attributionId;
  }

  /**
   * set project attribution id as given attribution id
   * @param attributionId attribution id
   */
  public void setProjectAttributionId(long attributionId) {
    this.attributionId=attributionId;
  }

  /**
   * @return projectId project id
   */
  public long getProjectId() {
    return projectId;
  }

  /**
   * set project id as given projectid
   * @param projectId project id
   */
  public void setProjectId(long projectId) {
    this.projectId=projectId;
  }

  /**
   * @return title gallery app title
   */
  public String getTitle() {
    return title;
  }

  /**
   * set gallery app title as given title
   * @param title gallery app title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return developerId developer id
   */
  public String getDeveloperId() {
    return developerId;
  }

  /**
   * set developer id as given developerId
   * @param developerId developer id
   */
  public void setDeveloperId(String developerId) {
    this.developerId = developerId;
  }

  /**
   * @return description the description of galleryapp
   */
  public String getDescription() {
    return description;
  }

  /**
   * set description as given description
   * @param description the description of galleryapp
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return createionDate the creation date
   */
  public long getCreationDate() {
    return this.creationDate;
  }

  /**
   * set the creation date as given creationDate
   * @param creationDate the creation date
   */
  public void setCreationDate(long creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @return updateDate the update date
   */
  public long getUpdateDate() {
    return this.updateDate;
  }

  /**
   * set the update date as given updateDate
   * @param updateDate the update date
   */
  public void setUpdateDate(long updateDate) {
    this.updateDate = updateDate;
  }

  /**
   * @return imageURL the image url
   */
  public String getImageURL() {
    return imageURL;
  }

  /**
   * set the image url as given image url
   * @param imageURL image url
   */
  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

  /**
   * @return projectName project name
   */
  public String getProjectName() {
    return projectName;
  }

  /**
   * set the project name as given projectName
   * @param projectName project name
   */
  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  /**
   * @return downlads num of downloads
   */
  public int getDownloads() {
    return downloads;
  }

  /**
   * set num of downloads as given number
   * @param downloads
   */
  public void setDownloads(int downloads) {
    this.downloads = downloads;
  }

  /**
   * increase the num of downloads by 1
   */
  public void incrementDownloads() {
    this.downloads++;
  }

  /**
   * @return views num of views
   */
  public int getViews() {
    return views;
  }

  /**
   * set num of views as given number
   * @param views
   */
  public void setViews(int views) {
    this.views = views;
  }

  /**
   * @return likes num of likes
   */
  public int getLikes() {
    return likes;
  }

  /**
   * set num of likes as given number
   * @param likes
   */
  public void setLikes(int likes) {
    this.likes = likes;
  }

  /**
   * @return unreadLikes num of unread likes
   */
  public int getUnreadLikes() {
    return unreadLikes;
  }

  /**
   * set num of unreadLikes as given number
   * @param unreadLikes
   */
  public void setUnreadLikes(int unreadLikes) {
    this.unreadLikes = unreadLikes;
  }

  /**
   * @return unreadDownloads number of unread downloads
   */
  public int getUnreadDownloads() {
    return unreadDownloads;
  }

  /**
   * set num of unreadDownloads as given number
   * @param unreadDownloads
   */
  public void setUnreadDownloads(int unreadDownloads) {
    this.unreadDownloads = unreadDownloads;
  }

  /**
   * @return comments num of comments
   */
  public int getComments() {
    return comments;
  }

  /**
   * set num of comments as given number
   * @param comments
   */
  public void setComments(int comments) {
    this.comments = comments;
  }

  // I believe we can eliminate the following until next comment
  /**
   * set image blob id as given id
   * @param imageBlobId image blob id
   */
  public void setImageBlobId(String imageBlobId) {
    this.imageBlobId = imageBlobId;
  }

  /**
   * @return imageBlobId image blob id
   */
  public String getImageBlobId() {
    return this.imageBlobId;
  }

  /**
   * set source blob id as given id
   * @param sourceBlobId source blob id
   */
  public void setSourceBlobId(String sourceBlobId) {
    this.sourceBlobId = sourceBlobId;
  }

  /**
   * @return get source blob id as given id
   */
  public String getSourceBlobId() {
    return this.sourceBlobId;
  }
  // end of eliminate stuff

  /**
   * set galery app id as given id
   * @param galleryAppId
   */
  public void setGalleryAppId(long galleryAppId) {
    this.galleryAppId = galleryAppId;
  }

  /**
   * @return galleryAppId  gallery app id
   */
  public long getGalleryAppId() {
    return this.galleryAppId;
  }

  /**
   * @return developerName get developer name
   */
  public String getDeveloperName() {
    return developerName;
  }

  /**
   * set developer name as given name
   * @param name
   */
  public void setDeveloperName(String name) {
    this.developerName = name;
  }

  /**
   * check if gallery app is active
   * @return true is active, otherwise false
   */
  public boolean getActive() {
    return active;
  }

  /**
   * set active status as given boolean
   * @param active
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * @return moreInfo more info (http link)
   */
  public String getMoreInfo() {
    return moreInfo;
  }

  /**
   * set more info as given value
   * @param moreInfo
   */
  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  /**
   * @return credit the gallery app credit info
   */
  public String getCredit() {
    return credit;
  }

  /**
   * set the credit as given info
   * @param credit
   */
  public void setCredit(String credit) {
    this.credit = credit;
  }

  /**
   * @return list of app tags
   */
  public ArrayList<String> getTags() {
    return this.tags;
  }

  /**
   * set tags as given list
   * @param tags
   */
  public void setTags(ArrayList<String> tags) {
    this.tags = tags;
  }

  @Override
  public String toString() {
    return title + " ||| " + description + " ||| " +  imageURL;
  }
}