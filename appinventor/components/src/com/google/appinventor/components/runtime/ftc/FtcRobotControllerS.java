// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.ftc;

import com.google.appinventor.components.runtime.FtcRobotController;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.factory.RobotFactory;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant.WifiDirectAssistantCallback;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FtcRobotControllerS implements WifiDirectAssistantCallback {

  private final static double NETWORK_MAX_WAIT = 120.0; // 2 minutes

  private final FtcRobotController ftcRobotController;
  private final Context context;

  private WifiDirectAssistant wifiDirect;
  private Robot robot;
  private EventLoop eventLoop;

  private WifiDirectAssistant.Event wifiDirectStatus = WifiDirectAssistant.Event.DISCONNECTED;
  private String robotStatus = "Robot Status: null";

  private FtcRobotControllerA.Callback callback = null;
  private final EventLoopMonitor eventLoopMonitor = new EventLoopMonitor();

  private final ElapsedTime networkTimer = new ElapsedTime();

  private final Lock networkLock = new ReentrantLock();
  private final Condition networkChange = networkLock.newCondition();

  private Thread robotSetupThread = null;

  public FtcRobotControllerS(FtcRobotController ftcRobotController) {
    this.ftcRobotController = ftcRobotController;
    context = ftcRobotController.getContext();
  }

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
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          // we received an interrupt, abort
          updateRobotStatus("Robot Status: abort due to interrupt");
          return;
        }

        robot = RobotFactory.createRobot();

        updateRobotStatus("Robot Status: waiting on network");

        // wait for network to come up
        try {
          networkTimer.reset();
          networkLock.lock();
          while (wifiDirect.isConnected() == false) {
            try {
              networkChange.await(1, TimeUnit.SECONDS);
              if (networkTimer.time() > NETWORK_MAX_WAIT) {
                updateRobotStatus("Robot Status: network timed out");
                // finally block will release lock
                return;
              }
            } catch (InterruptedException e) {
              DbgLog.msg("interrupt waiting for network; aborting setup");
              return;
            }
          }
        } finally {
          networkLock.unlock();
        }

        // now that we have network, start up the robot
        updateRobotStatus("Robot Status: starting robot");
        try {
          robot.eventLoopManager.setMonitor(eventLoopMonitor);
          InetAddress addr = wifiDirect.getGroupOwnerAddress();
          robot.start(addr, addr, eventLoop);
        } catch (RobotCoreException e) {
          updateRobotStatus("Robot Status: failed to start robot");
        }
      } catch (RobotCoreException e) {
        updateRobotStatus("Robot Status: Unable to create robot!");
        e.printStackTrace();
      }
    }
  }

  // Methods called by FtcRobotController and FtcRobotControllerA.

  String getDriverStationMac() {
    return ftcRobotController.getDriverStationMac();
  }

  WifiDirectAssistant.Event getWifiDirectStatus() {
    return wifiDirectStatus;
  }

  String getRobotStatus() {
    return robotStatus;
  }

  public void bindHeyLiz() {
    DbgLog.msg("Starting FTC Controller S");

    wifiDirect = WifiDirectAssistant.getWifiDirectAssistant(context);
    wifiDirect.setCallback(this);

    wifiDirect.enable();

    if (wifiDirect.isConnected() == false) {
      wifiDirect.discoverPeers();
    }
  }

  public void unbindHeyLiz() {
    DbgLog.msg("Stopping FTC Controller S");

    wifiDirect.disable();
    shutdownRobot();
  }

  synchronized void setCallback(FtcRobotControllerA.Callback callback) {
    this.callback = callback;
  }

  synchronized void setupRobot(EventLoop eventLoop) {

    /*
     * There is a bug in the Android activity life cycle with regards to apps
     * launched via USB. To work around this bug we will only honor this
     * method if setup is not currently running
     *
     * See: https://code.google.com/p/android/issues/detail?id=25701
     */

    if (robotSetupThread != null && robotSetupThread.isAlive()) {
      DbgLog.msg("FtcRobotControllerS.setupRobot() is currently running, stopping old setup");
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

  synchronized void shutdownRobot() {

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
      case PEERS_AVAILABLE:
        if (wifiDirect.getConnectStatus() == WifiDirectAssistant.ConnectStatus.CONNECTED ||
            wifiDirect.getConnectStatus() == WifiDirectAssistant.ConnectStatus.CONNECTING) {
          /*
           * We get extra an extra PEER_AVAILABLE event when first connecting, and right after
           * the connection is complete. Just ignore these events.
           */
          return;
        }

        // look for driver station
        List<WifiP2pDevice> peers = wifiDirect.getPeers();
        for (WifiP2pDevice peer : peers) {
          if (peer.deviceAddress.equalsIgnoreCase(ftcRobotController.getDriverStationMac())) {
            // driver station found; connect
            wifiDirect.connect(peer);
            break;
          }
        }
        break;
      case CONNECTING:
      case CONNECTED_AS_PEER:
        wifiDirect.cancelDiscoverPeers();
        break;
      case DISCONNECTED:
        wifiDirect.discoverPeers();
        break;
      case ERROR:
        DbgLog.error("Wifi Direct Error: " + wifiDirect.getFailureReason());
        break;
      default:
        break;
    }

    updateWifiDirectStatus(event);

    // change has been processed, notify code waiting on networkChange
    try {
      networkLock.lock();
      networkChange.signalAll();
    } finally {
      networkLock.unlock();
    }
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
}
