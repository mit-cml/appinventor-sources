package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import java.util.logging.Logger;

public class TopToolbarGSoC extends TopToolbar {
  private static final Logger LOG = Logger.getLogger(TopToolbarGSoC.class.getName());
  interface TopToolbarUiBinderGSoC extends UiBinder<Toolbar, TopToolbarGSoC> {}
  private static final TopToolbarUiBinderGSoC UI_BINDER =
      GWT.create(TopToolbarUiBinderGSoC.class);

  @UiField DropDownButton fileDropDown;
  @UiField DropDownButton connectDropDown;
  @UiField DropDownButton buildDropDown;
  @UiField DropDownButton settingsDropDown;
  @UiField DropDownButton adminDropDown;
  @UiField (provided = true) Boolean hasWriteAccess;

  @Override
  public void bindUI() {
    // The boolean needs to be reversed here so it is true when items need to be visible.
    // UIBinder can't negate the boolean itself.
    LOG.info("bindUI GSoC");
    readOnly = Ode.getInstance().isReadOnly();
    hasWriteAccess = !readOnly;

    initWidget(UI_BINDER.createAndBindUi(this));
    super.fileDropDown = fileDropDown;
    super.connectDropDown = connectDropDown;
    super.buildDropDown = buildDropDown;
    super.settingsDropDown = settingsDropDown;
    super.adminDropDown = adminDropDown;
  }

}
