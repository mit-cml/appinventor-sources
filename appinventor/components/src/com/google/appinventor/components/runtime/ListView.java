// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.common.LayoutType;
import com.google.appinventor.components.common.ListOrientation;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.YailDictionary;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

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
@UsesLibraries({"recyclerview.jar", "recyclerview.aar", "cardview.jar", "cardview.aar", "dynamicanimation.jar"})
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
        "android.permission.READ_EXTERNAL_STORAGE")
public final class ListView extends AndroidViewComponent {

  protected static final String LOG_TAG = "ListView";

  private EditText txtSearchBox;
  protected final ComponentContainer container;
  private final LinearLayout linearLayout;

  private RecyclerView recyclerView;
  private ListAdapterWithRecyclerView listAdapterWithRecyclerView;
  private LinearLayoutManager layoutManager;
  private List<Object> items;
  private int selectionIndex;
  private String selection;
  private String selectionDetailText = "Uninitialized";
  private boolean showFilter = false;
  private static final boolean DEFAULT_ENABLED = false;
  private int orientation;

  private int backgroundColor;
  private static final int DEFAULT_BACKGROUND_COLOR = Component.COLOR_BLACK;

  private int elementColor = COLOR_NONE;

  private int textColor;
  private int detailTextColor;

  private int selectionColor;

  private float fontSizeMain;
  private float fontSizeDetail;
  private String fontTypeface;
  private String fontTypeDetail;

  private String hint;

  /* for backward compatibility */
  private static final float DEFAULT_TEXT_SIZE = 22;

  private int imageWidth;
  private int imageHeight;
  private static final int DEFAULT_IMAGE_WIDTH = 200;

  // variable for ListView layout types
  private int layout;
  private String propertyValue;  // JSON string representing data entered through the Designer

  private boolean multiSelect;
  private Paint dividerPaint;
  private int dividerColor;
  private int dividerSize;
  private static final int DEFAULT_DIVIDER_SIZE = 0;
  private int margins;
  private static final int DEFAULT_RADIUS = 0;
  private int radius;
  private RecyclerView.EdgeEffectFactory edgeEffectFactory;
  private ListBounceEdgeEffectFactory bounceEdgeEffectFactory;
  private boolean bounceEffect;
  private final LinearLayout listLayout;
  private static final int DEFAULT_MARGINS_SIZE = 0;

