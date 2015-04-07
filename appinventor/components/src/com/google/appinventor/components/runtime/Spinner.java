// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
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
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.YailList;

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
public final class Spinner extends AndroidViewComponent implements OnItemSelectedListener {

  private final android.widget.Spinner view;
  private ArrayAdapter<String> adapter;
  private YailList items = new YailList();
  private String selection = "";
  private int selectionIndex = 0;
  private boolean isInitialized=false;

  private boolean bold = false;
  private int fontTypeface = Component.TYPEFACE_DEFAULT;
  private boolean italic = false;
  private int backgroundColor = Component.COLOR_DEFAULT;
  private int textColor = Component.COLOR_BLACK;
  private int textAlignment = Component.ALIGNMENT_CENTER;
  private float fontSize = Component.FONT_DEFAULT_SIZE;


  public Spinner(ComponentContainer container) {
    super(container);
    view = new android.widget.Spinner(container.$context());

    // set regular and dropdown layouts
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    view.setAdapter(adapter);
    view.setOnItemSelectedListener(this);
    //Aayush
    Enabled(true);
      FontBold(bold);
      FontTypeface(fontTypeface);
      FontItalic(italic);
      FontSize(fontSize);
      BackgroundColor(backgroundColor);
      TextColor(textColor);
      TextAlignment(textAlignment);

    container.$add(this);
  }

  /**
   * Returns the Spinner's background color as an alpha-red-green-blue
   * integer.
   *
   * @return  background RGB color with alpha
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          description = "Returns the button's background color")
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the Spinner's background color as an alpha-red-green-blue
   * integer.  If the parameter is {@link Component#COLOR_DEFAULT}, the
   * original beveling is restored.  If an Image has been set, the color
   * change will not be visible until the Image is removed.
   *
   * @param argb background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty(description = "Specifies the button's background color. " +
          "The background color will not be visible if an Image is being displayed.")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
      //if you want to change only background of tesxtbox color
    if(selectionIndex==0) return;
    TextView selectedTextView = (TextView) view.getSelectedView();
    TextViewUtil.setBackgroundColor(selectedTextView, argb);
  }

  @Override
  public View getView(){
    return view;
  }

  /**
   * Returns true if the Spinner is active and clickable.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(
          category = PropertyCategory.BEHAVIOR,
          description = "If set, user can tap check box to cause action.")
  public boolean Enabled() {
    return view.isEnabled();
  }

  /**
   * Specifies whether the Spinner should be active and clickable.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    view.setEnabled(enabled);
  }

  /**
   * Returns true if the Spinners's selected text should be bold.
   * If bold has been requested, this property will return true, even if the
   * font does not support bold.
   *
   * @return  {@code true} indicates bold, {@code false} normal
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          description = "If set, button text is displayed in bold.")
  public boolean FontBold() {
    return bold;
  }

  /**
   * Specifies whether the button's text should be bold.
   * Some fonts do not support bold.
   *
   * @param bold  {@code true} indicates bold, {@code false} normal
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE)
  public void FontBold(boolean bold) {
    this.bold = bold;
    if(selectionIndex==0) return;
    TextView selectedTextView = (TextView) view.getSelectedView();
    TextViewUtil.setFontTypeface(selectedTextView, fontTypeface, bold, italic);
  }

  /**
   * Returns true if the Spinner's text should be italic.
   * If italic has been requested, this property will return true, even if the
   * font does not support italic.
   *
   * @return  {@code true} indicates italic, {@code false} normal
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          description = "If set, button text is displayed in italics.")
  public boolean FontItalic() {
    return italic;
  }

  /**
   * Specifies whether the Spinner's text should be italic.
   * Some fonts do not support italic.
   *
   * @param italic  {@code true} indicates italic, {@code false} normal
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE)
  public void FontItalic(boolean italic) {
    this.italic = italic;
      if(selectionIndex==0) return;
    TextView selectedTextView = (TextView) view.getSelectedView();
    TextViewUtil.setFontTypeface(selectedTextView, fontTypeface, bold, italic);
  }

    /**
     * Returns the Spinner's text's font size, measured in pixels.
     *
     * @return  font size in pixel
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Point size for button text.")
    public float FontSize() {
        return fontSize;
    }

    /**
     * Specifies the Spinner's text's font size, measured in pixels.
     *
     * @param size  font size in pixel
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public void FontSize(float size) {
        fontSize = size;
        if(selectionIndex==0) return;
        TextView selectedTextView = (TextView) view.getSelectedView();
        TextViewUtil.setFontSize(selectedTextView, size);
    }

  /**
   * Returns the Spinner's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @return  one of {@link Component#TYPEFACE_DEFAULT},
   *          {@link Component#TYPEFACE_SERIF},
   *          {@link Component#TYPEFACE_SANSSERIF} or
   *          {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          description = "Font family for button text.",
          userVisible = false)
  public int FontTypeface() {
    return fontTypeface;
  }

  /**
   * Specifies the Spinner's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @param typeface  one of {@link Component#TYPEFACE_DEFAULT},
   *                  {@link Component#TYPEFACE_SERIF},
   *                  {@link Component#TYPEFACE_SANSSERIF} or
   *                  {@link Component#TYPEFACE_MONOSPACE}
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE,
          defaultValue = Component.TYPEFACE_DEFAULT + "")
  @SimpleProperty(
          userVisible = false)
  public void FontTypeface(int typeface) {
    fontTypeface = typeface;
      if(selectionIndex==0) return;
    TextView selectedTextView = (TextView) view.getSelectedView();
    TextViewUtil.setFontTypeface(selectedTextView, typeface, bold, italic);
  }

    /**
     * Returns the alignment of the Spinner's text: center, normal
     * (e.g., left-justified if text is written left to right), or
     * opposite (e.g., right-justified if text is written left to right).
     *
     * @return  one of {@link Component#ALIGNMENT_NORMAL},
     *          {@link Component#ALIGNMENT_CENTER} or
     *          {@link Component#ALIGNMENT_OPPOSITE}
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Left, center, or right.",
            userVisible = false)
    public int TextAlignment() {
        return textAlignment;
    }

    /**
     * Specifies the alignment of the Spinner's text: center, normal
     * (e.g., left-justified if text is written left to right), or
     * opposite (e.g., right-justified if text is written left to right).
     *
     * @param alignment  one of {@link Component#ALIGNMENT_NORMAL},
     *                   {@link Component#ALIGNMENT_CENTER} or
     *                   {@link Component#ALIGNMENT_OPPOSITE}
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT,
            defaultValue = Component.ALIGNMENT_CENTER + "")
    @SimpleProperty(userVisible = false)
    public void TextAlignment(int alignment) {
        this.textAlignment = alignment;
        if (selectionIndex == 0) return;
        TextView selectedTextView = (TextView) view.getSelectedView();
        TextViewUtil.setAlignment(selectedTextView, alignment, true);
    }

    /**
     * Returns the Spinner's text color as an alpha-red-green-blue
     * integer.
     *
     * @return  text RGB color with alpha
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Color for button text.")
    public int TextColor() {
        return textColor;
    }

    /**
     * Specifies the Spinner's text color as an alpha-red-green-blue
     * integer.
     *
     * @param argb  text RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
    @SimpleProperty
    public void TextColor(int argb) {
        textColor = argb;
        if(selectionIndex==0) return;
        TextView selectedTextView = (TextView) view.getSelectedView();
        TextViewUtil.setTextColor(selectedTextView, argb);
    }

  /**
   * Selection property getter method.
   */
  @SimpleProperty(description = "Returns the current selected item in the spinner ",
      category = PropertyCategory.BEHAVIOR)
  public String Selection(){
    return selection;
  }

