/*

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import android.widget.EditText;
import android.view.View;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.RECYCLERVIEW_COMPONENT_VERSION,
    description = "<p>This is a visible component that displays a list of text elements." +
        " <br> The list can be set using the ElementsFromString property" +
        " or using the Elements block in the blocks editor. </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/recyclerView.png")
@SimpleObject
public final class RecyclerView extends AndroidViewComponent {

  protected final ComponentContainer container;
  private EditText editText;

  public RecyclerView(ComponentContainer container) {
    super(container);
    this.container = container;

    editText = new EditText(container.$context());
    
    container.$add(this);

};

  @Override
  public View getView() {
    return editText;
  }

}

*/





package com.google.appinventor.components.runtime;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
//
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;


import android.widget.LinearLayout.LayoutParams;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;




@DesignerComponent(version = YaVersion.RECYCLERVIEW_COMPONENT_VERSION,
    description = "<p>This is a visible component that displays a list of text elements." +
        " <br> The list can be set using the ElementsFromString property" +
        " or using the Elements block in the blocks editor. </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/recyclerView.png")
@SimpleObject
@UsesLibraries(libraries ="RecyclerView.jar")
public final class RecyclerView extends AndroidViewComponent{

private EditText txtSearchBox;
private ArrayAdapter<String> adapter;
protected final ComponentContainer container;
private final LinearLayout linearLayout;
    
    private static final String LOG_TAG = "RecyclerView";
    
    private android.support.v7.widget.RecyclerView recyclerView;
    private Context ctx;
    
    private int selectionIndex;
    private String selection;
    private boolean showFilter = false;
    private static final boolean DEFAULT_ENABLED = false;

    private YailList input;

    ListAdapterWithRecyclerView listAdapterWithRecyclerView;

    private int backgroundColor;
    private static final int DEFAULT_BACKGROUND_COLOR = Component.COLOR_BLACK;

    // The text color of the ListView's items.  All items have the same text color
    private int textColor;
    private static final int DEFAULT_TEXT_COLOR = Component.COLOR_WHITE;

    private int selectionColor;
    private static final int DEFAULT_SELECTION_COLOR = Component.COLOR_LTGRAY;

    private int textSize;
    private static final int DEFAULT_TEXT_SIZE = 22;

    /**
   * Creates a new RecyclerView component.
   * @param container  container that the component will be placed in
   */
  public RecyclerView(ComponentContainer container) {
    super(container);
    this.container = container;

  //txtSearchBox = new EditText(container.$context());

   linearLayout = new LinearLayout(container.$context());
   linearLayout.setOrientation(LinearLayout.VERTICAL);


    ctx=container.$context();

    input = YailList.makeEmptyList();
    
  //   initialize selectionIndex which also sets selection
   
    SelectionIndex(0);

    recyclerView = new android.support.v7.widget.RecyclerView(container.$context());
    
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
          //listAdapterWithRecyclerView.getFilter().filter(cs);
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
  //  SelectionColor(DEFAULT_SELECTION_COLOR);

    //textColor = DEFAULT_TEXT_COLOR;
   // TextColor(textColor);
   // textSize = DEFAULT_TEXT_SIZE;
    //TextSize(textSize);
    ElementsFromString("");

    linearLayout.addView(txtSearchBox);
    linearLayout.addView(recyclerView);
    linearLayout.requestLayout();
    container.$add(this);



};

@Override
  public View getView() {
    return linearLayout;
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
   * Sets true or false to determine whether the search filter box is displayed in the ListView
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

  @SimpleProperty(description="List of text elements to show in the ListView.  This will" +
                "signal an error if the elements are not text strings.",
      category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList input) {
    input = ElementsUtil.elements(input, "RecyclerView");
 
//doubt setAdapterData() why called here
    setAdapter(input);
  }

  /**
   * Elements property getter method
   *
   * @return a YailList representing the list of strings to be picked from
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public YailList Elements() {
    return input;
  }


  /**
   * Specifies the text elements of the ListView.
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
  */ 
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description="The TextView elements specified as a string with the " +
      "items separated by commas " +
      "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the " +
      "list.",  category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String itemstring) {
    input = ElementsUtil.elementsFromString(itemstring);
    String[] str = input.toStringArray();
    
    setAdapterr(str);

    }


