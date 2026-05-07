// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.widgets.properties.EditableProperty;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock for the non-visible Translator component. This needs a separate mock
 * from other non-visible components so that we can fetch the token property
 * from the server.
 *
 * Based on {@link MockCloudDB}.
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class MockTranslator extends MockNonVisibleComponent {

  public static final String TYPE = "Translator";
  private static final String PROPERTY_NAME_PROJECT_ID = "ProjectID";
  private static final String PROPERTY_NAME_APIKEY = "ApiKey";

  private boolean persistToken = false;

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   * @param editor
   * @param type
   * @param iconImage
   */
  public MockTranslator(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  /**
   * Initializes the "ProjectID" property dynamically.
   *
   * @param widget the iconImage for the MockCloudDB
   */
  @Override
  public final void initComponent(Widget widget) {
    super.initComponent(widget);
    DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    String projectID = "";
    if (currentProject != null) {
      projectID = currentProject.name;
    }

    changeProperty(PROPERTY_NAME_PROJECT_ID, projectID);
    getTokenFromServer();       // Get Token from the server
  }

  @Override
  public boolean isPropertyforYail(String propertyName) {
    if ((propertyName.equals(PROPERTY_NAME_PROJECT_ID)) ||
      (propertyName.equals(PROPERTY_NAME_APIKEY))) {
      return true;
    }
    return super.isPropertyforYail(propertyName);
  }

  @Override
  public boolean isPropertyPersisted(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_APIKEY)) {
      return persistToken;
    } else {
      return super.isPropertyPersisted(propertyName);
    }
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    if (propertyName.equals(PROPERTY_NAME_APIKEY)) {
      EditableProperty token = properties.getProperty(PROPERTY_NAME_APIKEY);
      int tokenType = token.getType();
      persistToken = false;
      tokenType |= EditableProperty.TYPE_NONPERSISTED;
      tokenType |= EditableProperty.TYPE_DOYAIL;
      token.setType(tokenType);
      getTokenFromServer();
    }
    super.onPropertyChange(propertyName, newValue);
  }


  private void getTokenFromServer() {
    Ode.getInstance().getTokenAuthService().getTranslateToken(new OdeAsyncCallback<String>() {
      @Override
      public void onSuccess(String token) {
        if (token == null) {
          onFailure(new UnsupportedOperationException(
              "Server is not configured to generate Translator tokens."));
          return;
        }
        EditableProperty tokenProperty = properties.getProperty(PROPERTY_NAME_APIKEY);
        if (tokenProperty == null) {
          return;
        }
        String existingToken = tokenProperty.getValue();
        if (!existingToken.isEmpty()) {
          return;             // If we have a value, don't over-write it
        }
        changeProperty(PROPERTY_NAME_APIKEY, token);
      }

      @Override
      public void onFailure(Throwable t) {
        changeProperty(PROPERTY_NAME_APIKEY, "ERROR : token not created");
        super.onFailure(t);
      }
    });
  }

}
