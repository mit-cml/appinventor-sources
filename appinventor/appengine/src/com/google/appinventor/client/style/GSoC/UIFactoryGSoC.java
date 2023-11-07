package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.UIStyleFactory;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;

public class UIFactoryGSoC extends UIStyleFactory {
  @UiTemplate("TopToolbarGSoC.ui.xml")
  public interface TopToolbarUiBinderTest extends UiBinder<Toolbar, TopToolbar> {}

  @Override
  public FlowPanel createTopToolbar(TopToolbar target) {
    TopToolbarUiBinderTest ui_binder =
        GWT.create(TopToolbarUiBinderTest.class);
    return ui_binder.createAndBindUi(target);
  }
}
