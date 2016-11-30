// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.GalleryList;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.GallerySettings;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Gallery Client is a facade for the ui to talk to the gallery server side.
 * It is a Singleton and has a list of listeners (GalleryPage, GalleryList)
 *
 * @author wolberd@gmail.com (David Wolber)
 * @author vincentaths@gmail.com (Vincent Zhang)
 */
public class GalleryClient {

  private List<GalleryRequestListener> listeners;
  //private GallerySettings settings;

  public static final int REQUEST_FEATURED = 1;
  public static final int REQUEST_RECENT = 2;
  public static final int REQUEST_SEARCH = 3;
  public static final int REQUEST_MOSTLIKED = 4;
  public static final int REQUEST_MOSTDOWNLOADED = 5;
  public static final int REQUEST_MOSTVIEWED = 6;
  public static final int REQUEST_BYDEVELOPER = 7;
  public static final int REQUEST_BYTAG = 8;
  public static final int REQUEST_ALL = 9;
  public static final int REQUEST_REMIXED_TO = 10;
  public static final int REQUEST_TUTORIAL = 11;

  private static volatile GalleryClient  instance= null;

  public String ENVIRONMENT = null;

  /**
   * constructor
   */
  private GalleryClient () {
    listeners = new ArrayList<GalleryRequestListener>();
  }

  /**
   * @return the instance of GalleryClient
   */
  public static GalleryClient getInstance () {
    if (instance == null) {
      synchronized (GalleryClient.class) {
        instance = new GalleryClient();
      }
    }
    return instance;
  }

  /**
   * add listener to listners list
   * @param listener gallery request listener
   */
  public void addListener(GalleryRequestListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove a listener from the listeners list
   * @param listener gallery request listener
   */
  public void removeListener(GalleryRequestListener listener) {
    listeners.remove(listener);
  }

  /**
   * Returns the gallery settings.
   *
   * @return  gallery settings
   */
  public GallerySettings getGallerySettings() {
    return Ode.getGallerySettings();
  }

 /**
  * FindApps calls search and then tells listeners when done
  * @param keywords search keywords
  * @param start staring index for search
  * @param count number of results
  * @param sortOrder currently unused,
  */
  public void FindApps(String keywords, int start, int count, int sortOrder, final boolean refreshable) {
     // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<GalleryAppListResult> callback = new OdeAsyncCallback<GalleryAppListResult>(
    // failure message
    MESSAGES.gallerySearchError()) {
      @Override
      public void onSuccess(GalleryAppListResult appsResult) {
        // the server has returned us something
        for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
          if (i.next().onAppListRequestCompleted(appsResult, REQUEST_SEARCH, refreshable)) {
            i.remove();
          }
        }
      }
    };

    //this is below the call back, but of course it is done first
    ode.getGalleryService().findApps(keywords, start,count,callback);
  }
 /**
  * GetAppsByDeveloper gets apps by developer and then tells listeners when done
  * @param start staring index for search
  * @param count number of results
  * @param developerId id of developer
  */
  public void GetAppsByDeveloper(int start, int count, String developerId) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<GalleryAppListResult> callback = new OdeAsyncCallback<GalleryAppListResult>(
    // failure message
    MESSAGES.galleryDeveloperAppError()) {
      @Override
      public void onSuccess(GalleryAppListResult appsResult) {
        // the server has returned us something
        for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
          if (i.next().onAppListRequestCompleted(appsResult, REQUEST_BYDEVELOPER, false)) {
            i.remove();
          }
        }
      }
    };
    // This is below the call back, but of course it is done first
    ode.getGalleryService().getDeveloperApps(developerId, start,count,callback);
  }
 /**
  * GetFeatured gets featured apps, currently unimplemented
  * @param start staring index
  * @param count number of results
  * @param sortOrder unused sort order
  */
  public void GetFeatured(int start, int count, int sortOrder, final boolean refreshable) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<GalleryAppListResult> callback = new OdeAsyncCallback<GalleryAppListResult>(
    // failure message
    MESSAGES.galleryRecentAppsError()) {
      @Override
      public void onSuccess(GalleryAppListResult appsResult) {
        // the server has returned us something
        for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
          if (i.next().onAppListRequestCompleted(appsResult, REQUEST_FEATURED, false)) {
            i.remove();
          }
        }
      }
    };
    // This is below the call back, but of course it is done first
    ode.getGalleryService().getFeaturedApp(start, count, callback);
  }
