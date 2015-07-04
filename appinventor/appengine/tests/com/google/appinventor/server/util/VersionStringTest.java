// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import static junit.framework.Assert.*;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Tests {@link VersionString}.
 *
 * @author spertus@google.com (Ellen Spertus)
 */
public class VersionStringTest extends TestCase {
  private static VersionString v0 = new VersionString("0.0.91");
  private static VersionString v1 = new VersionString("0.91");
  private static VersionString v2 = new VersionString("0.100.5");
  private static VersionString v3 = new VersionString("1");
  private static VersionString v4 = new VersionString("1.0.0.1");

  // Considered including convenience method for doing comparisons,
  // but reviewer wisely pointed out this would make the line numbers
  // in test failures less useful.
  //
  // private static int compare(String vs1, String vs2) {
  //   return new VersionString(vs1).compareTo(new VersionString(vs2));
  // }

  public void testNumericallyEqual() throws Exception {
    assertEquals(0, new VersionString("1").compareTo(new VersionString("00001")));
    assertEquals(0, new VersionString("1.0.5").compareTo(new VersionString("01.00.05")));
    assertEquals(new VersionString("1.5"), new VersionString("1.5"));
    assertEquals(new VersionString("1.0"), new VersionString("1.00.00.0"));
    assertEquals(new VersionString("3.0"), new VersionString("3"));
  }

  public void testMalformatted() throws Exception {
    try {
      new VersionString("");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new VersionString(".");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new VersionString(".2");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new VersionString("3..6");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new VersionString("12a3");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new VersionString("12.");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new VersionString("12.-6");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testEqualLengths() throws Exception {
    assertEquals(0, new VersionString("1.5").compareTo(new VersionString("1.5")));
    assertEquals(+1, new VersionString("1.14.2").compareTo(new VersionString("1.5.9")));
    assertEquals(-1, new VersionString("3.1.4").compareTo(new VersionString("3.2.3")));
  }

  public void testUnequalLengths() throws Exception {
    // First is shorter.
    assertEquals(-1, new VersionString("1.2").compareTo(new VersionString("1.2.1")));
    assertEquals(0, new VersionString("1.0").compareTo(new VersionString("1.00.00.0")));
    assertEquals(+1, new VersionString("2.16").compareTo(new VersionString("2.9.4")));

    // Second is shorter.
    assertEquals(-1, new VersionString("1.2.3").compareTo(new VersionString("1.3")));
    assertEquals(0, new VersionString("3.0").compareTo(new VersionString("3")));
    assertEquals(+1, new VersionString("1.8.11").compareTo(new VersionString("1.8")));
  }

  public void testSorting() throws Exception {
    SortedSet<VersionString> set = new TreeSet<VersionString>();
    set.add(v3);
    set.add(v0);
    set.add(v1);
    set.add(v4);
    set.add(v2);
    assertEquals(v0, set.first());
    assertTrue(set.remove(v0));
    assertEquals(v1, set.first());
    assertTrue(set.remove(v1));
    assertEquals(v2, set.first());
    assertTrue(set.remove(v2));
    assertEquals(v3, set.first());
    assertTrue(set.remove(v3));
    assertEquals(v4, set.first());
    assertTrue(set.remove(v4));
    assertTrue(set.isEmpty());
  }

  public void testHashing() throws Exception {
    Map<VersionString, Integer> map = new HashMap<VersionString, Integer>();
    map.put(v3, 3);
    map.put(v0, 0);
    map.put(v1, 1);
    map.put(v2, 2);
    map.put(v4, 4);

    // Calls to intValue() are needed to help compiler distninguish between
    // assertEquals(int, int) and assertEquals(Object, Object).
    assertEquals(0, map.get(new VersionString("0.0.091.0")).intValue());
    assertEquals(1, map.get(new VersionString("000.91.00")).intValue());
    assertEquals(2, map.get(new VersionString("0000.100.05")).intValue());
    assertEquals(3, map.get(new VersionString("1.0.0.000")).intValue());
    assertEquals(4, map.get(new VersionString("01.0.0.01.0")).intValue());
  }

  public void testToString() throws Exception {
    assertEquals("0.0.91", v0.toString());
    assertEquals("0.91", v1.toString());
    assertEquals("0.100.5", v2.toString());
    assertEquals("1", v3.toString());
    assertEquals("1.0.0.1", v4.toString());
    assertEquals("1.0.4", new VersionString("1.0.04.0").toString());
  }
}
