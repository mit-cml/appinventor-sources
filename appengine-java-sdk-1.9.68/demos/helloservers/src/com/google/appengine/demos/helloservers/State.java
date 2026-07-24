// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.demos.helloservers;

import java.util.LinkedList;

/**
 * Example class for keeping data in ram between requests.
 *
 *
 */
public class State {

  private static final int MEGABYTE = 1024 * 1024;
  private static final LinkedList<byte[]> state = new LinkedList<byte[]>();

  /**
   * Increase the amount of RAM used.
   *
   * @param megabytes The number of megabytes to grow.
   */
  public static void grow(int megabytes) {
    state.add(new byte[megabytes * MEGABYTE]);
  }

  public static int size() {
    int size = 0;
    for (byte[] data : state) {
      size += data.length;
    }
    return size / MEGABYTE;
  }

}
