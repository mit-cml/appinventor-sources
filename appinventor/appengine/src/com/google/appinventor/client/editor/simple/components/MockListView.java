// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
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

  /**
   * Creates a new MockListView component. I place a label inside a simplepanel which is then placed into a vertical panel
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

    initComponent(listViewWidget);
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidget, "&HFF000000");
  }

  @Override
  public void onCreateFromPalette() {
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /**
   * This function is called when the show filter box is checked or unchecked.
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
  private void setElementsFromStringProperty (String text){
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
    MockComponentsUtil.setWidgetBackgroundColor(labelInItem, "&HFF000000");
    MockComponentsUtil.setWidgetTextColor(labelInItem, "&HFFFFFFFF");
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
    }
  }
}
