// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;

import android.widget.LinearLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.ListViewArrayAdapterImageSingleText;
import com.google.appinventor.components.runtime.util.ListViewArrayAdapterImageTwoText;
import com.google.appinventor.components.runtime.util.ListViewArrayAdapterSingleText;
import com.google.appinventor.components.runtime.util.ListViewArrayAdapterTwoText;
import com.google.appinventor.components.runtime.util.ListViewArrayAdapterTwoTextLinear;
import com.google.appinventor.components.runtime.util.YailList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This is a visible component that allows to place a list of text elements in your {@link Form} to
 * display. The list can be set using the {@link #ElementsFromString(String)} property or using the
 * {@link #Elements(YailList)} block in the blocks editor.
 *
 *   Warning: This component will not work correctly on Screens that are scrollable if its
 * {@link #Height(int)} is set to Fill Parent.
 *
 * @internaldoc
 * TODO(hal): Think about generalizing this to include more than text.
 * @author halabelson@google.com (Hal Abelson)
 * @author osmidy@mit.edu (Olivier Midy)
 */

@DesignerComponent(version = YaVersion.LISTVIEW_COMPONENT_VERSION,
    description = "<p>This is a visible component that displays a list of text elements." +
        " <br> The list can be set using the ElementsFromString property" +
        " or using the Elements block in the blocks editor. </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/listView.png")
@SimpleObject
public final class ListView extends AndroidViewComponent implements AdapterView.OnItemClickListener {

  private static final String LOG_TAG = "ListView";

  private final android.widget.ListView view;
  private EditText txtSearchBox;
  protected final ComponentContainer container;
  private final LinearLayout listViewLayout;

  // The adapter contains spannables rather than strings, since we will be changing the item
  // colors using ForegroundColorSpan
  private ArrayAdapter<Spannable> adapter;
  private ArrayAdapter<Spannable> adapterCopy;
  private YailList items;
  private int selectionIndex;
  private String selection;
  private String selectionDetailText;
  private boolean showFilter = false;
  private static final boolean DEFAULT_ENABLED = false;

  private int backgroundColor;
  private static final int DEFAULT_BACKGROUND_COLOR = Component.COLOR_BLACK;

  // The text color of the ListView's items.  All items have the same text color
  private int textColor;
  // The color of secondary text of ListView's items. All secondary text items have same text color
  private int detailTextColor;
  private static final int DEFAULT_TEXT_COLOR = Component.COLOR_WHITE;

  private int selectionColor;
  private static final int DEFAULT_SELECTION_COLOR = Component.COLOR_LTGRAY;

  private int textSize;
  private int detailTextSize;
  private static final int DEFAULT_TEXT_SIZE = 22;

  private int imageWidth;
  private int imageHeight;
  private static final int DEFAULT_IMAGE_WIDTH = 200;

  // variable for ListView layout types
  private int layout;
  private String propertyValue;

  private ArrayAdapter<JSONObject> itemAdapter;
  private ArrayAdapter<JSONObject> itemAdapterCopy;
  private ArrayList<JSONObject> currentItems;
  private Boolean custom;

  /**
   * Creates a new ListView component.
   * @param container  container that the component will be placed in
   */
  public ListView(ComponentContainer container) {
    super(container);
    this.container = container;
    currentItems = new ArrayList<>();
    items = YailList.makeEmptyList();
    // initialize selectionIndex which also sets selection
    SelectionIndex(0);
    view = new android.widget.ListView(container.$context());
    view.setOnItemClickListener(this);
    view.setChoiceMode(android.widget.ListView.CHOICE_MODE_SINGLE);
    view.setScrollingCacheEnabled(false);
    view.setTextFilterEnabled(true);
    listViewLayout = new LinearLayout(container.$context());
    listViewLayout.setOrientation(LinearLayout.VERTICAL);

    txtSearchBox = new EditText(container.$context());
    txtSearchBox.setSingleLine(true);
    txtSearchBox.setWidth(Component.LENGTH_FILL_PARENT);
    txtSearchBox.setPadding(10, 10, 10, 10);
    txtSearchBox.setHint("Search list...");
    if (!AppInventorCompatActivity.isClassicMode()) {
      txtSearchBox.setBackgroundColor(Color.WHITE);
    }

    //set up the listener
    txtSearchBox.addTextChangedListener(new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
          // When user changed the Text
          if(!currentItems.isEmpty()) {
            setAdapterData();
            itemAdapter.getFilter().filter(cs.toString());
          } else {
            adapter.getFilter().filter(cs);
          }
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

    // set the colors and initialize the elements
    // note that the TextColor and ElementsFromString setters
    // need to have the textColor set first, since they reset the
    // adapter

    Width(Component.LENGTH_FILL_PARENT);
    BackgroundColor(DEFAULT_BACKGROUND_COLOR);
    SelectionColor(DEFAULT_SELECTION_COLOR);

    textColor = DEFAULT_TEXT_COLOR;
    TextColor(textColor);
    detailTextColor = DEFAULT_TEXT_COLOR;
    DetailTextColor(detailTextColor);
    textSize = DEFAULT_TEXT_SIZE;
    detailTextSize = DEFAULT_TEXT_SIZE;
    TextSize(textSize);
    DetailTextSize(detailTextSize);
    imageWidth = DEFAULT_IMAGE_WIDTH;
    imageHeight = DEFAULT_IMAGE_WIDTH;
    ImageWidth(imageWidth);
    ImageHeight(imageHeight);
    ElementsFromString("");

    listViewLayout.addView(txtSearchBox);
    listViewLayout.addView(view);
    listViewLayout.requestLayout();
    container.$add(this);
    ListViewLayout(ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT);
    ListData("");
    Custom(false);
  }

  @Override
  public View getView() {
    return listViewLayout;
  }

  /**
  * Specifies the `%type%`'s vertical height, measured in pixels.
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
  * Specifies the horizontal width of the `%type%`, measured in pixels.
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
   * Sets visibility of the filter bar. `true`{:.logic.block} will show the bar,
   * `false`{:.logic.block} will hide it.
   *
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
   *
   * @suppressdoc
   * @return true or false (visibility)
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns current state of ShowFilterBar for visibility.")
  public boolean ShowFilterBar() {
    return showFilter;
  }

  /**
   * Specifies the list of choices to display.
   * @param itemsList a YailList containing the strings to be added to the ListView
   */
  @SimpleProperty(description="List of text elements to show in the ListView.  This will " +
                "signal an error if the elements are not text strings.",
      category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList itemsList) {
    items = ElementsUtil.elements(itemsList, "Listview");
    setAdapterData();
  }

  /**
   * Elements property getter method
   *
   * @suppressdoc
   * @return a YailList representing the list of strings to be picked from
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public YailList Elements() {
    return items;
  }

  /**
   * Set the list of choices from a string of comma-separated values.
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA, defaultValue = "")
  @SimpleProperty(description="The TextView elements specified as a string with the " +
      "items separated by commas " +
      "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the " +
      "list.",  category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring) {
    items = ElementsUtil.elementsFromString(itemstring);
    setAdapterData();
  }

  /**
   * Sets the items of the ListView through an adapter
   */
  public void setAdapterData(){
    if(!currentItems.isEmpty()) {
      // if the data is available in AddData property
      if(layout == ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT) {
        ListViewArrayAdapterSingleText adapterSingleText = new ListViewArrayAdapterSingleText(textSize, textColor,
            container, currentItems);
        itemAdapter = adapterSingleText.createAdapter();
        itemAdapterCopy = new ArrayAdapter<>(container.$context(), android.R.layout.simple_list_item_1);
      } else if(layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT) {
        ListViewArrayAdapterTwoText adapterTwoText = new ListViewArrayAdapterTwoText(textSize, detailTextSize,
            textColor, detailTextColor, container, currentItems);
        itemAdapter = adapterTwoText.createAdapter();
        itemAdapterCopy = new ArrayAdapter<>(container.$context(), android.R.layout.simple_list_item_2);
      } else if(layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) {
        ListViewArrayAdapterTwoTextLinear adapterTwoTextLinear = new ListViewArrayAdapterTwoTextLinear(textSize,
            detailTextSize, textColor, detailTextColor, container, currentItems);
        itemAdapter = adapterTwoTextLinear.createAdapter();
        itemAdapterCopy = new ArrayAdapter<>(container.$context(), 0);
      } else if(layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
        ListViewArrayAdapterImageSingleText adapterImageSingleText = new ListViewArrayAdapterImageSingleText(textSize,
            textColor, imageWidth, imageHeight, container, currentItems);
        itemAdapter = adapterImageSingleText.createAdapter();
        itemAdapterCopy = new ArrayAdapter<>(container.$context(), 0);
      } else if(layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT) {
        ListViewArrayAdapterImageTwoText adapterImageTwoText = new ListViewArrayAdapterImageTwoText(textSize,
            detailTextSize, textColor, detailTextColor, imageWidth, imageHeight, container, currentItems);
        itemAdapter = adapterImageTwoText.createAdapter();
        itemAdapterCopy = new ArrayAdapter<>(container.$context(), 0);
      }
      view.setAdapter(itemAdapter);
      for(int i = 0; i < itemAdapter.getCount(); ++i) {
        itemAdapterCopy.insert(itemAdapter.getItem(i), i);
      }
    } else {
      // if the data is not available in AddData property but is available in ElementsFromString property
      adapter = new ArrayAdapter<Spannable>(container.$context(), android.R.layout.simple_list_item_1,
          itemsToColoredText());
      view.setAdapter(adapter);

      adapterCopy = new ArrayAdapter<Spannable>(container.$context(), android.R.layout.simple_list_item_1);
      for (int i = 0; i < adapter.getCount(); ++i) {
        adapterCopy.insert(adapter.getItem(i), i);
      }
    }
  }

  public Spannable[] itemsToColoredText() {
    // TODO(hal): Generalize this so that different items could have different
    // colors and even fonts and sizes
    int size = items.size();
    int displayTextSize = textSize;
    Spannable [] objects = new Spannable[size];
    for (int i = 1; i <= size; i++) {
      // Note that the ListPicker and otherPickers pickers convert Yail lists to string by calling
      // YailList.ToStringArray.
      // ListView however, does the string conversion via the adapter, so we must ensure
      // that the adapter uses YailListElementToSring
      String itemString = YailList.YailListElementToString(items.get(i));
      // Is there a more efficient way to do conversion to spannable strings that does not
      // need to allocate new objects?
      Spannable chars = new SpannableString(itemString);
      chars.setSpan(new ForegroundColorSpan(textColor),0,chars.length(),0);
      if (!container.$form().getCompatibilityMode()) {
        displayTextSize = (int) (textSize * container.$form().deviceDensity());
      }
      chars.setSpan(new AbsoluteSizeSpan(displayTextSize),0,chars.length(),0);
      objects[i - 1] = chars;
    }
    return objects;
  }

  /**
   * The index of the currently selected item, starting at `1`. If no item is selected, the value
   * will be `0`. If an attempt is made to set this to a number less than `1` or greater than the
   * number of items in the `ListView`, `SelectionIndex` will be set to `0`, and
   * {@link #Selection(String)} will be set to the empty text.
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
   *
   * @suppressdoc
   * @param index the index to be selected
   */
  @SimpleProperty(description="Specifies the position of the selected item in the ListView. " +
      "This could be used to retrieve" +
      "the text at the chosen position. If an attempt is made to set this to a " +
      "number less than 1 or greater than the number of items in the ListView, SelectionIndex " +
      "will be set to 0, and Selection will be set to the empty text."
      ,
      category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int index){
    if(!currentItems.isEmpty()) {
      selectionIndex = ElementsUtil.selectionIndex(currentItems, index);
      selection = ElementsUtil.setSelectionFromIndex(selectionIndex, currentItems.get(selectionIndex-1));
      selectionDetailText = ElementsUtil.setDetailSelectionFromIndex(selectionIndex, currentItems.get(selectionIndex-1));
    } else {
      selectionIndex = ElementsUtil.selectionIndex(index, items);
      // Now, we need to change Selection to correspond to SelectionIndex.
      selection = ElementsUtil.setSelectionFromIndex(index, items);
      selectionDetailText = "";
    }

  }

  /**
  * Returns the text in the `ListView` at the position of {@link #SelectionIndex(int)}.
  */
  @SimpleProperty(description="Returns the text last selected in the ListView.",
      category = PropertyCategory
      .BEHAVIOR)
  public String Selection(){
      return selection;
  }

  /**
   * Selection property setter method.
   *
   * @suppressdoc
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Selection(String value) {
    selection = value;
    // Now, we need to change SelectionIndex to correspond to Selection.
    if(!currentItems.isEmpty()) {
      selectionIndex = ElementsUtil.setSelectedIndexFromValue(currentItems, value);
      selectionDetailText = ElementsUtil.setDetailSelectionFromIndex(selectionIndex, currentItems.get(selectionIndex-1));
    } else {
      selectionIndex = ElementsUtil.setSelectedIndexFromValue(value, items);
      selectionDetailText = "";
    }
  }

  /**
   * Returns the Secondary or Detail text in the ListView at the position set by SelectionIndex
   */
  @SimpleProperty(description = "Returns the secondary text of the selected row in the ListView.",
      category = PropertyCategory.BEHAVIOR)
  public String SelectionDetailText(){
    return selectionDetailText;
  }

  /**
   * SelectionDetailText property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void SelectionDetailText(String value){
    selectionDetailText = value;
    if(!currentItems.isEmpty() && (selection == null || selection.equals(""))) {
      selectionIndex = ElementsUtil.setSelectedIndexFromDetailTextValue(currentItems, value);
      selection = ElementsUtil.setSelectionFromIndex(selectionIndex, currentItems.get(selectionIndex-1));
    } else {
      selectionIndex = 0;
      selection = "";
    }
  }

  /**
   * Simple event to raise when the component is clicked. Implementation of
   * AdapterView.OnItemClickListener
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if(!currentItems.isEmpty()) {
      JSONObject item = (JSONObject) parent.getAdapter().getItem(position);
      this.selection = item.has("Text1") ? item.getString("Text1") : "";
      this.selectionDetailText = item.has("Text2") ? item.getString("Text2") : "";
      this.selectionIndex = itemAdapterCopy.getPosition(item)+1;
    } else {
      System.out.println("Spannable Adapter/...........");
      Spannable item = (Spannable) parent.getAdapter().getItem(position);
      this.selection = item.toString();
      this.selectionIndex = adapterCopy.getPosition(item) + 1; // AI lists are 1-based
    }
    AfterPicking();
  }

  /**
   * Simple event to be raised after the an element has been chosen in the list.
   * The selected element is available in the {@link #Selection(String)} property.
   */
  @SimpleEvent(description = "Simple event to be raised after the an element has been chosen in the" +
      " list. The selected element is available in the Selection property.")
  public void AfterPicking() {
    EventDispatcher.dispatchEvent(this, "AfterPicking");
  }

  /**
   * Assigns a value to the backgroundColor
   * @param color  an alpha-red-green-blue integer for a color
   */

  public void setBackgroundColor(int color) {
      backgroundColor = color;
      view.setBackgroundColor(backgroundColor);
      listViewLayout.setBackgroundColor(backgroundColor);
      // Keeps background color behind list elements correct when scrolling through listView
      view.setCacheColorHint(backgroundColor);
  }

  /**
   * Returns the listview's background color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @return background color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(
      description = "The color of the listview background.",
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * The color of the `ListView` background.
   *
   * @internaldoc
   * Specifies the ListView's background color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @param argb background color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty
  public void BackgroundColor(int argb) {
      backgroundColor = argb;
      setBackgroundColor(backgroundColor);
  }

  /**
   * Returns the listview's selection color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   * Is not supported on Icecream Sandwich or earlier
   *
   * @return selection color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(description = "The color of the item when it is selected.")
  @IsColor
  public int SelectionColor() {
    return selectionColor;
  }

  /**
   * The color of the item when it is selected.
   *
   * @internaldoc
   * Specifies the ListView's selection color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   * Is not supported on Icecream Sandwich or earlier
   *
   * @param argb selection color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
  @SimpleProperty
  public void SelectionColor(int argb) {
    selectionColor = argb;
    view.setSelector(new GradientDrawable(
      GradientDrawable.Orientation.TOP_BOTTOM, new int[]{argb, argb}
    ));
  }

  /**
   * Returns the listview's text item color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @return background color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(
      description = "The text color of the listview items.",
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int TextColor() {
    return textColor;
  }

  /**
   * The text color of the `ListView` items.
   *
   * @internaldoc
   * Specifies the ListView item's text color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @param argb background color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void TextColor(int argb) {
      textColor = argb;
      setAdapterData();
  }

  /**
   * Returns the color of the secondary text in a ListView layout
   *
   * @return color of the secondary text
   */
  @SimpleProperty(
      description = "The text color of DetailText of listview items. ",
      category = PropertyCategory.APPEARANCE)
  public int DetailTextColor() {
    return detailTextColor;
  }

  /**
   * Specifies the color of the secondary text in a ListView layout
   *
   * @param argb background color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void DetailTextColor(int argb) {
    detailTextColor = argb;
    setAdapterData();
  }

  /**
   * Returns the listview's text font Size
   *
   * @return text size as an float
   */
  @SimpleProperty(
      description = "The text size of the listview items.",
      category = PropertyCategory.APPEARANCE)
  public int TextSize() {
    return textSize;
  }

  /**
   * Specifies the `ListView` item's text font size
   *
   * @param integer value for font size
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_TEXT_SIZE + "")
  @SimpleProperty
  public void TextSize(int fontSize) {
    if(fontSize>1000)
      textSize = 999;
    else
      textSize = fontSize;
    setAdapterData();
  }

  /**
   * Returns the listview's secondary-text font Size
   *
   * @return text size as an float
   */
  @SimpleProperty(
      description = "The detailText size of the listview items.",
      category = PropertyCategory.APPEARANCE)
  public int DetailTextSize() {
    return detailTextSize;
  }

  /**
   * Specifies the ListView item's secondary-text font size
   *
   * @param integer value for font size
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_TEXT_SIZE + "")
  @SimpleProperty
  public void DetailTextSize(int fontSize) {
    if(fontSize>1000)
      detailTextSize = 999;
    else
      detailTextSize = fontSize;
    setAdapterData();
  }

  /**
   * Returns the image width of ListView layouts containing images
   *
   * @return width of image
   */
  @SimpleProperty(
      description = "The image width of the listview image items.",
      category = PropertyCategory.APPEARANCE)
  public int ImageWidth() {
    return imageWidth;
  }

  /**
   * Specifies the image width of ListView layouts containing images
   *
   * @param width sets the width of image in the ListView row
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_IMAGE_WIDTH + "")
  @SimpleProperty
  public void ImageWidth(int width) {
    imageWidth = width;
    setAdapterData();
  }

  /**
   * Returns the image height of ListView layouts containing images
   *
   * @return height of image
   */
  @SimpleProperty(
      description = "The image height of the listview image items.",
      category = PropertyCategory.APPEARANCE)
  public int ImageHeight() {
    return imageHeight;
  }

  /**
   * Specifies the image height of ListView layouts containing images
   *
   * @param height sets the height of image in the ListView row
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_IMAGE_WIDTH + "")
  @SimpleProperty
  public void ImageHeight(int height) {
    imageHeight = height;
    setAdapterData();
  }

  /**
   * Returns type of layout selected to display. Designer only property.
   *
   * @return layout as integer value
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
  public int ListViewLayout() {
    return layout;
  }

  /**
   * Specifies type of layout for ListView row. Designer only property.
   *
   * @param value integer value to determine type of ListView layout
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_LAYOUT,
      defaultValue = ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT+"")
  @SimpleProperty(userVisible = false)
  public void ListViewLayout(int value) {
    layout = value;
    setAdapterData();
  }

  /**
   * Returns the data to be displayed in the ListView as a JsonString. Designer only property.
   *
   * @return string form of the array of JsonObject
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String ListData() {
    return propertyValue;
  }

  /**
   * Specifies data to be displayed in the ListView rows as an ArrayList of JsonObjects. Designer only property.
   *
   * @param propertyValue string representation of row data (JsonArray of JsonObjects)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_ADD_DATA)
  @SimpleProperty(userVisible = false, category = PropertyCategory.BEHAVIOR)
  public void ListData(String propertyValue){
    this.propertyValue = propertyValue;
    if(propertyValue != null && propertyValue != "") {
      JSONArray arr = new JSONArray(propertyValue);
      for(int i = 0; i < arr.length(); ++i) {
        currentItems.add(i, arr.getJSONObject(i));
      }
    }
    setAdapterData();
  }

  @SimpleProperty(
          category = PropertyCategory.BEHAVIOR)
  public boolean Custom() {
    return custom;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty
  public void Custom(boolean value) {
    custom = value;
  }
}
