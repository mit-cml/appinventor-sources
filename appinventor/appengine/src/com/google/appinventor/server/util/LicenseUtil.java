// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// This is unreleased code.

package com.google.appinventor.server.util;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.user.Config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.UUID;

/**
 * Provide a set of utility function used by the license management code.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */

public class LicenseUtil {
  private final transient static StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  // KLUDGE: We cache the authCode here to avoid a second call to getLicenseConfig()
  private static String authCode = null;

  /**
   * Return a unique ID for this system based on a Network Interface
   * MAC address. The "hint" input is potentially one of these addresses
   * which is returned iff one of the interfaces on the system has this
   * as a hardware interface. Otherwise we use the hardware address of
   * the first interface to have one. We use this hint, because we cannot
   * guarantee that a particular interface will always be the first returned.
   *
   * @param hint A previously returned hardware address based identifier
   * @returns A hardware address based unique identifier -- usually the same
   *          as the hint.
   */

  private static String getSysId(String hint) {
    String retval = "";
    String first = null;
    byte [] tmp = null;
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface inf = interfaces.nextElement();
        retval += inf.getName() + ": ";
        tmp = inf.getHardwareAddress();
        if (tmp == null) {
          continue;
        } else {
          String hardwareAddress = bytesToHex(tmp);
          if (first == null) {  // In case we don't find hint, we'll use this one
            first = hardwareAddress;
          }
          if (hint == null || hardwareAddress.equals(hint)) {
            return hardwareAddress;
          }
        }
      }
      // Fell through, hint must be invalid, use first
      return first;
    } catch (SocketException e) {
      return null;
    }
  }

  /**
   * Returns the system unique ID. This is computed based on a UUID stored in the data store
   * and the hardware address of a system network interface (using the code above).
   *
   * @returns system unique Id
   */

  public static String getSysUID() {
    String sysUID = null;
    String hardware = null;
    String oldhardware = "";
    LicenseConfig conf = storageIo.getLicenseConfig();
    if (conf != null) {
      sysUID = conf.getUUID();
      oldhardware = conf.getHardwareHint();
      authCode = conf.getAuthCode();
    }
    boolean storeback = false;
    if (sysUID == null || hardware == null) {
      storeback = true;
    }
    hardware = getSysId(oldhardware);  // Get a hardware address from an interface
    if (hardware == null) {         // Could not find hardware address, fail
      System.err.println("hardware address is NULL.");
      return null;
    }
    if (!oldhardware.equals(hardware)) { // Hardware address changed...
      storeback = true;
    }
    System.err.println("Hardware address = " + hardware);
    if (sysUID == null) {           // Need to generate one.
      sysUID = UUID.randomUUID().toString();
    }
    if (storeback) {
      LicenseConfig nc = new LicenseConfig(hardware, sysUID, null);
      storageIo.setLicenseConfig(nc);
    }
    try {
      MessageDigest mc = MessageDigest.getInstance("MD5");
      mc.update(sysUID.getBytes());
      mc.update(hardware.getBytes());
      return bytesToHex(mc.digest());
    } catch (NoSuchAlgorithmException e) {
      return null;              // Shouldn't happen
    }
  }

  public static String getAuthCode() {
    if (authCode != null) {
      return authCode;
    } else {
      LicenseConfig conf = storageIo.getLicenseConfig();
      return conf.getAuthCode();
    }
  }

  public void storeLicenseMac(String newMac) {
    LicenseConfig nc = new LicenseConfig(null, null, newMac);
    // Note: hardware address and sysUID will not be overwritten
    // if provided as null
    storageIo.setLicenseConfig(nc);
  }

  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
  private static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for ( int j = 0; j < bytes.length; j++ ) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

}
