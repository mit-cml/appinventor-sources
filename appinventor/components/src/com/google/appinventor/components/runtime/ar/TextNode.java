// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import android.util.Log;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;

// TODO: update the component version
@UsesAssets(fileNames = "plane.obj")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a text in an ARView3D. The text is positioned " +
        "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR, iconName = "images/textNode.png")
@SimpleObject
public final class TextNode extends ARNodeBase implements ARText {

  private static final String LOG_TAG = "TextNode";

  // Plane obj dimensions: treat as a 1x1 unit quad, width governs the collision footprint
  public static final float TEXT_OBJ_WIDTH = 1.0f;

  private String text = "";
  private float fontSizeInCentimeters = 6.0f;
  private float depthInCentimeters = 1.0f;
  private float width = 1.0f;
  private float height = 0.5f;
  private String objectModel = Form.ASSETS_PREFIX + "plane.obj";
  private String texture = "";
  private String font = "";

  public TextNode(final ARNodeContainer container) {
    super(container);
    Model(objectModel);
    Texture(texture);
    container.addNode(this);
  }

  // -------------------------------------------------------------------------
  // Text & Font Properties
  // -------------------------------------------------------------------------

  @Override
  @SimpleProperty(description = "Text to display by the TextNode. If this is " +
      "set to \"\", the TextNode will not be shown.")
  public String Text() { return text; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Text(String txt) {
    text = txt;
    Log.i(LOG_TAG, "Text set to: " + txt);
  }

  @SimpleProperty(description = "Font family.")
  public String Font() { return font; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Font(String f) {
    font = f;
    Log.i(LOG_TAG, "Font set to: " + f);
  }

  @Override
  @SimpleProperty(description = "The font size in centimeters. Values less than " +
      "zero will be treated as their absolute value. When set to zero, the TextNode " +
      "will not be shown.")
  public float FontSizeInCentimeters() { return fontSizeInCentimeters; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "6.0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FontSizeInCentimeters(float size) {
    fontSizeInCentimeters = Math.abs(size);
    Log.i(LOG_TAG, "FontSizeInCentimeters set to: " + fontSizeInCentimeters);
  }

  // -------------------------------------------------------------------------
  // Geometry Properties
  // -------------------------------------------------------------------------

  @Override
  @SimpleProperty(description = "How far, in centimeters, the TextNode extends along the z-axis. " +
      "Values less than zero will be treated as zero.")
  public float DepthInCentimeters() { return depthInCentimeters; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1.0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void DepthInCentimeters(float depth) {
    depthInCentimeters = Math.max(0f, depth);
    Log.i(LOG_TAG, "DepthInCentimeters set to: " + depthInCentimeters);
  }

  // -------------------------------------------------------------------------
  // Collision / Bounds — mirrors SphereNode pattern
  // -------------------------------------------------------------------------

  @Override
  public void updateCollisionShape() {
    // plane.obj is a unit quad — visual width = TEXT_OBJ_WIDTH * Scale()
    float visualWidth = TEXT_OBJ_WIDTH * Scale();
    // Use a box volume sized to the visible quad footprint
    collisionVolume = new BoxVolume(visualWidth, visualWidth * (height / width), depthInCentimeters / 100f);
    Log.i(LOG_TAG, "Collision shape updated — visual width: " + visualWidth + "m");
  }

  @Override
  public float[] getModelBounds() {
    float scaledWidth  = width  * Scale();
    float scaledHeight = height * Scale();
    float scaledDepth  = depthInCentimeters / 100f;
    return new float[]{ scaledWidth, scaledHeight, scaledDepth };
  }

  // -------------------------------------------------------------------------
  // Scaling
  // -------------------------------------------------------------------------

  @Override
  @SimpleFunction(description = "Changes the text's scale by the given scalar, " +
      "maintaining bottom position if physics is enabled.")
  public void ScaleBy(float scalar) {
    Log.i(LOG_TAG, "Scaling text " + name + " by " + scalar);

    float oldScale = Scale();
    float newScale = oldScale * Math.abs(scalar);

    if (EnablePhysics()) {
      // Adjust Y so the bottom of the node stays grounded
      float previousHalfHeight = (height * oldScale) / 2f;
      float newHalfHeight      = (height * newScale) / 2f;
      float[] currentPos = getCurrentPosition();
      currentPos[1] = currentPos[1] - previousHalfHeight + newHalfHeight;
      setCurrentPosition(currentPos);
    }

    Scale(newScale);
    updateCollisionShape();
    Log.i(LOG_TAG, "Scale complete: " + oldScale + " → " + newScale);
  }

  // -------------------------------------------------------------------------
  // Debug Helpers — mirrors SphereNode pattern
  // -------------------------------------------------------------------------

  @SimpleFunction(description = "Logs the current collision shape dimensions for debugging.")
  public void DebugCollisionShape() {
    float visualWidth = TEXT_OBJ_WIDTH * Scale();
    Log.i(LOG_TAG, "=== COLLISION SHAPE DEBUG ===");
    Log.i(LOG_TAG, "Text obj width:    " + TEXT_OBJ_WIDTH + "m (unit)");
    Log.i(LOG_TAG, "Scale:             " + Scale());
    Log.i(LOG_TAG, "Visual width:      " + visualWidth + "m");
    Log.i(LOG_TAG, "Depth:             " + depthInCentimeters + "cm");
    Log.i(LOG_TAG, "Has physics:       " + EnablePhysics());
    Log.i(LOG_TAG, "=============================");
  }

  @SimpleFunction(description = "Logs the current physics state for debugging.")
  public void DebugPhysicsState() {
    float[] pos = getCurrentPosition();
    Log.i(LOG_TAG, "=== PHYSICS STATE DEBUG ===");
    Log.i(LOG_TAG, "Position:          " + arrayToString(pos));
    Log.i(LOG_TAG, "Has physics:       " + EnablePhysics());
    Log.i(LOG_TAG, "Mass:              " + Mass());
    Log.i(LOG_TAG, "Scale:             " + Scale());
    Log.i(LOG_TAG, "Static Friction:   " + StaticFriction());
    Log.i(LOG_TAG, "Dynamic Friction:  " + DynamicFriction());
    Log.i(LOG_TAG, "Restitution:       " + Restitution());
    Log.i(LOG_TAG, "Drag Sensitivity:  " + DragSensitivity());
    Log.i(LOG_TAG, "Font size (cm):    " + fontSizeInCentimeters);
    Log.i(LOG_TAG, "Depth (cm):        " + depthInCentimeters);
    Log.i(LOG_TAG, "===========================");
  }

  // -------------------------------------------------------------------------
  // Deferred / future properties (not yet implemented)
  //
  // TODO: WrapText       — controls whether text wraps within the node width
  // TODO: TextAlignment  — ALIGNMENT_NORMAL / CENTER / OPPOSITE
  // TODO: Truncation     — how overflowing text is truncated
  // TODO: CornerRadius   — rounded corners on the backing plane
  // -------------------------------------------------------------------------
}