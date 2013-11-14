package com.google.appinventor.client;

import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import java.util.List;
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
   
   public void onAppListRequestCompleted(List<GalleryApp> apps, int requestID);
   public void onCommentsRequestCompleted(List<GalleryComment> comments);
   public void onSourceLoadCompleted(UserProject projectInfo);
}