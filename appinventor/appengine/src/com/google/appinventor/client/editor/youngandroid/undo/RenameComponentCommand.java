// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.undo;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

/**
 * Command that represents renaming a component.
 */
public class RenameComponentCommand implements DesignerCommand {

  private final YaFormEditor formEditor;
  private final String componentUuid;
  private final String oldName;
  private final String newName;

  public RenameComponentCommand(YaFormEditor formEditor, String componentUuid,
      String oldName, String newName) {
    this.formEditor = formEditor;
    this.componentUuid = componentUuid;
    this.oldName = oldName;
    this.newName = newName;
  }

  @Override
  public void execute() {
    // Redo: rename to newName
    rename(newName, oldName);
  }

  @Override
  public void undo() {
    // Undo: rename back to oldName
    rename(oldName, newName);
  }

  private void rename(String toName, String fromName) {
    MockComponent component = formEditor.getComponentByUuid(componentUuid);
    if (component != null) {
      component.changeProperty(MockComponent.PROPERTY_NAME_NAME, toName);
      component.getRoot().fireComponentRenamed(component, fromName);
    }
  }

  @Override
  public String getDescription() {
    return "Rename " + oldName + " to " + newName;
  }
}
