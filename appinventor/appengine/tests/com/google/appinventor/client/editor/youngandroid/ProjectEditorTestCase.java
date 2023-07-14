// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.client.settings.user.UserSettings;
import com.google.appinventor.client.explorer.project.Project;
import com.google.gwt.junit.client.GWTTestCase;

public class ProjectEditorTestCase extends GWTTestCase{
  public void testProjectSettingsIcon() throws Exception {
    //settings: {"SimpleSettings":{"AccentColor":"&HFFFF4081","ActionBar":"False","AppName":"p1","BlocksToolkit":"","DefaultFileScope":"App","Icon":"","PhonePreview":"Classic","PhoneTablet":"False","PrimaryColor":"&HFF3F51B5","PrimaryColorDark":"&HFF303F9F","ScreenCheckboxStateMap":"","ShowHiddenComponents":"False","ShowListsAsJson":"True","Sizing":"Responsive","Theme":"Classic","TutorialURL":"","UsesLocation":"False","VersionCode":"1","VersionName":"1.0"}}
    
    Ode ode = new Ode();
    ode.onModuleLoad();
    ode.getProjectManager().addProject(new UserProject(6565, "name", "YoungAndroid", 0, 0, false));

    YoungAndroidProjectNode projectNode = new YoungAndroidProjectNode("name", 6565);
    YoungAndroidFormNode formNode = new YoungAndroidFormNode("src/appinventor/ai_test/Screen1.scm");
    projectNode.addChild(formNode); 

    YaProjectEditor projectEditor = new YaProjectEditor(projectNode);
    YaFormEditor editor = new YaFormEditor(projectEditor, formNode);
    
    editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON, "");

  }

  public void testProjectScreenCheckBoxStateMap() {
    //ProjectRootNode projectNode = new ProjectRootNode("p1", 656, "jjgh"){};
    Ode ode = new Ode();
    ode.onModuleLoad();
    ode.getProjectManager().addProject(new UserProject(6565, "name", "YoungAndroid", 0, 0, false));
    YoungAndroidProjectNode projectNode = new YoungAndroidProjectNode("name", 6565);
    YaProjectEditor projectEditor = new YaProjectEditor(projectNode);
    projectEditor.changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS, 
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SCREEN_CHECKBOX_STATE_MAP, 
          ""
        );

  }

  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient_dev";
  }
}
