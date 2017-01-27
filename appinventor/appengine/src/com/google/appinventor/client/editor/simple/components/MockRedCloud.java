// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.components.FirebaseAuthService;
import com.google.appinventor.shared.rpc.components.FirebaseAuthServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;



/**
 * Mock for the non-visible RedCloud component. This needs a separate mock
 * from other non-visible components so that some of its properties can be
 * given dynamic default values.
 *
 * @author natalie@csail.mit.edu (Natalie Lao)
 */
public class MockRedCloud extends MockNonVisibleComponent {

  public static final String TYPE = "RedCloud";
  private static final String PROPERTY_NAME_PROJECT_ID = "ProjectID";
  private static final String PROPERTY_NAME_ACCOUNT_NAME = "AccountName";

  //Persist feature to be implemented
  private boolean persistToken = false;

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   * @param editor
   * @param type
   * @param iconImage
   */
  public MockRedCloud(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  /**
   * Initializes the "ProjectID", "AccountName" properties dynamically.
   *
   * @param widget the iconImage for the MockRedCloud
   */
  @Override
  public final void initComponent(Widget widget) {
    super.initComponent(widget);

    String accName = Ode.getInstance().getUser().getUserEmail() + "";
    DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    String projectID = "";
    if (currentProject != null) {
      projectID = currentProject.name;
    }

    changeProperty(PROPERTY_NAME_PROJECT_ID, projectID);
    changeProperty(PROPERTY_NAME_ACCOUNT_NAME, accName);
  }

  @Override
  public boolean isPropertyforYail(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_ACCOUNT_NAME) ||
      (propertyName.equals(PROPERTY_NAME_PROJECT_ID))) {
      return true;
    }
    return super.isPropertyforYail(propertyName);
  }

}
