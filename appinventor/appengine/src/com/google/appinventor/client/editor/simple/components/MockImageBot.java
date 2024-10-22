// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.utils.MessageDialog;
import com.google.appinventor.client.widgets.properties.EditableProperty;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mock for the non-visible ImageBot component. This needs a separate mock
 * from other non-visible components so that we can fetch the token property
 * from the server.
 *
 * Based on {@link MockChatBot}.
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class MockImageBot extends MockNonVisibleComponent {
  private static final Logger LOG = Logger.getLogger(MockImageBot.class.getName());
  public static final String TYPE = "ImageBot";

  private static final String PROPERTY_NAME_TOKEN = "Token";
  private static boolean warningGiven = false; // Whether or not we have given experimental warning

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   * @param editor
   * @param type
   * @param iconImage
   */
  public MockImageBot(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  /**
   * Get a ChatBot auth token from the server
   *
   * @param widget the iconImage for the MockImageBot
   */
  @Override
  public final void initComponent(Widget widget) {
    super.initComponent(widget);
    getTokenFromServer();       // Get Token from the server
  }

  @Override
  public boolean isPropertyforYail(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_TOKEN)) {
      return true;
    }
    return super.isPropertyforYail(propertyName);
  }

  /**
   * Called when the component is dropped in the Designer window
   * we give a warning that ImageBot is still experimental.
   */

  @Override
  public void onCreateFromPalette() {
    if (!warningGiven) {
      warningGiven = true;
      MessageDialog.messageDialog(MESSAGES.warningDialogTitle(),
        MESSAGES.imageBotExperimentalWarning(),
        MESSAGES.okButton(), null, null);
    }
  }

  /**
   * onPropertyChange: If the property we are changing is the token, then
   * check to see if it begins with a "%" in which case we alter the type
   * to be TYPE_NONPERSISTED.
   */
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    if (propertyName.equals(PROPERTY_NAME_TOKEN)) {
      EditableProperty token = properties.getProperty(PROPERTY_NAME_TOKEN);
      int tokenType = token.getType();
      if (newValue == null || newValue.isEmpty()) {
        tokenType |= EditableProperty.TYPE_NONPERSISTED;
        tokenType |= EditableProperty.TYPE_DOYAIL;
        token.setType(tokenType);
        getTokenFromServer();
        return;                 // Callback from getTokenFromServer finishes up
      } else if (newValue.charAt(0) == '%') {
        tokenType &= ~EditableProperty.TYPE_NONPERSISTED;
      }
      token.setType(tokenType);
    }
    super.onPropertyChange(propertyName, newValue);
  }

  /*
   * Get the token from the server. NOTE: We use the same back end call as ChatBot,
   * because the tokens are identical. Someday this may change, but not today!
   */
  private void getTokenFromServer() {
    LOG.info("getTokenFromServer Called");
    Ode.getInstance().getTokenAuthService().getChatBotToken(new OdeAsyncCallback<String>() {
      @Override
      public void onSuccess(String token) {
        if (token == null) {
          onFailure(new UnsupportedOperationException(
              "Server is not configured to generate ImageBot tokens."));
          return;
        }
        EditableProperty tokenProperty = properties.getProperty(PROPERTY_NAME_TOKEN);
        if (tokenProperty == null) {
          return;
        }
        String existingToken = tokenProperty.getValue();
        if (!existingToken.isEmpty()) {
          LOG.info("bailing on getTokenFromServer existingToken = " + existingToken);
          return;             // If we have a value, don't over-write it
        }
        int tokenType = tokenProperty.getType();
        tokenType |= EditableProperty.TYPE_NONPERSISTED;
        tokenType |= EditableProperty.TYPE_DOYAIL;
        tokenProperty.setType(tokenType);
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
