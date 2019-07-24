package com.google.appinventor.components.runtime;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

//import android.text.Spannable;

//import android.text.SpannableString;
//import android.text.Html;
//import android.text.style.AbsoluteSizeSpan;
//import android.text.style.ForegroundColorSpan;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;

import com.google.appinventor.components.runtime.util.MediaUtil;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.Manifest;
import java.io.IOException;
import java.util.*;
//
import org.json.JSONArray;
import org.json.JSONObject;
//import com.google.gwt.json.client.JSONParser;
//import com.google.gwt.json.client.JSONValue;
//import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidListViewAddDataPropertyEditor;


/**
 * RecyclerView Component. Non-Visible component to create a RecyclerView in the Screen from a series of
 * elements added from a comma separated set of text elements. It is similar to the ListPicker
 * component but this one is placed on screen instead of opening a new Activity.
 * TOFO(hal): Think about generalizing this to include more than text/
 * @author halabelson@google.com (Hal Abelson)
 * @author osmidy@mit.edu (Olivier Midy)
 */

@DesignerComponent(version = YaVersion.RECYCLERVIEW_COMPONENT_VERSION,
    description = "<p>This is a visible component that displays a list of text elements." +
        " <br> The list can be set using the ElementsFromString property" +
        " or using the Elements block in the blocks editor. </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/recyclerView.png")
@SimpleObject
@UsesLibraries(libraries ="RecyclerView.jar, CardView.jar, CardView.aar")
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
    "android.permission.READ_EXTERNAL_STORAGE")
public final class RecyclerView extends AndroidViewComponent{

    private static final String LOG_TAG = "RecyclerView";
    
    private EditText txtSearchBox;
    protected final ComponentContainer container;
    private final LinearLayout linearLayout;
    private android.support.v7.widget.RecyclerView recyclerView;
    private Context ctx;
    private int selectionIndex;
    private String selection;
    private boolean showFilter = false;
    private static final boolean DEFAULT_ENABLED = false;

    private YailList inputFirst,inputSecond,inputImage;
    private String[] strFirst,strSecond,images; 
    private String picturePath = "";
    private ArrayList<String>imagePathList = new ArrayList<String>();

    private   ListAdapterWithRecyclerView listAdapterWithRecyclerView;

    private int backgroundColor;
    private static final int DEFAULT_BACKGROUND_COLOR = Component.COLOR_BLACK;

    // The text color of the ListView's items.  All items have the same text color
    private int textColor;
    private static final int DEFAULT_TEXT_COLOR = Component.COLOR_WHITE;

    private int selectionColor;
    private static final int DEFAULT_SELECTION_COLOR = Component.COLOR_LTGRAY;

    private int textSize;
    private static final int DEFAULT_TEXT_SIZE = 22;

    //
    private int layout;
    private String propertyValue;
    private ArrayList<JSONObject> currentItems;


    /**
   * Creates a new RecyclerView component.
   * @param container  container that the component will be placed in
   */
  public RecyclerView(ComponentContainer container) {
    super(container);
    this.container = container;

    linearLayout = new LinearLayout(container.$context());
    linearLayout.setOrientation(LinearLayout.VERTICAL);

   //
    currentItems = new ArrayList<>();

    ctx=container.$context();
    inputFirst = YailList.makeEmptyList();
    inputSecond = YailList.makeEmptyList();
    inputImage = YailList.makeEmptyList();
    //imagePathList = new ArrayList<String>();
    
  //   initialize selectionIndex which also sets selection
   
    SelectionIndex(0);

    recyclerView = new android.support.v7.widget.RecyclerView(container.$context());
//    android.support.v7.widget.RecyclerView.LayoutParams paramms=new android.support.v7.widget.RecyclerView.LayoutParams(android.support.v7.widget.RecyclerView.LayoutParams.MATCH_PARENT,android.support.v7.widget.RecyclerView.LayoutParams.MATCH_PARENT);
//    recyclerView.setLayoutParams(paramms);

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
  
    textColor = DEFAULT_TEXT_COLOR;
    textSize = DEFAULT_TEXT_SIZE;
    ElementsFromStringFirst("");
    ElementsFromStringSecond("");

 //   ElementImages("");    

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
*/
  @SimpleProperty(description="List of text elements to show in the ListView.  This will" +
                "signal an error if the elements are not text strings.",
      category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList input) {
    input = ElementsUtil.elements(input, "RecyclerView");
   setAdapterr();
  }

  /**
   * Elements property getter method
   *
   * @return a YailList representing the list of strings to be picked from
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public YailList Elements() {
    return inputFirst;
  }

  /**
   * Specifies the first text elements of the ListView.
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
  */ 
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description="The TextView elements specified as a string with the " +
      "items separated by commas " +
      "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the " +
      "list.",  category = PropertyCategory.BEHAVIOR)
  public void ElementsFromStringFirst(String itemstring) {
    strFirst = itemstring.split(",");

   setAdapterr();

    }


