// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.components.FormChangeListener;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.Set;

/**
 * Property editor for selecting a component for a property.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class YoungAndroidComponentSelectorPropertyEditor
    extends AdditionalChoicePropertyEditor implements FormChangeListener {
  // UI elements
  private final ListBox componentsList;

  private final ListWithNone choices;

  // The YaFormEditor associated with this property editor.
  private final YaFormEditor editor;

  // The types of component that can be chosen
  private final Set<String> componentTypes;

  /**
   * Creates a new property editor for selecting a component.
   *
   * @param editor the editor that this property editor belongs to
   */
  public YoungAndroidComponentSelectorPropertyEditor(YaFormEditor editor) {
    this(editor, null);
  }

  /**
   * Creates a new property editor for selecting a component, where the
   * user chooses among components of one or more component types.
   *
   * @param editor the editor that this property editor belongs to
   * @param componentTypes types of component that can be selected, or null if
   *        all types of components can be selected.
   */
  public YoungAndroidComponentSelectorPropertyEditor(final YaFormEditor editor,
      Set<String> componentTypes) {
    this.editor = editor;
    this.componentTypes = componentTypes;

    VerticalPanel selectorPanel = new VerticalPanel();
    componentsList = new ListBox();
    componentsList.setVisibleItemCount(10);
    componentsList.setWidth("100%");
    selectorPanel.add(componentsList);
    selectorPanel.setWidth("100%");

    choices = new ListWithNone(MESSAGES.noneCaption(), new ListWithNone.ListBoxWrapper() {
      @Override
      public void addItem(String item) {
        componentsList.addItem(item);
      }

      @Override
      public String getItem(int index) {
        return componentsList.getItemText(index);
      }

      @Override
      public void removeItem(int index) {
        componentsList.removeItem(index);
      }

      @Override
      public void setSelectedIndex(int index) {
        componentsList.setSelectedIndex(index);
      }
    });

    // At this point, the editor hasn't finished loading.
    // Use a DeferredCommand to finish the initialization after the editor has finished loading.
    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        if (editor.isLoadComplete()) {
          finishInitialization();
        } else {
          // Editor still hasn't finished loading.
          DeferredCommand.addCommand(this);
        }
      }
    });

    initAdditionalChoicePanel(selectorPanel);
  }

  private void finishInitialization() {
    // Add a FormChangeListener so we'll know when components are added/removed/renamed.
    editor.getForm().addFormChangeListener(this);

    // Fill choices with the components.
    for (MockComponent component : editor.getComponents().values()) {
      if (componentTypes == null || componentTypes.contains(component.getType())) {
        choices.addItem(component.getName());
      }
    }

    // Previous version had a bug where the value could be accidentally saved as "None".
    // If the property value is "None" and choices doesn't contain the value "None", set the
    // property value to "".
    String value = property.getValue();
    if (value.equals("None") && !choices.containsValue(value)) {
      property.setValue("");
    }
  }

  @Override
  public void orphan() {
    editor.getForm().removeFormChangeListener(this);
    super.orphan();
  }

  @Override
  protected void openAdditionalChoiceDialog() {
    choices.selectValue(property.getValue());
    super.openAdditionalChoiceDialog();
    componentsList.setFocus(true);
  }

  @Override
  protected String getPropertyValueSummary() {
    String value = property.getValue();
    if (choices.containsValue(value)) {
      return choices.getDisplayItemForValue(value);
    }
    return value;
  }

  @Override
  protected boolean okAction() {
    int selected = componentsList.getSelectedIndex();
    if (selected == -1) {
      Window.alert(MESSAGES.noComponentSelected());
      return false;
    }
    property.setValue(choices.getValueAtIndex(selected));
    return true;
  }

  // FormChangeListener

  public void onComponentPropertyChanged(MockComponent component,
      String propertyName, String propertyValue) {
  }

  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (permanentlyDeleted) {
      if (componentTypes == null || componentTypes.contains(component.getType())) {
        String componentName = component.getName();

        // Check whether our component was removed.
        String currentValue = property.getValue();
        if (componentName.equals(currentValue)) {
          // Our component was removed.
          property.setValue("");
        }

        // Remove the component from the list.
        choices.removeValue(componentName);
      }
    }
  }

  public void onComponentAdded(MockComponent component) {
    if (componentTypes == null || componentTypes.contains(component.getType())) {
      choices.addItem(component.getName());
    }
  }

  public void onComponentRenamed(MockComponent component, String oldName) {
    if (componentTypes == null || componentTypes.contains(component.getType())) {
      String newName = component.getName();

      // Add the new name to the list.
      choices.addItem(newName);

      // Check whether our component was renamed.
      String currentValue = property.getValue();
      if (oldName.equals(currentValue)) {
        // Our component was renamed.
        property.setValue(newName);
      }

      // Remove the old name from the choices.
      choices.removeValue(oldName);
    }
  }

  public void onComponentSelectionChange(MockComponent component, boolean selected) {
  }
}
