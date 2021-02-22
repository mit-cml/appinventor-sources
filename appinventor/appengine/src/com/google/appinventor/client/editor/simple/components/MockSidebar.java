// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * Mock Sidebar component.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
public class MockSidebar extends MockContainer {
  public static final String TYPE = "Sidebar";
  private AbsolutePanel sidebarWidget;

  // whether the mock sidebar is opened or closed.
  private boolean opened;

  // whether the mock sidebar is openable (false when Action Bar is absent)
  private boolean enabled;

  /**
   * Creates a new MockSidebar component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockSidebar(SimpleEditor editor) {
    super(editor, TYPE, images.sidebar(), new MockHVLayout(ComponentConstants.LAYOUT_ORIENTATION_VERTICAL));

    rootPanel.setHeight("100%");
    sidebarWidget = new AbsolutePanel();
    sidebarWidget.setStylePrimaryName("ode-SimpleMockContainer");
    sidebarWidget.addStyleName("ode-SimpleMockFormMenu");
    sidebarWidget.add(rootPanel);

    initComponent(sidebarWidget);
  }

  @Override
  int getHeightHint() {
    int heightHint = super.getHeightHint();
    if (heightHint == LENGTH_PREFERRED) {
      heightHint = getForm().usableScreenHeight;
    }
    return heightHint;
  }

  @Override
  protected boolean acceptableSource(DragSource source) {
    MockComponent component = null;
    if (source instanceof MockComponent) {
      component = (MockComponent) source;
    } else if (source instanceof SimplePaletteItem) {
      component = (MockComponent) source.getDragWidget();
    }
    if (component instanceof MockVisibleComponent) {
      if (component instanceof MockSidebarHeader || component instanceof MockMenuItem) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether the sidebar is shown in designer.
   *
   * @return {@code true} iff sidebar is visible
   */
  public boolean isOpen() {
    return opened;
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  @Override
  protected void onSelectedChange(boolean selected) {
    super.onSelectedChange(selected);
    if (selected && !opened) {
      toggle();
    }
  }

  /**
   * Set the mock sidebar's enabled property.
   *
   * @param enabled {@code true} iff sidebar can be opened
   */
  public void setEnabled(boolean enabled) {
    if (!enabled && opened) {
      toggle();
    }
    this.enabled = enabled;
  }

  /**
   * Toggle (open or close) the mock sidebar; has no effect when sidebar is disabled.
   */
  public void toggle() {
    if (enabled) {
      opened = !opened;
      refreshForm();
    }
  }
}
