// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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
import com.google.appinventor.components.runtime.util.ErrorMessages;
import java.util.ArrayList;
import java.util.List;

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
  private boolean waitForResponse = false;

  private static UdooAdkBroadcastReceiver instance = null;
  protected UdooAdkBroadcastReceiver() {
  }
  public static UdooAdkBroadcastReceiver getInstance() {
    if (instance == null) {
      instance = new UdooAdkBroadcastReceiver();
    }
    return instance;
  }

  public void onCreate(ContextWrapper wrapper)
  {
    activity = wrapper;
    usbManager = (UsbManager)activity.getSystemService(Context.USB_SERVICE);
    registerReceiver();
  }

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

  public synchronized void disconnect()
  {
    this.connected = false;
    notifyAll();

    if (fileDescriptor != null) {
      try {
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
    Log.d(TAG, "[UdooAdkBroadcastReceiver] Connect");

    if (!this.waitForResponse) {
      this.connected = false;
      tryOpen();
    }
  }

  private boolean tryOpen()
  {
    UsbAccessory[] accessories = usbManager.getAccessoryList();
    UsbAccessory accessory = null;
    if (accessories == null) {
      Log.v(TAG, "No accessories found!");
      form.dispatchErrorOccurredEvent((Component)connectedComponents.get(0), "tryOpen", ErrorMessages.ERROR_UDOO_ADK_NO_DEVICE);
      return false;
    }
    for (UsbAccessory iaccessory : accessories) {
      if (iaccessory != null && iaccessory.getManufacturer().equals("UDOO")) {
        accessory = iaccessory;
      }
    }

    if (accessory == null) {
      Log.v(TAG, "No accessory found.");
      form.dispatchErrorOccurredEvent((Component)connectedComponents.get(0), "tryOpen", ErrorMessages.ERROR_UDOO_ADK_NO_DEVICE);
      return false;
    }
    
    if (!usbManager.hasPermission(accessory)) {
      Log.v(TAG, "No permissions, requesting");
      if (pendingIntent == null) {
        Log.v(TAG, "Requesting permission.");
        pendingIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(accessory, pendingIntent);
      }
      return false;
    }
    
    boolean success = false;

    try {
      fileDescriptor = usbManager.openAccessory(accessory);
      if (fileDescriptor == null) {
        Log.v(TAG, "Failed to open file descriptor.");
        return false;
      }

      try {
        FileDescriptor fd = fileDescriptor.getFileDescriptor();
        inputStream = new FileInputStream(fd);
        outputStream = new FileOutputStream(fd);

        this.waitForResponse = true;

        outputStream.write('H');
        outputStream.flush();

        Log.d(TAG, "Connecting...");

        // We're going to block now. We're counting on the IOIO to
        // write back a byte, or otherwise we're locked until
        // physical disconnection. This is a known OpenAccessory
        // bug:
        // http://code.google.com/p/android/issues/detail?id=20545
        while (inputStream.read() < 0) {
          trySleep(500);
        }

        this.waitForResponse = false;

        Log.d(TAG, "Connected!");

        this.arduino = new UdooArduinoManager(outputStream, inputStream, this);
        this.arduino.hi();
        this.connected = true;

        for (UdooConnectedInterface c : connectedComponents) {
          c.Connected();
        }

        success = true;
        return true;
      } catch (IOException e) {
        this.connected = false;
        Log.v(TAG, "Failed to open streams", e);
        return false;
      } finally {
        if (!success) {
          try {
            fileDescriptor.close();
          } catch (IOException e) {
            Log.e(TAG, "Failed to close file descriptor.", e);
          }
          fileDescriptor = null;
        }
      }
    } finally {
      if (!success && pendingIntent != null) {
        pendingIntent.cancel();
        pendingIntent = null;
      }
    }
  }
    
  private void trySleep(long time) {
    synchronized (UdooAdkBroadcastReceiver.this) {
      try {
        UdooAdkBroadcastReceiver.this.wait(time);
      } catch (InterruptedException e) {
      }
    }
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

  public boolean isConnected()
  {
    return this.connected;
  }

  public void registerComponent(UdooConnectedInterface component, Form form)
  {
    this.connectedComponents.add(component);
    this.form = form;
  }

  @Override
  public UdooArduinoManager arduino() {
    return this.arduino;
  }
}
