// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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
import java.util.Collection;

// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a capsule in an ARView3D.  The capsule is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

@UsesAssets(fileNames = "pawn_albedo.png, pawn.obj")
  public final class CapsuleNode extends ARNodeBase implements ARCapsule {


  private Anchor anchor = null;
  private Trackable trackable = null;
  private String texture = "pawn_albedo.png";
  private String objectModel = "pawn.obj";
  private float scale = 1.0f;

    public CapsuleNode(final ARNodeContainer container) {
      super(container);
      //parentSession = session;
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
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1")
    @SimpleProperty(description = "The scale of the node.  This is used to multiply its " +
            "sizing properties.  Values less than zero will be treated as their absolute value.")
   public float Scale() {return this.scale;}


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
    public void MoveTo(float x, float y, float z){}

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
    public String Texture()  { return this.texture; }

    @Override
    public void Texture(String texture) {this.texture = texture;}

  }
