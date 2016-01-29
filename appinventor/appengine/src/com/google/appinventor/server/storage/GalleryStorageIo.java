// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;


import java.util.List;

import com.google.appinventor.shared.rpc.project.Email;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryAppReport;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.GalleryCommentReport;
import com.google.appinventor.shared.rpc.project.GalleryModerationAction;
import com.google.appinventor.shared.rpc.project.GalleryReportListResult;



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
  GalleryApp createGalleryApp(String title, String projectName, String description, String moreInfo, String credit, long projectId, String userId);

  /**
   * updates gallery app
   * @param galleryId id of app being updated
   * @param title new title of  app
   * @param description new description of app
   * @param userId if of user publishing this app
   */
  void updateGalleryApp(long galleryId, String title, String description, String moreInfo, String credit, String userId);

  /**
   * Returns total number of GalleryApps
   * @return number of GalleryApps
   */
  Integer getNumGalleryApps();

  /**
   * Returns a wrapped class which contains list of most recently
   * updated galleryApps and total number of results in database
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  GalleryAppListResult getRecentGalleryApps(int start, int count);

  /**
   * Returns a wrapped class which contains a list of most downloaded
   * gallery apps and total number of results in database
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  GalleryAppListResult getMostDownloadedApps(int start, int count);

  /**
   * Returns a wrapped class which contains a list of most liked
   * gallery apps and total number of results in database
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  GalleryAppListResult getMostLikedApps(int start, int count);

  /**
   *Returns a wrapped class which contains a list of featured gallery app
   * @param start start index
   * @param count count number
   * @return list of gallery app
   */
  GalleryAppListResult getFeaturedApp(int start, int count);

   /**
   *Returns a wrapped class which contains a list of tutorial gallery app
   * @param start start index
   * @param count count number
   * @return list of gallery app
   */
  GalleryAppListResult getTutorialApp(int start, int count);

  /**
   * check if app is featured already
   * @param galleryId gallery id
   * @return true if featured, otherwise false
   */
  boolean isFeatured(long galleryId);

  /**
   * check if app is tutorial already
   * @param galleryId gallery id
   * @return true if tutorial, otherwise false
   */
  boolean isTutorial(long galleryId);

  /**
   * mark an app as featured
   * @param galleryId gallery id
   * @return
   */
  boolean markAppAsFeatured(long galleryId);

  /**
   * mark an app as tutorial
   * @param galleryId gallery id
   * @return
   */
  boolean markAppAsTutorial(long galleryId);

  /**
   * Returns a wrapped class which contains a list of galleryApps
   * by a particular developer and total number of results in database
   * @param userId id of developer
   * @param start starting index of apps you want
   * @param count number of apps you want
   * @return list of {@link GalleryApp}
   */
  GalleryAppListResult getDeveloperApps(String userId, int start, int count);

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
   * salvage the gallery app by given galleryId
   * @param galleryId id of gallery app
   */
  void salvageGalleryApp(long galleryId);

  /**
   * save AttributionId
   * @param galleryId id of gallery app that was like
   * @param attributionId id of project's attribution
   * @return the id of attribution info
   */
  long saveAttribution(long galleryId, long attributionId);
  /**
   * get the AttributionId
   * @param galleryId id of gallery app that was like
   * @return the attribution id
   */
  long remixedFrom(long galleryId);
  /**
   * get the list of Children Gallery App
   * @param galleryId id of gallery app that was like
   * @return list of Children Gallery App
   */
  List<GalleryApp> remixedTo(long galleryId);
  /**
   * mark an report as resolved
   * @param reportId the id of the app
   */
  boolean markReportAsResolved(long reportId, long galleryId);
  /**
   * deactivate app
   * @param galleryId the id of the gallery app
   */
  boolean deactivateGalleryApp(long galleryId);
  /**
   * check if gallery app is activated
   * @param galleryId the id of the gallery app
   */
  boolean isGalleryAppActivated(long galleryId);
  /**
   * Returns a list of comments for an app
   * @param galleryId id of gallery app
   * @return list of {@link GalleryComment}
   */
  List<GalleryComment> getComments(long galleryId);

  /**
   * adds a report (flag) to a gallery app
   * @param reportText the report
   * @param galleryId id of the galleryApp
   * @param offenderId id of user who is being reported
   * @param reporterId if of the user who reported
   * @return the id of the new report
   */
  long addAppReport(final String reportText, final long galleryId,
      final String offenderId, final String reporterId);
  /**
  * check if an app is reported by a user
  * @param galleryId id of gallery app that was like
  * @param userId id of user who likes it
  * @return true if relation exists
  */
  boolean isReportedByUser(long galleryId,String userId);
  /**
   * Returns a list of reports (flags) for an app
   * @param galleryId id of gallery app
   * @param start start index
   * @param count number to return
   * @return list of {@link GalleryAppReport}
   */
  List<GalleryAppReport> getAppReports(long galleryId, int start, int count);

  /**
   * Returns a wrapped class which contains a list of reports (flags) for unresolved app
   * and total number of results in database
   * @param start start index
   * @param count number to return
   * @return list of {@link GalleryAppReport}
   */
  GalleryReportListResult getAppReports(int start, int count);
  /**
  * Returns a wrapped class which contains a list of reports (flags) for resolved and unresolved app
  * and total number of results in database
  * @param start start index
  * @param count number to retrieve
  * @return the list of reports
  */
  GalleryReportListResult getAllAppReports(int start, int count);
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

  /**
   * send email from sender to receiver
   * @param senderId sender id
   * @param receiverId receiver id
   * @param senderEmail sender email
   * @param receiverEmail receiver email
   * @param title title of email
   * @param body body of email
   * @return email id
   */
  long sendEmail(String senderId, String receiverId,
      String senderEmail, String receiverEmail,
      String title, String body);

  /**
   * get email based on emailId
   * @param emailId email id
   * @return Message message object
   */
  Email getEmail(long emailId);

  /**
   * check if read to send app stats to user
   * @param userId
   * @param galleryId
   * @param adminEmail
   * @param currentHost
   */
  boolean checkIfSendAppStats(String userId, long galleryId, String adminEmail, String currentHost);

  /**
   * store moderation action
   * @param reportId report id
   * @param galleryId gallery id
   * @param emailId email id
   * @param moderatorId moderator id
   * @param actionType action type
   * @param moderatorName moderator name
   * @param emailPreview email preview
   */
  void storeModerationAction(long reportId, long galleryId, long emailId, String moderatorId, int actionType, String moderatorName, String emailPreview);

  /**
   * get moderation actions
   * @param reportId report id
   * @return list of GalleryModerationAction
   */
  List<GalleryModerationAction> getModerationActions(long reportId);
}
