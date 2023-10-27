package com.google.appinventor.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;

public class UIStyle {

  @UiTemplate("style/GSoC/Ode.ui.xml")
  interface OdeUiBinderGSoC extends UiBinder<FlowPanel, Ode> {}

  @UiTemplate("style/GSoC/TopToolbar.ui.xml")
  interface TopToolbarUiBinderGSoC extends UiBinder<FlowPanel, TopToolbar> {}

  public static FlowPanel getOdeMain(Ode target) {
    OdeUiBinderGSoC ui_binder = GWT.create(OdeUiBinderGSoC.class);
    return ui_binder.createAndBindUi(target);
  }

  public static FlowPanel bindTopToolbar(TopToolbar target) {
    TopToolbarUiBinderGSoC ui_binder = GWT.create(TopToolbarUiBinderGSoC.class);
    return ui_binder.createAndBindUi(target);
  }
}