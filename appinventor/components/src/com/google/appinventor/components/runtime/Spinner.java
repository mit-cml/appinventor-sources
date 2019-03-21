// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import android.widget.TextView;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.*;

@DesignerComponent(version = YaVersion.SPINNER_COMPONENT_VERSION,
    description = "<p>A spinner component that displays a pop-up with a list of elements." +
        " These elements can be set in the Designer or Blocks Editor by setting the" +
        "<code>ElementsFromString</code> property to a string-separated concatenation" +
        " (for example, <em>choice 1, choice 2, choice 3</em>) or by setting the " +
        "<code>Elements</code> property to a List in the Blocks editor. " +
        "Spinners are created with the first item already selected. So selecting " +
        " it does not generate an After Picking event. Consequently it's useful to make the " +
        " first Spinner item be a non-choice like \"Select from below...\". </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/spinner.png")
@SimpleObject
public final class Spinner extends TouchComponent<android.widget.Spinner> implements OnItemSelectedListener {
  
  private SpinnerArrayAdapter adapter;
  private YailList items = new YailList();
  private int oldAdapterCount;
  private int oldSelectionIndex;

  public Spinner(ComponentContainer container) {
    super(container);
    // Themes made the Spinner look and feel change significantly. This allows us to be backward
    // compatible with established expectations of behavior. However, on Honeycomb and higher there
    // is a different constructor to get the old behavior. To ensure we work on older devices, that
    // instantiation is moved to HoneycombUtil
    if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
      view = new android.widget.Spinner(container.$context());
    } else {
      view = HoneycombUtil.makeSpinner(container.$context());
    }

    // set regular and dropdown layouts
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
    view.setAdapter(adapter);
    view.setOnItemSelectedListener(this);

    initToggle();

