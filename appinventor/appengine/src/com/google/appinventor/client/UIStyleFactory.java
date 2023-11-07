package com.google.appinventor.client;

import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;

public class UIStyleFactory {

  @UiTemplate("Ode.ui.xml")
  public interface OdeUiBinder extends UiBinder<FlowPanel, Ode> {}
  @UiTemplate("style/GSoC/Ode.ui.xml")
  interface OdeUiBinderGSoC extends UiBinder<FlowPanel, Ode> {}

  @UiTemplate("TopToolbar.ui.xml")
  public interface TopToolbarUiBinderTest extends UiBinder<Toolbar, TopToolbar> {}

  public static FlowPanel createOde(Ode target, String style) {
    if (style == "modern") {
      OdeUiBinderGSoC ui_binder = GWT.create(OdeUiBinderGSoC.class);
      return ui_binder.createAndBindUi(target);
    }
    OdeUiBinder ui_binder = GWT.create(OdeUiBinder.class);
    return ui_binder.createAndBindUi(target);
  }

  public FlowPanel createTopToolbar(TopToolbar target) {
    TopToolbarUiBinderTest ui_binder =
        GWT.create(TopToolbarUiBinderTest.class);
    return ui_binder.createAndBindUi(target);
  }
}