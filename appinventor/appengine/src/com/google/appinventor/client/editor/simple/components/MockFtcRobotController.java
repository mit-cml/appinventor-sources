// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
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

  // GWT panel widget used to mock a Simple FtcRobotController
  private final SimplePanel panelWidget;

  /**
   * Creates a new MockFtcRobotController component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockFtcRobotController(SimpleEditor editor) {
    super(editor, TYPE, images.ftc());

    // Initialize mock FtcRobotController UI
    panelWidget = new SimplePanel();
    panelWidget.setStylePrimaryName("ode-SimpleMockComponent");

    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setWidth("100%");
    verticalPanel.setHeight("100%");
    DOM.setStyleAttribute(verticalPanel.getElement(), "border", "1px solid #000000");
    verticalPanel.add(makeLabel("Device Name: ..........", "#C1E2E4"));
    verticalPanel.add(makeLabel("Active Configuration File: ..........", "#309EA4"));
    verticalPanel.add(new Label("Wifi Direct - .........."));
    verticalPanel.add(new Label("Robot Status: .........."));
    verticalPanel.add(new Label("Op Mode: .........."));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("-"));
    verticalPanel.add(new Label("Gamepad 1 .........."));
    verticalPanel.add(new Label("Gamepad 2 .........."));

    panelWidget.add(verticalPanel);
    initComponent(panelWidget);
  }

  private Label makeLabel(String text, String color) {
    Label label = new Label(text);
    DOM.setStyleAttribute(label.getElement(), "backgroundColor", color);
    return label;
  }
}
