


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
//
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
//
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

//public final class MockRecyclerView extends MockImageBase {

    /**
   * Component type name.
   */
  public static final String TYPE = "RecyclerView";
  private static final String PROPERTY_NAME_PICTURE = "Picture";
  protected static final String PROPERTY_NAME_RECYCLERVIEW_FIRST = "ElementsFromStringFirst";
  protected static final String PROPERTY_NAME_RECYCLERVIEW_SECOND = "ElementsFromStringSecond";
  private final VerticalPanel listViewWidget;
  private final VerticalPanel listViewWidgets;
  private HorizontalPanel listItemWidget;

  // Widget for showing the image.
  private Image image;
  private String picturePropValue;
  
  private TextBox textBoxWidget;
  private InlineLabel labelInItemFirst,labelInItemSecond;
  private VerticalPanel panelForItem;
  private String[] currentListFirst;
  private String[] currentListSecond;
  private String[] currentImageList;
  private boolean filterShowing = false;

  private static final String DEFAULT_BACKGROUND_COLOR = "&HFF000000";

  private static final String DEFAULT_TEXT_COLOR = "&HFFFFFFFF";

  //  Needed for background color of labelInItem
  private String backgroundColor;
  private String textColor;
  private String currentElementsFirst="";
  private String currentElementsSecond="";
  private String currentImage="";
