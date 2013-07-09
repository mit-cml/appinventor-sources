// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import java.util.Formatter;
import java.security.MessageDigest;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AppInvHTTPD;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;

/**
 * Component for obtaining Phone Information. Currently supports
 * obtaining the IP Address of the phone and whether or not it is
 * connected via a WiFi connection.
 *
 * @author lmercer@mit.edu (Logan Mercer)
 *
 */
@DesignerComponent(version = YaVersion.PHONESTATUS_COMPONENT_VERSION,
                   description = "Component that returns information about the phone.",
                   category = ComponentCategory.INTERNAL,
                   nonVisible = true,
                   iconName = "images/phoneip.png")
@SimpleObject
public class PhoneStatus extends AndroidNonvisibleComponent implements Component {

  private static Activity activity;
  private static final String LOG_TAG = "PhoneStatus";
  private final Form form;

  public PhoneStatus(ComponentContainer container) {
    super(container.$form());
    this.form = container.$form();
    activity = container.$context();
  }

  @SimpleFunction(description = "Returns the IP address of the phone in the form of a String")
  public static String GetWifiIpAddress() {
    DhcpInfo ip;
    Object wifiManager = activity.getSystemService("wifi");
    ip = ((WifiManager) wifiManager).getDhcpInfo();
    int s_ipAddress= ip.ipAddress;
    String ipAddress;
    if (isConnected())
      ipAddress = intToIp(s_ipAddress);
    else
      ipAddress = "Error: No Wifi Connection";
    return ipAddress;
  }

  @SimpleFunction(description = "Returns TRUE if the phone is on Wifi, FALSE otherwise")
  public static boolean isConnected() {
    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService("connectivity");
    NetworkInfo networkInfo = null;
    if (connectivityManager != null) {
      networkInfo =
        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }
    return networkInfo == null ? false : networkInfo.isConnected();
  }

  @SimpleFunction(description = "Establish the secret seed for HOTP generation. " +
    "Return the SHA1 of the provided seed, this will be used to contact the " +
    "rendezvous server.")
  public String setHmacSeedReturnCode(String seed) {
    AppInvHTTPD.setHmacKey(seed);
    MessageDigest Sha1;
    try {
      Sha1 = MessageDigest.getInstance("SHA1");
    } catch (Exception e) {
      Log.e(LOG_TAG, "Exception getting SHA1 Instance", e);
      return "";
    }
    Sha1.update(seed.getBytes());
    byte [] result = Sha1.digest();
    StringBuffer sb = new StringBuffer(result.length * 2);
    Formatter formatter = new Formatter(sb);
    for (byte b : result) {
      formatter.format("%02x", b);
    }
    Log.d(LOG_TAG, "Seed = " + seed);
    Log.d(LOG_TAG, "Code = " + sb.toString());
    return sb.toString();
  }

  @SimpleFunction(description = "Returns true if we are running in the emulator")
  public boolean isEmulator() {
    if (Build.FINGERPRINT.startsWith("generic"))
      return true;
    return false;
  }

  @SimpleFunction(description = "Start the internal AppInvHTTPD to listen for incoming forms. FOR REPL USE ONLY!")
  public void startHTTPD() {
    ReplForm.topform.startHTTPD();
  }

  @SimpleFunction(description = "Declare that we have loaded our initial assets and other assets should come from the sdcard")
  public void setAssetsLoaded() {
    if (form instanceof ReplForm) {
      ((ReplForm) form).setAssetsLoaded();
    }
  }

  public static String intToIp(int i) {
    return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >>24) & 0xFF);
  }
}
