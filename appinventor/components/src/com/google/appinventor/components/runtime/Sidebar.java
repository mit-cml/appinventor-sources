// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Component for sidebar used to hold MenuItems. Sidebar appears on touching
 * the drawer icon on the ActionBar or by swiping from left edge of the screen.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
@DesignerComponent(version = YaVersion.SIDEBAR_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT,
    description = "Component for sidebar used to hold MenuItems. Sidebar appears on touching " +
        "the drawer icon on the ActionBar or by swiping from left edge of the screen.",
    showOnPalette = false)
@SimpleObject
public class Sidebar extends AndroidViewComponent implements Component, ComponentContainer {

  private ComponentContainer container;
  private Menu sidebarMenu;

  private List<MenuItem> items;

  /**
   * Creates a new AndroidViewComponent.
   *
   * @param container container, component will be placed in
   */
  public Sidebar(ComponentContainer container) {
    super(container);
    this.container = container;
    items = new ArrayList<>();
    sidebarMenu = $form().navigationView.getMenu();
    sidebarMenu.clear();
    $form().navigationView.removeHeaderView($form().navigationView.getHeaderView(0));
    for (MenuItem item : items) {
      item.addToSidebarMenu(sidebarMenu);
    }
  }

  public void addSidebarItem(MenuItem item) {
    items.add(item);
    if (sidebarMenu != null) {
      item.addToSidebarMenu(sidebarMenu);
    }
  }

  public void addHeader(SidebarHeader sidebarHeader) {
    if (sidebarHeader.viewLayout.getLayoutManager().getParent() != null) {
      ((ViewGroup) sidebarHeader.viewLayout.getLayoutManager().getParent()).removeView(sidebarHeader.viewLayout.getLayoutManager());
    }
    $form().navigationView.addHeaderView(sidebarHeader.viewLayout.getLayoutManager());
  }

  @Override
  public View getView() {
    return $form().navigationView;
  }

  @Override
  public Activity $context() {
    return container.$context();
  }

  @Override
  public Form $form() {
    return container.$form();
  }

  @Override
  public void $add(AndroidViewComponent component) {
  }

  @Override
  public void setChildWidth(AndroidViewComponent component, int width) {
  }

  @Override
  public void setChildHeight(AndroidViewComponent component, int height) {
  }

  @Override
  public void setChildNeedsLayout(AndroidViewComponent component) {
  }

  @Override
  public List<? extends Component> getChildren() {
    return Collections.unmodifiableList(items);
  }
}
