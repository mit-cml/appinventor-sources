// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * These commands post to the Web and get responses that are assumed
 * to be JSON structures: a string, a JSON array, or a JSON object.
 * It's up to the caller of these routines to decide which version
 * to use, and to decode the response.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
public class WebServiceUtil {

  private static final WebServiceUtil INSTANCE = new WebServiceUtil();
  private static final String LOG_TAG = "WebServiceUtil";
  private static HttpClient httpClient = null;
  private static Object httpClientSynchronizer = new Object();

  private WebServiceUtil(){
  }

  /**
   * Returns the one <code>WebServiceUtil</code> instance
   * @return the one <code>WebServiceUtil</code> instance
   */
  public static WebServiceUtil getInstance() {
    // This needs to be here instead of in the constructor because
    // it uses classes that are in the AndroidSDK and thus would
    // cause Stub! errors when running the component descriptor.
    synchronized(httpClientSynchronizer) {
      if (httpClient == null) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        BasicHttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
        HttpConnectionParams.setSoTimeout(params, 20 * 1000);
        ConnManagerParams.setMaxTotalConnections(params, 20);
        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params,
            schemeRegistry);
        WebServiceUtil.httpClient = new DefaultHttpClient(manager, params);
      }
    }
    return INSTANCE;
  }

  /**
   * Make a post command to serviceURL with params and return the
   * response String as a JSON array.
   *
   * @param serviceURL The URL of the server to post to.
   * @param commandName The path to the command.
   * @param params A List of NameValuePairs to send as parameters
   * with the post.
   * @param callback A callback function that accepts a JSON array
   * on success.
   */
  public void postCommandReturningArray(String serviceURL, String commandName,
      List<NameValuePair> params, final AsyncCallbackPair<JSONArray> callback) {
    AsyncCallbackPair<String> thisCallback = new AsyncCallbackPair<String>() {
      public void onSuccess(String httpResponseString) {
        try {
          callback.onSuccess(new JSONArray(httpResponseString));
        } catch (JSONException e) {
          callback.onFailure(e.getMessage());
        }
      }
      public void onFailure(String failureMessage) {
        callback.onFailure(failureMessage);
      }
    };
    postCommand(serviceURL, commandName, params, thisCallback);
  }

  /**
   * Make a post command to serviceURL with paramaterss and
   * return the response String as a JSON object.
   *
   * @param serviceURL The URL of the server to post to.
   * @param commandName The path to the command.
   * @param params A List of NameValuePairs to send as parameters
   * with the post.
   * @param callback A callback function that accepts a JSON object
   * on success.
   */
  public void postCommandReturningObject(final String serviceURL,final String commandName,
      List<NameValuePair> params, final AsyncCallbackPair<JSONObject> callback) {
    AsyncCallbackPair<String> thisCallback = new AsyncCallbackPair<String>() {
    public void onSuccess(String httpResponseString) {
        try {
          callback.onSuccess(new JSONObject(httpResponseString));
        } catch (JSONException e) {
          callback.onFailure(e.getMessage());
        }
      }
      public void onFailure(String failureMessage) {
        callback.onFailure(failureMessage);
      }
    };
    postCommand(serviceURL, commandName, params, thisCallback);
  }

  /**
   * Make a post command to serviceURL with params and return the
   * response String.
   *
   * @param serviceURL The URL of the server to post to.
   * @param commandName The path to the command.
   * @param params A List of NameValuePairs to send as parameters
   * with the post.
   * @param callback A callback function that accepts a String on
   * success.
   */
  public void postCommand(final String serviceURL, final String commandName,
      List<NameValuePair> params, AsyncCallbackPair<String> callback) {
    Log.d(LOG_TAG, "Posting " + commandName + " to " + serviceURL + " with arguments " + params);

    if (serviceURL == null || serviceURL.equals("")) {
      callback.onFailure("No service url to post command to.");
    }
    final HttpPost httpPost = new HttpPost(serviceURL + "/" + commandName);

    if (params == null) {
      params = new ArrayList<NameValuePair>();
    }
    try {
      String httpResponseString;
      ResponseHandler<String> responseHandler = new BasicResponseHandler();
      httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
      httpPost.setHeader("Accept", "application/json");
      httpResponseString = httpClient.execute(httpPost, responseHandler);
      callback.onSuccess(httpResponseString);
    } catch (UnsupportedEncodingException e) {
      Log.w(LOG_TAG, e);
      callback.onFailure("Failed to encode params for web service call.");
    } catch (ClientProtocolException e) {
      Log.w(LOG_TAG, e);
      callback.onFailure("Communication with the web service encountered a protocol exception.");
    } catch (IOException e) {
      Log.w(LOG_TAG, e);
      callback.onFailure("Communication with the web service timed out.");
    }
  }
}
