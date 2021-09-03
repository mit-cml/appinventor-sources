// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.widget.LinearLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.appinventor.components.runtime.util.YailDictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a visible component that displays a list of text and image elements in your {@link Form} to
 * display. Simple lists of strings may be set using the {@link #ElementsFromString(String)} property.
 * More complex lists of elements containing multiple strings and/or images can be created using the
 * {@link #ListData(String)} and {@link #ListViewLayout(int)} properties.
 *
 * [Information on Layouts](../other/advanced-listview.html)
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
    description = "<p>This is a visible component that displays a list of text and image elements.</p>" +
        " <p>Simple lists of strings may be set using the ElementsFromString property." +
        " More complex lists of elements containing multiple strings and/or images can be created using " +
        "the ListData and ListViewLayout properties. </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/listView.png")
@SimpleObject
@UsesLibraries(libraries ="recyclerview.jar, cardview.jar, cardview.aar")
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
        "android.permission.READ_EXTERNAL_STORAGE")
public final class ListView extends AndroidViewComponent implements AdapterView.OnItemClickListener {

  private static final String LOG_TAG = "ListView";

  private EditText txtSearchBox;
  protected final ComponentContainer container;
  private final LinearLayout linearLayout;

  private RecyclerView recyclerView;
  private ListAdapterWithRecyclerView listAdapterWithRecyclerView;
  private YailList stringItems;
  private List<YailDictionary> dictItems;
  private int selectionIndex;
  private String selection;
  private String selectionDetailText;
  private boolean showFilter = false;
  private static final boolean DEFAULT_ENABLED = false;
  private int orientation;

  private int backgroundColor;
  private static final int DEFAULT_BACKGROUND_COLOR = Component.COLOR_BLACK;

  private int textColor;
  private int detailTextColor;

  private int selectionColor;

  private float fontSizeMain;
  private float fontSizeDetail;
  private int fontTypeface;
  private int fontTypeDetail;

  /* for backward compatibility */
  private static final int DEFAULT_TEXT_SIZE = 22;

  private int imageWidth;
  private int imageHeight;
  private static final int DEFAULT_IMAGE_WIDTH = 200;

  // variable for ListView layout types
  private int layout;
  private String propertyValue;  // JSON string representing data entered through the Designer

  /**
   * Creates a new ListView component.
   *
   * @param container container that the component will be placed in
   */
  public ListView(ComponentContainer container) {

    super(container);
    this.container = container;
    stringItems = YailList.makeEmptyList();
    dictItems = new ArrayList<>();

    linearLayout = new LinearLayout(container.$context());
    linearLayout.setOrientation(LinearLayout.VERTICAL);
    orientation = ComponentConstants.LAYOUT_ORIENTATION_VERTICAL;
    layout = ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT;

    recyclerView = new RecyclerView(container.$context());
    LayoutParams paramms = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    recyclerView.setLayoutParams(paramms);
    // initialize selectionIndex which also sets selection
    SelectionIndex(0);

    txtSearchBox = new EditText(container.$context());
    txtSearchBox.setSingleLine(true);
    txtSearchBox.setWidth(Component.LENGTH_FILL_PARENT);
    txtSearchBox.setPadding(10, 10, 10, 10);
    txtSearchBox.setHint("Search list...");
    if (!AppInventorCompatActivity.isClassicMode()) {
      txtSearchBox.setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
    }

    //set up the listener
    txtSearchBox.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        // When user changed the Text
        if (cs.length() == 0) {
          setAdapterData();
        } else {
          listAdapterWithRecyclerView.getFilter().filter(cs.toString());
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

    BackgroundColor(Component.COLOR_BLACK);
    SelectionColor(Component.COLOR_LTGRAY);
    TextColor(Component.COLOR_WHITE);
    TextColorDetail(Component.COLOR_WHITE);
    FontSize(22.0f);  // This was the original size of ListView text.
    FontSizeDetail(Component.FONT_DEFAULT_SIZE);
    FontTypeface(Component.TYPEFACE_DEFAULT);
    FontTypefaceDetail(Component.TYPEFACE_DEFAULT);
    // initially assuming that the image is of square shape
    ImageWidth(DEFAULT_IMAGE_WIDTH);
    ImageHeight(DEFAULT_IMAGE_WIDTH);
    ElementsFromString("");
    ListData("");

    linearLayout.addView(txtSearchBox);
    linearLayout.addView(recyclerView);
    linearLayout.requestLayout();
    container.$add(this);
    Width(Component.LENGTH_FILL_PARENT);
    ListViewLayout(ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT);
  }

  @Override
  public View getView() {
    return linearLayout;
  }

  /**
   * Specifies the `%type%`'s vertical height, measured in pixels.
   *
   * @param height for height length
   */
  @Override
  @SimpleProperty(description = "Determines the height of the list on the view.",
          category = PropertyCategory.APPEARANCE)
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }

