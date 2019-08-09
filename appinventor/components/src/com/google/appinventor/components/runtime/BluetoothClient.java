// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.BluetoothReflection;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * BluetoothClient component
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.BLUETOOTHCLIENT_COMPONENT_VERSION,
    description = "Bluetooth client component",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/bluetooth.png")
@SimpleObject
@UsesPermissions(permissionNames =
                 "android.permission.BLUETOOTH, " +
                 "android.permission.BLUETOOTH_ADMIN")
public final class BluetoothClient extends BluetoothConnectionBase implements RealTimeChartDataSource<String, String> {
  private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

  private final List<Component> attachedComponents = new ArrayList<Component>();
  private Set<Integer> acceptableDeviceClasses;

  // Set of observers
  private HashSet<ChartDataBase> dataSourceObservers = new HashSet<ChartDataBase>();

  // Executor Service to poll data continuously from the Input Stream
  // which holds data sent by Bluetooth connections. Used for sending
  // data to Data listeners, and only initialized as soon as an observer
  // is added to this component.
  private ScheduledExecutorService dataPollService;

  // Fixed polling rate for the Data Polling Service (in milliseconds)
  private static final int POLLING_RATE = 100;

  /**
   * Creates a new BluetoothClient.
   */
  public BluetoothClient(ComponentContainer container) {
    super(container, "BluetoothClient");
  }

  boolean attachComponent(Component component, Set<Integer> acceptableDeviceClasses) {
    if (attachedComponents.isEmpty()) {
      // If this is the first/only attached component, we keep the acceptableDeviceClasses.
      this.acceptableDeviceClasses = (acceptableDeviceClasses == null)
          ? null
          : new HashSet<Integer>(acceptableDeviceClasses);

    } else {
      // If there is already one or more attached components, the acceptableDeviceClasses must be
      // the same as what we already have.
      if (this.acceptableDeviceClasses == null) {
        if (acceptableDeviceClasses != null) {
          return false;
        }
      } else {
        if (acceptableDeviceClasses == null) {
          return false;
        }
        if (!this.acceptableDeviceClasses.containsAll(acceptableDeviceClasses)) {
          return false;
        }
        if (!acceptableDeviceClasses.containsAll(this.acceptableDeviceClasses)) {
          return false;
        }
      }
    }

    attachedComponents.add(component);
    return true;
  }

  void detachComponent(Component component) {
    attachedComponents.remove(component);
    if (attachedComponents.isEmpty()) {
      acceptableDeviceClasses = null;
    }
  }

