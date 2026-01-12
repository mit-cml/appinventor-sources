// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import android.os.Handler;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsyncCallbackPair;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

// When the component is installed in App Inventor, the Javadoc
// comments will become included in the automatically-generated system
// documentation, except for lines starting with tags (such as @author).

/**
 * The MediaStore component communicates with a web service to store media objects. This component
 * has a single method that stores a media object in the services blob store, and returns a pointer
 * to the object via a url. The accompanying Web service is at (http://ai-mediaservice.appspot.com).
 *
 * This component is currently in development, and more functionality will be added.
 *
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney)
 */

// The annotations here provide information to the compiler about
// integrating the component into App Inventor system.  The following
// three annotations stipulate that MediaStore will appear in the
// designer, that it will be an object in the App Inventor language,
// and say what Android system permissions it requires.
//

@DesignerComponent(version = YaVersion.MEDIASTORE_COMPONENT_VERSION,
    description = "Non-visible component that communicates with a Web service and stores media " +
        "files.",
    category = ComponentCategory.INTERNAL,
    nonVisible = true,
    iconName = "images/mediastore.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries("httpmime.jar")
public final class MediaStore extends AndroidNonvisibleComponent implements Component {
  protected final ComponentContainer componentContainer;
  private static final String LOG_TAG_COMPONENT = "MediaStore: ";
  private String serviceURL;
  private Handler androidUIHandler;

  /**
   * Creates a new MediaStore component.
   *
   * @param container the Form that this component is contained in.
   */
  public MediaStore(ComponentContainer container) {
    super(container.$form());
    componentContainer = container;
    androidUIHandler = new Handler();
    serviceURL = "http://ai-mediaservice.appspot.com";
  }

  /**
   * Returns the URL of the MediaStore web service.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String ServiceURL() {
    return serviceURL;
  }

  /**
   * Specifies the URL of the  Web service.
   * The default value is the App Inventor MediaStore service running on App Engine.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "http://ai-mediaservice.appspot.com")
  @SimpleProperty
  public void ServiceURL(String url) {
    serviceURL = url;
  }

  /**
   * Asks the Web service to store the given media file.
   *
   * @param mediafile The value to store.
   */
  @SimpleFunction
  public void PostMedia(String mediafile) throws FileNotFoundException{
    AsyncCallbackPair<String> myCallback = new AsyncCallbackPair<String>() {
      public void onSuccess(final String response) {
        androidUIHandler.post(new Runnable() {
          public void run() {
            MediaStored(response);
          }
        });
      }
      public void onFailure(final String message) {
        androidUIHandler.post(new Runnable() {
          public void run() {
            WebServiceError(message);
          }
        });
      }
    };

    try {
      HttpClient client = new DefaultHttpClient();

      MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
      entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

      String[] pathtokens = mediafile.split("/");
      String newMediaPath;

      if (pathtokens[0].equals("file:")) {
        newMediaPath = new java.io.File(new URL(mediafile).toURI()).getAbsolutePath();
      } else {
        newMediaPath = mediafile;
      }

      File media = new File(newMediaPath);
      entityBuilder.addPart("file", new FileBody(media));

      HttpEntity entity = entityBuilder.build();

      String uploadURL = getUploadUrl();
      HttpPost post = new HttpPost(uploadURL);
      post.setEntity(entity);
      HttpResponse response = client.execute(post);

      HttpEntity httpEntity = response.getEntity();
      String result = EntityUtils.toString(httpEntity);
      myCallback.onSuccess(result);
    } catch (Exception e) {
      e.printStackTrace();
      myCallback.onFailure(e.getMessage());
    }
  }

  private String getUploadUrl() {
    try {
      String url = serviceURL;
      URL obj = new URL(url);

      HttpURLConnection con = (HttpURLConnection) obj.openConnection();

      // optional default is GET
      con.setRequestMethod("GET");

      // add request header
      String USER_AGENT = "AppInventor";
      con.setRequestProperty("User-Agent", USER_AGENT);
      con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

      // get and build upload URL from response
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      StringBuilder response = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      // return upload URL
      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * Indicates that a MediaStored server request has succeeded.
   *
   * @param url the value that was returned.
   */
  @SimpleEvent
  public void MediaStored(String url) {
    EventDispatcher.dispatchEvent(this, "MediaStored", url);
  }

  /**
   * Indicates that the communication with the Web service signaled an error
   *
   * @param message the error message
   */
  @SimpleEvent
  public void WebServiceError(String message) {
    EventDispatcher.dispatchEvent(this, "WebServiceError", message);
  }
}
