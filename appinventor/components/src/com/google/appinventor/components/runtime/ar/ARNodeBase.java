// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import android.util.Log;

import java.util.List;
import java.util.Collection;

@SimpleObject
public abstract class ARNodeBase implements ARNode, FollowsMarker {
  // protected ContainerType container = null;
  protected ARView3D arView = null;

  @SuppressWarnings("WeakerAccess")
  protected ARNodeBase(ARNodeContainer container) {
    // any initializing that needs to be done here
    //arView = container;
  }

  @Override
  public int Height() { return 0; }

  @Override
  public void Height(int height) {}

  @Override
  public int Width() { return 0; }

  @Override
  public void Width(int width) {}

  @Override
  public Anchor Anchor() { return null; }

  @Override
  public void Anchor(Anchor a) {}

  @Override
  public Object Pose() { return null; }

  @Override
  public void Pose(Object p) {}

  @Override
  public float[] PoseFromPropertyPosition(){return null;};

  @Override
  public void PoseFromPropertyPosition(String positionFromProperty){} ;
  @Override
  public void PoseFromPropertyPositions(String x, String y, String z){} ;

  @Override
  public Trackable Trackable() { return null; }

  @Override
  public void Trackable(Trackable t) {}

  @Override
  public Session Session() { return null; }

  @Override
  public void Session(Session s) {}

  @Override
  public String Model() { return null; }

  @Override
  public void Model(String o) {}

  @Override
  @SimpleProperty(description = "Returns the type of node as a String.")
  public String Type() { return getClass().getSimpleName(); };


  @SimpleProperty(description = "Convert current pose to yail",
      category = PropertyCategory.APPEARANCE)
  public YailDictionary PoseToYailDictionary() {
    Log.i("poseToYailDictionary", "Capsule pose as YailDict");
    Pose p = this.Anchor().getPose();
    Log.i("poseToYailDictionary", "pose is " + p);
    if (p != null) {
      YailDictionary innerDictSave = new YailDictionary();
      YailDictionary yailDictSave = new YailDictionary();

      innerDictSave.put("x", p.tx());
      innerDictSave.put("y", p.ty());
      innerDictSave.put("z", p.tz());
      yailDictSave.put("t", innerDictSave);
      innerDictSave.put("x", p.qx());
      innerDictSave.put("y", p.qy());
      innerDictSave.put("z", p.qz());
      innerDictSave.put("w", p.qw());
      yailDictSave.put("q", innerDictSave);

      Log.i("exporting pose as YailDict", "with " + yailDictSave);
      return yailDictSave;
    }
    return null;

  }

