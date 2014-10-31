// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests MediaUtil.java.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class MediaUtilTest extends TestCase {

  public void testFileUrlToFilePath() throws Exception {
    assertEquals("/sdcard/17 Candle - Follow Me Down.m4a",
        MediaUtil.fileUrlToFilePath("file:///sdcard/17%20Candle%20-%20Follow%20Me%20Down.m4a"));
    assertEquals("/sdcard/17 Candle - Follow Me Down.m4a",
        MediaUtil.fileUrlToFilePath("file:/sdcard/17%20Candle%20-%20Follow%20Me%20Down.m4a"));

    assertEquals("/sdcard/Ali Spagnola - Radiation.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/Ali%20Spagnola%20-%20Radiation.mp3"));
    assertEquals("/sdcard/Ali Spagnola - Radiation.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/Ali%20Spagnola%20-%20Radiation.mp3"));

    assertEquals("/sdcard/Amanda Blank - DJ.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/Amanda%20Blank%20-%20DJ.mp3"));
    assertEquals("/sdcard/Amanda Blank - DJ.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/Amanda%20Blank%20-%20DJ.mp3"));

    assertEquals("/sdcard/Brett Dennen - Heaven.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/Brett%20Dennen%20-%20Heaven.mp3"));
    assertEquals("/sdcard/Brett Dennen - Heaven.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/Brett%20Dennen%20-%20Heaven.mp3"));

    assertEquals("/sdcard/Jackie Tohn - The Falling.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/Jackie%20Tohn%20-%20The%20Falling.mp3"));
    assertEquals("/sdcard/Jackie Tohn - The Falling.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/Jackie%20Tohn%20-%20The%20Falling.mp3"));

    assertEquals("/sdcard/Marcus Miller - Pluck.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/Marcus%20Miller%20-%20Pluck.mp3"));
    assertEquals("/sdcard/Marcus Miller - Pluck.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/Marcus%20Miller%20-%20Pluck.mp3"));

    assertEquals("/sdcard/Miike Snow - Animal.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/Miike%20Snow%20-%20Animal.mp3"));
    assertEquals("/sdcard/Miike Snow - Animal.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/Miike%20Snow%20-%20Animal.mp3"));

    assertEquals("/sdcard/Mos Def - Quiet Dog.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/Mos%20Def%20-%20Quiet%20Dog.mp3"));
    assertEquals("/sdcard/Mos Def - Quiet Dog.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/Mos%20Def%20-%20Quiet%20Dog.mp3"));

    assertEquals("/sdcard/White Denim - I Start To Run.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/White%20Denim%20-%20I%20Start%20To%20Run.mp3"));
    assertEquals("/sdcard/White Denim - I Start To Run.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/White%20Denim%20-%20I%20Start%20To%20Run.mp3"));

    assertEquals("/sdcard/William Fitzsimmons - Goodmorning.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/William%20Fitzsimmons%20-%20Goodmorning.mp3"));
    assertEquals("/sdcard/William Fitzsimmons - Goodmorning.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/William%20Fitzsimmons%20-%20Goodmorning.mp3"));

    assertEquals("/sdcard/Zack Borer -That's The Way.mp3",
        MediaUtil.fileUrlToFilePath("file:///sdcard/Zack%20Borer%20-That's%20The%20Way.mp3"));
    assertEquals("/sdcard/Zack Borer -That's The Way.mp3",
        MediaUtil.fileUrlToFilePath("file:/sdcard/Zack%20Borer%20-That's%20The%20Way.mp3"));

    try {
      MediaUtil.fileUrlToFilePath("http://www.google.com");
      fail("Exception expected because scheme is not file");
    } catch (IOException e) {
      // Expected
    }

    try {
      MediaUtil.fileUrlToFilePath("not a well formed url");
      fail("Exception expected because url is not well formed");
    } catch (IOException e) {
      // Expected
    }
  }
}
