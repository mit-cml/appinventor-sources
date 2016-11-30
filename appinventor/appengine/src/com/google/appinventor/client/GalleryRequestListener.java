// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import java.util.List;

import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.UserProject;

/**
 * UI objects will call gallery client to get apps, the methods here are the callback
 *
 */
public interface GalleryRequestListener {

  public static final int REQUEST_FEATURED=1;
  public static final int REQUEST_RECENT=2;
  public static final int REQUEST_SEARCH=3;
  public static final int REQUEST_MOSTLIKED=4;
  public static final int REQEST_MOSTDOWNLOADED=5;
  public static final int REQUEST_MOSTVIEWED=6;

  /**
   * After a request for an application list is proceed this method will be called.
   *
   * @param appsResult
   * @param requestID
   * @param refreshable
   * @return true if the listener should be removed, otherwise false.
   */
  public boolean onAppListRequestCompleted(GalleryAppListResult appsResult, int requestID, boolean refreshable);

  /**
   * After a request for application comments has been completed, this method will be called.
   *
   * @param comments
   * @return true if the listener should be removed, otherwise false.
   */
  public boolean onCommentsRequestCompleted(List<GalleryComment> comments);

  /**
   * After a request for sources has been completed, this method will be called.
   *
   * @param projectInfo
   * @return true if the listener should be removed, otherwise false.
   */
  public boolean onSourceLoadCompleted(UserProject projectInfo);
}