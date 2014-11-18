// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.InlineLabel;

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

  private static final String DEFAULT_BACKGROUND_COLOR = "&HFF000000";

  private static final String DEFAULT_TEXT_COLOR = "&HFFFFFFFF";

  //  Needed for background color of labelInItem
  private String backgroundColor;
  private String textColor;
  private String currentElements;

  /**
   * Creates a new MockListView component. It places a label inside a simplepanel which
   * is then placed into a vertical panel
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockListView(SimpleEditor editor) {
    super(editor, TYPE, images.listview());
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

  /*
   * Sets the text to be added in the listview
   */
  private void setElementsFromStringProperty(String text){
    currentElements = text;
    currentList = text.split(",");

    listViewWidget.clear();
    createFilterBox();

    if (filterShowing) {
      textBoxWidget.setVisible(true);
    }
    else {
      textBoxWidget.setVisible(false);
    }

    for(int i=0; i<currentList.length; i++){
      createLabelItem(i);
      createLabelPanel();
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
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      textColor = newValue;
      if (currentList != null) {
        setElementsFromStringProperty(currentElements);
      }
      refreshForm();
    }
  }
}
