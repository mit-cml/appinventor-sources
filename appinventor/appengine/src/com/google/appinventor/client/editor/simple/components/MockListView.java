// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidListViewAddDataPropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock ListView component.
 *
 */
public final class MockListView extends MockVisibleComponent {

 /**
   * Component type name.
   */
  public static final String TYPE = "ListView";
  private final VerticalPanel listViewWidget;

  private TextBox textBoxWidget;
  private InlineLabel labelInItem;
  private SimplePanel panelForItem;
  private String[] currentList;
  private boolean filterShowing = false;
  private VerticalPanel verticalItemPanel;
  private HorizontalPanel horizontalItemPanel;

  private static final String DEFAULT_BACKGROUND_COLOR = "&HFF000000";

  private static final String DEFAULT_TEXT_COLOR = "&HFFFFFFFF";

  //  Needed for background color of labelInItem
  private String backgroundColor;
  private String textColor;
  private String detailTextColor;
  private String currentElements;

  // variable to store the layout type for listview
  private int layout;

  // list to store data to be inflated in listview
  private List<JSONObject> currentItems;

  /**
   * Creates a new MockListView component. It places a label inside a simplepanel which
   * is then placed into a vertical panel
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockListView(SimpleEditor editor) {
    super(editor, TYPE, images.listview());

    currentItems = new ArrayList<>();

    listViewWidget = new VerticalPanel();
    //TODO (Jose) extract magic numbers as ComponentConstants.java
    listViewWidget.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    listViewWidget.setStylePrimaryName("ode-SimpleMockComponent");
    listViewWidget.setStyleName("listViewComponentStyle", true);

    createFilterBox();

    // textColor must be set before the component is initialized, because onPropertyChange
    // might call setElementsFromString, which tries to set the item textcolor
    textColor  = DEFAULT_TEXT_COLOR;
    detailTextColor = DEFAULT_TEXT_COLOR;

    initComponent(listViewWidget);
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidget, DEFAULT_BACKGROUND_COLOR);
  }

  @Override
  public void onCreateFromPalette() {
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /**
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

  /**
   * Sets the text to be added in the listview
   */
  private void setElementsFromStringProperty(String text){
    currentElements = text;
    currentList = text.split(",");

    if (currentItems.isEmpty()) {
      listViewWidget.clear();
      createFilterBox();

      if (filterShowing) {
        textBoxWidget.setVisible(true);
      } else {
        textBoxWidget.setVisible(false);
      }

      for (int i = 0; i < currentList.length; ++i){
        createLabelItem(i);
        createLabelPanel();
      }
    }
  }

  private void createLabelItem(int i) {
    labelInItem =new InlineLabel(currentList[i]);
    labelInItem.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    MockComponentsUtil.setWidgetBackgroundColor(labelInItem, backgroundColor);
    MockComponentsUtil.setWidgetTextColor(labelInItem, textColor);
  }

  private void createLabelPanel() {
    panelForItem =new SimplePanel();
    panelForItem.setStylePrimaryName("listViewItemStyle");
    panelForItem.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
    panelForItem.add(labelInItem);
    listViewWidget.add(panelForItem);
  }

  /**
   * update the layout type of the listview
   */
  private void updateLayoutType(String value) {
    layout = Integer.parseInt(value);
    YoungAndroidListViewAddDataPropertyEditor editor =
        (YoungAndroidListViewAddDataPropertyEditor) properties.getProperty(PROPERTY_NAME_LISTVIEW_ADD_DATA).getEditor();
    editor.setLayout(layout);
    if (!currentItems.isEmpty()) {
      createLabelItem(currentItems);
    }
  }

  /**
   * reads JSONString and convert it to JSONObject for each row and add it to ArrayList
   */
  private void displayOnDesigner(String value) {
    currentItems.clear();
    JSONValue jsonValue = (value.isEmpty() || value.equals("")) ? null : JSONParser.parseStrict(value);
    if (jsonValue != null) {
      JSONArray array = jsonValue.isArray();
      for (int i = 0; i < array.size(); ++i) {
        JSONObject jsonObject = array.get(i).isObject();
        currentItems.add(i, jsonObject);
      }
      if (!currentItems.isEmpty()) {
        createLabelItem(currentItems);
      }
    }
  }

  private void createSingleTextLayout(JSONObject object) {
    verticalItemPanel = new VerticalPanel();
    verticalItemPanel.setStylePrimaryName("listViewItemStyle");
    verticalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    verticalItemPanel.add(createInlineLabel(text1, textColor));
    listViewWidget.add(verticalItemPanel);
  }

