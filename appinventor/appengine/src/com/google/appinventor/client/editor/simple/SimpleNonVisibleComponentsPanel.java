// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.shared.simple.ComponentDatabaseChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import java.util.Map;

/**
 * Panel in the Simple design editor holding non-visible Simple components.
 *
 */
public class SimpleNonVisibleComponentsPanel<T extends DesignerRootComponent> extends Composite
    implements DropTarget, ComponentDatabaseChangeListener {

  // UI elements
  protected final Label heading;
  protected final FlowPanel componentsPanel;

  // Backing mocked root component
  protected T root;

  /**
   * Creates new component design panel for non-visible components.
   */
  public SimpleNonVisibleComponentsPanel() {
    // Initialize UI
    VerticalPanel panel = new VerticalPanel();
    panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

    heading = new Label("");
    heading.setStyleName("ya-NonVisibleComponentsHeader");
    panel.add(heading);

    componentsPanel = new FlowPanel();
    componentsPanel.setStyleName("ode-SimpleUiDesignerNonVisibleComponents");
    panel.add(componentsPanel);

    initWidget(panel);
  }

  /**
   * Associates a root component with this panel.
   *
   * @param root  backing mocked root component
   */
  public void setRoot(T root) {
    this.root = root;
  }

  /**
   * Adds a new non-visible component to this panel. Note that this method
   * will not add the component to the root component! This needs to be done
   * separately.
   *
   * @param component  Simple mock component to be added
   */
  public void addComponent(MockComponent component) {
    componentsPanel.add(component);
    if (componentsPanel.getWidgetCount() > 0) {
      heading.setText(MESSAGES.nonVisibleComponentsHeader());
    }
  }

  /**
   * Removes a new non-visible component from this panel. Note that this method
   * will not remove the component from the root component! This needs to be
   * done separately.
   *
   * @param component  Simple mock component to be removed
   */
  public void removeComponent(MockComponent component) {
    componentsPanel.remove(component);
    if (componentsPanel.getWidgetCount() == 0) {
      heading.setText("");
    }
  }

  // DropTarget implementation

  @Override
  public Widget getDropTargetWidget() {
    return this;
  }

  // TODO(user): Draw a colored border around the edges of the non-visible component
  //                    area while an eligible component is hovering over it.
  @Override
  public boolean onDragEnter(DragSource source, int x, int y) {
    // Accept palette items corresponding to non-visible components only
    return (source instanceof SimplePaletteItem) &&
        !((SimplePaletteItem) source).isVisibleComponent();
  }

  @Override
  public void onDragContinue(DragSource source, int x, int y) {
    // no action
  }

  @Override
  public void onDragLeave(DragSource source) {
    // no action
  }

  @Override
  public void onDrop(DragSource source, int x, int y, int offsetX, int offsetY) {
    MockComponent sourceComponent = ((SimplePaletteItem) source).createMockComponent();

    // Add component to the root
    root.addComponent(sourceComponent);

    // Add component to this panel
    addComponent(sourceComponent);
    sourceComponent.select(null);
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
