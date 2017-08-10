// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.shared.rpc.cloudDB.CloudDBAuthServiceAsync;
import com.google.appinventor.shared.rpc.user.UserInfoServiceAsync;
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
  private static final String PROPERTY_NAME_ACCOUNT_NAME = "AccountName";
  private static final String PROPERTY_NAME_TOKEN = "Token";
  //private static final String PROPERTY_NAME_HUUID = "Huuid";

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
  public MockCloudDB(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  /**
   * Initializes the "ProjectID", "AccountName" properties dynamically.
   *
   * @param widget the iconImage for the MockCloudDB
   */
  @Override
  public final void initComponent(Widget widget) {
    super.initComponent(widget);
    String accName = Ode.getInstance().getUser().getUserEmail() + "";
    Ode.getInstance().getCloudDBAuthService().getToken(new OdeAsyncCallback<String>() {
      @Override
      public void onSuccess(String token) {
        changeProperty(PROPERTY_NAME_TOKEN,token);
      }
      @Override
      public void onFailure(Throwable t){
        changeProperty(PROPERTY_NAME_TOKEN,"ERROR : token not created");
        super.onFailure(t);
      }
    });
    /*Ode.getInstance().getUserInfoService().gethuuid(new OdeAsyncCallback<String>() {
      @Override
      public void onSuccess(String huuid) {
        changeProperty(PROPERTY_NAME_HUUID,huuid);
      }
      @Override
      public void onFailure(Throwable t){
        changeProperty(PROPERTY_NAME_HUUID,"ERROR : huuid could not be created");
        super.onFailure(t);
      }
    });*/
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
