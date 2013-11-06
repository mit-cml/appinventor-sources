// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.yacodeblocks;

import openblocks.yacodeblocks.AndroidController.DeviceConnectionListener;

/**
 * An AndroidController for codeblocks provides access to an external
 * environment for cases where codeblocks needs to communicate with an
 * android device.  An AndroidController instance is designed to manage a
 * particular application on the phone (referred to as "the application" in
 * the method comments below). It can manage multiple devices. At most one
 * device at a time can be the "selected device" that is running the application.
 *
 * This code is not located in third_party so that we don't call into the
 * android third_party code from the codeblocks third_party code.
 *
 * @author kerr@google.com (Debby Wallach)
 *
 */

public interface AndroidController {
  
  interface DeviceConnectionListener {
    public void deviceConnected(String serialNumber);
    public void deviceDisconnected(String serialNumber);
  }
  
  /**
   * Provide methods to be called when a device is connected or disconnected.
   * @param listener
   */
  public void setDeviceListener(DeviceConnectionListener listener);
  
  /**
   * Cause the external controller to initialize the communication bridge
   * to the android. 
   * @param adbLocation must contain the fully-qualified location of adb on
   * the system.
   * @return true iff the initialization was successful.
   */
  public boolean androidInitializeCommunicationBridge(String adbLocation);
  
  /**
   * Restart the android communication bridge. Returns true if the restart
   * succeeds, else false.
   */
  public boolean androidRestartBridge();

  /**
   * Cause the external controller to synchronize and install the
   * application that this class is designed for to the selected
   * device.  Throws an exception if this fails for any reason (including
   * there not being a device).
   */
  public void androidSyncAndInstallStarterApplication() 
      throws AndroidControllerException, ExternalStorageException;

  /**
   * Cause the external controller to synchronize and install the specified
   * application to the selected device.  Throws an exception if
   * this fails for any reason (including there not being a device).
   * @param apkFilePath is the location of the application on the local file system.
   * @param appName is the name of the application.
   * @param packageName is the name of the package. It might look
   *   something like "com.gmail.USERNAME.<appName>";
   */
  public void androidSyncAndInstallSpecificApplication(String apkFilePath, String appName,
      String packageName) throws AndroidControllerException, ExternalStorageException;

  /**
   * Query the external controller whether the selected device
   * has the starter application running on it.
   * @return true if it is.  false if there is no selected device or the
   *   app is not running.
   */
  public boolean androidIsStarterApplicationRunning();

  /**
   * Cause the external controller to start the starter application.  May throw
   * an exception if the start fails. Will not re-start the application if it
   * is already running.
   */
  public void androidStartStarterApplication() throws AndroidControllerException;

  /**
   * Attempts to kill starter application on the selected device, if any.
   */
  public void androidKillStarterApplication();

  /**
   * Attempts to kill specified application on the selected device, if any.
   */
  public void androidKillSpecificApplication(String packageName);

  /**
   * Cause the external controller to create a port forwarding between a
   * local and a remote port.  This is useful if you are creating a socket
   * level connection to the application running on the device.
   * @return true iff successful
   */
  public boolean androidForwardTcpPort(int localPort, int remotePort);

  /**
   * Tells the external controller to clean things up.  Must be called
   * before the code exits.
   */
  public void androidCleanUpBeforeExit();

  /**
   * Copy a file onto the selected device.
   * @param fileToPush the file to copy
   * @param remotePath the full path to copy the file to on the phone
   * @throws AndroidControllerException
   * @throws ExternalStorageException if it appears that the external storage
   *   is not mounted on the device
   */
  public void pushFileToDevice(String fileToPush, String remotePath)
      throws AndroidControllerException, ExternalStorageException;
  
  /**
   * Make device the currently selected device. Used for WiFi connection.
   * Throws AndroidControllerException if device isn't a WiFi device.
   */
  public void selectDevice(String device, String ipAddress) throws AndroidControllerException;

  /**
   * Make device the currently selected device. device should have previously
   * been provided as an argument to deviceConnected. 
   * Throws AndroidControllerException if device is not currently connected.
   */
  public void selectDevice(String device) throws AndroidControllerException;
  
  /**
   * @return the name of the currently selected device, or null if there is none
   */
  public String getSelectedDevice();

}
