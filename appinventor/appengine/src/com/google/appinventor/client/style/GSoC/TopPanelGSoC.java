package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.TopPanel;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class TopPanelGSoC extends TopPanel {

  @UiTemplate("TopPanelGSoC.ui.xml")
  interface TopPanelUiBinderGSoC extends UiBinder<FlowPanel, TopPanelGSoC> {}

  private static final TopPanelUiBinderGSoC UI_BINDER = GWT.create(TopPanelUiBinderGSoC.class);

  @UiField TopToolbarGSoC topToolbar;
  @UiField ImageElement logo;
  @UiField Label readOnly;
  @UiField FlowPanel rightPanel;
  @UiField
  DropDownButton languageDropDown;
  @UiField
  DropDownButton accountButton;
  @UiField
  DropDownItem deleteAccountItem;
  @UiField
  FlowPanel links;

  @Override
  public void bindUI() {
    initWidget(UI_BINDER.createAndBindUi(this));
    super.topToolbar = topToolbar;
    super.logo = logo;
    super.readOnly = readOnly;
    super.rightPanel = rightPanel;
    super.languageDropDown = languageDropDown;
    super.accountButton = accountButton;
    super.deleteAccountItem = deleteAccountItem;
    super.links = links;
  }
}
