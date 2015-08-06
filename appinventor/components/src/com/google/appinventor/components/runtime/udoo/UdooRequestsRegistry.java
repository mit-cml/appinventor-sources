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
  private static final HashMap<Integer, UdooInterruptibleInterface> interrupts = new HashMap();
  
  public static void register(int id, BlockingQueue<JSONObject> queue) {
    registry.put(id, queue);
  }
  
  static void registerInterrupt(int interruptId, UdooInterruptibleInterface interruptible) {
    interrupts.put(interruptId, interruptible);
  }
  
  public static void onRead(JSONObject response, Integer id) {
    BlockingQueue<JSONObject> queue = registry.get(id);
    if (queue == null) {
      UdooInterruptibleInterface component = interrupts.get(id);
      if (component == null) {
        return;
      }
      
      component.InterruptFired();
      return;
    }
    
    try {
      queue.put(response);
      registry.remove(id);
      return;
    } catch (InterruptedException ex) {
      Logger.getLogger(UdooRequestsRegistry.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
