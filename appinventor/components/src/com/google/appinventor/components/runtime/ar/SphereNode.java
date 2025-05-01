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
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import android.util.Log;


@UsesAssets(fileNames = "sphere.obj, Palette.png")
  public final class SphereNode extends ARNodeBase implements ARSphere {

    private Anchor anchor = null;
    private Trackable trackable = null;
    private String objectModel = Form.ASSETS_PREFIX + "sphere.obj";
    private String texture = Form.ASSETS_PREFIX + "Palette.png";
    private float scale = 1.0f;

    public SphereNode(final ARNodeContainer container) {
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

    @Override
    @SimpleFunction(description = "move a sphere node properties at the " +
            "specified (x,y,z) position.")
    public void MoveTo(float x, float y, float z){}

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
  public String Texture() { return this.texture; }

  @Override
  @SimpleProperty(userVisible = false)
  public void Texture(String texture) {}

  @Override
  @SimpleProperty(userVisible = false)
  public int TextureOpacity() { return 0; }

  @Override
  @SimpleProperty(userVisible = false)
  public void TextureOpacity(int textureOpacity) {}

}
