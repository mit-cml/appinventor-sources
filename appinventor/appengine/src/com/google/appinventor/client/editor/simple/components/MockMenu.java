// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * Mock Menu component.
 *
 * @author xy93@cornell.edu (Steven Ye)
 */
public final class MockMenu extends MockContainer {
  public static final String TYPE = "Menu";
  private AbsolutePanel menuWidget;

  // whether the mock menu is opened or closed
  private boolean open;

  // whether the mock menu is openable (false when Action Bar is absent)
  private boolean enabled;

  /**
   * Creates a new MockMenu component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockMenu(SimpleEditor editor) {
    super(editor, TYPE, images.menu(), new MockHVLayout(ComponentConstants.LAYOUT_ORIENTATION_VERTICAL));

    rootPanel.setHeight("100%");
    menuWidget = new AbsolutePanel();
    menuWidget.setStylePrimaryName("ode-SimpleMockContainer");
    menuWidget.addStyleName("ode-SimpleMockFormMenu");
    menuWidget.add(rootPanel);

    initComponent(menuWidget);
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
  protected boolean acceptableSource(DragSource source) {
    MockComponent component = null;
    if (source instanceof MockComponent) {
      component = (MockComponent) source;
    } else if (source instanceof SimplePaletteItem) {
      component = (MockComponent) source.getDragWidget();
    }
    return component instanceof MockMenuItem;
  }

  @Override
  protected void onSelectedChange(boolean selected) {
    super.onSelectedChange(selected);
    if (selected && !open) {
      toggle();
    }
  }

  /**
   * Whether the menu is shown in designer.
   *
   * @return {@code true} iff menu is visible
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * Set the mock menu's enabled property.
   *
   * @param enabled  {@code true} iff menu can be opened
   */
  public void setEnabled(boolean enabled) {
    if (!enabled && open) {
      toggle();
    }
    this.enabled = enabled;
  }

  /**
   * Toggle (open or close) the mock menu; has no effect when menu is disabled.
   */
  public void toggle() {
    if (enabled) {
      open = !open;
      refreshForm();
    }
  }

}
