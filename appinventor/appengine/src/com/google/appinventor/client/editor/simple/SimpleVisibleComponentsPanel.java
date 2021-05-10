// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ListBox;

import java.util.List;
import java.util.Map;

/**
 * Panel in the Simple design editor holding visible Simple components.
 *
 */
public final class SimpleVisibleComponentsPanel extends Composite implements DropTarget, ComponentDatabaseChangeListener {
  // UI elements
  private final VerticalPanel phoneScreen;
  private final CheckBox checkboxShowHiddenComponents;
  private final ListBox listboxPhoneTablet; // A ListBox for Phone/Tablet/Monitor preview sizes
  private final ListBox listboxPhonePreview; // A ListBox for Holo/Material/iOS preview styles
  private final int[][] drop_lst = { {320, 505}, {480, 675}, {768, 1024} };
  private final String[] drop_lst_phone_preview = { "Android Material", "Android Holo", "iOS" };

  // Corresponding panel for non-visible components (because we allow users to drop
  // non-visible components onto the form, but we show them in the non-visible
  // components panel)
  private final SimpleNonVisibleComponentsPanel nonVisibleComponentsPanel;
  private final ProjectEditor projectEditor;

  private MockForm form;

  /**
   * Creates new component design panel for visible components.
   *
   * @param nonVisibleComponentsPanel  corresponding panel for non-visible
   *                                   components
   */
  public SimpleVisibleComponentsPanel(final SimpleEditor editor,
      SimpleNonVisibleComponentsPanel nonVisibleComponentsPanel) {
    this.nonVisibleComponentsPanel = nonVisibleComponentsPanel;
    projectEditor = editor.getProjectEditor();

    // Initialize UI
    phoneScreen = new VerticalPanel();
    phoneScreen.setStylePrimaryName("ode-SimpleFormDesigner");

    checkboxShowHiddenComponents = new CheckBox(MESSAGES.showHiddenComponentsCheckbox()) {
      @Override
      protected void onLoad() {
        // Get project settings
        String screenCheckboxMap = projectEditor.getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS, 
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SCREEN_CHECKBOX_STATE_MAP
        );
        if (screenCheckboxMap != null && !screenCheckboxMap.equals("")) {
          projectEditor.buildScreenHashMap(screenCheckboxMap);
          Boolean isChecked = projectEditor.getScreenCheckboxState(form.getTitle());
          checkboxShowHiddenComponents.setValue(isChecked);
        }
      }
    };
    checkboxShowHiddenComponents.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean isChecked = event.getValue();
        projectEditor.setScreenCheckboxState(form.getTitle(), isChecked);
        projectEditor.changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS, 
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SCREEN_CHECKBOX_STATE_MAP, 
          projectEditor.getScreenCheckboxMapString()
        );
        if (form != null) {
          form.refresh();
        }
      }
    });
    phoneScreen.add(checkboxShowHiddenComponents);

    listboxPhoneTablet = new ListBox() {
      @Override
      protected void onLoad() {
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        String sizing = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING);
        boolean fixed = (sizing.equals("Fixed"));
        listboxPhoneTablet.setVisible(!fixed);
        if (fixed) {
          changeFormPreviewSize(0, 320, 505);
        } else {
          getUserSettingChangeSize();
        }
      }
    };
    listboxPhoneTablet.addItem("Phone size");
    listboxPhoneTablet.addItem("Tablet size");
    listboxPhoneTablet.addItem("Monitor size");
    listboxPhoneTablet.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int idx = listboxPhoneTablet.getSelectedIndex();
        int width = drop_lst[idx][0];
        int height = drop_lst[idx][1];
        String val = Integer.toString(idx) + "," + Integer.toString(width) + "," + Integer.toString(height);
        // here, we can change settings by putting val into it
        projectEditor.changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_TABLET, val);
        changeFormPreviewSize(idx, width, height);
      }
    });

    phoneScreen.add(listboxPhoneTablet);

    listboxPhonePreview = new ListBox() {
      @Override
      protected void onLoad() {
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        String previewStyle = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME);
        boolean classic = (previewStyle.equals("Classic"));
        listboxPhonePreview.setVisible(!classic);
        if (classic) {
          changeFormPhonePreview(-1, "Classic");
        } else {
          getUserSettingChangePreview();
        }
      }
    };
    listboxPhonePreview.addItem("Android 5+ Devices");
    listboxPhonePreview.addItem("Android 3.0-4.4.2 Devices");
    listboxPhonePreview.addItem("iOS 13 Devices");
    listboxPhonePreview.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int idx = listboxPhonePreview.getSelectedIndex();
        String val = drop_lst_phone_preview[idx];
        // here, we can change settings by putting chosenStyle value into it
        projectEditor.changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_PREVIEW, val);
        changeFormPhonePreview(idx, val);
      }
    });

    phoneScreen.add(listboxPhonePreview);

    initWidget(phoneScreen);
  }

  public boolean isHiddenComponentsCheckboxChecked() {
    return checkboxShowHiddenComponents.getValue();
  }

  // get width and height stored in user settings, and change the preview size.
  private void getUserSettingChangeSize() {
    String val = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_TABLET);
    int idx = 0;
    int width = 320;
    int height = 505;

    if (val.equals("True")) {
      idx = 1;
      width = drop_lst[idx][0];
      height = drop_lst[idx][1];
    }

    String[] parts = val.split(",");
    if (parts.length == 3) {
      idx = Integer.parseInt(parts[0]);
      width = Integer.parseInt(parts[1]);
      height = Integer.parseInt(parts[2]);
    }
    listboxPhoneTablet.setItemSelected(idx, true);
    changeFormPreviewSize(idx, width, height);
  }

  // get Phone Preview stored in user settings, and change the preview style.
  private void getUserSettingChangePreview() {
    String val = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_PREVIEW);
    int idx = 0;

    if (val.equals("Classic")) {
      val = "Android Material";
    }

    if (val.equals("Android Holo")) {
      idx = 1;
    } else if (val.equals("iOS")) {
      idx = 2;
    }
    listboxPhonePreview.setItemSelected(idx, true);
    changeFormPhonePreview(idx, val);
  }

  private void changeFormPreviewSize(int idx, int width, int height) {

    if (form == null)
      return;

    form.changePreviewSize(width, height, idx);

    String info = " (" + height + "," + width + ")";
    if (idx == 0) {
      listboxPhoneTablet.setItemText(idx, MESSAGES.previewPhoneSize() + info);
      listboxPhoneTablet.setItemText(1, MESSAGES.previewTabletSize());
      listboxPhoneTablet.setItemText(2, MESSAGES.previewMonitorSize());
    } else if (idx == 1) {
      listboxPhoneTablet.setItemText(idx, MESSAGES.previewTabletSize() + info);
      listboxPhoneTablet.setItemText(0, MESSAGES.previewPhoneSize());
      listboxPhoneTablet.setItemText(2, MESSAGES.previewMonitorSize());
    } else {
      listboxPhoneTablet.setItemText(idx, MESSAGES.previewMonitorSize() + info);
      listboxPhoneTablet.setItemText(0, MESSAGES.previewPhoneSize());
      listboxPhoneTablet.setItemText(1, MESSAGES.previewTabletSize());
    }
    // change settings
  }

  private void changeFormPhonePreview(int idx, String chosenVal) {

    if (form == null)
      return;

    form.changePhonePreview(idx, chosenVal);

    String info = " (" + chosenVal + ")";
    if (idx == 0) {
      listboxPhonePreview.setItemText(idx, MESSAGES.previewAndroidMaterial() + info);
      listboxPhonePreview.setItemText(1, MESSAGES.previewAndroidHolo());
      listboxPhonePreview.setItemText(2, MESSAGES.previewIOS());
    } else if (idx == 1) {
      listboxPhonePreview.setItemText(idx, MESSAGES.previewAndroidHolo() + info);
      listboxPhonePreview.setItemText(0, MESSAGES.previewAndroidMaterial());
      listboxPhonePreview.setItemText(2, MESSAGES.previewIOS());
    } else if (idx == 2){
      listboxPhonePreview.setItemText(idx, MESSAGES.previewIOS() + info);
      listboxPhonePreview.setItemText(0, MESSAGES.previewAndroidMaterial());
      listboxPhonePreview.setItemText(1, MESSAGES.previewAndroidHolo());
    }
    // change settings
  }

  public void enableTabletPreviewCheckBox(boolean enable){
    if (form != null){
      if (!enable){
        changeFormPreviewSize(0, 320, 505);
        listboxPhoneTablet.setVisible(enable);
      } else {
        getUserSettingChangeSize();
        listboxPhoneTablet.setVisible(enable);
      }
    }
    listboxPhoneTablet.setEnabled(enable);
  }

  public void enablePhonePreviewCheckBox(boolean enable){
    if (form != null){
      if (!enable){
        changeFormPhonePreview(-1,"Classic");
        listboxPhonePreview.setVisible(enable);
      } else {
        getUserSettingChangePreview();
        listboxPhonePreview.setVisible(enable);
      }
    }
    listboxPhonePreview.setEnabled(enable);
  }

  /**
   * Associates a Simple form component with this panel.
   *
   * @param form  backing mocked form component
   */
  public void setForm(MockForm form) {
    this.form = form;
    phoneScreen.add(form);
  }

  // DropTarget implementation

  // Non-visible components will be forwarded to the non-visible components design panel
  // as a courtesy. Visible components will be accepted by individual MockContainers.

  @Override
  public Widget getDropTargetWidget() {
    return this;
  }

  @Override
  public boolean onDragEnter(DragSource source, int x, int y) {
    // Accept palette items for non-visible components only
    return (source instanceof SimplePaletteItem) &&
      !((SimplePaletteItem) source).isVisibleComponent() &&
      nonVisibleComponentsPanel.onDragEnter(source, -1, -1);
  }

  @Override
  public void onDragContinue(DragSource source, int x, int y) {
    nonVisibleComponentsPanel.onDragContinue(source, -1, -1);
  }

  @Override
  public void onDragLeave(DragSource source) {
    nonVisibleComponentsPanel.onDragLeave(source);
  }

  @Override
  public void onDrop(DragSource source, int x, int y, int offsetX, int offsetY) {
    nonVisibleComponentsPanel.onDrop(source, -1, -1, offsetX, offsetY);
  }

  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {

  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    return true;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {

  }

  @Override
  public void onResetDatabase() {

  }
}
