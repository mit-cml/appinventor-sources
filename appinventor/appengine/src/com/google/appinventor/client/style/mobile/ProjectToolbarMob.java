// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;


public class ProjectToolbarMob extends ProjectToolbar {

    interface ProjectToolbarUiBinderMob extends UiBinder<Toolbar, ProjectToolbarMob> {}
    private static final ProjectToolbarMob.ProjectToolbarUiBinderMob uibinder =
            GWT.create(ProjectToolbarMob.ProjectToolbarUiBinderMob.class);
    @UiField
    Label projectLabel;
    @UiField Label trashLabel;

    public void bindProjectToolbar() {
        populateToolbar(uibinder.createAndBindUi(this));
        super.projectLabel = projectLabel;
        super.trashLabel = trashLabel;
    }
}