  /**
   * Checks whether the Bluetooth device with the given address is paired.
   *
   * @param address the MAC address of the Bluetooth device
   * @return true if the device is paired, false otherwise
   */
  @SimpleFunction(description = "Checks whether the Bluetooth device with the specified address " +
  "is paired.")
  public boolean IsDevicePaired(String address) {
    String functionName = "IsDevicePaired";
    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_AVAILABLE);
      return false;
    }

    if (!BluetoothReflection.isBluetoothEnabled(bluetoothAdapter)) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_ENABLED);
      return false;
    }

    // Truncate the address at the first space.
    // This allows the address to be an element from the AddressesAndNames property.
    int firstSpace = address.indexOf(" ");
    if (firstSpace != -1) {
      address = address.substring(0, firstSpace);
    }

    if (!BluetoothReflection.checkBluetoothAddress(bluetoothAdapter, address)) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_ADDRESS);
      return false;
    }

    Object bluetoothDevice = BluetoothReflection.getRemoteDevice(bluetoothAdapter, address);
    return BluetoothReflection.isBonded(bluetoothDevice);
  }

  /**
   * Returns the list of paired Bluetooth devices. Each element of the returned
   * list is a String consisting of the device's address, a space, and the
   * device's name.
   *
   * This method calls isDeviceClassAcceptable to determine whether to include
   * a particular device in the returned list.
   *
   * @return a List representing the addresses and names of paired
   *         Bluetooth devices
   */
  @SimpleProperty(description = "The addresses and names of paired Bluetooth devices",
      category = PropertyCategory.BEHAVIOR)
  public List<String> AddressesAndNames() {
    List<String> addressesAndNames = new ArrayList<String>();

    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter != null) {
      if (BluetoothReflection.isBluetoothEnabled(bluetoothAdapter)) {
        for (Object bluetoothDevice : BluetoothReflection.getBondedDevices(bluetoothAdapter)) {
          if (isDeviceClassAcceptable(bluetoothDevice)) {
            String name = BluetoothReflection.getBluetoothDeviceName(bluetoothDevice);
            String address = BluetoothReflection.getBluetoothDeviceAddress(bluetoothDevice);
            addressesAndNames.add(address + " " + name);
          }
        }
      }
    }

    return addressesAndNames;
  }

  /**
   * Returns true if the class of the given device is acceptable.
   *
   * @param bluetoothDevice the Bluetooth device
   */
  private boolean isDeviceClassAcceptable(Object bluetoothDevice) {
    if (acceptableDeviceClasses == null) {
      // Add devices are acceptable.
      return true;
    }

    Object bluetoothClass = BluetoothReflection.getBluetoothClass(bluetoothDevice);
    if (bluetoothClass == null) {
      // This device has no class.
      return false;
    }

    int deviceClass = BluetoothReflection.getDeviceClass(bluetoothClass);
    return acceptableDeviceClasses.contains(deviceClass);
  }

  /**
   * Connect to a Bluetooth device with the given address.
   *
   * @param address the MAC address of the Bluetooth device
   * @return true if the connection was successful, false otherwise
   */
  @SimpleFunction(description = "Connect to the Bluetooth device with the specified address and " +
      "the Serial Port Profile (SPP). Returns true if the connection was successful.")
  public boolean Connect(String address) {
    return connect("Connect", address, SPP_UUID);
  }

  /**
   * Connect to a Bluetooth device with the given address and a specific UUID.
   *
   * @param address the MAC address of the Bluetooth device
   * @param uuid the UUID
   * @return true if the connection was successful, false otherwise
   */
  @SimpleFunction(description = "Connect to the Bluetooth device with the specified address and " +
  "UUID. Returns true if the connection was successful.")
  public boolean ConnectWithUUID(String address, String uuid) {
    return connect("ConnectWithUUID", address, uuid);
  }

  /**
   * Connects to a Bluetooth device with the given address and UUID.
   *
   * If the address contains a space, the space and any characters after it
   * are ignored. This facilitates passing an element of the list returned from
   * the addressesAndNames method above.
   *
   * @param functionName the name of the SimpleFunction calling this method
   * @param address the address of the device
   * @param uuidString the UUID
   */
  private boolean connect(String functionName, String address, String uuidString) {
    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_AVAILABLE);
      return false;
    }

    if (!BluetoothReflection.isBluetoothEnabled(bluetoothAdapter)) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_ENABLED);
      return false;
    }

    // Truncate the address at the first space.
    // This allows the address to be an element from the AddressesAndNames property.
    int firstSpace = address.indexOf(" ");
    if (firstSpace != -1) {
      address = address.substring(0, firstSpace);
    }

    if (!BluetoothReflection.checkBluetoothAddress(bluetoothAdapter, address)) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_ADDRESS);
      return false;
    }

    Object bluetoothDevice = BluetoothReflection.getRemoteDevice(bluetoothAdapter, address);
    if (!BluetoothReflection.isBonded(bluetoothDevice)) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_PAIRED_DEVICE);
      return false;
    }

    if (!isDeviceClassAcceptable(bluetoothDevice)) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_REQUIRED_CLASS_OF_DEVICE);
      return false;
    }

    UUID uuid;
    try {
      uuid = UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_UUID, uuidString);
      return false;
    }

    Disconnect();

    try {
      connect(bluetoothDevice, uuid);
      return true;
    } catch (IOException e) {
      Disconnect();
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_UNABLE_TO_CONNECT);
      return false;
    }
  }

  private void connect(Object bluetoothDevice, UUID uuid) throws IOException {
    Object bluetoothSocket;
    if (!secure && SdkLevel.getLevel() >= SdkLevel.LEVEL_GINGERBREAD_MR1) {
      // createInsecureRfcommSocketToServiceRecord was introduced in level 10
      bluetoothSocket = BluetoothReflection.createInsecureRfcommSocketToServiceRecord(
          bluetoothDevice, uuid);
    } else {
      bluetoothSocket = BluetoothReflection.createRfcommSocketToServiceRecord(
          bluetoothDevice, uuid);
    }
    BluetoothReflection.connectToBluetoothSocket(bluetoothSocket);
    setConnection(bluetoothSocket);
    Log.i(logTag, "Connected to Bluetooth device " +
        BluetoothReflection.getBluetoothDeviceAddress(bluetoothDevice) + " " +
        BluetoothReflection.getBluetoothDeviceName(bluetoothDevice) + ".");
  }

  @Override
  public void addDataObserver(ChartDataBase dataComponent) {
    // Data Polling Service has not been initialized yet; Initialize it
    // (since Data Component is added)
    if (dataPollService == null) {
      startBluetoothDataPolling();
    }

    // Add the Data Component as an observer
    dataSourceObservers.add(dataComponent);
  }

  /**
   * Starts the scheduled Data Polling Service that
   * continuously reads data and notifies all the
   * observers with the new data.
   */
  private void startBluetoothDataPolling() {
    // Create a new Scheduled Executor (Single threaded)
    dataPollService = Executors.newSingleThreadScheduledExecutor();

    // Execute runnable task at a fixed millisecond rate
    dataPollService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        // Ensure that the BluetoothClient is connected
        if (IsConnected()) {
          // Check how many bytes can be received
          int bytesReceivable = BytesAvailableToReceive();

          // At least one byte can be received and there
          // are observers which can receive the data
          if (bytesReceivable > 0 && !dataSourceObservers.isEmpty()) {
            // Read all of the available data
            String result = ReceiveText(-1);

            // Notify observers with the new data (and null key)
            notifyDataObservers(null, result);
          }
        }
      }
    }, 0, POLLING_RATE, TimeUnit.MILLISECONDS);
  }

  @Override
  public void removeDataObserver(ChartDataBase dataComponent) {
    dataSourceObservers.remove(dataComponent);
  }

  @Override
  public void notifyDataObservers(String key, Object newValue) {
    for (ChartDataBase observer : dataSourceObservers) {
      observer.onReceiveValue(this, key, newValue);
    }
  }

  @Override
  public String getDataValue(String key) {
    return "0";
  }
}