  /**
   * Specifies the second text elements of the ListView.
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
  */ 
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description="The TextView elements specified as a string with the " +
      "items separated by commas " +
      "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the " +
      "list.",  category = PropertyCategory.BEHAVIOR)
  public void ElementsFromStringSecond(String itemstring) {
    strFirst = itemstring.split(",");

   setAdapterr();
}


  /**
   * Returns the path of the image's picture.
   *
   * @return  the path of the image's picture
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Picture() {
    return picturePath;
  }
  /**
   * Specifies the path of the image's picture.
   *
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path of the image's picture
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty
  public void Picture(String path) {
   /* if (MediaUtil.isExternalFile(path) &&
        container.$form().isDeniedPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
      container.$form().askPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
          new PermissionResultHandler() {
            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
              if (granted) {
                Picture(path);
              } else {
                container.$form().dispatchPermissionDeniedEvent(RecyclerView.this, "Picture", permission);
              }
            }
          });
      return;
    }
*/
    picturePath+=path;
    imagePathList.add(path);


setAdapterr(); 
  }


  /**
   * Specifies the text elements of the ListView.
   * @param itemstring a string containing a comma-separated list of the strings to be picked from
   
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description="The Image paths specified as a string with the " +
      "items separated by commas " +
      "Each word before the comma will the path of image from media in the " +
      "list.",  category = PropertyCategory.BEHAVIOR)
  public void ElementImages(String itemstring) {
    inputImage = ElementsUtil.elementsFromString(itemstring);
    images = inputImage.toStringArray();    
        
    if(str!=null && str.length>0)     
    setAdapterr(str,images);

    }

  /**
   * Sets the items of the ListView through an adapter
   */
 public void setAdapterr(){

  /*  int size =(str.length)/2;
    String[] first=new String[size];
    String[] second=new String[size];
    
    //Drawable[] third=new Drawable[size];

    for (int i = 0; i < str.length; i++) {
      int x=i/2;
      if(i%2==1){second[x]=str[i];}
      else{first[x]=str[i];}
    }
*/


  //  if (strFirst != null && strSecond != null && strFirst.length == strSecond.length && imagePathList != null && imagePathList.size() > 0) {

    int size = currentItems.size();

    String[] first=new String[size];
    String[] second=new String[size];
    
    ArrayList<Drawable> third = new ArrayList<Drawable>();
    System.out.println(layout);
    if(layout==0){
    for(int i=0;i<size;i++){
      JSONObject object = currentItems.get(i);
      first[i]=object.has("Text1")?object.getString("Text1"):"";   
    } 
    }else if(layout==1){
    for(int i=0;i<size;i++){
      JSONObject object = currentItems.get(i);
      first[i]=object.has("Text1")?object.getString("Text1"):"";
      second[i]=object.has("Text2")?object.getString("Text2"):"";    
    } 
    }else if(layout==2){
    for(int i=0;i<size;i++){
      JSONObject object = currentItems.get(i);
      first[i]=object.has("Text1")?object.getString("Text1"):"";
      second[i]=object.has("Text2")?object.getString("Text2"):"";
    } 
    }else if(layout==3){
    for(int i=0;i<size;i++){
      JSONObject object = currentItems.get(i);
      first[i]=object.has("Text1")?object.getString("Text1"):"";
     
      String imagee=object.has("Image")?object.getString("Image"):"None";
        try {
        third.add(MediaUtil.getBitmapDrawable(container.$form(), imagee));
        } catch (IOException ioe) {
        Log.e("Image", "Unable to load " + imagee);
        third.add(null);
        }    
    } 
    }else if(layout==4){
    for(int i=0;i<size;i++){
      JSONObject object = currentItems.get(i);
      first[i]=object.has("Text1")?object.getString("Text1"):"";
      second[i]=object.has("Text2")?object.getString("Text2"):"";
     
      String imagee=object.has("Image")?object.getString("Image"):"None";
        try {
        third.add(MediaUtil.getBitmapDrawable(container.$form(), imagee));
        } catch (IOException ioe) {
        Log.e("Image", "Unable to load " + imagee);
        third.add(null);
        }    
    } 
    }
    
    listAdapterWithRecyclerView =new ListAdapterWithRecyclerView(container.$context(),first,second,third,textColor,textSize,layout);  
    LinearLayoutManager layoutManager=new LinearLayoutManager(ctx);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(listAdapterWithRecyclerView);
    }
   //}

   /*
public String[] itemsToColoredText(String[] str) {
    // TODO(hal): Generalize this so that different items could have different
    // colors and even fonts and sizes
    int size = str.length;
    int displayTextSize = textSize;
    String [] objects = new String[size];
    for (int i = 0; i < size; i++) {
      // Note that the ListPicker and otherPickers pickers convert Yail lists to string by calling
      // YailList.ToStringArray.
      // ListView however, does the string conversion via the adapter, so we must ensure
      // that the adapter uses YailListElementToSring
      //String itemString = YailList.YailListElementToString(items.get(i));
      String itemString = str[i];
      // Is there a more efficient way to do conversion to spannable strings that does not
      // need to allocate new objects?
      SpannableString chars = new SpannableString(itemString);
      chars.setSpan(new ForegroundColorSpan(textColor),0,chars.length(),0);
      if (!container.$form().getCompatibilityMode()) {
        displayTextSize = (int) (textSize * container.$form().deviceDensity());
      }
      chars.setSpan(new AbsoluteSizeSpan(displayTextSize),0,chars.length(),0);

/*      public String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
           return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
           return Html.fromHtml(html);
        }
}
   objects[i - 1] = Html.fromHtml(chars, Html.FROM_HTML_MODE_LEGACY).toString();
    }

    return objects;
  }

*/

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
    selectionIndex = ElementsUtil.selectionIndex(index, inputFirst);
    // Now, we need to change Selection to correspond to SelectionIndex.
    selection = ElementsUtil.setSelectionFromIndex(index, inputFirst);
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
    selectionIndex = ElementsUtil.setSelectedIndexFromValue(value, inputFirst);
  }


