// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.flags;

import junit.framework.TestCase;

/**
 * Tests Flag.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class FlagTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty("name", "Kate");
    System.setProperty("age", "29");
    System.setProperty("bad_int", "abc");
    System.setProperty("size", "123.456");
    System.setProperty("bad_float", "abc");
    System.setProperty("married", "true");
  }

  public void testStringFlag() {
    Flag<String> name = Flag.createFlag("name", "");
    assertEquals("Kate", name.get());

    Flag<String> flag = Flag.createFlag("not_found", "default");
    assertEquals("default", flag.get());
  }

  public void testsIntegerFlag() {
    Flag<Integer> age = Flag.createFlag("age", 0);
    assertEquals(29, (int) age.get());

    Flag<Integer> flag = Flag.createFlag("not_found", 217);
    assertEquals(217, (int) flag.get());

    Flag<Integer> bad = Flag.createFlag("bad_int", 217);
    try {
      bad.get();
      fail();
    } catch (IllegalFlagValueException e) {
      // Expected
    }
  }

  public void testsFloatFlag() {
    Flag<Float> size = Flag.createFlag("size", 8f);
    assertEquals(123.456f, size.get());

    Flag<Float> flag = Flag.createFlag("not_found", 218.217f);
    assertEquals(218.217f, flag.get());

    Flag<Float> bad = Flag.createFlag("bad_float", 111.222f);
    try {
      bad.get();
      fail();
    } catch (IllegalFlagValueException e) {
      // Expected
    }
  }

  public void testsBooleanFlag() {
    Flag<Boolean> married = Flag.createFlag("married", false);
    assertTrue(married.get());

    Flag<Boolean> defaultTrue = Flag.createFlag("not_found", true);
    assertTrue(defaultTrue.get());

    Flag<Boolean> defaultFalse = Flag.createFlag("not_found", false);
    assertFalse(defaultFalse.get());
  }

  public void testSetForTest() {
    Flag<String> flag = Flag.createFlag("fun toy", "hula hoop");
    assertEquals("hula hoop", flag.get());

    flag.setForTest("pogo stick");
    assertEquals("pogo stick", flag.get());
  }
}
