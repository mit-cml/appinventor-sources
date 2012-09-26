// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
