// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import java.util.Map;

/**
 * A MockLayout for the Form that can switch between linear (vertical stacking) and absolute
 * (free-position drag-and-drop) modes at runtime. Used by MockForm to support the ScreenLayout
 * property.
 */
final class MockSwitchableFormLayout extends MockLayout {

  // The linear layout delegate (always present)
  private final MockFormLayout linearLayout;

  // The absolute layout delegate (lazily created when switching to absolute mode)
  private MockAbsoluteLayout absoluteLayout;

  // Currently active delegate
  private MockLayout activeDelegate;

  /**
   * Creates a new MockSwitchableFormLayout starting in linear mode.
   *
   * @param linearLayout  the MockFormLayout to use for linear mode
   */
  MockSwitchableFormLayout(MockFormLayout linearLayout) {
    this.linearLayout = linearLayout;
    this.activeDelegate = linearLayout;
  }

  /**
   * Returns the inner MockFormLayout (used for alignment property setters).
   */
  MockFormLayout getLinearLayout() {
    return linearLayout;
  }

  /**
   * Switches the active layout mode.
   *
   * @param useAbsolute  true to switch to absolute positioning, false for linear stacking
   */
  void setAbsoluteMode(boolean useAbsolute) {
    if (useAbsolute) {
      if (absoluteLayout == null) {
        absoluteLayout = new MockAbsoluteLayout();
        // Propagate the container (package-private access within the same package)
        absoluteLayout.container = container;
      }
      activeDelegate = absoluteLayout;
    } else {
      if (absoluteLayout != null) {
        absoluteLayout.dispose();
        absoluteLayout = null;
      }
      activeDelegate = linearLayout;
    }
    // Sync dimensions to the new active delegate
    activeDelegate.layoutWidth = layoutWidth;
    activeDelegate.layoutHeight = layoutHeight;
  }

  // Ensure both delegates always have the current container set.
  private void syncContainer() {
    if (container != null) {
      linearLayout.container = container;
      if (absoluteLayout != null) {
        absoluteLayout.container = container;
      }
    }
  }

  @Override
  LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    syncContainer();
    activeDelegate.layoutWidth = layoutWidth;
    activeDelegate.layoutHeight = layoutHeight;
    return activeDelegate.createContainerLayoutInfo(layoutInfoMap);
  }

  @Override
  void layoutChildren(LayoutInfo containerLayoutInfo) {
    activeDelegate.layoutChildren(containerLayoutInfo);
    layoutWidth = activeDelegate.layoutWidth;
    layoutHeight = activeDelegate.layoutHeight;
  }

  @Override
  void onDragEnter(int x, int y) {
    activeDelegate.onDragEnter(x, y);
  }

  @Override
  void onDragContinue(int x, int y) {
    activeDelegate.onDragContinue(x, y);
  }

  @Override
  void onDragLeave() {
    activeDelegate.onDragLeave();
  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    return activeDelegate.onDrop(source, x, y, offsetX, offsetY);
  }

  @Override
  void dispose() {
    linearLayout.dispose();
    if (absoluteLayout != null) {
      absoluteLayout.dispose();
    }
  }
}
