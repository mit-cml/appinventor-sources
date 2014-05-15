// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;

import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

/**
 * Text box using auto-completion to pick out a text values defined in elements list..
 *
 * @author manishkk74@gmail.com (Manish Khanchandani)
 */

@DesignerComponent(version = YaVersion.CUSTOMAUTOCOMPLETE_COMPONENT_VERSION,
    description = "An CustomAutocomplete is a kind of text box.  " +
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
public class CustomAutocomplete extends TextBoxBase {

  private ArrayAdapter<String> adapter;
  private YailList items = new YailList();

  /**
   * Create a new CustomAutocomplete component.
   *
   * @param container the parent container.
   */
  public CustomAutocomplete(ComponentContainer container) {
    super(container, new AutoCompleteTextView(container.$context()));
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_dropdown_item_1line);
    ((AutoCompleteTextView) super.view).setAdapter(adapter);
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

//     Note(halabelson):  I am commenting out this test.  Android provider.Constacts was
//     deprecated in Donut, but email picking still seems to work on newer versions of the SDK.
//     If there's a phone where it does not work, we'll get the error at PuntContactSelection
//     Note that there is still a general problem with contact picking on Motoblur.
//
//    if (SdkLevel.getLevel() > SdkLevel.LEVEL_DONUT) {
//      container.$form().dispatchErrorOccurredEvent(this, "EmailAddress",
//          ErrorMessages.ERROR_FUNCTIONALITY_NOT_SUPPORTED_EMAIL_PICKER);
//    }

    EventDispatcher.dispatchEvent(this, "GotFocus");
  }
}