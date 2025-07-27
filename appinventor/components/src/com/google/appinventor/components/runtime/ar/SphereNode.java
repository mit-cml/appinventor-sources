// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.*;


import com.google.appinventor.components.common.PropertyTypeConstants;

import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.Session;
import android.util.Log;



@UsesAssets(fileNames = "sphere.obj, Palette.png")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a 3D model in an ARView3D.  " +
        "External model files can be uploaded to the server for use, but the files " +
        "must be less than 5 MB.  An error will occur if the provided model file " +
        "cannot be loaded.", // TODO: include anything about converting models when uploaded to the server.
    category = ComponentCategory.AR)
@SimpleObject
  public final class SphereNode extends ARNodeBase implements ARSphere {

    private String objectModel = Form.ASSETS_PREFIX + "sphere.obj";
    private String texture = Form.ASSETS_PREFIX + "Palette.png";

    public SphereNode(final ARNodeContainer container) {
      super(container);
      Model( objectModel);
      Texture(texture);
      container.addNode(this); // hooks in session
    }

    @Override
    public void Session(Session s){ this.session = s;};


    @Override
    @SimpleFunction(description = "move a capsule node properties at the " +
        "specified (x,y,z) position.")
    public void MoveBy(float x, float y, float z){

      float[] position = { 0, 0, 0};
      float[] rotation = {0, 0, 0, 1};

      //float[] currentAnchorPoseRotation = rotation;
      TrackingState trackingState = null;
      if (this.Anchor() != null) {
        float[] translations = this.Anchor().getPose().getTranslation();
        position = new float[]{translations[0] + x, translations[1] + y, translations[2] + z};
        //currentAnchorPoseRotation = Anchor().getPose().getRotationQuaternion(); or getTranslation() not working yet
        trackingState = this.Anchor().getTrackingState();
      }

      Pose newPose = new Pose(position, rotation);
      if (this.trackable != null ){
        Anchor(this.trackable.createAnchor(newPose));
        Log.i("sphere","moved anchor BY " + newPose+ " with rotaytion "+rotation);
      }else {
        if (trackingState == TrackingState.TRACKING){
          if (session != null){
            Log.i("sphere", "moved anchor BY, make anchor with SESSION, ");
            Anchor(session.createAnchor(newPose));
          } else{
            Log.i("sphere", "tried to move anchor BY pose, session must be 0" + (session == null));
          }
        }

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
        Log.i("sphere","moved anchor to pose: " + newPose+ " with rotaytion "+currentAnchorPoseRotation);
      }else {
        Log.i("sphere", "tried to move anchor to pose");
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


    @Override
    @SimpleProperty(description = "The radius of the sphere in centimeters.")
    public float RadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "5")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void RadiusInCentimeters(float radiusInCentimeters) {}


  // FUNCTIONS
  @SimpleFunction(description = "Sets the color of all nodes with the given opacity.  " +
          "Opacity vales less than 0 will be treated as 0, and values greater than 100 will be " +
          "treated as 100.")
  public void SetFillColorForAllNodes(int color, int opacity) {}

  @SimpleFunction(description = "<p>Sets the color of a node named \"name\" with the given opacity.  " +
          "If a node named \"name\" does not exist, then the <code>NodeNotFound</code> event " +
          "will be triggered.  <code>shouldColorChildNodes</code> specifies if all nodes below " +
          "below the named node in the node tree should also have their color set.  If " +
          "<code>shouldColorChildNodes</code> is false and the named node cannot be colored, an " +
          "error will occur.  Otherwise, no error will occur, and the child nodes will attempt to be colored.</p>" +
          "Opacity vales less than 0 will be treated as 0, and values greater than 100 will be " +
          "treated as 100.")
  public void SetFillColorForNode(String name, int color, int opacity, boolean shouldColorChildNodes) {}

  @SimpleFunction(description = "<p>Sets the texture of a node named \"name\" with the given opacity.  " +
          "If a node named \"name\" does not exist, then the <code>NodeNotFound</code> event " +
          "will be triggered.  <code>shouldTexturizeChildNodes</code> specifies if all nodes below " +
          "below the named node in the node tree should also have their color set.  If " +
          "<code>shouldTexturizeChildNodes</code> is false and the named node cannot be textured, an " +
          "error will occur.  Otherwise, no error will occur, and the child nodes will attempt to be textured.</p>" +
          "Opacity vales less than 0 will be treated as 0, and values greater than 100 will be " +
          "treated as 100.")
  public void SetTextureForNode(String name, String texture, int opacity, boolean shouldTexturizeChildNodes) {
    //EventDispatcher.dispatchEvent(this, "SetTextureForNode");
    this.texture = texture;
  }

  @SimpleFunction(description = "Sets the texture of all nodes with the given opacity." +
          "Opacity vales less than 0 will be treated as 0, and values greater than 100 will be " +
          "treated as 100.")
  public void SetTextureForAllNodes(String texture, int opacity) {}

  @SimpleFunction(description = "<p>Sets whether the shadow is shown for a node named \"name\".  " +
          "If a node named \"name\" does not exist, then the <code>NodeNotFound</code> event " +
          "will be triggered.  <code>shouldShadowChildNodes</code> specifies if all nodes below " +
          "below the named node in the node tree should also have their shadow set.")
  public void SetShowShadowForNode(String name, boolean showShadow, boolean shouldShadowChildNodes) {}

  @SimpleFunction(description = "Sets if all nodes show a shadow.")
  public void SetShowShadowForAllNodes(boolean showShadow) {}

  @SimpleFunction(description = "Plays all animations in the model, if it has animations.")
  public void PlayAnimationsForAllNodes() {}

  @SimpleFunction(description = "Plays animations attached to a node named \"name\".  " +
          "If a node named \"name\" does not exist, then the <code>NodeNotFound</code> event " +
          "will be triggered.  <code>shouldPlayChildNodes</code> specifies if all nodes below " +
          "below the named node in the node tree should also have their animations played.")
  public void PlayAnimationsForNode(String name, boolean shouldPlayChildNodes) {}

  @SimpleFunction(description = "Stops all animations in the model, if it has animations.")
  public void StopAnimationsForAllNodes() {}

  @SimpleFunction(description = "Stops animations attached to a node named \"name\".  " +
          "If a node named \"name\" does not exist, then the <code>NodeNotFound</code> event " +
          "will be triggered.  <code>shouldStopChildNodes</code> specifies if all nodes below " +
          "below the named node in the node tree should also have their animations stopped.")
  public void StopAnimationsForNode(String name, boolean shouldStopChildNodes) {}

  @SimpleFunction(description = "Renames a node named \"oldName\" to \"newName\".  " +
          "If no node exists with name \"oldName\", then the <code>NodeNotFound</code> event will be triggered.")
  public void RenameNode(String oldName, String newName) {}

  @SimpleEvent(description = "This event is triggered when the user tries to access a " +
          "node named \"name\", but a node with that \"name\" does not exist.")
  public void NodeNotFound(String name) {}

  // Hidden Properties
  @Override
  @SimpleProperty(userVisible = false)
  public boolean ShowShadow() { return false; }

  @Override
  @SimpleProperty(userVisible = false)
  public void ShowShadow(boolean showShadow) {}

  @Override
  @SimpleProperty(userVisible = false)
  public int FillColor() { return 0; }

  @Override
  @SimpleProperty(userVisible = false)
  public void FillColor(int color) {}

  @Override
  @SimpleProperty(userVisible = false)
  public int FillColorOpacity() { return 0; }

  @Override
  @SimpleProperty(userVisible = false)
  public void FillColorOpacity(int FillColorOpacity) {}


  @Override
  @SimpleProperty(userVisible = false)
  public int TextureOpacity() { return 0; }

  @Override
  @SimpleProperty(userVisible = false)
  public void TextureOpacity(int textureOpacity) {}

}
