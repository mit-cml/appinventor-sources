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
  private final ListBox listboxPhoneTablet; // A CheckBox for Phone/Tablet preview sizes
  private final int[] drop_lst = new int[] { 320, 505, 480, 675, 800, 995 };

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

    listboxPhoneTablet = new ListBox() {
      @Override
      protected void onLoad() {
        projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_TABLET);
        changeFormPreviewSize(0, 320, 505);
      }
    };
    listboxPhoneTablet.addItem("Select 'phone' to see Preview on Phone size.");
    listboxPhoneTablet.addItem("Select 'tablet' to see Preview on Tablet size.");
    listboxPhoneTablet.addItem("Select 'monitor' to see Preview on Monitor size.");
    changeFormPreviewSize(0, 320, 505);
    listboxPhoneTablet.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int idx = listboxPhoneTablet.getSelectedIndex();
        int width = drop_lst[2 * idx];
        int height = drop_lst[2 * idx + 1];
        String val = Integer.toString(width) + "," + Integer.toString(height);
        projectEditor.changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_TABLET, val);
        changeFormPreviewSize(idx, width, height);
      }
    });
    phoneScreen.add(listboxPhoneTablet);

    initWidget(phoneScreen);
  }

  private void changeFormPreviewSize(int idx, int width, int height) {
    if (form != null) {
      form.changePreviewSize(width, height);
      if (idx == 0) {
        listboxPhoneTablet.setItemText(idx, MESSAGES.previewPhoneSize());
      } else if (idx == 1) {
        listboxPhoneTablet.setItemText(idx, MESSAGES.previewTabletSize());
      } else {
        listboxPhoneTablet.setItemText(idx, MESSAGES.previewMonitorSize());
      }
    }
  }

  public void enableTabletPreviewCheckBox(boolean enable){
    if (form != null){
      if (!enable){
        form.changePreviewSize(320, 505);
        listboxPhoneTablet.setItemSelected(0, true);
      }
    }
    listboxPhoneTablet.setEnabled(enable);
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
