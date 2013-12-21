package com.google.appinventor.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.client.youngandroid.TextValidators;

import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;

import java.io.IOException;

//import java.net.URLEncoder;
// import java.io.UnsupportedEncodingException;


public class GalleryClient {

  private GalleryRequestListener listener;
  public static final int REQUEST_FEATURED=1;
  public static final int REQUEST_RECENT=2;
  public static final int REQUEST_SEARCH=3;
  public static final int REQUEST_MOSTLIKED=4;
  public static final int REQUEST_MOSTDOWNLOADED=5;
  public static final int REQUEST_MOSTVIEWED=6;
  public static final int REQUEST_BYDEVELOPER=7;
  public static final int REQUEST_BYTAG=8;
  public static final int REQUEST_ALL=9;

  /*
   * Create a client and set the listener so when client ops complete they
   * can tell the view that cares
   */
  public GalleryClient(GalleryRequestListener listener) {
    this.listener=listener;
  }
  
  public void FindApps(String keywords, int start, int count, int sortOrder) {
 
  }

  public void GetAppsByDeveloper(int start, int count, String developerId) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
    // failure message
    MESSAGES.galleryError()) {
    @Override
    public void onSuccess(List<GalleryApp> apps) {
      // the server has returned us something
      listener.onAppListRequestCompleted(apps, REQUEST_BYDEVELOPER); 
    }
    };
      
    // ok, this is below the call back, but of course it is done first 
    ode.getGalleryService().getDeveloperApps(developerId, start,count,callback);


  }

  public void GetFeatured(int start, int count, int sortOrder) {

  }

  public void GetMostRecent(int start, int count) { 
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
    // failure message
    MESSAGES.galleryError()) {
    @Override
    public void onSuccess(List<GalleryApp> apps) {
      // the server has returned us something
      listener.onAppListRequestCompleted(apps, REQUEST_RECENT); 
    }
    };
      
    // ok, this is below the call back, but of course it is done first 
    ode.getGalleryService().getRecentApps(start,count,callback);
  
  }
  
  public void GetMostDownloaded(int start, int count) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
    // failure message
    MESSAGES.galleryError()) {
    @Override
    public void onSuccess(List<GalleryApp> apps) {
      // the server has returned us something
      listener.onAppListRequestCompleted(apps, REQUEST_MOSTDOWNLOADED); 
    }
    };
      
    // ok, this is below the call back, but of course it is done first 
    ode.getGalleryService().getMostDownloadedApps(start,count,callback);
  
  }

  public void GetMostViewed(int start, int count) {

  }

  public void GetMostLiked(int start, int count) {

  }

  public void GetComments(String appId, int start, int count) {
   
  }
  public void Publish(GalleryApp app) {
    // TODO Auto-generated method stub
  }

  public void Update(GalleryApp app) {
    // TODO Auto-generated method stub
  }

  public void loadSourceFile(GalleryApp gApp) {
    final String projectName=gApp.getProjectName();
    final String sourceURL=gApp.getSourceURL();
    final long galleryId = gApp.getGalleryAppId();
    
    // first check name to see if valid and unique...
    if (!TextValidators.checkNewProjectName(projectName))
      return;  // the above function takes care of error messages
    // Callback for updating the project explorer after the project is created on the back-end
    final Ode ode = Ode.getInstance();

    final OdeAsyncCallback<Void> galleryCallback = new OdeAsyncCallback<Void>(	      
    // failure message
    MESSAGES.createProjectError()) {
      @Override
      public void onSuccess(Void arg2) {
      }
    };
    
    final OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(	      
    // failure message
    MESSAGES.createProjectError()) {
      @Override
      public void onSuccess(UserProject projectInfo) {
        // if we were able to create the new project, lets increment download count in gallery db
        ode.getGalleryService().appWasDownloaded(galleryId,projectInfo.getProjectId(),galleryCallback);

        // now relay the result back to UI client
        listener.onSourceLoadCompleted(projectInfo);    
      }
    };
    // this is really what's happening here, we call server to load project
    ode.getProjectService().newProjectFromGallery(projectName, sourceURL, galleryId, callback);
  } 
 
 

  private String getStartCountString(int start, int count) {
    return ":"+String.valueOf(start)+":"+String.valueOf(count);  
  }

}
