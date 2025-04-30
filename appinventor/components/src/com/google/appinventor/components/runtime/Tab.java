// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;

import java.io.IOException;

/**
 * Use a tab component to add a new tab in a tab arrangement.
 *
 * This component extends `HVArrangement`, within which the components of this particular tab can be placed.
 * The components are vertically aligned within a tab.
 *
 * This component can be placed only in a `TabArrangement` component.
 *
 * @author jsuyash1514@gmail.com (Suyash Jain)
 */
@DesignerComponent(version = YaVersion.TAB_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT,
    description = "<p>A component used to add a new tab in a tab arrangement.</p>")
@SimpleObject
public class Tab extends HVArrangement<ViewGroup> implements Component, ComponentContainer {
  private static final String LOG_TAG = Tab.class.getSimpleName();
  private com.google.android.material.tabs.TabLayout.Tab tab;
  private String text = "";
  private boolean showText = true;
  private String iconPath = "";
  private Drawable icon = null;
  private boolean showIcon = true;
  public boolean isScrollable = false;

  /**
   * Creates a new Tab component.
   *
   * @param container container, component will be placed in
   */
  public Tab(TabArrangement container) {
    super(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
        new FrameLayout(container.$context()));
    container.addTab(this);
  }

  public TabLayout.Tab getTab() {
    return tab;
  }

  public void setTab(TabLayout.Tab tab) {
    this.tab = tab;
  }

  @Override
  public void Left(int x) {
    // Tabs can only be in TabArrangements
  }

  @Override
  public int Left() {
    return 0;  // Tabs can only be in TabArrangements
  }

  @Override
  public void Top(int y) {
    // Tabs can only be in TabArrangements
  }

  @Override
  public int Top() {
    return 0;  // Tabs can only be in TabArrangements
  }

  /**
   * Specifies the text displayed by the tab label.
   *
   * @param text
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies the text displayed by the tab label.")
  public void Text(String text) {
    this.text = text;
    tab.setText(showText ? text : "");
  }

  /**
   * Returns the text displayed by the tab label.
   *
   * @return text
   */
  @SimpleProperty(description = "Returns the text displayed by the tab label")
  public String Text() {
    CharSequence text = tab.getText();
    return (text == null ? "" : text.toString());
  }

  /**
   * Specifies the visibility of the tab label.
   *
   * @param show true if the tab label should be visible
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies the visibility of the tab label")
  public void ShowText(boolean show) {
    showText = show;
    tab.setText(show ? text : "");
  }

  /**
   * Returns the visibility of the tab label.
   *
   * @return true if the tab label is visible
   */
  @SimpleProperty(description = "Returns the visibility of the tab label")
  public boolean ShowText() {
    return showText;
  }

  /**
   * Specifies the icon displayed in the tab.
   *
   * @param path
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies the icon displayed in the tab")
  public void Icon(String path) {
    try {
      icon = MediaUtil.getBitmapDrawable($form(), path);
      iconPath = path;
      tab.setIcon(showIcon ? icon : null);
    } catch (IOException e) {
    }
  }

  /**
   * Returns the icon displayed in the tab.
   *
   * @return
   */
  @SimpleProperty(description = "Returns the icon displayed in the tab")
  public String Icon() {
    return iconPath;
  }

  /**
   * Specifies the visibility of the tab icon.
   *
   * @param show true if the tab icon should be visible
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies the visibility of the tab icon")
  public void ShowIcon(boolean show) {
    showIcon = show;
    tab.setIcon(show ? icon : null);
  }

  /**
   * Returns the visibility of the tab icon.
   *
   * @return true if the tab icon is visible
   */
  @SimpleProperty(description = "Returns the visibility of the tab icon")
  public boolean ShowIcon() {
    return showIcon;
  }

  /**
   * When checked, there will be a vertical scrollbar on the tab arrangement, and the height of the
   * tab content can exceed the physical height of the tab arrangement. When unchecked, the tab content
   * height is constrained to the height of the tab arrangement.
   *
   * @param isScrollable true if the tab content should be vertically scrollable
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Scrollable(boolean isScrollable) {
    this.isScrollable = isScrollable;
  }

  /**
   * Scrollable property getter method.
   *
   * @return true if the tab content is vertically scrollable
   */
  @SimpleProperty
  public boolean Scrollable() {
    return isScrollable;
  }

  /**
   * Indicates that the user selected the tab.
   */
  @SimpleEvent
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }

  /**
   * Shows the tab contents if not currently active.
   */
  @SimpleFunction
  public void Show() {
    tab.select();
  }
}
