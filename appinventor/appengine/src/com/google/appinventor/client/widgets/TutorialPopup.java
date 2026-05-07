// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * A class that displays a tutorial popup positioned below a given element with a small arrow
 * pointing up to the element.
 */
@JsType(isNative = true, name = "Popup", namespace = JsPackage.GLOBAL)
public class TutorialPopup {
  /**
   * The callback, if any, to be invoked when the popup is clicked.
   */
  @JsFunction
  public interface Callback {
    void onClick();
  }

  /**
   * Creates a new tutorial popup.
   *
   * @param text the text to display in the popup
   * @param callback the callback to invoke when the popup is clicked, or null if no action is desired
   */
  public TutorialPopup(String text, Callback callback) {
  }

  /**
   * Show the tutorial popup.
   *
   * @param el the element to position the popup below
   */
  public native void show(Element el);

  /**
   * Hides the tutorial popup.
   */
  public native void hide();
}
