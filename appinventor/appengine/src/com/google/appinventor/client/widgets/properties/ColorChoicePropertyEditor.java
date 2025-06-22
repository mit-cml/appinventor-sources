// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.shared.settings.SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_PROJECT_COLORS;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.widgets.TextButton;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


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
                    "width:15px; height:15px; border-radius:200px; vertical-align: middle\"; border: 1px solid var(--border-color);>&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;&nbsp;" + name;
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

    private Element container;

    /**
     * Creates a new instance of the property editor.
     *
     * @param hexPrefix    colors to be shown in property editor - must not be
     * {@code null} or empty
     * @param defaultValue the color of the default value, for display in the editor only
     */

    private final String defaultColorsString;

    private final boolean showCustomColors; // for handling LEGO colors
    private final List<String> defaultColorList;
    private final List<String> projectColors;
    private final Color[] colors;

    public ColorChoicePropertyEditor(Color[] colors, final String hexPrefix, final String defaultValue, boolean showCustomColors) {
        this.colors = colors;
        this.showCustomColors = showCustomColors;
        ProjectEditor projectEditor = Ode.getCurrentProjectEditor();

        defaultColorList = new ArrayList<>();
        for (Color color : colors) {
            defaultColorList.add("#" + color.rgbString);
        }

        defaultColorsString = String.join(",", defaultColorList);

        String projectColorsFromProperty;
        if (projectEditor != null) {
            projectColorsFromProperty = projectEditor.getProjectSettingsProperty(PROJECT_YOUNG_ANDROID_SETTINGS, YOUNG_ANDROID_SETTINGS_PROJECT_COLORS);
        } else {
            projectColorsFromProperty = "";
        }
        this.projectColors = new ArrayList<>();
        if (!projectColorsFromProperty.isEmpty()) {
            String[] colorArray = projectColorsFromProperty.split(",");
            for (String color : colorArray) {
                if (!color.startsWith("&H"))
                    continue; // reject if does not starts with &H, might be a corrupt value

                // storing 1, can we do something to make the color all over persistent?
                // storing the frequency in project properties might make them persistent
                colorFrequency.put(color, 1);
                this.projectColors.add(color);
            }
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

    public void setContainer(Element container) {
        this.container = container;
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
            String displayValue = hex.endsWith("FF") ? hex.substring(0, hex.length() - 2) : hex;
            selectColor.setHTML(Color.getHtmlDescription(defaultValue, displayValue));
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
        String displayValue = hex.endsWith("FF") ? hex.substring(0, hex.length() - 2) : hex;
        return "<span style=\"background:rgba(" + r + "," + g + "," + b + "," + a + "); display: inline-block; " +
                "width:15px; height:15px; border-radius:200px; border: 1px solid var(--border-color);\">&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;&nbsp;" + displayValue;
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
        if (container != null) {
            showPicker(getElement(), container, pickerColor, getProjectColorsHexString(), defaultColorsString);
        } else {
            showPicker(getElement(), getProjectColorsHexString());
        }
    }

    private String getProjectColorsHexString() {
        List<String> projectColorsCopy = new ArrayList<>();
        for (String color : projectColors) {
            projectColorsCopy.add(color.startsWith("#") ? color : getAlphaHexString(color));
        }
        return String.join(",", projectColorsCopy);
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

    // JSNI Methods
    private native void showPicker(Element element, Element containerElement, String defaultColor, String projectColors, String defaultColors)/*-{
    var prefix = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::hexPrefix;
    var that = this;
    defaultColors = defaultColor + "," + defaultColors;
    var pickr = $wnd.Pickr.create({
            el: element,
            useAsButton: true,
            theme: 'nano',
            container: containerElement,
            showAlways: false,
            'default': defaultColor,
            sliders: "h",
            position: 'bottom-middle',
            swatches: defaultColors.split(","),
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
                    clear: false,
                    cancel: true,
                    save: true
                }
            }
          });

        pickr.on('hide', function(instance){
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

    private native void showPicker(Element element, String projectColorsString)/*-{
    var prefix = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::hexPrefix;
    var defaultColorListString = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::defaultColorsString;
    var defaultColor = this.@com.google.appinventor.client.widgets.properties.ColorChoicePropertyEditor::pickerColor;
    var that = this;
    defaultColors = defaultColor + "," + defaultColorListString;
    var pickr = $wnd.Pickr.create({
            el: element,
            useAsButton: true,
            theme: 'nano',
            showAlways: false,
            'default': defaultColor,
            sliders: "h",
            position: 'bottom-middle',
            swatches: defaultColorListString.split(","),
            swatches2: projectColorsString.split(","),
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
                    clear: false,
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
        if (defaultColorList.contains(formatColor(color)))
            return;

        colorFrequency.put(color, colorFrequency.getOrDefault(color, 0) + 1);
        sortColors();

        ProjectEditor projectEditor = Ode.getCurrentProjectEditor();
        if (projectEditor != null) {
            projectEditor.changeProjectSettingsProperty(PROJECT_YOUNG_ANDROID_SETTINGS,
                    YOUNG_ANDROID_SETTINGS_PROJECT_COLORS, String.join(",", projectColors));
        }
    }

    private void sortColors() {
        List<Map.Entry<String, Integer>> sortedColors = new ArrayList<>(this.colorFrequency.entrySet());

        sortedColors.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        this.projectColors.clear();

        int n = 14;
        if (n > sortedColors.size())
            n = sortedColors.size();
        for (int i = 0; i < n; i++)
            this.projectColors.add(sortedColors.get(i).getKey());

    }

    private final HashMap<String, Integer> colorFrequency = new HashMap<>();

    public static String formatColor(String color) { // color : &HFF303F9F, output : #303F9F (to check wether it is a color from primary colors list)
        return "#" + color.substring(4);
    }

    public static String getAlphaHexString(String color) {
        if (color.isEmpty()) return "";
        color = color.startsWith("&H") ? color.substring(2) : Long.toHexString(Long.parseLong(color));
        int len = color.length();
        if (len < 8) {
            do {
                color = 'F' + color;
            } while (++len < 8);
        }
        return '#' + color.substring(2) + color.substring(0, 2);
    }
}
