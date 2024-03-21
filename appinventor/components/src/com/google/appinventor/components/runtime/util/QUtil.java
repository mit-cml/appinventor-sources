// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import java.io.File;

/**
 * Utility functions to handle API changes in Android Q.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class QUtil {

  /**
   * Get the SDK-specific external storage path. On Android Q and later, this is always an
   * app-private directory on the "external" storage. On earlier versions of Android, this returns
   * the external storage path for all apps (although possibly user-specific).
   * If the {@code forcePrivate} flag is true, then the app-private directory will always be
   * returned on devices running Android 2.2 Froyo or later.
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @param forcePrivate force the use of the context-specific path
   * @param useLegacy use the legacy external storage path even on Android Q and higher
   * @return the path to the app's shared storage.
   */
  public static String getExternalStoragePath(Context context, boolean forcePrivate,
      boolean useLegacy) {
    return getExternalStorageDir(context, forcePrivate, useLegacy).getAbsolutePath();
  }

  /**
   * Get the SDK-specific external storage path. On Android Q and later, this is always an
   * app-private directory on the "external" storage. On earlier versions of Android, this returns
   * the external storage path for all apps (although possibly user-specific).
   * If the {@code forcePrivate} flag is true, then the app-private directory will always be
   * returned on devices running Android 2.2 Froyo or later.
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @param forcePrivate force the use of the context-specific path
   * @return the path to the app's shared storage.
   */
  public static String getExternalStoragePath(Context context, boolean forcePrivate) {
    return getExternalStoragePath(context, forcePrivate, false);
  }

  /**
   * Get the SDK-specific external storage path. On Android Q and later, this is always an
   * app-private directory on the "external" storage. On earlier versions of Android, this returns
   * the external storage path for all apps (although possibly user-specific).
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @return the path to the app's shared storage.
   */
  public static String getExternalStoragePath(Context context) {
    return getExternalStoragePath(context, false, false);
  }

  /**
   * Get the SDK-specific external storage directory. On Android Q and later, this is always an
   * app-private directory on the "external" storage. On earlier versions of Android, this returns
   * the external storage path for all apps (although possibly user-specific).
   * If the {@code forcePrivate} flag is true, then the app-private directory will always be
   * returned on devices running Android 2.2 Froyo or later.
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @return the path to the app's shared storage.
   */
  public static File getExternalStorageDir(Context context) {
    return getExternalStorageDir(context, false);
  }

  /**
   * Get the SDK-specific external storage directory. On Android Q and later, this is always an
   * app-private directory on the "external" storage. On earlier versions of Android, this returns
   * the external storage path for all apps (although possibly user-specific).
   * If the {@code forcePrivate} flag is true, then the app-private directory will always be
   * returned on devices running Android 2.2 Froyo or later.
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @param forcePrivate force the use of the context-specific path
   * @return the path to the app's shared storage.
   */
  public static File getExternalStorageDir(Context context, boolean forcePrivate) {
    return getExternalStorageDir(context, forcePrivate, false);
  }

  /**
   * Get the SDK-specific external storage directory. On Android Q and later, this is always an
   * app-private directory on the "external" storage. On earlier versions of Android, this returns
   * the external storage path for all apps (although possibly user-specific).
   * If the {@code forcePrivate} flag is true, then the app-private directory will always be
   * returned on devices running Android 2.2 Froyo or later.
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @param forcePrivate force the use of the context-specific path
   * @param legacy use the legacy path on Android Q and later
   * @return the path to the app's shared storage.
   */
  @SuppressWarnings("deprecation")
  public static File getExternalStorageDir(Context context, boolean forcePrivate, boolean legacy) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
      // Context#getExternalFilesDir didn't exist until Froyo
      return Environment.getExternalStorageDirectory();
    }
    if ((!legacy && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) || forcePrivate) {
      // Q no longer allows using getExternalStorageDirectory()
      return context.getExternalFilesDir(null);
    } else {
      return Environment.getExternalStorageDirectory();
    }
  }

  /**
   * Get the SDK-specific path to where the REPL assets live. On Android Q and later, this is always
   * an app-private directory on the "external" storage. On earlier versions of Android, this
   * returns the external storage path for all apps (although possibly user-specific).
   * If the {@code forcePrivate} flag is true, then the app-private directory will always be
   * returned on devices running Android 2.2 Froyo or later.
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @param forcePrivate force the use of the context-specific path
   * @return the path to the REPL's asset storage
   */
  public static String getReplAssetPath(Context context, boolean forcePrivate) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return getExternalStoragePath(context, forcePrivate) + "/assets/";
    } else {
      return getExternalStoragePath(context, forcePrivate) + "/AppInventor/assets/";
    }
  }

  /**
   * Get the SDK-specific path where the REPL databases live. On Android Q and later, this is always
   * an app-private directory on the "external" storage. On earlier versions of Android, this
   * returns the external storage path for all apps (although possibly user-specific).
   * If the {@code forcePrivate} flag is true, then the app-private directory will always be
   * returned on devices running Android 2.2 Froyo or later.
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @param forcePrivate force the use of the context-specific path
   * @return the path to the REPL's database storage
   */
  public static String getReplDatabasePath(Context context, boolean forcePrivate) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return getExternalStoragePath(context, forcePrivate) + "/databases/";
    } else {
      return getExternalStoragePath(context, forcePrivate) + "/AppInventor/databases/";
    }
  }

  /**
   * Get the SDK-specific path to where the REPL assets live. On Android Q and later, this is always
   * an app-private directory on the "external" storage. On earlier versions of Android, this
   * returns the external storage path for all apps (although possibly user-specific).
   *
   * <p>
   * For more details on why this is needed, see the deprecation notice at
   * https://developer.android.com/reference/android/os/Environment#getExternalStorageDirectory()
   * </p>
   *
   * @param context a context, such as a Form
   * @return the path to the REPL's asset storage
   */
  public static String getReplAssetPath(Context context) {
    return getReplAssetPath(context, false);
  }

  /**
   * Get the SDK-specific path to where the REPL data live. On Android Q and later, this is always
   * an app-private directory on the "external" storage. On earlier versions of Android, this
   * returns the external storage path for all apps (although possibly user-specific).
   * If the {@code forcePrivate} flag is true, then the app-private directory will always be
   * returned on devices running Android 2.2 Froyo or later.
   *
   * @param context a context, such as a Form
   * @param forcePrivate force the use of the context-specific path
   * @return the path to the REPL's data storage
   */
  public static String getReplDataPath(Context context, boolean forcePrivate) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return getExternalStoragePath(context, forcePrivate) + "/data/";
    } else {
      return getExternalStoragePath(context, forcePrivate) + "/AppInventor/data/";
    }
  }

  /**
   * Get the SDK-specific path to where the REPL data live. On Android Q and later, this is always
   * an app-private directory on the "external" storage. On earlier versions of Android, this
   * returns the external storage path for all apps (although possibly user-specific).
   *
   * @param context a context, such as a Form
   * @return the path to the REPL's data storage
   */
  public static String getReplDataPath(Context context) {
    return getReplDataPath(context, false);
  }
}
