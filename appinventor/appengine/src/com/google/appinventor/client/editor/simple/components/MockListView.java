// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidListViewAddDataPropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

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

  private final FlowPanel listViewWidget;
  private final FlowPanel listPanel;

  private TextBox textBoxWidget;
  private String[] currentList;
  private FlowPanel verticalItemPanel;
  private FlowPanel horizontalItemPanel;
  private boolean firstItem;
  private boolean horizontalOrientation = false;

  private static final String DEFAULT_BACKGROUND_COLOR = "&HFF000000";

  private static final String DEFAULT_TEXT_COLOR = "&HFFFFFFFF";

  //  Needed for background color of labelInItem
  private String elementColor;
  private String dividerColor;
  private int dividerThickness;
  private int cornerRadius;
  private int elementMargins;
  private String textColor;
  private String detailTextColor;
  private String currentElements;
  private String mainTypeface;
  private String detailTypeface;
  private String mainFontSize;
  private String detailFontSize;
  private int imageHeight;
  private int imageWidth;
  private int itemHeight;

  // variable to store the layout type for listview
  private int layout;

  // list to store data to be inflated in listview
  private List<JSONObject> currentItems;

  /**
   * Creates a new MockListView component. It places a label inside a simplepanel which is then
   * placed into a vertical panel
   *
   * @param editor editor of source file the component belongs to
   */
  public MockListView(SimpleEditor editor) {
    super(editor, TYPE, images.listview());

    currentItems = new ArrayList<>();
    listPanel = new FlowPanel();
    listPanel.setStylePrimaryName("listViewScrollableStyle");
    listViewWidget = new FlowPanel();
    // TODO (Jose) extract magic numbers as ComponentConstants.java
    listViewWidget.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    listViewWidget.setStylePrimaryName("ode-SimpleMockComponent");
    listViewWidget.setStyleName("listViewComponentStyle", true);
    createFilterBox();
    listViewWidget.add(listPanel);

    // textColor must be set before the component is initialized, because onPropertyChange
    // might call setElementsFromString, which tries to set the item textcolor
    textColor = DEFAULT_TEXT_COLOR;
    detailTextColor = DEFAULT_TEXT_COLOR;
    mainTypeface = "0";
    detailTypeface = "0";
    mainFontSize = "22.0";
    detailFontSize = "14.0";
    imageHeight = 40; // (200 / 5)
    imageWidth = 40; // (200 / 5)

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
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidget, text);
  }

  /** Sets the listview's ElementColor property to a new value. */
  private void setElementColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&H00FFFFFF"; // none color
    }
    elementColor = text;
  }

  /** Sets the listview's DividerColor property to a new value. */
  private void setDividerColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF"; // white color
    }
    dividerColor = text;
  }

  private void createFilterBox() {
    textBoxWidget = new TextBox();
    textBoxWidget.setText("Search list...");

    textBoxWidget.getElement().getStyle().clearMargin();
    textBoxWidget.setStyleName("listViewFilterStyle");
    textBoxWidget.setVisible(false);
    listViewWidget.add(textBoxWidget);
  }

  /**
   * Sets the text to be added in the listview
   */
  private void setElementsFromStringProperty(String text) {
    currentElements = text;
    currentList = text.split(",");
    if (currentItems.isEmpty()) {
      listPanel.clear();
      firstItem = true;
      for (String item : currentList) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Text1", new JSONString(item));
        populateList(jsonObject);
      }
    }
  }

  /**
   * update the layout type of the listview
   */
  private void updateLayoutType(String value) {
    layout = Integer.parseInt(value);
    YoungAndroidListViewAddDataPropertyEditor editor =
        (YoungAndroidListViewAddDataPropertyEditor)
            properties.getProperty(PROPERTY_NAME_LISTVIEW_ADD_DATA).getEditor();
    editor.setLayout(layout);
  }

  /**
   * reads JSONString and convert it to JSONObject for each row and add it to ArrayList
   */
  private void displayOnDesigner(String value) {
    currentItems.clear();
    JSONValue jsonValue =
        (value.isEmpty() || value.equals("")) ? null : JSONParser.parseStrict(value);
    if (jsonValue != null) {
      JSONArray array = jsonValue.isArray();
      for (int i = 0; i < array.size(); ++i) {
        JSONObject jsonObject = array.get(i).isObject();
        currentItems.add(i, jsonObject);
      }
    }
  }

  private void createSingleTextLayout(JSONObject object) {
    verticalItemPanel = new FlowPanel();
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    InlineLabel main = createInlineLabel(text1, textColor);
    MockComponentsUtil.setWidgetFontSize(main, mainFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, main, mainTypeface);
    verticalItemPanel.add(main);
    setItemHeight(false, false);
    decorateWidget(verticalItemPanel);    
    listPanel.add(verticalItemPanel);
  }

  private void createTwoTextVerticalLayout(JSONObject object) {
    verticalItemPanel = new FlowPanel();
    FlowPanel container = new FlowPanel();
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String text2 = object.containsKey("Text2") ? object.get("Text2").isString().stringValue() : "";
    InlineLabel main = createInlineLabel(text1, textColor);
    MockComponentsUtil.setWidgetFontSize(main, mainFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, main, mainTypeface);
    main.setWidth("100%");
    main.getElement().getStyle().setDisplay(Display.BLOCK);
    InlineLabel detail = createInlineLabel(text2, detailTextColor);
    MockComponentsUtil.setWidgetFontSize(detail, detailFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, detail, detailTypeface);
    detail.setWidth("100%");
    detail.getElement().getStyle().setDisplay(Display.BLOCK);
    container.add(main);
    container.add(detail);
    setItemHeight(true, false);
    decorateWidget(verticalItemPanel);
    container.getElement().getStyle().setProperty("flex-direction", "column");
    verticalItemPanel.add(container);
    listPanel.add(verticalItemPanel);
  }

  private void createTwoTextHorizontalLayout(JSONObject object) {
    horizontalItemPanel = new FlowPanel();
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String text2 = object.containsKey("Text2") ? object.get("Text2").isString().stringValue() : "";
    InlineLabel main = createInlineLabel(text1, textColor);
    MockComponentsUtil.setWidgetFontSize(main, mainFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, main, mainTypeface);
    InlineLabel detail = createInlineLabel(text2, detailTextColor);
    MockComponentsUtil.setWidgetFontSize(detail, detailFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, detail, detailTypeface);
    detail.getElement().getStyle().setMarginLeft(5, Unit.PX);
    horizontalItemPanel.add(main);
    horizontalItemPanel.add(detail);
    horizontalItemPanel.getElement().getStyle().setVerticalAlign(VerticalAlign.BASELINE);  
    //horizontalItemPanel.getElement().getStyle().setAlignItems(Style.AlignItems.BASELINE);
    setItemHeight(false, false);
    decorateWidget(horizontalItemPanel);
    listPanel.add(horizontalItemPanel);
  }

  private void createImageSingleTextLayout(JSONObject object) {
    horizontalItemPanel = new FlowPanel();
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String image =
        object.containsKey("Image") ? object.get("Image").isString().stringValue() : "None";
    InlineLabel main = createInlineLabel(text1, textColor);
    main.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
    main.getElement().getStyle().setDisplay(Display.INLINE);
    MockComponentsUtil.setWidgetFontSize(main, mainFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, main, mainTypeface);    
    horizontalItemPanel.add(createImage(image, imageWidth + "px", imageHeight + "px"));
    horizontalItemPanel.add(main);
    setItemHeight(false, true);
    decorateWidget(horizontalItemPanel);
    listPanel.add(horizontalItemPanel);
  }

  private void createImageTwoTextLayout(JSONObject object) {
    horizontalItemPanel = new FlowPanel();
    verticalItemPanel = new FlowPanel();
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String text2 = object.containsKey("Text2") ? object.get("Text2").isString().stringValue() : "";
    String image =
        object.containsKey("Image") ? object.get("Image").isString().stringValue() : "None";
    InlineLabel main = createInlineLabel(text1, textColor);
    MockComponentsUtil.setWidgetFontSize(main, mainFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, main, mainTypeface);
    InlineLabel detail = createInlineLabel(text2, detailTextColor);
    MockComponentsUtil.setWidgetFontSize(detail, detailFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, detail, detailTypeface);
    verticalItemPanel.add(main);
    verticalItemPanel.add(detail);
    verticalItemPanel.getElement().getStyle().setDisplay(Display.INLINE_FLEX);
    verticalItemPanel.getElement().getStyle().setProperty("flexDirection", "column");
    verticalItemPanel.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);    
    horizontalItemPanel.add(createImage(image, imageWidth + "px", imageHeight + "px"));
    horizontalItemPanel.add(verticalItemPanel);
    setItemHeight(true, true);
    decorateWidget(horizontalItemPanel);
    listPanel.add(horizontalItemPanel);
  }

  private void createImageTwoTextVerticalLayout(JSONObject object) {
    FlowPanel verticalItemPanel = new FlowPanel();
    FlowPanel horizontalItemPanel = new FlowPanel();
    FlowPanel container = new FlowPanel();
    String text1 = object.containsKey("Text1") ? object.get("Text1").isString().stringValue() : "";
    String text2 = object.containsKey("Text2") ? object.get("Text2").isString().stringValue() : "";
    String image = object.containsKey("Image") ? object.get("Image").isString().stringValue() : "None";
    InlineLabel main = createInlineLabel(text1, textColor);
    MockComponentsUtil.setWidgetFontSize(main, mainFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, main, mainTypeface);
    main.getElement().getStyle().setTextAlign(TextAlign.CENTER);
    InlineLabel detail = createInlineLabel(text2, detailTextColor);
    MockComponentsUtil.setWidgetFontSize(detail, detailFontSize);
    MockComponentsUtil.setWidgetFontTypeface(this.editor, detail, detailTypeface);
    detail.getElement().getStyle().setTextAlign(TextAlign.CENTER);
    verticalItemPanel.add(main);
    verticalItemPanel.add(detail);
    verticalItemPanel.getElement().getStyle().setDisplay(Display.FLEX);
    verticalItemPanel.getElement().getStyle().setProperty("flexDirection", "column");
    verticalItemPanel.getElement().getStyle().setProperty("alignItems", "center");
    FlowPanel imageContainer = createImage(image, imageWidth + "px", imageHeight + "px");
    imageContainer.getElement().getStyle().setProperty("padding", "0px 0px 5px 0px");
    container.add(imageContainer);
    container.add(verticalItemPanel);
    container.getElement().getStyle().setDisplay(Display.FLEX);
    container.getElement().getStyle().setProperty("flexDirection", "column");
    container.getElement().getStyle().setProperty("justifyContent", "center");
    container.getElement().getStyle().setProperty("alignItems", "center");
    container.setWidth("100%");
    horizontalItemPanel.add(container);
    int height = Math.round(Float.parseFloat(mainFontSize) + Float.parseFloat(detailFontSize));
    itemHeight = imageHeight + height + 13;
    
    decorateWidget(horizontalItemPanel);
    listPanel.add(horizontalItemPanel);
  }

  private void setItemHeight(boolean detail, boolean image) {
    float mainHeight = Float.parseFloat(mainFontSize);
    float detailHeight = Float.parseFloat(detailFontSize);
    int height = Math.round(detail ? mainHeight + detailHeight : mainHeight);
    itemHeight = (image ? Math.max(imageHeight, height) : height) + 8;
  }

  /**
   * creates view of each row item in the designer according to the type of layout
   */
  private void createLabelItem(List<JSONObject> arrayList) {
    listPanel.clear();
    firstItem = true;
    for (JSONObject object : arrayList) {
      populateList(object);
    }
  }

  private void populateList(JSONObject object) {
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
    } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TOP_TWO_TEXT) {
      createImageTwoTextVerticalLayout(object);
    }
  }

  /**
   * retrieves an image from media to diaplay in designer
   *
   * @param imageName name of the image to be displayed for the row
   * @param widthValue width of the image
   * @param heightValue height of the image
   */
  private FlowPanel createImage(String imageName, String widthValue, String heightValue) {
    Image image = new Image();
    FlowPanel container = new FlowPanel();    
    container.setStylePrimaryName("listViewImageStyle");
    String url = convertImagePropertyValueToUrl(imageName);
    if (url == null) {
      // text was not recognized as an asset. Just display the icon for this type of component.
      image.setUrl(getIconImage().getUrl());
    } else {
      image.setUrl(url);
    }    
    container.setSize(widthValue, heightValue);
    image.setSize("100%", "100%");
    image.getElement().getStyle().setProperty("objectFit", "contain");
    image.getElement().getStyle().setProperty("objectPosition", "center");    
    container.add(image);
    return container;
}

  /**
   * creates label for a text element of a row in designer
   *
   * @param value text to be displayed
   * @param color color of the text
   */
  private InlineLabel createInlineLabel(String value, String color) {
    InlineLabel label = new InlineLabel(value.isEmpty() ? " ​" : value);
    MockComponentsUtil.setWidgetTextColor(label, color);
    label.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
    label.getElement().getStyle().setTextAlign(TextAlign.LEFT);    
    return label;
  }

  /** Sets the divider color, divider thickness, corner raius and margins for the given widget. */
  private void decorateWidget(Widget widget) {
    widget.setStylePrimaryName("listViewItemStyle");
    widget.setStyleName("listViewHorizontalItemStyle", horizontalOrientation);
    listPanel.setStyleName("listViewHorizontalScrollableStyle", horizontalOrientation);
    if (elementMargins > 0) {
      itemHeight += (elementMargins / 3) * 2;
      listPanel.getElement().getStyle().setPadding(elementMargins / 3, Unit.PX);
      if (!firstItem) {
        widget
            .getElement()
            .getStyle()
            .setProperty(
                horizontalOrientation ? "marginLeft" : "marginTop", elementMargins / 3 + "px");
      }
    } else {
      listPanel.getElement().getStyle().clearPadding();
      if (dividerThickness > 0) {
        if (!firstItem) {
          FlowPanel divider = new FlowPanel();
          divider.setStyleName("listViewHorizontalItemStyle", horizontalOrientation);
          if (horizontalOrientation) {
            divider.setSize(dividerThickness + "px", itemHeight + "px");
          } else {
            divider.setSize("100%", dividerThickness + "px");
          }
          MockComponentsUtil.setWidgetBackgroundColor(divider, dividerColor);
          listPanel.add(divider);
        }
      }
    }
    if (cornerRadius > 0) {
      widget.getElement().getStyle().setProperty("borderRadius", cornerRadius / 3 + "px");
    }
    MockComponentsUtil.setWidgetBackgroundColor(widget, elementColor);
    firstItem = false;
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
      textBoxWidget.setVisible(Boolean.parseBoolean(newValue));
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      textColor = newValue;
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_DETAILTEXTCOLOR)) {
      detailTextColor = newValue;
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_LAYOUT)) {
      updateLayoutType(newValue);
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_LISTVIEW_ADD_DATA)) {
      displayOnDesigner(newValue);
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_HINT)) {
      textBoxWidget.setText(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_ELEMENTCOLOR)) {
      setElementColorProperty(newValue);
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_DIVIDERCOLOR)) {
      setDividerColorProperty(newValue);
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_DIVIDERTHICKNESS)) {
      dividerThickness = Integer.valueOf(newValue);
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_ELEMENTCORNERRADIUS)) {
      cornerRadius = Integer.valueOf(newValue);
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_ELEMENTMARGINSWIDTH)) {
      elementMargins = Integer.valueOf(newValue);
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_ORIENTATION)) {
      horizontalOrientation = newValue.equals("1");
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_FONTSIZE)) {
      mainFontSize = newValue;
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_FONTSIZEDETAIL)) {
      detailFontSize = newValue;
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_FONTTYPEFACE)) {
      mainTypeface = newValue;
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_FONTTYPEFACEDETAIL)) {
      detailTypeface = newValue;
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_IMAGEHEIGHT)) {
      imageHeight = Integer.valueOf(newValue) / 5;
      refreshElements();
    } else if (propertyName.equals(PROPERTY_NAME_IMAGEWIDTH)) {
      imageWidth = Integer.valueOf(newValue) / 5;
      refreshElements();
    }
  }

  private void refreshElements() {
    if (currentList != null) {
      setElementsFromStringProperty(currentElements);
    }
    if (!currentItems.isEmpty()) {
      createLabelItem(currentItems);
    }
    refreshForm();
  }
}
