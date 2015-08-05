// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import java.util.ArrayList;
import java.util.List;

/**
 * ADK/USB for local App Iventor <-> UDOO connections.
 * 
 * @author Francesco Montefoschi francesco.monte@gmail.com
 */
public class UdooAdkBroadcastReceiver extends BroadcastReceiver implements UdooConnectionInterface
{
  private static final String TAG = "UDOOBroadcastReceiver";
  private static final String ACTION_USB_PERMISSION = "com.google.appinventor.components.runtime.action.USB_PERMISSION";

  private ContextWrapper activity;
  private UsbManager usbManager;
  private PendingIntent pendingIntent;
  private ParcelFileDescriptor fileDescriptor;
  private FileInputStream inputStream;
  private FileOutputStream outputStream;
  private UdooArduinoManager arduino;
  private boolean connected = false;
  List<UdooConnectedInterface> connectedComponents = new ArrayList<UdooConnectedInterface>();
  Form form;
  private boolean isConnecting;

  @Override
  public void onCreate(ContextWrapper wrapper)
  {
    activity = wrapper;
    usbManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
    registerReceiver();
  }

  @Override
  public void onDestroy()
  {
    unregisterReceiver();
  }

  @Override
  public synchronized void onReceive(Context context, Intent intent)
  {
    final String action = intent.getAction();
    if (ACTION_USB_PERMISSION.equals(action)) {
      pendingIntent = null;
      if (intent.getBooleanExtra(usbManager.EXTRA_PERMISSION_GRANTED, false)) {
        notifyAll();
        connect();
      } else {
        Log.e(TAG, "Permission denied");
        form.dispatchErrorOccurredEvent((Component)connectedComponents.get(0), "onReceive", ErrorMessages.ERROR_UDOO_ADK_NO_PERMISSIONS);
      }
    }
  }

  @Override
  public synchronized void disconnect()
  {
    if (this.arduino != null) {
      Log.d(TAG, "Stopping ArduinoManager");
      this.arduino.disconnect();
      this.arduino.stop();
      this.arduino = null;
    }
    
    this.connected = false;
    this.isConnecting = false;
    notifyAll();

    if (fileDescriptor != null) {
      try {
        Log.d(TAG, "Closing file descriptor and streams");
        inputStream.close();
        outputStream.close();
        fileDescriptor.close();
      } catch (IOException e) {
        Log.e(TAG, "Failed to close file descriptor.", e);
      }
      fileDescriptor = null;
    }

    if (pendingIntent != null) {
      pendingIntent.cancel();
      pendingIntent = null;
    }
  }

  public synchronized void connect()
  {
    Log.d(TAG, "Connecting UdooAdkBroadcastReceiver");

    this.connected = false;
    tryOpen();
  }

  private void tryOpen()
  {
    UsbAccessory[] accessories = usbManager.getAccessoryList();
    UsbAccessory accessory = null;
    if (accessories == null) {
      Log.v(TAG, "No accessories found!");
      form.dispatchErrorOccurredEvent((Component)connectedComponents.get(0), "tryOpen", ErrorMessages.ERROR_UDOO_ADK_NO_DEVICE);
      return;
    }
    for (UsbAccessory iaccessory : accessories) {
      if (iaccessory != null && iaccessory.getManufacturer().equals("UDOO")) {
        accessory = iaccessory;
      }
    }

    if (accessory == null) {
      Log.v(TAG, "No accessory found.");
      form.dispatchErrorOccurredEvent((Component)connectedComponents.get(0), "tryOpen", ErrorMessages.ERROR_UDOO_ADK_NO_DEVICE);
      return;
    }
    
    if (!usbManager.hasPermission(accessory)) {
      Log.v(TAG, "No permissions, requesting");
      if (pendingIntent == null) {
        this.isConnecting = true;
        Log.v(TAG, "Requesting permission.");
        pendingIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(accessory, pendingIntent);
      }
      return;
    }
    
    try {
      fileDescriptor = usbManager.openAccessory(accessory);
      if (fileDescriptor == null) {
        Log.v(TAG, "Failed to open file descriptor.");
        return;
      }

      FileDescriptor fd = fileDescriptor.getFileDescriptor();
      inputStream = new FileInputStream(fd);
      outputStream = new FileOutputStream(fd);

      this.arduino = new UdooArduinoManager(outputStream, inputStream, this);
      if (!this.arduino.hi()) {
        this.connected = false;
        this.isConnecting = false;
        return;
      }
      
      this.connected = true;
      this.isConnecting = false;
      for (UdooConnectedInterface c : connectedComponents) {
        c.Connected();
      }

    } finally {
      if (pendingIntent != null) {
        pendingIntent.cancel();
        pendingIntent = null;
      }
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

  private void registerReceiver() {
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    activity.registerReceiver(this, filter);
  }

  private void unregisterReceiver() {
    try {
      activity.unregisterReceiver(this);
    }
    catch (IllegalArgumentException e) {
    }
  }

  @Override
  public boolean isConnected()
  {
    return this.connected;
  }

  @Override
  public void registerComponent(UdooConnectedInterface component, Form form)
  {
    this.connectedComponents.add(component);
    this.form = form;
  }

  @Override
  public UdooArduinoManager arduino() {
    return this.arduino;
  }

  @Override
  public boolean isConnecting() {
    return this.isConnecting;
  }
}
