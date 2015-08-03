// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class UdooArduinoReader
{
  private final InputStream is;
  private Thread thread;
  private boolean running;
  
  public UdooArduinoReader(InputStream is) {
    this.is = is;
    this.registerReader(this.is);
    this.running = true;
  }
  
  private void registerReader(final InputStream inputStream)
  {
    this.thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (running) {
          String readResponse = this.read();
          if (readResponse != null) {
//            Log.d("Pushing: ", readResponse);
            UdooRequestsRegistry.onRead(readResponse);
          }

          try {
            Thread.sleep(50);
          } catch (InterruptedException ex) {
          }
        }
      }
      
      private String read()
      {
        byte[] buffer = new byte[256];
        byte[] response;
        String message = null;
        int mByteRead = -1;

        try {
          if (inputStream instanceof FileInputStream) {
            mByteRead = inputStream.read(buffer, 0, buffer.length);
            if (mByteRead != -1) {
              response = Arrays.copyOfRange(buffer, 0, mByteRead);
              message = new String(response).trim();
            }
          } else {
            message = "";
            do {
              mByteRead = inputStream.read(buffer, 0, buffer.length);
              message += new String(Arrays.copyOfRange(buffer, 0, mByteRead));
            } while (!message.contains("\n"));
            message = message.trim();
          }

        } catch (IOException e) {
          Log.e("ARDUINO IO Exception", e.getMessage());
          stop();
          message = null;
        }

        return message;
      }
    });
    
    thread.start();
  }

  void stop() {
    this.running = false;
  }

  
}