  private void createTwoTextVerticalLayout(JSONObject object) {
    verticalItemPanel = new VerticalPanel();
    verticalItemPanel.setStylePrimaryName("listViewItemStyle");
    verticalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String text2 = object.containsKey("Text2") ? object.get("Text2").isString().stringValue() : "";
    verticalItemPanel.add(createInlineLabel(text1, textColor));
    verticalItemPanel.add(createInlineLabel(text2, detailTextColor));
    listViewWidget.add(verticalItemPanel);
  }

  private void createTwoTextHorizontalLayout(JSONObject object) {
    horizontalItemPanel = new HorizontalPanel();
    horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
    horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String text2 = object.containsKey("Text2") ? object.get("Text2").isString().stringValue() : "";
    InlineLabel label1 = createInlineLabel(text1, textColor);
    InlineLabel label2 = createInlineLabel(text2, detailTextColor);
    horizontalItemPanel.add(label1);
    horizontalItemPanel.add(label2);
    listViewWidget.add(horizontalItemPanel);
  }

  private void createImageSingleTextLayout(JSONObject object) {
    horizontalItemPanel = new HorizontalPanel();
    horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
    horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String image = object.containsKey("Image") ? object.get("Image").isString().stringValue() : "None";
    horizontalItemPanel.add(createImage(image, ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px"));
    horizontalItemPanel.add(createInlineLabel(text1, textColor));
    listViewWidget.add(horizontalItemPanel);
  }

  private void createImageTwoTextLayout(JSONObject object) {
    horizontalItemPanel = new HorizontalPanel();
    horizontalItemPanel.setStylePrimaryName("listViewItemStyle");
    horizontalItemPanel.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");
    verticalItemPanel = new VerticalPanel();
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String text2 = object.containsKey("Text2") ? object.get("Text2").isString().stringValue() : "";
    String image = object.containsKey("Image") ? object.get("Image").isString().stringValue() : "None";
    verticalItemPanel.add(createInlineLabel(text1, textColor));
    verticalItemPanel.add(createInlineLabel(text2, detailTextColor));
    horizontalItemPanel.add(createImage(image, ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px"));
    horizontalItemPanel.add(verticalItemPanel);
    listViewWidget.add(horizontalItemPanel);
  }

  /**
   * creates view of each row item in the designer according to the type of layout
   */
  private void createLabelItem(List<JSONObject> arrayList) {
    listViewWidget.clear();
    createFilterBox();

    if (filterShowing) {
      textBoxWidget.setVisible(true);
    } else {
      textBoxWidget.setVisible(false);
    }

    for (int i = 0; i < arrayList.size(); ++i) {
      JSONObject object = arrayList.get(i);
      if (layout == ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT) {
        createSingleTextLayout(object);
      } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT) {
        createTwoTextVerticalLayout(object);
      } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) {
        createTwoTextHorizontalLayout(object);
      } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
        createImageSingleTextLayout(object);
      } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT) {
        createImageTwoTextLayout(object);
      }
    }
  }

  /**
   * retrieves an image from media to diaplay in designer
   * @param imageName name of the image to be displayed for the row
   * @param widthValue width of the image
   * @param heightValue height of the image
   */
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

  /**
   * creates label for a text element of a row in designer
   * @param value text to be displayed
   * @param color color of the text
   */
  private InlineLabel createInlineLabel(String value, String color) {
    InlineLabel label = new InlineLabel(value);
    MockComponentsUtil.setWidgetBackgroundColor(label, backgroundColor);
    MockComponentsUtil.setWidgetTextColor(label, color);
    return label;
  }

  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_LISTVIEW)) {
      setElementsFromStringProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_FILTER_BAR)) {
      setFilterShowBox(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
      if (currentList != null) {
        setElementsFromStringProperty(currentElements);
      }
      if (!currentItems.isEmpty()) {
        createLabelItem(currentItems);
      }
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      textColor = newValue;
      if (currentList != null) {
        setElementsFromStringProperty(currentElements);
      }
      if (!currentItems.isEmpty()) {
        createLabelItem(currentItems);
      }
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_DETAILTEXTCOLOR)) {
      detailTextColor = newValue;
      if (currentList != null) {
        setElementsFromStringProperty(currentElements);
      }
      if (!currentItems.isEmpty()) {
        createLabelItem(currentItems);
      }
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_LAYOUT)) {
      updateLayoutType(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_ADD_DATA)) {
      displayOnDesigner(newValue);

      /* checks if data is present in ElementsFromStringProperty too
       * and hence populate the listview with its data when data from
       * AddData property is cleared
       */
      if(currentList != null) {
        setElementsFromStringProperty(currentElements);
      }
      refreshForm();
    }
  }
}
