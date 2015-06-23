// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.collect.Maps;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MemoryLeakUtil provides some useful methods for detecting memory leaks.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class MemoryLeakUtil {
  private static final String LOG_TAG = "MemoryLeakUtil";

  private static final AtomicInteger prefixGenerator = new AtomicInteger(0);
  private static final Map<String, WeakReference<Object>> TRACKED_OBJECTS = Maps.newTreeMap();

  private MemoryLeakUtil() {
  }

  /**
   * Tracks the given Object by saving a WeakReference to it so that we can check later if it has
   * been garbage collected.
   *
   * @param tag the tag for the Object
   * @param object the Object to track
   * @return the key that is used to track the Object
   */
  public static String trackObject(String tag, Object object) {
    String key = (tag == null)
        ? prefixGenerator.incrementAndGet() + "_"
        : prefixGenerator.incrementAndGet() + "_" + tag;
    TRACKED_OBJECTS.put(key, new WeakReference<Object>(object));
    return key;
  }

  /**
   * Checks whether a particular tracked object has been garbage collected.
   *
   * @param key a key returned from {@link #trackObject}
   * @param stopTrackingIfCollected whether to stop tracking the object if it has been garbage
   *        collected
   * @return true if the tracked object has been collected; false otherwise
   * @throws IllegalArgumentException if the key is not valid
   */
  public static boolean isTrackedObjectCollected(String key, boolean stopTrackingIfCollected) {
    System.gc();
    WeakReference<Object> ref = TRACKED_OBJECTS.get(key);
    if (ref != null) {
      Object o = ref.get();
      String tag = key.substring(key.indexOf("_") + 1);
      Log.i(LOG_TAG, "Object with tag " + tag + " has " +
          ((o != null) ? "not " : "") + "been garbage collected.");
      if (stopTrackingIfCollected && o == null) {
        TRACKED_OBJECTS.remove(key);
      }
      return o == null;
    }
    throw new IllegalArgumentException("key not found");
  }

  /**
   * Checks whether all tracked objects have been garbage collected, logging summary information
   * including how many have been collected and how many have not. Optionally, it can log details
   * showing which objects have been garbage collected and which have not.
   *
   * @param verbose whether to log details about each tracked object
   * @param stopTrackingCollectedObjects whether to stop tracking objects that have been garbage
   *        collected
   */
  public static void checkAllTrackedObjects(boolean verbose, boolean stopTrackingCollectedObjects) {
    Log.i(LOG_TAG, "Checking Tracked Objects ----------------------------------------");
    System.gc();
    int countRemaining = 0;
    int countCollected = 0;
    for (Iterator<Map.Entry<String, WeakReference<Object>>> it =
        TRACKED_OBJECTS.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, WeakReference<Object>> entry = it.next();
      String key = entry.getKey();
      WeakReference<Object> ref = entry.getValue();
      Object o = ref.get();
      if (o != null) {
        countRemaining++;
      } else {
        countCollected++;
        if (stopTrackingCollectedObjects) {
          it.remove();
        }
      }
      if (verbose) {
        String tag = key.substring(key.indexOf("_") + 1);
        Log.i(LOG_TAG, "Object with tag " + tag + " has " +
            ((o != null) ? "not " : "") + "been garbage collected.");
      }
    }
    Log.i(LOG_TAG, "summary: collected " + countCollected);
    Log.i(LOG_TAG, "summary: remaining " + countRemaining);
    Log.i(LOG_TAG, "-----------------------------------------------------------------");
  }
}
