// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.os.Build;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PermissionConstraint;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.errors.StopBlocksExecution;
import com.google.appinventor.components.runtime.util.BluetoothReflection;
import com.google.appinventor.components.runtime.util.BulkPermissionRequest;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SUtil;
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
 * Use `BluetoothClient` to connect your device to other devices using Bluetooth. This component
 * uses the Serial Port Profile (SPP) for communication. If you are interested in using Bluetooth
 * low energy, please see the
 * [BluetoothLE](http://iot.appinventor.mit.edu/#/bluetoothle/bluetoothleintro) extension.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.BLUETOOTHCLIENT_COMPONENT_VERSION,
    description = "Bluetooth client component",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/bluetooth.png")
@SimpleObject
@UsesPermissions({BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_CONNECT, BLUETOOTH_SCAN})
public final class BluetoothClient extends BluetoothConnectionBase
    implements RealTimeDataSource<String, String> {
  private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
  private static final String[] RUNTIME_PERMISSIONS =
      new String[] { BLUETOOTH_CONNECT, BLUETOOTH_SCAN };

  private final List<Component> attachedComponents = new ArrayList<Component>();
  private Set<Integer> acceptableDeviceClasses;

  // Set of observers
  private HashSet<DataSourceChangeListener> dataSourceObservers = new HashSet<>();

  // Executor Service to poll data continuously from the Input Stream
  // which holds data sent by Bluetooth connections. Used for sending
  // data to Data listeners, and only initialized as soon as an observer
  // is added to this component.
  private ScheduledExecutorService dataPollService;

  // Fixed polling rate for the Data Polling Service (in milliseconds)
  private int pollingRate = 10;
  private boolean noLocationNeeded = false;

  /**
   * Creates a new BluetoothClient.
   */
  public BluetoothClient(ComponentContainer container) {
    super(container, "BluetoothClient");
    DisconnectOnError(false);
  }

  /**
   * Returns whether BluetoothClient/BluetoothServer should be disconnected automatically when an error occurs.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
          description = "Disconnects BluetoothClient automatically when an error occurs.")
  public boolean DisconnectOnError() {
    return disconnectOnError;
  }

  /**
   * Specifies whether BluetoothClient/BluetoothServer should be disconnected automatically when an error occurs.
   *
   * @param disconnectOnError {@code true} to disconnect BluetoothClient/BluetoothServer automatically when an error occurs.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty
  public void DisconnectOnError(boolean disconnectOnError) {
    this.disconnectOnError = disconnectOnError;
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
   * device's name. On Android 12 or later, if the permissions BLUETOOTH_CONNECT
   * and BLUETOOTH_SCAN have not been granted to the app, the block will raise
   * an error via the Screen's PermissionDenied event.
   *
   * @internaldoc
   * This method calls isDeviceClassAcceptable to determine whether to include
   * a particular device in the returned list.
   *
   * @return a List representing the addresses and names of paired
   *         Bluetooth devices
   */
  @SimpleProperty(description = "The addresses and names of paired Bluetooth devices",
      category = PropertyCategory.BEHAVIOR)
  public List<String> AddressesAndNames() {
    // Because this is a property we can check that we have the right permissions, but without
    // call/cc or CPS we cannot defer the operation, so we throw PermissionException instead
    // and the app developer will have to handle it.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      for (String permission : RUNTIME_PERMISSIONS) {
        if (form.isDeniedPermission(permission)) {
          throw new PermissionException(permission);
        }
      }
    }

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
   * The polling rate in milliseconds when the Bluetooth Client is used as a Data Source in a
   * Chart Data component. The minimum value is 1.
   *
   * @param rate the rate in milliseconds
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  @DesignerProperty(defaultValue = "10")
  public void PollingRate(int rate) {
    // Resolve polling rate values that are too small to the smallest possible value.
    if (rate < 1) {
      this.pollingRate = 1;
    } else {
      this.pollingRate = rate;
    }
  }

  /**
   * Returns the configured polling rate value of the Bluetooth Client.
   *
   * @return  polling rate value
   */
  @SimpleProperty
  public int PollingRate() {
    return this.pollingRate;
  }

  /**
   * On Android 12 and later, indicates that Bluetooth is not used to determine the user's location.
   *
   * @param setting true if the user's location won't be inferred
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  @DesignerProperty(defaultValue = "False",
      editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)
  @UsesPermissions(constraints = {
      @PermissionConstraint(name = BLUETOOTH_SCAN, usesPermissionFlags = "neverForLocation")
  })
  public void NoLocationNeeded(boolean setting) {
    noLocationNeeded = setting;
  }

  @SimpleProperty(userVisible = false)
  public boolean NoLocationNeeded() {
    return noLocationNeeded;
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
  private boolean connect(final String functionName, String address, final String uuidString) {
    final String finalAddress = address;
    if (SUtil.requestPermissionsForConnecting(form, this, functionName,
        new PermissionResultHandler() {
          @Override
          public void HandlePermissionResponse(String permission, boolean granted) {
            connect(functionName, finalAddress, uuidString);
          }
        })) {
      return false;
    }

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

  /**
   * Starts the scheduled Data Polling Service that
   * continuously reads data and notifies all the
   * observers with the new data.
   */
  private void startBluetoothDataPolling() {
    // Create a new Scheduled Executor. The executor is made single
    // threaded to prevent race conditions between consequent
    // Bluetooth reading as well as due to performance (since the
    // chosen polling interval is chosen to be quite small)
    dataPollService = Executors.newSingleThreadScheduledExecutor();

    // Execute runnable task at a fixed millisecond rate
    dataPollService.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        // Retrieve data value (with a null key, since
        // key value does not matter for BluetoothClient)
        String value = getDataValue(null);

        // Notify data observers of the retrieved value if it is
        // non-empty
        if (!value.equals("")) {
          notifyDataObservers(null, value);
        }
      }
    }, 0, pollingRate, TimeUnit.MILLISECONDS);
  }

  @Override
  public synchronized void addDataObserver(DataSourceChangeListener dataComponent) {
    // Data Polling Service has not been initialized yet; Initialize it
    // (since Data Component is added)
    if (dataPollService == null) {
      startBluetoothDataPolling();
    }

    // Add the Data Component as an observer
    dataSourceObservers.add(dataComponent);
  }

  @Override
  public synchronized void removeDataObserver(DataSourceChangeListener dataComponent) {
    dataSourceObservers.remove(dataComponent);

    // No more Data Source observers exist;
    // Shut down polling service and null it
    // (the reason for nulling is so that a new
    // service could be created upon adding a new
    // observer)
    if (dataSourceObservers.isEmpty()) {
      dataPollService.shutdown();
      dataPollService = null;
    }
  }

  @Override
  public void notifyDataObservers(String key, Object newValue) {
    for (DataSourceChangeListener observer : dataSourceObservers) {
      observer.onReceiveValue(this, key, newValue);
    }
  }

  @Override
  public String getDataValue(String key) {
    String value = "";

    // Ensure that the BluetoothClient is connected
    if (IsConnected()) {
      // Check how many bytes can be received
      int bytesReceivable = BytesAvailableToReceive();

      // At least one byte can be received
      if (bytesReceivable > 0) {
        // Read contents from the Bluetooth connection until delimiter
        value = ReceiveText(-1);
      }
    }

    return value;
  }
}
