// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;


import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
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


@UsesAssets(fileNames = "plane.obj, Palette.png")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a video in an ARView3D.  The video is positioned " +
        "at a point, and the source, or video to be played, can be set." +
        "<p>App Inventor for Android only permits video files under 1 MB and " +
        "limits the total size of an application to 5 MB, not all of which is " +
        "available for media (video, audio, and sound) files.  If your media " +
        "files are too large, you may get errors when packaging or installing " +
        "your application, in which case you should reduce the number of media " +
        "files or their sizes.  Most video editing software, such as Windows " +
        "Movie Maker and Apple iMovie, can help you decrease the size of videos " +
        "by shortening them or re-encoding the video into a more compact format.</p>",
    category = ComponentCategory.AR)
@SimpleObject
public final class WebViewNode extends ARNodeBase implements ARWebView {

  private float[] fromPropertyPosition = {0f,0f,0f};
  private Anchor anchor = null;
  private Trackable trackable = null;
  private String texture = "";
  private String objectModel = Form.ASSETS_PREFIX + "plane.obj";
  private float scale = 1.0f;


  public WebViewNode(final ARNodeContainer container) {
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
      Log.i("webview","moved anchor BY " + newPose+ " with rotaytion "+rotation);
    }else {
      Log.i("webview", "tried to move anchor BY pose");
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
  @SimpleProperty(description = "How far, in centimeters, the WebViewNode extends along the x-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the WebViewNode " +
    "will not be shown.")
  public float WidthInCentimeters() { return 0.5f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "12.5")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void WidthInCentimeters(float widthInCentimeters) {}

  @Override
  @SimpleProperty(description = "How far, in centimeters, the WebViewNode extends along the y-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the WebViewNode " +
    "will not be shown.")
  public float HeightInCentimeters() { return 0.175f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "17.5")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void HeightInCentimeters(float heightInCentimeters) {}

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "URL of the page the WebViewNode should initially open to.  " +
    "Setting this will load the page.")
  public void HomeUrl(String url) {}

  @Override
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String HomeUrl() { return ""; }

  // Methods
  @Override
  @SimpleFunction(description = "Returns true if the WebViewNode can go backward in the history list.")
  public boolean CanGoBack() { return false; }

  @Override
  @SimpleFunction(description = "Returns true if the WebViewNode can go forward in the history list.")
  public boolean CanGoForward() { return false; }

  @Override
  @SimpleFunction(description = "Go back to the previous page in the history list.  " +
    "Does nothing if there is no previous page.")
  public void GoBack() {}

  @Override
  @SimpleFunction(description = "Go forward to the next page in the history list.   " +
    "Does nothing if there is no next page.")
  public void GoForward() {}

  @Override
  @SimpleFunction(description = "Reloads the current webpage.")
  public void Reload() {}

  @Override
  @SimpleFunction(description = "Load the page at the given URL.")
  public void GoToUrl(String url) {}

  @Override
  @SimpleFunction(description = "Loads the home URL page.  This happens automatically when " +
    "the home URL is changed.")
  public void GoHome() {}

  // Hidden Properties
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
  public void FillColorOpacity(int textureOpacity) {}

  @Override
  @SimpleFunction(description = "move a webview node properties at the " +
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
  @SimpleProperty(userVisible = false)
  public String Texture() { return ""; }

  @Override
  @SimpleProperty(userVisible = false)
  public void Texture(String texture) {}

  @Override
  @SimpleProperty(userVisible = false)
  public int TextureOpacity() { return 0; }

  @Override
  @SimpleProperty(userVisible = false)
  public void TextureOpacity(int textureOpacity) {}

  @Override
  @SimpleProperty(userVisible = false)
  public void PinchToScale(boolean pinchToScale) {}

  @Override
  @SimpleProperty(userVisible = false)
  public boolean PinchToScale() { return false; }

  @Override
  @SimpleProperty(userVisible = false)
  public void PanToMove(boolean panToMove) {}

  @Override
  @SimpleProperty(userVisible = false)
  public boolean RotateWithGesture() { return false; }

  @Override
  @SimpleProperty(userVisible = false)
  public void RotateWithGesture(boolean rotateWithGesture) {}

  // Hidden Methods
  @Override
  public void Click() {}

  @Override
  public void LongClick() {}
}
