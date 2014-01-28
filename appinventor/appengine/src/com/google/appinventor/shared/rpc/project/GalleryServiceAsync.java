// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Interface for the service providing project information. All declarations
 * in this interface are mirrored in {@link ProjectService}. For further
 * information see {@link ProjectService}.
 *
 */
public interface GalleryServiceAsync {

  /**
   * @see GalleryService#publishApp(long, String, String, String)
   */
  void publishApp(long projectId, String title, String projectName, String description,
      AsyncCallback<GalleryApp> callback);
   /**
   * @see GalleryService#updateApp(GalleryApp)
   */
  void updateApp(GalleryApp app, boolean newImage, AsyncCallback<Void> callback);

  /**
   * @see GalleryService#updateAppMetadata(GalleryApp)
   */
  void updateAppMetadata(GalleryApp app,AsyncCallback<Void> callback);

  /**
   * @see GalleryService#updateAppSource(long, long, String, String, String)
   */
  void updateAppSource(long galleryId, long projectId, String projectName, 
      AsyncCallback<Void> callback);

  /**
   * @see GalleryService#publishImage(String, String, NewProjectParameters)
   */
//  void publishImage(long projectId, String title, String description,
//      AsyncCallback<Long> callback);

  /**
   * @see GalleryService#deleteApp(long galleryId)
   */
  void deleteApp(long galleryId, AsyncCallback<java.lang.Void> arg2);
  
  /**
   * @see GalleryService#appWasDownloaded(long galleryId, long projectId)
   */
  void appWasDownloaded(long galleryId, long projectId, AsyncCallback<java.lang.Void> arg2);

  /**
   * @see GalleryService#getRecentApps(int start, int count)
   */
  void getRecentApps(int start, int count, AsyncCallback<List<GalleryApp>> callback);
  

   /**
   * @see GalleryService#getMostDownloadedApps(int start, int count)
   */
  void getMostDownloadedApps(int start, int count, AsyncCallback<List<GalleryApp>> callback);

  /**
   * @see GalleryService#getDeveloperApps(String userId, int start, int count)
   */
  void getDeveloperApps(String userId, int start, int count, AsyncCallback<List<GalleryApp>> callback);
  
  /**
   * @see GalleryService#findApps(String keywords, int start, int count)
   */
  void findApps( String keywords, int start, int count, AsyncCallback<List<GalleryApp>> callback);

   /**
   * @see GalleryService#getApp(int start, int count)
   */
  void getApp(long galleryId, AsyncCallback<GalleryApp> callback);

  /**
   * @see GalleryService#getComments(long galleryId)
   */
  void getComments(long galleryId, AsyncCallback<List<GalleryComment>> callback);
  /**
   * @see GalleryService#publishComment(long galleryId, String comment)
   */
  void publishComment(long galleryId, String comment, AsyncCallback<java.lang.Long> date);

  
}
