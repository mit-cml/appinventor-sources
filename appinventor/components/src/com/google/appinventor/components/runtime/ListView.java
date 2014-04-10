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
import com.google.appinventor.components.runtime.util.YailList;

import android.view.View;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import android.text.TextWatcher;
import android.text.Editable;


/**
 * ListView Component.
 *
 */
@DesignerComponent(version = YaVersion.LISTVIEW_COMPONENT_VERSION, 
    description = "<p>This is a visible component that allows you to place a list in your view display a list of strings that you decide to set<br>" +
        "You can set the list using ElementsFromString or you can use the Elements blocks in the blocks editor. </p>",
    category = ComponentCategory.USERINTERFACE,
    nonVisible = false,
    iconName = "images/listView.png")

@SimpleObject
public final class ListView extends AndroidViewComponent implements AdapterView.OnItemClickListener {
  private final android.widget.ListView view;
  EditText txtSearchBox;
  protected final ComponentContainer container;
  private final LinearLayout lay;
  ArrayAdapter<String> adapter;
  String[] items;
  int index=0;
  String selection;
  int selectionIndex;
  private boolean showFilter = false;
  private static final boolean DEFAULT_ENABLED = false;

  /**
   * Creates a new ListView component.
   *
   * @param container  container, component will be placed in
   */
  public ListView(ComponentContainer container) {
    super(container);
    this.container=container;
    view = new android.widget.ListView(container.$context());
    view.setOnItemClickListener(this);
    lay = new LinearLayout(container.$context());
    lay.setOrientation(LinearLayout.VERTICAL);

    ElementsFromString("");
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_list_item_1, items);
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
      if (showFilter==false) {
        txtSearchBox.setVisibility(View.GONE);
      }else{
         txtSearchBox.setVisibility(View.VISIBLE);
      }
    lay.setBackgroundColor(COLOR_BLACK);
    lay.addView(txtSearchBox);
    lay.addView(view);
    lay.requestLayout();
    container.$add(this);
    Width(Component.LENGTH_FILL_PARENT);
  }

  @Override
  public View getView() {
    return lay;
  }

  /**
  *This function sets the list of the ListView
  */
  public void setList(){
    adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_list_item_1, items);
    view.setAdapter(adapter);
  }

  /**
  * Sets the height of the listView on the screen
  *  * @param int for height length
  */
  @Override
  @SimpleProperty(description = "Determines the height of the list on the view.", category =PropertyCategory.APPEARANCE)
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }

  /**
  * Sets the width of the listView on the screen
  * @param int for width length
  */
  @Override
  
  @SimpleProperty(description = "Determines the width of the list on the view.", category = PropertyCategory.APPEARANCE)
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }

  /**
  * Sets true or false to determine whether the search filter box is displayed in the listview or not
  */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = DEFAULT_ENABLED ? "True"
      : "False")
  @SimpleProperty
  public void ShowFilterBar(boolean showFilter) {
    this.showFilter = showFilter;
    if (showFilter==false) {
        txtSearchBox.setVisibility(View.GONE);
      }else{
         txtSearchBox.setVisibility(View.VISIBLE);
      }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns current state of ShowFilterBar indicating if "
      + "Search Filter Bar will be displayed on ListView or not")
  public boolean ShowFilterBar() {
    return showFilter;
  }

  /**
   * Specifies the String elements you want to add to the listview.
   *
   * @param text  new caption for label
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description="Build a list with a string of words separated by commas like this Cheese,Cheddar and "
    +"each word before the comma will form a cell in your list.",  category = PropertyCategory.BEHAVIOR)
  public void ElementsFromString(String text) {
    items=text.split(",");
    setList();
  }

  @SimpleProperty(description="Send a list of strings to build your list.",  category = PropertyCategory.BEHAVIOR)
  public void Elements(YailList text) {
    items=text.toStringArray();
    setList();
  }

  @SimpleProperty(description="Choose a position to be your index. This could be used to retrieve "
    +"the text at the position choosen in the list.",  category = PropertyCategory.BEHAVIOR)
  public void SelectionIndex(int i){
    index=i-1;
    selection=items[index];
  }

  /**
  * Returns the text in the listview at the position set by selectionindex
  */
  @SimpleProperty(description="Returns the text of your selection.",  category = PropertyCategory.BEHAVIOR)
  public String Selection(){
      selection=items[index];
      return selection;
  }

  /**
   * Simple event to raise when the component is clicked
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    index=position;
    AfterPicking();
  }


  @SimpleEvent
  public void BeforePicking() {
    EventDispatcher.dispatchEvent(this, "BeforePicking");
  }

  /**
   * Simple event to be raised after the list returns its
   * result and the properties have been filled in.
   */
  @SimpleEvent
  public void AfterPicking() {
    EventDispatcher.dispatchEvent(this, "AfterPicking");
  }

}