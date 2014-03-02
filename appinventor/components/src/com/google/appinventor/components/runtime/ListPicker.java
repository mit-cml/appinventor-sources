// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.YailList;

import android.app.Activity;
import android.content.Intent;

/**
 * A button allowing a user to select one among a list of text strings.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author M. Hossein Amerkashi (kkashi01@gmail.com)
 */
@DesignerComponent(version = YaVersion.LISTPICKER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "<p>A button that, when clicked on, displays a list of " +
    "texts for the user to choose among. The texts can be specified through " +
    "the Designer or Blocks Editor by setting the " +
    "<code>ElementsFromString</code> property to their string-separated " +
    "concatenation (for example, <em>choice 1, choice 2, choice 3</em>) or " +
    "by setting the <code>Elements</code> property to a List in the Blocks " +
    "editor.</p>" +
    "<p>Setting property ShowFilterBar to true, will make the list searchable.  " +
    "Other properties affect the appearance of the button " +
    "(<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and " +
    "whether it can be clicked on (<code>Enabled</code>).</p>")
@SimpleObject
public class ListPicker extends Picker implements ActivityResultListener, Deleteable {

  private static final String LIST_ACTIVITY_CLASS = ListPickerActivity.class.getName();
  static final String LIST_ACTIVITY_ARG_NAME = LIST_ACTIVITY_CLASS + ".list";
  static final String LIST_ACTIVITY_RESULT_NAME = LIST_ACTIVITY_CLASS + ".selection";
  static final String LIST_ACTIVITY_RESULT_INDEX = LIST_ACTIVITY_CLASS + ".index";
  static final String LIST_ACTIVITY_ANIM_TYPE = LIST_ACTIVITY_CLASS + ".anim";
  static final String LIST_ACTIVITY_SHOW_SEARCH_BAR = LIST_ACTIVITY_CLASS + ".search";
  static final String LIST_ACTIVITY_TITLE = LIST_ACTIVITY_CLASS + ".title";

  private YailList items;
  private String selection;
  private int selectionIndex;
  private boolean showFilter =false;
  private static final boolean DEFAULT_ENABLED = false;
  private String title = "";    // The Title to display the List Picker with
                                // if left blank, the App Name is used instead

  /**
   * Create a new ListPicker component.
   *
   * @param container the parent container.
   */
  public ListPicker(ComponentContainer container) {
    super(container);
    items = new YailList();
    selection = "";
    selectionIndex = 0;
  }

  /**
   * Selection property getter method.
   */
  @SimpleProperty(
      description = "The selected item.  When directly changed by the " +
      "programmer, the SelectionIndex property is also changed to the first " +
      "item in the ListPicker with the given value.  If the value does not " +
      "appear, SelectionIndex will be set to 0.",
      category = PropertyCategory.BEHAVIOR)
  public String Selection() {
    return selection;
  }

