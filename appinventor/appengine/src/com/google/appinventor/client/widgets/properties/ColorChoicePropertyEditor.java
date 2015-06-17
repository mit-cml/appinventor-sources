// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Command;

import java.util.List;

/**
 * Property editor for color properties.
 *
 */
public abstract class ColorChoicePropertyEditor extends PropertyEditor {

  /**
   * Color definitions for property editor.
   */
  public static final class Color {

    /**
     * Constants for most common alpha values: transparent and opaque.
     */
    public static final String ALPHA_TRANSPARENT = "00";
    public static final String ALPHA_OPAQUE = "FF";

    // Color name
    private final String name;

    // Alpha value in hex format without leading hex designator
    private final String alphaString;

    // Color RGB value in hex format without leading hex designator
    private final String rgbString;

    // Color RGB with alpha value
    private final long argbValue;

    /**
     * Creates a new color definition.
     *
     * @param name  color name
     * @param rgb  RGB value for the color - must be 6 hex digits
     */
    public Color(String name, String rgb) {
      this(name, ALPHA_OPAQUE, rgb);
    }

    /**
     * Creates a new color definition.
     *
     * @param name  color name
     * @param alpha  alpha value for the color - must be 2 hex digits -
     * @param rgb  RGB value for the color - must be 6 hex digits
     */
    public Color(String name, String alpha, String rgb) {
      this.name = name;
      alphaString = alpha;
      rgbString = rgb;
      argbValue = Long.valueOf(alpha + rgb, 16);
    }

    /**
     * Returns a description of the color in HTML format.
     *
     * @return  color description
     */
    String getHtmlDescription() {
      return "<span style=\"background:#" + rgbString + "; border:1px solid black; " +
          "width:1em; height:1em\">&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;&nbsp;" + name;
    }
  }

  // UI for the list of colors will be represented by a ContextMenu
  private final DropDownButton selectedColorMenu;
  //private final MenuBar colorPanel;
  //private Color selectedColor;

  // Prefix for hex numbers
  private final String hexPrefix;

  // Colors
  private final Color[] colors;

  // Widget Name
  private static final String WIDGET_NAME = "Color Choice Property Editor";

  /**
   * Creates a new instance of the property editor.
   *
   * @param hexPrefix  language specific hex number prefix
   * @param colors  colors to be shown in property editor - must not be
   *                {@code null} or empty
   */
  public ColorChoicePropertyEditor(final Color[] colors, final String hexPrefix) {
    this.hexPrefix = hexPrefix;
    this.colors = colors;

    // Initialize UI
    List<DropDownItem> choices = Lists.newArrayList();
    for (final Color color : colors) {
      choices.add(new DropDownItem(WIDGET_NAME, color.getHtmlDescription(), new Command() {
        @Override
        public void execute() {
          property.setValue(hexPrefix + color.alphaString + color.rgbString);
        }
      }));
    }
    selectedColorMenu = new DropDownButton(WIDGET_NAME, colors[0].getHtmlDescription(), choices, false,  true, false);

    selectedColorMenu.setStylePrimaryName("ode-ColorChoicePropertyEditor");

    initWidget(selectedColorMenu);
  }

  @Override
  protected void updateValue() {
    // When receiving the property values from the server hex numbers were converted to decimal
    // numbers
    String propertyValue = property.getValue();
    int radix = 10;
    if (propertyValue.startsWith(hexPrefix)) {
      propertyValue = propertyValue.substring(hexPrefix.length());
      radix = 16;
    }

    long argbValue = Long.valueOf(propertyValue, radix) & 0xFFFFFFFFL;
    for (final Color color : colors) {
      if (color.argbValue == argbValue) {
        selectedColorMenu.setHTML(color.getHtmlDescription());
        break;
      }
    }
  }
}
