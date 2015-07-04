// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import junit.framework.TestCase;

/**
 * Tests BoundingBox class.
 *
 */
public class BoundingBoxTest extends TestCase {
  private void assertEquality(BoundingBox bb, double left, double top,
                              double right, double bottom) {
    assertEquals(left, bb.getLeft());
    assertEquals(top, bb.getTop());
    assertEquals(right, bb.getRight());
    assertEquals(bottom, bb.getBottom());
  }

  private void assertNoIntersection(
      double left1, double top1, double right1, double bottom1,
      double left2, double top2, double right2, double bottom2) {
    // Create bounding boxes.
    BoundingBox bb1 = new BoundingBox(left1, top1, right1, bottom1);
    BoundingBox bb2 = new BoundingBox(left2, top2, right2, bottom2);

    // Make sure they don't intersect.
    assertFalse(bb1.intersectDestructively(bb2));
    assertFalse(bb2.intersectDestructively(bb1));

    // Make sure bounding boxes are unchanged.
    assertEquality(bb1, left1, top1, right1, bottom1);
    assertEquality(bb2, left2, top2, right2, bottom2);
  }

  public void testIntersectNone() {
    // First box is entirely above second box.
    assertNoIntersection(100, 5, 200, 10,
                        100, 11, 200, 25);

    // First box is entirely below second box.
    assertNoIntersection(100, 50, 200, 60,
                        100, 11, 200, 25);

    // First box is entirely to the left of second box
    assertNoIntersection(50, 5, 99, 10,
                        100, 5, 200, 25);

    // First box is entirely to the right of second box
    assertNoIntersection(201, 5, 230, 10,
                        100, 5, 200, 25);

  }

  private void assertIntersection(
      double left1, double top1, double right1, double bottom1,
      double left2, double top2, double right2, double bottom2,
      double left, double top, double right, double bottom) {
    // Create bounding boxes.
    BoundingBox bb1 = new BoundingBox(left1, top1, right1, bottom1);
    BoundingBox bb2 = new BoundingBox(left2, top2, right2, bottom2);

    // Check result and mutated object.
    assertTrue(bb1.intersectDestructively(bb2));
    assertEquality(bb1, left, top, right, bottom);
    assertEquality(bb2, left2, top2, right2, bottom2);

    // Redefine the boxes and reverse the order of comparison.
    bb1 = new BoundingBox(left1, top1, right1, bottom1);
    bb2 = new BoundingBox(left2, top2, right2, bottom2);

    // Check result and mutated object.
    assertTrue(bb2.intersectDestructively(bb1));
    assertEquality(bb2, left, top, right, bottom);
    assertEquality(bb1, left1, top1, right1, bottom1);
  }

  public void testIntersection() {
    assertIntersection(
        100, 5, 200, 10,   // bounding box 1
        100, 9, 101, 10,   // bounding box 2
        100, 9, 101, 10);  // intersection
    assertIntersection(
        100, 5, 200, 10,   // bounding box 1
        100, 5, 200, 10,   // bounding box 2
        100, 5, 200, 10);  // intersection
    assertIntersection(
        100, 5, 200, 10,   // bounding box 1
        90, 4, 201, 20,    // bounding box 2
        100, 5, 200, 10);  // intersection
    assertIntersection(
        100, 5, 200, 10,   // bounding box 1
        110, 4, 190, 9,    // bounding box 2
        110, 5, 190, 9);   // intersection
  }
}
