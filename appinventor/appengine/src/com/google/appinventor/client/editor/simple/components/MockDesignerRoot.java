// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.designer.DesignerChangeListener;
import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TreeItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base container implementation for those components that serve as the root of a designer view.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class MockDesignerRoot extends MockContainer implements DesignerRootComponent {
  private final Set<DesignerChangeListener> changeListeners = new HashSet<>();
  private final List<MockComponent> selectedComponents = new ArrayList<MockComponent>(Collections.singleton(this));
  private MockContainer pasteTarget = this;
  private int view = 1;

  MockDesignerRoot(SimpleEditor editor, String type, ImageResource icon, MockLayout layout) {
    super(editor, type, icon, layout);
  }

  public abstract void refresh();
  public abstract void refresh(boolean force);

  @Override
  public DesignerRootComponent getRoot() {
    return this;
  }

  @Override
  public boolean isRoot() {
    return true;
  }

  /**
   * Builds a tree of the component hierarchy of the form for display in the
   * {@code SourceStructureExplorer}.
   *
   * @return  tree showing the component hierarchy of the form
   */
  public TreeItem buildComponentsTree() {
    return buildComponentsTree(view);
  }

  /**
   * Builds a tree of the component hierarchy of the form for display in the
   * {@code SourceStructureExplorer}.
   *
   * @return  tree showing the component hierarchy of the form
   */
  public TreeItem buildComponentsTree(int view) {
    this.view = view;
    return buildTree(view);
  }

  @Override
  public List<MockComponent> getSelectedComponents() {
    return selectedComponents;
  }

  @Override
  public final MockComponent getLastSelectedComponent() {
    return selectedComponents.get(selectedComponents.size() - 1);
  }

  @Override
  public void addDesignerChangeListener(DesignerChangeListener listener) {
    changeListeners.add(listener);
  }

  @Override
  public void removeDesignerChangeListener(DesignerChangeListener listener) {
    changeListeners.remove(listener);
  }

  @Override
  public MockComponent asMockComponent() {
    return this;
  }

  /**
   * Changes the component that is currently selected in the root.
   * <p>
   * There will always be exactly one component selected in a root
   * at any given time.
   */
  public final void setSelectedComponent(MockComponent newSelectedComponent, NativeEvent event) {
    if (newSelectedComponent == null) {
      throw new IllegalArgumentException("at least one component must always be selected");
    }
    boolean shouldSelectMultipleComponents = shouldSelectMultipleComponents(event);
    if (selectedComponents.size() == 1 && selectedComponents.contains(newSelectedComponent)) {
      // Attempting to change the selection from old to new when they are the same breaks
      // Marker drag. See https://github.com/mit-cml/appinventor-sources/issues/1936
      return;
    }

    // Remove an previously selected component from the list of selected components, but only if
    // there would still be something selected.
    if (shouldSelectMultipleComponents && selectedComponents.contains(newSelectedComponent)
        && selectedComponents.size() > 1) {
      selectedComponents.remove(newSelectedComponent);
      newSelectedComponent.onSelectedChange(false);
      return;
    }
    if (newSelectedComponent instanceof MockContainer) {
      pasteTarget = (MockContainer) newSelectedComponent;
    } else {
      pasteTarget = newSelectedComponent.getContainer();
    }

    if (!shouldSelectMultipleComponents) {
      for (MockComponent component : selectedComponents) {
        if (component != newSelectedComponent) {
          component.onSelectedChange(false);
        }
      }
      selectedComponents.clear();
    }
    selectedComponents.add(newSelectedComponent);
    newSelectedComponent.onSelectedChange(true);
  }

  public final MockContainer getPasteTarget() {
    return pasteTarget;
  }

  public final void setPasteTarget(MockContainer target) {
    this.pasteTarget = target;
  }

  /**
   * Triggers a component property change event to be sent to the listener on the listener list.
   */
  public void fireComponentPropertyChanged(MockComponent component,
                                           String propertyName, String propertyValue) {
    for (DesignerChangeListener listener : changeListeners) {
      listener.onComponentPropertyChanged(component, propertyName, propertyValue);
    }
  }

  /**
   * Triggers a component removed event to be sent to the listener on the listener list.
   */
  public void fireComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    for (DesignerChangeListener listener : changeListeners) {
      listener.onComponentRemoved(component, permanentlyDeleted);
    }
  }

  /**
   * Triggers a component added event to be sent to the listener on the listener list.
   */
  public void fireComponentAdded(MockComponent component) {
    for (DesignerChangeListener listener : changeListeners) {
      listener.onComponentAdded(component);
    }
  }

  /**
   * Triggers a component renamed event to be sent to the listener on the listener list.
   */
  public void fireComponentRenamed(MockComponent component, String oldName) {
    for (DesignerChangeListener listener : changeListeners) {
      listener.onComponentRenamed(component, oldName);
    }
  }

  /**
   * Triggers a component selection change event to be sent to the listener on the listener list.
   */
  public void fireComponentSelectionChange(MockComponent component, boolean selected) {
    for (DesignerChangeListener listener : changeListeners) {
      listener.onComponentSelectionChange(component, selected);
    }
  }

  private boolean shouldSelectMultipleComponents(NativeEvent e) {
    if (e == null) {
      return false;
    }
    if (Window.Navigator.getPlatform().toLowerCase().startsWith("mac")) {
      return e.getMetaKey();
    } else {
      return e.getCtrlKey();
    }
  }

}
