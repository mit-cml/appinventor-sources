package com.google.appinventor.client;

import java.util.ArrayList;
import java.util.List;
/*
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
*/

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.shared.rpc.project.GalleryApp;

import java.io.IOException;


// web sample: http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
public class GalleryClient implements GalleryInterface {

	private final String USER_AGENT = "Mozilla/5.0";
    List<GalleryRequestListener> listeners = new ArrayList<GalleryRequestListener>();

    public void Subscribe(GalleryRequestListener listener)
    {
       listeners.add(listener);
    }

	public void FindApps(String keywords, int start, int count,
			int sortOrder) {
        // we need to deal with URL encoding!!
		requestApps("http://gallery.appinventor.mit.edu/rpc?tag=search:"+keywords);
		
	}

	public void FindByTag(String tag, int start, int count,
			int sortOrder) {
		// need to deal with URL encoding
		requestApps("http://gallery.appinventor.mit.edu/rpc?tag=tag:"+tag+":0:5");
	}

	public void GetApps(int start, int count, int sortOrder,
			String sortField) {
		// currently returns five apps sorted by uid (app id)
		requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all:0:5:asc:uid");
	}

	public void GetAppsByDeveloper(int start, int count,
			String uid) {
      // need to fix this one, i think it takes display name as the param but...
		requestApps("http://gallery.appinventor.mit.edu/rpc?tag=by_developer:");
	}

	public void GetCategories() {
     // we need another way to deal with non-list things
     //"http://gallery.appinventor.mit.edu/rpc?tag=get_categories"
     
	}

	public void GetFeatured(int start, int count, int sortOrder) {
		requestApps("http://gallery.appinventor.mit.edu/rpc?tag=featured");
	}

    public void GetMostRecent(int start, int count) {
      requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all:0:5:desc:uploadTime");
    }

	public void GetMostDownloaded(int start, int count) {
      //doesn't work, need Vince to add index
      requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all:0:5:desc:numDownloads");
	}

	public void GetMostViewed(int start, int count) {
      //doesn't work, need Vince to add index
      requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all:0:5:desc:numViewed");
	}
	public void GetMostLiked(int start, int count) {
	
		requestApps("http://gallery.appinventor.mit.edu/rpc?tag=all:0:5:desc:numLikes");
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

	public String status="xyz";
	
	
	private void requestApps(String url)  {
		int STATUS_CODE_OK = 200;
  
  		
        // Callback for when the server returns us the apps
	    final Ode ode = Ode.getInstance();
	    // was string
	    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
			      // failure message
	      MESSAGES.galleryError()) {
	        @Override
	        public void onSuccess(List<GalleryApp> list) {
	        // the server has returned us something
	        if (list== null) {
			  return;
		    }
		    // things are good so lets refresh the list
		   
			//refreshTable(list,false);
	        //Window.alert("api call: got some data:"+list.get(0).getTitle());
            for (GalleryRequestListener listener : listeners)
            {
               listener.onGalleryRequestCompleted(list);   
            }
	      }
	    };
	   // ok, this is below the call back, but of course it is done first 
	   ode.getProjectService().getApps(url, callback);
	
	}
	public List<GalleryApp> generateFakeApps()  {
		GalleryApp app1 = new GalleryApp("Sports Analyzer", "Joe Smith", "a great game","1/1/13","2/1/13","http://lh3.ggpht.com/zyfGqqiN4P8GvXFVbVf-RLC--PrEDeRCu5jovFYD6l3TXYfU5pR70HXJ3yr-87p5FUGFSxeUgOMecodBOcTFYA7frUg6QTrS5ocMcNk=s100","http://www.appinventor.org/apps2/ihaveadream/ihaveadream.aia",2,5);
		GalleryApp app2 = new GalleryApp("Basketball Quiz", "Bill Jones", "sports quiz","2/3/13","2/5/13", "http://lh5.ggpht.com/21QTcnF3vENnlyKiYbtxrcU0VlxNlJp1Ht79pZ_GU5z3gWPxdefa79DIqjI2FvDLNz4zieFeE15y00r4DJjHMix6DVQeu-X5o_xG1g=s100","http://www.appinventor.org/apps2/ihaveadream/ihaveadream.aia",7,3);
		//app1.setName(requestApps( "http://app-inventor-gallery.appspot.com/rpc?tag=all:0:3:asc:uid" ));
		ArrayList<GalleryApp> list = new ArrayList<GalleryApp>();
		list.add(app1);
		list.add(app2);
		return list;
	}
	

}
