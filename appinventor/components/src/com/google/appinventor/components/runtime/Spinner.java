// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

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
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.YailList;

@DesignerComponent(version = YaVersion.SPINNER_VERSION, description = "<p>A spinner component, that when clicked on, displays a pop-up"
    + " with a list of elements. These elements can be set in"
    + "the Designer or Blocks Editor by setting the "
    + "<code>ElementsFromString</code> property to their string-separated "
    + "concatenation (for example, <em>choice 1, choice 2, choice 3</em>) or "
    + "by setting the <code>Elements</code> property to a List in the Blocks " + "editor.</p>", category = ComponentCategory.USERINTERFACE, nonVisible = false, iconName = "images/spinner.png")
@SimpleObject
public final class Spinner extends AndroidViewComponent implements OnItemSelectedListener {

  private final android.widget.Spinner view;
  private ArrayAdapter<String> adapter;

  public Spinner(ComponentContainer container) {
    super(container);
    view = new android.widget.Spinner(container.$context());

    // Adds the component to its designated container
    container.$add(this);

    // items = new YailList();
    // selection = "";
    // selectionIndex = 0;

    // set regular and dropdown layouts
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    view.setAdapter(adapter);
    view.setOnItemSelectedListener(this);
  }

  @Override
  public View getView(){
    return view;
  }

  /**
   * Selection property getter method.
   */
  @SimpleProperty(description = "Returns the current selected item in the spinner ", category = PropertyCategory.BEHAVIOR)
  public String Selection(){
    return view.getSelectedItem().toString();
  }

  /**
   * Selection property setter method.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "Set the selected in the spinner", category = PropertyCategory.BEHAVIOR)
  public void Selection(String value){
    view.setSelection(adapter.getPosition(value));
  }

  /**
   * Selection index property getter method.
   */
  @SimpleProperty(description = "The index of the currently selected item, starting at "
      + "1.  If no item is selected, the value will be 0", category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex(){
    return view.getSelectedItemPosition();
  }

  /**
   * Selection index property setter method.
   */
  // Not a designer property, since this could lead to unpredictable
  // results if Selection is set to an incompatible value.
  @SimpleProperty(description = "Set the spinner selection to the element at the given index. If an attempt is "
      + "made to set this to a number less than 1 or greater than the number "
      + "of items in the Spinner, SelectionIndex will be set to 0, and "
      + "Selection will be set to the empty text.", category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int index){
    view.setSelection(index);
    if (index <= 0 || index > adapter.getCount()) {
      view.setSelection(0);
    } else {
      view.setSelection(index);
    }
  }

  /**
   * Elements property getter method
   */
  @SimpleProperty(description = "return a YailList representing the list of strings to be picked from", category = PropertyCategory.BEHAVIOR)
  public YailList Elements(){
    YailList items = new YailList();
    for (int i = 0; i < adapter.getCount(); i++) {
      items.add(adapter.getItem(i));
    }
    return items;
  }

  /**
   * Elements property setter method
   */
  @SimpleProperty(description = "adds the passed string to the Spinner list", category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList itemList){
    adapter.clear();
    Object[] objects = itemList.toStringArray();
    for (int i = 0; i < objects.length; i++) {
      if (!(objects[i] instanceof String)) {
        throw new YailRuntimeError("Items passed to Spinner must be Strings", "Error");
      }
    }
    adapter.addAll(itemList.toStringArray());
  }

  /**
   * ElementsFromString property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "sets the Spinner list to the elements passed in the comma-separated string", category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring){
    adapter.clear();
    if (itemstring.length() != 0) {
      adapter.addAll(itemstring.split(" *, *"));
    }
  }

  /**
   * Prompt property getter method
   */
  @SimpleProperty(description = "String with the current prompt for the Spinner", category = PropertyCategory.APPEARANCE)
  public String Prompt(){
    return view.getPrompt().toString();
  }

  /**
   * Prompt property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "sets the Spinner prompt to the give string", category = PropertyCategory.APPEARANCE)
  public void Prompt(String str){
    view.setPrompt(str);
  }

  /**
   * To display the dropdown list without the user having to click it
   */
  @SimpleFunction(description = "displays the dropdown list for selection, same action as when user clicks")
  public void DisplayDropdown(){
    view.performClick();
  }

  /**
   * Indicates a user has selected an item
   */
  @SimpleEvent(description = "Even called when the user selects an item from the dropdown list")
  public void DidSelectItem(String selection){
    EventDispatcher.dispatchEvent(this, "DidSelectItem", selection);
  }

  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
    this.view.setSelection(pos);
    DidSelectItem(this.view.getSelectedItem().toString());

  }

  // not sure when gets called, did not add and event for it
  public void onNothingSelected(AdapterView<?> parent){
    view.setSelection(0);
  }

}
