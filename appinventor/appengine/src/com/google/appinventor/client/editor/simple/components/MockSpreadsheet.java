// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock for the non-visible Spreadsheet component. This needs a separate mock
 * to set APPLICATION_NAME to the name of the current App Inventor project.
 *
 * Cribbed from MockFusionTablesControl.java
 *
 * @author theng@mit.edu (Tommy S. Heng)
 */
public class MockSpreadsheet extends MockNonVisibleComponent {
  public static final String TYPE = "Spreadsheet";

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   * @param editor
   * @param type
   * @param iconImage
   */
  public MockSpreadsheet(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  /**
   * Sets the APPLICATION_NAME property to the project name
   */
  @Override
  public final void initComponent (Widget widget) {
    super.initComponent(widget);

    // Retrieve the current project name
    DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    String projectName = "MIT App Inventor";
    if (currentProject != null) {
      projectName = currentProject.name;
    }

    changeProperty("ApplicationName", projectName);
  }
}
