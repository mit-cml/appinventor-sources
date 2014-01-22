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

import com.google.appinventor.client.explorer.project.Project;


public class GalleryClient {

  private List<GalleryRequestListener> listeners;
  public static final int REQUEST_FEATURED=1;
  public static final int REQUEST_RECENT=2;
  public static final int REQUEST_SEARCH=3;
  public static final int REQUEST_MOSTLIKED=4;
  public static final int REQUEST_MOSTDOWNLOADED=5;
  public static final int REQUEST_MOSTVIEWED=6;
  public static final int REQUEST_BYDEVELOPER=7;
  public static final int REQUEST_BYTAG=8;
  public static final int REQUEST_ALL=9;

  private static volatile GalleryClient  instance= null;
  private GalleryClient () {
    listeners = new ArrayList<GalleryRequestListener>();
  }
  public static GalleryClient getInstance () {
    if (instance == null) {
      synchronized (GalleryClient.class) {
        instance = new GalleryClient();
      }
    }
    return instance;
  }

  public void addListener(GalleryRequestListener listener) {
    listeners.add(listener);
  }
  
  /*
   * Create a client and set the listener so when client ops complete they
   * can tell the view that cares

  public GalleryClient(GalleryRequestListener listener) {
    this.listener=listener;
  }
    */ 
  public void FindApps(String keywords, int start, int count, int sortOrder) {
     // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
    // failure message
    MESSAGES.galleryDeveloperAppError()) {
    @Override
    public void onSuccess(List<GalleryApp> apps) {
      // the server has returned us something
      for (GalleryRequestListener listener:listeners) {
        listener.onAppListRequestCompleted(apps, REQUEST_SEARCH); 
      }
    }
    };
      
    // ok, this is below the call back, but of course it is done first 
    ode.getGalleryService().findApps(keywords, start,count,callback);
  }

  public void GetAppsByDeveloper(int start, int count, String developerId) {
    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
    // failure message
    MESSAGES.galleryDeveloperAppError()) {
    @Override
    public void onSuccess(List<GalleryApp> apps) {
      // the server has returned us something
      for (GalleryRequestListener listener:listeners) {
        listener.onAppListRequestCompleted(apps, REQUEST_BYDEVELOPER); 
      }
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
      for (GalleryRequestListener listener:listeners) {
        listener.onAppListRequestCompleted(apps, REQUEST_RECENT);
      } 
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
      for (GalleryRequestListener listener:listeners) {
        listener.onAppListRequestCompleted(apps, REQUEST_MOSTDOWNLOADED);
      } 
    }
    };
      
    // ok, this is below the call back, but of course it is done first 
    ode.getGalleryService().getMostDownloadedApps(start,count,callback);
  
  }

  public void GetMostViewed(int start, int count) {

  }

  public void GetMostLiked(int start, int count) {

  }

  public void GetComments(long appId,int start,int count) {
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<List<GalleryComment>> galleryCallback = new OdeAsyncCallback<List<GalleryComment>>(	      
    // failure message
    MESSAGES.galleryCommentError()) {
      @Override
      public void onSuccess(List<GalleryComment> comments) {
        // now relay the result back to UI client
        for (GalleryRequestListener listener:listeners) {
          listener.onCommentsRequestCompleted(comments); 
        }
      }
    };
    ode.getGalleryService().getComments(appId,galleryCallback);

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
    OdeLog.log("***** in galleryClient.loadSourceFile, sourceURL is:" + sourceURL);
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
        Project project = ode.getProjectManager().addProject(projectInfo);
        Ode.getInstance().openYoungAndroidProjectInDesigner(project);
      /*
        if (projectInfo==null) {
          OdeLog.log("***** in galleryClient.loadSourceFile callback, projectInfo is null");
        } else {
          OdeLog.log("***** in galleryClient.loadSourceFile callback, projectId is:"+projectInfo.getProjectId());
          // if we were able to create the new project, lets increment download count in gallery db
          ode.getGalleryService().appWasDownloaded(galleryId,projectInfo.getProjectId(),galleryCallback);

          // now relay the result back to UI client
          OdeLog.log("***** in galleryClient.loadSourceFile num listeners is:"+String.valueOf(listeners.size()));
          for (GalleryRequestListener listener:listeners) {
            OdeLog.log("***** in galleryClient.loadSourceFile listener is"+listener.getClass());
            listener.onSourceLoadCompleted(projectInfo);
          }
        } 
       */   
      }
    };
    // this is really what's happening here, we call server to load project
    ode.getProjectService().newProjectFromGallery(projectName, sourceURL, galleryId, callback);
  } 
  // GalleryApp (and possibly others) call this to tell galleryList (and possibly others)
  // to update
  public void appWasChanged() {
    // for now, let's update the recent list
    GetMostRecent(0,5);
  }

  private String getStartCountString(int start, int count) {
    return ":"+String.valueOf(start)+":"+String.valueOf(count);  
  }

}
