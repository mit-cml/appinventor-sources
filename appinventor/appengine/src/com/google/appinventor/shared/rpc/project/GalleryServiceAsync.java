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
   * @see GalleryService#publishApp(String, String, NewProjectParameters)
   */
  void publishApp(long projectId, String title, String description,
      AsyncCallback<Long> callback);

  /**
   * @see GalleryService#deleteApp(long galleryId)
   */
  void deleteApp(long galleryId, AsyncCallback<java.lang.Void> arg2);


  /**
   * @see GalleryService#getRecentApps(int start, int count)
   */
  void getRecentApps(int start, int count, AsyncCallback<List<GalleryApp>> callback);
  

   /**
   * @see GalleryService#getMostDownloadedApps(int start, int count)
   */
  void getMostDownloadedApps(int start, int count, AsyncCallback<List<GalleryApp>> callback);
  
  /**
   * @see GalleryService#findApps(String keywords, int start, int count)
   */
  void findApps( String keywords, int start, int count, AsyncCallback<List<GalleryApp>> callback);

   /**
   * @see GalleryService#getApp(int start, int count)
   */
  void getApp(long galleryId, AsyncCallback<GalleryApp> callback);

  
}
