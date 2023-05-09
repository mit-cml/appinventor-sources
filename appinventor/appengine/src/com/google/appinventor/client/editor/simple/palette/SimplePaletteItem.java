// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ComponentCoverage;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockComponentsUtil;
import com.google.appinventor.client.widgets.dnd.DragSourcePanel;
import com.google.appinventor.client.widgets.dnd.DragSourceSupport;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

/**
 * This class represents a component on the component palette panel.
 *
 */
public class SimplePaletteItem extends DragSourcePanel {
  // Queried to determine the set of UI elements that accept drops of palette items
  private DropTargetProvider dropTargetProvider;

  // Component descriptor (needed for mock component instantiation)
  private SimpleComponentDescriptor scd;

  //Getting an instance of ComponentConverge class to get the component platform support information
  private ComponentCoverage componentCoverage = ComponentCoverage.getInstance();

  private static final Images images = Ode.getImageBundle();

  // Cached prototype of the component that this palette item creates.
  // Properties of the prototype may be queried by accessors.
  private MockComponent componentPrototype;

  //It is here to keep the selected panel item
  private static Widget selectedPaletteItemWidget;

  /**
   * Creates a new palette item.
   *
   * @param scd component descriptor for palette item
   * @param dropTargetProvider provider of targets that palette items can be dropped on
   */
  public SimplePaletteItem(SimpleComponentDescriptor scd, DropTargetProvider dropTargetProvider) {
    this.dropTargetProvider = dropTargetProvider;
    this.scd = scd;

    componentPrototype = null;

    // Initialize palette item UI
    HorizontalPanel panel = new HorizontalPanel();
    panel.setStylePrimaryName("ode-SimplePaletteItem");

    Image image = scd.getImage();
    image.setStylePrimaryName("ode-SimplePaletteItem-icon");
    panel.add(image);
    panel.setCellHorizontalAlignment(image, HorizontalPanel.ALIGN_LEFT);
    panel.setCellWidth(image, "30px");

    Label label = new Label(ComponentsTranslation.getComponentName(scd.getName()));
    label.setHorizontalAlignment(Label.ALIGN_LEFT);
    label.addStyleName("ode-SimplePaletteItem-caption");
    panel.add(label);

    HorizontalPanel optPanel = new HorizontalPanel();

    boolean isAndroidCompatible = componentCoverage.isAndroidCompatible(scd.getName());
    boolean isIosCompatible = componentCoverage.isIosCompatible(scd.getName());

    if(!isAndroidCompatible){
      //display apple icon if the component is available only in ios
      Image iosImage = new Image(images.iosLogo());
      iosImage.setWidth("16px");
      iosImage.setHeight("16px");
      iosImage.setStylePrimaryName("ode-SimplePaletteItem-icon");
      optPanel.add(iosImage);
      optPanel.setCellHorizontalAlignment(iosImage, HorizontalPanel.ALIGN_LEFT);
      optPanel.setCellWidth(iosImage, "30px");
    }
    else if(!isIosCompatible) {
      //display android icon if the component is available only in android
      Image androidImage = new Image(images.androidLogo());
      androidImage.setWidth("16px");
      androidImage.setHeight("16px");
      androidImage.setStylePrimaryName("ode-SimplePaletteItem-icon");
      optPanel.add(androidImage);
      optPanel.setCellHorizontalAlignment(androidImage, HorizontalPanel.ALIGN_LEFT);
      optPanel.setCellWidth(androidImage, "30px");
    }

    ComponentHelpWidget helpImage = new ComponentHelpWidget(scd);
    optPanel.add(helpImage);
    optPanel.setCellHorizontalAlignment(helpImage, HorizontalPanel.ALIGN_LEFT);

    if (scd.getExternal()) {
      ComponentRemoveWidget deleteImage = new ComponentRemoveWidget(scd);
      optPanel.add(deleteImage);
      optPanel.setCellHorizontalAlignment(deleteImage, HorizontalPanel.ALIGN_RIGHT);
    }

    panel.add(optPanel);
    panel.setCellHorizontalAlignment(optPanel, HorizontalPanel.ALIGN_RIGHT);

    panel.setWidth("100%");
    add(panel);
    setWidth("100%");

    addHandlers();
  }

  /**
   * Selects (sets the background to green of) a palette item when it is clicked.
   *
   * @param paletteItemWidget the Widget of the panel item to be selected
   */
  private static void select(Widget paletteItemWidget) {
    if (selectedPaletteItemWidget != null) {
      selectedPaletteItemWidget.getElement().getStyle().setProperty("backgroundColor", "white");
    }
    selectedPaletteItemWidget = paletteItemWidget;
    selectedPaletteItemWidget.getElement().getStyle().setProperty("backgroundColor", "#d2e0a6");
  }

  private void addHandlers() {
    addMouseDownHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent arg0) {
        select(getWidget());
      }
    });
    addTouchStartHandler(new TouchStartHandler() {
      @Override
      public void onTouchStart(TouchStartEvent event) {
        select(getWidget());
      }
    });
  }

  /**
   * Returns a new mock component for the palette item.
   * <p>
   * The caller is assumed to take ownership of the returned component.
   *
   * @return mock component
   */
  public MockComponent createMockComponent() {
    cacheInternalComponentPrototype();

    MockComponent returnedComponentPrototype = componentPrototype;
    componentPrototype = null;
    return returnedComponentPrototype;
  }

  /**
   * Returns whether this palette item creates components with a visual representation.
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
