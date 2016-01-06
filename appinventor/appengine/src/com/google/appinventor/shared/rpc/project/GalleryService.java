// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import java.util.List;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Interface for the service providing gallery information.
 *
 */
@RemoteServiceRelativePath(ServerLayout.GALLERY_SERVICE)
public interface GalleryService extends RemoteService {


  /** Load the gallery settings (enabled, bucket) at startup time
   *
   * @return the gallery settings
   */
  GallerySettings loadGallerySettings();

  /**
   * Publishes a gallery app
   * @param projectId id of the project being published
   * @param projectName name of project
   * @param title title of new gallery app
   * @param description description of new gallery app
   * @return a {@link GalleryApp} for new galleryApp
   */
  GalleryApp publishApp(long projectId, String projectName, String title,
    String moreInfo, String credit, String description);
  /**
   * update a gallery app
   * @param app info about app being updated
   * @param newImage  true if the user has submitted a new image
   */
  void updateApp(GalleryApp app, boolean newImage);

  /**
   * update a gallery app's source (aia)
   * @param galleryId id of gallery app to be updated
   * @param projectId id of project so we can grab source
   * @param projectName name of project, this is name in new aia
   */
  void updateAppSource (long galleryId, long projectId, String projectName);
  /**
   * update a gallery app's meta data
   * @param app info about app being updated
   *
   */
  void updateAppMetadata(GalleryApp app);

  /**
   * index all gallery apps (admin method)
   * @param count the max number of apps to index
   */
  void indexAll(int count);

  /**
   * Deletes a new gallery app
   * @param galleryId id of app to delete
   */
  void deleteApp(long galleryId);

  /**
   * Returns the total number of gallery apps
   * @return num of GalleryApps
   */
  Integer getNumApps();

  /**
   * Returns a wrapped class which contains list of most recently
   * updated galleryApps and total number of results in database
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  GalleryAppListResult getRecentApps(int start, int count);

  /**
   * Returns a wrapped class which contains a list of most downloaded
   * gallery apps and total number of results in database
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  GalleryAppListResult getMostDownloadedApps(int start, int count);

  /**
   * Returns a wrapped class which contains a list of most liked
   * gallery apps and total number of results in database
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  GalleryAppListResult getMostLikedApps(int start, int count);

  /**
   * Returns a wrapped class which contains a list of featured gallery app
   * @param start start index
   * @param count count number
   * @return list of gallery app
   */
  GalleryAppListResult getFeaturedApp(int start, int count);

  /**
   * Returns a wrapped class which contains a list of tutorial gallery app
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
   * @param userId id of the developer
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  GalleryAppListResult getDeveloperApps(String userId, int start, int count);

  /**
   * Returns a wrapped class which contains a list of galleryApps and
   * total number of results in database
   * @param keywords keywords to search for
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */

  GalleryAppListResult findApps(String keywords, int start, int count);

  /**
   * Returns a GalleryApp object for the given id
   * @param galleryId  gallery ID as received by
   *                   {@link #getRecentGalleryApps()}
   *
   * @return  gallery app object
   */
  GalleryApp getApp(long galleryId);

  /**
   * Record the fact that app was downloaded
   * @param galleryId id of app that was downloaded
   */
  void appWasDownloaded(long galleryId);

  /**
   * Returns the comments for an app
   * @param galleryId  gallery ID as received by
   *                   {@link #getRecentGalleryApps()}
   * @return  a list of comments
   */
  List<GalleryComment> getComments(long galleryId);

  /**
   * publish a comment for a gallery app
   * @param galleryId the id of the app
   * @param comment the comment
   */
  long publishComment(long galleryId, String comment);

  /**
   * increase likes for a gallery app by 1
   * @param galleryId the id of the app
   */
  int increaseLikes(long galleryId);

  /**
   * decrease likes for a gallery app by 1
   * @param galleryId the id of the app
   */
  int decreaseLikes(long galleryId);

  /**
   * get num of likes of a gallery app
   * @param galleryId the id of the app
   */
  int getNumLikes(long galleryId);

  /**
   * check if an app is liked by a user
   * @param galleryId the id of the app
   */
  boolean isLikedByUser(long galleryId);

  /**
   * salvage the gallery app by given galleryId
   */
  void salvageGalleryApp(long galleryId);

  /**
  * adds a report (flag) to a gallery app
  * @param app app that is being reported
  * @param reportText the report
  * @return the id of the new report
  */
  long addAppReport(GalleryApp app, String reportText);

  /**
  * gets recent reports
  * @param start start index
  * @param count number to retrieve
  * @return the list of reports
  */
  GalleryReportListResult getRecentReports(int start, int count);

  /**
  * gets existing reports
  * @param start start index
  * @param count number to retrieve
  * @return the list of reports
  */
  GalleryReportListResult getAllAppReports(int start, int count);

  /**
  * check if an app is reported by a user
  * @param galleryId the id of the app
  */
  boolean isReportedByUser(long galleryId);

  /**
   * save the attribution of an app
   * @param galleryId the id of the app
   * @param attributionId the id of the attribution app
   */
  long saveAttribution(long galleryId, long attributionId);

  /**
   * get the attribution id of an app
   * @param galleryId the id of the app
   */
  long remixedFrom(long galleryId);

  /**
   * get the children ids of an app
   * @param galleryId the id of the app
   */
  List<GalleryApp> remixedTo(long galleryId);

  /**
   * send an Email
   * @param senderId sender id
   * @param receiverId receiver id
   * @param receiverEmail receiver email
   * @param title title of email
   * @param body body of email
   * @return emailId
   */
  long sendEmail(String senderId, String receiverId, String receiverEmail,
      String title, String body);

  /**
   * check if ready to send app stats to user
   * @param userId
   * @param galleryId
   * @param adminEmail
   * @param currentHost
   */
  boolean checkIfSendAppStats(String userId, long galleryId, String adminEmail, String currentHost);

  /**
   * get email based on given email id
   * @param emailId email id
   * @return Email email
   */
  Email getEmail(long emailId);

  /**
   * mark an report as resolved
   * @param reportId the id of the report
   */
  boolean markReportAsResolved(long reportId, long galleryId);

  /**
   * deactivate gallery app
   * @param galleryId the id of the gallery app
   */
  boolean deactivateGalleryApp(long galleryId);

  /**
   * check if gallery app  is activated
   * @param galleryId the id of the gallery app
   */
  boolean isGalleryAppActivated(long galleryId);

  /**
   * Store moderation actions based on actionType
   * @param reportId
   * @param galleryId
   * @param emailId
   * @param moderatorId
   * @param actionType
   */
  void storeModerationAction(long reportId, long galleryId, long emailId, String moderatorId, int actionType, String moderatorName, String emailPreview);

  /**
   * get moderation actions based on given reportId
   * @param reportId
   * @return list of GalleryModerationAction
   */
  List<GalleryModerationAction> getModerationActions(long reportId);

  String getBlobServingUrl(String url);

}
