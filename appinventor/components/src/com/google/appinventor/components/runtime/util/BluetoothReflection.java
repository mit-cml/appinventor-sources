// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

// Even though the code doesn't need these imports, javadoc does.
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/**
 * Provides access to Android Bluetooth classes via Java reflection.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class BluetoothReflection {
  private static final int BOND_BONDED = 0xC; // from android.bluetooth.BluetoothDevice

  private BluetoothReflection() {
  }

  // BluetoothAdapter methods

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothAdapter#getDefaultAdapter()}.
   *
   * @return a {@link android.bluetooth.BluetoothAdapter} object, or null if
   *         Bluetooth is not available
   */
  public static Object getBluetoothAdapter() {
    // return BluetoothAdapter.getDefaultAdapter();
    Class bluetoothAdapterClass;
    try {
      bluetoothAdapterClass = Class.forName("android.bluetooth.BluetoothAdapter");
    } catch (ClassNotFoundException e) {
      // Bluetooth is not available on this Android device.
      return null;
    }
    return invokeStaticMethod(getMethod(bluetoothAdapterClass, "getDefaultAdapter"));
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothAdapter#isEnabled()}.
   *
   * @param bluetoothAdapter a {@link android.bluetooth.BluetoothAdapter} object
   * @return true if Bluetooth is enabled, false otherwise
   */
  public static boolean isBluetoothEnabled(Object bluetoothAdapter) {
    // boolean enabled = bluetoothAdapter.isEnabled();
    return (Boolean) invokeMethod(getMethod(bluetoothAdapter.getClass(), "isEnabled"),
        bluetoothAdapter);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothAdapter#getBondedDevices()}.
   *
   * @param bluetoothAdapter a {@link android.bluetooth.BluetoothAdapter} object
   * @return a Set of {@link android.bluetooth.BluetoothDevice} objects
   */
  public static Set getBondedDevices(Object bluetoothAdapter) {
    // return bluetoothAdapter.getBondedDevices();
    return (Set) invokeMethod(getMethod(bluetoothAdapter.getClass(), "getBondedDevices"),
        bluetoothAdapter);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothAdapter#checkBluetoothAddress(String)}.
   *
   * @param bluetoothAdapter a {@link android.bluetooth.BluetoothAdapter} object
   * @param address a string that might be a bluetooth MAC address
   * @return true if the address is valid, false otherwise
   */
  public static boolean checkBluetoothAddress(Object bluetoothAdapter, String address) {
    // return bluetoothAdapter.checkBluetoothAddress(address);
    return (Boolean) invokeMethod(
        getMethod(bluetoothAdapter.getClass(), "checkBluetoothAddress", String.class),
        bluetoothAdapter, address);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothAdapter#getRemoteDevice(String)}.
   *
   * @param bluetoothAdapter a {@link android.bluetooth.BluetoothAdapter} object
   * @param address the bluetooth MAC address of the device
   * @return a {@link android.bluetooth.BluetoothDevice} object
   */
  public static Object getRemoteDevice(Object bluetoothAdapter, String address)
      throws IllegalArgumentException {
    // return bluetoothAdapter.getRemoteDevice(address);
    return invokeMethodThrowsIllegalArgumentException(
        getMethod(bluetoothAdapter.getClass(), "getRemoteDevice", String.class),
        bluetoothAdapter, address);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothAdapter#listenUsingRfcommWithServiceRecord(String,UUID)}.
   *
   * @param bluetoothAdapter a {@link android.bluetooth.BluetoothAdapter} object
   * @param name  service name for SDP record
   * @param uuid  uuid for SDP record
   * @return a listening RFCOMM {@link android.bluetooth.BluetoothServerSocket}
   */
  public static Object listenUsingRfcommWithServiceRecord(Object bluetoothAdapter, String name,
      UUID uuid) throws IOException {
    // return bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid);
    return invokeMethodThrowsIOException(getMethod(bluetoothAdapter.getClass(),
        "listenUsingRfcommWithServiceRecord", String.class, UUID.class),
        bluetoothAdapter, name, uuid);
  }

  // Note: The following @link generates a Javadoc warning because we build against a
  // version of the jar that doesn't yet have the method, which was added in API level 10.
  /**
   * Invokes the method {@link
   * android.bluetooth.BluetoothAdapter#listenUsingInsecureRfcommWithServiceRecord(String, UUID)}.
   *
   * @param bluetoothAdapter a {@link android.bluetooth.BluetoothAdapter} object
   * @param name  service name for SDP record
   * @param uuid  uuid for SDP record
   * @return a listening RFCOMM {@link android.bluetooth.BluetoothServerSocket}
   */
  public static Object listenUsingInsecureRfcommWithServiceRecord(
      Object bluetoothAdapter, String name, UUID uuid) throws IOException {
    // return bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
    return invokeMethodThrowsIOException(getMethod(bluetoothAdapter.getClass(),
        "listenUsingInsecureRfcommWithServiceRecord", String.class, UUID.class),
        bluetoothAdapter, name, uuid);
  }

  // BluetoothDevice methods

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothDevice#getName()}.
   *
   * @param bluetoothDevice a {@link android.bluetooth.BluetoothDevice} object
   * @return the name of the given {@link android.bluetooth.BluetoothDevice}
   */
  public static String getBluetoothDeviceName(Object bluetoothDevice) {
    // return bluetoothDevice.getName();
    return (String) invokeMethod(getMethod(bluetoothDevice.getClass(), "getName"),
        bluetoothDevice);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothDevice#getAddress()}.
   *
   * @param bluetoothDevice a {@link android.bluetooth.BluetoothDevice} object
   * @return the address of the given {@link android.bluetooth.BluetoothDevice}
   */
  public static String getBluetoothDeviceAddress(Object bluetoothDevice) {
    // return bluetoothDevice.getAddress();
    return (String) invokeMethod(getMethod(bluetoothDevice.getClass(), "getAddress"),
        bluetoothDevice);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothDevice#getBondState()}.
   *
   * @param bluetoothDevice a {@link android.bluetooth.BluetoothDevice} object
   * @return the bond state of the given {@link android.bluetooth.BluetoothDevice}
   */
  public static boolean isBonded(Object bluetoothDevice) {
    // return bluetoothDevice.getBondState() == BOND_BONDED;
    int bondState = (Integer) invokeMethod(getMethod(bluetoothDevice.getClass(), "getBondState"),
        bluetoothDevice);
    return bondState == BOND_BONDED;
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothDevice#getBluetoothClass()}.
   *
   * @param bluetoothDevice a {@link android.bluetooth.BluetoothDevice} object
   * @return the BluetoothClass of the given {@link android.bluetooth.BluetoothDevice}
   */
  public static Object getBluetoothClass(Object bluetoothDevice) {
    // return bluetoothDevice.getBluetoothClass();
    return invokeMethod(getMethod(bluetoothDevice.getClass(), "getBluetoothClass"),
        bluetoothDevice);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothDevice#createRfcommSocketToServiceRecord(UUID)}.
   *
   * @param bluetoothDevice a {@link android.bluetooth.BluetoothDevice} object
   * @param uuid the service record uuid
   * @return a {@link android.bluetooth.BluetoothSocket} object
   */
  public static Object createRfcommSocketToServiceRecord(Object bluetoothDevice, UUID uuid)
      throws IOException {
    // return bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
    return invokeMethodThrowsIOException(
        getMethod(bluetoothDevice.getClass(), "createRfcommSocketToServiceRecord", UUID.class),
        bluetoothDevice, uuid);
  }

  // Note: The following @link generates a Javadoc warning because we build against a
  // version of the jar that doesn't yet have the method, which was added in API level 10.
  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothDevice#createInsecureRfcommSocketToServiceRecord(Object, UUID)}.
   *
   * @param bluetoothDevice a {@link android.bluetooth.BluetoothDevice} object
   * @param uuid the service record uuid
   * @return a {@link android.bluetooth.BluetoothSocket} object
   */
  public static Object createInsecureRfcommSocketToServiceRecord(Object bluetoothDevice, UUID uuid)
      throws IOException {
    // return bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
    return invokeMethodThrowsIOException(
        getMethod(bluetoothDevice.getClass(),
            "createInsecureRfcommSocketToServiceRecord", UUID.class),
        bluetoothDevice, uuid);
  }

  // BluetoothClass methods

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothClass#getDeviceClass()}.
   *
   * @param bluetoothClass a {@link android.bluetooth.BluetoothClass} object
   * @return the (major and minor) device class component of the given
   *         {@link android.bluetooth.BluetoothClass}
   */
  public static int getDeviceClass(Object bluetoothClass) {
    // return bluetoothClass.getBluetoothClass();
    return (Integer) invokeMethod(getMethod(bluetoothClass.getClass(), "getDeviceClass"),
        bluetoothClass);
  }

  // BluetoothSocket methods

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothSocket#connect()}.
   *
   * @param bluetoothSocket a {@link android.bluetooth.BluetoothSocket} object
   */
  public static void connectToBluetoothSocket(Object bluetoothSocket) throws IOException {
    // bluetoothSocket.connect();
    invokeMethodThrowsIOException(getMethod(bluetoothSocket.getClass(), "connect"),
        bluetoothSocket);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothSocket#getInputStream()}.
   *
   * @param bluetoothSocket a {@link android.bluetooth.BluetoothSocket} object
   * @return the {@link InputStream}
   */
  public static InputStream getInputStream(Object bluetoothSocket) throws IOException {
    // return bluetoothSocket.getInputStream();
    return (InputStream) invokeMethodThrowsIOException(
        getMethod(bluetoothSocket.getClass(), "getInputStream"),
        bluetoothSocket);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothSocket#getOutputStream()}.
   *
   * @param bluetoothSocket a {@link android.bluetooth.BluetoothSocket} object
   * @return the {@link OutputStream}
   */
  public static OutputStream getOutputStream(Object bluetoothSocket) throws IOException {
    // return bluetoothSocket.getOutputStream();
    return (OutputStream) invokeMethodThrowsIOException(
        getMethod(bluetoothSocket.getClass(), "getOutputStream"),
        bluetoothSocket);
  }

  /**
   * Invokes the method
   * {@link android.bluetooth.BluetoothSocket#close()}.
   *
   * @param bluetoothSocket a {@link android.bluetooth.BluetoothSocket} object
   */
  public static void closeBluetoothSocket(Object bluetoothSocket) throws IOException {
    // bluetoothSocket.close();
    invokeMethodThrowsIOException(getMethod(bluetoothSocket.getClass(), "close"),
        bluetoothSocket);
  }

  // BluetoothServerSocket methods

  /**
   * Invokes the method {@link android.bluetooth.BluetoothServerSocket#accept()}.
   *
   * @param bluetoothServerSocket a {@link android.bluetooth.BluetoothServerSocket} object
   * @return a {@link android.bluetooth.BluetoothSocket} object
   */
  public static Object accept(Object bluetoothServerSocket) throws IOException {
    // return bluetoothServerSocket.accept();
    return invokeMethodThrowsIOException(getMethod(bluetoothServerSocket.getClass(), "accept"),
        bluetoothServerSocket);
  }

  /**
   * Invokes the method {@link android.bluetooth.BluetoothServerSocket#close()}.
   *
   * @param bluetoothServerSocket a {@link android.bluetooth.BluetoothServerSocket} object
   */
  public static void closeBluetoothServerSocket(Object bluetoothServerSocket) throws IOException {
    // bluetoothServerSocket.close();
    invokeMethodThrowsIOException(getMethod(bluetoothServerSocket.getClass(), "close"),
        bluetoothServerSocket);
  }


  // Reflection helper methods

  private static Method getMethod(Class clazz, String name) {
    try {
      return clazz.getMethod(name, new Class[0]);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static Method getMethod(Class clazz, String name, Class<?>... parameterTypes) {
    try {
      return clazz.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object invokeStaticMethod(Method method) {
    try {
      return method.invoke(null);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      cause.printStackTrace();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(cause);
      }
    }
  }

  private static Object invokeMethod(Method method, Object thisObject, Object... args) {
    try {
      return method.invoke(thisObject, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      cause.printStackTrace();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(cause);
      }
    }
  }

  private static Object invokeMethodThrowsIllegalArgumentException(Method method,
      Object thisObject, Object... args) throws IllegalArgumentException {
    try {
      return method.invoke(thisObject, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      cause.printStackTrace();
      if (cause instanceof IllegalArgumentException) {
        throw (IllegalArgumentException) cause;
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  private static Object invokeMethodThrowsIOException(Method method, Object thisObject,
      Object... args) throws IOException {
    try {
      return method.invoke(thisObject, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      cause.printStackTrace();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(e);
      }
    }
  }
}
