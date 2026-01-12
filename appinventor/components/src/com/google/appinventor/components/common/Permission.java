// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a Permission type representing permissions like camera, location, etc.
 */
public enum Permission implements OptionList<String> {
  CoarseLocation("ACCESS_COARSE_LOCATION"),
  FineLocation("ACCESS_FINE_LOCATION"),
  @Deprecated  // Not a runtime permission
  MockLocation("ACCESS_MOCK_LOCATION"),
  LocationExtraCommands("ACCESS_LOCATION_EXTRA_COMMANDS"),
  ReadExternalStorage("READ_EXTERNAL_STORAGE"),
  WriteExternalStorage("WRITE_EXTERNAL_STORAGE"),
  Camera("CAMERA"),
  Audio("RECORD_AUDIO"),
  @Deprecated  // Not a runtime permission
  Vibrate("VIBRATE"),
  @Deprecated  // Not a runtime permission
  Internet("INTERNET"),
  @Deprecated  // Not a runtime permission
  NearFieldCommunication("NFC"),
  @Deprecated  // Not a runtime permission
  Bluetooth("BLUETOOTH"),
  @Deprecated  // Not a runtime permission
  BluetoothAdmin("BLUETOOTH_ADMIN"),
  @Deprecated  // Not a runtime permission
  WifiState("ACCESS_WIFI_STATE"),
  @Deprecated  // Not a runtime permission
  NetworkState("ACCESS_NETWORK_STATE"),
  @Deprecated  // Not a runtime permission
  AccountManager("ACCOUNT_MANAGER"),
  @Deprecated  // Not a runtime permission
  ManageAccounts("MANAGE_ACCOUNTS"),
  GetAccounts("GET_ACCOUNTS"),
  ReadContacts("READ_CONTACTS"),
  @Deprecated  // Not a runtime permission
  UseCredentials("USE_CREDENTIALS"),
  // Added in Android SDK 31
  BluetoothAdvertise("BLUETOOTH_ADVERTISE"),
  BluetoothConnect("BLUETOOTH_CONNECT"),
  BluetoothScan("BLUETOOTH_SCAN"),
  // Added in Android SDK 33
  ReadMediaImages("READ_MEDIA_IMAGES"),
  ReadMediaVideo("READ_MEDIA_VIDEO"),
  ReadMediaAudio("READ_MEDIA_AUDIO");

  private final String value;

  Permission(String perm) {
    this.value = perm;
  }

  public String toUnderlyingValue() {
    return value;
  }

  private static final Map<String, Permission> lookup = new HashMap<>();

  static {
    for (Permission perm : Permission.values()) {
      lookup.put(perm.toUnderlyingValue(), perm);
    }
  }

  public static Permission fromUnderlyingValue(String perm) {
    return lookup.get(perm);
  }
}
