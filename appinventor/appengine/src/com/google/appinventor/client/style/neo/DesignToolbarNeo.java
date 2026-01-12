// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.widgets.ToolbarItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import java.util.logging.Logger;

public class DesignToolbarNeo extends DesignToolbar {
  private static final Logger LOG = Logger.getLogger(DesignToolbarNeo.class.getName());
  interface DesignToolbarUiBinderNeo extends UiBinder<Toolbar, DesignToolbarNeo> {}

  @UiField protected DropDownButton pickFormItem;
  @UiField protected ToolbarItem addFormItem;
  @UiField protected ToolbarItem removeFormItem;
  @UiField protected ToolbarItem switchToDesign;
  @UiField protected ToolbarItem switchToBlocks;
  @UiField protected ToolbarItem sendToGalleryItem;

  @Override
  public void bindUI() {
    DesignToolbarUiBinderNeo uibinder = GWT.create(DesignToolbarUiBinderNeo.class);
    populateToolbar(uibinder.createAndBindUi(this));
    super.pickFormItem = pickFormItem;
    super.addFormItem = addFormItem;
    super.removeFormItem = removeFormItem;
    super.switchToDesign = switchToDesign;
    super.switchToBlocks = switchToBlocks;
    super.sendToGalleryItem = sendToGalleryItem;
  }
}
