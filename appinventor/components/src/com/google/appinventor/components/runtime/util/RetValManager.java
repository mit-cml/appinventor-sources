// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2013-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
// This work is licensed under a Creative Commons Attribution 3.0 Unported License.

package com.google.appinventor.components.runtime.util;

import android.util.Log;

import com.google.appinventor.components.runtime.PhoneStatus;
import com.google.appinventor.components.runtime.ReplForm;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/*
 * A Class for managing return values from evaluating Repl Forms and
 * stashing them in a JSON Array for return to the Blocks Editor from the
 * Companion.
 */

public class RetValManager {

  private static final String LOG_TAG = "RetValManager";
  private static final Object semaphore = new Object();
  private static final long TENSECONDS = 10000; // Ten Seconds (in milliseconds)

  // There can be only one!
  private static ArrayList<JSONObject> currentArray = new ArrayList<JSONObject>(10);

  // Need a better place for this version string, but for various reasons, this is how we
  // are going to do this for now...

  // Prevent instantiation, we are only called statically
  private RetValManager() {
  }

  /*
   * appendReturnValue -- Add a result, already encoded as a String to
   * the array of pending values.
   *
   * @param blockid The block id of the block this is for (-1 for no particular block)
   * @param ok Indication of success or failure
   * @param item The item to append
   */
  public static void appendReturnValue(String blockid, String ok, String item) {
    synchronized (semaphore) {
      JSONObject retval = new JSONObject();
      try {
        retval.put("status", ok);
        retval.put("type", "return");
        retval.put("value", item);
        retval.put("blockid", blockid);
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Error building retval", e);
        return;
      }
      boolean sendNotify = currentArray.isEmpty();
      currentArray.add(retval);
      if (PhoneStatus.getUseWebRTC()) {
        webRTCsendCurrent();
      } else if (sendNotify) {
        semaphore.notifyAll();
      }
    }
  }

  public static void sendError(String error) {
    synchronized (semaphore) {
      JSONObject retval = new JSONObject();
      try {
        retval.put("status", "OK");
        retval.put("type", "error");
        retval.put("value", error);
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Error building retval", e);
        return;
      }
      boolean sendNotify = currentArray.isEmpty();
      currentArray.add(retval);
      if (PhoneStatus.getUseWebRTC()) {
        webRTCsendCurrent();
      } else if (sendNotify) {
        semaphore.notifyAll();
      }
    }
  }

  /*
   * pushScreen -- Push to a new Screen
   *
   * @param screenName The screen to go to.
   * @param value The value to hand it
   */
  public static void pushScreen(String screenName, Object value) {
    synchronized (semaphore) {
      JSONObject retval = new JSONObject();
      try {
        retval.put("status", "OK");
        retval.put("type", "pushScreen");
        retval.put("screen", screenName);
        if (value != null)
          retval.put("value", value.toString());
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Error building retval", e);
        return;
      }
      boolean sendNotify = currentArray.isEmpty();
      currentArray.add(retval);
      if (PhoneStatus.getUseWebRTC()) {
        webRTCsendCurrent();
      } else if (sendNotify) {
        semaphore.notifyAll();
      }
    }
  }

  /*
   * popScreen -- Pop to a Previous Screen
   *
   * @param screenName The screen to go to.
   * @param value The value to hand it
   */
  public static void popScreen(String value) {
    synchronized (semaphore) {
      JSONObject retval = new JSONObject();
      try {
        retval.put("status", "OK");
        retval.put("type", "popScreen");
        if (value != null)
          retval.put("value", value.toString());
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Error building retval", e);
        return;
      }
      boolean sendNotify = currentArray.isEmpty();
      currentArray.add(retval);
      if (PhoneStatus.getUseWebRTC()) {
        webRTCsendCurrent();
      } else if (sendNotify) {
        semaphore.notifyAll();
      }
    }
  }

  /*
   * assetTransferred
   *
   * @param name name of the asset transferred
   */
  public static void assetTransferred(String name) {
    synchronized (semaphore) {
      JSONObject retval = new JSONObject();
      try {
        retval.put("status", "OK");
        retval.put("type", "assetTransferred");
        if (name != null)
          retval.put("value", name.toString());
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Error building retval", e);
        return;
      }
      boolean sendNotify = currentArray.isEmpty();
      currentArray.add(retval);
      if (PhoneStatus.getUseWebRTC()) {
        webRTCsendCurrent();
      } else if (sendNotify) {
        semaphore.notifyAll();
      }
    }
  }

  /*
   * extensionsLoaded
   *
   */
  public static void extensionsLoaded() {
    synchronized (semaphore) {
      JSONObject retval = new JSONObject();
      try {
        retval.put("status", "OK");
        retval.put("type", "extensionsLoaded");
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Error building retval", e);
        return;
      }
      boolean sendNotify = currentArray.isEmpty();
      currentArray.add(retval);
      if (PhoneStatus.getUseWebRTC()) {
        webRTCsendCurrent();
      } else if (sendNotify) {
        semaphore.notifyAll();
      }
    }
  }

  /*
   * fetch -- Fetch all pending results as a JSON encoded array.
   *
   * NOTE: This code is not used when we are using webrtc
   *
   * @param block true if we should block waiting for results
   * @return String The JSON encoded array.
   */
  public static String fetch(boolean block) {
    long startTime = System.currentTimeMillis();
    synchronized (semaphore) {
      while (currentArray.isEmpty() && block) {
        long time = System.currentTimeMillis();
        if ((time - startTime) > (TENSECONDS - 100)) // Time to give up...
          break;
        try {
          semaphore.wait(TENSECONDS);
        } catch (InterruptedException e) {
        }
      }
      JSONArray arrayoutput = new JSONArray(currentArray);
      JSONObject output = new JSONObject();
      try {
        output.put("status", "OK");
        output.put("values", arrayoutput);
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Error fetching retvals", e);
        return("{\"status\" : \"BAD\", \"message\" : \"Failure in RetValManager\"}");
      }
      currentArray.clear();       // empty it out
      return output.toString();
    }
  }

  // Only used for webrtc. Note: Our caller is holding "semphore" so we don't
  // need to worry about multi-thread synchonization here
  private static void webRTCsendCurrent() {
    try {
      JSONObject output = new JSONObject();
      output.put("status", "OK");
      output.put("values", new JSONArray(currentArray));
      ReplForm.returnRetvals(output.toString());
    } catch (JSONException e) {
      Log.e(LOG_TAG, "Error building retval", e);
      return;
    }
    currentArray.clear();
  }

}
