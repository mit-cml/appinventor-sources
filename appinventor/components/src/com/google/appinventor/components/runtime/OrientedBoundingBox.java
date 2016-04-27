// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.BoundingBox;

/**
 * Bounding box abstraction, similar to {@link android.graphics.Rect}, but
 * compatible with rotation.
 *
 */
public class OrientedBoundingBox extends BoundingBox{
	private double left;
	private double top;
	private double right;
	private double bottom;
	
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
	public OrientedBoundingBox(double centerX, double centerY, double vertTopRightX, double vertTopRightY,
			double vertTopLeftX, double vertTopLeftY, double vertBottomRightX, double vertBottomRightY,
			double vertBottomLeftX, double vertBottomLeftY) {
		// Gabriel Assumption: Y axis values increase downward, and X axis
		// values increase to the right.
		// TODO: Confident that from one axis, the other can be derived. So
		// perhaps just need center and
		// two vertices.
		axisLengthX = vertBottomRightX - vertBottomLeftX;
		axisLengthY = vertBottomRightY - vertBottomLeftY;
		axisHeightX = vertBottomLeftX - vertTopLeftX;
		axisHeightY = vertBottomLeftY - vertTopLeftY;
		
		left = Math.min(vertTopLeftX, vertBottomLeftX);
		right = Math.max(vertTopRightX, vertBottomRightX);
		top = Math.min(vertTopLeftY, vertTopRightY);
		bottom = Math.max(vertBottomLeftY, vertBottomRightY);

		double axisLengthMagnitude = Math.sqrt(axisLengthX * axisLengthX + axisLengthY * axisLengthY);
		axisLengthX = axisLengthX / axisLengthMagnitude;
		axisLengthY = axisLengthY / axisLengthMagnitude;
		extentLength = axisLengthMagnitude / 2;
		// TODO: remove debug assertion
		if (Math.abs(1 - Math.sqrt(axisLengthX * axisLengthX + axisLengthY * axisLengthY)) > .0001) {
			// TODO: Think of some debugging statements.
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

	/**
	 * Gets the x-coordinate of the top left vertex.
	 *
	 * @return the x-coordinate for the top left vertex
	 */
	public double getTopLeftX() {

		return vertTopLeftX;
	}

	/**
	 * Gets the y-coordinate of the top left vertex.
	 *
	 * @return the y-coordinate for the top left vertex
	 */
	public double getTopLeftY() {

		return vertTopLeftY;
	}

	/**
	 * Gets the x-coordinate of the top right vertex.
	 *
	 * @return the x-coordinate for the top right vertex
	 */
	public double getTopRightX() {

		return vertTopRightX;
	}

	/**
	 * Gets the y-coordinate of the top right vertex.
	 *
	 * @return the y-coordinate for the top right vertex
	 */
	public double getTopRightY() {

		return vertTopRightY;
	}

	/**
	 * Gets the x-coordinate of the bottom left vertex.
	 *
	 * @return the x-coordinate for the bottom left vertex
	 */
	public double getBottomLeftX() {

		return vertBottomLeftX;
	}

	/**
	 * Gets the y-coordinate of the bottom left vertex.
	 *
	 * @return the y-coordinate for the bottom left vertex
	 */
	public double getBottomLeftY() {

		return vertBottomLeftY;
	}

	/**
	 * Gets the x-coordinate of the bottom right vertex.
	 *
	 * @return the x-coordinate for the bottom right vertex
	 */
	public double getBottomRightX() {

		return vertBottomRightX;
	}

	/**
	 * Gets the y-coordinate of the bottom right vertex.
	 *
	 * @return the y-coordinate for the bottom right vertex
	 */
	public double getBottomRightY() {

		return vertBottomRightY;
	}

	/**
	 * Determines whether this bounding box intersects with the passed oriented
	 * bounding box.
	 * 
	 * This is done by checking the other bounding box's vertices to see if they fall
	 * within this bounding box. If they do, they intersect.
	 * 
	 * @param bb
	 *            oriented bounding box to intersect with this oriented bounding
	 *            box
	 * @return {@code true} if they intersect, {@code false} otherwise
	 */
	/*
	@Override
	public boolean intersectDestructively(BoundingBox bb) {
		//TODO: Refactor this once I ask about the use of arrays/lists and iterators.
		// Go through each of the vertices of the other oriented bounding box
		// and check if they fall within the borders of this oriented bounding
		// box
		double currentX;
		double currentY;
		double currentLength;
		double currentHeight;

		currentX = bb.getBottomLeftX();
		currentY = bb.getBottomLeftY();
		// Shift the coordinates to this bounding box's reference frame.
		currentX -= centerX;
		currentY -= centerY;
		currentLength = currentX * axisLengthX + currentY * axisLengthY;
		currentHeight = currentX * axisHeightX + currentY * axisHeightY;

		// Check if shifted coordinates fall inside the bounding box
		if (Math.abs(currentLength) <= extentLength && Math.abs(currentHeight) <= extentHeight) {
			return true;
		}

		currentX = bb.getBottomRightX();
		currentY = bb.getBottomRightY();
		// Shift the coordinates to this bounding box's reference frame.
		currentX -= centerX;
		currentY -= centerY;
		currentLength = currentX * axisLengthX + currentY * axisLengthY;
		currentHeight = currentX * axisHeightX + currentY * axisHeightY;

		// Check if shifted coordinates fall inside the bounding box
		if (Math.abs(currentLength) <= extentLength && Math.abs(currentHeight) <= extentHeight) {
			return true;
		}

		currentX = bb.getTopLeftX();
		currentY = bb.getTopLeftY();
		// Shift the coordinates to this bounding box's reference frame.
		currentX -= centerX;
		currentY -= centerY;
		currentLength = currentX * axisLengthX + currentY * axisLengthY;
		currentHeight = currentX * axisHeightX + currentY * axisHeightY;

		// Check if shifted coordinates fall inside the bounding box
		if (Math.abs(currentLength) <= extentLength && Math.abs(currentHeight) <= extentHeight) {
			return true;
		}

		currentX = bb.getTopRightX();
		currentY = bb.getTopRightY();
		// Shift the coordinates to this bounding box's reference frame.
		currentX -= centerX;
		currentY -= centerY;
		currentLength = currentX * axisLengthX + currentY * axisLengthY;
		currentHeight = currentX * axisHeightX + currentY * axisHeightY;

		// Check if shifted coordinates fall inside the bounding box
		if (Math.abs(currentLength) <= extentLength && Math.abs(currentHeight) <= extentHeight) {
			return true;
		}

		return false;

	}
*/
	
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

	//TODO: Update this
	public String toString() {
		return "<OrientedBoundingBox (left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom
				+ ">";
	}
}
