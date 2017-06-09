// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package edu.colorado.lpc.blockytalkyble;

import android.os.ParcelUuid;

import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Notifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Static helper methods related to the BluetoothLE component.
 *
 * @author William Byrne (will2596@gmail.com)
 */
final class BLEUtil {

  // Regex to ensure that input strings follow the canonical UUID format outlined in RFC4122
  private static final Pattern UUID_FORMAT =
      Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

  // Regex to detect invalid characters in a potential UUID string
  private static final Pattern INVALID_UUID_CHARS = Pattern.compile("[^0-9a-fA-F-]");

  /**
   * Validator for prospective service and characteristic UUID strings used in
   * the BluetoothLE component.
   *
   * @param UUID the UUID to be validated
   * @return true if the UUID is valid, false otherwise
   */
  static boolean hasValidUUIDFormat(String UUID) {
      return UUID_FORMAT.matcher(UUID).find();
  }

  /**
   * Checks the prospective UUID string for invalid characters.
   *
   * @param UUID the UUID to be validated
   * @return true if the UUID has invalid characters, false otherwise
   */
  static boolean hasInvalidUUIDChars(String UUID) {
    return INVALID_UUID_CHARS.matcher(UUID).find();
  }

  /**
   * Function that takes in a List of ParcelUuids and converts it into a
   * List of corresponding Strings.
   *
   * @param serviceUUIDs - a List containing ParcelUuid types
   * @return a List containing String types representing the Uuid's
   */
  static List<String> stringifyParcelUuids(List<ParcelUuid> serviceUUIDs) {
    List<String> deviceServices = new ArrayList<String>();
    for (ParcelUuid serviceUuid : serviceUUIDs) {
      deviceServices.add(serviceUuid.toString());
    }
    return deviceServices;
  }
}