//
  private String layout="4";
  private ArrayList<JSONObject> currentItems;
  //private HorizontalPanel horizontalItemPanel;
    /**
     * Creates a new MockImage component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockRecyclerView(SimpleEditor editor) {
        super(editor, TYPE, images.recyclerview());

    listViewWidget = new VerticalPanel();
    //TODO (Jose) extract magic numbers as ComponentConstants.java
    listViewWidget.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    listViewWidget.setStylePrimaryName("ode-SimpleMockComponent");
    listViewWidget.setStyleName("listViewComponentStyle", true);


    listViewWidgets = new VerticalPanel();
    //TODO (Jose) extract magic numbers as ComponentConstants.java
    listViewWidgets.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    listViewWidgets.setStylePrimaryName("ode-SimpleMockComponent");
    listViewWidgets.setStyleName("listViewComponentStyle", true);

  //  YoungAndroidListViewAddDataPropertyEditor editor =
    //    (YoungAndroidListViewAddDataPropertyEditor) properties.getProperty(PROPERTY_NAME_LISTVIEW_ADD_DATA).getEditor();
   // editor.setLayout(layout);

    currentItems = new ArrayList<>();

    createFilterBox();

     // textColor must be set before the component is initialized, because onPropertyChange
    // might call setSlementsFromString, which tries to set the item textcolor
    textColor  = DEFAULT_TEXT_COLOR;

    initComponent(listViewWidgets);
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidget, DEFAULT_BACKGROUND_COLOR);


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
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidget, text);
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
    listViewWidget.add(textBoxWidget);
  }

   private void setElementsFromStringFirstProperty(String text){
    currentElementsFirst = text;
    currentListFirst = text.split(",");

    setItems();  

  }

  private void setElementsFromStringSecondProperty(String text){
    currentElementsSecond = text;
    currentListSecond = text.split(",");

    listViewWidget.clear();
    createFilterBox();

    if (filterShowing) {
      textBoxWidget.setVisible(true);
    }
    else {
      textBoxWidget.setVisible(false);
    }
   setItems();  

  }

  private void addNewPicture(String stringImage){
    if(currentImage == "")
      {currentImage += stringImage;}
    else
      {currentImage +=",";
       currentImage +=stringImage;}

       setElementImagesProperty(currentImage);
  }

  private void setElementImagesProperty(String stringImage){
   currentImage=stringImage;
   currentImageList = currentImage.split(",");
   listViewWidget.clear();
  
  setItems();  

}

  private void setItems(){

    // TODO: Handle multiple images in Picture property
    if(currentImageList !=null && currentImageList.length > 0 && currentListFirst.length == currentListSecond.length) {
      listViewWidget.clear();
      createFilterBox();

      if (filterShowing) {
        textBoxWidget.setVisible(true);
      } else {
        textBoxWidget.setVisible(false);
      }
      for (int i = 0; i < currentListFirst.length; i++) {
        createLabelItem(i);
        createImageItem(0);
        createLabelPanel();
      }
    }
  }

  private void createLabelItem(int i) {
   
    listItemWidget = new HorizontalPanel();

    labelInItemFirst =new InlineLabel(currentListFirst[i]);
    labelInItemSecond =new InlineLabel(currentListSecond[i]);
    labelInItemFirst.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    MockComponentsUtil.setWidgetBackgroundColor(labelInItemFirst, backgroundColor);
    MockComponentsUtil.setWidgetTextColor(labelInItemFirst, textColor);

    labelInItemSecond.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    MockComponentsUtil.setWidgetBackgroundColor(labelInItemSecond, backgroundColor);
    MockComponentsUtil.setWidgetTextColor(labelInItemSecond, textColor);

    listItemWidget.add(labelInItemFirst);
    listItemWidget.add(labelInItemSecond);
  }

  private void createImageItem(int i){
    image = new Image();
    image.addErrorHandler(new ErrorHandler() {
      @Override
      public void onError(ErrorEvent event) {
        if (picturePropValue != null && !picturePropValue.isEmpty()) {
          OdeLog.elog("Error occurred while loading image " + picturePropValue);
        }
        refreshForm(true);
      }
    });
    image.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent event) {
        refreshForm(true);
        //resizeImage();  // resize after the new image occupies the form
      }
    });
    setPictureProperty(currentImageList[i]);
  }

  /*
   * Sets the image's url to a new value.
  */ 
  private void setPictureProperty(String text) {
    picturePropValue = text;
    String url = convertImagePropertyValueToUrl(text);
    if (url == null) {
      // text was not recognized as an asset. Just display the icon for this type of component.
     image.setUrl(getIconImage().getUrl());
    } else {
      image.setUrl(url);
   }
   image.setHeight(10+"px");
   //image.setWidth(10+"px");
   
  }
  private void createLabelPanel() {
    panelForItem =new VerticalPanel();
    panelForItem.setStylePrimaryName("listViewItemStyle");
    panelForItem.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
    ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");

    //panelForItem.add(labelInItemFirst);
    panelForItem.add(image);
    panelForItem.add(listItemWidget);
    listViewWidget.add(panelForItem);
  }

  private void displayOnDesigner(String value) {
    currentItems.clear();
    JSONValue jsonValue = (value.isEmpty() || value.equals("")) ? null : JSONParser.parseStrict(value);
    if(jsonValue != null) {
      JSONArray array = jsonValue.isArray();
      for(int i = 0; i < array.size(); ++i) {
        JSONObject jsonObject = array.get(i).isObject();
        currentItems.add(i, jsonObject);
      }
      if(!currentItems.isEmpty()) {
        createLabelItems(currentItems);
      }
    }
  }


  private void createLabelItems(ArrayList<JSONObject> arrayList) {
    listViewWidgets.clear();
    createFilterBox();
    
    if (filterShowing) {
      textBoxWidget.setVisible(true);
    } else {
      textBoxWidget.setVisible(false);
    }

    for(int i = 0; i < arrayList.size(); ++i) {
      JSONObject object = arrayList.get(i);
      /*if (layout.equals("0")) {
        verticalItemPanel = new VerticalPanel();
        verticalItemPanel.setStylePrimaryName("listViewItemStyle");
        verticalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
       ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        verticalItemPanel.add(createInlineLabel(text1, textColor));
        listViewWidget.add(verticalItemPanel);
      } else if(layout.equals("1")) {
        verticalItemPanel = new VerticalPanel();
        verticalItemPanel.setStylePrimaryName("listViewItemStyle");
        verticalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        verticalItemPanel.add(createInlineLabel(text1, textColor));
        verticalItemPanel.add(createInlineLabel(text2, detailTextColor));
        listViewWidget.add(verticalItemPanel);
      } else if(layout.equals("2")) {
        horizontalItemPanel = new HorizontalPanel();
        horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
        horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
       ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        InlineLabel label1 = createInlineLabel(text1, textColor);
        label1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        InlineLabel label2 = createInlineLabel(text2, detailTextCoslor);
        label2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
//        horizontalItemPanel.setCellWidth(label1, "150px");
//        horizontalItemPanel.setCellWidth(label2, "150px");
        horizontalItemPanel.add(label1);
        horizontalItemPanel.add(label2);
//        horizontalItemPanel.setCellHorizontalAlignment(label1, HasHorizontalAlignment.ALIGN_LEFT);
//        horizontalItemPanel.setCellHorizontalAlignment(label2, HasHorizontalAlignment.ALIGN_RIGHT);
        listViewWidget.add(horizontalItemPanel);
      } else */
      //if (layout.equals("3")) {
     /*   VerticalPanel textBoxPanel = new VerticalPanel();
        horizontalItemPanel = new HorizontalPanel();
        horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
        horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        String image = object.containsKey("Image")?object.get("Image").isString().stringValue():"None";
        horizontalItemPanel.add(createImage(image, ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px"));
        textBoxPanel.add(createInlineLabel(text1, textColor));
        textBoxPanel.add(createInlineLabel(text2, textColor));
        //horizontalItemPanel.add(createInlineLabel(text1, textColor));
        //horizontalItemPanel.add(createInlineLabel(text2, textColor));
        horizontalItemPanel.add(textBoxPanel);
        listViewWidgets.add(horizontalItemPanel);
      */

      //}

      if (layout.equals("0")) {
       VerticalPanel verticalItemPanel = new VerticalPanel();
        verticalItemPanel.setStylePrimaryName("listViewItemStyle");
        verticalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
       ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        verticalItemPanel.add(createInlineLabel(text1, textColor));
        listViewWidgets.add(verticalItemPanel);
      } else if(layout.equals("1")) {
       VerticalPanel verticalItemPanel = new VerticalPanel();
        verticalItemPanel.setStylePrimaryName("listViewItemStyle");
        verticalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        verticalItemPanel.add(createInlineLabel(text1, textColor));
        verticalItemPanel.add(createInlineLabel(text2, textColor));
        listViewWidgets.add(verticalItemPanel);
      } else if(layout.equals("2")) {
       HorizontalPanel horizontalItemPanel = new HorizontalPanel();
        horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
        horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
       ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        InlineLabel label1 = createInlineLabel(text1, textColor);
        InlineLabel label2 = createInlineLabel(text2, textColor);
        horizontalItemPanel.add(label1);
        horizontalItemPanel.add(label2);
        listViewWidgets.add(horizontalItemPanel);
      } else if (layout.equals("3")) {
       HorizontalPanel horizontalItemPanel = new HorizontalPanel();
        horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
        horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String image = object.containsKey("Image")?object.get("Image").isString().stringValue():"None";
        horizontalItemPanel.add(createImage(image, ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px"));
        horizontalItemPanel.add(createInlineLabel(text1, textColor));
        listViewWidgets.add(horizontalItemPanel);
      } else if(layout.equals("4")) {
       HorizontalPanel horizontalItemPanel = new HorizontalPanel();
        horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
        horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
       VerticalPanel verticalItemPanel = new VerticalPanel();
        String text1 = object.containsKey("Text1")?object.get("Text1").isString().stringValue():"";
        String text2 = object.containsKey("Text2")?object.get("Text2").isString().stringValue():"";
        String image = object.containsKey("Image")?object.get("Image").isString().stringValue():"None";
        verticalItemPanel.add(createInlineLabel(text1, textColor));
        verticalItemPanel.add(createInlineLabel(text2, textColor));
        horizontalItemPanel.add(createImage(image, ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px"));
        horizontalItemPanel.add(verticalItemPanel);
        listViewWidgets.add(horizontalItemPanel);
      }


    }
  }

  private InlineLabel createInlineLabel(String value, String color) {
    InlineLabel label = new InlineLabel(value);
    //label.setSize("50%==", heightValue);
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
      } else {
          image.setUrl(url);
          image.setSize(widthValue, heightValue);
      }


      return image;
  }

private void updateLayoutType(String value) {
    layout = value;
    YoungAndroidListViewAddDataPropertyEditor editor =
        (YoungAndroidListViewAddDataPropertyEditor) properties.getProperty(PROPERTY_NAME_LISTVIEW_ADD_DATA).getEditor();
    editor.setLayout(layout);
 //   if(!currentItems.isEmpty()) {
 //     createLabelItem(currentItems);
 //   }
  }
  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    // Apply changed properties to the mock component
   if (propertyName.equals(PROPERTY_NAME_RECYCLERVIEW_FIRST)) {
      setElementsFromStringFirstProperty(newValue);
      refreshForm();
    }else if(propertyName.equals(PROPERTY_NAME_RECYCLERVIEW_SECOND)){
        setElementsFromStringSecondProperty(newValue);
        refreshForm();
    } 
    else if (propertyName.equals(PROPERTY_NAME_SHOW_FILTER_BAR)) {
      setFilterShowBox(newValue);
      refreshForm();
    } 
    else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
      if (currentListFirst != null) {
        setElementsFromStringFirstProperty(currentElementsFirst);
      }
      if (currentListSecond != null) {
        setElementsFromStringSecondProperty(currentElementsFirst);
      }
      if (currentImageList != null) {
        setElementImagesProperty(currentImage);
      }
      refreshForm();
    }
     else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      textColor = newValue;
      createLabelItems(currentItems);
      if (currentListFirst != null) {
        setElementsFromStringFirstProperty(currentElementsFirst);
      }
      if (currentListSecond != null) {
        setElementsFromStringSecondProperty(currentElementsSecond);
      }
      if (currentImageList != null) {
        setElementImagesProperty(currentImage);
      }
      refreshForm();
    }else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_IMAGES)) {
     addNewPicture(newValue); // setUrl() triggers onLoad
    }else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_ADD_DATA)) {
    // addNewPicture(newValue); // setUrl() triggers onLoad
      displayOnDesigner(newValue);
      if (currentListFirst != null) {
        setElementsFromStringFirstProperty(currentElementsFirst);
      }
      if (currentListSecond != null) {
        setElementsFromStringSecondProperty(currentElementsSecond);
      }
      if (currentImageList != null) {
        setElementImagesProperty(currentImage);
      }
      refreshForm();
    
    }else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_LAYOUT)) {
      updateLayoutType(newValue);
      refreshForm();
    }
  }


  }
