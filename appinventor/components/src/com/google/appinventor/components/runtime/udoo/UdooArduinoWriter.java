// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

class UdooArduinoWriter extends AsyncTask<JSONObject, Void, JSONObject>
{
  private final Random rand;
  private final OutputStream outputStream;
  private final UdooConnectionInterface udooConnection;
  
  public UdooArduinoWriter(UdooConnectionInterface udooConnection, OutputStream outputStream, Random rand)
  {
    this.udooConnection = udooConnection;
    this.outputStream = outputStream;
    this.rand = rand;
  }
  
  @Override
  protected JSONObject doInBackground(JSONObject...json)
  {
    int id = generateId();
    try {
      json[0].put("id", id);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    String request = json[0].toString()+'\n';
    BlockingQueue<JSONObject> queue = new ArrayBlockingQueue(1);
    UdooRequestsRegistry.register(id, queue);
    
    try {
      Log.d("UdooREQUEST", request);
      outputStream.write(request.getBytes());
      outputStream.flush();

    } catch (IOException e) {
      
      String enodev = "ENODEV (No such device)";
      if (e.getMessage().contains(enodev)) {
        JSONObject err = new JSONObject();
        try {
          err.put("success", false);
          err.put("message", e.getMessage());
        } catch (JSONException ex) {
          ex.printStackTrace();
        }
    
        return err;
      }
      
      Logger.getLogger(UdooArduinoManager.class.getName()).log(Level.SEVERE, null, e);
      udooConnection.reconnect();

      e.printStackTrace();
      return null;
    }
    
    try {
      JSONObject obj = queue.take();
      Log.d("UdooRESPONSE", obj.toString());
      return obj;
    } catch (InterruptedException ex) {
      Logger.getLogger(UdooArduinoManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    JSONObject err = new JSONObject();
    try {
      err.put("success", false);
    } catch (JSONException ex) {}

    return err;
  }
  
  public int generateId()
  {
    int min=100000000;
    int max=999999999;

    return rand.nextInt((max - min) + 1) + min;
  }
}

