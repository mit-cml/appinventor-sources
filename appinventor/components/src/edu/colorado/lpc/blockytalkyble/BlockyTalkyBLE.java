// Copyright 2017 University of Colorado Boulder, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package edu.colorado.lpc.blockytalkyble;

import android.util.Log;
import android.app.Activity;

import android.content.pm.PackageManager;

import android.util.StringBuilderPrinter;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;

import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.SdkLevel;

import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.common.collect.Lists;
import gnu.lists.FString;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.*;

@DesignerComponent(version = 1, description = "", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/bluetooth.png")

@SimpleObject(external = true)
public class BlockyTalkyBLE extends AndroidNonvisibleComponent {
  // private BluetoothLE bleConnection = null;


  private final Activity activity;
  private final ComponentContainer container;
  private final Form form;
  private edu.colorado.lpc.blockytalkyble.BluetoothLEint innerBLE;
  private final String finalChunkIndicator = ">";

  public static abstract class BLEResponseHandler<T> {
    public void onReceive(String serviceUuid, String characteristicUuid, List<T> values) {
    }

    public void onStringReceive() {

    }

    public void onNumberReceive() {

    }

    public void onWrite(String serviceUuid, String characteristicUuid, List<T> values) {
    }
  }

  private final BLEResponseHandler<Integer> txCharacteristicHandler = new BLEResponseHandler<Integer>() {
    @Override
    public void onReceive(String serviceUuid, String characteristicUuid, List<Integer> values) {
      // assume these values is the string, also assume they are valid UTF-8 (??)
      //char[] temp = new char[values.size()+1];
      StringBuilder builder = new StringBuilder(values.size());
      for (Integer x : values) {
        builder.append(Character.toChars(x));
      }
      String keyValueString = builder.toString();

      final String specialCharacters = "^\\$[]+";
      String toSplitOn = null;
      if (specialCharacters.indexOf(BlockyTalkyBLEConstants.DELIMITER) == -1) {
        toSplitOn = BlockyTalkyBLEConstants.DELIMITER;
      } else {
        toSplitOn = "\\" + BlockyTalkyBLEConstants.DELIMITER;
      }

      String[] parts = keyValueString.split(toSplitOn);
      ValueType type = null;
      String key;
      String value;
      if(parts.length > 2) {
        type = typeFromString(parts[0]);
        key = parts[1];
        value = parts[2].substring(0, parts[2].length() - 1);
      } else {
        key = parts[0];
        value = parts[1].substring(0, parts[1].length() -1);
      }
      //Log.d(LOG_TAG,"XonRecv:"+"->"+keyValueString+"->"+parts[0]+"->"+parts[1]+"<-");

      String s = "onReceivevalues:";
      for (Integer x : values) {
        s += "," + x;
      }

      Log.d(BlockyTalkyBLEConstants.LOG_TAG, s + "->" + type + "->" + key + " " + value);
      if (type== ValueType.NUMBER) {
        NumberReceivedfromMicrobit(key,Integer.parseInt(value));
      } else if (type == ValueType.STRING) {
        StringReceivedfromMicrobit(key, value);
      }
    }
  };

  private final BLEResponseHandler<Integer> rXCharacteristicWriteHandler = new BLEResponseHandler<Integer>() {
    @Override
    public void onWrite(String serviceUuid, String characteristicUuid, List<Integer> values) {
      Log.d(BlockyTalkyBLEConstants.LOG_TAG, "rXCharacteristicWriteHandler called");
      WroteRXCharacteristic(values);
    }
  };

  // @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT)
  // @SimpleProperty
  // public void BluetoothDevice(BluetoothLE bluetoothLE) {
  //   bleConnection = bluetoothLE;
  //   bluetoothLE.SetMicrobitBT(this) ;
  //    }

  // @SimpleProperty(description = "The BluetoothLE component connected to the micro:bit device.")
  // public BluetoothLE BluetoothDevice() {
  //   return bleConnection;
  // }

  // public MicrobitBT(Form form) {
  //    super(form);
  //  }

  public BlockyTalkyBLE(ComponentContainer container) {
    super(container.$form());

    activity = container.$context();
    form = container.$form();
    this.container = container;

    // Perform preliminary checks to see if we are usable on this device.
    // If this test does not pass, we log an advanced warning then proceed.
    // Individualized errors are signaled in each @SimpleFunction.
    if (!container.$form().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Log.e(BlockyTalkyBLEConstants.LOG_TAG, "Bluetooth LE is unsupported on this hardware. " + "Any subsequent function calls will complain.");
    } else if (SdkLevel.getLevel() < SdkLevel.LEVEL_LOLLIPOP) {
      Log.e(BlockyTalkyBLEConstants.LOG_TAG, "The BluetoothLE extension is unsupported at this API Level. "
          + "Any subsequent function calls will complain.");
    } else {
      Log.d(BlockyTalkyBLEConstants.LOG_TAG, "Appear to have Bluetooth LE support, continuing...");
    }

    innerBLE = new edu.colorado.lpc.blockytalkyble.BluetoothLEint(this, activity, container);
    innerBLE.StartScanning();
  }

  // @SimpleFunction
  public void ReceiveMsgsFromMicrobit() {
    innerBLE.RegisterForByteValues(BlockyTalkyBLEConstants.UART_SERVICE_UUID, BlockyTalkyBLEConstants.TX_CHARACTERISTIC_CHARACTERISTIC_UUID, false,
        txCharacteristicHandler);
  }

  //@SimpleFunction
  public void RequestTXCharacteristic() {
    innerBLE.RegisterForByteValues(BlockyTalkyBLEConstants.UART_SERVICE_UUID, BlockyTalkyBLEConstants.TX_CHARACTERISTIC_CHARACTERISTIC_UUID, false,
        txCharacteristicHandler);
  }

  //@SimpleFunction
  public void StopTXCharacteristicUpdates() {
    innerBLE.UnregisterForValues(BlockyTalkyBLEConstants.UART_SERVICE_UUID, BlockyTalkyBLEConstants.TX_CHARACTERISTIC_CHARACTERISTIC_UUID, txCharacteristicHandler);
  }

  // @SimpleEvent
  // public void TXCharacteristicReceived(final List<Integer> UART_TX_Field) {
  //   EventDispatcher.dispatchEvent(this, "TXCharacteristicReceived", UART_TX_Field);
  // }

  /*@SimpleEvent
  public void MsgReceivedfromMicrobit(final String key, final String value) {
    Log.d(BlockyTalkyBLEConstants.LOG_TAG, "MsgReceivedfrom:" + key + "_" + value + "<-");
    EventDispatcher.dispatchEvent(this, "MsgReceivedfromMicrobit", key, value);
  }*/

  @SimpleEvent
  public void StringReceivedfromMicrobit(final String key, final String value) {
    Log.d(BlockyTalkyBLEConstants.LOG_TAG, "StringReceivedfrom:" + key + "_" + value + "<-");
    EventDispatcher.dispatchEvent(this,"StringReceivedfromMicrobit", key, value);
  }

  @SimpleEvent
  public void NumberReceivedfromMicrobit(final String key, final int value) {
    Log.d(BlockyTalkyBLEConstants.LOG_TAG, "NumberReceivedfrom:" + key + "_" + value + "<-");
    EventDispatcher.dispatchEvent(this,"NumberReceivedfromMicrobit", key, value);
  }

  // starting with one string
  /*@SimpleFunction
  public void SendKeyValue(final String key, final String value) {
    // convert string to bytes need to be an object?
    String message = key + BlockyTalkyBLEConstants.DELIMITER + value + BlockyTalkyBLEConstants.TERMINATOR;
    // innerBLE.WriteByteValuesWithResponse(UART_SERVICE_UUID, RX_CHARACTERISTIC_CHARACTERISTIC_UUID, false, mes, rXCharacteristicWriteHandler);
    if (innerBLE.IsDeviceConnected()) {
      innerBLE.WriteByteValuesWithResponse(BlockyTalkyBLEConstants.UART_SERVICE_UUID, BlockyTalkyBLEConstants.RX_CHARACTERISTIC_CHARACTERISTIC_UUID, false,
          toList(Integer.class, message, 1), rXCharacteristicWriteHandler);
    } else {
      signalError("SendKeyValue", BlockyTalkyBLEError.ERROR_NOT_CURRENTLY_CONNECTED);
      Log.e(BlockyTalkyBLEConstants.LOG_TAG, "No connect microbit to write to ");
    }
    // byte[] content = mes.getBytes("UTF-8");

    // innerBLE.WriteByteValuesWithResponse(UART_SERVICE_UUID, RX_CHARACTERISTIC_CHARACTERISTIC_UUID, false,
    //           checkedCast(Integer.class, toIntList(content)), rXCharacteristicWriteHandler);
  }*/

  public void sendTypedMessage(final String key, final String value, ValueType type) {
    String message = type.getIndicator() + BlockyTalkyBLEConstants.DELIMITER + key + BlockyTalkyBLEConstants.DELIMITER + value + BlockyTalkyBLEConstants.TERMINATOR;
    Log.i(BlockyTalkyBLEConstants.LOG_TAG, "Sending following message: " + message);
    if (innerBLE.IsDeviceConnected()) {
      innerBLE.WriteByteValuesWithResponse(BlockyTalkyBLEConstants.UART_SERVICE_UUID, BlockyTalkyBLEConstants.RX_CHARACTERISTIC_CHARACTERISTIC_UUID, false,
              toList(Integer.class, message, 1), rXCharacteristicWriteHandler);
    } else {
      signalError("SendTypedMessage:" + type.name(), BlockyTalkyBLEError.ERROR_NOT_CURRENTLY_CONNECTED);
      Log.e(BlockyTalkyBLEConstants.LOG_TAG, "No connected microbit to write to ");
    }
  }

  @SimpleFunction
  public void sendStringKeyValue(final String key, final String value) {
    sendTypedMessage(key, value, ValueType.STRING);
  }

  @SimpleFunction
  public void sendNumberKeyValue(final String key, final int value) {
    sendTypedMessage(key, String.valueOf(value), ValueType.NUMBER);
  }

  // Kari - is this an event we should see?? TODO - try it
  // @SimpleEvent
  public void WroteRXCharacteristic(final List<Integer> UART_TX_Field) {
    Log.d(BlockyTalkyBLEConstants.LOG_TAG, "WroteRXCharacteristic");
    EventDispatcher.dispatchEvent(this, "WroteRXCharacteristic", UART_TX_Field);
  }

  // Kari started adding functionf/events here

  @SimpleFunction(description = "Connect to a Microbit with index. Index specifies the position in"
      + " BluetoothLE device list, starting from 1.")
  public void ConnectMicrobit(int index) {
    if (innerBLE != null) {
      innerBLE.Connect(index);
      innerBLE.StopScanning();

    }
  }

  @SimpleFunction(description = "Disconnect from the currently connected Microbit if one is connected.")
  public void Disconnect() {
    if (innerBLE != null) {
      innerBLE.Disconnect();
    }
  }

  // @SimpleFunction(description = "Connect to BluetoothLE device with address. Address specifies bluetooth address" +
  //     " of the BluetoothLE device.")
  // public void ConnectWithAddress(String address) {
  //   if (inner != null) {
  //     inner.ConnectWithAddress(address);
  //   }
  // }

  @SimpleEvent(description = "Trigger event when a Microbit is connected.")
  public void Connected() {
  }

  @SimpleEvent(description = "This event is triggered when a Microbit is disconnected.")
  public void Disconnected() {

  }

  // @SimpleProperty(description = "Return a list of Microbits as a String.",
  //     category = PropertyCategory.BEHAVIOR)
  // public String MicrobitListString() {
  //   if (innerBLE != null) {
  //     Log.d(LOG_TAG,"MicrobitListString:"+innerBLE.DeviceListString());
  //    return innerBLE.DeviceListString();
  //   }
  //   return "";
  // }

  @SimpleProperty(description = "Return a list of Microbits.", category = PropertyCategory.BEHAVIOR)
  public YailList MicrobitList() {
    YailList aList = new YailList(); // empty list
    if (innerBLE != null) {
      ArrayList mBits = innerBLE.DeviceList();
      Log.d(BlockyTalkyBLEConstants.LOG_TAG, "MicrobitList:list passed in arrayList len = " + mBits.size());
      Object[] strArray = mBits.toArray();
      aList = YailList.makeList(mBits);
    }
    return aList;
  }

  @SimpleEvent(description = "Trigger event when a new Microbit is found.")
  public void MicrobitFound() {
  }

  @SimpleFunction(description = "Start Scanning for BMicrobits.")
  public void StartScanning() {
    if (innerBLE != null) {
      innerBLE.StartScanning();
    }
  }

  //@SimpleFunction(description = "Stop Scanning for Microbits.")
  public void StopScanning() {
    if (innerBLE != null) {
      innerBLE.StopScanning();
    }
  }

  public ValueType typeFromString(String typeString) {
      if(typeString.equals("N")) {
        return ValueType.NUMBER;
      } else if (typeString.equals("S")){
        return ValueType.STRING;
      } else {
        return null;
      }
  }

  public void ByteValuesReceived(final String serviceUuid, final String characteristicUuid, final YailList byteValues) {
    EventDispatcher.dispatchEvent(this, "ByteValuesReceived", serviceUuid, characteristicUuid, byteValues);
  }

  public void ShortValuesReceived(final String serviceUuid, final String characteristicUuid,
      final YailList shortValues) {
    EventDispatcher.dispatchEvent(this, "ShortValuesReceived", serviceUuid, characteristicUuid, shortValues);
  }

  public void IntegerValuesReceived(final String serviceUuid, final String characteristicUuid,
      final YailList intValues) {
    EventDispatcher.dispatchEvent(this, "IntegerValuesReceived", serviceUuid, characteristicUuid, intValues);
  }

  public void FloatValuesReceived(final String serviceUuid, final String characteristicUuid,
      final YailList floatValues) {
    EventDispatcher.dispatchEvent(this, "FloatValuesReceived", serviceUuid, characteristicUuid, floatValues);
  }

  public void StringValuesReceived(final String serviceUuid, final String characteristicUuid,
      final YailList stringValues) {
    EventDispatcher.dispatchEvent(this, "StringValuesReceived", serviceUuid, characteristicUuid, stringValues);
  }

  public void StringValuesWritten(final String serviceUuid, final String characteristicUuid,
      final YailList stringValues) {
    EventDispatcher.dispatchEvent(this, "StringValuesWritten", serviceUuid, characteristicUuid, stringValues);
  }

  public void FloatValuesWritten(final String serviceUuid, final String characteristicUuid,
      final YailList floatValues) {
    EventDispatcher.dispatchEvent(this, "FloatValuesWritten", serviceUuid, characteristicUuid, floatValues);
  }

  public void IntegerValuesWritten(final String serviceUuid, final String characteristicUuid,
      final YailList intValues) {
    EventDispatcher.dispatchEvent(this, "IntegerValuesWritten", serviceUuid, characteristicUuid, intValues);
  }

  public void ShortValuesWritten(final String serviceUuid, final String characteristicUuid,
      final YailList shortValues) {
    EventDispatcher.dispatchEvent(this, "ShortValuesWritten", serviceUuid, characteristicUuid, shortValues);
  }

  public void ByteValuesWritten(final String serviceUuid, final String characteristicUuid, final YailList byteValues) {
    EventDispatcher.dispatchEvent(this, "ByteValuesWritten", serviceUuid, characteristicUuid, byteValues);
  }

  // EVERYTHING BELOW HERE IS UTILS AND SHOULD PROBABLY BE IN A SEPARATE CLASS

  /**
   * Convert an unknown object type to a list of a certain type,
   * casting when available/appropriate.
   *
   * The general flow is as follows:
   *
   * 1. If value is of type T, return a list containing the value
   * 2. If value is a YailList:
   *    a. Skip the *list* header and proceed as a list
   * 3. If value is a collection or list:
   *    a. Create a new list checking the type of each entry against T
   * 4. If value is a CharSequence (String or FString)
   *    a. Convert the string to bytes and cast up to T
   * 5. Throw an exception because we don't know how to process the value
   *
   * @param <T> The target type
   * @param tClass Class of the target type, for casting
   * @param value Original value to be converted to a List
   * @param size The size of the type (not necessarily the size of T)
   * @throws ClassCastException if an entity cannot be converted to a list or
   * an element cannot be converted to type T.
   */
  private static <T> List<T> toList(Class<T> tClass, Object value, int size) {
    if (tClass.isAssignableFrom(value.getClass())) {
      return Collections.singletonList(tClass.cast(value));
    } else if (value instanceof YailList) { // must come before List and Collection due to *list* header
      Iterator<?> i = ((YailList) value).iterator();
      i.next(); // skip *list* symbol
      return listFromIterator(tClass, i);
    } else if (value instanceof List) {
      return listFromIterator(tClass, ((List<?>) value).iterator());
    } else if (value instanceof Collection) {
      return listFromIterator(tClass, ((Collection<?>) value).iterator());
    } else if (value instanceof String) {
      // this assumes that the string is being cast to a list of UTF-8 bytes or UTF-16LE chars
      try {
        byte[] content = ((String) value).getBytes(size == 1 ? "UTF-8" : "UTF-16LE");
        if (tClass.equals(Integer.class)) {
          return checkedCast(tClass, toIntList(content));
        }
        return Collections.emptyList();
      } catch (UnsupportedEncodingException e) {
        // Both UTF-8 and UTF-16LE are required by JVM. This should never happen
        Log.wtf(BlockyTalkyBLEConstants.LOG_TAG, "No support for UTF-8 or UTF-16", e);
        return Collections.emptyList();
      }
    } else if (value instanceof FString) {
      // this assumes that the string is being cast to a list of UTF-8 bytes
      return toList(tClass, value.toString(), size);
    } else {
      throw new ClassCastException("Unable to convert " + value + " to list");
    }
  }

  /**
   * Create a checked list from an iterator.
   *
   * @param <T> The target type
   * @param tClass The class of T, for casting
   * @param i Iterator yielding elements for the new list
   * @throws ClassCastException if an entity cannot be converted to a list or
   * an element cannot be converted to type T.
   */
  @SuppressWarnings("unchecked")
  private static <T> List<T> listFromIterator(Class<T> tClass, Iterator<?> i) {
    // Primitive types cannot be cast to one another using boxed values...
    if (tClass.equals(Integer.class)) {
      return (List<T>) toIntList((List<? extends Number>) (List) Lists.newArrayList(i));
    } else if (tClass.equals(Long.class)) {
      return (List<T>) toLongList((List<? extends Number>) (List) Lists.newArrayList(i));
    } else if (tClass.equals(Float.class)) {
      return (List<T>) toFloatList((List<? extends Number>) (List) Lists.newArrayList(i));
    }
    List<T> result = new ArrayList<T>();
    while (i.hasNext()) {
      Object o = i.next();
      if (!tClass.isInstance(o) && !tClass.isAssignableFrom(o.getClass())) {
        throw new ClassCastException(
            "Unable to convert " + o + " of type " + o.getClass().getName() + " to type " + tClass.getName());
      }
      result.add(tClass.cast(o));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static <T> List<T> checkedCast(Class<T> tClass, List<?> list) {
    for (Object o : list) {
      if (!tClass.isInstance(o) && !tClass.isAssignableFrom(o.getClass())) {
        throw new ClassCastException("Unable to convert " + o + " to type " + tClass.getName());
      }
    }
    return (List<T>) list;
  }

  private static <T extends Number> List<Float> toFloatList(List<T> value) {
    List<Float> result = new ArrayList<Float>(value.size());
    for (T o : value) {
      result.add(o.floatValue());
    }
    return result;
  }

  private static <T extends Number> List<Long> toLongList(List<T> value) {
    List<Long> result = new ArrayList<Long>(value.size());
    for (T o : value) {
      result.add(o.longValue());
    }
    return result;
  }

  private static <T extends Number> List<Integer> toIntList(List<T> value) {
    List<Integer> result = new ArrayList<Integer>(value.size());
    for (T o : value) {
      result.add(o.intValue());
    }
    return result;
  }

  private static List<Integer> toIntList(byte[] values) {
    List<Integer> result = new ArrayList<Integer>(values.length);
    for (byte b : values) {
      result.add((int) b);
    }
    return result;
  }

  private void signalError(final String functionName, final BlockyTalkyBLEError errorName, final Object... messageArgs) {
    final String errorMessage = String.format(errorName.getErrorMessage(), messageArgs);
    Log.e(BlockyTalkyBLEConstants.LOG_TAG, errorMessage);
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.d(BlockyTalkyBLEConstants.LOG_TAG, "putting error on screen" + errorMessage);
        container.$form().ErrorOccurredDialog(BlockyTalkyBLE.this, functionName, errorName.getErrorCode(), errorMessage, "BluetoothLE",
            "Dismiss");
      }
    });
  }
}
