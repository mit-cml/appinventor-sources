// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;
//TODO: Remove debugging code.
//Gabriel Modifications
import android.util.Log;

/**
 * Bounding box abstraction, similar to {@link android.graphics.Rect}.
 *
 * Gabriel Modification: Made compatible with rotation.
 */
public final class BoundingBox {
	private double left;
	private double top;
	private double right;
	private double bottom;

	//START: Gabriel Modification
	private boolean rotates = false;
	private double centerX;
	private double centerY;
	private double extentLength;
	private double extentHeight;
	// The following axes are meant to be components of unit vectors.
	private double axisLengthX;
	private double axisLengthY;
	private double axisHeightX;
	private double axisHeightY;

	// Vertices
	private double vertTopRightX;
	private double vertTopRightY;
	private double vertTopLeftX;
	private double vertTopLeftY;
	private double vertBottomRightX;
	private double vertBottomRightY;
	private double vertBottomLeftX;
	private double vertBottomLeftY;
	//END: Gabriel Modification

	/**
	 * Constructor for a bounding box. All coordinates are inclusive.
	 *
	 * @param l
	 *            leftmost x-coordinate
	 * @param t
	 *            topmost y-coordinate
	 * @param r
	 *            rightmost x-coordinate
	 * @param b
	 *            bottommost y-coordinate
	 */
	public BoundingBox(double l, double t, double r, double b) {
		left = l;
		top = t;
		right = r;
		bottom = b;
	}
	
	//START: Gabriel Modification. Rotation compatible constructor
	/**
	 * Constructor for an oriented bounding box. TODO: Confident I only need 3
	 * of the vertices. Revise and refactor.
	 *
	 * @param vertTopRightX
	 *            x-coordinate for Top Right vertex
	 * @param vertTopRightY
	 *            y-coordinate for Top Right vertex
	 * @param vertTopLeftX
	 *            x-coordinate for Top Left vertex
	 * @param vertTopLeftY
	 *            y-coordinate for Top Left vertex
	 * @param vertBottomRightX
	 *            x-coordinate for Bottom Right vertex
	 * @param vertBottomRightY
	 *            y-coordinate for Bottom Right vertex
	 * @param vertBottomLeftX
	 *            x-coordinate for Bottom Left vertex
	 * @param vertBottomLeftY
	 *            y-coordinate for Bottom Left vertex
	 */
	public BoundingBox(double centerX, double centerY, double vertTopRightX, double vertTopRightY,
			double vertTopLeftX, double vertTopLeftY, double vertBottomRightX, double vertBottomRightY,
			double vertBottomLeftX, double vertBottomLeftY) {
		// Assumption: Y axis values increase downward, and X axis
		// values increase to the right.
		// TODO: Confident that from one axis, the other can be derived. So
		// perhaps just need center and
		// two vertices.
		rotates = true;
		axisLengthX = vertBottomRightX - vertBottomLeftX;
		axisLengthY = vertBottomRightY - vertBottomLeftY;
		axisHeightX = vertBottomLeftX - vertTopLeftX;
		axisHeightY = vertBottomLeftY - vertTopLeftY;

		double axisLengthMagnitude = Math.sqrt(axisLengthX * axisLengthX + axisLengthY * axisLengthY);
		axisLengthX = axisLengthX / axisLengthMagnitude;
		axisLengthY = axisLengthY / axisLengthMagnitude;
		extentLength = axisLengthMagnitude / 2;
		// TODO: remove debug assertion
		if (Math.abs(1 - Math.sqrt(axisLengthX * axisLengthX + axisLengthY * axisLengthY)) > .0001) {
			// TODO: Think of some debugging statements.
			Log.e("AppInventor", "Axis not properly transformed to unit length vectors!");
		}

		// axisHeightX = -1*axisLengthY;
		// axisHeightY = axisLengthX;
		double axisHeightMagnitude = Math.sqrt(axisHeightX * axisHeightX + axisHeightY * axisHeightY);
		axisHeightX = axisHeightX / axisHeightMagnitude;
		axisHeightY = axisHeightY / axisHeightMagnitude;
		extentHeight = axisHeightMagnitude / 2;
		// TODO: remove debug assertion
		if (Math.abs(1 - Math.sqrt(axisHeightX * axisHeightX + axisHeightY * axisHeightY)) > .0001) {
			// TODO: Think of some debugging statements.
		}

		this.centerX = centerX;
		this.centerY = centerY;
		this.vertTopRightX = vertTopRightX;
		this.vertTopRightY = vertTopRightY;
		this.vertTopLeftX = vertTopLeftX;
		this.vertTopLeftY = vertTopLeftY;
		this.vertBottomRightX = vertBottomRightX;
		this.vertBottomRightY = vertBottomRightY;
		this.vertBottomLeftX = vertBottomLeftX;
		this.vertBottomLeftY = vertBottomLeftY;
	}
	//END: Gabriel Modification. Rotation compatible constructor
	
	/**
	 * Determines whether this bounding box intersects with the passed bounding
	 * box and, if so, mutates the bounding box to be the intersection. This was
	 * designed to behave the same as
	 * {@link android.graphics.Rect#intersect(android.graphics.Rect)}.
	 *
	 * @param bb
	 *            bounding box to intersect with this bounding box
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

		// Mutate this bounding box to be the intersection before returning
		// true.
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
		return "<BoundingBox (left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom + ">";
	}
}