  /**
   * Selection property setter method.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "Set the selected item in the spinner",
      category = PropertyCategory.BEHAVIOR)
  public void Selection(String value){
    selection = value;
    view.setSelection(adapter.getPosition(value));
    // Now, we need to change SelectionIndex to correspond to Selection.
    selectionIndex = ElementsUtil.setSelectedIndexFromValue(value, items);
  }

  /**
   * Selection index property getter method.
   */
  @SimpleProperty(description = "The index of the currently selected item, starting at 1. If no " +
      "item is selected, the value will be 0.", category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex(){
    return selectionIndex;
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
    selectionIndex = ElementsUtil.selectionIndex(index, items);
    view.setSelection(selectionIndex - 1); // AI lists are 1-based
    // Now, we need to change Selection to correspond to SelectionIndex.
    selection = ElementsUtil.setSelectionFromIndex(index, items);
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
    items = ElementsUtil.elements(itemList, "Spinner");

    //avoid firing off the OnItemSelect when component is initialized or data is changed
    isInitialized = false;

    setAdapterData(itemList.toStringArray());
  }

  /**
   * ElementsFromString property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "sets the Spinner list to the elements passed in the " +
      "comma-separated string", category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring){
    items = ElementsUtil.elementsFromString(itemstring);
    setAdapterData(itemstring.split(" *, *"));
    SelectionIndex(1);
  }

  private void setAdapterData(String[] theItems) {
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
      BeforePicking();
      view.performClick();
  }

    /**
     * Event to raise when the button of the component is clicked or the list is shown
     * using the Open block.  This event occurs before the list of items is displayed, and
     * can be used to prepare the list before it is shown.
     */
    @SimpleEvent
    public void BeforePicking() {
        EventDispatcher.dispatchEvent(this, "BeforePicking");
    }

  /**
   * Indicates a user has selected an item
   */
  @SimpleEvent(description = "Event called after the user selects an item from the dropdown list.")
  public void AfterSelecting(String selection){
    EventDispatcher.dispatchEvent(this, "AfterSelecting", selection);
  }

  public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
    //prevent AfterPicking triggering when component has just been instantiated.
    if (!isInitialized) {
        isInitialized = true;
        return;
    }

    SelectionIndex(position + 1); // AI lists are 1-based
    AfterSelecting(selection);
      FontBold(bold);
      FontSize(fontSize);
      FontTypeface(fontTypeface);
      FontItalic(italic);
      BackgroundColor(backgroundColor);
      TextColor(textColor);
      TextAlignment(textAlignment);
  }

  public void onNothingSelected(AdapterView<?> parent){
    view.setSelection(0);
  }


}
