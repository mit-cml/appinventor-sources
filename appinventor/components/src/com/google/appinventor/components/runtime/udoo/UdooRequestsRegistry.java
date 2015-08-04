// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * Keeps track of request ID and the corresponding response queue.
 * 
 * @author Francesco Montefoschi francesco.monte@gmail.com
 */
class UdooRequestsRegistry
{
  private static final HashMap<Integer, BlockingQueue<JSONObject>> registry = new HashMap();
  
  public static void register(int id, BlockingQueue<JSONObject> queue) {
    registry.put(id, queue);
  }
  
  public static void onRead(JSONObject response, Integer id) {
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
