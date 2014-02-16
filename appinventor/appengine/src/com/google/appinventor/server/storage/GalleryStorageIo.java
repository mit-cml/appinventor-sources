// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;


import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.GalleryAppReport;
import com.google.appinventor.shared.rpc.project.GalleryCommentReport;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;



/**
 * Interface of methods to simplify access to the gallery storage systems.
 *
 * In all of the methods below that take a user id, it should be a string
 * that uniquely identifies the logged-in user and will continue to do so
 * indefinitely. It is up to the caller to choose the source of user ids.
 *
 */
public interface GalleryStorageIo {
  
  /**
   * Returns a gallery app
   * @param galleryId id of gallery app you want
   * @return a {@link GalleryApp} for gallery App
   */
  GalleryApp getGalleryApp(long galleryId);

  /**
   * creates a new gallery app
   * @param title title of new app
   * @param projectName name of new app's aia file
   * @param description description of new app
   * @param projectId id of the project being published to gallery
   * @param userId if of user publishing this app
   * @return a {@link GalleryApp} for gallery App
   */
  GalleryApp createGalleryApp(String title, String projectName, String description, long projectId, String userId);

  /**
   * updates gallery app
   * @param galleryId id of app being updated
   * @param title new title of  app
   * @param description new description of app
   * @param userId if of user publishing this app
   */
  void updateGalleryApp(long galleryId, String title, String description, String userId);


  /**
   * Returns an array of recently published GalleryApps
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  List<GalleryApp> getRecentGalleryApps(int start, int count);
   
  /**
   * Returns an array of most downloaded GalleryApps
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  List<GalleryApp> getMostDownloadedApps(int start, int count);

  /**
   * Returns a list of apps created by a particular developer
   * @param userId id of developer
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  List<GalleryApp> getDeveloperApps(String userId, int start, int count);

  /**
   * records that an app has been downloaded
   * @param galleryId the id of gallery app that was downloaded
   */
  void incrementDownloads(long galleryId);

  /**
   * deletes an app
   * @param galleryId the id of gallery app to be deleted
   */
  void deleteApp(long galleryId);

  /**
   * adds a comment to a gallery app
   * @param galleryId id of gallery app that was commented on
   * @param userId id of user who commented
   * @param comment comment
   * @return the id of the new comment
   */
  long addComment(long galleryId,String userId, String comment);

  /**
   * increase likes to a gallery app
   * @param galleryId id of gallery app that was like
   * @param userId id of user who likes it
   * @return the id of the new like
   */
  int increaseLikes(long galleryId,String userId);

  /**
   * decrease likes to a gallery app
   * @param galleryId id of gallery app that was like
   * @param userId id of user who likes it
   * @return the id of the new like
   */
  int decreaseLikes(long galleryId,String userId);
  /**
   * Returns the num of likes for an app
   * @param galleryId id of gallery app
   * @return num like of a gallery app
   */
  int getNumLikes(long galleryId);
  /**
   * check if an app is liked by a user
   * @param galleryId id of gallery app that was like
   * @param userId id of user who likes it
   * @return true if relation exists
   */
  boolean isLikedByUser(long galleryId,String userId);
  /**
   * Returns a list of comments for an app
   * @param galleryId id of gallery app
   * @return list of {@link GalleryComment}
   */
  List<GalleryComment> getComments(long galleryId);

  /**
   * adds a report (flag) to a gallery app
   * @param galleryId id of gallery app that was commented on
   * @param userId id of user who commented
   * @param report report
   * @return the id of the new report
   */
  long addAppReport(long galleryId,String userId, String report);
  /**
   * Returns a list of reports (flags) for an app
   * @param galleryId id of gallery app
   * @return list of {@link GalleryAppReport}
   */
  List<GalleryAppReport> getAppReports(long galleryId);

  /**
   * Returns a list of reports (flags) for all app
   * @return list of {@link GalleryAppReport}
   */
  List<GalleryAppReport> getAppReports();
  /**
   * adds a report (flag) to a gallery app comment
   * @param commentId id of comment that was reported
   * @param userId id of user who commented
   * @param report report
   * @return the id of the new report
   */
  long addCommentReport(long commentId,String userId, String report);
  /**
   * Returns a list of reports (flags) for a comment
   * @param commentId id of comment
   * @return list of {@link GalleryCommentReport}
   */
  List<GalleryCommentReport> getCommentReports(long commentId);
  /**
   * Returns a list of reports (flags) for all comments
   * @return list of {@link GalleryCommentReport}
   */
  List<GalleryCommentReport> getCommentReports();

  
}
