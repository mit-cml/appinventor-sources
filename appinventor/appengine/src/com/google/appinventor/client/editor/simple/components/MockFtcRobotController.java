// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
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

  private static final String STYLE_BORDER = "border";
  private static final String STYLE_BACKGROUND_COLOR = "backgroundColor";
  private static final String STYLE_TEXT_COLOR = "color";
  private static final String STYLE_TEXT_ALIGN = "textAlign";
  private static final String STYLE_FONT_SIZE = "fontSize";

  private static final String COLOR_TRANSPARENT= "transparent";
  private static final String COLOR_BLACK = "#000000";
  private static final String COLOR_WHITE = "#ffffff";
  private static final String COLOR_MEDIUM_RED = "#4e0106";

  private static final String TEXT_ALIGN_RIGHT = "right";

  private static final String LABEL_DEVICE_NAME = "<Device>";
  private static final String LABEL_CONFIGURATION = "Active Configuration File:";
  private static final String LABEL_WIFI_DIRECT = "Wifi Direct - enabled";
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
    DOM.setStyleAttribute(verticalPanel.getElement(), STYLE_BORDER, "1px solid" + COLOR_BLACK);

    // Black bar on top
    HorizontalPanel topBar = new HorizontalPanel();
    DOM.setStyleAttribute(topBar.getElement(), STYLE_BACKGROUND_COLOR, COLOR_BLACK);
    topBar.setSize("100%", "80px");
    Image robotIcon = new Image(images.robotControllerIcon());
    DOM.setStyleAttribute(robotIcon.getElement(), STYLE_BORDER, "5px solid" + COLOR_BLACK);
    robotIcon.setSize("40px", "70px");
    topBar.add(robotIcon);
    Label label = new Label(LABEL_DEVICE_NAME);
    DOM.setStyleAttribute(label.getElement(), STYLE_FONT_SIZE, "16px");
    DOM.setStyleAttribute(label.getElement(), STYLE_TEXT_COLOR, COLOR_WHITE);
    label.setSize("222px", "100%");
    topBar.add(label);
    Image menuButtons = new Image(images.robotControllerMenu());
    DOM.setStyleAttribute(menuButtons.getElement(), STYLE_BORDER, "15px solid" + COLOR_BLACK);
    menuButtons.setSize("8px", "50px");
    topBar.add(menuButtons);
    verticalPanel.add(topBar);

    HorizontalPanel header = new HorizontalPanel();
    DOM.setStyleAttribute(header.getElement(), STYLE_BACKGROUND_COLOR, COLOR_MEDIUM_RED);
    header.setSize("100%", "100%");
    label = new Label(LABEL_CONFIGURATION);
    DOM.setStyleAttribute(label.getElement(), STYLE_FONT_SIZE, "15px");
    DOM.setStyleAttribute(label.getElement(), STYLE_TEXT_COLOR, COLOR_WHITE);
    header.add(label);
    configuration = new Label();
    DOM.setStyleAttribute(configuration.getElement(), STYLE_BORDER, "3px solid transparent");
    DOM.setStyleAttribute(configuration.getElement(), STYLE_TEXT_COLOR, COLOR_WHITE);
    DOM.setStyleAttribute(configuration.getElement(), STYLE_TEXT_ALIGN, TEXT_ALIGN_RIGHT);
    header.add(configuration);
    verticalPanel.add(header);

    verticalPanel.add(new Label(LABEL_WIFI_DIRECT));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label(LABEL_ROBOT_STATUS));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label(LABEL_OP_MODE));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label(LABEL_GAMEPAD_1));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label(LABEL_GAMEPAD_2));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("-"));

    panelWidget.add(verticalPanel);
    initComponent(panelWidget);
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
