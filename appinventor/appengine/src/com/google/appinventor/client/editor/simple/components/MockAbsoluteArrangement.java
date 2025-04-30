// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MockAbsoluteArrangement extends MockContainer {

  /**
   * Component type name.
   */
  public static final String TYPE = "AbsoluteArrangement";
  private static final Logger LOG = Logger.getLogger(MockAbsoluteArrangement.class.getName());

  // Form UI components
  protected final AbsolutePanel layoutWidget;

  // Property names
  private static final String PROPERTY_NAME_IMAGE = "Image";

  private boolean hasImage;

  // We need to maintain these so we can show color and shape only when
  // there is no image.
  private String backgroundColor;

  private final Image image;
  private String imagePropValue;

  /**
   * Creates a mock absolute arrangement object.
   *
   * @param editor editor object
   */
  public MockAbsoluteArrangement(SimpleEditor editor) {
    super(editor, TYPE, images.table(), new MockAbsoluteLayout());

    rootPanel.setHeight("100%");

    layoutWidget = new AbsolutePanel();
    layoutWidget.setStylePrimaryName("ode-SimpleMockContainer");
    layoutWidget.add(rootPanel);

    image = new Image();
    image.addErrorHandler(new ErrorHandler() {
      @Override
      public void onError(ErrorEvent event) {
        if (imagePropValue != null && !imagePropValue.isEmpty()) {
          LOG.log(Level.SEVERE, "Error occurred while loading image " + imagePropValue);
        }
        refreshForm();
      }
    });
    image.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent event) {
        refreshForm();
      }
    });

    initComponent(layoutWidget);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_IMAGE)) {
      setImageProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    } else {
      if (propertyName.equals(PROPERTY_NAME_WIDTH)) {
        refreshForm();
      } else if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
        refreshForm();
      }
    }
  }

  /*
   * Sets the button's Image property to a new value.
   */
  private void setImageProperty(String text) {
    imagePropValue = text;
    String url = convertImagePropertyValueToUrl(text);
    if (url == null) {
      hasImage = false;
      url = "";
      setBackgroundColorProperty(backgroundColor);
    } else {
      hasImage = true;
      // Layouts do not show a background color if they have an image.
      // The container's background color shows through any transparent
      // portions of the Image, an effect we can get in the browser by
      // setting the widget's background color to COLOR_NONE.
      MockComponentsUtil.setWidgetBackgroundColor(layoutWidget, "&H" + COLOR_NONE);
    }
    MockComponentsUtil.setWidgetBackgroundImage(layoutWidget, url);
    image.setUrl(url);
  }

  /*
   * Sets the button's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    backgroundColor = text;
    // Android Buttons do not show a background color if they have an image.
    if (hasImage) {
      return;
    }
    if (MockComponentsUtil.isDefaultColor(text)) {
      // CSS background-color for ode-SimpleMockButton (copied from Ya.css)
      text = "&HFFE8E8E8";
    }
    MockComponentsUtil.setWidgetBackgroundColor(layoutWidget, text);
  }
}
