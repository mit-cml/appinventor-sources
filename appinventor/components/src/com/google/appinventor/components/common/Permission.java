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
  MockLocation("ACCESS_MOCK_LOCATION"),
  LocationExtraCommands("ACCESS_LOCATION_EXTRA_COMMANDS"),
  ReadExternalStorage("READ_EXTERNAL_STORAGE"),
  WriteExternalStorage("WRITE_EXTERNAL_STORAGE"),
  Camera("CAMERA"),
  Audio("RECORD_AUDIO"),
  Vibrate("VIBRATE"),
  Internet("INTERNET"),
  NearFieldCommunication("NFC"),
  Bluetooth("BLUETOOTH"),
  BluetoothAdmin("BLUETOOTH_ADMIN"),
  WifiState("ACCESS_WIFI_STATE"),
  NetworkState("ACCESS_NETWORK_STATE"),
  AccountManager("ACCOUNT_MANAGER"),
  ManageAccounts("MANAGE_ACCOUNTS"),
  GetAccounts("GET_ACCOUNTS"),
  ReadContacts("READ_CONTACTS"),
  UseCredentials("USE_CREDENTIALS");

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
