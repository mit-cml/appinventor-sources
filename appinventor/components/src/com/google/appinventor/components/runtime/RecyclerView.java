


package com.google.appinventor.components.runtime;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;

/*
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;

*/

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

    private static final String LOG_TAG = "RecyclerView";
    
    private android.support.v7.widget.RecyclerView recyclerView;
    private Context ctx;
    private EditText txtSearchBox;
    protected final ComponentContainer container;
    private final LinearLayout linearLayout;
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

    ctx=container.$context();

    input = YailList.makeEmptyList();
   // First = YailList.makeEmptyList();
   // Second = YailList.makeEmptyList();
    
    // initialize selectionIndex which also sets selection
    SelectionIndex(0);

    recyclerView = new android.support.v7.widget.RecyclerView(container.$context());
    linearLayout = new LinearLayout(container.$context());
    linearLayout.setOrientation(LinearLayout.VERTICAL);

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
   // BackgroundColor(DEFAULT_BACKGROUND_COLOR);
   // SelectionColor(DEFAULT_SELECTION_COLOR);

    textColor = DEFAULT_TEXT_COLOR;
   // TextColor(textColor);
    textSize = DEFAULT_TEXT_SIZE;
   // TextSize(textSize);
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
   * Set a list of text elements to build a ListView
   * @param itemsList a YailList containing the strings to be added to the ListView
   */
//  @SimpleProperty(description="List of text elements to show in the ListView.  This will" +
//                "signal an error if the elements are not text strings.",
//      category = PropertyCategory.BEHAVIOR)
//  public void Elements(YailList Input) {
//    Input = ElementsUtil.elements(input, "RecyclerView");
 
//doubt setAdapterData() why called here
//    setAdapterData();
//  }

  /**
   * Elements property getter method
   *
   * @return a YailList representing the list of strings to be picked from
   */
//  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//  public YailList Elements() {
//    return input;
//  }

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

    listAdapterWithRecyclerView =new ListAdapterWithRecyclerView(ctx,first,second);

    ViewGroup.LayoutParams paramms=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
    recyclerView.setLayoutParams(paramms);

    LinearLayoutManager layoutManager=new LinearLayoutManager(ctx);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(listAdapterWithRecyclerView);

  //  container.$add(this);  
 

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
