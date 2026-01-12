// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.shared.simple.ComponentDatabaseChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Panel in the Simple design editor holding visible Simple components.
 *
 */
public abstract class SimpleVisibleComponentsPanel<T extends DesignerRootComponent>
    extends Composite implements DropTarget, ComponentDatabaseChangeListener {
  private static final Logger LOG = Logger.getLogger(SimpleVisibleComponentsPanel.class.getName());

  // Corresponding panel for non-visible components (because we allow users to drop
  // non-visible components onto the root, but we show them in the non-visible
  // components panel)
  private final SimpleNonVisibleComponentsPanel<T> nonVisibleComponentsPanel;

  protected T root;

  /**
   * Creates new component design panel for visible components.
   *
   * @param nonVisibleComponentsPanel  corresponding panel for non-visible
   *                                   components
   */
  public SimpleVisibleComponentsPanel(SimpleNonVisibleComponentsPanel<T> nonVisibleComponentsPanel) {
    this.nonVisibleComponentsPanel = nonVisibleComponentsPanel;
  }

  public SimpleNonVisibleComponentsPanel<T> getNonVisibleComponentsPanel() {
    return nonVisibleComponentsPanel;
  }

  public abstract void setRoot(T root);

  protected abstract void bindUI();

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
