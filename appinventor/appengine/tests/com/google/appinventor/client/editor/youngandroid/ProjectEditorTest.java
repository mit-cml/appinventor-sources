// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid;

//import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
//import org.mockito.Spy;
//import org.mockito.Mock;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.client.settings.user.UserSettings;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.SettingsConstants;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ YaFormEditor.class })
public class ProjectEditorTest extends TestCase {

  /*@Before
    public void setUpBeforeClass() throws Exception {
        Spy(YaProjectEditor.class);
        Mockito.spy();
    }*/

    @Test
    public void testProjectSettingsIcon() throws Exception {
        YaFormEditor YaFormEditorMock = PowerMock.createMock(YaFormEditor.class);
        YaFormEditorMock.getProjectEditor().changeProjectSettingsProperty(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON, "");
        /*YaFormEditor mockObject = Mockito.mock(YaFormEditor.class);
        mockObject.getProjectEditor().changeProjectSettingsProperty(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON, "");*/
        //settings: {"SimpleSettings":{"AccentColor":"&HFFFF4081","ActionBar":"False","AppName":"p1","BlocksToolkit":"","DefaultFileScope":"App","Icon":"","PhonePreview":"Classic","PhoneTablet":"False","PrimaryColor":"&HFF3F51B5","PrimaryColorDark":"&HFF303F9F","ScreenCheckboxStateMap":"","ShowHiddenComponents":"False","ShowListsAsJson":"True","Sizing":"Responsive","Theme":"Classic","TutorialURL":"","UsesLocation":"False","VersionCode":"1","VersionName":"1.0"}}

    }

  /*@Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient_dev";
  }*/
}