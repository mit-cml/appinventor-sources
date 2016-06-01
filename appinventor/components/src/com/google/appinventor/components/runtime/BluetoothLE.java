// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


/**
 * Author: Andrew McKinney <mckinney@mit.edu>
 * Author: Cristhian Ulloa <cristhian2ulloa@gmail.com>
 * Author: tiffanyle <le.tiffanya@gmail.com>
 */

@DesignerComponent(version = YaVersion.BLUETOOTHLE_COMPONENT_VERSION,
    description = "This is a trial version of BluetoothLE component, blocks need to be specified later",
    category = ComponentCategory.EXPERIMENTAL,
    nonVisible = true,
    iconName = "images/bluetooth.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.BLUETOOTH, " + "android.permission.BLUETOOTH_ADMIN" + "android.permission.ACCESS_COARSE_LOCATION")

public class BluetoothLE extends AndroidNonvisibleComponent implements Component {

  /**
   * Basic Variable
   */
  private static final String LOG_TAG = "BluetoothLEComponent";
  private final Activity activity;
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothGatt currentBluetoothGatt;
  private int device_rssi = 0;
  private final Handler uiThread;
  private boolean mLogEnabled = true;
  private String mLogMessage;

  /**
   * BluetoothLE Info List
   */
  private HashMap<String, BluetoothGatt> gattList;
  private String deviceInfoList = "";
  private List<BluetoothDevice> mLeDevices;
  private List<BluetoothGattService> mGattService;
  private ArrayList<BluetoothGattCharacteristic> gattChars;
  private String serviceUUIDList;
  private String charUUIDList;
  private BluetoothGattCharacteristic mGattChar;
  private HashMap<BluetoothDevice, Integer> mLeDeviceRssi;

  /**
   * BluetoothLE Device Status
   */
  private boolean isEnabled = false;
  private boolean isScanning = false;
  private boolean isConnected = false;
  private boolean isCharRead = false;
  private boolean isCharWrite = false;
  private boolean isServiceRead = false;

  /**
   * GATT value
   */
  private int battery = -1;
  private String tempUnit = "";
  private byte[] bodyTemp;
  private byte[] heartRate;
  private int linkLoss_value = -1;
  private int txPower = -1;
  private byte[] data;
  private byte[] descriptorValue;
  private int intValue = 0;
  private float floatValue = 0;
  private String stringValue = "";
  private String byteValue = "";
  private int intOffset = 0;
  private int strOffset = 0;
  private int floatOffset = 0;

  private int charType = 0; //byte = 0; int = 1; string = 2; float = 3

  // BLE Advertisements
  private BluetoothLeScanner mBluetoothLeScanner;
  private Handler mHandler = new Handler();
  private AdvertiseCallback mAdvertiseCallback;
  private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
  private long SCAN_PERIOD = 5000;
  private String advertisementScanResult = "";
  private List<String> scannedAdvertiserNames = new ArrayList<String>();
  private HashMap<String, ScanResult> scannedAdvertisers = new HashMap<String, ScanResult>();
  private List<String> advertiserAddresses = new ArrayList<String>();
  private HashMap<String, String> nameToAddress = new HashMap<String, String>();
  private boolean isAdvertising = false;

  /**
   * Callback function called when a Bluetooth LE Advertisement is found
   */
  private ScanCallback mScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      super.onScanResult(callbackType, result);

      if (result == null || result.getDevice() == null || TextUtils.isEmpty(result.getDevice().getName())) {
        return;
      }

      StringBuilder builder = new StringBuilder(result.getDevice().getName());

