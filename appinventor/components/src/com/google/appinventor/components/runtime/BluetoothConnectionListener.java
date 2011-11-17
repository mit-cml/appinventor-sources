// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

/**
 * Callback for receiving Bluetooth connection events
 *
 * @author lizlooney@google.com (Liz Looney)
 */
interface BluetoothConnectionListener {
  /**
   *
   */
  void afterConnect(BluetoothConnectionBase bluetoothConnection);

  /**
   *
   */
  void beforeDisconnect(BluetoothConnectionBase bluetoothConnection);
}
