// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_ADVERTISE;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import static android.content.Context.BLUETOOTH_SERVICE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

import android.content.Context;

import android.os.Build;

import com.google.appinventor.components.runtime.BluetoothClient;
import com.google.appinventor.components.runtime.BluetoothServer;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.PermissionResultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SUtil provides logic to handle behavioral changes introduced in Android 12. On earlier versions
 * of Android, older behaviors will be used whereas on Android 12 and greater the newer behaviors
 * will apply.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class SUtil {
  /**
   * Requests Android version specific permissions needed to scan for Bluetooth devices.
   *
   * <p>Starting with Android 12, apps must ask for BLUETOOTH_SCAN permission rather than the
   * deprecated BLUETOOTH and BLUETOOTH_ADMIN permissions. This method will also request
   * fine location permission if the {@code BluetoothClient} is </p>
   *
   * @param form the form used for requesting permissions
   * @param client the component requesting the permissions
   * @param caller the calling method initiating the request, for error messages
   * @param continuation the code to execute after the permissions have been granted
   * @return true if a permission request is initiated or false if all required permissions are
   *         granted
   */
  @SuppressWarnings("unused")  // Provided for extensions
  public static boolean requestPermissionsForScanning(Form form, BluetoothClient client,
      String caller, PermissionResultHandler continuation) {
    List<String> permsNeeded = new ArrayList<>();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      permsNeeded.add(BLUETOOTH_SCAN);
    } else {
      permsNeeded.add(BLUETOOTH);
      permsNeeded.add(BLUETOOTH_ADMIN);
    }

    if (!client.NoLocationNeeded() && form.doesAppDeclarePermission(ACCESS_FINE_LOCATION)) {
      permsNeeded.add(ACCESS_FINE_LOCATION);
    }
    return performRequest(form, client, caller, permsNeeded, continuation);
  }

  /**
   * Requests Android version specific permissions needed to connect to paired Bluetooth devices.
   *
   * <p>Starting with Android 12, apps must ask for BLUETOOTH_CONNECT permission rather than the
   * deprecated BLUETOOTH and BLUETOOTH_ADMIN permissions.</p>
   *
   * @param form the form used for requesting permissions
   * @param client the component requesting the permissions
   * @param caller the calling method initiating the request, for error messages
   * @param continuation the code to execute after the permissions have been granted
   * @return true if a permission request is initiated or false if all required permissions are
   *         granted
   */
  public static boolean requestPermissionsForConnecting(Form form, BluetoothClient client,
      String caller, PermissionResultHandler continuation) {
    return requestPermissionsForS(BLUETOOTH_CONNECT, form, client, caller, continuation);
  }

  /**
   * Requests Android version specific permissions needed to connect to paired Bluetooth devices.
   *
   * <p>Starting with Android 12, apps must ask for BLUETOOTH_CONNECT permission rather than the
   * deprecated BLUETOOTH and BLUETOOTH_ADMIN permissions.</p>
   *
   * @param form the form used for requesting permissions
   * @param server the component requesting the permissions
   * @param caller the calling method initiating the request, for error messages
   * @param continuation the code to execute after the permissions have been granted
   * @return true if a permission request is initiated or false if all required permissions are
   *         granted
   */
  public static boolean requestPermissionsForAdvertising(Form form, BluetoothServer server,
      String caller, PermissionResultHandler continuation) {
    return requestPermissionsForS(BLUETOOTH_ADVERTISE, form, server, caller, continuation);
  }

  /**
   * Requests Android version specific permission {@code sdk31Permission} for Bluetooth support.
   *
   * @param sdk31Permission the permission needed by the requesting operation
   * @param form the form used for requesting permissions
   * @param source the component requesting the permission
   * @param caller the calling method initiating the request, for error messages
   * @param continuation the code to execute after the permissions have been granted
   * @return true if a permission request is initiated or false if all required permissions are
   *         granted
   */
  public static boolean requestPermissionsForS(String sdk31Permission, Form form,
      Component source, String caller, PermissionResultHandler continuation) {
    return requestPermissionsForS(new String[] { sdk31Permission }, form, source, caller,
        continuation);
  }

  /**
   * Requests Android version specific permissions {@code sdk31Permissions} for Bluetooth support.
   *
   * @param sdk31Permissions the permission needed by the requesting operation
   * @param form the form used for requesting permissions
   * @param source the component requesting the permission
   * @param caller the calling method initiating the request, for error messages
   * @param continuation the code to execute after the permissions have been granted
   * @return true if a permission request is initiated or false if all required permissions are
   *         granted
   */
  public static boolean requestPermissionsForS(String[] sdk31Permissions, Form form,
      Component source, String caller, PermissionResultHandler continuation) {
    List<String> permsNeeded = new ArrayList<>();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      Collections.addAll(permsNeeded, sdk31Permissions);
    } else {
      permsNeeded.add(BLUETOOTH);
      permsNeeded.add(BLUETOOTH_ADMIN);
    }
    return performRequest(form, source, caller, permsNeeded, continuation);
  }

  /**
   * Obtains a reference to the device's preferred BluetoothAdapter.
   *
   * @param context an Android context to use for accessing the Bluetooth service
   * @return a BluetoothAdapter reference, or null if Bluetooth is not supported
   */
  public static BluetoothAdapter getAdapter(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      BluetoothManager manager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
      return manager.getAdapter();
    } else {
      return BluetoothAdapter.getDefaultAdapter();
    }
  }

  private static boolean performRequest(Form form, Component source, String caller,
      final List<String> permsNeeded, final PermissionResultHandler continuation) {
    boolean ready = true;
    for (String permission : permsNeeded) {
      if (form.isDeniedPermission(permission)) {
        ready = false;
        break;
      }
    }
    if (!ready) {
      final String[] permissions = permsNeeded.toArray(new String[0]);
      form.askPermission(new BulkPermissionRequest(source, caller, permissions) {
        @Override
        public void onGranted() {
          continuation.HandlePermissionResponse(permsNeeded.get(0), true);
        }
      });
    }
    return !ready;
  }
}
