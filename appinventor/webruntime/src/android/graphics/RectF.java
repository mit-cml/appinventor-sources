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
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.internal.util.FastMath;

import java.io.PrintWriter;

/**
 * RectF holds four float coordinates for a rectangle. The rectangle is
 * represented by the coordinates of its 4 edges (left, top, right, bottom).
 * These fields can be accessed directly. Use width() and height() to retrieve
 * the rectangle's width and height. Note: most methods do not check to see that
 * the coordinates are sorted correctly (i.e. left <= right and top <= bottom).
 */
public class RectF {
  public float left;
  public float top;
  public float right;
  public float bottom;

  /**
   * Create a new empty RectF. All coordinates are initialized to 0.
   */
  public RectF() {
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
  public RectF(float left, float top, float right, float bottom) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }
}
