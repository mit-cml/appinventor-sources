


package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.InlineLabel;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidListViewAddDataPropertyEditor;
import java.util.ArrayList;

/**
 * Mock RecyclerView component.
 *
 */
public final class MockRecyclerView extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "RecyclerView";
  public static final String PROPERTY_NAME_TEXTCOLOR = "TextMainColor";
  public static final String PROPERTY_NAME_DETAILTEXTCOLOR = "TextDetailColor";
  
  private final VerticalPanel listViewWidgets;
  
  private Image image;
    
  private TextBox textBoxWidget;
  
  private boolean filterShowing = false;

  private static final String DEFAULT_BACKGROUND_COLOR = "&HFF000000";

  private static final String DEFAULT_TEXT_COLOR = "&HFFFFFFFF";

  //  Needed for background color of labelInItem
  private String backgroundColor;
  private String textMainColor;
  private String textDetailColor;
  
  private int layout;
  private ArrayList<JSONObject> currentItems;

  private int orientation = 1;

  /**
  * Creates a new MockImage component.
  *
  * @param editor  editor of source file the component belongs to
  */
  public MockRecyclerView(SimpleEditor editor) {
    super(editor, TYPE, images.recyclerview());

    listViewWidgets = new VerticalPanel();
    //TODO (Jose) extract magic numbers as ComponentConstants.java
    listViewWidgets.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    listViewWidgets.setStylePrimaryName("ode-SimpleMockComponent");
    listViewWidgets.setStyleName("listViewComponentStyle", true);

    currentItems = new ArrayList<>();

    createFilterBox();

    // textColor must be set before the component is initialized, because onPropertyChange
    // might call setSlementsFromString, which tries to set the item textcolor
    textMainColor  = DEFAULT_TEXT_COLOR;
    textDetailColor = DEFAULT_TEXT_COLOR;
    initComponent(listViewWidgets);
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidgets, DEFAULT_BACKGROUND_COLOR);
    }

     @Override
  public void onCreateFromPalette() {
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the listview's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    backgroundColor = text;
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidgets, text);
  }


   /**
   * This method is called when the show filter box is checked or unchecked.
   * Checking the showfilterbar adds a textbox in the mocklistview and
   * vice versa.
   */
  private void setFilterShowBox(String value) {
    if (Boolean.parseBoolean(value)) {
      filterShowing = true;
      textBoxWidget.setVisible(true);
    }
    else {
      filterShowing = false;
      textBoxWidget.setVisible(false);
    }
  }

    private void createFilterBox() {
    textBoxWidget = new TextBox();
    textBoxWidget.setText("Search list...");
    textBoxWidget.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_FILTER_PREFERRED_HEIGHT + "px");
    textBoxWidget.setVisible(false);
    listViewWidgets.add(textBoxWidget);
  }


  private void displayOnDesigner(String value) {
    currentItems.clear();
    JSONValue jsonValue = (value.isEmpty() || value.equals("")) ? null : JSONParser.parseStrict(value);
    if(jsonValue != null){
      JSONArray array = jsonValue.isArray();
      for(int i = 0; i < array.size(); ++i) {
        JSONObject jsonObject = array.get(i).isObject();
        currentItems.add(i, jsonObject);
      }
      if(!currentItems.isEmpty()){
        createLabelItems(currentItems);
      }
    }
  }


  private void createLabelItems(ArrayList<JSONObject> arrayList) {
    listViewWidgets.clear();
    createFilterBox();
    
    if (filterShowing) {
      textBoxWidget.setVisible(true);
    }else{
      textBoxWidget.setVisible(false);
    }

    for(int i = 0; i < arrayList.size(); ++i) {
      JSONObject object = arrayList.get(i);
      
      if(layout == 0) {
        VerticalPanel verticalItemPanel = new VerticalPanel();
        verticalItemPanel.setStylePrimaryName("listViewItemStyle");
        verticalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        verticalItemPanel.add(createInlineLabel(text1, textMainColor));
        listViewWidgets.add(verticalItemPanel);
      } 
      else if(layout == 1) {
        VerticalPanel verticalItemPanel = new VerticalPanel();
        verticalItemPanel.setStylePrimaryName("listViewItemStyle");
        verticalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        verticalItemPanel.add(createInlineLabel(text1, textMainColor));
        verticalItemPanel.add(createInlineLabel(text2, textDetailColor));
        listViewWidgets.add(verticalItemPanel);
      }
      else if(layout == 2) {
        HorizontalPanel horizontalItemPanel = new HorizontalPanel();
        horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
        horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        InlineLabel label1 = createInlineLabel(text1, textMainColor);
        InlineLabel label2 = createInlineLabel(text2, textDetailColor);
        horizontalItemPanel.add(label1);
        horizontalItemPanel.add(label2);
        listViewWidgets.add(horizontalItemPanel);
      } 
      else if (layout == 3) {
        HorizontalPanel horizontalItemPanel = new HorizontalPanel();
        horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
        horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String image = object.containsKey("Image")?object.get("Image").isString().stringValue():"None";
        horizontalItemPanel.add(createImage(image, ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px"));
        horizontalItemPanel.add(createInlineLabel(text1, textMainColor));
        listViewWidgets.add(horizontalItemPanel);
      } 
      else if(layout == 4) {
        HorizontalPanel horizontalItemPanel = new HorizontalPanel();
        horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
        horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        VerticalPanel verticalItemPanel = new VerticalPanel();
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        String image = object.containsKey("Image")?object.get("Image").isString().stringValue():"None";
        verticalItemPanel.add(createInlineLabel(text1, textMainColor));
        verticalItemPanel.add(createInlineLabel(text2, textDetailColor));
        horizontalItemPanel.add(createImage(image, ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px"));
        horizontalItemPanel.add(verticalItemPanel);
        listViewWidgets.add(horizontalItemPanel);
      }
    }
  }

  private InlineLabel createInlineLabel(String value, String color) {
    InlineLabel label = new InlineLabel(value);
    MockComponentsUtil.setWidgetBackgroundColor(label, backgroundColor);
    MockComponentsUtil.setWidgetTextColor(label, color);
    return label;
  }

  private Image createImage(String imageName, String widthValue, String heightValue) {
    Image image = new Image();
    String url = convertImagePropertyValueToUrl(imageName);
    if (url == null) {
      // text was not recognized as an asset. Just display the icon for this type of component.
      image.setUrl(getIconImage().getUrl());
    }else{
      image.setUrl(url);
      image.setSize(widthValue, heightValue);
    }
    return image;
  }

  private void updateLayoutType(String value) {
    layout = Integer.parseInt(value);
    YoungAndroidListViewAddDataPropertyEditor editor =
        (YoungAndroidListViewAddDataPropertyEditor) properties.getProperty(PROPERTY_NAME_LISTVIEW_ADD_DATA).getEditor();
    editor.setLayout(layout);
  }

  private void setOrientation(String text){
    orientation = Integer.parseInt(text);   
  }

  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    // Apply changed properties to the mock component
    
    if (propertyName.equals(PROPERTY_NAME_SHOW_FILTER_BAR)) {
      setFilterShowBox(newValue);
      refreshForm();
    } 
    else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
      refreshForm();
    }
     else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      textMainColor = newValue;
      createLabelItems(currentItems);
      refreshForm();
    }
    else if (propertyName.equals(PROPERTY_NAME_DETAILTEXTCOLOR)) {
      textDetailColor = newValue;
      createLabelItems(currentItems);
      refreshForm();
    }
    else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_ADD_DATA)) {
      displayOnDesigner(newValue);
      refreshForm();
    }else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_LAYOUT)) {
      updateLayoutType(newValue);
      refreshForm();
    }else if (propertyName.equals(PROPERTY_NAME_RECYCLERVIEW_ORIENTATION)){
      setOrientation(newValue);
      refreshForm();
    }
  }
  }
