// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2023 Kodular, All rights reserved
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public final class MockLinearProgress extends MockVisibleComponent {
  public static final String TYPE = "LinearProgress";

  private static final String PROPERTY_NAME_INDETERMINATE = "Indeterminate";
  private static final String PROPERTY_NAME_PROGRESS_COLOR = "ProgressColor";
  private static final String PROPERTY_NAME_INDETERMINATE_COLOR = "IndeterminateColor";

  private HorizontalPanel horizontalPanel;
  private SimplePanel leftBar;
  private SimplePanel middleBar;
  private boolean isIndeterminate = true;
  private String progressColorBackup = "&HFF2196F2";
  private String indeterminateColorBackup = "&HFF2196F2";

  public MockLinearProgress(SimpleEditor editor) {
    super(editor, TYPE, images.linearProgress());

    horizontalPanel = new HorizontalPanel();
    horizontalPanel.setStylePrimaryName("ode-SimpleMockComponent");
    MockComponentsUtil.setWidgetBackgroundColor(horizontalPanel, "&HFFCCCCCC");

    //left bar for indeterminate property
    leftBar = new SimplePanel();
    leftBar.setStylePrimaryName("ode-SimpleMockComponent");
    leftBar.setWidth("20%");
    leftBar.setHeight("6px");

    //middle bar for indeterminate property
    middleBar = new SimplePanel();
    middleBar.setStylePrimaryName("ode-SimpleMockComponent");
    middleBar.setWidth("83%");
    middleBar.setHeight("6px");

    horizontalPanel.add(leftBar);
    horizontalPanel.add(middleBar);

    initComponent(horizontalPanel);
  }

  private void setProgressColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  //white
    }
    progressColorBackup = text;
    if (!isIndeterminate) {
      setColor(text);
    }
  }

  private void setIndeterminateColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  //white
    }
    indeterminateColorBackup = text;
    if (isIndeterminate) {
      setColor(text);
    }
  }

  private void setColor(String text) {
    MockComponentsUtil.setWidgetBackgroundColor(middleBar, text);
    MockComponentsUtil.setWidgetBackgroundColor(leftBar, text);
  }

  private void setIndeterminate(String text) {
    isIndeterminate = Boolean.parseBoolean(text);
    setColor(isIndeterminate ? indeterminateColorBackup : progressColorBackup);
    leftBar.setWidth(isIndeterminate ? "20%" : "0%");
    middleBar.setWidth(isIndeterminate ? "83%" : "100%");

    leftBar.setVisible(isIndeterminate);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    switch (propertyName) {
      case PROPERTY_NAME_INDETERMINATE:
        setIndeterminate(newValue);
        refreshForm();
        break;
      case PROPERTY_NAME_PROGRESS_COLOR:
        setProgressColorProperty(newValue);
        break;
      case PROPERTY_NAME_INDETERMINATE_COLOR:
        setIndeterminateColorProperty(newValue);
        break;
    }
  }

  @Override
  int getWidthHint() {
    int widthHint = super.getWidthHint();
    if (widthHint == LENGTH_PREFERRED) {
      widthHint = LENGTH_FILL_PARENT;
    }
    return widthHint;
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }
}
