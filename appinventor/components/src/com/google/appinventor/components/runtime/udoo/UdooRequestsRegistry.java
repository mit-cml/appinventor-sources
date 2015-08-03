// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import android.util.Log;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

class UdooRequestsRegistry
{
  private static final HashMap<Integer, BlockingQueue<JSONObject>> registry = new HashMap();
  
  public static void register(int id, BlockingQueue<JSONObject> queue) {
    registry.put(id, queue);
  }
  
  public static void onRead(String json) {
    JSONObject response = null;
    Integer id = null;
    try {
      response = new JSONObject(json);
      id = (Integer) response.get("id");
    } catch (JSONException ex) {
      Logger.getLogger(UdooRequestsRegistry.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    BlockingQueue<JSONObject> queue = registry.get(id);
    if (queue != null) {
      try {
        queue.put(response);
        return;
      } catch (InterruptedException ex) {
        Logger.getLogger(UdooRequestsRegistry.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
