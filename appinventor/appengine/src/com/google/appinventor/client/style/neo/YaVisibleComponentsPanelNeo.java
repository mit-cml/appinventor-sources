// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.components.Icon;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaVisibleComponentsPanel;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class YaVisibleComponentsPanelNeo extends YaVisibleComponentsPanel {
  interface SimpleVisibleComponentsPanelUiBinderNeo extends UiBinder<VerticalPanel,
      YaVisibleComponentsPanelNeo> {}
  @UiField protected VerticalPanel phoneScreen;
  @UiField(provided = true) protected ListBox listboxPhoneTablet; // A ListBox for Phone/Tablet/Monitor preview sizes
  @UiField(provided = true) protected ListBox listboxPhonePreview;
  @UiField Icon os_icon;
  @UiField Icon size_icon;
  @UiField CheckBox HiddenComponentsCheckbox;

  public YaVisibleComponentsPanelNeo(final ProjectEditor projectEditor,
      SimpleNonVisibleComponentsPanel<MockForm> nonVisibleComponentsPanel) {
    super(projectEditor, nonVisibleComponentsPanel);
  }

  @Override
  protected void initializeListboxes() {
    // Initialize UI
    listboxPhoneTablet = new ListBox() {
      @Override
      protected void onLoad() {
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        String sizing = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING);
        boolean fixed = (sizing.equals("Fixed"));
        listboxPhoneTablet.setVisible(!fixed);
        if (fixed) {
          size_icon.setVisible(false);
          changeFormPreviewSize(0, 320, 505);
        } else {
          size_icon.setVisible(true);
          getUserSettingChangeSize();
        }
      }
    };

    listboxPhonePreview = new ListBox() {
      @Override
      protected void onLoad() {
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        String previewStyle = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME);
        boolean classic = (previewStyle.equals("Classic"));
        listboxPhonePreview.setVisible(!classic);
        os_icon.setVisible(!classic);
        if (classic) {
          changeFormPhonePreview(-1, "Classic");
        } else {
          getUserSettingChangePreview();
        }
      }
    };
  }

  @Override
  public void enableTabletPreviewCheckBox(boolean enable){
    super.enableTabletPreviewCheckBox(enable);
    size_icon.setVisible(enable);
  }

  @Override
  public void enablePhonePreviewCheckBox(boolean enable){
    super.enablePhonePreviewCheckBox(enable);
    os_icon.setVisible(enable);
  }


  @Override
  protected void bindUI() {
    SimpleVisibleComponentsPanelUiBinderNeo uibinder =
        GWT.create(SimpleVisibleComponentsPanelUiBinderNeo.class);
    uibinder.createAndBindUi(this);
    super.listboxPhonePreview = listboxPhonePreview;
    super.listboxPhoneTablet = listboxPhoneTablet;
    super.phoneScreen = phoneScreen;
    super.HiddenComponentsCheckbox = HiddenComponentsCheckbox;
  }
}
