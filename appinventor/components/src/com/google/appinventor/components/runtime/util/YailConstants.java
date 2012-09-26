// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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