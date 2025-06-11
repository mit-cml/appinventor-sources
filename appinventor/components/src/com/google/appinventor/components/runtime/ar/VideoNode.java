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


// TODO: either supply a simple quad or make one
@UsesAssets(fileNames = "cube.obj, Palette.png")
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
public final class VideoNode extends ARNodeBase implements ARVideo {


  private Anchor anchor = null;
  private Trackable trackable = null;
  private String texture = "";
  private String objectModel = Form.ASSETS_PREFIX + "plane.obj";
  private float scale = 1.0f;

  public VideoNode(final ARNodeContainer container) {
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


    String[] positionArray = positionFromProperty.split(",");
    float[] position = {0f,0f,0f};

    for (int i = 0; i < positionArray.length; i++) {
      position[i] = Float.parseFloat(positionArray[i]);
    }
    float[] rotation = {0,0,0, 1}; // no rotation TBD
    if (this.trackable != null) {
      Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
      Anchor(myAnchor);
    }
  }
  @Override
  public float Scale() { return this.scale; }

  @Override
  public void Scale(float t) { this.scale = t;}


  @Override
  @SimpleProperty(description = "How far, in centimeters, the VideoNode extends along the x-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the VideoNode " +
    "will not be shown.")
  public float WidthInCentimeters() { return 0.5f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "50")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void WidthInCentimeters(float widthInCentimeters) {}

  @Override
  @SimpleProperty(description = "How far, in centimeters, the VideoNode extends along the y-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the VideoNode " +
    "will not be shown.")
  public float HeightInCentimeters() { return 0.5f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "37.5")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void HeightInCentimeters(float heightInCentimeters) {}

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
    defaultValue = "")
  @SimpleProperty(
    description = "The \"path\" to the video.  Usually, this will be the "
    + "name of the video file, which should be added in the Designer.",
    category = PropertyCategory.BEHAVIOR)
  public void Source(String path) {}

  @Override
  @SimpleProperty(description = "Returns true if the video is currently playing " +
    "false otherwise.")
  public boolean IsPlaying() { return false; }

  /**
  * Sets the volume property to a number between 0 and 100.
  *
  * @param vol  the desired volume level
  */
  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
    defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
                  description = "Sets the volume to a number between 0 and 100. " +
    "Values less than 0 will be treated as 0, and values greater than 100 " +
    "will be treated as 100.")
  public void Volume(int vol) {}

  // Methods
  @Override
  @SimpleFunction(description = "Starts playback of the video.")
  public void Play() {}

  @Override
  @SimpleFunction(description = "Pauses playback of the video.")
  public void Pause() {}

  @Override
  @SimpleFunction(description = "Returns duration of the video in milliseconds.")
  public int GetDuration() { return 0; }

  @Override
  @SimpleFunction(description = "Seeks to the requested time (specified in milliseconds) in the video. " +
      "If the video is paused, the frame shown will not be updated by the seek. " +
      "The player can jump only to key frames in the video, so seeking to times that " +
      "differ by short intervals may not actually move to different frames.")
  public void SeekTo(int ms) {}

  // Events
  @Override
  @SimpleEvent(description = "Indicated that the video has reached the end.")
  public void Completed() {}

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
  public void FillColorOpacity(int FillColorOpacity) {}

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
}
