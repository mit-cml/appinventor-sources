package com.google.appinventor.client;

import com.google.appinventor.shared.rpc.project.GalleryApp;
import java.util.List;
import com.google.appinventor.shared.rpc.project.UserProject;

public interface GalleryRequestListener {

  /**
   * UI objects will call gallery client to get apps, the methods here are the callback
   *
   */
   
   public void onGalleryRequestCompleted(List<GalleryApp> apps);
   public void onSourceLoadCompleted(UserProject projectInfo);
}