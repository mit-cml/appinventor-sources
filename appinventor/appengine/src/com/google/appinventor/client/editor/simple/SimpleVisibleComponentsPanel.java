// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

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
  private final CheckBox checkboxPhoneTablet; // A CheckBox for Phone/Tablet preview sizes

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
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        boolean showHiddenComponents = Boolean.parseBoolean(
            projectEditor.getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_HIDDEN_COMPONENTS));
        checkboxShowHiddenComponents.setValue(showHiddenComponents);
      }
    };
    checkboxShowHiddenComponents.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
        projectEditor.changeProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_HIDDEN_COMPONENTS,
            isChecked ? "True" : "False");
        if (form != null) {
          form.refresh();
        }
      }
    });
    phoneScreen.add(checkboxShowHiddenComponents);

    checkboxPhoneTablet = new CheckBox(MESSAGES.previewPhoneSize()) {
      @Override
      protected void onLoad() {
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        boolean showPhoneTablet = Boolean.parseBoolean(
            projectEditor.getProjectSettingsProperty(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_TABLET));
        checkboxPhoneTablet.setValue(showPhoneTablet);
        changeFormPreviewSize(showPhoneTablet);
      }
    };
    checkboxPhoneTablet.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
          boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
          projectEditor.changeProjectSettingsProperty(
              SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
              SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_TABLET,
              isChecked ? "True" : "False");
          changeFormPreviewSize(isChecked);
        }
    });
    phoneScreen.add(checkboxPhoneTablet);

    initWidget(phoneScreen);
  }

  private void changeFormPreviewSize(boolean isChecked) {
    if (form != null){
      if (isChecked){
        form.changePreviewSize(true);
        checkboxPhoneTablet.setText(MESSAGES.previewPhoneSize());
      }
      else {
        form.changePreviewSize(false);
        checkboxPhoneTablet.setText(MESSAGES.previewTabletSize());
      }
    }
  }

  public void enableTabletPreviewCheckBox(boolean enable){
    if (form != null){
      if (!enable){
        form.changePreviewSize(false);
        checkboxPhoneTablet.setText(MESSAGES.previewTabletSize());
        checkboxPhoneTablet.setChecked(false);
      }
    }
    checkboxPhoneTablet.setEnabled(enable);
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
