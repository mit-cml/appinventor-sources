// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;


import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;

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
   *
   * @param userId unique user id
   * @return user data
   */
  GalleryApp getGalleryApp(long galleryId);

  /**
   * creates a new gallery app
   *
   * @param userId unique user id
   * @return user data
   */
  long createGalleryApp(String title, String projectName, String description, long projectId, String userId);

    /**
   * updates gallery app
   *
   * 
   */
  void updateGalleryApp(long galleryId, String title, String description, String userId);


  /**
   * Returns an array of recently published GalleryApps
   *
   * @return  list of gallery app ids
   */
  List<GalleryApp> getRecentGalleryApps(int start, int count);
   
   /**
   * Returns an array of most downloaded GalleryApps
   *
   * @return  list of gallery app ids
   */
  List<GalleryApp> getMostDownloadedApps(int start, int count);
  
  void incrementDownloads(long galleryId);

  // comment stuff
  void addComment(long galleryId,String userId, String comment);
  List<GalleryComment> getComments(long galleryId);
  // studio stuff... to come
  
}
