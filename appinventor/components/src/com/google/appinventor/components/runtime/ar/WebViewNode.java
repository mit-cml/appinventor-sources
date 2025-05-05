// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
description = "<p>A component that displays a webpage in an ARView3D.  The webpage is positioned " +
      "at a point and can be interacted with as well as changed.  Because you can interact with " +
      "the webage, you cannot use gestures such as Click, LongClick, PanToMove, PinchToScale, and " +
      "RotateWithGesture.</p>" +
      "<p>Warning: This is not a full browser as some pages may not be fully rendered.</p>",
category = ComponentCategory.AR)

@SimpleObject
public final class WebViewNode extends ARNodeBase implements ARWebView {
  public WebViewNode(final ARNodeContainer container) {
    super(container);
    // Additional updates
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
