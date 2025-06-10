// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

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

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import android.util.Log;
// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a torus in an ARView3D.  The torus is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

@UsesAssets(fileNames = "torus.obj,Palette.png")
  public final class TorusNode extends ARNodeBase implements ARTorus {

    private Anchor anchor = null;
    private Trackable trackable = null;
    private String objectModel = "torus.obj";
    private String texture = "Palette.png";
    private float scale = 1.0f;

    public TorusNode(final ARNodeContainer container) {
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
    @SimpleProperty(description = "The 3D model file to be loaded.",
            category = PropertyCategory.APPEARANCE)
    public String Model() { return this.objectModel; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
    public void Model(String model) {this.objectModel = model;}

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

    @SimpleProperty(description = "Set the current pose of the object from property",
        category = PropertyCategory.APPEARANCE)
    @Override
    public void PoseFromPropertyPosition(String positionFromProperty) {
      Log.i("setting Capsule pose", "with position" +positionFromProperty);


      String[] positionArray = positionFromPropery.split(",");
      float[] position = {0f,0f,0f};

      for (int i = 0; i < positionArray.length; i++) {
        position[i] = Float.parseFloat(positionArray[i]);
      }
      float[] rotation = {pose.qx(), pose.qy(), pose.qz(), 1};
      if (this.trackable != null) {
        Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
        Anchor(myAnchor);
      }
    }


  @Override
    @SimpleProperty(description = "The ring radius defines the size of the overall " +
      "torus (or major radius) in centimeters.  Values less than zero will be treated " +
      "as their absolute value.  When set to zero, the TorusNode will not be shown.")
    public float RingRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "4")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void RingRadiusInCentimeters(float ringRadiusInCentimeters) {}

    @Override
    @SimpleProperty(description = "The pipe radius defines the size of the surface that " +
    "encircles the ring, or the pipe radius.  This can be thought of as the band of the ring.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the " +
    "TorusNode will not be shown.")
    public float PipeRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void PipeRadiusInCentimeters(float pipeRadiusInCentimeters) {}


  @Override
  public String Texture()  { return this.texture; }

  @Override
  public void Texture(String texture) {this.texture = texture;}
  }
