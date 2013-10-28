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

import java.io.IOException;


public class GalleryClient {

  private GalleryRequestListener listener;

  public GalleryClient(GalleryRequestListener listener) {
    this.listener=listener;
  }
  
  public void FindApps(String keywords, int start, int count, int sortOrder) {
  // we need to deal with URL encoding of keywords
  requestApps("http://gallery.appinventor.mit.edu/rpc?tag=search:"+keywords+
      getStartCountString(start,count));		
  }

  public void FindByTag(String tag, int start, int count, int sortOrder) {
    // need to deal with URL encoding
    requestApps("http://gallery.appinventor.mit.edu/rpc?tag=tag:"+tag+ 
       getStartCountString(start,count));
  }
  // must have start and count, with filter after
  public void GetApps(int start, int count, int sortOrder, String sortField) {
    // currently returns five apps sorted by uid (app id)
    requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all"+ 
      getStartCountString(start,count)+":asc:uid");
  }

  public void GetAppsByDeveloper(int start, int count, String developerName) {
    // need to fix this one, i think it takes display name as the param but...
    requestApps("http://gallery.appinventor.mit.edu/rpc?tag=by_developer:"+
        developerName+getStartCountString(start,count));
  }

  public void GetCategories() {
     // we need another way to deal with non-list returns
     //"http://gallery.appinventor.mit.edu/rpc?tag=get_categories"
  }

  public void GetFeatured(int start, int count, int sortOrder) {
    requestApps("http://gallery.appinventor.mit.edu/rpc?tag=featured"+
       getStartCountString(start,count));
  }
  // uploadTime,desc gets most recent uploaded of source, 
  // creationTime would give time project was first added to gallery
  public void GetMostRecent(int start, int count) {
    requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all"+
       getStartCountString(start,count)+":desc:uploadTime");
  }
  
  public void GetMostDownloaded(int start, int count) {
    //doesn't work, need Vince to add index
    requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all"+
       getStartCountString(start,count)+":desc:numDownloads");
  }

  public void GetMostViewed(int start, int count) {
    //doesn't work, need Vince to add index
    requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all"+
       getStartCountString(start,count)+":desc:numViewed");
  }

  public void GetMostLiked(int start, int count) {
    requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all"+
       getStartCountString(start,count)+":desc:numLikes");
  }
	
  public void GetProject(String id) {
    // we need another way to deal with non-list things 
    //"http://gallery.appinventor.mit.edu/rpc?tag=getinfo:"+id);
  }
  // http://gallery.appinventor.mit.edu/rpc?tag=comments:id
  public void GetComments(String id,int start,int count)
  {
    // need a different callback function
  }
  public void Publish(GalleryApp app) {
    // TODO Auto-generated method stub
  }

  public void Update(GalleryApp app) {
    // TODO Auto-generated method stub
  }


  private void requestApps(String url)  {
    int STATUS_CODE_OK = 200;  

    // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
      // failure message
      MESSAGES.galleryError()) {
      @Override
      public void onSuccess(List<GalleryApp> list) {
        // the server has returned us something
    	  OdeLog.log("#############################");
        if (list== null) {
          return;
        }
        // things are good so tell the ui listener
        listener.onGalleryRequestCompleted(list);   
      }
    };
    // ok, this is below the call back, but of course it is done first 
    ode.getProjectService().getApps(url, callback);
  }
	
  public void loadSourceFile(final String projectName, String sourceURL) {
    // first check name to see if valid and unique...
    if (!TextValidators.checkNewProjectName(projectName))
      return;  // the above function takes care of error messages
    // Callback for updating the project explorer after the project is created on the back-end
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(	      
    // failure message
    MESSAGES.createProjectError()) {
      @Override
      public void onSuccess(UserProject projectInfo) {
        // just relay the result back to UI client
        listener.onSourceLoadCompleted(projectInfo);    
      }
    };
    // this is really what's happening here, we call server to load project
    ode.getProjectService().newProjectFromExternalTemplate(projectName, sourceURL,callback);
  } 
    
  public List<GalleryApp> generateFakeApps()  {
//    GalleryApp app1 = new GalleryApp("Sports Analyzer", "Joe Smith", "a great game","1/1/13","2/1/13","http://lh3.ggpht.com/zyfGqqiN4P8GvXFVbVf-RLC--PrEDeRCu5jovFYD6l3TXYfU5pR70HXJ3yr-87p5FUGFSxeUgOMecodBOcTFYA7frUg6QTrS5ocMcNk=s100","http://www.appinventor.org/apps2/ihaveadream/ihaveadream.aia",2,5,"","","");
//    GalleryApp app2 = new GalleryApp("Basketball Quiz", "Bill Jones", "sports quiz","2/3/13","2/5/13", "http://lh5.ggpht.com/21QTcnF3vENnlyKiYbtxrcU0VlxNlJp1Ht79pZ_GU5z3gWPxdefa79DIqjI2FvDLNz4zieFeE15y00r4DJjHMix6DVQeu-X5o_xG1g=s100","http://www.appinventor.org/apps2/ihaveadream/ihaveadream.aia",7,3,"","","");
//    //app1.setName(requestApps( "http://app-inventor-gallery.appspot.com/rpc?tag=all:0:3:asc:uid" ));
//    ArrayList<GalleryApp> list = new ArrayList<GalleryApp>();
//    list.add(app1);
//    list.add(app2);
//    return list;
	  return null;
  }

  private String getStartCountString(int start, int count) {
    return ":"+String.valueOf(start)+":"+String.valueOf(count);  
  }  
}
