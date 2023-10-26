package com.google.appinventor.client;

import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.wizards.Dialog;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;

public class UIStyle {

  @UiTemplate("style/GSoC/Ode.ui.xml")
  interface OdeUiBinderGSoC extends UiBinder<FlowPanel, Ode> {}

  @UiTemplate("style/GSoC/TopToolbar.ui.xml")
  interface TopToolbarUiBinderGSoC extends UiBinder<FlowPanel, TopToolbar> {}

  @UiTemplate("style/GSoC/ProjectToolbar.ui.xml")
  interface ProjectToolbarUiBinderGSoC extends UiBinder<Toolbar, ProjectToolbar> {}

  @UiTemplate("style/GSoC/NewYoungAndroidProjectWizard.ui.xml")
  public interface NewYoungAndroidProjectWizardUiBinderGSoC extends UiBinder<Dialog, NewYoungAndroidProjectWizard> {}

  public static FlowPanel getOdeMain(Ode target) {
    OdeUiBinderGSoC ui_binder = GWT.create(OdeUiBinderGSoC.class);
    return ui_binder.createAndBindUi(target);
  }

  public static FlowPanel bindTopToolbar(TopToolbar target) {
    TopToolbarUiBinderGSoC ui_binder = GWT.create(TopToolbarUiBinderGSoC.class);
    return ui_binder.createAndBindUi(target);
  }

  public static void bindNewProjectWizard(NewYoungAndroidProjectWizard target) {
    NewYoungAndroidProjectWizardUiBinderGSoC UI_BINDER =
        GWT.create(NewYoungAndroidProjectWizardUiBinderGSoC.class);
    UI_BINDER.createAndBindUi(target);
  }

  public static Toolbar bindProjectToobar(ProjectToolbar target) {
    ProjectToolbarUiBinderGSoC UI_BINDER =
      GWT.create(ProjectToolbarUiBinderGSoC.class);
    return UI_BINDER.createAndBindUi(target);
  }
}