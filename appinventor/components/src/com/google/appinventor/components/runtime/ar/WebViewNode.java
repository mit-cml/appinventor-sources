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
  private String objectModel = Form.ASSETS_PREFIX + "plane.obj";
  private String texture = Form.ASSETS_PREFIX + "Palette.png";


  public WebViewNode(final ARNodeContainer container) {
    super(container);
    //Texture(texture); // or url, not sure how this works yet
    Model(objectModel);
    container.addNode(this);
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



}
