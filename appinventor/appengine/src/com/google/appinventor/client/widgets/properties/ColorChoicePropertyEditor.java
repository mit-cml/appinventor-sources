// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

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
      return Color.getHtmlDescription(rgbString, name);
    }

    static String getHtmlDescription(String rgbString, String name) {
      return "<span style=\"background:#" + rgbString + "; border:1px solid black; " +
          "width:1em; height:1em\">&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;&nbsp;" + name;
    }
  }

  // UI for the list of colors will be represented by a ContextMenu
  private final DropDownButton selectedColorMenu;

  // Prefix for hex numbers
  private final String hexPrefix;

  /**
   * Flag indicating whether the advanced (custom) color picker is available.
   */
  private final boolean advanced;

  // Colors
  private final Color[] colors;

  /**
   * The default value of the color, shown when Default is selected.
   */
  private final String defaultValue;

  // Widget Name
  private static final String WIDGET_NAME = "Color Choice Property Editor";

  /**
   * Panel to hold the color picker palette and associated controls.
   */
  private PopupPanel advancedPanel;

  /**
   * Reference to the native palette picker.
   */
  private JavaScriptObject palettePicker;

  /**
   * Command to show the native picker.
   */
  private Command showCustomPicker;

  /**
   * The default color value, as a numeric value.
   */
  private long defaultValueArgb;

  /**
   * Creates a new instance of the property editor.
   *
   * @param hexPrefix  language specific hex number prefix
   * @param colors  colors to be shown in property editor - must not be
   *                {@code null} or empty
   */
  public ColorChoicePropertyEditor(final Color[] colors, final String hexPrefix, final String defaultValue) {
    this(colors, hexPrefix, defaultValue, false);
  }

  /**
   * Creates a new instance of the property editor.
   *
   * @param colors  language specific hex number prefix
   * @param hexPrefix  colors to be shown in property editor - must not be
   *                   {@code null} or empty
   * @param defaultValue  the color of the default value, for display in the editor only
   * @param advanced  specify true to show a button for the advanced picker
   */
  public ColorChoicePropertyEditor(final Color[] colors, final String hexPrefix, final String defaultValue, final boolean advanced) {
    this.hexPrefix = hexPrefix;
    this.colors = colors;
    this.advanced = advanced;
    if (defaultValue.startsWith(hexPrefix)) {
      this.defaultValue = defaultValue.substring(defaultValue.length()-6);  // Take last 6 digits (assumes RRGGBB format)
      this.defaultValueArgb = Long.valueOf("FF" + this.defaultValue, 16) & 0xFFFFFFFFL;
    } else {
      this.defaultValue = defaultValue;
      this.defaultValueArgb = Long.valueOf(defaultValue, 10) & 0xFFFFFFFFL;
    }

    // Initialize UI
    List<DropDownItem> choices = Lists.newArrayList();
    for (final Color color : colors) {
      final String description = color.argbValue == 0 ?
          Color.getHtmlDescription(this.defaultValue, color.name) : color.getHtmlDescription();
      choices.add(new DropDownItem(WIDGET_NAME, description, new Command() {
        @Override
        public void execute() {
          if (color.argbValue == 0) {
            // Handle default value specially to prevent sending #x00000000 to the REPL...
            property.setValue(defaultValue);
          } else {
            property.setValue(hexPrefix + color.alphaString + color.rgbString);
          }
          if (advanced) {
            String customColor = color.argbValue == 0 ?
                Color.ALPHA_OPAQUE + ColorChoicePropertyEditor.this.defaultValue :
                color.alphaString + color.rgbString;
            selectedColorMenu.replaceLastItem(new DropDownItem(WIDGET_NAME, makeCustomHTML(customColor), showCustomPicker));
          }
        }
      }));
    }
    if (advanced) {
      SimplePanel container = new SimplePanel();
      prepareCustomColorPicker(container.getElement());
      showCustomPicker = new Command() {
        @Override
        public void execute() {
          showCustomColorPicker();
        }
      };

      Button cancelButton = new Button();
      cancelButton.setText(MESSAGES.cancelButton());
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          dismissAdvancedPicker();
        }
      });

      Button doneButton = new Button();
      doneButton.setText(MESSAGES.done());
      doneButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String color = getColor().toUpperCase();
          dismissAdvancedPicker();
          property.setValue(color);
          selectedColorMenu.replaceLastItem(new DropDownItem(WIDGET_NAME, makeCustomHTML(color), showCustomPicker));
        }
      });

      HorizontalPanel buttons = new HorizontalPanel();
      buttons.add(cancelButton);
      buttons.add(doneButton);

      VerticalPanel panel = new VerticalPanel();
      panel.add(container);
      panel.add(buttons);

      advancedPanel = new PopupPanel();
      advancedPanel.add(panel);
      advancedPanel.setAutoHideEnabled(true);

      choices.add(new DropDownItem(WIDGET_NAME, makeCustomHTML(1.0, 255, 0, 0), new Command() {
        @Override
        public void execute() {
          showCustomColorPicker();
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
    // Screens can be loaded in an arbitrary order and if Screen1 is not the first screen loaded,
    // then a value of "" will be sent. Just ignore it because it will be corrected after Screen1
    // is loaded.
    if (propertyValue == null || propertyValue.isEmpty()) {
      return;
    }
    int radix = 10;
    if (propertyValue.startsWith(hexPrefix)) {
      propertyValue = propertyValue.substring(hexPrefix.length());
      radix = 16;
    }

    long argbValue = Long.valueOf(propertyValue, radix) & 0xFFFFFFFFL;
    if (argbValue == this.defaultValueArgb || argbValue == 0) {
      selectedColorMenu.setHTML(Color.getHtmlDescription(defaultValue, Ode.MESSAGES.defaultColor()));
      if (advanced) {
        selectedColorMenu.replaceLastItem(new DropDownItem(WIDGET_NAME, makeCustomHTML(this.defaultValueArgb), showCustomPicker));
        setPickerColor(this.defaultValueArgb);
      }
      return;
    }
    String html = makeCustomHTML(argbValue);
    for (final Color color : colors) {
      if (color.argbValue == argbValue) {
        selectedColorMenu.setHTML(color.getHtmlDescription());
        if (advanced) {
          // Update the custom picker state
          selectedColorMenu.replaceLastItem(new DropDownItem(WIDGET_NAME, html, showCustomPicker));
          setPickerColor(argbValue);
        }
        return;
      }
    }
    if (advanced) {
      // No predefined color matched, so assume a custom value.
      selectedColorMenu.setHTML(html);
      selectedColorMenu.replaceLastItem(new DropDownItem(WIDGET_NAME, html, showCustomPicker));
      setPickerColor(argbValue);
    }
  }

  /**
   * Make a HTML string for the custom picker dropdown menu item.
   *
   * @param a Alpha channel value of the color in [0, 1]
   * @param r Red channel value of the color in [0, 255]
   * @param g Green channel value of the color in [0, 255]
   * @param b Blue channel value of the color in [0, 255]
   * @return HTML string for the Custom... dropdown entry
   */
  private static String makeCustomHTML(double a, int r, int g, int b) {
    return "<span style=\"background:rgba(" + r + "," + g + "," + b + "," + a + "); border:1px solid black; " +
        "width:1em; height:1em\">&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;&nbsp;" + MESSAGES.customEllipsis();
  }

  /**
   * Make a HTML string for the custom picker dropdown menu item.
   *
   * @param argbValue Long storing the color in ARGB format
   * @return HTML string for the Custom... dropdown entry
   */
  private static String makeCustomHTML(long argbValue) {
    int b = (int)(argbValue & 0xFF);
    int g = (int)((argbValue >> 8) & 0xFF);
    int r = (int)((argbValue >> 16) & 0xFF);
    double a = (double)((argbValue >> 24) & 0xFF) / 255.0;
    return makeCustomHTML(a, r, g, b);
  }

  /**
   * Make a HTML string for the custom picker dropdown menu item.
   *
   * @param argb String storing the color in ARGB hex format
   * @return HTML string for the Custom... dropdown entry
   */
  private String makeCustomHTML(String argb) {
    if (argb.startsWith(hexPrefix)) {
      argb = argb.substring(hexPrefix.length());
    }
    int a = Integer.parseInt(argb.substring(0, 2), 16);
    int r = Integer.parseInt(argb.substring(2, 4), 16);
    int g = Integer.parseInt(argb.substring(4, 6), 16);
    int b = Integer.parseInt(argb.substring(6, 8), 16);
    double aDouble = (double) a / 255.0;
    return makeCustomHTML(aDouble, r, g, b);
  }

  private void showCustomColorPicker() {
    advancedPanel.showRelativeTo(selectedColorMenu);
    redrawPanel();
  }

  private void dismissAdvancedPicker() {
    advancedPanel.hide();
  }

  private void setPickerColor(long argbValue) {
    String b = Integer.toHexString((int) (argbValue & 0xFF));
    String g = Integer.toHexString((int) ((argbValue >> 8) & 0xFF));
    String r = Integer.toHexString((int) ((argbValue >> 16) & 0xFF));
    String a = Integer.toHexString((int) ((argbValue >> 24) & 0xFF));
    if (b.length() < 2) b = "0" + b;
    if (g.length() < 2) g = "0" + g;
    if (r.length() < 2) r = "0" + r;
    if (a.length() < 2) a = "0" + a;
    setPickerColorNative("#" + r + g + b + a);
  }

  // JSNI Methods
  private native void prepareCustomColorPicker(Element parent)/*-{
    var picker = new goog.ui.HsvaPalette(new goog.dom.DomHelper(top.document), null, 1, 'goog-hsva-palette-sm');
    picker.render(parent);
    this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::palettePicker = picker;
  }-*/;

  private native String getColor()/*-{
    var palette = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::palettePicker;
    var color = palette.getColorRgbaHex();  // Color format is #rrggbbaa
    var prefix = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::hexPrefix;
    return prefix + color.substr(7, 2) + color.substr(1, 6);
  }-*/;

  private native void setPickerColorNative(String rgba)/*-{
    var palette = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::palettePicker;
    palette.setColorRgbaHex(rgba);
  }-*/;

  private native void redrawPanel()/*-{
    var palette = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::palettePicker;
    setTimeout(function() {
      palette.setColorRgbaHex(palette.getColorRgbaHex());
      palette.updateUi();
    });
  }-*/;
}
