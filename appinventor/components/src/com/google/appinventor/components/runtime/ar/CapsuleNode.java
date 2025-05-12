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
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import org.json.JSONObject;
import java.util.Locale;

// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a capsule in an ARView3D.  The capsule is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

@UsesAssets(fileNames = "pawn.obj, Palette.png")
  public final class CapsuleNode extends ARNodeBase implements ARCapsule {


  private Anchor anchor = null;
  private Trackable trackable = null;
  private String texture = "";
  private String objectModel = Form.ASSETS_PREFIX + "pawn.obj";
  private float scale = 1.0f;

    public CapsuleNode(final ARNodeContainer container) {
      super(container);
      //parentSession = session;
      container.addNode(this);
    }

    @Override
    public Anchor Anchor() { return this.anchor; }

    @Override
    public void Anchor(Anchor a) { this.anchor = a;}

  @SimpleProperty(description = "Get the current pose of the object",
      category = PropertyCategory.APPEARANCE)
  @Override
  public Object Pose() {
      Pose p = (Pose)this.anchor.getPose();
      return p;
    }


  @SimpleProperty(description = "Get the current pose of the object",
      category = PropertyCategory.APPEARANCE)
  public Object PoseToJson() {
      Pose p = this.anchor.getPose();
      Locale locale = Locale.ENGLISH;

    JSONObject innerObject = new JSONObject();
    JSONObject saveObject = new JSONObject();
    innerObject.put("x", p.tx());
    innerObject.put("y", p.ty());
    innerObject.put("z", p.tz());
    saveObject.put("t", innerObject);
    innerObject.put("x", p.qx());
    innerObject.put("y", p.qy());
    innerObject.put("z", p.qz());
    innerObject.put("w", p.qw());
    saveObject.put("q", innerObject);

      //Object[] var2 = new Object[]{p.tx(), p.ty(), p.tz(), p.qx(), p.qy(), p.qz(), p.qw()};
      //String result = String.format(locale, "{\"t\":{\"x\":%.3f, \"y\":%.3f, \"z\":%.3f}, \"q\":{\"x\":%.2f, \"y\":%.2f, \"z\":%.2f, \"w\":%.2f}}", var2);
      Log.i("exporting Capsule pose as JSON", "with " +saveObject);
      return saveObject;

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


  @SimpleProperty(description = "Serialize the capsule node to json",
      category = PropertyCategory.APPEARANCE)
  public Object CapsuleNodeToJson() {
    Pose p = this.anchor.getPose();
    Locale locale = Locale.ENGLISH;

    JSONObject saveObject = new JSONObject();
    saveObject.put("model", this.Model());
    saveObject.put("texture", this.Texture());
    saveObject.put("scale", this.Scale());
    saveObject.put("pose", this.PoseToJson());
    saveObject.put("type", "capsule");

    Object[] var2 = new Object[]{this.Model(), this.Texture(), this.Scale(), this.PoseToJson(), "capsule"};
    String result = String.format(locale, "{\"model\":\"%s\", \"texture\":\"%s\", \"scale\":%f}, \"pose\": %s, \"type\": \"%s\"}", var2);
    Log.i("exporting Capsule pose as JSON", "with " + " " + saveObject);
    return saveObject;

  }

    @Override
    public Trackable Trackable() { return this.trackable; }

    @Override
    public void Trackable(Trackable t) { this.trackable = t;}

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1")
    @SimpleProperty(description = "The scale of the node.  This is used to multiply its " +
            "sizing properties.  Values less than zero will be treated as their absolute value.")
   public float Scale() {
        return this.scale;
    }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
    public void Scale(float s) {
        this.scale = s;}


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
    public void MoveTo(float x, float y, float z){
      float[] position = {x, y, z};
      float[] rotation = {0, 0, 0, 1};

      float[] currentAnchorPoseRotation = rotation;
      if (this.Anchor() != null) {
        //currentAnchorPoseRotation = Anchor().getPose().getRotationQuaternion(); not working yet
      }
      Pose newPose = new Pose(position, rotation);
      if (this.trackable != null){
        Anchor(this.trackable.createAnchor(newPose));
        Log.i("capsule","moved anchor to pose: " + newPose+ " with rotaytion "+currentAnchorPoseRotation);
      }else {
        Log.i("capsule", "tried to move anchor to pose");
      }
    }

    @Override
    @SimpleFunction(description = "move a capsule node properties to detectedplane.")
    public void MoveToDetectedPlane(ARDetectedPlane targetPlane, Object p) {
        this.trackable = (Trackable) targetPlane.DetectedPlane();
        if (this.anchor != null) {
          this.anchor.detach();
        }
        Anchor(this.trackable.createAnchor((Pose) p));
        Log.i("created Capsule Anchor!", " ");
    }


  @SimpleFunction(description = "move a capsule node properties to detectedplane.")
  public void MoveToPose(String p) {

    Pose pose = ARUtils.parsePoseObject(p);
    if (this.anchor != null) {
      this.anchor.detach();
    }
    if (this.trackable != null) {
      Anchor(this.trackable.createAnchor(pose));
      Log.i("created Capsule Anchor!", " ");
    } else {
      Log.e("could not Capsule Anchor, no trackable found", " ");
    }

    Log.i("created Capsule Anchor!", " ");
  }


    @Override
    @SimpleProperty(description = "How far, in centimeters, the CapsuleNode extends along the y-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero or when set to " +
      "less than double the CapRadius, the CapsuleNode will not appear.")
    public float HeightInCentimeters() { return 0.5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "7")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void HeightInCentimeters(float heightInCentimeters) {}

    @Override
    @SimpleProperty(description = "The radius, in centimeters, of two hemispheres " +
      "or caps at the ends of a CapsuleNode.  Values less than zero will be treated as their " +
      "absolute values.  When set to zero or when set to greater than half of the Height, the " +
      "CapsuleNode will not appear.")
    public float CapRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "2")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void CapRadiusInCentimeters(float capRadiusInCentimeters) {}

    @Override
    @SimpleProperty(description = "Gets the 3D texture",
            category = PropertyCategory.APPEARANCE)
    public String Texture()  {
        Log.d("capnode","get texture on capnode" + this.texture);
        return this.texture; }

    @Override
    @SimpleProperty(description = "The 3D texturebe loaded.",
            category = PropertyCategory.APPEARANCE)
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
    public void Texture(String texture) {
        Log.d("capnode","set texture on capnode" + texture);
        this.texture = texture;}

  }