  /**
   * Specifies the horizontal width of the `%type%`, measured in pixels.
   *
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
    } else {
      txtSearchBox.setVisibility(View.GONE);
    }
  }

  /**
   * Returns true or false depending on the visibility of the Filter bar element
   *
   * @return true or false (visibility)
   * @suppressdoc
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
          description = "Returns current state of ShowFilterBar for visibility.")
  public boolean ShowFilterBar() {
    return showFilter;
  }

  /**
   * Specifies the list of choices to display.
   *
   * @param itemsList a YailList containing the strings to be added to the ListView
   */
  @SimpleProperty(description = "List of elements to show in the ListView. Depending on the ListView, this may be a list of strings or a list of 3-element sub-lists containing Text, Description, and Image file name.",
          category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList itemsList) {
    dictItems.clear();
    stringItems = YailList.makeEmptyList();
    if (itemsList.size() > 0) {
      Object firstitem = itemsList.getObject(0);
      // Check to see if this is a list of strings (backward compatibility) or a list of Dictionaries
      if (firstitem instanceof YailDictionary) {
        //  To preserve backward compatibility with the old single-string ListView, we check to be sure we
        // have dictionary elements. If first element is a dictionary, treat all as such.
        for (int i = 0; i < itemsList.size(); i++) {
          Object o = itemsList.getObject(i);
          if (o instanceof YailDictionary) {
            YailDictionary yailItem = (YailDictionary) o;
            dictItems.add(i, yailItem);
          } else {
            // Support strings mixed in with the Dictionary elements because somebody will end up doing this.
            YailDictionary yailItem = new YailDictionary();
            yailItem.put(Component.LISTVIEW_KEY_MAIN_TEXT, YailList.YailListElementToString(o));
            dictItems.add(i, yailItem);
          }
        }
      } else {
        // Support legacy single-string ListViews
        stringItems = ElementsUtil.elements(itemsList, "Listview");
      }
    }
    setAdapterData();
  }

