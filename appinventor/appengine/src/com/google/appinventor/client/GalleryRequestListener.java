// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import java.util.List;

import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.UserProject;

public interface GalleryRequestListener {

  public static final int REQUEST_FEATURED=1;
  public static final int REQUEST_RECENT=2;
  public static final int REQUEST_SEARCH=3;
  public static final int REQUEST_MOSTLIKED=4;
  public static final int REQEST_MOSTDOWNLOADED=5;
  public static final int REQUEST_MOSTVIEWED=6;

  /**
   * UI objects will call gallery client to get apps, the methods here are the callback
   *
   */
   public void onAppListRequestCompleted(GalleryAppListResult appsResult, int requestID, boolean refreshable);
   public void onCommentsRequestCompleted(List<GalleryComment> comments);
   public void onSourceLoadCompleted(UserProject projectInfo);
}