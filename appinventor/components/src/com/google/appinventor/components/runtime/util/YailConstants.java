// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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