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
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;


// TODO: either supply a simple quad or make one
@UsesAssets(fileNames = "cube.obj, Palette.png")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a plane in an ARView3D.  The plane is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject
  public final class PlaneNode extends ARNodeBase implements ARPlane {

  private float[] fromPropertyPosition = {0f,0f,0f};
  private Anchor anchor = null;
  private Trackable trackable = null;
  // TODO: either supply a simple quad or make one
  private String objectModel = Form.ASSETS_PREFIX + "plane.obj";
  private String texture = Form.ASSETS_PREFIX + "Palette.png";
  private float scale = 1.0f;

  public PlaneNode(final ARNodeContainer container) {
    super(container);
    // Additional updates
    container.addNode(this);
  }

  @Override // wht is the significance?
  public Anchor Anchor() { return this.anchor; }

  @Override
  public void Anchor(Anchor a) { this.anchor = a;}

  @Override
  public Trackable Trackable() { return this.trackable; }

  @Override
  public void Trackable(Trackable t) { this.trackable = t;}

  @Override
  @SimpleProperty(description = "The 3D model file to be loaded.",
      category = PropertyCategory.APPEARANCE)
  public String Model() { return this.objectModel; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  public void Model(String model) {this.objectModel = model;}

  @Override
  @SimpleFunction(description = "move a capsule node properties at the " +
      "specified (x,y,z) position.")
  public void MoveBy(float x, float y, float z){

    float[] position = { 0, 0, 0};
    float[] rotation = {0, 0, 0, 1};

    //float[] currentAnchorPoseRotation = rotation;

    if (this.Anchor() != null) {
      float[] translations = this.Anchor().getPose().getTranslation();
      position = new float[]{translations[0] + x, translations[1] + y, translations[2] + z};
      //currentAnchorPoseRotation = Anchor().getPose().getRotationQuaternion(); or getTranslation() not working yet
    }

    Pose newPose = new Pose(position, rotation);
    if (this.trackable != null){
      Anchor(this.trackable.createAnchor(newPose));
      Log.i("capsule","moved anchor BY " + newPose+ " with rotaytion "+rotation);
    }else {
      Log.i("capsule", "tried to move anchor BY pose");
    }
  }


  @Override
  @SimpleFunction(description = "Changes the node's position by (x,y,z).")
  public void MoveTo(float x, float y, float z) {
    float[] position = {x, y, z};
    float[] rotation = {0, 0, 0, 1};

    float[] currentAnchorPoseRotation = rotation;
    if (this.Anchor() != null) {
      //currentAnchorPoseRotation = Anchor().getPose().getRotationQuaternion(); or getTranslation() not working yet
    }
    Pose newPose = new Pose(position, rotation);
    if (this.trackable != null){
      Anchor(this.trackable.createAnchor(newPose));
      Log.i("webview","moved anchor to pose: " + newPose+ " with rotaytion "+currentAnchorPoseRotation);
    }else {
      Log.i("webview", "tried to move anchor to pose");
    }
  }


  @Override
  @SimpleFunction(description = "move a sphere node properties at the " +
      "specified (x,y,z) position.")
  public void MoveToDetectedPlane(ARDetectedPlane targetPlane, Object p) {
    this.trackable = (Trackable) targetPlane.DetectedPlane();
    if (this.anchor != null) {
      this.anchor.detach();
    }
    Anchor(this.trackable.createAnchor((Pose) p));
    Log.i("created Anchor!", " " );
  }

  @SimpleProperty(description = "Set the current pose of the object",
      category = PropertyCategory.APPEARANCE)
  @Override
  public void Pose(Object p) {
    Log.i("setting Capsule pose", "with " +p);
    Pose pose = (Pose) p;

    float[] position = {pose.tx(), pose.ty(), pose.tz()};
    float[] rotation = {pose.qx(), pose.qy(), pose.qz(), 1};
    if (this.trackable != null) {
      Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    }
  }

  /* we need this b/c if the anchor isn't yet trackable, we can't create an anchor. therefore, we need to store the position as a float */
  @Override
  public float[] PoseFromPropertyPosition(){ return fromPropertyPosition; }


  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "Set the current pose of the object from property. Format is a comma-separated list of 3 coordinates: x, y, z such that 0, 0, 1 places the object at x of 0, y of 0 and z of 1",
      category = PropertyCategory.APPEARANCE)
  @Override
  public void PoseFromPropertyPosition(String positionFromProperty) {
    String[] positionArray = positionFromProperty.split(",");
    float[] position = {0f,0f,0f};

    for (int i = 0; i < positionArray.length; i++) {
      position[i] = Float.parseFloat(positionArray[i]);
    }
    this.fromPropertyPosition = position;
    float[] rotation = {0f,0f,0f, 1f}; // no rotation rn TBD
    if (this.trackable != null) {
      Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    }
    Log.i("store sphere pose", "with position" +positionFromProperty);
  }


  @Override
  public float Scale() { return this.scale; }

  @Override
  public void Scale(float t) { this.scale = t;}

  @Override
  @SimpleProperty(description = "How far, in centimeters, the PlaneNode extends along the x-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the PlaneNode " +
    "will not be shown.")
  public float WidthInCentimeters() { return 0.5f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "6")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void WidthInCentimeters(float widthInCentimeters) {}

  @Override
  @SimpleProperty(description = "How far, in centimeters, the PlaneNode extends along the y-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the PlaneNode " +
    "will not be shown.")
  public float HeightInCentimeters() { return 0.5f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "2")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void HeightInCentimeters(float heightInCentimeters) {}

  @Override
  @SimpleProperty(description = "This determines how rounded the boxes corners will be.  " +
    "A value of zero specifies no rounded corners, and a value of half the height or " +
    "width of the PlaneNode (whichever is greater) makes it fully rounded, with no " +
    "straight edges.  Values less than zero will be treated as zero.")
  public float CornerRadius() { return 0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void CornerRadius(float cornerRadius) {}


  @Override
  @SimpleProperty(userVisible = false)
  public String Texture() { return this.texture; }

  @Override
  @SimpleProperty(userVisible = false)
  public void Texture(String texture) {}

}
