// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.TranslationDesignerPallete;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockComponentsUtil;
import com.google.appinventor.client.widgets.dnd.DragSourcePanel;
import com.google.appinventor.client.widgets.dnd.DragSourceSupport;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class represents a component on the component palette panel.
 *
 */
public class SimplePaletteItem extends DragSourcePanel {
  // Queried to determine the set of UI elements that accept drops of palette items
  private DropTargetProvider dropTargetProvider;

  // Component descriptor (needed for mock component instantiation)
  private SimpleComponentDescriptor scd;

  // Cached prototype of the component that this palette item creates.
  // Properties of the prototype may be queried by accessors.
  private MockComponent componentPrototype;

  /**
   * Creates a new palette item.
   *
   * @param scd  component descriptor for palette item
   * @param dropTargetProvider  provider of targets that palette items can be dropped on
   */
  public SimplePaletteItem(SimpleComponentDescriptor scd, DropTargetProvider dropTargetProvider) {
    this.dropTargetProvider = dropTargetProvider;
    this.scd = scd;

    componentPrototype = null;

    // Initialize palette item UI
    HorizontalPanel panel = new HorizontalPanel();
    panel.setStylePrimaryName("ode-SimplePaletteItem");

    Image image = scd.getImage();
    panel.add(image);
    panel.setCellHorizontalAlignment(image, HorizontalPanel.ALIGN_LEFT);
    panel.setCellWidth(image, "30px");

    Label label = new Label(TranslationDesignerPallete.getCorrespondingString(scd.getName()));
    label.setHorizontalAlignment(Label.ALIGN_LEFT);
    label.addStyleName("ode-SimplePaletteItem-caption");
    panel.add(label);

    ComponentHelpWidget helpImage = new ComponentHelpWidget(scd);
    panel.add(helpImage);
    panel.setCellHorizontalAlignment(helpImage, HorizontalPanel.ALIGN_RIGHT);

    panel.setWidth("100%");
    add(panel);
    setWidth("100%");
  }

  /**
   * Returns a new mock component for the palette item.
   * <p>
   * The caller is assumed to take ownership of the returned component.
   *
   * @return  mock component
   */
  public MockComponent createMockComponent() {
    cacheInternalComponentPrototype();

    MockComponent returnedComponentPrototype = componentPrototype;
    componentPrototype = null;
    return returnedComponentPrototype;
  }

  /**
   * Returns whether this palette item creates components with a
   * visual representation.
   */
  public boolean isVisibleComponent() {
    cacheInternalComponentPrototype();
    return componentPrototype.isVisibleComponent();
  }

  private void cacheInternalComponentPrototype() {
    if (componentPrototype == null) {
      componentPrototype = scd.createMockComponentFromPalette();
    }
  }

  // DragSource implementation

  @Override
  public void onDragStart() {
    // no action
  }

  @Override
  public Widget createDragWidget(int x, int y) {
    MockComponent component = createMockComponent();
    // Some components override getPreferredWidth/Height because getOffsetWidth/Height (which is
    // what MockComponentsUtil.getPreferredSizeOfDetachedWidget uses) returns very inaccurate
    // values. These components can give us the width/height even when the component is not
    // attached.
    int width = component.getPreferredWidth();
    int height = component.getPreferredHeight();
    if (width <= 0 && height <= 0) {
      // Other components don't override getPreferredWidth/Height, which means that we'll get 0 (or
      // less) because the component is not attached. So, we use getPreferredSizeOfDetachedWidget.
      int[] size = MockComponentsUtil.getPreferredSizeOfDetachedWidget(component);
      width = size[0];
      height = size[1];
    }
    component.setPixelSize(width, height);
    DragSourceSupport.configureDragWidgetToAppearWithCursorAt(component, width / 2, height / 2);
    return component;
  }

  @Override
  public Widget getDragWidget() {
    return dragSourceSupport.getDragWidget();
  }

  @Override
  public DropTarget[] getDropTargets() {
    return dropTargetProvider.getDropTargets();
  }

  @Override
  public void onDragEnd() {
    // no action
  }

  // Utility methods

  public String getName() {
      return scd.getName();
  }
}
