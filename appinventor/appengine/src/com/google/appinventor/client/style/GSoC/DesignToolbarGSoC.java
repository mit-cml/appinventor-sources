package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.widgets.ToolbarItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

import java.util.logging.Logger;

public class DesignToolbarGSoC extends DesignToolbar {
  private static final Logger LOG = Logger.getLogger(DesignToolbarGSoC.class.getName());
  interface DesignToolbarUiBinderGSoC extends UiBinder<Toolbar, DesignToolbarGSoC> {}

  @UiField protected DropDownButton pickFormItem;
  @UiField protected ToolbarItem addFormItem;
  @UiField protected ToolbarItem removeFormItem;
  @UiField protected ToolbarItem switchToDesign;
  @UiField protected ToolbarItem switchToBlocks;
  @UiField protected ToolbarItem sendToGalleryItem;
  @UiField protected ToolbarItem projectPropertiesDialog;


  @Override
  public void bindUI() {
    DesignToolbarUiBinderGSoC UI_BINDER = GWT.create(DesignToolbarUiBinderGSoC.class);
    populateToolbar(UI_BINDER.createAndBindUi(this));
    super.pickFormItem = pickFormItem;
    super.addFormItem = addFormItem;
    super.removeFormItem = removeFormItem;
    super.switchToDesign = switchToDesign;
    super.switchToBlocks = switchToBlocks;
  }
}
