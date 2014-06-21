// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;

import android.content.Context;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.view.inputmethod.InputMethodManager;

/**
 * Text box using auto-completion to pick out a text values defined in elements list..
 *
 * @author manishkk74@gmail.com (Manish Khanchandani)
 */

@DesignerComponent(version = YaVersion.AUTOCOMPLETE_COMPONENT_VERSION,
    description = "An Autocomplete is a kind of text box.  " +
    "If the user begins entering the text " +
    ", the phone will show a dropdown menu of " +
    "choices that complete the entry.  If there are many elements, the " +
    "dropdown can take several seconds to appear, and can show intermediate results " +
    "while the matches are being computed. <p>The initial contents " +
    "of the text box and the contents< after user entry is in the <code>Text</code> " +
    "property.  If the <code>Text</code> property is initially empty, " +
    "the contents of the <code>Hint</code> property will be faintly shown " +
    "in the text box as a hint to the user.</p>\n " +
    "<p>Other properties affect the appearance of the text box " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be used (<code>Enabled</code>).</p>\n" +
    "<p>Text boxes like this are usually used with <code>Button</code> " +
    "components, with the user clicking on the button when text entry is " +
    "complete.",
    category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class Autocomplete extends TextBoxBase {

  private ArrayAdapter<String> adapter;
  private YailList items = new YailList();

  AutoCompleteTextView actv;

  /**
   * Create a new Autocomplete component.
   *
   * @param container the parent container.
   */
  public Autocomplete(ComponentContainer container) {
    super(container, new AutoCompleteTextView(container.$context()));
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_dropdown_item_1line);
    actv = (AutoCompleteTextView) super.view;
    actv.setAdapter(adapter);
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
  @SimpleProperty(description = "adds the passed text element to the list",
      category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList itemList){
    items = ElementsUtil.elements(itemList, "Autocomplete");
    setAdapterData(itemList.toStringArray());
  }

  /**
   * ElementsFromString property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "sets the Elements list to the elements passed in the " +
      "comma-separated string", category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring){
    items = ElementsUtil.elementsFromString(itemstring);
    setAdapterData(itemstring.split(" *, *"));
  }

  private void setAdapterData(String[] theItems) {
    adapter.clear();
    for (int i = 0; i < theItems.length; i++){
      adapter.add(theItems[i]);
    }
  }

  /**
   * Event raised when this component is selected for input, such as by
   * the user touching it.
   */
  @SimpleEvent
  @Override
  public void GotFocus() {
    EventDispatcher.dispatchEvent(this, "GotFocus");
  }

  /**
   * Hide the soft keyboard
   */
  @SimpleFunction(
      description = "Hide the keyboard.  Only multiline text boxes need this. " +
      "Single line text boxes close the keyboard when the users presses the Done key.")
  public void HideKeyboard() {
    InputMethodManager imm =
      (InputMethodManager) container.$context().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  /**
   * showDropDown: displays the dropdown
   */
  @SimpleFunction(
      description = "showDropDown: displays the dropdown")
  public void showDropDown() {
    actv.showDropDown();
  }
}