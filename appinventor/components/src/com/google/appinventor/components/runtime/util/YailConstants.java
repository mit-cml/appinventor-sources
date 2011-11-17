// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime.util;

import gnu.mapping.SimpleSymbol;

/**
 * YailConstants contains variables used by Yail.
 *
 */
public class YailConstants {

  //  YailLists must begin with YAIL_HEADER
  public static final SimpleSymbol YAIL_HEADER = new SimpleSymbol("*list*");

  // Disable instantiation
  private YailConstants() {
  }
}