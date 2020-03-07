// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

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
import com.google.appinventor.components.runtime.util.HoneycombUtil;
import com.google.appinventor.components.runtime.util.YailList;

/**
 * A `Spinner` component that displays a dialog with a list of elements. These elements can be set
 * in the Designer or Blocks Editor by setting the {@link #ElementsFromString(String)} property to
 * a comma-separated list of values (for example, `choice 1, choice 2, choice 3`) or by setting the
 * {@link #Elements(YailList)} property to a List in the Blocks editor. Spinners are created with
 * the first item already selected, so selecting it does not generate an
 * {@link #AfterSelecting(String)} event. Consequently it's useful to make the first `Spinner` item
 * be a non-choice like "Select from below...".
 */
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

    container.$add(this);

    Prompt("");
    oldSelectionIndex = SelectionIndex();
  }

  @Override
  public View getView(){
    return view;
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
   * Specifies the current selected item in the `Spinner`.
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
   * Set the `Spinner` selection to the element at the given index.
   * If an attempt is made to set this to a number less than `1` or greater than the number of
   * items in the `Spinner`, `SelectionIndex` will be set to `0`, and {@link #Selection(String)}
   * will be set to the empty text.
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
   * Specifies the list of choices to display.
   */
  @SimpleProperty(description = "Adds the passed text element to the Spinner list",
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
   * Set the list of choices from a string of comma-separated values.
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA, defaultValue = "")
  @SimpleProperty(description = "Sets the Spinner list to the elements passed in the " +
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
   * Specifies the text used for the title of the Spinner window.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "Sets the Spinner window prompt to the given title",
      category = PropertyCategory.APPEARANCE)
  public void Prompt(String str){
    view.setPrompt(str);
  }

  @SimpleFunction(description = "Displays the dropdown list for selection, " +
      "same action as when the user clicks on the spinner.")
  public void DisplayDropdown(){
    view.performClick();
  }

  /**
   * Event called after the user selects an item from the dropdown list.
   */
  @SimpleEvent(description = "Event called after the user selects an item from the dropdown list.")
  public void AfterSelecting(String selection){
    EventDispatcher.dispatchEvent(this, "AfterSelecting", selection);
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

}