  /**
   * Selection property setter method.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Selection(String value) {
    selection = value;
    // Now, we need to change SelectionIndex to correspond to Selection.
    // If multiple Selections have the same SelectionIndex, use the first.
    // If none do, arbitrarily set the SelectionIndex to its default value
    // of 0.
    for (int i = 0; i < items.size(); i++) {
      // The comparison is case-sensitive to be consistent with yail-equal?.
      if (items.getString(i).equals(value)) {
        selectionIndex = i + 1;
        return;
      }
    }
    selectionIndex = 0;
  }

  @DesignerProperty(
    editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = DEFAULT_ENABLED ? "True" : "False")  @SimpleProperty
  public void ShowFilterBar(boolean showFilter) {
    this.showFilter = showFilter;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "Returns current state of ShowFilterBar indicating if " +
    "Search Filter Bar will be displayed on ListPicker or not")
  public boolean ShowFilterBar() {
    return showFilter;
  }
  /**
   * Selection index property getter method.
   */
  @SimpleProperty(
      description = "The index of the currently selected item, starting at " +
      "1.  If no item is selected, the value will be 0.  If an attempt is " +
      "made to set this to a number less than 1 or greater than the number " +
      "of items in the ListPicker, SelectionIndex will be set to 0, and " +
      "Selection will be set to the empty text.",
      category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex() {
    return selectionIndex;
  }

  /**
   * Selection index property setter method.
   */
  // Not a designer property, since this could lead to unpredictable
  // results if Selection is set to an incompatible value.
  @SimpleProperty
  public void SelectionIndex(int index) {
    if (index <= 0 || index > items.size()) {
      selectionIndex = 0;
      selection = "";
    } else {
      selectionIndex = index;
      // YailLists are 0-based, but we want to be 1-based.
      selection = items.getString(selectionIndex - 1);
    }
  }

  /**
   * Elements property getter method
   *
   * @return a YailList representing the list of strings to be picked from
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public YailList Elements() {
    return items;
  }

  /**
   * Elements property setter method
   * @param itemList - a YailList containing the strings to be added to the
   *                   ListPicker
   */
  // TODO(user): we need a designer property for lists
  @SimpleProperty
  public void Elements(YailList itemList) {
    Object[] objects = itemList.toStringArray();
    for (int i = 0; i < objects.length; i++) {
      if (!(objects[i] instanceof String)) {
        throw new YailRuntimeError("Items passed to ListPicker must be Strings", "Error");
      }
    }
    items = itemList;
  }

  /**
   * ElementsFromString property setter method
   *
   * @param itemstring - a string containing a comma-separated list of the
   *                     strings to be picked from
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
                    defaultValue = "")
  // TODO(sharon): it might be nice to have a list editorType where the developer
  // could directly enter a list of strings (e.g. one per row) and we could
  // avoid the comma-separated business.
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring) {
    if (itemstring.length() == 0) {
      items = new YailList();
    } else {
      items = YailList.makeList((Object[]) itemstring.split(" *, *"));
    }
  }

  /**
   * Title property getter method.
   *
   * @return  list picker title
   */
    @SimpleProperty(category = PropertyCategory.APPEARANCE,
                    description = "Optional title displayed at the top of the list of choices.")
  public String Title() {
    return title;
  }

  /**
   * Title property setter method: sets a new caption for the list picker in the
   * list picker activity's title bar.
   *
   * @param title  new list picker caption
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Title(String title) {
    this.title = title;
  }

  @Override
  public Intent getIntent() {
    Intent intent = new Intent();
    intent.setClassName(container.$context(), LIST_ACTIVITY_CLASS);
    intent.putExtra(LIST_ACTIVITY_ARG_NAME, items.toStringArray());
    intent.putExtra(LIST_ACTIVITY_SHOW_SEARCH_BAR, String.valueOf(showFilter)); //convert to string
    if (!title.equals("")) {
      intent.putExtra(LIST_ACTIVITY_TITLE, title);
    }
    // Get the current Form's opening transition anim type,
    // and pass it to the list picker activity. For consistency,
    // the closing animation will be the same (but in reverse)
    String openAnim = container.$form().getOpenAnimType();
    intent.putExtra(LIST_ACTIVITY_ANIM_TYPE, openAnim);
    return intent;
  }

  // ActivityResultListener implementation

  /**
   * Callback method to get the result returned by the list picker activity
   *
   * @param requestCode a code identifying the request.
   * @param resultCode a code specifying success or failure of the activity
   * @param data the returned data, in this case an Intent whose data field
   *        contains the selected item.
   */
  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      if (data.hasExtra(LIST_ACTIVITY_RESULT_NAME)) {
        selection = data.getStringExtra(LIST_ACTIVITY_RESULT_NAME);
      } else {
        selection = "";
      }
      selectionIndex = data.getIntExtra(LIST_ACTIVITY_RESULT_INDEX, 0);
      AfterPicking();
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    container.$form().unregisterForActivityResult(this);
  }

}
