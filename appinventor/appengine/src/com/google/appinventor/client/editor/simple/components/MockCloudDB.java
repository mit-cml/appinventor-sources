// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.properties.EditableProperty;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock for the non-visible CloudDB component. This needs a separate mock
 * from other non-visible components so that some of its properties can be
 * given dynamic default values.
 *
 * @author natalie@csail.mit.edu (Natalie Lao)
 */
public class MockCloudDB extends MockNonVisibleComponent {

  public static final String TYPE = "CloudDB";
  private static final String PROPERTY_NAME_PROJECT_ID = "ProjectID";
  private static final String PROPERTY_NAME_TOKEN = "Token";
  private static final String PROPERTY_NAME_REDIS_SERVER = "RedisServer";
  private static final String PROPERTY_NAME_DEFAULT_REDISSERVER = "DefaultRedisServer";

  private boolean persistToken = false;

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   * @param editor
   * @param type
   * @param iconImage
   */
  public MockCloudDB(SimpleEditor editor, String type, Image iconImage) {
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
    String defaultRedisServer = Ode.getInstance().getSystemConfig().getDefaultCloudDBserver();
    changeProperty(PROPERTY_NAME_DEFAULT_REDISSERVER, defaultRedisServer);
    getTokenFromServer();       // Get Token from the server
  }

  @Override
  public boolean isPropertyforYail(String propertyName) {
    if ((propertyName.equals(PROPERTY_NAME_PROJECT_ID)) ||
      (propertyName.equals(PROPERTY_NAME_DEFAULT_REDISSERVER)) ||
      (propertyName.equals(PROPERTY_NAME_TOKEN))) {
      return true;
    }
    return super.isPropertyforYail(propertyName);
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    return !propertyName.equals(PROPERTY_NAME_DEFAULT_REDISSERVER)
      && super.isPropertyVisible(propertyName);
  }

  @Override
  public boolean isPropertyPersisted(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_DEFAULT_REDISSERVER)) {
      return false;             // We don't persist the default server as it is really
                                // a property of the service, not the project per se
    } else if (propertyName.equals(PROPERTY_NAME_TOKEN)) {
      return persistToken;
    } else {
      return super.isPropertyPersisted(propertyName);
    }
  }

  // We provide our own onPropertyChange to catch the case where the
  // RedisServer is changed to/from the DEFAULT value.  This effects
  // the persistence of the Token property. If we are using a private
  // redis server, we want to persist the Token. Otherwise we do not.

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    if (propertyName.equals(PROPERTY_NAME_REDIS_SERVER)) {
      // If this is the default server, then make the Token property
      // non-persistent, but output it in YAIL
      EditableProperty token = properties.getProperty(PROPERTY_NAME_TOKEN);
      if (token == null) {      // First pass through and "Token" isn't set yet
        super.onPropertyChange(propertyName, newValue);
        return;
      }
      int tokenType = token.getType();
      if (newValue.equals("DEFAULT")) {
        if (token.getValue().isEmpty() || !(token.getValue().substring(0, 1).equals("%"))) {
          token.setValue("");   // Set it to empty so getTokenFromServer will fill in
          persistToken = false;
          tokenType |= EditableProperty.TYPE_NONPERSISTED;
          tokenType |= EditableProperty.TYPE_DOYAIL;
          getTokenFromServer(); // will fill it in.
        } else {
          tokenType &= ~EditableProperty.TYPE_NONPERSISTED;
          persistToken = true;
        }
      } else {
        tokenType &= ~EditableProperty.TYPE_NONPERSISTED;
        persistToken = true;
      }
      token.setType(tokenType);
      onPropertyChange(PROPERTY_NAME_TOKEN, token.getValue());
    } else if (propertyName.equals(PROPERTY_NAME_TOKEN)) {
      EditableProperty serverProperty = properties.getProperty(PROPERTY_NAME_REDIS_SERVER);
      EditableProperty token = properties.getProperty(PROPERTY_NAME_TOKEN);
      if (token == null) {      // First pass through and "Token" isn't set yet
        super.onPropertyChange(propertyName, newValue);
        return;
      }
      int tokenType = token.getType();
      // if the Redis Server property is "DEFAULT" we don't persist the
      // Token property
      if (serverProperty == null) { // Nothing we can do here
        super.onPropertyChange(propertyName, newValue);
        return;
      }
      String server = serverProperty.getValue();
      if (server.equals("DEFAULT")) {
        if (newValue == null || newValue.isEmpty() ||
          !(newValue.substring(0, 1).equals("%"))) {
          persistToken = false; // Now that the auto-save is scheduled, we no longer want
                                // to persist the token
          tokenType |= EditableProperty.TYPE_NONPERSISTED;
          tokenType |= EditableProperty.TYPE_DOYAIL;
        } else {
          tokenType &= ~EditableProperty.TYPE_NONPERSISTED;
          persistToken = true;
        }
      } else {
        tokenType &= ~EditableProperty.TYPE_NONPERSISTED;
        persistToken = true;
      }
      token.setType(tokenType);
    }
    super.onPropertyChange(propertyName, newValue);
  }

  private void getTokenFromServer() {
    Ode.getInstance().getTokenAuthService().getCloudDBToken(new OdeAsyncCallback<String>() {
      @Override
      public void onSuccess(String token) {
        if (token == null) {
          onFailure(new UnsupportedOperationException(
              "Server is not configured to generate CloudDB tokens."));
          return;
        }
        EditableProperty tokenProperty = properties.getProperty(PROPERTY_NAME_TOKEN);
        if (tokenProperty == null) {
          return;
        }
        String existingToken = tokenProperty.getValue();
        if (!existingToken.isEmpty()) {
          return;             // If we have a value, don't over-write it
        }
        changeProperty(PROPERTY_NAME_TOKEN, token);
      }

      @Override
      public void onFailure(Throwable t) {
        changeProperty(PROPERTY_NAME_TOKEN, "ERROR : token not created");
        super.onFailure(t);
      }
    });
  }

}
