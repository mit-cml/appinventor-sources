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
import com.google.appinventor.client.utils.MessageDialog;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.components.FirebaseAuthService;
import com.google.appinventor.shared.rpc.components.FirebaseAuthServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;



/**
 * Mock for the non-visible FirebaseDB component. This needs a separate mock
 * from other non-visible components so that some of its properties can be
 * given dynamic default values.
 *
 * @author will2596@gmail.com (William Byrne)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class MockFirebaseDB extends MockNonVisibleComponent {

  public static final String TYPE = "FirebaseDB";
  private static final String PROPERTY_NAME_DEVELOPER_BUCKET = "DeveloperBucket";
  private static final String PROPERTY_NAME_PROJECT_BUCKET = "ProjectBucket";
  private static final String PROPERTY_NAME_FIREBASE_TOKEN = "FirebaseToken";
  private static final String PROPERTY_NAME_FIREBASE_URL = "FirebaseURL";
  private static final String PROPERTY_NAME_DEFAULT_URL = "DefaultURL";
  private static final FirebaseAuthServiceAsync AUTH_SVC = GWT.create(FirebaseAuthService.class);
  private static boolean warningGiven = false; // Whether or not we have given experimental warning

  private boolean persistToken = false;

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   * @param editor
   * @param type
   * @param iconImage
   */
  public MockFirebaseDB(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  /**
   * Initializes the "ProjectBucket", "DeveloperBucket", "FirebaseToken"
   * properties dynamically.
   *
   * @param widget the iconImage for the MockFirebaseDB
   */
  @Override
  public final void initComponent(Widget widget) {
    super.initComponent(widget);

    String devBucket = Ode.getInstance().getUser().getUserEmail().replace(".", ":") + "";
    DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    String projectName = "";
    if (currentProject != null) {
      projectName = currentProject.name;
    }

    // We use the "super" for the developer bucket here because we
    // override the changeProperty method to ignore setting the developer
    // bucket. The idea here is to set it here to a value based on the
    // person's userid (email address) but fail to load whatever value
    // is in the project file. By doing this people can put projects
    // in the Gallery which use the FirebaseDB component and have the
    // developer bucket get changed to the current user when they download
    // the project from the Gallery.
    super.changeProperty(PROPERTY_NAME_DEVELOPER_BUCKET, devBucket + "/");
    changeProperty(PROPERTY_NAME_PROJECT_BUCKET, projectName);

    // The default URL is loaded from the system config which in turn is
    // is loaded from the "Flag" module which reads properties from
    // appengine-web.xml (in the App Engine version). The standalone version
    // stores it in appinventor.xml (at least for now)
    String defaultURL = Ode.getInstance().getSystemConfig().getFirebaseURL();
    changeProperty(PROPERTY_NAME_DEFAULT_URL, defaultURL);
    OdeLog.log("Default Firebase URL = " + defaultURL);

    AsyncCallback<String> callback = new AsyncCallback<String>() {
      @Override
      public void onSuccess(String JWT) {
        String oldJWT = getPropertyValue(PROPERTY_NAME_FIREBASE_TOKEN);
        String FirebaseURL = getPropertyValue(PROPERTY_NAME_FIREBASE_URL);
        // Only set a new token if one wasn't already loaded OR if
        // we are using the default FirebaseURL in which case the new
        // token will always be correct, potentially replacing a bad
        // value if a project is imported that originally was exported
        // by a different user
        //
        // Normally this code is invoked early in component creation
        // default values are then set here. If a project is being
        // loaded, the properties saved in the project file will
        // be loaded later. However in this case, we are in a
        // callback which is often invoked *after* the project has
        // been loaded. We don't want to over-write a user supplied
        // token, so we only store then one we fetched if there isn't
        // one already there. This is a bit of a kludge, but I haven't
        // figured out a better way without making larger scale
        // alterations to Mock instantiation --Jeff
        if (oldJWT.equals("") || FirebaseURL.equals("DEFAULT")) {
          changeProperty(PROPERTY_NAME_FIREBASE_TOKEN, JWT);
          onPropertyChange(PROPERTY_NAME_FIREBASE_TOKEN, JWT);
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        OdeLog.elog("Failed to create FirebaseDB JWT!");
      }
    };

    AUTH_SVC.getToken(projectName, callback);
  }

  /**
   * Called when the component is dropped in the Designer window
   * we give a warning that firebase is still experimental.
   */

  @Override
  public void onCreateFromPalette() {
    if (!warningGiven) {
      warningGiven = true;
      MessageDialog.messageDialog(MESSAGES.warningDialogTitle(),
        MESSAGES.firebaseExperimentalWarning(),
        MESSAGES.okButton(), null, null);
    }
  }

  /**
   * Enforces the invisibility of the "DeveloperBucket" and "FirebaseToken"
   * properties.
   *
   * @param  propertyName the name of the property to check
   * @return true for a visible property, false for an invisible property
   */
  @Override
  protected boolean isPropertyVisible(String propertyName) {
    return !propertyName.equals(PROPERTY_NAME_DEVELOPER_BUCKET)
      && !propertyName.equals(PROPERTY_NAME_DEFAULT_URL)
      && super.isPropertyVisible(propertyName);
  }

  /**
   * Changes the value of a component property.
   * This version ignores the developer bucket. So its value
   * is *not* loaded from the project
   *
   * @param name  property name
   * @param value  new property value
   *
   */
  @Override
  public void changeProperty(String name, String value) {
    if (name.equals(PROPERTY_NAME_DEVELOPER_BUCKET)) {
      return;
    } else if (name.equals(PROPERTY_NAME_FIREBASE_URL)) {
      onPropertyChange(name, value);
    }
    super.changeProperty(name, value);
  }

  // We provide our own onPropertyChange to catch the case
  // where the FirebaseURL is changed to/from the DEFAULT value.
  // This effects the persistability (is that a word?) of the FirebaseToken
  // property. If we are using a private Firebase Account, we want to persis
  // the FirebaseToken. Otherwise we do not.
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    if (propertyName.equals(PROPERTY_NAME_FIREBASE_URL)) {
      // If this is the DEFAULT URL, then make the FirebaseToken property
      // non-persistant, but output it in YAIL
      EditableProperty firebaseToken = properties.getProperty(PROPERTY_NAME_FIREBASE_TOKEN);
      int tokenType = firebaseToken.getType();
      if (newValue.equals("DEFAULT")) {
        persistToken = false;
        tokenType |= EditableProperty.TYPE_NONPERSISTED;
        tokenType |= EditableProperty.TYPE_DOYAIL;
      } else {
        tokenType &= ~EditableProperty.TYPE_NONPERSISTED;
        persistToken = true;
      }
      firebaseToken.setType(tokenType);
      // Need to fire this change to mark the form dirty when we change the type
      // of the property
      onPropertyChange(PROPERTY_NAME_FIREBASE_TOKEN, firebaseToken.getValue());
    }
    super.onPropertyChange(propertyName, newValue);
  }

  /**
   * Arranges that the Developer Bucket property is not persisted.
   * We only use this property when the Firebase URL is set to its default
   * value. In this case the developer bucket MUST be set to an value
   * which is based on the current logged in use. Only this user can fetch
   * a Firebase token which will grant access to the objects in the
   * developer bucket.
   *
   * If the user sets up their own Firebase account, this property is
   * not used at all.
   *
   * We also do not persist the FirebaseToken iff we are using the
   * default Firebase account.
   *
   */
  @Override
  public boolean isPropertyPersisted(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_DEVELOPER_BUCKET) ||
      propertyName.equals(PROPERTY_NAME_DEFAULT_URL)) {
      return false;
    } else if (propertyName.equals(PROPERTY_NAME_FIREBASE_TOKEN)) {
      // We keep track of whether or not to persist the FirebaseToken property
      return persistToken;
    } else {
      return super.isPropertyPersisted(propertyName);
    }
  }

  @Override
  public boolean isPropertyforYail(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_DEVELOPER_BUCKET) ||
      (propertyName.equals(PROPERTY_NAME_FIREBASE_TOKEN)) ||
      (propertyName.equals(PROPERTY_NAME_DEFAULT_URL))) {
      return true;
    }
    return super.isPropertyforYail(propertyName);
  }

}
