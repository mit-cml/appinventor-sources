// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.undo;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

/**
 * Command that represents deleting a component from the form.
 * Undo recreates the component from the stored JSON; redo deletes it again.
 */
public class DeleteComponentCommand implements DesignerCommand {

  private final YaFormEditor formEditor;
  private final String componentJson;
  private final String containerUuid;
  private final int childIndex;
  private final boolean isVisible;

  /** UUID extracted from the JSON for redo (finding the recreated component). */
  private String componentUuid;

  public DeleteComponentCommand(YaFormEditor formEditor, String componentJson,
      String containerUuid, int childIndex, boolean isVisible) {
    this.formEditor = formEditor;
    this.componentJson = componentJson;
    this.containerUuid = containerUuid;
    this.childIndex = childIndex;
    this.isVisible = isVisible;
  }

  @Override
  public void execute() {
    // Redo: delete the component again
    if (componentUuid != null) {
      MockComponent component = formEditor.getComponentByUuid(componentUuid);
      if (component != null) {
        formEditor.removeComponentForUndo(component);
      }
    }
  }

  @Override
  public void undo() {
    // Undo: recreate the component from stored JSON
    MockContainer container = getContainer();
    if (container != null) {
      MockComponent recreated = formEditor.recreateComponentFromJson(
          componentJson, container, childIndex);
      if (recreated != null) {
        componentUuid = recreated.getUuid();
        recreated.select(null);
      }
    }
  }

  @Override
  public String getDescription() {
    return "Delete component";
  }

  private MockContainer getContainer() {
    if (containerUuid == null) {
      return formEditor.getForm();
    }
    MockComponent comp = formEditor.getComponentByUuid(containerUuid);
    if (comp instanceof MockContainer) {
      return (MockContainer) comp;
    }
    return formEditor.getForm();
  }
}
