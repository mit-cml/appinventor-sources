// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager.NameNotFoundException;

import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;

import android.os.Build;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesNativeLibraries;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;
import com.google.appinventor.components.runtime.util.AppInvHTTPD;
import com.google.appinventor.components.runtime.util.EclairUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.WebRTCNativeMgr;

import java.security.MessageDigest;

import java.util.Formatter;


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
@UsesLibraries(libraries = "webrtc.jar," +
    "google-http-client-beta.jar," +
    "google-http-client-android2-beta.jar," +
    "google-http-client-android3-beta.jar")
@UsesNativeLibraries(v7aLibraries = "libjingle_peerconnection_so.so",
  v8aLibraries = "libjingle_peerconnection_so.so",
  x86_64Libraries = "libjingle_peerconnection_so.so")
public class PhoneStatus extends AndroidNonvisibleComponent implements Component {

  private static Activity activity;
  private static final String LOG_TAG = "PhoneStatus";
  private final Form form;
  private static PhoneStatus mainInstance = null;
  private static boolean useWebRTC = false;
  private String firstSeed = null;
  private String firstHmacSeed = null;

  public PhoneStatus(ComponentContainer container) {
    super(container.$form());
    this.form = container.$form();
    activity = container.$context();
    if (mainInstance == null) { // First one?
      mainInstance = this;
    }
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
    "rendezvous server. Note: This code also starts the connection negotiation " +
    "process if we are using WebRTC. This is a bit of a kludge...")
  public String setHmacSeedReturnCode(String seed, String rendezvousServer) {

    /* If we get an empty seed, just ignore it. */
    if (seed.equals("")) {
      return "";
    }

    /*
     * Check to see if we are being re-entered.
     *
     * The Companion's design is to setup communications with
     * the user's browser and get to work. Once this process starts,
     * enough things are in motion that it is best to quit the
     * Companion and start a fresh copy if a different code is needed.
     *
     * If the same code is entered more then once, we just ignore the
     * second attempt. This often happens when someone scans a QR Code
     * and then presses the "Connect" Button because they do not know
     * that they don't have to do that. Effectively we are "de-bouncing"
     * the button.
     *
     */
    if (firstSeed != null) {    // Hmm. We've been here before!
      if (!firstSeed.equals(seed)) {
        // Attempting to use a different seed (code)
        // Provide a warning dialog box
        Notifier.oneButtonAlert(Form.getActiveForm(),
          "You cannot use two codes with one start up of the Companion. You should restart the " +
          "Companion and try again.",
          "Warning", "OK", new Runnable() {
              @Override public void run() {
                // We are going to die here, so the user has to start a new copy. This isn't ideal. A more
                // correct solution would be to gracefully shutdown the connection process and restart it with
                // the new seed.
                Form.getActiveForm().finish();
                System.exit(0);         // Truly ugly...
              }
            });
      }
      return firstHmacSeed;
    }

    firstSeed = seed;

    /*
     * Set the HMAC seed, but only if we are doing the legacy HTTP
     * thing.  Note: Currently we *always* start the HTTP Daemon, even
     * in WebRTC mode By not setting the seed, we ensure that the HTTP
     * Daemon cannot accept any blocks
     *
     */

    if (!useWebRTC) {
      AppInvHTTPD.setHmacKey(seed);
    }

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
    firstHmacSeed = sb.toString();
    return firstHmacSeed;
  }

  @SimpleFunction(description = "Returns true if we are running in the emulator or USB Connection")
  public boolean isDirect() {
    Log.d(LOG_TAG, "android.os.Build.VERSION.RELEASE = " + android.os.Build.VERSION.RELEASE);
    Log.d(LOG_TAG, "android.os.Build.PRODUCT = " + android.os.Build.PRODUCT);
    if (ReplForm.isEmulator()) { // Emulator is always direct
      return true;
    }
    if (form instanceof ReplForm) {
      return ((ReplForm)form).isDirect();
    } else {
      return false;
    }
  }

  @SimpleFunction(description = "Start the WebRTC engine")
  public void startWebRTC(String rendezvousServer, String iceServers) {
    WebRTCNativeMgr webRTCNativeMgr = new WebRTCNativeMgr(rendezvousServer, iceServers);
    webRTCNativeMgr.initiate((ReplForm) form, (Context)activity, firstSeed);
    ((ReplForm)form).setWebRTCMgr(webRTCNativeMgr);
  }