/**
  * GetTutorial gets tutorial apps, implemented in GalleryList.java
  * @param start staring index
  * @param count number of results
  * @param sortOrder unused sort order
  */
  public void GetTutorial(int start, int count, int sortOrder, final boolean refreshable) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<GalleryAppListResult> callback = new OdeAsyncCallback<GalleryAppListResult>(
    // failure message
    MESSAGES.galleryRecentAppsError()) {
      @Override
      public void onSuccess(GalleryAppListResult appsResult) {
        // the server has returned us something
        for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
          if (i.next().onAppListRequestCompleted(appsResult, REQUEST_TUTORIAL, false)) {
            i.remove();
          }
        }
      }
    };
    // This is below the call back, but of course it is done first
    ode.getGalleryService().getTutorialApp(start, count, callback);
  }
/**
  * GetMostRecent gets most recently updated apps then tells listeners
  * @param start staring index
  * @param count number of results
  */
  public void GetMostRecent(int start, int count, final boolean refreshable) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<GalleryAppListResult> callback = new OdeAsyncCallback<GalleryAppListResult>(
    // failure message
    MESSAGES.galleryRecentAppsError()) {
      @Override
      public void onSuccess(GalleryAppListResult appsResult) {
        // the server has returned us something
        for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
          if (i.next().onAppListRequestCompleted(appsResult, REQUEST_RECENT, false)) {
            i.remove();
          }
        }
      }
    };
    // This is below the call back, but of course it is done first
    ode.getGalleryService().getRecentApps(start, count, callback);
  }
  /**
  * GetMostLiked gets the most liked apps then tells listeners
  * @param start staring index
  * @param count number of results
  */
  public void GetMostLiked(int start, int count, final boolean refreshable) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<GalleryAppListResult> callback = new OdeAsyncCallback<GalleryAppListResult>(
    // failure message
    MESSAGES.galleryLikedAppsError()) {
      @Override
      public void onSuccess(GalleryAppListResult appsResult) {
        // the server has returned us something
        for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
          if (i.next().onAppListRequestCompleted(appsResult, REQUEST_MOSTLIKED, false)) {
            i.remove();
          }
        }
      }
    };

    // ok, this is below the call back, but of course it is done first
    ode.getGalleryService().getMostLikedApps(start,count,callback);
  }
  /**
  * GetMostDownloaded gets the most downloaded apps then tells listeners
  * @param start staring index
  * @param count number of results
  */
  public void GetMostDownloaded(int start, int count, final boolean refreshable) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<GalleryAppListResult> callback = new OdeAsyncCallback<GalleryAppListResult>(
    // failure message
    MESSAGES.galleryDownloadedAppsError()) {
      @Override
      public void onSuccess(GalleryAppListResult appsResult) {
        // the server has returned us something
        for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
          if (i.next().onAppListRequestCompleted(appsResult, REQUEST_MOSTDOWNLOADED, false)) {
            i.remove();
          }
        }
      }
    };

    // ok, this is below the call back, but of course it is done first
    ode.getGalleryService().getMostDownloadedApps(start,count,callback);
  }
  /**
   * GetRemixedToList gets children list that apps remixed to then tells listeners
   */
  public void GetRemixedToList(GalleryAppListResult appsResult) {
    for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
      if (i.next().onAppListRequestCompleted(appsResult, REQUEST_REMIXED_TO, true)) {
        i.remove();
      }
    }
  }

  public void GetMostViewed(int start, int count) {

  }

  public void GetMostLiked(int start, int count) {

  }
  /**
  * GetComments gets comments for an app then tells listeners
  * @param appId app id
  * @param start staring index
  * @param count number of results
  */
  public void GetComments(long appId,int start,int count) {
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<List<GalleryComment>> galleryCallback = new OdeAsyncCallback<List<GalleryComment>>(
    // failure message
    MESSAGES.galleryCommentError()) {
      @Override
      public void onSuccess(List<GalleryComment> comments) {
        // now relay the result back to UI client
        for (Iterator<GalleryRequestListener> i = listeners.iterator(); i.hasNext();) {
          if (i.next().onCommentsRequestCompleted(comments)) {
            i.remove();
          }
        }
      }
    };
    ode.getGalleryService().getComments(appId,galleryCallback);

  }
  // the following two methods are not implemented. we just publish/update directly
  // from the view classes (and not using client facade)
  public void Publish(GalleryApp app) {
    // TODO Auto-generated method stub
  }

  public void Update(GalleryApp app) {
    // TODO Auto-generated method stub
  }
 /**
  * loadSourceFile opens the app as a new app inventor project
  * @param gApp the app to open
  * @return True if success, otherwise false
  */
  public boolean loadSourceFile(GalleryApp gApp, String newProjectName, final PopupPanel popup) {
    final String projectName = newProjectName;
    final String sourceKey = getGallerySettings().getSourceKey(gApp.getGalleryAppId());
    final long galleryId = gApp.getGalleryAppId();

    // first check name to see if valid and unique...
    if (!TextValidators.checkNewProjectName(projectName))
      return false;  // the above function takes care of error messages
    // Callback for updating the project explorer after the project is created on the back-end
    final Ode ode = Ode.getInstance();

    final OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(
    // failure message
    MESSAGES.createProjectError()) {
      @Override
      public void onSuccess(UserProject projectInfo) {
        Project project = ode.getProjectManager().addProject(projectInfo);
        Ode.getInstance().openYoungAndroidProjectInDesigner(project);
        popup.hide();
      }
      @Override
      public void onFailure(Throwable caught) {
        popup.hide();
        super.onFailure(caught);
      }
    };
    // this is really what's happening here, we call server to load project
    ode.getProjectService().newProjectFromGallery(projectName, sourceKey, galleryId, callback);
    return true;
  }

 /**
  * appWasChanged called to tell galleryList (and possibly others) that app is modified
  */
  public void appWasChanged() {
    // for now, let's update the recent list, the popular list and feature list (in case one was deleted)
    GetMostRecent(0,GalleryList.NUMAPPSTOSHOW, true);
    GetMostLiked(0,GalleryList.NUMAPPSTOSHOW, true);
    GetFeatured(0, GalleryList.NUMAPPSTOSHOW, 0, true);
    GetTutorial(0,GalleryList.NUMAPPSTOSHOW, 0, true);
  }

 /**
  * appWasDownloaded called to tell backend that app is downloaded
  */
  public void appWasDownloaded(final long galleryId, final String userId) {
    // Inform the GalleryService (which eventually goes to ObjectifyGalleryStorageIo)
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<Void> callback = new OdeAsyncCallback<Void>(
      MESSAGES.galleryDownloadedAppsError()) {
      @Override
      public void onSuccess(Void result) {
        // If app was successfully downloaded, get another async call going
        // This call we increment the download count of this app
        final OdeAsyncCallback<GalleryApp> appCallback = new OdeAsyncCallback<GalleryApp>(
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(GalleryApp app) {
            app.incrementDownloads();
          }
        };
        Ode.getInstance().getGalleryService().getApp(galleryId, appCallback);

        final OdeAsyncCallback<Boolean> checkCallback = new OdeAsyncCallback<Boolean>(
        MESSAGES.galleryError()) {
          @Override
          public void onSuccess(Boolean b) {
            //email will be send automatically if condition matches (in ObjectifyGalleryStorageIo)
          }
        };
        Ode.getInstance().getGalleryService().checkIfSendAppStats(userId, galleryId,
            getGallerySettings().getAdminEmail(), Window.Location.getHost(), checkCallback);
       }
    };
    // ok, this is below the call back, but of course it is done first
    ode.getGalleryService().appWasDownloaded(galleryId, callback);


  }

  public static final String DEFAULTGALLERYIMAGE="images/genericApp.png";
  public static final String DEFAULTUSERIMAGE="images/android_icon_.png";

  /**
   * URL is in GCS, of form: /gs/<bucket>/gallery/apps/6046115656892416/aia
   * @return gallery bucket
   */
  public String getBucket() {
    return getGallerySettings().getBucket();
  }

  /**
   * get cloud image url
   * @param galleryId gallery id
   * @return url of cloud image
   */
  public String getCloudImageURL(long galleryId) {
    if(getSystemEnvironment() != null &&
        getSystemEnvironment().toString().equals("Production")){
      return getGallerySettings().getCloudImageURL(galleryId);
    }else {
      return getGallerySettings().getCloudImageLocation(galleryId);
    }
  }

  /**
   * get project image url
   * @param projectId project id
   * @return url of project image
   */
  public String getProjectImageURL(long projectId) {
    if(getSystemEnvironment() != null &&
        getSystemEnvironment().toString().equals("Production")){
      return getGallerySettings().getProjectImageURL(projectId);
    }else {
      return getGallerySettings().getProjectImageLocation(projectId);
    }
  }

  /**
   * get user image url
   * @param userId user id
   * @return url of user image
   */
  public String getUserImageURL(String userId) {
    if(getSystemEnvironment() != null &&
        getSystemEnvironment().toString().equals("Production")){
      return getGallerySettings().getUserImageURL(userId);
    }else {
      return getGallerySettings().getUserImageLocation(userId);
    }
  }

  public void setSystemEnvironment(String value) {
    ENVIRONMENT = value;
  }
  public String getSystemEnvironment() {
    return this.ENVIRONMENT;
  }

}
