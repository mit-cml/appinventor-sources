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
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import org.json.JSONObject;
import java.util.Locale;

// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a capsule in an ARView3D.  The capsule is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

@UsesAssets(fileNames = "pawn.obj, sphere.obj, Palette.png")
  public final class CapsuleNode extends ARNodeBase implements ARCapsule {

  private float capRadius = 0.02f;// stored in meters
  private float height = 0.07f; // stored in meters
  private float width = 0.05f;

  public CapsuleNode(final ARNodeContainer container) {
    super(container);
    Model( Form.ASSETS_PREFIX + "pawn.obj");
    Texture(Form.ASSETS_PREFIX + "Palette.png");
    container.addNode(this);
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

    Log.i("exporting Capsule pose as JSON", "with " + saveObject);

    return saveObject;

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
  @SimpleFunction(description = "move a capsule node properties at the " +
      "specified (x,y,z) position.")
  public void MoveBy(float x, float y, float z) {

    float[] position = {0, 0, 0};
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
    if (this.trackable != null) {
      Anchor(this.trackable.createAnchor(newPose));
      Log.i("sphere", "moved anchor BY " + newPose + " with rotaytion " + rotation);
    } else {
      if (trackingState == TrackingState.TRACKING) {
        if (session != null) {
          Log.i("sphere", "moved anchor BY, make anchor with SESSION, ");
          Anchor(session.createAnchor(newPose));
        } else {
          Log.i("sphere", "tried to move anchor BY pose, session must be 0" + (session == null));
        }
      }

    }
  }


  @Override
  @SimpleFunction(description = "move a capsule node properties at the " +
      "specified (x,y,z) position.")
  public void MoveTo(float x, float y, float z) {
    float[] position = {x, y, z};
    float[] rotation = {0, 0, 0, 1};

    float[] currentAnchorPoseRotation = rotation;
    if (this.Anchor() != null) {
      //currentAnchorPoseRotation = Anchor().getPose().getRotationQuaternion(); not working yet
    }
    Pose newPose = new Pose(position, rotation);
    if (this.trackable != null) {
      Anchor(this.trackable.createAnchor(newPose));
      Log.i("capsule", "moved anchor to pose: " + newPose + " with rotaytion " + currentAnchorPoseRotation);
    } else {
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
  @SimpleFunction(description = "Changes the capsules's scale by the given scalar, maintaining bottom position if physics enabled.")
  public void ScaleBy(float scalar) {
    Log.i("CapsuleNode", "Scaling cap " + name + " by " + scalar);

    float oldScale = Scale();
    float newScale = oldScale * Math.abs(scalar);

    // Update physics immediately if enabled to maintain bottom position
    if (EnablePhysics()) {
      float previousSize = capRadius * Scale();
      // Adjust Y position to maintain ground contact
      float[] currentPos = getCurrentPosition();
      currentPos[1] = currentPos[1] - previousSize + (capRadius * newScale);
      setCurrentPosition(currentPos);
    }

    Scale(newScale);
    Log.i("CapsuleNode", "Scale complete - bottom position maintained");
  }

  @Override
  @SimpleProperty(description = "How far, in centimeters, the CapsuleNode extends along the y-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero or when set to " +
      "less than double the CapRadius, the CapsuleNode will not appear.")
  public float HeightInCentimeters() {
    return 0.5f;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "7")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void HeightInCentimeters(float heightInCentimeters) {
  }

  @Override
  @SimpleProperty(description = "The radius, in centimeters, of two hemispheres " +
      "or caps at the ends of a CapsuleNode.  Values less than zero will be treated as their " +
      "absolute values.  When set to zero or when set to greater than half of the Height, the " +
      "CapsuleNode will not appear.")
  public float CapRadiusInCentimeters() {
    return 0.05f;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "2")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void CapRadiusInCentimeters(float capRadiusInCentimeters) {
  }

}