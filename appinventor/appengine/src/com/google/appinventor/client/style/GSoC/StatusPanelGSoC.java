package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.StatusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

public class StatusPanelGSoC extends StatusPanel {
  interface StatusPanelUiBinderGSoC extends UiBinder<FlowPanel, StatusPanelGSoC> {}
  private static final StatusPanelUiBinderGSoC UI_BINDER = GWT.create(StatusPanelUiBinderGSoC.class);

  @UiField FlowPanel footer;
  @UiField Anchor tosLink;

  @Override
  public void bindUI() {
    StatusPanelUiBinderGSoC UI_BINDER = GWT.create(StatusPanelUiBinderGSoC.class);
    initWidget(UI_BINDER.createAndBindUi(this));
    super.footer = footer;
    super.tosLink = tosLink;
  }
}
