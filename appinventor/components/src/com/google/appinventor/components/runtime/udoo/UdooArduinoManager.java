// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdooArduinoManager
{
  private final OutputStream outputStream;
  private final UdooConnectionInterface udooConnection;
  private final UdooArduinoReader udooArduinoReader;
  
  public final int MIN_SKETCH_VERSION = 1;
  private Random rand;
  
  public UdooArduinoManager(OutputStream outputStream, InputStream inputStream, UdooConnectionInterface udooConnection)
  {
    this.outputStream = outputStream;
    this.udooConnection = udooConnection;
    this.udooArduinoReader = new UdooArduinoReader(inputStream);
  }

  public static final int HIGH = 1;
  public static final int LOW = 0;
  public static final int OUTPUT = 1;
  public static final int INPUT = 0;
  
  public boolean hi()
  {
    JSONObject json = new JSONObject();
    try {
      json.put("method", "hi");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    JSONObject response = sendJson(json);
    
    try {
      boolean success = ((Boolean) response.get("success")).booleanValue();
      
      if (success) {
        int version = (Integer) response.get("version");
        if (version < MIN_SKETCH_VERSION) {
          throw new RuntimeException("Arduino sketch too old.");
        }
        return true;
      }
      return false;
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    throw new RuntimeException("Invalid response from ADK");
  }
  
  public void digitalWrite(String pin, String value)
  {
    int lowhigh = HIGH;
    if (value.toUpperCase().charAt(0) == 'L') {
      lowhigh = LOW;
    }

    JSONObject json = new JSONObject();
    try {
      json.put("method", "digitalWrite");
      json.put("pin", pinNameToInt(pin));
      json.put("value", lowhigh);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    sendJson(json);
  }
  
  public void analogWrite(String pin, int value)
  {
    JSONObject json = new JSONObject();
    try {
      json.put("method", "analogWrite");
      json.put("pin", pinNameToInt(pin));
      json.put("value", value);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    sendJson(json);
  }
  
  public void pinMode(String pin, String value)
  {
    int inout = OUTPUT;
    if (value.toUpperCase().charAt(0) == 'I') {
      inout = INPUT;
    }
      
    JSONObject json = new JSONObject();
    try {
      json.put("method", "pinMode");
      json.put("pin", pinNameToInt(pin));
      json.put("value", inout);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    sendJson(json);
  }
  
  public void delay(int value)
  {
    JSONObject json = new JSONObject();
    try {
      json.put("method", "delay");
      json.put("value", value);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    sendJson(json);
  }
  
  public int digitalRead(String pin) throws Exception
  {
    JSONObject json = new JSONObject();
    try {
      json.put("method", "digitalRead");
      json.put("pin", pinNameToInt(pin));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    JSONObject response = sendJson(json);
    
    try {
      boolean success = ((Boolean) response.get("success")).booleanValue();
      int value = (Integer) response.get("value");
      
      if (success) {
        return value;
      }
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    throw new Exception("Invalid response from ADK");
  }
  
  public int analogRead(String pin) throws Exception
  {
    JSONObject json = new JSONObject();
    try {
      json.put("method", "analogRead");
      json.put("pin", pinNameToInt(pin));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    JSONObject response = sendJson(json);
    
    try {
      boolean success = ((Boolean) response.get("success")).booleanValue();
      int value = (Integer) response.get("value");
      
      if (success) {
        return value;
      }
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    throw new Exception("Invalid response from ADK");
  }
  
  public int map(int value, int fromLow, int fromHigh, int toLow, int toHigh) throws Exception
  {
    JSONObject json = new JSONObject();
    try {
      json.put("method", "map");
      json.put("value", value);
      json.put("fromLow", fromLow);
      json.put("fromHigh", fromHigh);
      json.put("toLow", toLow);
      json.put("toHigh", toHigh);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    JSONObject response = sendJson(json);
    
    try {
      boolean success = ((Boolean) response.get("success")).booleanValue();
      
      if (success) {
        return (Integer) response.get("value");
      }
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    throw new Exception("Invalid response from ADK");
  }
  
  public JSONObject sensor(String pin, String sensorName) throws Exception
  {
    JSONObject json = new JSONObject();
    try {
      json.put("sensor", sensorName);
      json.put("pin", pinNameToInt(pin));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    JSONObject response = sendJson(json);
    
    try {
      boolean success = ((Boolean) response.get("success")).booleanValue();
      
      if (success) {
        return response;
      }
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    throw new Exception("Invalid response from ADK");
  }
  
  
  private JSONObject sendJson(JSONObject json)
  {
    int id = generateId();
    try {
      json.put("id", id);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    String request = json.toString()+'\n';
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
      
      Log.d("Udoo", "XXX" + e.getMessage() + "XXX");
      Logger.getLogger(UdooArduinoManager.class.getName()).log(Level.SEVERE, null, e);
      udooConnection.disconnect();
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
      }
      udooConnection.connect();

      e.printStackTrace();
      Log.d("MANNAGGIA", "cagnastoriaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
      return null;
    }
    
    try {
      JSONObject obj = queue.take();
      Log.d("UdooRESPONSE", obj.toString());
      return obj;
    } catch (InterruptedException ex) {
      Logger.getLogger(UdooArduinoManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    Log.d("MANNAGGIA", "bruttissima storiaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    return null;
  }
  
  private int pinNameToInt(String name)
  {
    name = name.trim().toUpperCase();
    
    if (name.equals("A0")) {
      return 54;
    } else if (name.equals("A1")) {
      return 55;
    } else if (name.equals("A2")) {
      return 56;
    } else if (name.equals("A3")) {
      return 57;
    } else if (name.equals("A4")) {
      return 58;
    } else if (name.equals("A5")) {
      return 59;
    } else if (name.equals("A6")) {
      return 60;
    } else if (name.equals("A7")) {
      return 61;
    } else {
      return Integer.parseInt(name);
    }
  }
  
  public int generateId()
  {
    if (rand == null) {
      rand = new Random();
    }
    
    int min=100000000;
    int max=999999999;

    return rand.nextInt((max - min) + 1) + min;
}

  void stop()
  {
    this.udooArduinoReader.stop();
  }
}
