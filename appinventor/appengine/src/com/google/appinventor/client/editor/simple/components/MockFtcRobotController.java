// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Mock FtcRobotController component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockFtcRobotController extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "FtcRobotController";

  private static final String PROPERTY_NAME_CONFIGURATION = "Configuration";

  private static final String LABEL_DEVICE_NAME = "Device Name:";
  private static final String LABEL_CONFIGURATION = "Active Configuration File:";
  private static final String LABEL_WIFI_DIRECT = "Wifi Direct:";
  private static final String LABEL_ROBOT_STATUS = "Robot Status:";
  private static final String LABEL_OP_MODE = "Op Mode:";
  private static final String LABEL_GAMEPAD_1 = "Gamepad 1";
  private static final String LABEL_GAMEPAD_2 = "Gamepad 2";

  private final Label configuration;

  /**
   * Creates a new MockFtcRobotController component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockFtcRobotController(SimpleEditor editor) {
    super(editor, TYPE, images.ftc());

    // Initialize mock FtcRobotController UI
    VerticalPanel panelWidget = new VerticalPanel();
    panelWidget.setStylePrimaryName("ode-SimpleMockComponent");

    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSize("100%", "100%");
    DOM.setStyleAttribute(verticalPanel.getElement(), "border", "1px solid #000000");
    verticalPanel.add(makeLabel(LABEL_DEVICE_NAME, "backgroundColor", "#C1E2E4"));
    HorizontalPanel header = new HorizontalPanel();
    DOM.setStyleAttribute(header.getElement(), "backgroundColor", "#309EA4");
    header.setSize("100%", "100%");
    header.add(new Label(LABEL_CONFIGURATION));
    configuration = new Label();
    DOM.setStyleAttribute(configuration.getElement(), "textAlign", "right");
    header.add(configuration);
    verticalPanel.add(header);
    verticalPanel.add(new Label(LABEL_WIFI_DIRECT));
    verticalPanel.add(new Label(LABEL_ROBOT_STATUS));
    verticalPanel.add(new Label(LABEL_OP_MODE));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label(LABEL_GAMEPAD_1));
    verticalPanel.add(new Label(LABEL_GAMEPAD_2));

    panelWidget.add(verticalPanel);
    initComponent(panelWidget);
  }

  private Label makeLabel(String text, String attributeName, String value) {
    Label label = new Label(text);
    DOM.setStyleAttribute(label.getElement(), attributeName, value);
    label.setWidth("100%");
    return label;
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_CONFIGURATION)) {
      configuration.setText(newValue);
      refreshForm();
    }
  }
}
