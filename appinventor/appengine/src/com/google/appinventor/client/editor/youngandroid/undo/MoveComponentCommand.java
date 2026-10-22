// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.undo;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

/**
 * Command that represents moving a component from one container to another.
 */
public class MoveComponentCommand implements DesignerCommand {

  private final YaFormEditor formEditor;
  private final String componentUuid;
  private final String srcContainerUuid;
  private final int srcChildIndex;
  private final String dstContainerUuid;
  private final int dstChildIndex;

  public MoveComponentCommand(YaFormEditor formEditor, String componentUuid,
      String srcContainerUuid, int srcChildIndex,
      String dstContainerUuid, int dstChildIndex) {
    this.formEditor = formEditor;
    this.componentUuid = componentUuid;
    this.srcContainerUuid = srcContainerUuid;
    this.srcChildIndex = srcChildIndex;
    this.dstContainerUuid = dstContainerUuid;
    this.dstChildIndex = dstChildIndex;
  }

  @Override
  public void execute() {
    // Redo: move from source to destination
    moveComponent(srcContainerUuid, dstContainerUuid, dstChildIndex);
  }

  @Override
  public void undo() {
    // Undo: move from destination back to source
    moveComponent(dstContainerUuid, srcContainerUuid, srcChildIndex);
  }

  private void moveComponent(String fromContainerUuid, String toContainerUuid, int toIndex) {
    MockComponent component = formEditor.getComponentByUuid(componentUuid);
    if (component == null) {
      return;
    }
    MockContainer fromContainer = resolveContainer(fromContainerUuid);
    MockContainer toContainer = resolveContainer(toContainerUuid);
    if (fromContainer == null || toContainer == null) {
      return;
    }
    fromContainer.removeComponent(component, false);
    if (toIndex >= 0 && toIndex <= toContainer.getChildren().size()) {
      toContainer.addComponent(component, toIndex);
    } else {
      toContainer.addComponent(component);
    }
    component.select(null);
  }

  private MockContainer resolveContainer(String containerUuid) {
    if (containerUuid == null) {
      return formEditor.getForm();
    }
    MockComponent comp = formEditor.getComponentByUuid(containerUuid);
    if (comp instanceof MockContainer) {
      return (MockContainer) comp;
    }
    return formEditor.getForm();
  }

  @Override
  public String getDescription() {
    return "Move component";
  }
}
