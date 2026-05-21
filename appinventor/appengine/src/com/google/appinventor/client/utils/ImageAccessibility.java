// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 Massachusetts Institute of Technology. All Rights Reserved.

/* This is the package-info.java file for com.google.appinventor.client.utils.
 *
 * @author jos@josdesign.nl (Jos Hirth)
 */

package com.google.appinventor.client.utils;

import com.google.gwt.user.client.ui.Image;

/**
 * Utility methods for making images accessible.
 *
 * <p>This class provides helper methods to add accessibility attributes to GWT Image widgets.
 * GWT's Image widget doesn't automatically set alt attributes, so these utilities make it
 * easier to ensure all images have proper alternative text for screen readers.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Image image = new Image("icon.png");
 * ImageAccessibility.setAltText(image, "Component icon");
 * }</pre>
 */
public class ImageAccessibility {

  /**
   * Sets the alt text for an image using DOM manipulation.
   * This is the proper way to add alt attributes to GWT Image widgets.
   *
   * <p>Screen readers will read this text aloud when the image is encountered,
   * allowing visually impaired users to understand what the image represents.</p>
   *
   * @param image The GWT Image widget
   * @param altText The alternative text description
   */
  public static void setAltText(Image image, String altText) {
    if (image != null && altText != null) {
      image.getElement().setAttribute("alt", altText);
    }
  }

  /**
   * Marks an image as decorative (empty alt text and role="presentation").
   * Use this for images that are purely decorative and provide no information.
   *
   * <p>Decorative images should have empty alt text, which tells screen readers
   * to skip over them entirely. This prevents unnecessary clutter in the
   * accessibility tree.</p>
   *
   * @param image The GWT Image widget
   */
  public static void setDecorative(Image image) {
    if (image != null) {
      image.getElement().setAttribute("alt", "");
      image.getElement().setAttribute("role", "presentation");
    }
  }
}
