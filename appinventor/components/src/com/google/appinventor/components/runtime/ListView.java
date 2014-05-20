// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;

import android.view.View;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import android.text.TextWatcher;
import android.text.Editable;


/**
 * ListView Component. Non-Visible component to create a ListView in the Screen from a series of
 * elements added from a comma separated set of text elements. It is similar to the ListPicker
 * component but this one is placed on screen instead of opening a new Activity.
 */
@DesignerComponent(version = YaVersion.LISTVIEW_COMPONENT_VERSION,
    description = "<p>This is a visible component that allows to place a list of text elements in" +
        " your Screen to display. <br> The list can be set using the ElementsFromString property" +
        " or using the Elements block in the blocks editor. <br> Warning: This component will" +
        " not work correctly on Screens that are scrollable.</p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/listView.png")
@SimpleObject
public final class ListView extends AndroidViewComponent implements AdapterView.OnItemClickListener {
  private final android.widget.ListView view;
  private EditText txtSearchBox;
  protected final ComponentContainer container;
  private final LinearLayout listViewLayout;
  private ArrayAdapter<String> adapter;
  private YailList items;
  private int selectionIndex;
  private String selection;
  private boolean showFilter = false;
  private static final boolean DEFAULT_ENABLED = false;

  /**
   * Creates a new ListView component.
   * @param container  container that the component will be placed in
   */
  public ListView(ComponentContainer container) {
    super(container);
    this.container = container;
    view = new android.widget.ListView(container.$context());
    view.setOnItemClickListener(this);
    listViewLayout = new LinearLayout(container.$context());
    listViewLayout.setOrientation(LinearLayout.VERTICAL);

    ElementsFromString("");
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_list_item_1,
        items.toStringArray());
    view.setAdapter(adapter);
    view.setBackgroundColor(COLOR_BLACK);

    txtSearchBox = new EditText(container.$context());
    txtSearchBox.setSingleLine(true);
    txtSearchBox.setWidth(Component.LENGTH_FILL_PARENT);
    txtSearchBox.setPadding(10, 10, 10, 10);
    txtSearchBox.setHint("Search list...");

    //set up the listener
    txtSearchBox.addTextChangedListener(new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
          // When user changed the Text
          adapter.getFilter().filter(cs);
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          // no-op. Required method
        }

        @Override
        public void afterTextChanged(Editable arg0) {
          // no-op. Required method
        }
      });

    if (showFilter) {
      txtSearchBox.setVisibility(View.VISIBLE);
    } else {
      txtSearchBox.setVisibility(View.GONE);
    }

    listViewLayout.setBackgroundColor(COLOR_BLACK);
    listViewLayout.addView(txtSearchBox);
    listViewLayout.addView(view);
    listViewLayout.requestLayout();
    container.$add(this);
    Width(Component.LENGTH_FILL_PARENT);
  }

  @Override
  public View getView() {
    return listViewLayout;
  }

  /**
  * Sets the height of the listView on the screen
  * @param height for height length
  */
  @Override
  @SimpleProperty(description = "Determines the height of the list on the view.",
      category =PropertyCategory.APPEARANCE)
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }

  /**
  * Sets the width of the listView on the screen
  * @param width for width length
  */
  @Override
  @SimpleProperty(description = "Determines the width of the list on the view.",
      category = PropertyCategory.APPEARANCE)
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }

  /**
   * Sets true or false to determine whether the search filter box is displayed in the listview
   * or not
   * @param showFilter set the visibility according to this input
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = DEFAULT_ENABLED ? "True" : "False")
  @SimpleProperty(description = "Sets visibility of ShowFilterBar. True will show the bar, " +
      "False will hide it.")
  public void ShowFilterBar(boolean showFilter) {
    this.showFilter = showFilter;
    if (showFilter) {
      txtSearchBox.setVisibility(View.VISIBLE);
    }
    else {
      txtSearchBox.setVisibility(View.GONE);
    }
  }

  /**
   * Returns true or false depending on the visibility of the Filter bar element
   * @return true or false (visibility)
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns current state of ShowFilterBar for visibility.")
  public boolean ShowFilterBar() {
    return showFilter;
  }

  /**
   * Set a list of text elements to build a ListView
   * @param itemsList a YailList containing the strings to be added to the ListView
   */
  @SimpleProperty(description="Set a list of text elements to build your list.",
      category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList itemsList) {
    items = ElementsUtil.elements(itemsList, "Listview");
    setAdapterData();
  }

  /**
   * Specifies the text elements you want to add to the ListView.
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description="Build a list with a series of text elements separated by commas " +
      "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the " +
      "list.",  category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring) {
    items = ElementsUtil.elementsFromString(itemstring);
    setAdapterData();
  }

  /**
   * This function sets the list of the ListView through an adapter
   */
  public void setAdapterData(){
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_list_item_1,
        items.toStringArray());
    view.setAdapter(adapter);
  }

  /**
   * Selection index property getter method.
   */
  @SimpleProperty(
      description = "The index of the currently selected item, starting at " +
          "1.  If no item is selected, the value will be 0.  If an attempt is " +
          "made to set this to a number less than 1 or greater than the number " +
          "of items in the ListView, SelectionIndex will be set to 0, and " +
          "Selection will be set to the empty text.",
      category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex() {
    return selectionIndex;
  }

  /**
   * Sets the index to the passed argument for selection
   * @param index the index to be selected
   */
  @SimpleProperty(description="Choose a position to be your index. This could be used to retrieve" +
      "the text at the position chosen in the list. If an attempt is made to set this to a " +
      "number less than 1 or greater than the number of items in the ListView, SelectionIndex " +
      "will be set to 0, and Selection will be set to the empty text."
      ,
      category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int index){
    selectionIndex = ElementsUtil.selectionIndex(index, items);
    // Now, we need to change Selection to correspond to SelectionIndex.
    selection = ElementsUtil.setSelectionFromIndex(index, items);
  }

  /**
  * Returns the text in the ListView at the position set by SelectionIndex
  */
  @SimpleProperty(description="Returns the text last selected in the ListView.",
      category = PropertyCategory
      .BEHAVIOR)
  public String Selection(){
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
    selectionIndex = ElementsUtil.setSelectedIndexFromValue(value, items);
  }

  /**
   * Simple event to raise when the component is clicked. Implementation of
   * AdapterView.OnItemClickListener
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    SelectionIndex(position + 1); // AI lists are 1-based
    AfterPicking();
  }

  /**
   * Simple event to be raised after the an element has been chosen in the list.
   * The selected element is available in the Selection property.
   */
  @SimpleEvent(description = "Simple event to be raised after the an element has been chosen in the" +
      " list. The selected element is available in the Selection property.")
  public void AfterPicking() {
    EventDispatcher.dispatchEvent(this, "AfterPicking");
  }

}