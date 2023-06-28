// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.junit.client.GWTTestCase;

public class ProjectEditorTest extends GWTTestCase {
    public void testProjectSettingsIcon() {
    //settings: {"SimpleSettings":{"AccentColor":"&HFFFF4081","ActionBar":"False","AppName":"p1","BlocksToolkit":"","DefaultFileScope":"App","Icon":"","PhonePreview":"Classic","PhoneTablet":"False","PrimaryColor":"&HFF3F51B5","PrimaryColorDark":"&HFF303F9F","ScreenCheckboxStateMap":"","ShowHiddenComponents":"False","ShowListsAsJson":"True","Sizing":"Responsive","Theme":"Classic","TutorialURL":"","UsesLocation":"False","VersionCode":"1","VersionName":"1.0"}}
    //ProjectEditor projectEditor, FileNode fileNode
    ProjectRootNode projectRootNode = new ProjectRootNode("p1", 656, "jjgh"){};
    Project project = new Project("p1");
    ProjectEditor projectEditor = new YaProjectEditor(projectRootNode);

    FileNode fileNode = new FileNode("filename", "fileid");
    YoungAndroidFormNode formNode = new YoungAndroidFormNode("filename");
    final YaFormEditor editor = new YaFormEditor(projectEditor, formNode);

    editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON, "");
  }

  public void testProjectScreenCheckBoxStateMap() {
    ProjectRootNode projectRootNode = new ProjectRootNode("p1", 656, "jjgh"){};
    ProjectEditor projectEditor = new ProjectEditor(projectRootNode) {

      @Override
      public void processProject() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processProject'");
      }

      @Override
      protected void onShow() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onShow'");
      }

      @Override
      protected void onHide() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onHide'");
      }
      
    };
    projectEditor.changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS, 
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SCREEN_CHECKBOX_STATE_MAP, 
          ""
        );

  }

  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient";
  }
}
