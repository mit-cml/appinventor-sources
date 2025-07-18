//<!-- Copyright 2025 MIT, All rights reserved -->
//<!-- Released under the Apache License, Version 2.0 -->
//<!-- http://www.apache.org/licenses/LICENSE-2.0 -->

package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.json.client.JSONObject;


public class UiFactoryMobile extends UiStyleFactory {

    @Override
    public ProjectList createProjectList() {
        return new ProjectListMob();
    }

    @Override
    public ProjectFolder createProjectFolder(String name, long dateCreated, long dateModified, ProjectFolder parent) {
        return new ProjectFolderMob(name, dateCreated, dateModified, parent);
    }

    @Override
    public ProjectFolder createProjectFolder(String name, long dateCreated, ProjectFolder parent) {
        return new ProjectFolderMob(name, dateCreated, parent);
    }

    @Override
    public ProjectFolder createProjectFolder(JSONObject json, ProjectFolder parent) {
        return new ProjectFolderMob(json, parent, this);
    }


}
