/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.graphics;

import android.annotation.NonNull;

/**
 * Rect holds four integer coordinates for a rectangle. The rectangle is
 * represented by the coordinates of its 4 edges (left, top, right bottom).
 * These fields can be accessed directly. Use width() and height() to retrieve
 * the rectangle's width and height. Note: most methods do not check to see that
 * the coordinates are sorted correctly (i.e. left <= right and top <= bottom).
 * <p>
 * Note that the right and bottom coordinates are exclusive. This means a Rect
 * being drawn untransformed onto a {@link android.graphics.Canvas} will draw
 * into the column and row described by its left and top coordinates, but not
 * those of its bottom and right.
 */
public final class Rect {
  public int left;
  public int top;
  public int right;
  public int bottom;

  /**
   * Create a new empty Rect. All coordinates are initialized to 0.
   */
  public Rect() {
  }

  /**
   * Create a new rectangle with the specified coordinates. Note: no range
   * checking is performed, so the caller must ensure that left <= right and
   * top <= bottom.
   *
   * @param left   The X coordinate of the left side of the rectangle
   * @param top    The Y coordinate of the top of the rectangle
   * @param right  The X coordinate of the right side of the rectangle
   * @param bottom The Y coordinate of the bottom of the rectangle
   */
  public Rect(int left, int top, int right, int bottom) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  /**
   * Create a new rectangle with the specified coordinates. Note: no range
   * checking is performed, so the caller must ensure that left <= right and
   * top <= bottom.
   *
   * @param src The rectangle to copy from
   */
  public Rect(@NonNull Rect src) {
    this.left = src.left;
    this.top = src.top;
    this.right = src.right;
    this.bottom = src.bottom;
  }
}
