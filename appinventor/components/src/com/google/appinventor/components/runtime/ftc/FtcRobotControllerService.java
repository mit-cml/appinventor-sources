/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

// Modified for App Inventor by Liz Looney
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ftc;

//AI import android.app.Service;
import android.content.Context;
import android.content.Intent;
//AI import android.os.Binder;
import android.os.Build;
//AI import android.os.IBinder;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.Device;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.factory.RobotFactory;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant.WifiDirectAssistantCallback;

import java.net.InetAddress;

import com.google.appinventor.components.runtime.FtcRobotController;

public class FtcRobotControllerService /*AI extends Service*/ implements WifiDirectAssistantCallback {

  private final static double NETWORK_MAX_WAIT = 120.0; // in seconds
  private final static int NETWORK_WAIT = 1000; // in milliseconds

  //AI private final IBinder binder = new FtcRobotControllerBinder();

  private WifiDirectAssistant wifiDirect;
  private Robot robot;
  private EventLoop eventLoop;

  private WifiDirectAssistant.Event wifiDirectStatus = WifiDirectAssistant.Event.DISCONNECTED;
  private String robotStatus = "Robot Status: null";

  private FtcRobotControllerActivity.Callback callback = null;
  private final EventLoopMonitor eventLoopMonitor = new EventLoopMonitor();

  private final ElapsedTime networkTimer = new ElapsedTime();

  private Thread robotSetupThread = null;

  /*AI
  public class FtcRobotControllerBinder extends Binder {
    FtcRobotControllerService getService() {
      return FtcRobotControllerService.this;
    }
  }
  */

  private class EventLoopMonitor implements EventLoopManager.EventLoopMonitor {

    @Override
    public void onStateChange(EventLoopManager.State state) {
      if (callback == null) return; // nothing to do

      switch (state) {
        case INIT:
          callback.robotUpdate("Robot Status: init");
          break;
        case NOT_STARTED:
          callback.robotUpdate("Robot Status: not started");
          break;
        case RUNNING:
          callback.robotUpdate("Robot Status: running");
          break;
        case STOPPED:
          callback.robotUpdate("Robot Status: stopped");
          break;
        case EMERGENCY_STOP:
          callback.robotUpdate("Robot Status: EMERGENCY STOP");
          break;
        case DROPPED_CONNECTION:
          callback.robotUpdate("Robot Status: dropped connection");
          break;
      }
    }
  }

  private class RobotSetupRunnable implements Runnable {
    @Override
    public void run() {
      try {
        // if an old robot is around, shut it down
        if (robot != null) {
          robot.shutdown();
          robot = null;
        }

        updateRobotStatus("Robot Status: scanning for USB devices");

        /*
         * Give android a chance to finish scanning for USB devices before
         * we create our robot object.
         *
         * It take Android up to ~300ms per USB device plugged into a hub.
         * Higher quality hubs take less time.
         */
        try {
          Thread.sleep(1000L * ftcRobotController.getUsbScanTimeInSeconds());
        } catch (InterruptedException e) {
          // we received an interrupt, abort
          updateRobotStatus("Robot Status: abort due to interrupt");
          return;
        }

        robot = RobotFactory.createRobot();

        updateRobotStatus("Robot Status: waiting on network");

        // wait for network to come up
        networkTimer.reset();
        while (wifiDirect.isConnected() == false) {
          try {
            Thread.sleep(NETWORK_WAIT);
            if (networkTimer.time() > NETWORK_MAX_WAIT) {
              updateRobotStatus("Robot Status: network timed out");
              return;
            }
          } catch (InterruptedException e) {
            DbgLog.msg("interrupt waiting for network; aborting setup");
            return;
          }
        }

        // now that we have network, start up the robot
        updateRobotStatus("Robot Status: starting robot");
        try {
          robot.eventLoopManager.setMonitor(eventLoopMonitor);
          InetAddress addr = wifiDirect.getGroupOwnerAddress();
          robot.start(addr, eventLoop);
        } catch (RobotCoreException e) {
          updateRobotStatus("Robot Status: failed to start robot");
          RobotLog.setGlobalErrorMsg(e.getMessage());
        }
      } catch (RobotCoreException e) {
        updateRobotStatus("Robot Status: Unable to create robot!");
        RobotLog.setGlobalErrorMsg(e.getMessage());
      }
    }
  }

  public WifiDirectAssistant getWifiDirectAssistant() {
    return wifiDirect;
  }

