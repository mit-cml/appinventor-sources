/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.sticky.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * A collection of static factories to create {@link CustomButton}s with various
 * state faces.
 *
 */
public class Buttons {
  /**
   * Creates a {@link PushButton} with the specified face images and stylename.
   *
   * @param upImage
   *          the image to be used on the up face
   * @param hvImage
   *          the image to be used on the hover face
   * @param dnImage
   *          the image to be used on the down face
   * @param styleName
   *          the stylename to use for the widget
   * @param handler
   *          a click handler to which to bind the button
   * @return the button
   */
  public static PushButton createPushButtonWithImageStates(Image upImage,
      Image hvImage, Image dnImage, String styleName, ClickHandler handler) {
    final PushButton button = createPushButtonWithImageStates(upImage, hvImage,
        styleName, handler);
    button.getDownFace().setImage(dnImage);
    return button;
  }

  /**
   * Creates a {@link PushButton} with the specified face images and stylename.
   *
   * @param upImage
   *          the image to be used on the up face
   * @param hvImage
   *          the image to be used on the hover face
   * @param styleName
   *          the stylename to use for the widget
   * @param handler
   *          a click handler to which to bind the button
   * @return the button
   */
  public static PushButton createPushButtonWithImageStates(Image upImage,
      Image hvImage, String styleName, ClickHandler handler) {
    final PushButton button = createPushButtonWithImageStates(upImage,
        styleName, handler);
    button.getUpHoveringFace().setImage(hvImage);
    return button;
  }

  /**
   * Creates a {@link PushButton} with the specified face images and stylename.
   *
   * @param upImage
   *          the image to be used on the up face
   * @param styleName
   *          the stylename to use for the widget
   * @param handler
   *          a click handler to which to bind the button
   * @return the button
   */
  public static PushButton createPushButtonWithImageStates(Image upImage,
      String styleName, ClickHandler handler) {
    final PushButton button = new PushButton(upImage, handler);
    button.setStyleName(styleName);
    return button;
  }

  /**
   * Creates a {@link ToggleButton} with the specified face images and
   * stylename.
   *
   * @param upImage
   *          the image to be used on the up face
   * @param hvImage
   *          the image to be used on the hover face
   * @param dnImage
   *          the image to be used on the down face
   * @param styleName
   *          the stylename to use for the widget
   * @param handler
   *          a click handler to which to bind the button
   * @return the button
   */
  public static ToggleButton createToggleButtonWithImageStates(Image upImage,
      Image hvImage, Image dnImage, String styleName, ClickHandler handler) {
    final ToggleButton button = createToggleButtonWithImageStates(upImage,
        hvImage, styleName, handler);
    button.getDownFace().setImage(dnImage);
    return button;
  }

  /**
   * Creates a {@link ToggleButton} with the specified face images and
   * stylename.
   *
   * @param upImage
   *          the image to be used on the up face
   * @param hvImage
   *          the image to be used on the hover face
   * @param styleName
   *          the stylename to use for the widget
   * @param handler
   *          a click handler to which to bind the button
   * @return the button
   */
  public static ToggleButton createToggleButtonWithImageStates(Image upImage,
      Image hvImage, String styleName, ClickHandler handler) {
    final ToggleButton button = createToggleButtonWithImageStates(upImage,
        styleName, handler);
    button.getUpHoveringFace().setImage(hvImage);
    return button;
  }

  /**
   * Creates a {@link ToggleButton} with the specified face images and
   * stylename.
   *
   * @param upImage
   *          the image to be used on the up face
   * @param styleName
   *          the stylename to use for the widget
   * @param handler
   *          a click handler to which to bind the button
   * @return the button
   */
  public static ToggleButton createToggleButtonWithImageStates(Image upImage,
      String styleName, ClickHandler handler) {
    final ToggleButton button = new ToggleButton(upImage, handler);
    button.setStyleName(styleName);
    return button;
  }

  private Buttons() {
  }
}