  /**
   * Sets the items of the ListView through an adapter
   
  */
 public void setAdapterr(String[] str){


    int size =(str.length)/2;
    String[] first=new String[size];
    String[] second=new String[size];

    for (int i = 0; i < str.length; i++) {
      int x=i/2;
      if(i%2==0){first[x]=str[i];}
      else{second[x]=str[i];}
    }

    listAdapterWithRecyclerView =new ListAdapterWithRecyclerView(container.$context(),first,second);

    //ViewGroup.LayoutParams paramms=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
    //recyclerView.setLayoutParams(paramms);

    LinearLayoutManager layoutManager=new LinearLayoutManager(ctx);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(listAdapterWithRecyclerView);

  //  container.$add(this);  
 

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
  @SimpleProperty(description="Specifies the position of the selected item in the ListView. " +
      "This could be used to retrieve" +
      "the text at the chosen position. If an attempt is made to set this to a " +
      "number less than 1 or greater than the number of items in the ListView, SelectionIndex " +
      "will be set to 0, and Selection will be set to the empty text."
      ,
      category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int index){
    selectionIndex = ElementsUtil.selectionIndex(index, input);
    // Now, we need to change Selection to correspond to SelectionIndex.
    selection = ElementsUtil.setSelectionFromIndex(index, input);
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
    selectionIndex = ElementsUtil.setSelectedIndexFromValue(value, input);
  }


/**
   * Assigns a value to the backgroundColor
   * @param color  an alpha-red-green-blue integer for a color
 */  

  public void setBackgroundColor(int color) {
      backgroundColor = color;
      recyclerView.setBackgroundColor(backgroundColor);
      linearLayout.setBackgroundColor(backgroundColor);
      // Keeps background color behind list elements correct when scrolling through listView
      //recyclerView.setCacheColorHint(backgroundColor);
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
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
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
   
  @SimpleProperty(description = "The color of the item when it is selected.")
  public int SelectionColor() {
    return selectionColor;
  }

  /**
   * Specifies the ListView's selection color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   * Is not supported on Icecream Sandwich or earlier
   *
   * @param argb selection color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
  @SimpleProperty
  public void SelectionColor(int argb) {
    selectionColor = argb;
    recyclerView.setSelector(new GradientDrawable(
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
   
  @SimpleProperty(
      description = "The text color of the listview items.",
      category = PropertyCategory.APPEARANCE)
  public int TextColor() {
    return textColor;
  }

  /**
   * Specifies the ListView item's text color as an alpha-red-green-blue
   * integer, i.e., {@code 0xAARRGGBB}.  An alpha of {@code 00}
   * indicates fully transparent and {@code FF} means opaque.
   *
   * @param argb background color in the format 0xAARRGGBB, which
   * includes alpha, red, green, and blue components
   
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void TextColor(int argb) {
      textColor = argb;
      setAdapterData();
  }

  /**
   * Returns the listview's text font Size
   *
   * @return text size as an float
   
  @SimpleProperty(
      description = "The text size of the listview items.",
      category = PropertyCategory.APPEARANCE)
  public int TextSize() {
    return textSize;
  }

  /**
   * Specifies the ListView item's text font size
   *
   * @param integer value for font size
   
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_TEXT_SIZE + "")
  @SimpleProperty
  public void TextSize(int fontSize) {
      if(fontSize>1000)
        textSize = 999;
      else
        textSize = fontSize;
      setAdapter();
  }




/*  public void setAdapterData(){
    adapter = new ArrayAdapter<Spannable>(container.$context(), android.R.layout.simple_list_item_1,
        itemsToColoredText());
    view.setAdapter(adapter);

    adapterCopy = new ArrayAdapter<Spannable>(container.$context(), android.R.layout.simple_list_item_1);
    for (int i = 0; i < adapter.getCount(); ++i) {
      adapterCopy.insert(adapter.getItem(i), i);
    }
  }

*/
}
