// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.TypeConversion;

import junit.framework.TestCase;


/**
 * Tests functionality used in FTC components.
 *
 */
public class FtcTest extends TestCase {
  private static final double DELTA = 0.0001;

  public void testRangeClip() throws Exception {
    assertEquals(0.5, Range.clip(0.5, 0.0, 1.0), DELTA);
    assertEquals(0.0, Range.clip(-0.5, 0.0, 1.0), DELTA);
    assertEquals(1.0, Range.clip(2.5, 0.0, 1.0), DELTA);
  }

  public void testRangeScale() throws Exception {
    assertEquals(0.2, Range.scale(0.0, 0.0, 1.0, 0.2, 0.8), DELTA);
    assertEquals(0.8, Range.scale(1.0, 0.0, 1.0, 0.2, 0.8), DELTA);
    assertEquals(0.5, Range.scale(0.5, 0.0, 1.0, 0.2, 0.8), DELTA);
    assertEquals(0.35, Range.scale(0.25, 0.0, 1.0, 0.2, 0.8), DELTA);
    assertEquals(0.65, Range.scale(0.75, 0.0, 1.0, 0.2, 0.8), DELTA);

    assertEquals(0.0, Range.scale(0.2, 0.2, 0.8, 0.0, 1.0), DELTA);
    assertEquals(1.0, Range.scale(0.8, 0.2, 0.8, 0.0, 1.0), DELTA);
    assertEquals(0.5, Range.scale(0.5, 0.2, 0.8, 0.0, 1.0), DELTA);
    assertEquals(0.25, Range.scale(0.35, 0.2, 0.8, 0.0, 1.0), DELTA);
    assertEquals(0.75, Range.scale(0.65, 0.2, 0.8, 0.0, 1.0), DELTA);
  }

  public void testTypeConversionShort() throws Exception {
    byte[] bytes = TypeConversion.shortToByteArray((short) 0x1234);
    assertEquals((byte) 0x12, bytes[0]);
    assertEquals((byte) 0x34, bytes[1]);
    assertEquals((short) 0x1234, TypeConversion.byteArrayToShort(bytes));
  }

  public void testTypeConversionInt() throws Exception {
    byte[] bytes = TypeConversion.intToByteArray(0x12345678);
    assertEquals((byte) 0x12, bytes[0]);
    assertEquals((byte) 0x34, bytes[1]);
    assertEquals((byte) 0x56, bytes[2]);
    assertEquals((byte) 0x78, bytes[3]);
    assertEquals(0x12345678, TypeConversion.byteArrayToInt(bytes));
  }

  public void testTypeConversionLong() throws Exception {
    byte[] bytes = TypeConversion.longToByteArray(0x1234567890123456L);
    assertEquals((byte) 0x12, bytes[0]);
    assertEquals((byte) 0x34, bytes[1]);
    assertEquals((byte) 0x56, bytes[2]);
    assertEquals((byte) 0x78, bytes[3]);
    assertEquals((byte) 0x90, bytes[4]);
    assertEquals((byte) 0x12, bytes[5]);
    assertEquals((byte) 0x34, bytes[6]);
    assertEquals((byte) 0x56, bytes[7]);
    assertEquals(0x1234567890123456L, TypeConversion.byteArrayToLong(bytes));
  }
}
