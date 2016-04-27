// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
//Gabriel Modifications
import android.graphics.Matrix;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.BoundingBox;

/**
 * Simple image-based Sprite.
 *
 */
@DesignerComponent(version = YaVersion.IMAGESPRITE_COMPONENT_VERSION, description = "<p>A 'sprite' that can be placed on a "
		+ "<code>Canvas</code>, where it can react to touches and drags, "
		+ "interact with other sprites (<code>Ball</code>s and other "
		+ "<code>ImageSprite</code>s) and the edge of the Canvas, and move "
		+ "according to its property values.  Its appearance is that of the "
		+ "image specified in its <code>Picture</code> property (unless its "
		+ "<code>Visible</code> property is <code>False</code>.</p> "
		+ "<p>To have an <code>ImageSprite</code> move 10 pixels to the left "
		+ "every 1000 milliseconds (one second), for example, "
		+ "you would set the <code>Speed</code> property to 10 [pixels], the "
		+ "<code>Interval</code> property to 1000 [milliseconds], the "
		+ "<code>Heading</code> property to 180 [degrees], and the "
		+ "<code>Enabled</code> property to <code>True</code>.  A sprite whose "
		+ "<code>Rotates</code> property is <code>True</code> will rotate its "
		+ "image as the sprite's <code>Heading</code> changes.  Checking for collisions "
		+ "with a rotated sprite currently checks the sprite's unrotated position "
		+ "so that collision checking will be inaccurate for tall narrow or short "
		+ "wide sprites that are rotated.  Any of the sprite properties "
		+ "can be changed at any time under program control.</p> ", category = ComponentCategory.ANIMATION)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public class ImageSprite extends Sprite {
	private final Form form;
	private BitmapDrawable drawable;
	private int widthHint = LENGTH_PREFERRED;
	private int heightHint = LENGTH_PREFERRED;
	private String picturePath = ""; // Picture property
	private boolean rotates;
	// Gabriel Modification
	// Vertices
	float[] vTopLeft = new float[2];
	float[] vBottomLeft = new float[2];
	float[] vTopRight = new float[2];
	float[] vBottomRight = new float[2];
	// Center of rotation offset as percentage of image dimensions.
	// Currently limited to values [0-100].
	double xRotationOffsetPercent = 50.0;
	double yRotationOffsetPercent = 50.0;

	/**
	 * Constructor for ImageSprite.
	 *
	 * @param container
	 */
	public ImageSprite(ComponentContainer container) {
		super(container);
		form = container.$form();
		rotates = true;
	}

	/**
	 * This method uses getWidth and getHeight directly from the bitmap, so we
	 * apply corrections for density for coordinates and size.
	 * 
	 * @param canvas
	 *            the canvas on which to draw
	 */
	public void onDraw(android.graphics.Canvas canvas) {
		if (drawable != null && visible) {
			int xinit = (int) (Math.round(xLeft) * form.deviceDensity());
			int yinit = (int) (Math.round(yTop) * form.deviceDensity());
			int w = (int) (Width() * form.deviceDensity());
			int h = (int) (Height() * form.deviceDensity());
			drawable.setBounds(xinit, yinit, xinit + w, yinit + h);
			// If the sprite doesn't rotate, just draw the drawable
			// within the bounds of the sprite rectangle
			if (!rotates) {
				drawable.draw(canvas);
			} else {
				// if the sprite does rotate, draw the sprite on the canvas
				// that has been rotated in the opposite direction
				// Still within those same image bounds.
				canvas.save();
				// rotate the canvas for drawing. This pivot point of the
				// rotation will be the center of the sprite
				// TODO: Remove this hard-coded testing implementation once UI
				// elements
				// for editing the center offset are in place.
				if (picturePath.equals("stickfigure_small.jpg")) {
					yRotationOffsetPercent = 20.0;
					Log.e("AppInventor", "Center of Rotation Changed!");
				}
				canvas.rotate((float) (-Heading()), (float) (xinit + w * (xRotationOffsetPercent / 100.0)),
						(float) (yinit + h * (yRotationOffsetPercent / 100.0)));
				drawable.draw(canvas);
				canvas.restore();
			}
		}
	}

	/**
	 * Updates the x- and y-coordinates based on the heading and speed. The
	 * caller is responsible for calling {@link #registerChange()}.
	 */
	// Gabriel Modification. Added the call to updateVertices().
	@Override
	protected void updateCoordinates() {
		xLeft += speed * headingCos;
		yTop += speed * headingSin;
		updateVertices();
	}

	/**
	 * Updates the coordinates for the vertices of an image sprite based on the
	 * new xLeft and yTop values.
	 */
	private void updateVertices() {
		// Calculate vertices according to degrees of rotation (heading)
		float currentHeading = (float) Heading();
		float rotationHeading = (float) Heading() * -1; // Transform to the same
														// reference frame used
														// by the Matrix methods
		float currentHeight = (float) Height();
		float currentWidth = (float) Width();
		Matrix m = new Matrix();
		float[] vertTopLeft;
		float[] vertBottomLeft;
		float[] vertTopRight;
		float[] vertBottomRight;
		// TODO: Figure out if there is a more systemic approach for determining
		// cut-off degree values.
		// Idea: Base cut off on the heading values where diagonals are parallel
		// to x-axis.
		if (currentHeading >= -45 && currentHeading < 45) {
			vertTopLeft = new float[] { (float) xLeft, (float) yTop };
			vertBottomLeft = new float[] { (float) xLeft, (float) (yTop + currentHeight) };
			vertTopRight = new float[] { (float) (xLeft + currentWidth), (float) yTop };
			vertBottomRight = new float[] { (float) (xLeft + currentWidth), (float) (yTop + currentHeight) };
			m.setRotate(rotationHeading, (float) (xLeft + currentWidth / 2), (float) (yTop + currentHeight / 2));
			m.mapPoints(vertTopLeft);
			m.mapPoints(vertBottomLeft);
			m.mapPoints(vertTopRight);
			m.mapPoints(vertBottomRight);
		} else if (currentHeading >= 45 && currentHeading < 135) {
			vertTopLeft = new float[] { (float) (xLeft + currentWidth), (float) yTop };
			vertBottomLeft = new float[] { (float) xLeft, (float) yTop };
			vertTopRight = new float[] { (float) (xLeft + currentWidth), (float) (yTop + currentHeight) };
			vertBottomRight = new float[] { (float) xLeft, (float) (yTop + currentHeight) };
			m.setRotate(rotationHeading, (float) (xLeft + currentWidth / 2), (float) (yTop + currentHeight / 2));
			m.mapPoints(vertTopLeft);
			m.mapPoints(vertBottomLeft);
			m.mapPoints(vertTopRight);
			m.mapPoints(vertBottomRight);
		} else if (currentHeading >= 135) {
			vertTopLeft = new float[] { (float) (xLeft + currentWidth), (float) (yTop + currentHeight) };
			vertBottomLeft = new float[] { (float) (xLeft + currentWidth), (float) yTop };
			vertTopRight = new float[] { (float) xLeft, (float) (yTop + currentHeight) };
			vertBottomRight = new float[] { (float) xLeft, (float) yTop };
			m.setRotate(rotationHeading, (float) (xLeft + currentWidth / 2), (float) (yTop + currentHeight / 2));
			m.mapPoints(vertTopLeft);
			m.mapPoints(vertBottomLeft);
			m.mapPoints(vertTopRight);
			m.mapPoints(vertBottomRight);
		} else if (currentHeading >= -135 && currentHeading < -45) {
			vertTopLeft = new float[] { (float) xLeft, (float) (yTop + currentHeight) };
			vertBottomLeft = new float[] { (float) (xLeft + currentWidth), (float) (yTop + currentHeight) };
			vertTopRight = new float[] { (float) xLeft, (float) yTop };
			vertBottomRight = new float[] { (float) (xLeft + currentWidth), (float) yTop };
			m.setRotate(rotationHeading, (float) (xLeft + currentWidth / 2), (float) (yTop + currentHeight / 2));
			m.mapPoints(vertTopLeft);
			m.mapPoints(vertBottomLeft);
			m.mapPoints(vertTopRight);
			m.mapPoints(vertBottomRight);
		} else {
			vertTopLeft = new float[] { (float) (xLeft + currentWidth), (float) (yTop + currentHeight) };
			vertBottomLeft = new float[] { (float) (xLeft + currentWidth), (float) yTop };
			vertTopRight = new float[] { (float) xLeft, (float) (yTop + currentHeight) };
			vertBottomRight = new float[] { (float) xLeft, (float) yTop };
			m.setRotate(rotationHeading, (float) (xLeft + currentWidth / 2), (float) (yTop + currentHeight / 2));
			m.mapPoints(vertTopLeft);
			m.mapPoints(vertBottomLeft);
			m.mapPoints(vertTopRight);
			m.mapPoints(vertBottomRight);
		}
		this.vBottomLeft = vertBottomLeft;
		this.vBottomRight = vertBottomRight;
		this.vTopLeft = vertTopLeft;
		this.vTopRight = vertTopRight;

		Log.e("AppInventor", "Heading: " + Double.toString(Heading()));
		Log.e("AppInventor", "Picture Path: " + picturePath);
		// checkVertexInvariants();
	}

	/**
	 * Moves the sprite back in bounds if part of it extends out of bounds,
	 * having no effect otherwise. If the sprite is too wide to fit on the
	 * canvas, this aligns the left side of the sprite with the left side of the
	 * canvas. If the sprite is too tall to fit on the canvas, this aligns the
	 * top side of the sprite with the top side of the canvas.
	 */
	@Override
	protected void moveIntoBounds(int canvasWidth, int canvasHeight) {
		//TODO: Remove debugging code.
		//BoundingBox bb = getBoundingBox(0);
		boolean moved = false;

		// We set the xLeft and/or yTop fields directly, instead of calling
		// X(123) and Y(123), to avoid
		// having multiple calls to registerChange.

		// Check if the sprite is too wide to fit on the canvas.
		if (Width() > canvasWidth) {
			// Sprite is too wide to fit. If it isn't already at the left edge,
			// move it there.
			// It is important not to set moved to true if xLeft is already 0.
			// Doing so can cause a stack
			// overflow.
			if (xLeft != 0) {
				xLeft = 0;
				moved = true;
			}
		} else if (overWestEdge()) {
			// Gabriel Modifications
			// Move the sprite to the right by however much its gone outside
			// the canvas plus a small padding
			xLeft += Math.abs(Math.min(this.vTopLeft[0], this.vBottomLeft[0])) + 10;
			moved = true;
		} else if (overEastEdge(canvasWidth)) {
			// Gabriel Modifications
			// Move the sprite to the left by however much its gone outside
			// the canvas plus a small padding
			xLeft -= (Math.max(this.vBottomRight[0], this.vTopRight[0]) - canvasWidth) + 30;
			moved = true;
		}

		// Check if the sprite is too tall to fit on the canvas. We don't want
		// to cause a stack
		// overflow by moving the sprite to the top edge and then to the bottom
		// edge, repeatedly.
		if (Height() > canvasHeight) {
			// Sprite is too tall to fit. If it isn't already at the top edge,
			// move it there.
			// It is important not to set moved to true if yTop is already 0.
			// Doing so can cause a stack
			// overflow.
			if (yTop != 0) {
				yTop = 0;
				moved = true;
			}
		} else if (overNorthEdge()) {
			// Gabriel Modifications
			// Move the sprite down by however much its gone outside
			// the canvas plus a small padding
			yTop += Math.abs(Math.min(this.vTopLeft[1], this.vTopRight[1])) + 10;
			moved = true;
		} else if (overSouthEdge(canvasHeight)) {
			// Gabriel Modifications
			// Move the sprite up by however much its gone outside
			// the canvas plus a small padding
			yTop -= (Math.max(this.vBottomLeft[1], this.vBottomRight[1]) - canvasHeight) + 10;
			moved = true;
		}

		// Then, call registerChange (just once!) if necessary.
		if (moved) {
			// Gabriel Modifications
			updateVertices();
			// TODO: Remove debugging measures.
			Log.e("AppInventor",
					"TL: " + Float.toString(vTopLeft[0]) + "," + Float.toString(vTopLeft[1]) + " BL: "
							+ Float.toString(vBottomLeft[0]) + "," + Float.toString(vBottomLeft[1]) + " TR: "
							+ Float.toString(vTopRight[0]) + "," + Float.toString(vTopRight[1]) + " BR: "
							+ Float.toString(vBottomRight[0]) + "," + Float.toString(vBottomRight[1]));
			registerChange();
		}
	}

	// Code for detection of edge collision.
	@Override
	protected boolean overWestEdge() {

		// Check vertices according to degrees of rotation (heading)
		// TODO: Remove debugging measures.
		if (vTopLeft[0] <= 0 || vBottomLeft[0] <= 0 || vTopRight[0] <= 0 || vBottomRight[0] <= 0) {
			Log.e("AppInventor", "Hit West Edge Detected on ImageSprite");
			Log.e("AppInventor",
					"TL: " + Float.toString(vTopLeft[0]) + "," + Float.toString(vTopLeft[1]) + " BL: "
							+ Float.toString(vBottomLeft[0]) + "," + Float.toString(vBottomLeft[1]) + " TR: "
							+ Float.toString(vTopRight[0]) + "," + Float.toString(vTopRight[1]) + " BR: "
							+ Float.toString(vBottomRight[0]) + "," + Float.toString(vBottomRight[1]));
		}
		// TODO: only should have to check vTopLeft and vBottomLeft.
		return vTopLeft[0] <= 0 || vBottomLeft[0] <= 0 || vTopRight[0] <= 0 || vBottomRight[0] <= 0;
	}

	@Override
	protected boolean overEastEdge(int canvasWidth) {

		// Check vertices according to degrees of rotation (heading)
		// TODO: Remove debugging measures.
		if (vTopLeft[0] >= canvasWidth || vBottomLeft[0] >= canvasWidth || vTopRight[0] >= canvasWidth
				|| vBottomRight[0] >= canvasWidth) {
			Log.e("AppInventor", "Hit East Edge Detected on ImageSprite");
			Log.e("AppInventor",
					"TL: " + Float.toString(vTopLeft[0]) + "," + Float.toString(vTopLeft[1]) + " BL: "
							+ Float.toString(vBottomLeft[0]) + "," + Float.toString(vBottomLeft[1]) + " TR: "
							+ Float.toString(vTopRight[0]) + "," + Float.toString(vTopRight[1]) + " BR: "
							+ Float.toString(vBottomRight[0]) + "," + Float.toString(vBottomRight[1]));
		}
		// TODO: only should have to check vTopRight and vBottomRight.
		return vTopLeft[0] >= canvasWidth || vBottomLeft[0] >= canvasWidth || vTopRight[0] >= canvasWidth
				|| vBottomRight[0] >= canvasWidth;
	}

	@Override
	protected boolean overNorthEdge() {

		// Check vertices according to degrees of rotation (heading)
		// TODO: Remove debugging measures.
		if (vTopLeft[1] <= 0 || vTopRight[1] <= 0) {
			Log.e("AppInventor", "Hit North Edge Detected on ImageSprite");
			Log.e("AppInventor",
					"TL: " + Float.toString(vTopLeft[0]) + "," + Float.toString(vTopLeft[1]) + " BL: "
							+ Float.toString(vBottomLeft[0]) + "," + Float.toString(vBottomLeft[1]) + " TR: "
							+ Float.toString(vTopRight[0]) + "," + Float.toString(vTopRight[1]) + " BR: "
							+ Float.toString(vBottomRight[0]) + "," + Float.toString(vBottomRight[1]));
		}
		return vTopLeft[1] <= 0 || vTopRight[1] <= 0;
	}

	protected boolean overSouthEdge(int canvasHeight) {
		// TODO: Remove debugging measures.
		if (vBottomLeft[1] >= canvasHeight || vBottomRight[1] >= canvasHeight) {
			Log.e("AppInventor", "Hit North Edge Detected on ImageSprite");
			Log.e("AppInventor",
					"TL: " + Float.toString(vTopLeft[0]) + "," + Float.toString(vTopLeft[1]) + " BL: "
							+ Float.toString(vBottomLeft[0]) + "," + Float.toString(vBottomLeft[1]) + " TR: "
							+ Float.toString(vTopRight[0]) + "," + Float.toString(vTopRight[1]) + " BR: "
							+ Float.toString(vBottomRight[0]) + "," + Float.toString(vBottomRight[1]));
		}

		return vBottomLeft[1] >= canvasHeight || vBottomRight[1] >= canvasHeight;
	}

	/**
	 * Provides the bounding box for this sprite. Modifying the returned value
	 * does not affect the sprite.
	 *
	 * @param border
	 *            the number of pixels outside the sprite to include in the
	 *            bounding box
	 * @return the bounding box for this sprite
	 */
	/*
	@Override
	public BoundingBox getBoundingBox(int border) {
		Log.e("AppInventor", "Getting bounding box as image sprite.");
		return new BoundingBox((xLeft + Width() / 2), (yTop + Height() / 2),
				(double)vTopRight[0]+border-1, (double)vTopRight[1]-border, 
				(double)vTopLeft[0] - border, (double)vTopLeft[1] - border,
				(double)vBottomRight[0] - 1 + border, (double)vBottomRight[1] - 1 + border,
				(double)vBottomLeft[0] - border, (double)vBottomLeft[1] - 1 + border);
	}
*/
	/**
	 * Checks that the spatial relation invariants between the vertices are
	 * upheld.
	 */
	private void checkVertexInvariants() {
		double degreesD1 = Math.toDegrees(Math.atan(Width() / Height()));
		double currentHeading = Heading();
		try {
			if (currentHeading >= degreesD1 && currentHeading < 180 - (2 * degreesD1)) {
				if (vTopLeft[0] > vTopRight[0]) {
					throw new RuntimeException("Top left vertex X value to the right of Top Right X value");
				} else if (vTopLeft[0] > vBottomRight[0]) {
					throw new RuntimeException("Top left vertex X value to the right of Bottom Right X value");
				} else if (vTopLeft[1] > vBottomLeft[1]) {
					throw new RuntimeException("Top left vertex Y value above Bottom Left Y value");
				} else if (vBottomLeft[0] > vTopRight[0]) {
					throw new RuntimeException("Bottom left vertex X value to the right of Top Right X value");
				} else if (vBottomLeft[0] > vBottomRight[0]) {
					throw new RuntimeException("Bottom left vertex X value to the right of Bottom Right X value");
				} else if (vBottomRight[1] < vTopRight[1]) {
					throw new RuntimeException("Bottom right vertex Y value above Top Right Y value");
				}
			}
		} catch (RuntimeException e) {
			Log.e("AppInventor",
					"TL: " + Float.toString(vTopLeft[0]) + "," + Float.toString(vTopLeft[1]) + " BL: "
							+ Float.toString(vBottomLeft[0]) + "," + Float.toString(vBottomLeft[1]) + " TR: "
							+ Float.toString(vTopRight[0]) + "," + Float.toString(vTopRight[1]) + " BR: "
							+ Float.toString(vBottomRight[0]) + "," + Float.toString(vBottomRight[1]));
			throw e;
		}
	}

	/**
	 * Returns the path of the sprite's picture
	 *
	 * @return the path of the sprite's picture
	 */
	@SimpleProperty(description = "The picture that determines the sprite's appearence", category = PropertyCategory.APPEARANCE)
	public String Picture() {
		return picturePath;
	}

	/**
	 * Specifies the path of the sprite's picture
	 *
	 * <p/>
	 * See {@link MediaUtil#determineMediaSource} for information about what a
	 * path can be.
	 *
	 * @param path
	 *            the path of the sprite's picture
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
	@SimpleProperty
	public void Picture(String path) {
		picturePath = (path == null) ? "" : path;
		try {
			drawable = MediaUtil.getBitmapDrawable(form, picturePath);
		} catch (IOException ioe) {
			Log.e("ImageSprite", "Unable to load " + picturePath);
			drawable = null;
		}
		// note: drawable can be null!
		registerChange();
	}

	// The actual width/height of an ImageSprite whose Width/Height property is
	// set to Automatic or
	// Fill Parent will be the width/height of the image.

	@Override
	@SimpleProperty
	public int Height() {
		if (heightHint == LENGTH_PREFERRED || heightHint == LENGTH_FILL_PARENT || heightHint <= LENGTH_PERCENT_TAG) {
			// Drawable.getIntrinsicWidth/Height gives weird values, but
			// Bitmap.getWidth/Height works.
			return drawable == null ? 0 : (int) (drawable.getBitmap().getHeight() / form.deviceDensity());
		}
		return heightHint;
	}

	@Override
	@SimpleProperty
	public void Height(int height) {
		heightHint = height;
		registerChange();
	}

	@Override
	public void HeightPercent(int pCent) {
		// Ignore
	}

	@Override
	@SimpleProperty
	public int Width() {
		if (widthHint == LENGTH_PREFERRED || widthHint == LENGTH_FILL_PARENT || widthHint <= LENGTH_PERCENT_TAG) {
			// Drawable.getIntrinsicWidth/Height gives weird values, but
			// Bitmap.getWidth/Height works.
			return drawable == null ? 0 : (int) (drawable.getBitmap().getWidth() / form.deviceDensity());
		}
		return widthHint;
	}

	@Override
	@SimpleProperty
	public void Width(int width) {
		widthHint = width;
		registerChange();
	}

	@Override
	public void WidthPercent(int pCent) {
		// Ignore
	}

	/**
	 * Rotates property getter method.
	 *
	 * @return {@code true} indicates that the image rotates to match the
	 *         sprite's heading {@code false} indicates that the sprite image
	 *         doesn't rotate.
	 */
	@SimpleProperty(description = "If true, the sprite image rotates to match the sprite's heading. "
			+ "If false, the sprite image does not rotate when the sprite changes heading. "
			+ "The sprite rotates around its centerpoint.", category = PropertyCategory.BEHAVIOR)
	public boolean Rotates() {
		return rotates;
	}

	/**
	 * Rotates property setter method
	 *
	 * @param rotates
	 *            {@code true} indicates that the image rotates to match the
	 *            sprite's heading {@code false} indicates that the sprite image
	 *            doesn't rotate.
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
	@SimpleProperty
	public void Rotates(boolean rotates) {
		this.rotates = rotates;
		registerChange();
	}
}