  @SimpleProperty(description = "Serialize the arnode to yail",
      category = PropertyCategory.APPEARANCE)
  public YailDictionary ARNodeToYail() {
    Log.i("arNode", "going to try to export ARNode as yail");
    YailDictionary yailDict = new YailDictionary();

    yailDict.put("model", this.Model());
    yailDict.put("texture", this.Texture());
    yailDict.put("scale", this.Scale());
    yailDict.put("pose", this.PoseToYailDictionary());
    yailDict.put("type", "capsule");

    try {
      Log.i("exporting ARNode as Yail", "convert toYail " + " " + yailDict);
      return yailDict;
    } catch (Exception e){
      Log.e("failed to export as yail", "");
    }
    return null;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY,
      defaultValue = "True")
  @SimpleProperty
  public boolean Visible() { return true; }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies whether the component should be visible on the screen. "
          + "Value is true if the component is showing and false if hidden.")
  public void Visible(boolean visibility) {}


  @Override
  @SimpleProperty(description = "Specifies whether the node should show a shadow when " +
    "it is lit by Lights.")
  public boolean ShowShadow() { return false; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ShowShadow(boolean showShadow) {}

  @Override
  @SimpleProperty(description = "Sets the opacity of the node.  Values less than zero " +
    "will be treated as zero, and values greater than 100 will be treated as 100.")
  public int Opacity() { return 100; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Opacity(int opacity) {}

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
        description = "The color of the node.  If the Texture is set, the color is not shown.")
  public int FillColor() { return 0; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_RED)
  @SimpleProperty()
  public void FillColor(int color) {}

  @Override
  @SimpleProperty(description = "The opacity of the node's FillColor.  Values less than zero " +
    "will be treated as zero, and values greater than 100 will be treated as 100.")
  public int FillColorOpacity() { return 100; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FillColorOpacity(int colorOpacity) {}

  @Override
  @SimpleProperty(description = "The image used to texture the node.  If set, the FillColor is not shown.")
  public String Texture()  { return ""; }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  public void Texture(String texture) {}

  @Override
  @SimpleProperty(description = "The opacity of the node's Texture.  Values less than zero " +
    "will be treated as zero, and values greater than 100 will be treated as 100.")
  public int TextureOpacity()  { return 100; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void TextureOpacity(int textureOpacity) {}

  @Override
  @SimpleProperty(description = "If the property is set to true, then the node can be scaled using " +
    "the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.")
  public boolean PinchToScale() { return false; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void PinchToScale(boolean pinchToScale) {}

  @Override
  @SimpleProperty(description = "If the property is set to true, then the node can be moved " +
    "using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the " +
    "pan gesture.")
  public boolean PanToMove() { return false; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void PanToMove(boolean panToMove) {}

  @Override
  @SimpleProperty(description = "If the property is set to true, the the node can be rotated " +
    "around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and " +
    "counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed " +
    "with a rotation gesutre.")
  public boolean RotateWithGesture() { return false; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void RotateWithGesture(boolean rotateWithGesture) {}

  @Override
  @SimpleProperty(description = "The x position in centimeters of the node.")
  public float XPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void XPosition(float xPosition) {}

  @Override
  @SimpleProperty(description = "The y position in centimeters of the node.")
  public float YPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void YPosition(float yPosition) {}

  @Override
  @SimpleProperty(description = "The z position in centimeters of the node.")
  public float ZPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void ZPosition(float zPosition) {}

  @Override
  @SimpleProperty(description = "The x rotation of the node in degrees.")
  public float XRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void XRotation(float xRotation) {}

  @Override
  @SimpleProperty(description = "The y rotation of the node in degrees.")
  public float YRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void YRotation(float yRotation) {}

  @Override
  @SimpleProperty(description = "The z rotation of the node in degrees.")
  public float ZRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void ZRotation(float zRotation) {}

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1")
  @SimpleProperty(description = "The scale of the node.  This is used to multiply its " +
    "sizing properties.  Values less than zero will be treated as their absolute value.")
  public float Scale() { return 1f; }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Scale(float scalar) {}

  @Override
  @SimpleProperty(description = "Specifies whether a node is following an ImageMarker.  Returns " +
    "true if it is and false otherwise.")
  public boolean IsFollowingImageMarker() { return false; }

  // Functions



  @Override
  @SimpleFunction(description = "Changes the node's x rotation by the given degrees.")
  public void RotateXBy(float degrees) {}

  @Override
  @SimpleFunction(description = "Changes the node's y rotation by the given degrees.")
  public void RotateYBy(float degrees) {}

  @Override
  @SimpleFunction(description = "Changes the node's z rotation by the given degrees.")
  public void RotateZBy(float degrees) {}

  @Override
  @SimpleFunction(description = "Changes the node's scale by the given scalar.")
  public void ScaleBy(float scalar) {}

  @Override
  @SimpleFunction(description = "Changes the node's position by (x,y,z).")
  public void MoveBy(float x, float y, float z) {}


  @Override
  @SimpleFunction(description = "Changes the node's position to (x,y,z).")
  public void MoveTo(float x, float y, float z) {}

  @Override
  @SimpleFunction(description = "Changes the node's position to (x,y,z).")
  public void MoveToDetectedPlane(ARDetectedPlane targetPlane, Object p) {}

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between two nodes.")
  public float DistanceToNode(ARNode node) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between a node and a Spotlight.")
  public float DistanceToSpotlight(ARSpotlight light) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between a node and a Pointlight.")
  public float DistanceToPointLight(ARPointLight light) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between a node and a DetectedPlane.")
  public float DistanceToDetectedPlane(ARDetectedPlane detectedPlane) { return 1.0f; }

  // NOTE: uncomment should we want to allow nonuniform scaling
  // @Override
  // @SimpleProperty(description = "")
  // public float XScale() { return 0f; }
  //
  // @Override
  // @SimpleProperty(description = "")
  // public float YScale() { return 0f; }
  //
  // @Override
  // @SimpleProperty(description = "")
  // public float ZScale() { return 0f; }

  @Override
  @SimpleFunction(description = "Makes the node follow an ImageMarker and sets its position to be the center of " +
  "the detected image.")
  public void Follow(ARImageMarker imageMarker) {}

  @Override
  @SimpleFunction(description = "Makes the node follow an ImageMarker and sets its position to be the center of " +
  "the detected image with an offset of (x,y,z).")
  public void FollowWithOffset(ARImageMarker imageMarker, float x, float y, float z) {}

  @Override
  @SimpleFunction(description = "Makes the node stop following the ImageMarker and sets its position " +
    "to its current position when this block is called.")
  public void StopFollowingImageMarker() {}

  @Override
  @SimpleFunction(description = "Rotates the node to look at the given node.")
  public void LookAtNode(ARNode node) {}

  @Override
  @SimpleFunction(description = "Rotates the node to look at the DetectedPlane.")
  public void LookAtDetectedPlane(ARDetectedPlane detectedPlane) {}

  @Override
  @SimpleFunction(description = "Rotates the node to look at the Spotlight.")
  public void LookAtSpotlight(ARSpotlight light) {}

  @Override
  @SimpleFunction(description = "Rotates the node to look at the PointLight.")
  public void LookAtPointLight(ARPointLight light) {}

  @Override
  @SimpleFunction(description = "Rotates the node to look at the (x,y,z) position.")
  public void LookAtPosition(float x, float y, float z) {}


  // Events

  @Override
  @SimpleEvent(description = "The user clicked on the node.")
  public void Click() {}

  @Override
  @SimpleEvent(description = "The user long-pressed on the node.")
  public void LongClick() {}

  @Override
  @SimpleEvent(description = "The node stopped following an ImageMarker.  This event " +
  "will trigger after the StopFollowingImageMarker block is called.")
  public void StoppedFollowingMarker() {}

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return arView.getDispatchDelegate();
  }

}
