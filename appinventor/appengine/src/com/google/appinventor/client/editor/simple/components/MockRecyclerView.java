


package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.components.common.ComponentConstants;
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

    createFilterBox();

     // textColor must be set before the component is initialized, because onPropertyChange
    // might call setSlementsFromString, which tries to set the item textcolor
    textColor  = DEFAULT_TEXT_COLOR;

    initComponent(listViewWidget);
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

    listViewWidget.clear();
    createFilterBox();

    if (filterShowing) {
      textBoxWidget.setVisible(true);
    }
    else {
      textBoxWidget.setVisible(false);
    }

  if(currentImage!="" && currentElementsSecond!=""){
  //if(currentList.length==2*currentImageList.length)  
  setItems(currentListFirst,currentListSecond,currentImageList);  
}
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

  if(currentImage!="" && currentElementsFirst!=""){
 
  //if(currentList.length==2*currentImageList.length)  
  setItems(currentListFirst,currentListSecond,currentImageList);  
}
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
  
  if(currentElementsFirst!="" && currentElementsSecond != ""){
 
  //if(currentList.length==2*currentImageList.length)  
  setItems(currentListFirst,currentListSecond,currentImageList);  
}
}

  private void setItems(String[] currentListFirst,String[] currentListSecond, String[] currentImageList){
   

    if(currentListFirst.length == currentImageList.length && currentListFirst.length == currentListSecond.length){
    for(int i=0;i<currentListFirst.length;i++){
      createLabelItem(i);
      createImageItem(i);
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
    }
  }


  }
