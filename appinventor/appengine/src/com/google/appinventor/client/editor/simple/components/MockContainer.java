// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.common.base.Preconditions;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract superclass for all container mock components.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class MockContainer extends MockVisibleComponent implements DropTarget {

  protected final MockLayout layout;

  // List of components within the container
  protected final List<MockComponent> children;

  /**
   * Directly contains the widgets corresponding to children MockComponents.
   * <p>
   * This should not be modified directly by implementations;
   * use {@link #addComponent} and {@link #removeComponent} to make
   * modifications.
   * <p>
   * This is a GWT absolute panel so that the {@link MockLayout} associated
   * with this container can freely position and size children widgets as desired.
   */
  protected final AbsolutePanel rootPanel;

  /**
   * Creates a new component container.
   * <p>
   * Implementations are responsible for constructing their own visual appearance
   * and calling {@link #initWidget(com.google.gwt.user.client.ui.Widget)}.
   * This appearance should include {@link #rootPanel} so that children
   * components are displayed correctly.
   *
   * @param editor  editor of source file the component belongs to
   */
  MockContainer(SimpleEditor editor, String type, ImageResource icon,
      MockLayout layout) {
    super(editor, type, icon);

    this.layout = layout;
    layout.setContainer(this);

    children = new ArrayList<MockComponent>();
    rootPanel = new AbsolutePanel();
  }

  @Override
  public List<MockComponent> getChildren() {
    return children;
  }

  @Override
  public int getPreferredWidth() {
    return layout.getLayoutWidth();
  }

  @Override
  public int getPreferredHeight() {
    return layout.getLayoutHeight();
  }

  /**
   * Returns the layout used by this container to position its components.
   */
  public final MockLayout getLayout() {
    return layout;
  }

  @Override
  protected TreeItem buildTree() {
    return this.buildTree(1);
  }

  protected TreeItem buildTree(int view) {
    TreeItem itemNode = super.buildTree();
    //hide all containers except form if only nonvisible components are to be shown
    //in such a case, we need only the form's treeItem because all non-visible components are attached to it
    itemNode.setVisible(view != 3 || isForm());

    // Recursively build the tree for child components
    for (MockComponent child : children) {
      TreeItem childNode = child.buildTree();
      boolean isVisible = true;
      if (view == 2 && child instanceof MockNonVisibleComponent) {
        isVisible = false;
      } else if (view == 3 && child instanceof MockVisibleComponent) {
        isVisible = false;
      }
      childNode.setVisible(isVisible);
      itemNode.addItem(childNode);
    }

    itemNode.setState(expanded);

    return itemNode;
  }

  @Override
  public void collectTypesAndIcons(Map<String, String> typesAndIcons) {
    super.collectTypesAndIcons(typesAndIcons);
    for (MockComponent child : children) {
      child.collectTypesAndIcons(typesAndIcons);
    }
  }


  /**
   * Adds a new component to the end of this container.
   *
   * @param component  component to be added
   */
  public final void addComponent(MockComponent component) {
    addComponent(component, -1);
  }

  /**
   * Adds a new visible component to the container at the specified visible-index.
   *
   * @param component  visible component to be added
   * @param beforeVisibleIndex  visible-index at which the inserted component will appear,
   *                            or {@code -1} to insert the component at the end
   */
  public final void addVisibleComponent(MockComponent component, int beforeVisibleIndex) {
    List<MockComponent> visibleChildren = getShowingVisibleChildren();

    int beforeActualIndex;
    if ((beforeVisibleIndex == -1) || (beforeVisibleIndex >= visibleChildren.size())) {
      // Insert after last visible component
      if (visibleChildren.size() == 0) {
        beforeActualIndex = 0;
      } else {
        beforeActualIndex = getChildren().indexOf(
            /* lastVisibleChild */ visibleChildren.get(visibleChildren.size() - 1)) + 1;
      }
    } else {
      // Insert before the specified visible component
      beforeActualIndex = getChildren().indexOf(visibleChildren.get(beforeVisibleIndex));
    }

    addComponent(component, beforeActualIndex);
  }

  /**
   * Called when a component is pasted into this container in case additional
   * processing is needed.
   *
   * @param child the child component that was pasted
   */
  public void onPaste(MockComponent child) {
    // Provided for subclasses
  }

  /**
   * Adds a new component to the container at the specified index.
   *
   * @param component  component to be added
   * @param beforeIndex  index at which the inserted component will reside in the children,
   *                     or {@code -1} to insert the component at the end
   */
  private void addComponent(MockComponent component, int beforeIndex) {

    // Set the container to be the parent of the component
    component.setContainer(this);

    // Add the component as a child component of the container
    if (beforeIndex == -1) {
      children.add(component);
    } else {
      children.add(beforeIndex, component);
    }

    // Components with a visible representation require a re-layout of the container
    // (note that non-visible components will only be added to the editor's non-visible components
    // panel)
    if (component.isVisibleComponent()) {
      // NOTE: The order of widgets in the root panel does not necessarily
      //       match the order of their associated children of this container
      rootPanel.add(component);
      refreshForm();
    }

    getRoot().fireComponentAdded(component);
  }

  /**
   * Adds a new component to the container at the specified left and top margins.
   *
   * @param component component to be added
   * @param left left margin of the component inside the container
   * @param top top margin of the component inside the container
   */
  public final void addComponent(MockComponent component, int left, int top) {
    List<MockComponent> visibleChildren = getShowingVisibleChildren();

    int beforeActualIndex;

    if (visibleChildren.size() == 0) {
      beforeActualIndex = 0;
    } else {
      beforeActualIndex = getChildren().indexOf(visibleChildren.get(visibleChildren.size() - 1))
          + 1;
    }

    component.setContainer(this);
    children.add(beforeActualIndex, component);

    if (component.isVisibleComponent()) {
      rootPanel.add(component, left, top);
      refreshForm();
    }

    getForm().fireComponentAdded(component);
  }

  /**
   * Removes a component from the container (assumes that component is a child
   * component of the container).  If the component itself contains other
   * components, we first ask for confirmation.
   *
   * @param component  component to be removed
   * @param permanentlyDeleted true if the component is being permanently
   *        deleted, false if the component is being moved from one container
   *        to another container
   */
  public void removeComponent(MockComponent component, boolean permanentlyDeleted) {

    // Remove the component from the list of child components
    children.remove(component);

    // Removal of components with a visible representation requires a re-layout of the container
    if (component.isVisibleComponent()) {
      rootPanel.remove(component);
      if (permanentlyDeleted) {
        refreshForm();
      }
    } else {
      editor.getNonVisibleComponentsPanel().removeComponent(component);
    }

    getRoot().fireComponentRemoved(component, permanentlyDeleted);
  }

  /**
   * Returns the GWT root panel that displays the children of this container.
   * <p>
   * This method should only be called by layout managers.
   */
  final AbsolutePanel getRootPanel() {
    return rootPanel;
  }

  /**
   * Sets the size and position of the child component within the container.
   * Sizes and positions are given in pixels.
   * <p>
   * This method should only be called by layout managers.
   *
   * @param child  component to be sized and positioned
   * @param childLayoutInfo the layout info for the child
   * @param x  new relative x coordinate for the component
   * @param y  new relative y coordinate for the component
   */
  final void setChildSizeAndPosition(MockComponent child, LayoutInfo childLayoutInfo,
      int x, int y) {
    child.setPixelSize(childLayoutInfo.width, childLayoutInfo.height);
    // Note that the actual size of the child will be larger than childLayoutInfo.width X
    // childLayoutInfo.height because the actual size will include the CSS border.
    rootPanel.setWidgetPosition(child, x, y);
  }

  // DropTarget implementation

  @Override
  public final Widget getDropTargetWidget() {
    return getRootPanel();
  }

  /**
   * Indicates whether a component from the given source can be placed in
   * this container.
   *
   * @param source
   * @return true if the component is acceptable, false otherwise
   */
  protected boolean acceptableSource(DragSource source) {
    MockComponent component = null;
    if (source instanceof MockComponent) {
      component = (MockComponent) source;
    } else if (source instanceof SimplePaletteItem) {
      component = (MockComponent) source.getDragWidget();
    }
    if (component instanceof MockVisibleComponent) {
      // Sprites are only allowed on Canvas, not other containers.
      // Map features are only allowed on Map, not other containers.
      // Chart Data components are only allowed on Charts, not other containers.
      if (!(component instanceof MockSprite) && !(component instanceof MockMapFeature)
              && !(component instanceof MockChartData)) {
        return true;
      }
    }
    return false;
  }

  public boolean willAcceptComponentType(String type) {
    return !MockCanvas.ACCEPTABLE_TYPES.contains(type) && !MockMap.ACCEPTABLE_TYPES.contains(type);
  }

  /**
   * Indicates whether a component of the given type can be pasted into this container. By default,
   * this is the same as {@link #willAcceptComponentType(String)}. Subclasses may override this
   * behavior if they have custom rules.
   *
   * @param type the type of the component being considered for pasting
   * @return true if the component can be pasted, false otherwise
   */
  public boolean canPasteComponentOfType(String type) {
    return willAcceptComponentType(type);
  }

  // TODO(user): Draw a colored border around the edges of the container
  //                    area while an eligible component is hovering over it.
  @Override
  public final boolean onDragEnter(DragSource source, int x, int y) {
    boolean accept = acceptableSource(source);
    if (accept) {
      layout.onDragEnter(x, y);
    }
    return accept;
  }

  @Override
  public final void onDragContinue(DragSource source, int x, int y) {
    layout.onDragContinue(x, y);
  }

  @Override
  public final void onDragLeave(DragSource source) {
    layout.onDragLeave();
  }

  @Override
  public final void onDrop(DragSource source, int x, int y, int offsetX, int offsetY) {
    Preconditions.checkArgument(acceptableSource(source));

    MockComponent sourceComponent;
    MockContainer sourceContainer = null;
    boolean updatePropertiesPanel = false;
    if (source instanceof MockComponent) {
      // preexisting component already elsewhere in the form
      sourceComponent = (MockComponent) source;
      sourceContainer = sourceComponent.getContainer();
    } else if (source instanceof SimplePaletteItem) {
      // new component generated by a palette item
      sourceComponent = ((SimplePaletteItem) source).createMockComponent();
    } else {
      throw new IllegalArgumentException();
    }

    // handle change of visibility of x and y coordinate properties if
    // component is visible
    if (this instanceof MockAbsoluteArrangement
        && sourceComponent instanceof MockVisibleComponent) {
      ((MockVisibleComponent) sourceComponent).setCoordPropertiesVisible(true);
      if (editor instanceof YaFormEditor) {
        updatePropertiesPanel = true;
      }
    } else if (sourceComponent instanceof MockVisibleComponent) {
      ((MockVisibleComponent) sourceComponent).setCoordPropertiesVisible(false);
      if (sourceContainer instanceof MockAbsoluteArrangement) {
        if (editor instanceof YaFormEditor) {
          updatePropertiesPanel = true;
        }
      }
    }

    if (layout.onDrop(sourceComponent, x, y, offsetX, offsetY)) {
      sourceComponent.select(null);
      if (updatePropertiesPanel) {
        // update properties panel using YaFormEditor
        ((YaFormEditor) editor).refreshCurrentPropertiesPanel();
      }
    }
  }

  public List<DropTarget> getDropTargetsWithin() {
    ArrayList<DropTarget> targets = new ArrayList<DropTarget>();
    for (MockComponent child : children) {
      if (child instanceof MockContainer) {
        targets.addAll(((MockContainer) child).getDropTargetsWithin());
      }
    }
    targets.add(this);
    return targets;
  }

  @Override
  public void delete() {
    // Traverse list backwards to make removal easier
    for (int i = children.size() - 1; i >= 0; --i) {
      MockComponent child = children.get(i);

      // Manually delete child component to ensure that it is
      // completely removed from the Designer.
      child.delete();
    }

    super.delete();
  }

  @Override
  LayoutInfo createLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    return layout.createContainerLayoutInfo(layoutInfoMap);
  }
}
