// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

/**
 * Bounding box abstraction, similar to {@link android.graphics.Rect}.
 *
 */
public final class BoundingBox {
  private double left;
  private double top;
  private double right;
  private double bottom;

  /**
   * Constructor for a bounding box.  All coordinates are inclusive.
   *
   * @param l leftmost x-coordinate
   * @param t topmost y-coordinate
   * @param r rightmost x-coordinate
   * @param b bottommost y-coordinate
   */
  public BoundingBox(double l, double t, double r, double b) {
    left = l;
    top = t;
    right = r;
    bottom = b;
  }

  /**
   * Determines whether this bounding box intersects with the passed bounding
   * box and, if so, mutates the bounding box to be the intersection.  This was
   * designed to behave the same as
   * {@link android.graphics.Rect#intersect(android.graphics.Rect)}.
   *
   * @param bb bounding box to intersect with this bounding box
   * @return {@code true} if they intersect, {@code false} otherwise
   */
  public boolean intersectDestructively(BoundingBox bb) {
    // Determine intersection.
    double xmin = Math.max(left, bb.left);
    double xmax = Math.min(right, bb.right);
    double ymin = Math.max(top, bb.top);
    double ymax = Math.min(bottom, bb.bottom);

    // If there is no intersection, return false.
    if (xmin > xmax || ymin > ymax) {
      return false;
    }

    // Mutate this bounding box to be the intersection before returning true.
    left = xmin;
    right = xmax;
    top = ymin;
    bottom = ymax;
    return true;

  }

  /**
   * Gets the leftmost x-coordinate
   *
   * @return the leftmost x-coordinate
   */
  public double getLeft() {
    return left;
  }

  /**
   * Gets the uppermost y-coordinate
   *
   * @return the uppermost y-coordinate
   */
  public double getTop() {
    return top;
  }

  /**
   * Gets the rightmost x-coordinate
   *
   * @return the rightmost x-coordinate
   */
  public double getRight() {
    return right;
  }

  /**
   * Gets the bottommost y-coordinate
   *
   * @return the bottommost y-coordinate
   */
  public double getBottom() {
    return bottom;
  }

  public String toString() {
    return "<BoundingBox (left = " + left + ", top = " + top +
        ", right = " + right + ", bottom = " + bottom + ">";
  }
}