  public WifiDirectAssistant.Event getWifiDirectStatus() {
    return wifiDirectStatus;
  }

  public String getRobotStatus() {
    return robotStatus;
  }

  //AI @Override
  public /*AI IBinder*/ void onBind(/*AI Intent intent*/) {
    DbgLog.msg("Starting FTC Controller Service");

    DbgLog.msg("Android device is " + Build.MANUFACTURER + ", " + Build.MODEL);

    wifiDirect = WifiDirectAssistant.getWifiDirectAssistant(thisContext);
    wifiDirect.setCallback(this);

    wifiDirect.enable();
    if (Build.MODEL.equals(Device.MODEL_FOXDA_FL7007)) {
      // wifi channel selection does not work on the FOXDA when running as group owner
      wifiDirect.discoverPeers();
    } else {
      wifiDirect.createGroup();
    }

    //AI return binder;
  }

  //AI @Override
  public boolean onUnbind(/*AI Intent intent*/) {
    DbgLog.msg("Stopping FTC Controller Service");

    wifiDirect.disable();
    shutdownRobot();

    return false; // don't have new clients call onRebind()
  }

  public synchronized void setCallback(FtcRobotControllerActivity.Callback callback) {
    this.callback = callback;
  }

  public synchronized void setupRobot(EventLoop eventLoop) {

    /*
     * There is a bug in the Android activity life cycle with regards to apps
     * launched via USB. To work around this bug we will only honor this
     * method if setup is not currently running
     *
     * See: https://code.google.com/p/android/issues/detail?id=25701
     */

    if (robotSetupThread != null && robotSetupThread.isAlive()) {
      DbgLog.msg("FtcRobotControllerService.setupRobot() is currently running, stopping old setup");
      robotSetupThread.interrupt();
      while (robotSetupThread.isAlive() == true) Thread.yield();
      DbgLog.msg("Old setup stopped; restarting setup");
    }

    RobotLog.clearGlobalErrorMsg();
    DbgLog.msg("Processing robot setup");

    this.eventLoop = eventLoop;

    // setup the robot
    robotSetupThread = new Thread(new RobotSetupRunnable());
    robotSetupThread.start();

    // busy wait for setup thread to start
    while (robotSetupThread.getState() == Thread.State.NEW) Thread.yield();
  }

  public synchronized void shutdownRobot() {

    // if setup thread is running, stop it
    if (robotSetupThread != null && robotSetupThread.isAlive()) robotSetupThread.interrupt();

    // shut down the robot
    if (robot != null) robot.shutdown();
    robot = null; // need to set robot to null
    updateRobotStatus("Robot Status: null");
  }

  // Wifi Direct Assistant callback
  @Override
  public void onWifiDirectEvent(WifiDirectAssistant.Event event) {
    switch (event) {
      case CONNECTED_AS_GROUP_OWNER:
        DbgLog.msg("Wifi Direct - Group Owner");
        wifiDirect.cancelDiscoverPeers();
        break;
      case CONNECTED_AS_PEER:
        DbgLog.error("Wifi Direct - connected as peer, was expecting Group Owner");
        startActivity(new Intent(//AI getBaseContext(), ConfigWifiDirectActivity.class));
            "com.qualcomm.ftcrobotcontroller.ConfigWifiDirectActivity"));
        break;
      case CONNECTION_INFO_AVAILABLE:
        DbgLog.msg("Wifi Direct Passphrase: " + wifiDirect.getPassphrase());
        break;
      case ERROR:
        DbgLog.error("Wifi Direct Error: " + wifiDirect.getFailureReason());
        break;
      default:
        break;
    }

    updateWifiDirectStatus(event);
  }

  private void updateWifiDirectStatus(final WifiDirectAssistant.Event event) {
    wifiDirectStatus = event;
    if (callback != null) callback.wifiDirectUpdate(wifiDirectStatus);
  }

  private void updateRobotStatus(final String status) {
    robotStatus = status;
    if (callback != null) {
      callback.robotUpdate(status);
    }
  }

  // For App Inventor:

  private final FtcRobotController ftcRobotController;
  private final Context thisContext;

  public FtcRobotControllerService(FtcRobotController ftcRobotController, Context context) {
    this.ftcRobotController = ftcRobotController;
    thisContext = context;
  }

  private void startActivity(Intent intent) {
    try {
      thisContext.startActivity(intent);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