  /**
   * Creates a new ListView component.
   *
   * @param container container that the component will be placed in
   */
  public ListView(ComponentContainer container) {

    super(container);
    this.container = container;
    items = new ArrayList<>();

    linearLayout = new LinearLayout(container.$context());
    linearLayout.setOrientation(LinearLayout.VERTICAL);
    orientation = ComponentConstants.LAYOUT_ORIENTATION_VERTICAL;
    layout = ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT;

    recyclerView = new RecyclerView(container.$context());
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    recyclerView.setLayoutParams(params);

    layoutManager = new LinearLayoutManager(container.$context(), LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);

    listLayout = new LinearLayout(container.$context());
    LinearLayout.LayoutParams paramsList = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    listLayout.setLayoutParams(paramsList);
    listLayout.setOrientation(LinearLayout.VERTICAL);

    dividerColor = Component.COLOR_WHITE;
    dividerSize = DEFAULT_DIVIDER_SIZE;
    margins = DEFAULT_DIVIDER_SIZE;

    edgeEffectFactory = recyclerView.getEdgeEffectFactory();
    bounceEdgeEffectFactory = new ListBounceEdgeEffectFactory();

    txtSearchBox = new EditText(container.$context());
    txtSearchBox.setSingleLine(true);
    txtSearchBox.setWidth(Component.LENGTH_FILL_PARENT);
    txtSearchBox.setPadding(10, 10, 10, 10);
    HintText("Search list...");
    if (!AppInventorCompatActivity.isClassicMode()) {
      txtSearchBox.setBackgroundColor(COLOR_WHITE);
    }

    if (container.$form().isDarkTheme()) {
      txtSearchBox.setTextColor(COLOR_BLACK);
      txtSearchBox.setHintTextColor(COLOR_BLACK);
    }

    //set up the listener
    txtSearchBox.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        // When user changed the Text
        listAdapterWithRecyclerView.getFilter().filter(cs);
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
    BackgroundColor(DEFAULT_BACKGROUND_COLOR);
    SelectionColor(Component.COLOR_LTGRAY);
    TextColor(Component.COLOR_WHITE);
    TextColorDetail(Component.COLOR_WHITE);
    DividerColor(Component.COLOR_WHITE);
    DividerThickness(DEFAULT_DIVIDER_SIZE);
    ElementMarginsWidth(DEFAULT_MARGINS_SIZE);
    FontSize(DEFAULT_TEXT_SIZE);  // This was the original size of ListView text.
    FontSizeDetail(Component.FONT_DEFAULT_SIZE);
    FontTypeface(Component.TYPEFACE_DEFAULT);
    FontTypefaceDetail(Component.TYPEFACE_DEFAULT);
    // initially assuming that the image is of square shape
    ImageWidth(DEFAULT_IMAGE_WIDTH);
    ImageHeight(DEFAULT_IMAGE_WIDTH);
    ElementCornerRadius(DEFAULT_RADIUS);
    MultiSelect(false);
    BounceEdgeEffect(false);
    ElementsFromString("");
    ListData("");

    listLayout.addView(recyclerView);
    linearLayout.addView(txtSearchBox);
    linearLayout.addView(listLayout);
    linearLayout.requestLayout();
    container.$add(this);
    Width(Component.LENGTH_FILL_PARENT);
    ListViewLayout(ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT);
    // initialize selectionIndex which also sets selection
    SelectionIndex(0);
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

  @DesignerProperty (editorType= PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue= "Search list...")
  @SimpleProperty (description = "Sets hint on the filter bar.",
      category = PropertyCategory.BEHAVIOR)
  public void HintText(String hint) {
    this.hint = hint;
    txtSearchBox.setHint(hint);
  }

  @SimpleProperty
  public String HintText() {
    return hint;
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
  @SimpleProperty
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
  @SimpleProperty(description = "List filter bar, allows to search the list for relevant items. "
      + "True will display the bar, False will hide it.",
      category = PropertyCategory.BEHAVIOR)
  public boolean ShowFilterBar() {
    return showFilter;
  }

  /**
   * Specifies the list of choices to display.
   *
   * @param itemsList a List containing the strings to be added to the ListView
   */
  @SimpleProperty
  public void Elements(List<Object> itemsList) {
    items = new ArrayList<>(itemsList);
    updateAdapterData();
    listAdapterWithRecyclerView.notifyDataSetChanged();
  }

  /**
   * Elements property getter method
   *
   * @return a YailList representing the list of strings to be picked from
   * @suppressdoc
   */
  @SimpleProperty(description = "List of elements to show in the ListView. Depending "
      + "on the ListView, this may be a list of strings or a list of 3-element sub-lists "
      + "containing Text, Description, and Image file name.",
      category = PropertyCategory.BEHAVIOR)
  public List<Object> Elements() {
    return items;
  }

  /**
   * Set the list of choices specified as a string with the elements separated by commas
   * such as: Cheese,Fruit,Bacon,Radish.
   *
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA, defaultValue = "")
  @SimpleProperty(description = "The TextView elements specified as a string with the "
                                    + "stringItems separated by commas such as: Cheese,Fruit,Bacon,Radish. Each word "
                                    + "before the comma will be an element in the list.",
      category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring) {
    items = new ArrayList<Object>(ElementsUtil.elementsListFromString(itemstring));
    updateAdapterData();
    listAdapterWithRecyclerView.notifyDataSetChanged();
  }

  /**
   * The index of the currently selected item, starting at `1`. If no item is selected, the value
   * will be `0`. If an attempt is made to set this to a number less than `1` or greater than the
   * number of items in the `ListView`, `SelectionIndex` will be set to `0`, and
   * {@link #Selection(String)} will be set to the empty text.
   */
  @SimpleProperty(description = "The index of the currently selected item, starting at 1. "
                                    + "If no item is selected, the value will be 0. If an attempt is made to set this "
                                    + "to a number less than 1 or greater than the number of stringItems in the ListView, "
                                    + "SelectionIndex will be set to 0, and Selection will be set to the empty text.",
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
  @SimpleProperty
  public void SelectionIndex(int index) {
    selectionIndex = index;
    if (index > 0 && index <= items.size()) {
      Object o = items.get(index - 1);
      if (o instanceof YailDictionary) {
        if (((YailDictionary) o).containsKey(Component.LISTVIEW_KEY_MAIN_TEXT)) {
          selection = ((YailDictionary) o).get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
          selectionDetailText = ElementsUtil.toStringEmptyIfNull(((YailDictionary) o)
              .get(Component.LISTVIEW_KEY_DESCRIPTION));
        } else {
          selection = o.toString();
        }
      } else {
        selection = o.toString();
      }
      if (multiSelect) {
        listAdapterWithRecyclerView.changeSelections(selectionIndex - 1);
      } else {
        listAdapterWithRecyclerView.toggleSelection(selectionIndex - 1);
      }
    } else {
      selection = "";
      listAdapterWithRecyclerView.clearSelections();
      listAdapterWithRecyclerView.notifyDataSetChanged();
    }
  }

  /**
   * Returns the text in the `ListView` at the position of {@link #SelectionIndex(int)}.
   */
  @SimpleProperty(description = "The text value of the most recently selected item in the ListView.",
      category = PropertyCategory.BEHAVIOR)
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
    if (!items.isEmpty()) {
      for (int i = 0; i < items.size(); ++i) {
        Object item = items.get(i);
        if (item instanceof YailDictionary) {
          if (((YailDictionary) item).containsKey(Component.LISTVIEW_KEY_MAIN_TEXT)) {
            if (((YailDictionary) item).get(Component.LISTVIEW_KEY_MAIN_TEXT).toString() == value) {
              selectionIndex = i + 1;
              selectionDetailText = ElementsUtil.toStringEmptyIfNull(((YailDictionary) item)
                                                                         .get(Component.LISTVIEW_KEY_DESCRIPTION));
              break;
            }
            // Not found
            selectionIndex = 0;
            selectionDetailText = "Not Found";
          } else {
            if (item.toString().equals(value)) {
              selectionIndex = i + 1;
              break;
            }
            selectionIndex = 0;
          }
        } else {
          if (item.toString().equals(value)) {
            selectionIndex = i + 1;
            break;
          }
          selectionIndex = 0;
        }
      }
      SelectionIndex(selectionIndex);
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
   * Simple event to be raised after the an element has been chosen in the list.
   * The selected element is available in the {@link #Selection(String)} property.
   */
  @SimpleEvent(description = "Simple event to be raised after the an element has been chosen in the"
                                 + " list. The selected element is available in the Selection property.")
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
  @SimpleProperty(description = "The color of the listview background.",
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
    setAdapterData();
  }

  /**
   * Returns the listview's element color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @return background color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(description = "The color of the listview background.",
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int ElementColor() {
    return elementColor;
  }

  /**
   * The color of the `ListView` element.
   *
   * @param argb element color in the format 0xAARRGGBB, which
   *             includes alpha, red, green, and blue components
   * @internaldoc Specifies the ListView's element color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
  @SimpleProperty
  public void ElementColor(int argb) {
    elementColor = argb;
    setAdapterData();
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
  @SimpleProperty(description = "The color of the item when it is selected.",
      category = PropertyCategory.APPEARANCE)
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
  @SimpleProperty(description = "The color of the main text of ListView elements.",
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
  @SimpleProperty(description = "The color of the detail text of ListView elements. ",
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
   * This property is deprecated, use FontSize instead.
   */
  @Deprecated
  @SimpleProperty
  public int TextSize() {
    return Math.round(fontSizeMain);
  }

  /**
   * This property is deprecated, use FontSize instead.
   */
  @Deprecated
  @SimpleProperty
  public void TextSize(int textSize) {
    if (textSize > 1000) {
      textSize = 999;
    }
    FontSize(Float.valueOf(textSize));
  }

  /**
   * Returns the font size of the main text.
   *
   * @return text size as an float
   */
  @SimpleProperty(description = "The font size of the main text.",
      category = PropertyCategory.APPEARANCE)
  public float FontSize() {
    return fontSizeMain;
  }

  /**
   * Specifies the font size of the element's main text.
   *
   * @param float value for font size
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
           defaultValue = DEFAULT_TEXT_SIZE + "")
  @SimpleProperty
  public void FontSize(float fontSize) {
    if (fontSize > 1000 || fontSize < 1)
      fontSizeMain = 999;
    else
      fontSizeMain = fontSize;
    setAdapterData();
  }

   /**
   * Returns the font size of the detail text.
   *
   * @return text size as an float
   */
  @SimpleProperty(description = "The font size of the detail text.",
      category = PropertyCategory.APPEARANCE)
  public float FontSizeDetail() {
    return fontSizeDetail;
  }

  /**
   * Specifies the font size of the element's detail text.
   *
   * @param float value for font size
   */  
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
   * @return one of {@link Component#TYPEFACE_DEFAULT},
   * {@link Component#TYPEFACE_SERIF},
   * {@link Component#TYPEFACE_SANSSERIF} or
   * {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public String FontTypeface() {
    return fontTypeface;
  }

  /**
   * Specifies the label's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @param typeface one of {@link Component#TYPEFACE_DEFAULT},
   *                 {@link Component#TYPEFACE_SERIF},
   *                 {@link Component#TYPEFACE_SANSSERIF} or
   *                 {@link Component#TYPEFACE_MONOSPACE}
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE,
      defaultValue = Component.TYPEFACE_DEFAULT + "")
  @SimpleProperty(userVisible = false)
  public void FontTypeface(String typeface) {
    fontTypeface = typeface;
    setAdapterData();
  }

  /**
   * Returns the label's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @return one of {@link Component#TYPEFACE_DEFAULT},
   * {@link Component#TYPEFACE_SERIF},
   * {@link Component#TYPEFACE_SANSSERIF} or
   * {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public String FontTypefaceDetail() {
    return fontTypeDetail;
  }

  /**
   * Specifies the label's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @param typeface one of {@link Component#TYPEFACE_DEFAULT},
   *                 {@link Component#TYPEFACE_SERIF},
   *                 {@link Component#TYPEFACE_SANSSERIF} or
   *                 {@link Component#TYPEFACE_MONOSPACE}
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE,
      defaultValue = Component.TYPEFACE_DEFAULT + "")
  @SimpleProperty(userVisible = false)
  public void FontTypefaceDetail(String typeface) {
    fontTypeDetail = typeface;
    setAdapterData();
  }

  /**
   * Returns the image width of ListView layouts containing images
   *
   * @return width of image
   */
  @SimpleProperty(description = "Image width of ListView elements.",
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
  @SimpleProperty(description = "Image height of ListView elements.",
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
   * Returns type of layout selected to display.
   *
   * @return layout as integer value
   */
  @SimpleProperty(description = "Selecting the text and image layout in the ListView element.",
      category = PropertyCategory.APPEARANCE)
  public int ListViewLayout() {
    return layout;
  }

  /**
   * Specifies type of layout for ListView row.
   *
   * @param value integer value to determine type of ListView layout
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_LAYOUT,
      defaultValue = ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT + "")
  @SimpleProperty
  public void ListViewLayout(@Options(LayoutType.class) int value) {
    layout = value;
    setAdapterData();
  }

  /**
   * Returns true or false depending on the enabled state of multiselect.
   *
   * @return true or false (is multiselect)
   * @suppressdoc
   */
  // See https://github.com/mit-cml/appinventor-sources/pull/3235#issuecomment-2435573318
//  @SimpleProperty(description = "A function that allows you to select multiple elements. "
//                                    + "True - function enabled, false - disabled.",
//      category = PropertyCategory.BEHAVIOR)
  public boolean MultiSelect() {
    return multiSelect;
  }

  /**
   * Sets the multiselect function. `true`{:.logic.block} will enable the function,
   * `false`{:.logic.block} will disable.
   *
   * @param multiSelect sets the function according to this input
   */
  // See https://github.com/mit-cml/appinventor-sources/pull/3235#issuecomment-2435573318
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
//      defaultValue = "False")
//  @SimpleProperty
  public void MultiSelect(boolean multi) {
    if (selectionIndex > 0) {
      listAdapterWithRecyclerView.clearSelections();
      listAdapterWithRecyclerView.notifyDataSetChanged();
    }
    this.multiSelect = multi;
  }

  /**
   * Returns the type of layout's orientation.
   *
   * @return one of {@link ComponentConstants#LAYOUT_ORIENTATION_VERTICAL},
   * {@link ComponentConstants#LAYOUT_ORIENTATION_HORIZONTAL},
   */
  @SimpleProperty(description = "Specifies the layout's orientation (vertical, horizontal).",
      category = PropertyCategory.APPEARANCE)
  public int Orientation() {
    return orientation;
  }

  /**
   * Specifies the layout's orientation. This may be: `Vertical`, which displays elements
   * in rows one after the other; or `Horizontal`, which displays one element at a time and
   * allows the user to swipe left or right to brows the elements.
   *
   * @param orientation one of {@link ComponentConstants#LAYOUT_ORIENTATION_VERTICAL},
   *                    {@link ComponentConstants#LAYOUT_ORIENTATION_HORIZONTAL},
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_RECYCLERVIEW_ORIENTATION,
      defaultValue = ComponentConstants.LAYOUT_ORIENTATION_VERTICAL + "")
  @SimpleProperty
  public void Orientation(@Options(ListOrientation.class) int orientation) {
    this.orientation = orientation;
    if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
      layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    } else { // if (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) {
      layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    }
    recyclerView.requestLayout();
  }

  /**
   * Returns the data to be displayed in the ListView as a JsonString. Designer only property.
   *
   * @return string form of the array of JsonObject
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      userVisible = false)
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
  @SimpleProperty(userVisible = false)
  public void ListData(String propertyValue) {
    this.propertyValue = propertyValue;
    if (propertyValue != null && !propertyValue.isEmpty() && !propertyValue.equals("[]")) {
      try {
        JSONArray arr = new JSONArray(propertyValue);
        // Note that ListData is set from the designer only. If we change this, then the logic here
        // will need to be updated to handle the case where ListData is set from the blocks and
        // happens to clear the contents.
        items.clear();
        // Convert the JSON data into a list of Dictionaries
        for (int i = 0; i < arr.length(); ++i) {
          JSONObject jsonItem = arr.getJSONObject(i);
          YailDictionary yailItem = new YailDictionary();
          if (jsonItem.has(Component.LISTVIEW_KEY_MAIN_TEXT)) {
            yailItem.put(Component.LISTVIEW_KEY_MAIN_TEXT, jsonItem.getString(Component.LISTVIEW_KEY_MAIN_TEXT));
            yailItem.put(Component.LISTVIEW_KEY_DESCRIPTION, jsonItem.has(Component.LISTVIEW_KEY_DESCRIPTION) ? jsonItem.getString(Component.LISTVIEW_KEY_DESCRIPTION) : "");
            yailItem.put(Component.LISTVIEW_KEY_IMAGE, jsonItem.has(Component.LISTVIEW_KEY_IMAGE) ? jsonItem
                                                                                                        .getString(Component.LISTVIEW_KEY_IMAGE) : "");
            items.add(yailItem);
          }
        }
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Malformed JSON in ListView.ListData", e);
        container.$form().dispatchErrorOccurredEvent(this, "ListView.ListData", ErrorMessages.ERROR_DEFAULT, e.getMessage());
      }
      updateAdapterData();
      listAdapterWithRecyclerView.notifyDataSetChanged();
    }
  }

  /**
   * Creates a
   *
   * @param mainText   Primary text of the entry. Should be unique if possible.
   * @param detailText Additional descriptive text.
   * @param imageName  File name of an image that has been uploaded to media.
   */
  @SimpleFunction(description = "Create a ListView entry. MainText is required. DetailText and ImageName are optional.")
  public YailDictionary CreateElement(final String mainText, final String detailText, final String imageName) {
    YailDictionary dictItem = new YailDictionary();
    dictItem.put(Component.LISTVIEW_KEY_MAIN_TEXT, mainText);
    dictItem.put(Component.LISTVIEW_KEY_DESCRIPTION, detailText);
    dictItem.put(Component.LISTVIEW_KEY_IMAGE, imageName);
    return dictItem;
  }

  @SimpleFunction(description = "Get the Main Text of a ListView element.")
  public String GetMainText(final YailDictionary listElement) {
    return listElement.get(Component.LISTVIEW_KEY_MAIN_TEXT).toString();
  }

  @SimpleFunction(description = "Get the Detail Text of a ListView element.")
  public String GetDetailText(final YailDictionary listElement) {
    return listElement.get(Component.LISTVIEW_KEY_DESCRIPTION).toString();
  }

  @SimpleFunction(description = "Get the filename of the image of a ListView element that has been uploaded to Media.")
  public String GetImageName(final YailDictionary listElement) {
    return listElement.get(Component.LISTVIEW_KEY_IMAGE).toString();
  }

  /**
   * Returns the listview's divider color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @return divider color in the format 0xAARRGGBB, which includes
   * alpha, red, green, and blue components
   */
  @SimpleProperty(description = "The color of the list view divider.",
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int DividerColor() {
    return dividerColor;
  }

  /**
   * The color of the `ListView` divider.
   *
   * @param argb divider color in the format 0xAARRGGBB, which
   *             includes alpha, red, green, and blue components
   * @internaldoc Specifies the ListView's divider color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void DividerColor(int argb) {
    dividerColor = argb;
    dividerPaint = new Paint();
    dividerPaint.setColor(argb);
    setDivider();
  }

  /**
   * Returns the divider thickness of list view
   *
   * @return thickness of divider
   */
  @SimpleProperty(description = "The thickness of the element divider in the list view. "
                                    + "If the thickness is 0, the divider is not visible.",
      category = PropertyCategory.APPEARANCE)
  public int DividerThickness() {
    return dividerSize;
  }

  /**
   * Specifies the divider thickness of list view.
   * If the thickness is 0, the divider is not visible.
   *
   * @param size sets the thickness of divider in the list view
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_DIVIDER_SIZE + "")
  @SimpleProperty
  public void DividerThickness(int size) {
    this.dividerSize = size;
    setDivider();
  }

  /**
   * Returns the margins width of list view element
   *
   * @return width of margins
   */
  @SimpleProperty(description = "The margins width of the list view element. "
                                    + "If margins width > 0, then the divider is not displayed.",
      category = PropertyCategory.APPEARANCE)
  public int ElementMarginsWidth() {
    return margins;
  }

  /**
   * Specifies the width of the margins of a list view element.
   * If margins width > 0, then the divider is not displayed.
   *
   * @param width sets the width of the margins in the list view element
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_MARGINS_SIZE + "")
  @SimpleProperty
  public void ElementMarginsWidth(int width) {
    this.margins = width;
    setDivider();
  }

  /**
   * Returns the corner radius of the list view element.
   *
   * @return corner radius
   */
  @SimpleProperty(description = "The radius of the rounded corners of a list view element.",
      category = PropertyCategory.APPEARANCE)
  public int ElementCornerRadius() {
    return radius;
  }

  /**
   * Specifies the corner radius of the list view element.
   *
   * @param radius sets the radius of the corners in the list view element
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_RADIUS + "")
  @SimpleProperty
  public void ElementCornerRadius(int radius) {
    this.radius = radius;
    setAdapterData();
  }

  /**
   * Returns true or false depending on the enabled state of bounce effect.
   *
   * @return true or false (is bounce effect)
   * @suppressdoc
   */
  @SimpleProperty(description = "The effect of bounce from the edge after scrolling the list to the end. "
                                    + " True will enable the bounce effect, false will disable it.",
      category = PropertyCategory.BEHAVIOR)
  public boolean BounceEdgeEffect() {
    return bounceEffect;
  }

  /**
   * Sets the bounce effect function. `true`{:.logic.block} will enable the function,
   * `false`{:.logic.block} will disable.
   *
   * @param bounce sets the function according to this input
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void BounceEdgeEffect(boolean bounce) {
    if (bounce) {
      recyclerView.setEdgeEffectFactory(bounceEdgeEffectFactory);
    } else {
      recyclerView.setEdgeEffectFactory(edgeEffectFactory);
    }
    this.bounceEffect = bounce;
  }

  /**
   * Removes Item from list at a given index
   */
  @SimpleFunction(description = "Removes Item from list at a given index.")
  public void RemoveItemAtIndex(int index) {
    if (index < 1 || index > items.size()) {
      container.$form().dispatchErrorOccurredEvent(this, "RemoveItemAtIndex",
          ErrorMessages.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, index);
      return;
    }
    items.remove(index - 1);
    updateAdapterData();
    listAdapterWithRecyclerView.notifyItemRemoved(index - 1);
  }

  /**
   * Add new Item to list
   */
  @SimpleFunction(description = "Add new Item to list at the end.")
  public void AddItem(String mainText, String detailText, String imageName) {
    if (!items.isEmpty()) {
      Object o = items.get(0);
      if (o instanceof YailDictionary) {
        if (((YailDictionary) o).containsKey(Component.LISTVIEW_KEY_MAIN_TEXT)) {
          items.add(CreateElement(mainText, detailText, imageName));
        } else {
          items.add(mainText);
        }
      } else {
        items.add(mainText);
      }
    } else {
      if (layout == Component.LISTVIEW_LAYOUT_SINGLE_TEXT) {
        items.add(mainText);
      } else {
        items.add(CreateElement(mainText, detailText, imageName));
      }
    }
    updateAdapterData();
    listAdapterWithRecyclerView.notifyItemChanged(listAdapterWithRecyclerView.getItemCount() - 1);
  }

  /**
   * Add new Items to list
   */
  @SimpleFunction(description = "Add new Items to list at the end.")
  public void AddItems(List<Object> itemsList) {
    if (!itemsList.isEmpty()) {
      int positionStart = items.size();
      int itemCount = itemsList.size();
      items.addAll(itemsList);
      updateAdapterData();
      listAdapterWithRecyclerView.notifyItemRangeChanged(positionStart, itemCount);
    }
  }

  /**
   * Add new Item to list at a given index
   */
  @SimpleFunction(description = "Add new Item to list at a given index.")
  public void AddItemAtIndex(int index, String mainText, String detailText, String imageName) {
    if (index < 1 || index > items.size() + 1) {
      container.$form().dispatchErrorOccurredEvent(this, "AddItemAtIndex",
          ErrorMessages.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, index);
      return;
    }
    if (!items.isEmpty()) {
      Object o = items.get(0);
      if (o instanceof YailDictionary) {
        if (((YailDictionary) o).containsKey(Component.LISTVIEW_KEY_MAIN_TEXT)) {
          items.add(index - 1, CreateElement(mainText, detailText, imageName));
        } else {
          items.add(index - 1, mainText);
        }
      } else {
        items.add(index - 1, mainText);
      }
    } else {
      if (layout == Component.LISTVIEW_LAYOUT_SINGLE_TEXT) {
        items.add(index - 1, mainText);
      } else {
        items.add(index - 1, CreateElement(mainText, detailText, imageName));
      }
    }
    updateAdapterData();
    listAdapterWithRecyclerView.notifyItemInserted(index - 1);
  }

  /**
   * Add new Items to list at specific index
   */
  @SimpleFunction(description = "Add new Items to list at specific index.")
  public void AddItemsAtIndex(int index, YailList itemsList) {
    if (index < 1 || index > items.size() + 1) {
      container.$form().dispatchErrorOccurredEvent(this, "AddItemsAtIndex",
          ErrorMessages.ERROR_LISTVIEW_INDEX_OUT_OF_BOUNDS, index);
      return;
    }
    if (!itemsList.isEmpty()) {
      int positionStart = index - 1;
      int itemCount = itemsList.size();
      items.addAll(positionStart, itemsList);
      updateAdapterData();
      listAdapterWithRecyclerView.notifyItemRangeChanged(positionStart, itemCount);
    }
  }

  /**
   * Create a new adapter and apply visual changes, load data if it exists.
   */
  public void setAdapterData() {
    switch (layout) {
      case LISTVIEW_LAYOUT_SINGLE_TEXT:
        setListAdapter(new ListViewSingleTextAdapter(container, items,
            textColor, fontSizeMain, fontTypeface, detailTextColor, fontSizeDetail, fontTypeDetail,
            elementColor, selectionColor, radius, imageWidth, imageHeight));
        break;
      case LISTVIEW_LAYOUT_TWO_TEXT:
        setListAdapter(new ListViewTwoTextAdapter(container, items,
            textColor, fontSizeMain, fontTypeface, detailTextColor, fontSizeDetail, fontTypeDetail,
            elementColor, selectionColor, radius, imageWidth, imageHeight));
        break;
      case LISTVIEW_LAYOUT_TWO_TEXT_LINEAR:
        setListAdapter(new ListViewTwoTextLinearAdapter(container, items,
            textColor, fontSizeMain, fontTypeface, detailTextColor, fontSizeDetail, fontTypeDetail,
            elementColor, selectionColor, radius, imageWidth, imageHeight));
        break;
      case LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT:
        setListAdapter(new ListViewImageSingleTextAdapter(container, items,
            textColor, fontSizeMain, fontTypeface, detailTextColor, fontSizeDetail, fontTypeDetail,
            elementColor, selectionColor, radius, imageWidth, imageHeight));
        break;
      case LISTVIEW_LAYOUT_IMAGE_TWO_TEXT:
        setListAdapter(new ListViewImageTwoTextVerticalAdapter(container, items,
            textColor, fontSizeMain, fontTypeface, detailTextColor, fontSizeDetail, fontTypeDetail,
            elementColor, selectionColor, radius, imageWidth, imageHeight));
        break;
      case LISTVIEW_LAYOUT_IMAGE_TOP_TWO_TEXT:
        setListAdapter(new ListViewImageTopTwoTextAdapter(container, items,
          textColor, fontSizeMain, fontTypeface, detailTextColor, fontSizeDetail, fontTypeDetail,
          elementColor, selectionColor, radius, imageWidth, imageHeight));
        break;
    }    
  }

  /**
   * Deselect the item and update the data in adapter.
   */
  public void updateAdapterData() {
    SelectionIndex(0);
    listAdapterWithRecyclerView.updateData(items);
  }

  /**
   * Sets new dividers or margins in RecyclerView
   */
  private void setDivider() {
    DividerItemDecoration dividerDecoration = new DividerItemDecoration();
    dividerDecoration.removeLayoutChangeListener();
    for (int i = 0; i < recyclerView.getItemDecorationCount(); i++) {
      RecyclerView.ItemDecoration decoration = recyclerView.getItemDecorationAt(i);
      if (decoration instanceof DividerItemDecoration) {
        recyclerView.removeItemDecorationAt(i);
        break;
      }
    }
    recyclerView.addItemDecoration(dividerDecoration);
  }

  public void setListAdapter(ListAdapterWithRecyclerView adapter) {
    listAdapterWithRecyclerView = adapter;
    listAdapterWithRecyclerView.setOnItemClickListener(new ListAdapterWithRecyclerView.ClickListener() {
      @Override
      public void onItemClick(int position, View v) {
        SelectionIndex(position + 1);
        AfterPicking();
      }
    });
    recyclerView.setAdapter(listAdapterWithRecyclerView);
  }
  /**
   * A class that creates dividers between elements or margins, depending on the options selected.
   */
  private class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private int recyclerViewWidth = 0;
    private View.OnLayoutChangeListener layoutChangeListener;
    private RecyclerView parent;

    public DividerItemDecoration() {}

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
      // If margins are set, dividers will not be created.
      if (margins == 0) {
        ViewGroup.LayoutParams layoutParams;
        int childCount = parent.getChildCount();
        if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
          for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            layoutParams = child.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            child.setLayoutParams(layoutParams);
            int position = parent.getChildAdapterPosition(child);
            if (position != RecyclerView.NO_POSITION) {
              int left = child.getRight();
              int right = left + dividerSize;
              int top = child.getTop();
              int bottom = child.getBottom();
              canvas.drawRect(left, top, right, bottom, dividerPaint);
            }
          }
        } else {
          int width = parent.getWidth();
          for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            layoutParams = child.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            child.setLayoutParams(layoutParams);
            int position = parent.getChildAdapterPosition(child);
            if (position != RecyclerView.NO_POSITION) {
              int top = child.getBottom();
              int bottom = top + dividerSize;
              canvas.drawRect(0, top, width, bottom, dividerPaint);
            }
          }
        }
      }
    }

    @Override
    public void getItemOffsets(
        Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
      this.parent = parent;
      ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
      int position = parent.getChildAdapterPosition(view);
      if (margins == 0) {
        if (position != RecyclerView.NO_POSITION
            && position < parent.getAdapter().getItemCount() - 1) {
          if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
            outRect.set(0, 0, dividerSize, 0);
          } else {
            outRect.set(0, 0, 0, dividerSize);
          }
        } else {
          outRect.setEmpty();
        }
      } else {
        if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
          if (layoutChangeListener == null) {
            layoutChangeListener =
                new View.OnLayoutChangeListener() {
                  @Override
                  public void onLayoutChange(View v, int left, int top, int right,
                      int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (recyclerViewWidth != parent.getWidth()) {
                      recyclerViewWidth = parent.getWidth();
                      for (int i = 0; i < parent.getChildCount(); i++) {
                        View child = parent.getChildAt(i);
                        ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
                        childLayoutParams.width = recyclerViewWidth - (2 * margins);
                        child.setLayoutParams(childLayoutParams);
                      }
                      parent.invalidate();
                    }
                  }
                };
            parent.addOnLayoutChangeListener(layoutChangeListener);
          }
          recyclerViewWidth = parent.getWidth();
          layoutParams.width = recyclerViewWidth - (2 * margins);
          view.setLayoutParams(layoutParams);
          if (position == 0) {
            outRect.set(margins, margins, margins, margins);
          } else {
            outRect.set(0, margins, margins, margins);
          }
        } else {
          layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
          if (position == 0) {
            outRect.set(margins, margins, margins, margins);
          } else {
            outRect.set(margins, 0, margins, margins);
          }
        }
        view.setLayoutParams(layoutParams);
      }
    }

    public void removeLayoutChangeListener() {
      if (layoutChangeListener != null) {
        parent.removeOnLayoutChangeListener(layoutChangeListener);
      }
    }
  }

  @Deprecated
  @SimpleFunction(description = "Reload the ListView to reflect any changes in the data.")
  public void Refresh() {
    setAdapterData();
  }
}
