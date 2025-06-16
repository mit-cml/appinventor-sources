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
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
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

  private float[] fromPropertyPosition;
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
    Log.i("capsuleNode", "about to export Capsule pose as JSON");
    Pose p = this.anchor.getPose();
    Locale locale = Locale.ENGLISH;

    JSONObject innerObject = new JSONObject();
    JSONObject innerObject2 = new JSONObject();
    JSONObject saveObject = new JSONObject();
    innerObject.put("x", p.tx());
    innerObject.put("y", p.ty());
    innerObject.put("z", p.tz());
    saveObject.put("t", innerObject);
    innerObject2.put("x", p.qx());
    innerObject2.put("y", p.qy());
    innerObject2.put("z", p.qz());
    innerObject2.put("w", p.qw());
    saveObject.put("q", innerObject2);

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
    float[] rotation = {0f,0f,0f, 1f}; // no rotation rn TBD
    if (this.trackable != null) {
      Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    }
    Log.i("store Capsule pose", "with position" +positionFromProperty);
  }

  /*@Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXT, defaultValue = "")
  public void PoseFromPropertyPositions(String x, String y, String z) {
    Log.i("setting Capsule pose", "with position" + x + " " + y  + " " + z);

    float[] position = { Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z)};

    float[] rotation = {0,0,0, 1}; // no rotation rn TBD
    if (this.trackable != null) {
      Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    }
  }*/



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
        return this.texture; }

    @Override
    @SimpleProperty(description = "The 3D texturebe loaded.",
            category = PropertyCategory.APPEARANCE)
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
    public void Texture(String texture) {
        Log.d("capnode","set texture on capnode" + texture);
        this.texture = texture;}

  }
