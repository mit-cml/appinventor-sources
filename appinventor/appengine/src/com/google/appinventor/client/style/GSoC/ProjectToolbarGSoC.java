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
  @UiField public Label projectLabel;
  @UiField public Label trashLabel;

  public void bindProjectToolbar() {
    populateToolbar(UI_BINDER.createAndBindUi(this));
    LOG.info("Populated Toolbar");
  }

  public void projectLabel_setVisible(boolean visible) {
    LOG.info("ProjectToolbar GSoC projectLabel visible " + visible);
    projectLabel.setVisible(visible);
  }

  public void trashLabel_setVisible(boolean visible) {
    LOG.info("ProjectToolbar GSoC trashlabel visible " + visible);
    trashLabel.setVisible(visible);
  }
//  public void setButtonVisible(String widgetName, boolean visible) {
//    LOG.info("ProjectToolbar GSoC setButtonVisible " + widgetName + " " + visible);
//    setButtonVisible(widgetName, visible);
//  }
//
//  @Override
//  public void setButtonEnabled(String widgetName, boolean enabled) {
//    LOG.info("ProjectToolbar GSoC setButtonEnabled " + widgetName + " " + enabled);
//    setButtonEnabled(widgetName, enabled);
//  }
}
