package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.UIStyleFactory;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;

public class UIFactoryGSoC extends UIStyleFactory {

  @Override
  public ProjectList createProjectList() {
    return new ProjectListGSoC();
  }

  @Override
  public ProjectFolder createProjectFolder(String name, long dateCreated, long dateModified, ProjectFolder parent) {
    return new ProjectFolderGSoC(name, dateCreated, dateModified, parent);
  }

  @Override
  public ProjectFolder createProjectFolder(String name, long dateCreated, ProjectFolder parent) {
    return new ProjectFolderGSoC(name, dateCreated, parent);
  }

  @Override
  public ProjectFolder createProjectFolder(JSONObject json, ProjectFolder parent) {
    return new ProjectFolderGSoC(json, parent);
  }

  @Override
  public NewYoungAndroidProjectWizard createNewYoungAndroidProjectWizard() {
    return new NewYoungAndroidProjectWizardGSoC();
  }
}


