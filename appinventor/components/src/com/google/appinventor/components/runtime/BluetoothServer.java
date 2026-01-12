// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_ADVERTISE;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SUtil;

import java.io.IOException;

import java.util.UUID;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Use the `BluetoothServer` component to turn your device into a server that receive connections
 * from other apps that use the `BluetoothClient` component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.BLUETOOTHSERVER_COMPONENT_VERSION,
    description = "Bluetooth server component",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/bluetooth.png")
@SimpleObject
@UsesPermissions({BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_ADVERTISE})
public final class BluetoothServer extends BluetoothConnectionBase {
  private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

  private final Handler androidUIHandler;

  private final AtomicReference<BluetoothServerSocket> arBluetoothServerSocket;

  /**
   * Creates a new BluetoothServer.
   */
  public BluetoothServer(ComponentContainer container) {
    super(container, "BluetoothServer");
    androidUIHandler = new Handler(Looper.getMainLooper());
    arBluetoothServerSocket = new AtomicReference<>();
  }

  /**
   * Accept an incoming connection with the Serial Port Profile (SPP).
   */
  @SimpleFunction(description = "Accept an incoming connection with the Serial Port " +
      "Profile (SPP).")
  public void AcceptConnection(String serviceName) {
    accept("AcceptConnection", serviceName, SPP_UUID);
  }

  /**
   * Accept an incoming connection with a specific UUID.
   */
  @SimpleFunction(description = "Accept an incoming connection with a specific UUID.")
  public void AcceptConnectionWithUUID(String serviceName, String uuid) {
    accept("AcceptConnectionWithUUID", serviceName, uuid);
  }

  private void accept(final String functionName, final String name, final String uuidString) {
    if (SUtil.requestPermissionsForAdvertising(form, this, functionName,
        new PermissionResultHandler() {
          @Override
          public void HandlePermissionResponse(String permission, boolean granted) {
            accept(functionName, name, uuidString);
          }
        })) {
      return;
    }
    if (adapter == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_AVAILABLE);
      return;
    }

    if (!adapter.isEnabled()) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_ENABLED);
      return;
    }

    UUID uuid;
    try {
      uuid = UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_UUID, uuidString);
      return;
    }

    try {
      BluetoothServerSocket socket;
      if (!secure && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
        // listenUsingInsecureRfcommWithServiceRecord was introduced in level 10
        socket = adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
      } else {
        socket = adapter.listenUsingRfcommWithServiceRecord(name, uuid);
      }
      arBluetoothServerSocket.set(socket);
    } catch (IOException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_UNABLE_TO_LISTEN);
      return;
    }

    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        BluetoothSocket acceptedSocket = null;

        BluetoothServerSocket serverSocket = arBluetoothServerSocket.get();
        if (serverSocket != null) {
          try {
            try {
              acceptedSocket = serverSocket.accept();
            } catch (IOException e) {
              androidUIHandler.post(new Runnable() {
                public void run() {
                  form.dispatchErrorOccurredEvent(BluetoothServer.this, functionName,
                      ErrorMessages.ERROR_BLUETOOTH_UNABLE_TO_ACCEPT);
                }
              });
              return;
            }
          } finally {
            StopAccepting();
          }
        }

        if (acceptedSocket != null) {
          // Call setConnection and signal the event on the main thread.
          final BluetoothSocket bluetoothSocket = acceptedSocket;
          androidUIHandler.post(new Runnable() {
            public void run() {
              try {
                setConnection(bluetoothSocket);
              } catch (IOException e) {
                Disconnect();
                form.dispatchErrorOccurredEvent(BluetoothServer.this, functionName,
                    ErrorMessages.ERROR_BLUETOOTH_UNABLE_TO_ACCEPT);
                return;
              }

              ConnectionAccepted();
            }
          });
        }
      }
    });
  }

  /**
   * Returns true if this BluetoothServer component is accepting an
   * incoming connection.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public final boolean IsAccepting() {
    return (arBluetoothServerSocket.get() != null);
  }

  /**
   * Stop accepting an incoming connection.
   */
  @SimpleFunction(description = "Stop accepting an incoming connection.")
  public void StopAccepting() {
    BluetoothServerSocket serverSocket = arBluetoothServerSocket.getAndSet(null);
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        Log.w(logTag, "Error while closing bluetooth server socket: " + e.getMessage());
      }
    }
  }

  /**
   * Indicates that a bluetooth connection has been accepted.
   */
  @SimpleEvent(description = "Indicates that a bluetooth connection has been accepted.")
  public void ConnectionAccepted() {
    Log.i(logTag, "Successfullly accepted bluetooth connection.");
    EventDispatcher.dispatchEvent(this, "ConnectionAccepted");
  }
}
