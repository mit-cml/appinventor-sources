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

import java.io.IOException;


// web sample: http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
public class GalleryClient implements GalleryInterface {

	private final String USER_AGENT = "Mozilla/5.0";

	public List<GalleryApp> FindApps(String keywords, int start, int count,
			int sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<GalleryApp> FindByTag(String tag, int start, int count,
			int sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<GalleryApp> GetApps(int start, int count, int sortOrder,
			String sortField) {
		
		return this.generateFakeApps();
	}

	public List<GalleryApp> GetAppsByDeveloper(int start, int count,
			String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> GetCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<GalleryApp> GetFeatured(int start, int count, int sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<GalleryApp> GetMostDownloaded(int start, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<GalleryApp> GetMostViewed(int start, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	public GalleryApp GetProject(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void Publish(GalleryApp app) {
		// TODO Auto-generated method stub

	}

	public void Update(GalleryApp app) {
		// TODO Auto-generated method stub

	}

	public String status="xyz";
	
	
	private String requestApps(String url)  {
		int STATUS_CODE_OK = 200;
  
  		
    	RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
	    Request response=null;
    	try {
      	     response = builder.sendRequest(null, new RequestCallback() {
              public void onError(Request request, Throwable exception) {
              // Code omitted for clarity
            	  status="error";
              }

              public void onResponseReceived(Request request, Response response) {
              // Code omitted for clarity
              	status=response.getStatusText();
              	
              
              }
        
              });
        } catch (RequestException e) {
      // Code omitted for clarity
        	status = "exception";
       }
       return status;
  
       
     }
		
		
		/* HttpClientExample http = new HttpClientExample();
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
 
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
 
		HttpResponse response = client.execute(request);
 
		
        return response.getStatusLine().getStatusCode();
 		
		BufferedReader rd = new BufferedReader(
                       new InputStreamReader(response.getEntity().getContent()));
 
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		*/
	
	
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
