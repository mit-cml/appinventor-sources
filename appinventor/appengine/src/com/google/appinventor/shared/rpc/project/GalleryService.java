// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.io.InputStream;
import java.util.List;



/**
 * Interface for the service providing gallery information.
 *
 */
@RemoteServiceRelativePath(ServerLayout.GALLERY_SERVICE)
public interface GalleryService extends RemoteService {

  /**
   * Publishes a gallery app
   * @param projectId id of the project being published
   * @param projectName name of project
   * @param title title of new gallery app
   * @param description description of new gallery app
   * @return a {@link GalleryApp} for new galleryApp
   */
  GalleryApp publishApp(long projectId, String projectName, String title,
                         String description);
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
   * Returns a list of most recently updated galleryApps
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  List<GalleryApp> getRecentApps(int start, int count);
  
  /**
   * Returns a list of most downloaded gallery apps
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  List<GalleryApp> getMostDownloadedApps(int start, int count);

  /**
   * Returns a list of galleryApps by a particular developer
   * @param userId id of the developer
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  List<GalleryApp> getDeveloperApps(String userId, int start, int count);
  
  /**
   * Returns a list of galleryApps
   * @param keywords keywords to search for
   * @param start starting index
   * @param count number of apps to return
   * @return list of GalleryApps
   */
  List<GalleryApp> findApps(String keywords, int start, int count);

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
  List<GalleryAppReport> getRecentReports(int start, int count);

  /**
  * gets existing reports
  * @param start start index
  * @param count number to retrieve
  * @return the list of reports
  */
  List<GalleryAppReport> getAllAppReports(int start, int count);

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
  void sendMessageFromSystem(String senderId, String receiverId, String message);
  List<Message> getMessages(String receiverId);
  void readMessage(long msgId);
  void deleteMessage(long msgId);
  void appStatsWasRead(long appId);

  /**
   * mark an report as resolved
   * @param reportId the id of the report
   */
  boolean markReportAsResolved(long reportId);

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
   * update Database Field, should only be used by system admin
   */
  void updateDatabaseField();

}
