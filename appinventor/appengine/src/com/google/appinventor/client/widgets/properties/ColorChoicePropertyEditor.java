// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.CLog;
import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.widgets.TextButton;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import java.util.ArrayList;
import java.util.List;

/**
 * Property editor for color properties.
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
         * @param name color name
         * @param rgb  RGB value for the color - must be 6 hex digits
         */
        public Color(String name, String rgb) {
            this(name, ALPHA_OPAQUE, rgb);
        }

        /**
         * Creates a new color definition.
         *
         * @param name  color name
         * @param alpha alpha value for the color - must be 2 hex digits -
         * @param rgb   RGB value for the color - must be 6 hex digits
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
         * @return color description
         */
        String getHtmlDescription() {
            return Color.getHtmlDescription(rgbString, name);
        }

        static String getHtmlDescription(String rgbString, String name) {
            return "<span style=\"background:#" + rgbString + "; display: inline-block; " +
                    "width:15px; height:15px; border-radius:200px; vertical-align: middle; border:1px solid black;\">&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;&nbsp;" + name;
        }
    }

    // UI for the list of colors will be represented by a ContextMenu
    private final TextButton selectColor;

    // Prefix for hex numbers
    private final String hexPrefix;

    /**
     * The default value of the color, shown when Default is selected.
     */
    private final String defaultValue;

    /**
     * The default color value, as a numeric value.
     */
    private long defaultValueArgb;

    private String pickerColor = "";

    /**
     * Creates a new instance of the property editor.
     *
     * @param hexPrefix    colors to be shown in property editor - must not be
     * {@code null} or empty
     * @param defaultValue the color of the default value, for display in the editor only
     */

    private final List<String> defaultColorsList;

    private final boolean restrict; // for handling LEGO colors
    private final Color[] colors;

    public ColorChoicePropertyEditor(Color[] colors, final String hexPrefix, final String defaultValue, boolean restrict) {
        this.colors = colors;
        this.restrict = restrict;
        defaultColorsList = new ArrayList<>();

        for (Color color : colors) {
            defaultColorsList.add("#" + color.rgbString + color.alphaString);
        }

        this.hexPrefix = hexPrefix;
        if (defaultValue.startsWith(hexPrefix)) {
            this.defaultValue = defaultValue.substring(defaultValue.length() - 6);  // Take last 6 digits (assumes RRGGBB format)
            this.defaultValueArgb = Long.valueOf("FF" + this.defaultValue, 16) & 0xFFFFFFFFL;
        } else {
            this.defaultValue = defaultValue;
            this.defaultValueArgb = Long.valueOf(defaultValue, 10) & 0xFFFFFFFFL;
        }
        selectColor = new TextButton();
        Color color = new Color(MESSAGES.noneColor(), Color.ALPHA_TRANSPARENT, "FFFFFF");
        selectColor.setHTML(color.getHtmlDescription());
        selectColor.setStylePrimaryName("ode-ColorChoicePropertyEditor");
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                showCustomColorPicker();
            }
        }, ClickEvent.getType());
        initWidget(selectColor);
    }

    @Override
    protected void updateValue() {
        // There was a collision so we should show the multiple indicator
        if (isMultipleValues()) {
            selectColor.setHTML(MESSAGES.multipleValues());
            return;
        }
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
        String hex = argbToHex(argbValue);
        if (argbValue == this.defaultValueArgb || argbValue == 0) {
//            String displayValue = hex.endsWith("FF") ? hex.substring(0, hex.length() - 2) : hex;
            selectColor.setHTML(Color.getHtmlDescription(defaultValue, MESSAGES.defaultColor()));
            setPickerColor(this.defaultValueArgb);
            return;
        }
        String html = makeCustomHTML(argbValue);
        for (final Color color : colors) {
            if (color.argbValue == argbValue) {
                selectColor.setHTML(color.getHtmlDescription());
                return;
            }
        }
        selectColor.setHTML(html);
        setPickerColor(argbValue);
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
    private static String makeCustomHTML(long argbValue, double a, int r, int g, int b) {
        String hex = argbToHex(argbValue);
        return "<span style=\"background:rgba(" + r + "," + g + "," + b + "," + a + "); display: inline-block; " +
                "width:15px; height:15px; border-radius:200px; border: 1px solid var(--border-color);\">&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;&nbsp;" + hex;
    }

    /**
     * Make a HTML string for the custom picker dropdown menu item.
     *
     * @param argbValue Long storing the color in ARGB format
     * @return HTML string for the Custom... dropdown entry
     */
    private static String makeCustomHTML(long argbValue) {
        int b = (int) (argbValue & 0xFF);
        int g = (int) ((argbValue >> 8) & 0xFF);
        int r = (int) ((argbValue >> 16) & 0xFF);
        double a = (double) ((argbValue >> 24) & 0xFF) / 255.0;
        return makeCustomHTML(argbValue, a, r, g, b);
    }

    private void showCustomColorPicker() {
        JSONObject defaultColors = new JSONObject();
        for (Color color : colors) {
            defaultColors.put("#" + color.rgbString + color.alphaString, new JSONString(color.name));
        }
        if (restrict) {
            showAdvancedPicker(getElement(), pickerColor, defaultColors.toString());
        } else {
            showPicker(getElement(), pickerColor, getProjectColorsHexString(), defaultColors.toString());
        }
    }

    private String getProjectColorsHexString() {
        YaProjectEditor projectEditor = (YaProjectEditor) Ode.getCurrentProjectEditor();
        if (projectEditor != null) {
            List<String> projectColorsCopy = new ArrayList<>();
            for (String color : projectEditor.getProjectColors()) {
                projectColorsCopy.add("#" + color);
            }
            return String.join(",", projectColorsCopy);
        }
        return "";
    }

    public static String argbToHex(long argbValue) {
        String b = Integer.toHexString((int) (argbValue & 0xFF));
        String g = Integer.toHexString((int) ((argbValue >> 8) & 0xFF));
        String r = Integer.toHexString((int) ((argbValue >> 16) & 0xFF));
        String a = Integer.toHexString((int) ((argbValue >> 24) & 0xFF));
        if (b.length() < 2) b = "0" + b;
        if (g.length() < 2) g = "0" + g;
        if (r.length() < 2) r = "0" + r;
        if (a.length() < 2) a = "0" + a;
        return ("#" + r + g + b + a).toUpperCase();
    }

    private void setPickerColor(long argbValue) {
        this.pickerColor = argbToHex(argbValue);
    }

    private native void showPicker(Element element, String defaultColor, String projectColors, String defaultColors)/*-{
    var prefix = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::hexPrefix;
    var that = this;
    var pickr = $wnd.Pickr.create({
            el: element,
            useAsButton: true,
            theme: 'nano',
            showAlways: false,
            'default': defaultColor,
            sliders: "h",
            position: 'bottom-middle',
            swatches: JSON.parse(defaultColors),
            swatches2: projectColors.split(","),
            components: {
                preview: true,
                opacity: true,
                hue: true,
                interaction: {
                    hex: true,
                    rgba: true,
                    hsla: false,
                    hsva: false,
                    cmyk: false,
                    input: true,
                    cancel: true,
                    save: true
                }
            }
          });

        pickr.on('hide', function(instance){
            pickr.destroyAndRemove();
        });

        pickr.on('cancel', function(instance){
            pickr.destroyAndRemove();
        });

        pickr.on('save', function(instance){
          var color = pickr.getColor().toHEXA().toString();
          if (color.length === 7) {
              color += "FF";
          }
          var finalColor = prefix + color.substr(7, 2) + color.substr(1, 6);
          that.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::selectionChanged(Ljava/lang/String;)(finalColor);
          pickr.destroyAndRemove();
        });

        pickr.show();
  }-*/;

    private native void showAdvancedPicker(Element element, String defaultColor, String defaultColors)/*-{
    var prefix = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::hexPrefix;
    var that = this;
    var pickr = $wnd.Pickr.create({
            el: element,
            useAsButton: true,
            theme: 'nano',
            showAlways: false,
            'default': defaultColor,
            sliders: "h",
            position: 'bottom-middle',
            swatches: JSON.parse(defaultColors),
            swatches2: [],
            components: {
                preview: false,
                opacity: false,
                hue: false,
                interaction: {
                    hex: false,
                    rgba: false,
                    hsla: false,
                    hsva: false,
                    cmyk: false,
                    input: false,
                    cancel: true,
                    save: true
                }
            }
          });

        pickr.on('hide', function(instance){
            pickr.destroyAndRemove();
        });

        pickr.on('cancel', function(instance){
            pickr.destroyAndRemove();
        });

        pickr.on('save', function(instance){
          var color = pickr.getColor().toHEXA().toString();
          if (color.length === 7) {
              color += "FF";
          }
          var finalColor = prefix + color.substr(7, 2) + color.substr(1, 6);
          that.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::selectionChanged(Ljava/lang/String;)(finalColor);
          pickr.destroyAndRemove();
        });

        pickr.show();
  }-*/;

    @SuppressWarnings("unused")
    private void selectionChanged(String finalColor) {
        property.setValue(finalColor);
        addColor(finalColor);
    }

    public void addColor(String color) {
        // check if the color belongs to defaultColors
        final String hexa = formatColor(color);
        if (defaultColorsList.contains(hexa))
            return;

        YaProjectEditor projectEditor = (YaProjectEditor) Ode.getCurrentProjectEditor();
        if (projectEditor != null) {
            projectEditor.addColor(hexa.substring(1));
        }
    }

    public static String formatColor(String color) {
        return "#" + color.substring(4) + color.substring(2, 4);
    }

}
