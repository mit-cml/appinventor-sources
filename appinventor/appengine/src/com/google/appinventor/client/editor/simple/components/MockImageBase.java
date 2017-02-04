// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
// TODO(user): reconsider visibilities of the abstract base classes in this package

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.common.primitives.Ints;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Abstract superclass for MockImage and MockImageSprite.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class MockImageBase extends MockVisibleComponent {
  // Property names
  private static final String PROPERTY_NAME_PICTURE = "Picture";
  private static final String PROPERTY_SCALE_PICTURE_TO_FIT = "ScalePictureToFit";
  private static final String PROPERTY_SCALING = "Scaling";

  // Widget for showing the image.
  private final Image image;
  private String picturePropValue;
  private String scalingMode = "0"; // corresponds to Scale proportionally

  MockImageBase(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, icon);

    image = new Image();
    image.addErrorHandler(new ErrorHandler() {
      @Override
      public void onError(ErrorEvent event) {
        if (picturePropValue != null && !picturePropValue.isEmpty()) {
          OdeLog.elog("Error occurred while loading image " + picturePropValue);
        }
        refreshForm(true);
      }
    });
    image.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent event) {
        refreshForm(true);
        resizeImage();  // resize after the new image occupies the form
      }
    });

    SimplePanel simplePanel = new SimplePanel();
    simplePanel.setStylePrimaryName("ode-SimpleMockComponent");
    simplePanel.addStyleName("imageComponentCenterPanel");
    simplePanel.setWidget(image);
    initComponent(simplePanel);
  }

  /*
   * Sets the image's url to a new value.
   */
  private void setPictureProperty(String text) {
    picturePropValue = text;
    String url = convertImagePropertyValueToUrl(text);
    if (url == null) {
      // text was not recognized as an asset. Just display the icon for this type of component.
      image.setUrl(getIconImage().getUrl());
    } else {
      image.setUrl(url);
    }
  }

  @Override
  public int getPreferredWidth() {
    // The superclass uses getOffsetWidth, which won't work for us.
    // Hide away the current 100% size so we can get at the actual size, otherwise automatic size doesn't work
    String[] style = MockComponentsUtil.clearSizeStyle(image);
    int width = image.getWidth();
    MockComponentsUtil.restoreSizeStyle(image, style);
    return width;
  }

  @Override
  public int getPreferredHeight() {
    // The superclass uses getOffsetHeight, which won't work for us.
    // Hide away the current 100% size so we can get at the actual size, otherwise automatic size doesn't work
    String[] style = MockComponentsUtil.clearSizeStyle(image);
    int height = image.getHeight();
    MockComponentsUtil.restoreSizeStyle(image, style);
    return height;
  }

  /**
   * This resizes the picture according to
   * 1. height and width value of the div tag enclosing the img tag
   * 2. scaling mode. 0 - Scale proportionally, 1 - Scale to fit
   *    which correspond to the choices in ScalingChoicePropertyEditor
   *
   * This should be called whenever a property affecting the size is changed
   */
  private void resizeImage() {
    if (image.getUrl().equals(getIconImage().getUrl())) {
      unclipImage();
      return;
    }

    String width = getElement().getStyle().getWidth();
    String height = getElement().getStyle().getHeight();

    // the situation right after refreshing the page
    if (width.isEmpty() || height.isEmpty()) {
      return;
    }

    int frameWidth = Ints.tryParse(width.substring(0, width.indexOf("px")));
    int frameHeight = Ints.tryParse(height.substring(0, height.indexOf("px")));

    if (scalingMode.equals("0")) {
      float ratio = Math.min(frameWidth / (float) getPreferredWidth(),
          frameHeight / (float) getPreferredHeight());
      int scaledWidth = Double.valueOf(getPreferredWidth() * ratio).intValue();
      int scaledHeight = Double.valueOf(getPreferredHeight() * ratio).intValue();
      image.setSize(scaledWidth + "px", scaledHeight + "px");

    } else if (scalingMode.equals("1")) {
      image.setSize("100%", "100%");

    } else {
      throw new IllegalStateException("Illegal scaling mode: " + scalingMode);
    }
  }

  private void unclipImage() {
    Style style = image.getElement().getStyle();
    style.clearLeft();
    style.clearTop();
    style.clearWidth();
    style.clearHeight();
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_PICTURE)) {
      setPictureProperty(newValue); // setUrl() triggers onLoad
    } else if (propertyName.equals(PROPERTY_NAME_WIDTH)) {
      resizeImage();
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      resizeImage();
      refreshForm();
    } else if (propertyName.equals(PROPERTY_SCALING)) {
      scalingMode = newValue;
      resizeImage();
      refreshForm();
    } else if (propertyName.equals(PROPERTY_SCALE_PICTURE_TO_FIT)) {
      boolean scaleIt = Boolean.parseBoolean(newValue);
      if (scaleIt) {
        scalingMode = "1";
      } else {
        scalingMode = "0";
      }
      resizeImage();
      refreshForm();
    }
  }
}
