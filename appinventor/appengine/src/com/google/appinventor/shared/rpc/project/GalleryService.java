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
 * Interface for the service providing project information.
 *
 */
@RemoteServiceRelativePath(ServerLayout.GALLERY_SERVICE)
public interface GalleryService extends RemoteService {

  /**
   * Creates a new gallery app
   * @param title gallery title of app
   * @param description description of app
   * NEED TO DEAL WITH IMAGE
   *
   * @return a {@link GalleryApp} for new galleryApp
   */
  long publishApp(long projectId, String projectName, String title,
                         String description);

  long updateApp(long galleryId, long projectId, String projectName, String title,
                         String description);


  
//  void storeImage(InputStream is, long galleryId);
  
  /**
   * Deletes a new gallery app
   * @param title gallery title of app
   * @return void
   */
  void deleteApp(long galleryId);

  /**
   * Returns a list of galleryApps
   * @param starting index
   * @param number of apps to return
   * @return list of GalleryApps found by the back-end
   */
  List<GalleryApp> getRecentApps(int start, int count);
  
  /**
   * Returns a list of galleryApps
   * @param starting index
   * @param number of apps to return
   * @return list of GalleryApps found by the back-end
   */
  List<GalleryApp> getMostDownloadedApps(int start, int count);
  
  /**
   * Returns a list of galleryApps
   * @param keyword to search for
   * @param starting index
   * @param number of apps to return
   * @return list of GalleryApps found by the back-end
   */
  List<GalleryApp> findApps(String keywords, int start, int count);

  /**
   * Returns a GalleryApp object for the given id
   * @param galleryId  gallery ID as received by
   *                   {@link #getRecentGalleryApps()}
   *
   * @return  root node of project
   */
  GalleryApp getApp(long galleryId);

  void appWasDownloaded(long galleryId, long newProjectId);

  
}
