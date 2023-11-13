package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

import java.util.logging.Logger;

public class ProjectToolbarGSoC extends ProjectToolbar {
  private static final Logger LOG = Logger.getLogger(ProjectToolbarGSoC.class.getName());
  interface ProjectToolbarUiBinderGSoC extends UiBinder<Toolbar, ProjectToolbarGSoC> {}
  private static final ProjectToolbarUiBinderGSoC UI_BINDER =
      GWT.create(ProjectToolbarUiBinderGSoC.class);
  @UiField Label projectLabel;
  @UiField Label trashLabel;

  public void bindProjectToolbar() {
    populateToolbar(UI_BINDER.createAndBindUi(this));
    super.projectLabel = projectLabel;
    super.trashLabel = trashLabel;
  }
}
