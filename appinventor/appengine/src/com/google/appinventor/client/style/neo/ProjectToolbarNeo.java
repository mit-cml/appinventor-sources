// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

import java.util.logging.Logger;

public class ProjectToolbarNeo extends ProjectToolbar {
  private static final Logger LOG = Logger.getLogger(ProjectToolbarNeo.class.getName());
  interface ProjectToolbarUiBinderNeo extends UiBinder<Toolbar, ProjectToolbarNeo> {}
  private static final ProjectToolbarUiBinderNeo uibinder =
      GWT.create(ProjectToolbarUiBinderNeo.class);
  @UiField Label projectLabel;
  @UiField Label trashLabel;

  public void bindProjectToolbar() {
    populateToolbar(uibinder.createAndBindUi(this));
    super.projectLabel = projectLabel;
    super.trashLabel = trashLabel;
  }
}
