// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import java.util.Map;

/**
 * Defines the abstract base class for classes that control the layout of mock
 * containers.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class MockLayout {
  static final int BORDER_SIZE = MockComponent.BORDER_SIZE;

  protected int layoutWidth;
  protected int layoutHeight;

  protected MockContainer container;

  /**
   * Sets the container for this layout.
   */
  final void setContainer(MockContainer container) {
    this.container = container;
  }

  /*
   * Return the width of the layout
   */
  final int getLayoutWidth() {
    return layoutWidth;
  }

  /*
   * Return the height of the layout
   */
  final int getLayoutHeight() {
    return layoutHeight;
  }

  /**
   * Create LayoutInfo to hold information needed during layout.
   */
  abstract LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap);

  /**
   * Lays out the children of the container.
   */
  abstract void layoutChildren(LayoutInfo containerLayoutInfo);

  /**
   * @see com.google.appinventor.client.widgets.dnd.DropTarget#
   *      onDragEnter(com.google.appinventor.client.widgets.dnd.DragSource, int, int)
   */
  void onDragEnter(int x, int y) {
  }

  /**
   * @see com.google.appinventor.client.widgets.dnd.DropTarget#
   *      onDragContinue(com.google.appinventor.client.widgets.dnd.DragSource, int, int)
   */
  void onDragContinue(int x, int y) {
  }

  /**
   * @see com.google.appinventor.client.widgets.dnd.DropTarget#
   *      onDragLeave(com.google.appinventor.client.widgets.dnd.DragSource)
   */
  void onDragLeave() {
  }

  /**
   * @param source  the component that was dropped on the layout's container
   * @see com.google.appinventor.client.widgets.dnd.DropTarget#
   *      onDrop(com.google.appinventor.client.widgets.dnd.DragSource, int, int, int, int)
   * @return true if the source component was added to the layout's container,
   *         false otherwise
   */
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    return false;
  }

  /**
   * Disposes of this layout and any resources it has acquired (especially any
   * DOM nodes attached to the associated container).
   */
  void dispose() {
  }
}
