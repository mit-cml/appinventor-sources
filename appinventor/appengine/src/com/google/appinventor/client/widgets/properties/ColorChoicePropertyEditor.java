// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.widgets.properties;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

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

  // UI for the list of colors will be represented by a PopupPanel
  private final MenuBar selectedColorMenu;
  private final MenuBar colorPanel;
  private MenuItem selectedColor;

  // Prefix for hex numbers
  private final String hexPrefix;

  // Colors
  private final Color[] colors;

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
    colorPanel = new MenuBar(true);
    colorPanel.setStylePrimaryName("ode-ContextMenu");
    for (final Color color : colors) {
      MenuItem item = colorPanel.addItem(color.getHtmlDescription(), true, new Command() {
        @Override
        public void execute() {
          property.setValue(hexPrefix + color.alphaString + color.rgbString);
        }
      });
      item.setStylePrimaryName("ode-ContextMenuItem");
    }

    selectedColorMenu = new MenuBar();
    selectedColorMenu.setFocusOnHoverEnabled(false);
    selectedColorMenu.setStylePrimaryName("ode-ColorChoicePropertyEditor");
    selectedColor = selectedColorMenu.addItem(colors[0].getHtmlDescription(), true, colorPanel);
    selectedColor.setStylePrimaryName("ode-CurrentColor");

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
    for (Color color : colors) {
      if (color.argbValue == argbValue) {
        selectedColorMenu.removeItem(selectedColor);
        selectedColor = selectedColorMenu.addItem(color.getHtmlDescription(), true, colorPanel);
        selectedColor.setStylePrimaryName("ode-CurrentColor");
        break;
      }
    }
  }
}
