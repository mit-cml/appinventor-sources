// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;

/* can remove the following three 
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.user.User;
*/

import com.google.appinventor.shared.rpc.project.GalleryApp;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

/**
 * Interface of methods to simplify access to the storage systems.
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
  long createGalleryApp(String title, String description, long projectId);


  /**
   * Returns an array of recently published GalleryApps
   *
   * @return  list of gallery app ids
   */
  List<GalleryApp> getRecentGalleryApps(int start, int count);
  
  
  // need ...
  // getRecentGalleryApps
  // getMostViewedGalleryApps
  // findGalleryApps

  // comment stuff
  // studio stuff
  
}
