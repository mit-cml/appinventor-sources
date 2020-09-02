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
 * Mock Context Menu component.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
public class MockContextMenu extends MockContainer {
  public static final String TYPE = "ContextMenu";
  private AbsolutePanel menuWidget;

  /**
   * Creates a new MockContextMenu component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockContextMenu(SimpleEditor editor) {
    super(editor, TYPE, images.menu(), new MockHVLayout(ComponentConstants.LAYOUT_ORIENTATION_VERTICAL));

    rootPanel.setHeight("100%");
    menuWidget = new AbsolutePanel();
    menuWidget.setStylePrimaryName("ode-SimpleMockContainer");
    menuWidget.addStyleName("ode-SimpleMockFormMenu");
    menuWidget.add(rootPanel);

    initComponent(menuWidget);
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
    setVisible(selected);
    super.onSelectedChange(selected);
  }
}