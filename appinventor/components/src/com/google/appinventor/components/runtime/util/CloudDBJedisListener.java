// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//Notification Listener from: http://stackoverflow.com/questions/26406303/redis-key-expire-notification-with-jedis

package com.google.appinventor.components.runtime.util;

import android.util.Log;

import com.google.appinventor.components.runtime.CloudDB;
import com.google.appinventor.components.runtime.util.JsonUtil;

import java.util.List;
import java.util.Set;

import org.json.JSONException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class CloudDBJedisListener extends JedisPubSub {
  private static final boolean DEBUG = false;
  public CloudDB cloudDB;
  private Thread myThread;
  private static String LOG_TAG = "CloudDB"; // Yep, same as the CloudDB component.
                                             // This is on purpose because when we
                                             // are looking at logs for CloudDB
                                             // we want to know about us as well

  public CloudDBJedisListener(CloudDB thisCloudDB){
    cloudDB = thisCloudDB;
    myThread = Thread.currentThread();
  }

  @Override
  public void onSubscribe(String channel, int subscribedChannels) {
    if (DEBUG) {
      Log.d(LOG_TAG, "onSubscribe " + channel + " " + subscribedChannels);
    }
  }

  @Override
  public void onMessage(String channel, String message) {
    if (DEBUG) {
      Log.d(LOG_TAG, "onMessage channel " + channel + ", message: " + message);
    }
    try {
    // Message is a JSON encoded list of the tag that was just set and its value
      List<Object> data = null;
      data = (List<Object>) JsonUtil.getObjectFromJson((String) message);
      if (DEBUG) {
        Log.d(LOG_TAG, "onMessage: data = " + data);
      }
      String tag = (String) data.get(0);   // The variable that was changed
      List<Object> valueList = (List<Object>) data.get(1);
      for (Object value : valueList) {
        // Note: DataChanged will arrange to dispatch the event
        // on the UI thread.
        String retValue = JsonUtil.getJsonRepresentationIfValueFileName(value);
        if (retValue == null) {
          cloudDB.DataChanged(tag, value);
        } else {
          cloudDB.DataChanged(tag, retValue);
        }
      }
    } catch (JSONException e) {
      Log.e(LOG_TAG, "onMessage: JSONException", e);
      // CloudDBError arranges to generate the error UI on the
      // UI Thread
      cloudDB.CloudDBError("System Error: " + e.getMessage());
    }
  }

  public void terminate() {
    myThread.interrupt();
  }

  //add other Unimplemented methods
}