      String advertiserAddress = result.getDevice().getAddress();
      advertiserAddresses.add(advertiserAddress);
      scannedAdvertisers.put(advertiserAddress, result);
      scannedAdvertiserNames.add(result.getDevice().getName());
      nameToAddress.put(result.getDevice().getName(), advertiserAddress);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
      super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
      LogMessage("Discovery onScanFailed: " + errorCode, "e");
      super.onScanFailed(errorCode);
    }
  };


  public BluetoothLE(ComponentContainer container) {
    super(container.$form());
    activity = (Activity) container.$context();
    mLeDevices = new ArrayList<BluetoothDevice>();
    mGattService = new ArrayList<BluetoothGattService>();
    gattChars = new ArrayList<BluetoothGattCharacteristic>();
    mLeDeviceRssi = new HashMap<BluetoothDevice, Integer>();
    gattList = new HashMap<String, BluetoothGatt>();
    uiThread = new Handler();

    if (SdkLevel.getLevel() < SdkLevel.LEVEL_JELLYBEAN_MR2) {
      mBluetoothAdapter = null;
    } else {
      mBluetoothAdapter = newBluetoothAdapter(activity);
    }

    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
      isEnabled = false;
      LogMessage("No Valid BTLE Device on platform", "e");
      form.dispatchErrorOccurredEvent(this, "BluetoothLE", ErrorMessages.ERROR_BLUETOOTH_NOT_ENABLED);
    } else {
      isEnabled = true;
      LogMessage("BluetoothLE Device found", "i");
    }
  }

  private void LogMessage(String message, String level) {
    if (mLogEnabled) {
      mLogMessage = message;
      String errorLevel = "e";
      String warningLevel = "w";

      // push to appropriate logging
      if (level.equals(errorLevel)) {
        Log.e(LOG_TAG, message);
      } else if (level.equals(warningLevel)) {
        Log.w(LOG_TAG, message);
      } else {
        Log.i(LOG_TAG, message);
      }
    }
  }

  public static BluetoothAdapter newBluetoothAdapter(Context context) {
    final BluetoothManager bluetoothManager = (BluetoothManager) context
        .getSystemService(Context.BLUETOOTH_SERVICE);
    return bluetoothManager.getAdapter();
  }

  @SimpleFunction(description = "Start Scanning for BluetoothLE devices.")
  public void StartScanning() {
    if (!mLeDevices.isEmpty()) {
      mLeDevices.clear();
      mLeDeviceRssi.clear();
    }
    mBluetoothAdapter.startLeScan(mLeScanCallback);
    LogMessage("StartScanning Successfully.", "i");
  }

  @SimpleFunction(description = "Stop Scanning for BluetoothLE devices.")
  public void StopScanning() {
    mBluetoothAdapter.stopLeScan(mLeScanCallback);
    LogMessage("StopScanning Successfully.", "i");
  }

  @SimpleFunction(description = "Connect to a BluetoothLE device with index. Index specifies the position in BluetoothLE device list, starting from 0.")
  public void Connect(int index) {
    BluetoothGattCallback newGattCallback = null;
    currentBluetoothGatt = mLeDevices.get(index - 1).connectGatt(activity, false, initCallBack(newGattCallback));
    if (currentBluetoothGatt != null) {
      gattList.put(mLeDevices.get(index - 1).toString(), currentBluetoothGatt);
      LogMessage("Connect Successfully.", "i");
    } else {
      LogMessage("Connect Fail.", "e");
    }
  }

  @SimpleFunction(description = "Connect to BluetoothLE device with address. Address specifies bluetooth address of the BluetoothLE device.")
  public void ConnectWithAddress(String address) {
    for (BluetoothDevice bluetoothDevice : mLeDevices) {
      if (bluetoothDevice.toString().equals(address)) {
        BluetoothGattCallback newGattCallback = null;
        currentBluetoothGatt = bluetoothDevice.connectGatt(activity, false, initCallBack(newGattCallback));
        if (currentBluetoothGatt != null) {
          gattList.put(bluetoothDevice.toString(), currentBluetoothGatt);
          LogMessage("Connect with Address Successfully.", "i");
          break;
        } else {
          LogMessage("Connect with Address Fail.", "e");
        }
      }
    }
  }

  @SimpleFunction(description = "Disconnect from connected BluetoothLE device with address. Address specifies bluetooth address of the BluetoothLE device.")
  public void DisconnectWithAddress(String address) {
    if (gattList.containsKey(address)) {
      gattList.get(address).disconnect();
      isConnected = false;
      gattList.remove(address);
      LogMessage("Disconnect Successfully.", "i");
    } else {
      LogMessage("Disconnect Fail. No Such Address in the List", "e");
    }
  }

  @SimpleFunction(description = "Write String value to a connected BluetoothLE device. Service Unique ID, Characteristic Unique ID and String value"
      + "are required.")
  public void WriteStringValue(String service_uuid, String characteristic_uuid, String value) {
    LogMessage("stringValue: " + value, "i");
    writeChar(UUID.fromString(service_uuid), UUID.fromString(characteristic_uuid), value);
  }

  @SimpleFunction(description = "Write Integer value to a connected BluetoothLE device. Service Unique ID, Characteristic Unique ID, Integer value"
      + " and offset are required. Offset specifies the start position of writing data.")
  public void WriteIntValue(String service_uuid, String characteristic_uuid, int value, int offset) {
    LogMessage("intValue: " + value, "i");
    writeChar(UUID.fromString(service_uuid), UUID.fromString(characteristic_uuid), value, BluetoothGattCharacteristic.FORMAT_SINT32, offset);
  }

  @SimpleFunction(description="Write Float value to a connected BluetoothLE device. Service Unique ID, Characteristic Unique ID, Integer value"
      + " and offset are required. Offset specifies the start position of writing data. Value converted to IEEE 754 floating-point 32-bit layout before writing.")
  public void WriteFloatValue(String service_uuid, String characteristic_uuid, float value, int offset) {

   int floatrep = Float.floatToIntBits(value);

    LogMessage("floatrep: " + floatrep, "i");

    writeChar(UUID.fromString(service_uuid), UUID.fromString(characteristic_uuid), floatrep, BluetoothGattCharacteristic.FORMAT_SINT32, offset);
  }

  @SimpleFunction(description = "Write byte value to a connected BluetoothLE device. Service Unique ID, Characteristic Unique ID, Integer value"
      + " and offset are required. Offset specifies the start position of writing data.")
  public void WriteByteValue(String service_uuid, String characteristic_uuid, String value) {
    byte[] bval = value.getBytes();
    LogMessage("byteValue: " + bval, "i");
    writeChar(UUID.fromString(service_uuid), UUID.fromString(characteristic_uuid), bval);
  }

  @SimpleFunction(description = "Read Integer value from a connected BluetoothLE device. Service Unique ID, Characteristic Unique ID and offset"
      + " are required. Offset specifies the start position of reading data.")
  public void ReadIntValue(String service_uuid, String characteristic_uuid, int intOffset) {
    charType = 1;
    this.intOffset = intOffset;
    readChar(UUID.fromString(service_uuid), UUID.fromString(characteristic_uuid));
  }

  @SimpleFunction(description = "Read String value from a connected BluetoothLE device. Service Unique ID, Characteristic Unique ID and offset"
      + " are required. Offset specifies the start position of reading data.")
  public void ReadStringValue(String service_uuid, String characteristic_uuid, int strOffset) {
    charType = 2;
    this.strOffset = strOffset;
    readChar(UUID.fromString(service_uuid), UUID.fromString(characteristic_uuid));
  }

  @SimpleFunction(description = "Read Float value from a connected BluetoothLE device. Service Unique ID, Characteristic Unique ID and offset"
      + " are required. Offset specifies the start position of reading data.")
  public void ReadFloatValue(String service_uuid, String characteristic_uuid, int floatOffset) {
    charType = 3;
    this.floatOffset = floatOffset;
    readChar(UUID.fromString(service_uuid), UUID.fromString(characteristic_uuid));
  }

  @SimpleFunction(description = "Read Byte value from a connected BluetoothLE device. Service Unique ID and Characteristic Unique ID are required.")
  public void ReadByteValue(String service_uuid, String characteristic_uuid) {
    charType = 0;
    readChar(UUID.fromString(service_uuid), UUID.fromString(characteristic_uuid));
  }

  @SimpleFunction(description = "Get the RSSI (Received Signal Strength Indicator) of found device with index. Index specifies the position in BluetoothLE device list, starting from 0.")
  public int FoundDeviceRssi(int index) {
    if (index <= mLeDevices.size())
      return mLeDeviceRssi.get(mLeDevices.get(index - 1));
    else
      return -1;
  }

  @SimpleFunction(description = "Get the name of found device with index. Index specifies the position in BluetoothLE device list, starting from 0.")
  public String FoundDeviceName(int index) {
    if (index <= mLeDevices.size()) {
      LogMessage("Device Name is found", "i");
      return mLeDevices.get(index - 1).getName();
    } else {
      LogMessage("Device Name isn't found", "e");
      return null;
    }
  }

  @SimpleFunction(description = "Get the address of found device with index. Index specifies the position in BluetoothLE device list, starting from 0.")
  public String FoundDeviceAddress(int index) {
    if (index <= mLeDevices.size()) {
      LogMessage("Device Address is found", "i");
      return mLeDevices.get(index - 1).getAddress();
    } else {
      LogMessage("Device Address is found", "e");
      return "";
    }
  }

  @SimpleFunction(description = "Create and publish a Bluetooth LE advertisement. inData specifies the data that will be included in the advertisement. serviceUuid specifies the UUID of the advertisement.")
  public void StartAdvertising(String inData, String serviceUuid) {
    //create a scan callback if it does not already exist. If it does, you're already scanning for ads.
    if (mBluetoothAdapter != null) {
      mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

      AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
          isAdvertising = true;
          super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
          LogMessage("Advertising onStartFailure: " + errorCode, "e");
          super.onStartFailure(errorCode);
        }
      };

      AdvertiseSettings advSettings = new AdvertiseSettings.Builder()
          .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
          .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
          .setConnectable(false)
          .build();

      ParcelUuid pUuid = new ParcelUuid(UUID.fromString(serviceUuid));

      AdvertiseData advData = new AdvertiseData.Builder()
          .setIncludeDeviceName(true)
          .addServiceUuid(pUuid)
          .addServiceData(pUuid, inData.getBytes(Charset.forName("UTF-8")))
          .build();

      if (mAdvertiseCallback == null) {
        AdvertiseSettings settings = advSettings;
        AdvertiseData data = advData;

        mAdvertiseCallback = advertisingCallback;

        if (mBluetoothLeAdvertiser != null) {
          mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
        }
      }

      LogMessage("StartScanningAdvertisements Successfully.", "i");
    } else {
      LogMessage("No bluetooth adapter", "i");
    }
  }

  @SimpleFunction(description = "Stop Bluetooth LE Advertising.")
  public void StopAdvertising() {
    LogMessage("Stopping BLE Advertising", "i");
    if (mBluetoothLeAdvertiser != null) {
      mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
      isAdvertising = false;
      mAdvertiseCallback = null;
    }
  }

  @SimpleFunction(description = "Scans for Bluetooth LE advertisements. scanPeriod specifies how long the scan will run.")
  public void ScanAdvertisements(long scanPeriod) {
    SCAN_PERIOD = scanPeriod;

    // clear the information that was saved during previous scan
    advertiserAddresses = new ArrayList<String>();
    scannedAdvertisers = new HashMap<String, ScanResult>();
    scannedAdvertiserNames = new ArrayList<String>();
    nameToAddress = new HashMap<String, String>();


    // Will stop the scanning after a set time.
    uiThread.postDelayed(new Runnable() {
      @Override
      public void run() {
        stopAdvertisementScanning();
      }
    }, scanPeriod);

    if (mBluetoothAdapter != null) {
      mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

      if (mScanCallback != null) {

        if (mBluetoothLeScanner != null) {
          ScanSettings settings = new ScanSettings.Builder()
              .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
              .build();

          List<ScanFilter> filters = new ArrayList<ScanFilter>();
          ScanFilter filter = new ScanFilter.Builder()
              .build();
          // NOTE: removed service uuid from filter: ".setServiceUuid( new ParcelUuid(UUID.fromString( "0000b81d-0000-1000-8000-00805f9b34fb" ) ) )""

          filters.add(filter);

          if (settings != null) {
            mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
          } else {
            LogMessage("settings or filters are null.", "i");
          }
        } else {
          LogMessage("Bluetooth LE scanner is null", "i");
        }
      } else {
        LogMessage("mScanCallback is null", "i");
      }
    } else {
      LogMessage("No bluetooth adapter found", "i");
    }
  }

  @SimpleFunction(description = "Stops scanning for Bluetooth LE advertisements.")
  public void StopScanningAdvertisements() {
    LogMessage("Stopping BLE advertsiment scan.", "i");
    stopAdvertisementScanning();
  }

  @SimpleFunction(description = "Returns the advertisement data associated with the specified device address.")
  public String GetAdvertisementData(String deviceAddress, String serviceUuid) {
    return "" + Arrays.toString(scannedAdvertisers.get(deviceAddress).getScanRecord().getServiceData().get(ParcelUuid.fromString(serviceUuid)));
  }

  @SimpleFunction(description = "Returns the address of the device with the name specified.")
  public String GetAdvertiserAddress(String deviceName) {
    return nameToAddress.get(deviceName);
  }

  @SimpleFunction(description = "Returns the list of services available on the Adverising device.")
  public List<String> GetAdvertiserServiceUuids(String deviceAddress) {
    return stringifyServiceList(scannedAdvertisers.get(deviceAddress).getScanRecord().getServiceUuids());
  }

  @SimpleProperty(description = "Return the battery level.", category = PropertyCategory.BEHAVIOR)
  public String BatteryValue() {
    if (isCharRead) {
      return Integer.toString(battery);
    } else {
      return "Cannot Read Battery Level";
    }
  }

  @SimpleProperty(description = "Return the Tx power.", category = PropertyCategory.BEHAVIOR)
  public int TxPower() {
    return txPower;
  }

  @SimpleProperty(description = "Return true if a BluetoothLE device is connected; Otherwise, return false.", category = PropertyCategory.BEHAVIOR)
  public boolean IsDeviceConnected() {
    if (isConnected) {
      return true;
    } else {
      return false;
    }
  }

  @SimpleProperty(description = "Return a sorted list of BluetoothLE devices as a String.", category = PropertyCategory.BEHAVIOR)
  public String DeviceList() {
    deviceInfoList = "";
    mLeDevices = sortDeviceList(mLeDevices);
    if (!mLeDevices.isEmpty()) {
      for (int i = 0; i < mLeDevices.size(); i++) {
        if (i != (mLeDevices.size() - 1)) {
          deviceInfoList += mLeDevices.get(i).getAddress() + " " + mLeDevices.get(i).getName() + " "
              + Integer.toString(mLeDeviceRssi.get(mLeDevices.get(i))) + ",";
        } else {
          deviceInfoList += mLeDevices.get(i).getAddress() + " " + mLeDevices.get(i).getName() + " "
              + Integer.toString(mLeDeviceRssi.get(mLeDevices.get(i)));
        }
      }
    }
    return deviceInfoList;
  }

  @SimpleProperty(description = "Return the RSSI (Received Signal Strength Indicator) of connected device.", category = PropertyCategory.BEHAVIOR)
  public String ConnectedDeviceRssi() {
    return Integer.toString(device_rssi);
  }

  @SimpleProperty(description = "Returns the value of ScanPeriod.")
  public long AdvertisementScanPeriod() {
    return SCAN_PERIOD;
  }

  @SimpleProperty(description = "Returns a list of the names of the devices found during Advertisment scanning.")
  public List<String> GetAdvertiserNames() {
    return scannedAdvertiserNames;
  }

  @SimpleProperty(description = "Returns a list of the addresses of devices found during Advertisement scanning.")
  public List<String> GetAdvertiserAddresses() {
    return advertiserAddresses;
  }

  @SimpleProperty(description = "Returns true if the device is currently advertising, false otherwise.")
  public boolean IsDeviceAdvertising() {
    return isAdvertising;
  }

  @SimpleEvent(description = "Trigger event when a BluetoothLE device is connected.")
  public void Connected() {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "Connected");
      }
    });
  }

  @SimpleEvent(description = "Trigger event when RSSI (Received Signal Strength Indicator) of found BluetoothLE device changes")
  public void RssiChanged(final int device_rssi) {
    uiThread.postDelayed(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "RssiChanged", device_rssi);
      }
    }, 1000);
  }

  @SimpleEvent(description = "Trigger event when a new BluetoothLE device is found.")
  public void DeviceFound() {
    EventDispatcher.dispatchEvent(this, "DeviceFound");
  }

  public void ByteValueRead(final String byteValue) {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "ByteValueRead", byteValue);
      }
    });
  }

  public void IntValueRead(final int intValue) {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "IntValueRead", intValue);
      }
    });
  }

  public void StringValueRead(final String stringValue) {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "StringValueRead", stringValue);
      }
    });
  }

  public void FloatValueRead(final float floatValue) {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "FloatValueRead", floatValue);
      }
    });
  }

  @SimpleEvent(description = "Trigger event when byte value from connected BluetoothLE device is changed.")
  public void ByteValueChanged(final String byteValue) {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "ByteValueChanged", byteValue);
      }
    });
  }

  @SimpleEvent(description = "Trigger event when int value from connected BluetoothLE device is changed.")
  public void IntValueChanged(final int intValue) {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "IntValueChanged", intValue);
      }
    });
  }

  @SimpleEvent(description = "Trigger event when int value from connected BluetoothLE device is changed.")
  public void FloatValueChanged(final float floatValue) {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "FloatValueChanged", floatValue);
      }
    });
  }

  @SimpleEvent(description = "Trigger event when String value from connected BluetoothLE device is changed.")
  public void StringValueChanged(final String stringValue) {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "StringValueChanged", stringValue);
      }
    });
  }

  @SimpleEvent(description = "Trigger event when value is successfully written to connected BluetoothLE device.")
  public void ValueWrite() {
    uiThread.post(new Runnable() {
      @Override
      public void run() {
        EventDispatcher.dispatchEvent(BluetoothLE.this, "ValueWrite");
      }
    });
  }

  @SimpleFunction(description = "Return list of supported services for connected device as a String")
  public String GetSupportedServices() {
    if (mGattService == null) return ",";
    serviceUUIDList = ", ";
    for (int i = 0; i < mGattService.size(); i++) {
      if (i == 0) {
        serviceUUIDList = "";
      }
      UUID serviceUUID = mGattService.get(i).getUuid();
      String unknownServiceString = "Unknown Service";
      String serviceName = BluetoothLEGattAttributes.lookup(serviceUUID, unknownServiceString);
      serviceUUIDList += serviceUUID + " " + serviceName + ",";

    }
    return serviceUUIDList;
  }

  @SimpleFunction(description = "Return Unique ID of selected service with index. Index specified by list of supported services for a connected device, starting from 0.")
  public String GetServicebyIndex(int index) {
    return mGattService.get(index).getUuid().toString();
  }

  @SimpleFunction(description = "Return list of supported characteristics for connected device as a String")
  public String GetSupportedCharacteristics() {
    if (mGattService == null) return ",";
    charUUIDList = ", ";
    for (int i = 0; i < mGattService.size(); i++) {
      if (i == 0) {
        charUUIDList = "";
      }
      for (BluetoothGattCharacteristic characteristic : mGattService.get(i).getCharacteristics()) {
        gattChars.add(characteristic);
      }
    }
    String unknownCharString = "Unknown Characteristic";
    for (int j = 0; j < gattChars.size(); j++) {
      UUID charUUID = gattChars.get(j).getUuid();
      String charName = BluetoothLEGattAttributes.lookup(charUUID, unknownCharString);
      charUUIDList += charUUID + " " + charName + ",";
    }
    return charUUIDList;
  }

  @SimpleFunction(description = "Return Unique ID of selected characteristic with index. Index specified by list of supported characteristics for a connected device, starting from 0.")
  public String GetCharacteristicbyIndex(int index) {
    return gattChars.get(index).getUuid().toString();
  }

  /**
   * Functions
   */
  // sort the device list by RSSI
  private List<BluetoothDevice> sortDeviceList(List<BluetoothDevice> deviceList) {
    Collections.sort(deviceList, new Comparator<BluetoothDevice>() {
      @Override
      public int compare(BluetoothDevice device1, BluetoothDevice device2) {
        return mLeDeviceRssi.get(device1) - mLeDeviceRssi.get(device2);
      }
    });
    Collections.reverse(deviceList);
    return deviceList;
  }

  // add device when scanning
  private void addDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
    if (!mLeDevices.contains(device)) {
      mLeDevices.add(device);
      mLeDeviceRssi.put(device, rssi);
      DeviceFound();
    } else {
      mLeDeviceRssi.put(device, rssi);
    }
    RssiChanged(rssi);
  }

  // read characteristic based on UUID
  private void readChar(UUID ser_uuid, UUID char_uuid) {
    if (isServiceRead && !mGattService.isEmpty()) {
      for (BluetoothGattService aMGattService : mGattService) {
        if (aMGattService.getUuid().equals(ser_uuid)) {

          BluetoothGattDescriptor desc = aMGattService.getCharacteristic(char_uuid)
              .getDescriptor(BluetoothLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIGURATION);

          mGattChar = aMGattService.getCharacteristic(char_uuid);

          if (desc != null) {
            if ((aMGattService.getCharacteristic(char_uuid).getProperties() &
                BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
              desc.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            } else {
              desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            currentBluetoothGatt.writeDescriptor(desc);
          }
          if (mGattChar != null) {
            currentBluetoothGatt.setCharacteristicNotification(mGattChar, true);
            isCharRead = currentBluetoothGatt.readCharacteristic(mGattChar);
          }
          break;
        }
      }
    }

    if (isCharRead) {
      LogMessage("Read Character Successfully.", "i");
    } else {
      LogMessage("Read Character Fail.", "i");
    }
  }

  // Write characteristic based on uuid
  private void writeChar(UUID ser_uuid, UUID char_uuid, int value, int format, int offset) {
    if (isServiceRead && !mGattService.isEmpty()) {
      for (BluetoothGattService aMGattService : mGattService) {
        if (aMGattService.getUuid().equals(ser_uuid)) {
          mGattChar = aMGattService.getCharacteristic(char_uuid);
          if (mGattChar != null) {
            mGattChar.setValue(value, format, offset);
            isCharWrite = currentBluetoothGatt.writeCharacteristic(mGattChar);
          }
          break;
        }
      }
    }

    if (isCharWrite) {
      LogMessage("Write Gatt Characteristic Successfully", "i");
    } else {
      LogMessage("Write Gatt Characteristic Fail", "e");
    }
  }

  private void writeChar(UUID ser_uuid, UUID char_uuid, byte[] value) {
    if (isServiceRead && !mGattService.isEmpty()) {
      for (int i = 0; i < mGattService.size(); i++) {
        if (mGattService.get(i).getUuid().equals(ser_uuid)) {
          mGattChar = mGattService.get(i).getCharacteristic(char_uuid);
          if (mGattChar != null) {
            mGattChar.setValue(value);
            isCharWrite = currentBluetoothGatt.writeCharacteristic(mGattChar);
          }
          break;
        }
      }
    }

    if (isCharWrite) {
      LogMessage("Write Gatt Characteristic Successfully", "i");
    } else {
      LogMessage("Write Gatt Characteristic Fail", "e");
    }
  }

  private void writeChar(UUID ser_uuid, UUID char_uuid, String value) {
    if (isServiceRead && !mGattService.isEmpty()) {
      for (int i = 0; i < mGattService.size(); i++) {
        if (mGattService.get(i).getUuid().equals(ser_uuid)) {
          mGattChar = mGattService.get(i).getCharacteristic(char_uuid);
          if (mGattChar != null) {
            mGattChar.setValue(value);
            isCharWrite = currentBluetoothGatt.writeCharacteristic(mGattChar);
          }
          break;
        }
      }
    }

    if (isCharWrite) {
      LogMessage("Write Gatt Characteristic Successfully", "i");
    } else {
      LogMessage("Write Gatt Characteristic Fail", "e");
    }
  }

  private BluetoothAdapter.LeScanCallback mLeScanCallback;

  {
    mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            isScanning = true;
            addDevice(device, rssi, scanRecord);
          }
        });
      }
    };
  }

  private BluetoothGattCallback initCallBack(BluetoothGattCallback newGattCallback) {
    return this.mGattCallback;
  }

  private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      if (newState == BluetoothProfile.STATE_CONNECTED) {
        isConnected = true;
        gatt.discoverServices();
        gatt.readRemoteRssi();
        Connected();
      }

      if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        isConnected = false;
      }
    }

    @Override
    // New services discovered
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        mGattService = gatt.getServices();
        isServiceRead = true;
      }
    }

    @Override
    // Result of a characteristic read operation
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        data = characteristic.getValue();
        intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, intOffset);
        stringValue = characteristic.getStringValue(strOffset);
        floatValue = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, floatOffset);
        byteValue = "";
        for (byte i : data) {
          byteValue += i;
        }
        isCharRead = true;
        ByteValueRead(byteValue);
        IntValueRead(intValue);
        StringValueRead(stringValue);
        FloatValueRead(floatValue);
      }
    }

    @Override
    // Result of a characteristic read operation is changed
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
      data = characteristic.getValue();
      LogMessage("dataLength: " + data.length, "i");
      switch (charType) {
        case 0:
          byteValue = "";
          for (byte i : data) {
            byteValue += i;
          }
          LogMessage("byteValue: " + byteValue, "i");
          ByteValueChanged(byteValue);
          break;
        case 1:
          intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, intOffset);
          LogMessage("intValue: " + intValue, "i");
          IntValueChanged(intValue);
          break;
        case 2:
          stringValue = characteristic.getStringValue(strOffset);
          LogMessage("stringValue: " + stringValue, "i");
          StringValueChanged(stringValue);
          break;
        case 3:
          if (data.length == 1) {
            floatValue = (float) (data[0] * Math.pow(10, 0));
          } else if (data.length == 2 || data.length == 3) {
            floatValue = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, floatOffset);
          } else {
            floatValue = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, floatOffset);
          }
          LogMessage("floatValue: " + floatValue, "i");
          FloatValueChanged(floatValue);
        default:
          byteValue = "";
          for (byte i : data) {
            byteValue += i;
          }
          LogMessage("byteValue: " + byteValue, "i");
          ByteValueChanged(byteValue);
          break;
      }
      isCharRead = true;
    }

    @Override
    // set value of characteristic
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      LogMessage("Write Characteristic Successfully.", "i");
      ValueWrite();
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      descriptorValue = descriptor.getValue();
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      LogMessage("Write Descriptor Successfully.", "i");
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
      device_rssi = rssi;
      RssiChanged(device_rssi);
    }
  };

  /**
   * Function that stops the Bluetooth LE from scanning for advertisements
   */
  private void stopAdvertisementScanning() {
    LogMessage("Stopping advertisement scanning.", "i");
    mBluetoothLeScanner.stopScan(mScanCallback);
  }

  /**
   * Function that takes in a list of Uuid's and converts them to Strings
   * Input: List serviceUuids - a List containing ParcelUuid types
   * Return: List containing String types representing the Uuid's
   */
  private List<String> stringifyServiceList(List<ParcelUuid> serviceUuids) {
    List<String> deviceServices = new ArrayList<String>();
    for (ParcelUuid serviceUuid : serviceUuids) {
      deviceServices.add(serviceUuid.toString());
    }
    return deviceServices;
  }
}


