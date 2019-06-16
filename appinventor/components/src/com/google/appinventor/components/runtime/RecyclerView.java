


package com.google.appinventor.components.runtime;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
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
    
    private RecyclerView recyclerView;
    private Context ctx;
    private EditText txtSearchBox;
    protected final ComponentContainer container;
    private final LinearLayout linearLayout;

    private YailList input;

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

    Input = YailList.makeEmptyList();
   // First = YailList.makeEmptyList();
   // Second = YailList.makeEmptyList();
    
    // initialize selectionIndex which also sets selection
    SelectionIndex(0);

    recyclerView = new RecyclerView(container.$context());
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

    // set the colors and initialize the elements
    // note that the TextColor and ElementsFromString setters
    // need to have the textColor set first, since they reset the
    // adapter

    Width(Component.LENGTH_FILL_PARENT);
    BackgroundColor(DEFAULT_BACKGROUND_COLOR);
    SelectionColor(DEFAULT_SELECTION_COLOR);

    textColor = DEFAULT_TEXT_COLOR;
    TextColor(textColor);
    textSize = DEFAULT_TEXT_SIZE;
    TextSize(textSize);
    ElementsFromString("");
}



/**
   * Set a list of text elements to build a ListView
   * @param itemsList a YailList containing the strings to be added to the ListView
   */
  @SimpleProperty(description="List of text elements to show in the ListView.  This will" +
                "signal an error if the elements are not text strings.",
      category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList Input) {
    Input = ElementsUtil.elements(input, "RecyclerView");
 
//doubt setAdapterData() why called here
    setAdapterData();
  }

  /**
   * Elements property getter method
   *
   * @return a YailList representing the list of strings to be picked from
   */
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
    items = ElementsUtil.elementsFromString(itemstring);
    String[] str = items.toStringArray();
    String[] first,second;

    for (int i = 0; i < str.length; i++) {
      int x=i/2;
      if(i%2==0){first[x]=str[i];}
      else{second[x]=str[i];}
    }
    setAdapter(String[] first,String[] second);

  }

  /**
   * Sets the items of the ListView through an adapter
   */

 public void setAdapter(String[] first,String[] second){

    ListAdapterWithRecycleView listAdapterWithRecycleView =new ListAdapterWithRecycleView(context,first,second);

    ViewGroup.LayoutParams paramms=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
    recyclerView.setLayoutParams(paramms);

    LinearLayoutManager layoutManager=new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(listAdapterWithRecycleView);

    container.$add(this);  
 

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
