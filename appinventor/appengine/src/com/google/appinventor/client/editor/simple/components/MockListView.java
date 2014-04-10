// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
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
  private final VerticalPanel sliderWidget;

  // GWT widget used to mock a Simple TextBox
  private final TextBox textBoxWidget;
  private InlineLabel labelWidget;
  SimplePanel panelWidget;
  String[] currentList;

  /**
   * Creates a new MockListView component. I place a label inside a simplepanel which is then placed into a vertical panel
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockListView(SimpleEditor editor) {
    super(editor, TYPE, images.listview());
    textBoxWidget = new TextBox();
    sliderWidget = new VerticalPanel();
    sliderWidget.setSpacing(1);
    sliderWidget.setSize("100%", "100%");
    sliderWidget.setStylePrimaryName("ode-SimpleMockComponent");
    labelWidget=new InlineLabel();
    labelWidget.setSize("100%", "100%");
    panelWidget=new SimplePanel();
    panelWidget.setStyleName("listBoxItemStyle");
    panelWidget.setSize("100%", "100%");
    panelWidget.add(labelWidget);
    sliderWidget.add(panelWidget);
    

    initComponent(sliderWidget);
    MockComponentsUtil.setWidgetBackgroundColor(sliderWidget, "&HFF000000");
    MockComponentsUtil.setWidgetBackgroundColor(labelWidget, "&HFF000000");
    MockComponentsUtil.setWidgetTextColor(labelWidget, "&HFFFFFFFF");
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
  private void setFilterShowBox(String value){
    if(Boolean.parseBoolean(value)){
      textBoxWidget.setSize("100%", "100%");
      sliderWidget.clear();
      sliderWidget.add(textBoxWidget);
    for(int i=0; i<currentList.length; i++){
      labelWidget=new InlineLabel(currentList[i]);
      labelWidget.setSize("100%", "100%");
      MockComponentsUtil.setWidgetBackgroundColor(labelWidget, "&HFF000000");
      MockComponentsUtil.setWidgetTextColor(labelWidget, "&HFFFFFFFF");
      panelWidget=new SimplePanel();
      panelWidget.setStyleName("listBoxItemStyle");
      panelWidget.setSize("100%", "100%");
      panelWidget.add(labelWidget);
      sliderWidget.add(panelWidget);
    }
  }
  else{
    sliderWidget.clear();
    for(int i=0; i<currentList.length; i++){
      labelWidget=new InlineLabel(currentList[i]);
      labelWidget.setSize("100%", "100%");
      MockComponentsUtil.setWidgetBackgroundColor(labelWidget, "&HFF000000");
      MockComponentsUtil.setWidgetTextColor(labelWidget, "&HFFFFFFFF");
      panelWidget=new SimplePanel();
      panelWidget.setStyleName("listBoxItemStyle");
      panelWidget.setSize("100%", "100%");
      panelWidget.add(labelWidget);
      sliderWidget.add(panelWidget);
    }
  }
  }
  

  /*
   * Sets the text to be added in the listview
   */
  private void setElementsFromStringProperty (String text){
    currentList=text.split(",");
    sliderWidget.clear();
    for(int i=0; i<currentList.length; i++){
      labelWidget=new InlineLabel(currentList[i]);
      labelWidget.setSize("100%", "100%");
      MockComponentsUtil.setWidgetBackgroundColor(labelWidget, "&HFF000000");
      MockComponentsUtil.setWidgetTextColor(labelWidget, "&HFFFFFFFF");
      panelWidget=new SimplePanel();
      panelWidget.setStyleName("listBoxItemStyle");
      panelWidget.setSize("100%", "100%");
      panelWidget.add(labelWidget);
      sliderWidget.add(panelWidget);
    }

}

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_LIST)) {
      setElementsFromStringProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_FILTER_BAR)) {
      setFilterShowBox(newValue);
      refreshForm();
    }
  }
}