    Prompt("");
    oldSelectionIndex = SelectionIndex();
  }

  /**
   * Selection property getter method.
   */
  @SimpleProperty(description = "Returns the current selected item in the spinner ",
      category = PropertyCategory.BEHAVIOR)
  public String Selection(){
    return SelectionIndex() == 0 ? "" : (String) view.getItemAtPosition(SelectionIndex() - 1);
  }

  /**
   * Selection property setter method.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "Set the selected item in the spinner",
      category = PropertyCategory.BEHAVIOR)
  public void Selection(String value){
    SelectionIndex(ElementsUtil.setSelectedIndexFromValue(value, items));
  }

  /**
   * Selection index property getter method.
   */
  @SimpleProperty(description = "The index of the currently selected item, starting at 1. If no " +
      "item is selected, the value will be 0.", category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex(){
    return ElementsUtil.selectionIndex(view.getSelectedItemPosition() + 1, items);
  }

  /**
   * Selection index property setter method, not a designer property to prevent
   * inconsistencies if selection is invalid
   */
  @SimpleProperty(description = "Set the spinner selection to the element at the given index." +
      "If an attempt is made to set this to a number less than 1 or greater than the number of " +
      "items in the Spinner, SelectionIndex will be set to 0, and Selection will be set to empty.",
      category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int index){
    oldSelectionIndex = SelectionIndex();
    view.setSelection(ElementsUtil.selectionIndex(index, items) - 1); // AI lists are 1-based
  }

  /**
   * Elements property getter method
   */
  @SimpleProperty(description = "returns a list of text elements to be picked from.",
      category = PropertyCategory.BEHAVIOR)
  public YailList Elements(){
    return items;
  }

  /**
   * Elements property setter method
   */
  @SimpleProperty(description = "adds the passed text element to the Spinner list",
      category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList itemList){
    // The following conditional handles special cases for the fact that
    // spinners automatically select an item when non-empty data is fed
    if (itemList.size() == 0) {
      SelectionIndex(0);
    } else if (itemList.size() < items.size() && SelectionIndex() == items.size()) {
      SelectionIndex(itemList.size());
    }
    items = ElementsUtil.elements(itemList, "Spinner");
    setAdapterData(itemList.toStringArray());
  }

  /**
   * ElementsFromString property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "sets the Spinner list to the elements passed in the " +
      "comma-separated string", category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring){
    Elements(ElementsUtil.elementsFromString(itemstring));
  }

  private void setAdapterData(String[] theItems) {
    oldAdapterCount = adapter.getCount();
    adapter.clear();
    for (int i = 0; i < theItems.length; i++){
      adapter.add(theItems[i]);
    }
  }

  /**
   * Prompt property getter method
   */
  @SimpleProperty(description = "Text with the current title for the Spinner window",
      category = PropertyCategory.APPEARANCE)
  public String Prompt(){
    return view.getPrompt().toString();
  }

  /**
   * Prompt property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "sets the Spinner window prompt to the given tittle",
      category = PropertyCategory.APPEARANCE)
  public void Prompt(String str){
    view.setPrompt(str);
  }

  /**
   * To display the dropdown list without the user having to click it
   */
  @SimpleFunction(description = "displays the dropdown list for selection, " +
      "same action as when the user clicks on the spinner.")
  public void DisplayDropdown(){
    view.performClick();
  }

  /**
   * Indicates a user has selected an item
   */
  @SimpleEvent(description = "Event called after the user selects an item from the dropdown list.")
  public void AfterSelecting(String selection){
    EventDispatcher.dispatchEvent(this, "AfterSelecting", selection);
  }

    /**
     * Indicates a user has opened the Spinner selection menu
     */
    @SimpleEvent(description = "Event called after the user opens the dropdown list.")
    public void BeforeSelecting(){
        EventDispatcher.dispatchEvent(this, "BeforeSelecting");
    }

  public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
    // special case 1:
    // onItemSelected is fired when the adapter goes from empty to non-empty AND
    // SelectionIndex was not set, i.e. oldSelectionIndex == 0
    // special case 2:
    // onItemSelected is fired when the adapter goes from larger size to smaller size AND
    // the old selection position (one-based) is larger than the size of the new adapter
    if (oldAdapterCount == 0 && adapter.getCount() > 0 && oldSelectionIndex == 0 ||
        oldAdapterCount > adapter.getCount() && oldSelectionIndex > adapter.getCount()) {
      SelectionIndex(position + 1);  // AI lists are 1-based
      oldAdapterCount = adapter.getCount();
    } else {
      SelectionIndex(position + 1); // AI lists are 1-based
      AfterSelecting(Selection());
    }
  }

  public void onNothingSelected(AdapterView<?> parent){
    view.setSelection(0);
  }

    /**
     * Returns the alignment of the button's text: center, normal
     * (e.g., left-justified if text is written left to right), or
     * opposite (e.g., right-justified if text is written left to right).
     *
     * @return one of {@link Component#ALIGNMENT_NORMAL},
     * {@link Component#ALIGNMENT_CENTER} or
     * {@link Component#ALIGNMENT_OPPOSITE}
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Left, center, or right.",
            userVisible = false)
    public int TextAlignment() {
        return adapter.getTextAlignment();
    }

    /**
     * Specifies the alignment of the button's text: center, normal
     * (e.g., left-justified if text is written left to right), or
     * opposite (e.g., right-justified if text is written left to right).
     *
     * @param alignment one of {@link Component#ALIGNMENT_NORMAL},
     *                  {@link Component#ALIGNMENT_CENTER} or
     *                  {@link Component#ALIGNMENT_OPPOSITE}
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT,
            defaultValue = Component.ALIGNMENT_CENTER + "")
    @SimpleProperty(userVisible = false)
    public void TextAlignment(int alignment) {
        adapter.setTextAlignment(alignment);
    }

    /**
     * Returns true if the button's text should be bold.
     * If bold has been requested, this property will return true, even if the
     * font does not support bold.
     *
     * @return {@code true} indicates bold, {@code false} normal
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "If set, button text is displayed in bold.")
    public boolean FontBold() {
        return adapter.isBold();
    }

    /**
     * Specifies whether the button's text should be bold.
     * Some fonts do not support bold.
     *
     * @param bold {@code true} indicates bold, {@code false} normal
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public void FontBold(boolean bold) {
        adapter.setBold(bold);
    }

    /**
     * Returns true if the button's text should be italic.
     * If italic has been requested, this property will return true, even if the
     * font does not support italic.
     *
     * @return {@code true} indicates italic, {@code false} normal
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "If set, button text is displayed in italics.")
    public boolean FontItalic() {
        return adapter.isItalic();
    }

    /**
     * Specifies whether the button's text should be italic.
     * Some fonts do not support italic.
     *
     * @param italic {@code true} indicates italic, {@code false} normal
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public void FontItalic(boolean italic) {
        adapter.setItalic(italic);
    }

    /**
     * Returns the button's text's font size, measured in sp(scale-independent pixels).
     *
     * @return font size in sp(scale-independent pixels).
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Point size for button text.")
    public float FontSize() {
        return adapter.getFontSize();
    }

    /**
     * Specifies the button's text's font size, measured in sp(scale-independent pixels).
     *
     * @param size font size in sp(scale-independent pixels)
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public void FontSize(float size) {
        adapter.setFontSize(size);
    }

    /**
     * Returns the button's text's font face as default, serif, sans
     * serif, or monospace.
     *
     * @return one of {@link Component#TYPEFACE_DEFAULT},
     * {@link Component#TYPEFACE_SERIF},
     * {@link Component#TYPEFACE_SANSSERIF} or
     * {@link Component#TYPEFACE_MONOSPACE}
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Font family for button text.",
            userVisible = false)
    public int FontTypeface() {
        return adapter.getFontTypeface();
    }

    /**
     * Specifies the button's text's font face as default, serif, sans
     * serif, or monospace.
     *
     * @param typeface one of {@link Component#TYPEFACE_DEFAULT},
     *                 {@link Component#TYPEFACE_SERIF},
     *                 {@link Component#TYPEFACE_SANSSERIF} or
     *                 {@link Component#TYPEFACE_MONOSPACE}
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE,
            defaultValue = Component.TYPEFACE_DEFAULT + "")
    @SimpleProperty(
            userVisible = false)
    public void FontTypeface(int typeface) {
        adapter.setFontTypeface(typeface);
    }

    /**
     * Returns the button's text color as an alpha-red-green-blue
     * integer.
     *
     * @return text RGB color with alpha
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Color for button text.")
    public int TextColor() {
        return adapter.getTextColor();
    }

    /**
     * Specifies the button's text color as an alpha-red-green-blue
     * integer.
     *
     * @param argb text RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
    @SimpleProperty
    public void TextColor(int argb) {
        adapter.setTextColor(argb);
    }

    private static class SpinnerArrayAdapter extends ArrayAdapter<String> {
        // Backing for text alignment
        private int textAlignment;

        // Backing for font typeface
        private int fontTypeface;

        // Backing for font bold
        private boolean bold;

        // Backing for font italic
        private boolean italic;

        // Backing for text color
        private int textColor;

        // Backing for font size
        private float fontSize;

        public SpinnerArrayAdapter(final Context context, final int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            return view;
        }

        public int getTextAlignment() {
            return textAlignment;
        }

        public void setTextAlignment(int textAlignment) {
            this.textAlignment = textAlignment;
        }

        public int getFontTypeface() {
            return fontTypeface;
        }

        public void setFontTypeface(int fontTypeface) {
            this.fontTypeface = fontTypeface;
        }

        public boolean isBold() {
            return bold;
        }

        public void setBold(boolean bold) {
            this.bold = bold;
        }

        public boolean isItalic() {
            return italic;
        }

        public void setItalic(boolean italic) {
            this.italic = italic;
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public float getFontSize() {
            return fontSize;
        }

        public void setFontSize(float fontSize) {
            this.fontSize = fontSize;
        }
    }
}
