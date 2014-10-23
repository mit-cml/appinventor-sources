// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import junit.framework.TestCase;


/**
 * Tests LegoMindstormsNxtBase.java.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class LegoMindstormsNxtBaseTest extends TestCase {
  private LegoMindstormsNxtBase legoMindstormsNxtBase;
  private byte[] buffer = new byte[4];

  @Override
  protected void setUp() throws Exception {
    legoMindstormsNxtBase = new LegoMindstormsNxtBase();
  }

  public void testBoolean() throws Exception {
    copyAndGetBoolean(true);
    assertEquals(1, buffer[0]);

    copyAndGetBoolean(false);
    assertEquals(0, buffer[0]);
  }

  private void copyAndGetBoolean(boolean value) {
    legoMindstormsNxtBase.copyBooleanValueToBytes(value, buffer, 0);
    assertEquals(value, legoMindstormsNxtBase.getBooleanValueFromBytes(buffer, 0));
  }

  public void testSBYTE() throws Exception {
    copyAndGetSBYTE(0);
    copyAndGetSBYTE(1);
    copyAndGetSBYTE(-1);
    copyAndGetSBYTE(100);
    copyAndGetSBYTE(-100);
    copyAndGetSBYTE(Byte.MAX_VALUE);
    copyAndGetSBYTE(Byte.MIN_VALUE);
  }

  private void copyAndGetSBYTE(int value) {
    legoMindstormsNxtBase.copySBYTEValueToBytes(value, buffer, 0);
    assertEquals(value, legoMindstormsNxtBase.getSBYTEValueFromBytes(buffer, 0));
  }

  public void testUBYTE() throws Exception {
    copyAndGetUBYTE(0x00);
    copyAndGetUBYTE(0x01);
    copyAndGetUBYTE(0x88);
    copyAndGetUBYTE(0xFF);
  }

  private void copyAndGetUBYTE(int value) {
    legoMindstormsNxtBase.copyUBYTEValueToBytes(value, buffer, 0);
    assertEquals(value, legoMindstormsNxtBase.getUBYTEValueFromBytes(buffer, 0));
  }

  public void testSWORD() throws Exception {
    copyAndGetSWORD(0);
    copyAndGetSWORD(1);
    copyAndGetSWORD(-1);
    copyAndGetSWORD(100);
    copyAndGetSWORD(-100);
    copyAndGetSWORD(Short.MAX_VALUE);
    copyAndGetSWORD(Short.MIN_VALUE);
  }

  private void copyAndGetSWORD(int value) {
    legoMindstormsNxtBase.copySWORDValueToBytes(value, buffer, 0);
    assertEquals(value, legoMindstormsNxtBase.getSWORDValueFromBytes(buffer, 0));
  }

  public void testUWORD() throws Exception {
    copyAndGetUWORD(0x0000);
    copyAndGetUWORD(0x0001);
    copyAndGetUWORD(0x8888);
    copyAndGetUWORD(0xFFFF);
  }

  private void copyAndGetUWORD(int value) {
    legoMindstormsNxtBase.copyUWORDValueToBytes(value, buffer, 0);
    assertEquals(value, legoMindstormsNxtBase.getUWORDValueFromBytes(buffer, 0));
  }

  public void testSLONG() throws Exception {
    copyAndGetSLONG(0);
    copyAndGetSLONG(1);
    copyAndGetSLONG(-1);
    copyAndGetSLONG(100);
    copyAndGetSLONG(-100);
    copyAndGetSLONG(Integer.MAX_VALUE);
    copyAndGetSLONG(Integer.MIN_VALUE);
  }

  private void copyAndGetSLONG(int value) {
    legoMindstormsNxtBase.copySLONGValueToBytes(value, buffer, 0);
    assertEquals(value, legoMindstormsNxtBase.getSLONGValueFromBytes(buffer, 0));
  }

  public void testULONG() throws Exception {
    copyAndGetULONG(0x00000000L);
    copyAndGetULONG(0x00000001L);
    copyAndGetULONG(0x88888888L);
    copyAndGetULONG(0xFFFFFFFFL);
  }

  private void copyAndGetULONG(long value) {
    legoMindstormsNxtBase.copyULONGValueToBytes(value, buffer, 0);
    assertEquals(value, legoMindstormsNxtBase.getULONGValueFromBytes(buffer, 0));
  }
}