/**
   * Assigns a value to the backgroundColor
   * @param color  an alpha-red-green-blue integer for a color
 */  

  public void setBackgroundColor(int color) {
      backgroundColor = color;
      recyclerView.setBackgroundColor(backgroundColor);
      linearLayout.setBackgroundColor(backgroundColor);
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
   */
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
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void TextColor(int argb) {
      textColor = argb;
      setAdapterr();
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
   * Specifies the ListView item's text font size
   *
   * @param integer value for font size
  */ 
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue ="" + DEFAULT_TEXT_SIZE )
  @SimpleProperty
  public void TextSize(int fontSize) {
      if(fontSize>1000)
        textSize = 999;
      else
        textSize = fontSize;
      setAdapterr();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String AddData() {
    return propertyValue;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_ADD_DATA)
  @SimpleProperty(userVisible = false, category = PropertyCategory.BEHAVIOR)
  public void AddData(String propertyValue){
    this.propertyValue = propertyValue;
    if(propertyValue != null && propertyValue != "") {
      JSONArray arr = new JSONArray(propertyValue);
      for(int i = 0; i < arr.length(); ++i) {
        currentItems.add(i, arr.getJSONObject(i));
      }
    }

    setAdapterr();
  }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_LAYOUT,
      defaultValue = Component.LISTVIEW_LAYOUT_SINGLE_TEXT+"")
  @SimpleProperty(userVisible = false)
  public void ListViewLayout(int value) {
    layout = value;
    setAdapterr();
  }

  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
  public int ListViewLayout() {
    return layout;
  }
}


