// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

public class MockFloatingActionButton extends MockVisibleComponent {

  public static final String TYPE = "FloatingActionButton";
  public static final String PROPERTY_NAME_ICON = "Icon";

  private AbsolutePanel fabWidget;
  private Button fabButton;
  private Image fabImage = null;
  private String imagePath = "";

  public MockFloatingActionButton(SimpleEditor editor) {
    super(editor, TYPE, images.fab());

    fabWidget = new AbsolutePanel();
    fabWidget.setStylePrimaryName("ode-SimpleMockFormFABContainer");

    fabButton = new Button();
    fabButton.setStylePrimaryName("ode-SimpleMockFormFABButton");
    fabWidget.add(fabButton);

    initComponent(fabWidget);
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_HEIGHT) || propertyName.equals(PROPERTY_NAME_WIDTH)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  private void setIconProperty(String newValue) {
    if (fabImage == null) {
      fabImage = new Image();
      fabImage.addErrorHandler(new ErrorHandler() {
        @Override
        public void onError(ErrorEvent errorEvent) {
          refreshForm(true);
        }
      });
      fabImage.addLoadHandler(new LoadHandler() {
        @Override
        public void onLoad(LoadEvent loadEvent) {
          refreshForm(true);
        }
      });
      fabImage.setWidth("24px");
      fabImage.setHeight("24px");
    }
    String url = convertImagePropertyValueToUrl(newValue);
    if (url == null) {
      fabImage.removeFromParent();
      fabImage.setUrl(images.defaultImage().getURL());
    } else {
      fabImage.setUrl(url);
    }
    if (!fabImage.isAttached()) {
      fabButton.getElement().appendChild(fabImage.getElement());
    }
    imagePath = newValue;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (PROPERTY_NAME_ICON.equals(propertyName)) {
      setIconProperty(newValue);
    }
  }
}