  @SimpleFunction(description = "Start the internal AppInvHTTPD to listen for incoming forms. FOR REPL USE ONLY!")
  public void startHTTPD(boolean secure) {
    ReplForm.topform.startHTTPD(secure);
  }

  @SimpleFunction(description = "Declare that we have loaded our initial assets and other assets should come from the sdcard")
  public void setAssetsLoaded() {
    if (form instanceof ReplForm) {
      ((ReplForm) form).setAssetsLoaded();
    }
  }

  @SimpleFunction(description = "Causes an Exception, used to debug exception processing.")
  public static void doFault() throws Exception {
    throw new Exception("doFault called!");
    // Thread t = new Thread(new Runnable() { // Cause an exception in a background thread to test bugsense
    //  public void run() {
    //    String nonesuch = null;
    //    String causefault = nonesuch.toString(); // This should cause a null pointer fault.
    //  }
    //   });
    // t.start();
  }

  @SimpleFunction(description = "Downloads the URL and installs it as an Android Package via the installed browser")
  public void installURL(String url) {
    Uri uri = Uri.parse(url);
    Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
    form.startActivity(intent);
  }

  @SimpleFunction(description = "Really Exit the Application")
  public void shutdown() {
    form.finish();
    System.exit(0);             // We cannot be restarted, so we better kill the process
  }

  /**
   * This event is fired when the "settings" menu item is selected (only available in the
   * Companion App, defined in ReplForm.java).
   */
  @SimpleEvent
  public void OnSettings() {
    EventDispatcher.dispatchEvent(this, "OnSettings");
  }

  /**
   * Set whether or not we will use WebRTC to communicate with the server
   *
   * @param useWebRTC  Set True to use WebRTC
   *
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty()
  public void WebRTC(boolean useWebRTC) {
    this.useWebRTC = useWebRTC;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "If True we are using WebRTC to talk to the server.")
  public boolean WebRTC() {
    return useWebRTC;
  }

  /**
   * SdkLevel -- Return the current Android SDK Level
   *
   * We use this to send the Rendezvous server our API leve which it
   * can then log for statistics (so we know when we can deprecate an
   * older version of Android because usage is low enough).
   *
   * @return SdkLevel
   */

  @SimpleFunction(description = "Get the current Android SDK Level")
  public int SdkLevel() {
    return SdkLevel.getLevel();
  }

  /**
   * GetVersionName -- Return the package versionName
   *
   * We use this to determine whether or not the Companion is compatible
   * with the current version of App Inventor. We provide this to the
   * Rendezvous server. When in "WebRTC" mode, the MIT App Inventor
   * client gets this value from the Rendezvous server (the older HTTPD
   * approach has its own "_getversion" URL which is used to do this, but
   * we cannot use that approach when using WebRTC, and the Rendezvous server
   * approach we support here is actually better because it avoid a round
   * trip between the client and the Companion...
   *
   * @return The VersionName as a string
   */

  @SimpleFunction(description = "Return the our VersionName property")
  public String GetVersionName() {
    try {
      String packageName = form.getPackageName();
      return form.getPackageManager().getPackageInfo(packageName, 0).versionName;
    } catch (NameNotFoundException e) {
      Log.e(LOG_TAG, "Unable to get VersionName", e);
      return "UNKNOWN";
    }
  }

  @SimpleFunction(description = "Return the app that installed us")
  public String GetInstaller() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR) {
      String installer = EclairUtil.getInstallerPackageName("edu.mit.appinventor.aicompanion3", form);
      if (installer == null) {
        return "sideloaded";
      } else {
        return installer;
      }
    } else {
      return "unknown";
    }
  }

  @SimpleFunction(description = "Return the ACRA Installation ID")
  public String InstallationId() {
    return org.acra.util.Installation.id(Form.getActiveForm());
  }

  /* Static context way to get the useWebRTC flag */
  public static boolean getUseWebRTC() {
    return useWebRTC;
  }

  /**
   * Static function called from ReplForm when settings menu item is chosen.
   * Triggers the "OnSettings" event iff there is a PhoneStatus component (which
   * there will be in the Companion App where this is used).
   */
  static void doSettings() {
    Log.d(LOG_TAG, "doSettings called.");
    if (mainInstance != null) {
      mainInstance.OnSettings();
    } else {
      Log.d(LOG_TAG, "mainStance is null on doSettings");
    }
  }

  public static String intToIp(int i) {
    return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >>24) & 0xFF);
  }
}
