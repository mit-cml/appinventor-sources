// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdooTcpRedirector implements UdooConnectionInterface
{
  private static final String TAG = "UdooTcpRedirector";
  private Socket socket;
  private String address = null;
  private String port = null;
  private String secret = null;
  private boolean connected = false;
  private boolean isConnecting;
  public UdooArduinoManager arduino;
  List<UdooConnectedInterface> connectedComponents = new ArrayList<UdooConnectedInterface>();
  Form form;
  
  UdooTcpRedirector(String address, String port, String secret) {
    this.address = address;
    this.port = port;
    this.secret = secret;
  }

  @Override
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  public synchronized void disconnect() {
    if (this.arduino != null) {
      Log.d(TAG, "Stopping ArduinoManager");
      this.arduino.disconnect();
      this.arduino.stop();
      this.arduino = null;
    }
    
    this.connected = false;
    this.isConnecting = false;
    notifyAll();
    
    if (this.socket != null) {
      try {
        this.socket.close();
      } catch (IOException ex) {
        Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    this.connected = false;
  }

  @Override
  public UdooArduinoManager arduino() {
    return this.arduino;
  }

  public synchronized void connect()
  {
    this.isConnecting = true;
    Log.d(TAG, "[UdooTcpRedirector] Connect(" + this.address+ ":" + this.port + ")");
    try {
      this.socket = (new CreateSocketTask()).execute(this.address, this.port).get(5000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ex) {
      Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ExecutionException ex) {
      Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
    } catch (TimeoutException ex) {
      Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    if (this.socket == null) {
      Log.d(TAG, "[UdooTcpRedirector] Could not create socket!");
      this.connected = false;
      this.isConnecting = false;
      return;
    }
    
    OutputStream out;
    InputStream in;
    try {
      out = this.socket.getOutputStream();
      in = this.socket.getInputStream();
    } catch (IOException ex) {
      Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
      return;
    }
    
    this.arduino = new UdooArduinoManager(out, in, this);
    if (!this.arduino.hi()) {
      this.connected = false;
      this.isConnecting = false;
      return;
    }
    
    this.connected = true;
    this.isConnecting = false;
    for (UdooConnectedInterface c : connectedComponents) {
      Log.d(TAG, "notify " + c.toString());
      c.Connected();
    }
  }
  
  @Override
  public void reconnect()
  {
    this.disconnect();
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    this.connect();
  }

  @Override
  public void registerComponent(UdooConnectedInterface component, Form form) {
    this.connectedComponents.add(component);
    this.form = form;
  }

  @Override
  public void onCreate(ContextWrapper ctx) {
  }

  @Override
  public void onDestroy() {
    disconnect();
  }

  void setAddress(String address) {
    boolean changed = !address.equals(this.address);
    this.address = address;
    if (this.address != null && this.port != null && changed) {
      connect();
    }
  }
  
  void setPort(String port) {
    boolean changed = !port.equals(this.port);
    this.port = port;
    if (this.address != null && this.port != null && changed) {
      connect();
    }
  }
  
  void setArduino(InputStream in, OutputStream out) {
    this.arduino = new UdooArduinoManager(out, in, this);
    this.arduino.hi();
    this.connected = true;
  }

  @Override
  public boolean isConnecting() {
    return this.isConnecting;
  }
  
  
  private class CreateSocketTask extends AsyncTask<String, Void, Socket> {

    protected Socket doInBackground(String... args) {
      try {
        InetAddress serverAddr = InetAddress.getByName(args[0]);
        return new Socket(serverAddr, Integer.parseInt(args[1]));
      } catch (ConnectException ex) {
        form.dispatchErrorOccurredEvent((Component)connectedComponents.get(0), "doInBackground", ErrorMessages.ERROR_UDOO_TCP_NO_CONNECTION);
        Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
        Logger.getLogger(UdooTcpRedirector.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
    }
  }
}