  /**
   * Elements property getter method
   *
   * @return a YailList representing the list of strings to be picked from
   * @suppressdoc
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public YailList Elements() {
    if (dictItems.size() > 0) {
      return YailList.makeList(dictItems);
    } else {
      return stringItems;
    }
  }

  /**
   * Set the list of choices specified as a string with the elements separated by commas
   * such as: Cheese,Fruit,Bacon,Radish.
   *
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA, defaultValue = "")
  @SimpleProperty(description = "The TextView elements specified as a string with the " +
          "stringItems separated by commas " +
          "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the " +
          "list.", category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring) {
    stringItems = ElementsUtil.elementsFromString(itemstring);
    setAdapterData();
  }

  /**
   * Sets the stringItems of the ListView through an adapter
   */
  public void setAdapterData() {
    LinearLayoutManager layoutManager;
    if (!dictItems.isEmpty()) {
      // if the data is available in AddData property
      listAdapterWithRecyclerView = new ListAdapterWithRecyclerView(container, dictItems, textColor, detailTextColor, fontSizeMain, fontSizeDetail, fontTypeface, fontTypeDetail, layout, backgroundColor, selectionColor, imageWidth, imageHeight, false);

      listAdapterWithRecyclerView.setOnItemClickListener(new ListAdapterWithRecyclerView.ClickListener() {
        @Override
        public void onItemClick(int position, View v) {
          listAdapterWithRecyclerView.toggleSelection(position);
          SelectionIndex(position + 1);
          AfterPicking();
        }
      });
      GridLayoutManager gridlayoutManager;

      if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
        layoutManager = new LinearLayoutManager(container.$context(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
      } else { // if (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) {
        layoutManager = new LinearLayoutManager(container.$context(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
      } //else {
//        gridlayoutManager = new GridLayoutManager(container.$context(), gridCount, GridLayoutManager.VERTICAL, false);
//        recyclerView.setLayoutManager(gridlayoutManager);
      // TODO: Grid Layout
      //     }
      recyclerView.setAdapter(listAdapterWithRecyclerView);
    } else {
      // Legacy Support: if the data is not available in AddData property but is available in ElementsFromString property
      listAdapterWithRecyclerView = new ListAdapterWithRecyclerView(container, stringItems, textColor, fontSizeMain, fontTypeface, backgroundColor, selectionColor);

      listAdapterWithRecyclerView.setOnItemClickListener(new ListAdapterWithRecyclerView.ClickListener() {
        @Override
        public void onItemClick(int position, View v) {
          listAdapterWithRecyclerView.toggleSelection(position);
          SelectionIndex(position + 1);
          AfterPicking();
        }
      });
      layoutManager = new LinearLayoutManager(container.$context(), LinearLayoutManager.VERTICAL, false);
      recyclerView.setLayoutManager(layoutManager);
      recyclerView.setAdapter(listAdapterWithRecyclerView);
    }
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
                  "of stringItems in the ListView, SelectionIndex will be set to 0, and " +
                  "Selection will be set to the empty text.",
          category = PropertyCategory.BEHAVIOR)
  public int SelectionIndex() {
    return selectionIndex;
  }


  /**
   * Sets the index to the passed argument for selection
   *
   * @param index the index to be selected
   * @suppressdoc
   */
  @SimpleProperty(description = "Specifies the one-indexed position of the selected item in the " +
          "ListView. This could be used to retrieve" +
          "the text at the chosen position. If an attempt is made to set this to a " +
          "number less than 1 or greater than the number of stringItems in the ListView, SelectionIndex " +
          "will be set to 0, and Selection will be set to the empty text."
          ,
          category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int index) {
    if (!dictItems.isEmpty()) {
      selectionIndex = ElementsUtil.selectionIndex(index, YailList.makeList(dictItems));
      selection = dictItems.get(selectionIndex - 1).get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
      selectionDetailText = ElementsUtil.toStringEmptyIfNull(dictItems.get(selectionIndex - 1).get("Text2").toString());
    } else {
      selectionIndex = ElementsUtil.selectionIndex(index, stringItems);
      // Now, we need to change Selection to correspond to SelectionIndex.
      selection = ElementsUtil.setSelectionFromIndex(index, stringItems);
      selectionDetailText = "";
    }

  }

  /**
   * Returns the text in the `ListView` at the position of {@link #SelectionIndex(int)}.
   */
  @SimpleProperty(description = "Returns the text last selected in the ListView.",
          category = PropertyCategory
                  .BEHAVIOR)
  public String Selection() {
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
    if (!dictItems.isEmpty()) {
      for (int i = 0; i < dictItems.size(); ++i) {
        YailDictionary item = dictItems.get(i);
        if (item.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString() == value) {
          selectionIndex = i + 1;
          selectionDetailText = ElementsUtil.toStringEmptyIfNull(item.get("Text2"));
          break;
        }
        // Not found
        selection = "";
        selectionIndex = 0;
        selectionDetailText = "";
      }
    } else {
      selectionIndex = ElementsUtil.setSelectedIndexFromValue(value, stringItems);
      selectionDetailText = "";
    }
  }

  /**
   * Returns the Secondary or Detail text in the ListView at the position set by SelectionIndex
   */
  @SimpleProperty(description = "Returns the secondary text of the selected row in the ListView.",
          category = PropertyCategory.BEHAVIOR)
  public String SelectionDetailText() {
    return selectionDetailText;
  }

  /**
   * Simple event to raise when the component is clicked. Implementation of
   * AdapterView.OnItemClickListener
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    YailDictionary item = (YailDictionary) parent.getAdapter().getItem(position);
    this.selection = ElementsUtil.toStringEmptyIfNull(item.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString());
    this.selectionDetailText = ElementsUtil.toStringEmptyIfNull(item.get("Text2"));
    this.selectionIndex = position + 1;
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
   * @param argb background color in the format 0xAARRGGBB, which
   *             includes alpha, red, green, and blue components
   * @internaldoc Specifies the ListView's background color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    recyclerView.setBackgroundColor(backgroundColor);
    linearLayout.setBackgroundColor(backgroundColor);
//    setBackgroundColor(backgroundColor);
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
   * @param argb selection color in the format 0xAARRGGBB, which
   *             includes alpha, red, green, and blue components
   * @internaldoc Specifies the ListView's selection color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   * Is not supported on Icecream Sandwich or earlier
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
  @SimpleProperty
  public void SelectionColor(int argb) {
    selectionColor = argb;
    setAdapterData();
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
          description = "The text color of the listview stringItems.",
          category = PropertyCategory.APPEARANCE)
  @IsColor
  public int TextColor() {
    return textColor;
  }

  /**
   * The text color of the `ListView` items.
   *
   * @param argb background color in the format 0xAARRGGBB, which
   *             includes alpha, red, green, and blue components
   * @internaldoc Specifies the ListView item's text color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
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
          description = "The text color of DetailText of listview stringItems. ",
          category = PropertyCategory.APPEARANCE)
  public int TextColorDetail() {
    return detailTextColor;
  }

  /**
   * Specifies the color of the secondary text in a ListView layout
   *
   * @param argb background color in the format 0xAARRGGBB, which
   *             includes alpha, red, green, and blue components
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void TextColorDetail(int argb) {
    detailTextColor = argb;
    setAdapterData();
  }

  /**
   * Returns the listview's text font Size
   *
   * This property is provided for backwards compatibility
   * it takes and returns an integer, but in reality it just
   * updates the FontSize property, which works with floats
   *
   * @return text size as an integer
   */
  @SimpleProperty(
      description = "The text size of the listview items.",
      category = PropertyCategory.APPEARANCE)
  public int TextSize() {
    return Math.round(fontSizeMain);
  }

  /**
   * Specifies the `ListView` item's text font size
   *
   * @param integer value for font size
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_TEXT_SIZE + "")
  @SimpleProperty
  public void TextSize(int textSize) {
    if (textSize >1000) {
        textSize = 999;
    }
    FontSize(Float.valueOf(textSize));
  }

  /**
   * Returns the listview's text font Size
   *
   * @return text size as an float
   */
  @SimpleProperty(
          description = "The text size of the listview stringItems.",
          category = PropertyCategory.APPEARANCE,
          userVisible = false)
  public float FontSize() {
    return fontSizeMain;
  }

  /**
   * Specifies the `ListView` item's text font size
   *
   * @param integer value for font size
   */
  @SuppressWarnings("JavadocReference")
  // Temporarily removed until Companion with support is more prevalent
  // @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
  //         defaultValue = "22.0")
  @SimpleProperty
  public void FontSize(float fontSize) {
    if (fontSize > 1000 || fontSize < 1)
      fontSizeMain = 999;
    else
      fontSizeMain = fontSize;
    setAdapterData();
  }
  /**
   * Returns the listview's text font Size
   *
   * @return text size as an float
   */
  @SimpleProperty(
          description = "The text size of the listview stringItems.",
          category = PropertyCategory.APPEARANCE)
  public float FontSizeDetail() {
    return fontSizeDetail;
  }

  /**
   * Specifies the `ListView` item's text font size
   *
   * @param integer value for font size
   */
  @SuppressWarnings("JavadocReference")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
          defaultValue = Component.FONT_DEFAULT_SIZE + "")
  @SimpleProperty
  public void FontSizeDetail(float fontSize) {
    if (fontSize > 1000 || fontSize < 1)
      fontSizeDetail = 999;
    else
      fontSizeDetail = fontSize;
    setAdapterData();
  }

  /**
   * Returns the label's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @return  one of {@link Component#TYPEFACE_DEFAULT},
   *          {@link Component#TYPEFACE_SERIF},
   *          {@link Component#TYPEFACE_SANSSERIF} or
   *          {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          userVisible = false)
  public int FontTypeface() {
    return fontTypeface;
  }

  /**
   * Specifies the label's text's font face as default, serif, sans
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
    setAdapterData();
  }

  /**
   * Returns the label's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @return  one of {@link Component#TYPEFACE_DEFAULT},
   *          {@link Component#TYPEFACE_SERIF},
   *          {@link Component#TYPEFACE_SANSSERIF} or
   *          {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          userVisible = false)
  public int FontTypefaceDetail() {
    return fontTypeDetail;
  }

  /**
   * Specifies the label's text's font face as default, serif, sans
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
  public void FontTypefaceDetail(int typeface) {
    fontTypeDetail = typeface;
    setAdapterData();
  }
  /**
   * Returns the image width of ListView layouts containing images
   *
   * @return width of image
   */
  @SimpleProperty(
          description = "The image width of the listview image.",
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
          description = "The image height of the listview image stringItems.",
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
          defaultValue = ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT + "")
  @SimpleProperty(userVisible = false)
  public void ListViewLayout(int value) {
    layout = value;
    setAdapterData();
  }

  /**
   * Returns the style of the button.
   *
   * @return one of {@link Component#VERTICAL_ORIENTATION},
   * {@link Component#HORISONTAL_ORIENTATION},
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE)
  public int Orientation() {
    return orientation;
  }

  /**
   * Specifies the layout's orientation. This may be: `Vertical`, which displays elements
   * in rows one after the other; or `Horizontal`, which displays one element at a time and
   * allows the user to swipe left or right to brows the elements.
   *
   * @param orientation one of {@link Component#VERTICAL_ORIENTATION},
   *              {@link Component#HORIZONTAL_ORIENTATION},
   * @throws IllegalArgumentException if orientation is not a legal value.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_RECYCLERVIEW_ORIENTATION,
          defaultValue = Component.VERTICAL_ORIENTATION + "")
  @SimpleProperty(description = "Specifies the layout's orientation (vertical, horizontal). ")
  public void Orientation(int orientation) {
    this.orientation = orientation;
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
   * Specifies data to be displayed in the ListView elements. This property sets the
   * elements specified in {@link #ListViewLayout(int)}. For example, if the chosen
   * layout is `Image,MainText` this property will allow any number of elements to be
   * defined, each containing a filename for Image and a string for MainText.
   * Designer only property.
   *
   * @param propertyValue string representation of row data (JsonArray of JsonObjects)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_ADD_DATA)
  @SimpleProperty(userVisible = false, category = PropertyCategory.BEHAVIOR)
  public void ListData(String propertyValue) {
    this.propertyValue = propertyValue;
    dictItems.clear();
    if (propertyValue != null && propertyValue != "") {
      try {
        JSONArray arr = new JSONArray(propertyValue);
        // Convert the JSON data into a list of Dictionaries
        for (int i = 0; i < arr.length(); ++i) {
          JSONObject jsonItem = arr.getJSONObject(i);
          YailDictionary yailItem = new YailDictionary();
          if (jsonItem.has("Text1")) {
            yailItem.put(Component.LISTVIEW_KEY_MAIN_TEXT, jsonItem.getString("Text1"));
            yailItem.put(Component.LISTVIEW_KEY_DESCRIPTION, jsonItem.has("Text2") ? jsonItem.getString("Text2") : "");
            yailItem.put(Component.LISTVIEW_KEY_IMAGE, jsonItem.has("Image") ? jsonItem.getString("Image") : "");
            dictItems.add(yailItem);
          }
        }
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Malformed JSON in ListView.ListData", e);
        container.$form().dispatchErrorOccurredEvent(this, "ListView.ListData", ErrorMessages.ERROR_DEFAULT, e.getMessage());
      }
    }
    setAdapterData();
  }

  /**
   * Creates a
   *
   * @param mainText     Primary text of the entry. Should be unique if possible.
   * @param detailText   Additional descriptive text.
   * @param imageName    File name of an image that has been uploaded to media.
   *
   */
  @SimpleFunction(description = "Create a ListView entry. MainText is required. DetailText and ImageName are optional.")
  public YailDictionary CreateElement(final String mainText, final String detailText, final String imageName) {
    YailDictionary dictItem = new YailDictionary();
    dictItem.put(Component.LISTVIEW_KEY_MAIN_TEXT, mainText);
    dictItem.put("Text2", detailText);
    dictItem.put("Image", imageName);
    return dictItem;
  }

  @SimpleFunction(description = "Get the Main Text of a ListView element.")
  public String GetMainText(final YailDictionary listElement) {
    return listElement.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
  }

  @SimpleFunction(description = "Get the Detail Text of a ListView element.")
  public String GetDetailText(final YailDictionary listElement) {
    return listElement.get("Text2").toString();
  }

  @SimpleFunction(description = "Get the filename of the image of a ListView element that has been uploaded to Media.")
  public String GetImageName(final YailDictionary listElement) {
    return listElement.get("Image").toString();
  }

  @SimpleFunction(description = "Reload the ListView to reflect any changes in the data.")
  public void Refresh() {
    setAdapterData();
  }
}
