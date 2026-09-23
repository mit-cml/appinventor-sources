// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.undo;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

/**
 * Command that represents a property value change on a component.
 */
public class PropertyChangeCommand implements DesignerCommand {

  private final YaFormEditor formEditor;
  private final String componentUuid;
  private final String componentName;
  private final String propertyName;
  private final String oldValue;
  private String newValue;

  public PropertyChangeCommand(YaFormEditor formEditor, String componentUuid,
      String componentName, String propertyName, String oldValue, String newValue) {
    this.formEditor = formEditor;
    this.componentUuid = componentUuid;
    this.componentName = componentName;
    this.propertyName = propertyName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public void execute() {
    MockComponent component = formEditor.getComponentByUuid(componentUuid);
    if (component != null) {
      component.changeProperty(propertyName, newValue);
    }
  }

  @Override
  public void undo() {
    MockComponent component = formEditor.getComponentByUuid(componentUuid);
    if (component != null) {
      component.changeProperty(propertyName, oldValue);
    }
  }

  @Override
  public String getDescription() {
    return "Change " + componentName + "." + propertyName;
  }

  public String getComponentUuid() {
    return componentUuid;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getOldValue() {
    return oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }
}
