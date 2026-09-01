// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.undo;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

/**
 * Command that represents adding a component to the form.
 * Undo removes the component; redo recreates it from the stored JSON.
 */
public class AddComponentCommand implements DesignerCommand {

  private final YaFormEditor formEditor;
  private final String componentJson;
  private final String containerUuid;
  private final int childIndex;
  private final String componentUuid;
  private final boolean isNonVisible;

  public AddComponentCommand(YaFormEditor formEditor, String componentJson,
      String containerUuid, int childIndex, String componentUuid, boolean isNonVisible) {
    this.formEditor = formEditor;
    this.componentJson = componentJson;
    this.containerUuid = containerUuid;
    this.childIndex = childIndex;
    this.componentUuid = componentUuid;
    this.isNonVisible = isNonVisible;
  }

  @Override
  public void execute() {
    // Redo: recreate the component from stored JSON
    MockContainer container = getContainer();
    if (container != null) {
      formEditor.recreateComponentFromJson(componentJson, container, childIndex);
    }
  }

  @Override
  public void undo() {
    // Undo: remove the component
    MockComponent component = formEditor.getComponentByUuid(componentUuid);
    if (component != null) {
      formEditor.removeComponentForUndo(component);
    }
  }

  @Override
  public String getDescription() {
    return "Add component";
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
