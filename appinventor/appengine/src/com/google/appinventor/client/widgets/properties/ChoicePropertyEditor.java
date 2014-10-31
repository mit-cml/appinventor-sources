// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Property editor for a list of values.
 *
 */
public class ChoicePropertyEditor extends PropertyEditor implements ChangeHandler {

  /**
   * Caption/value pair for display in {@link ChoicePropertyEditor}.
   */
  public static class Choice {

    // Caption/value pair
    private final String caption;
    private final String value;

    /**
     * Creates a new caption/value pair.
     *
     * @param caption  text to be shown in choice editor list box
     * @param value  value to assign to property if choice is selected
     */
    public Choice(String caption, String value) {
      this.caption = caption;
      this.value = value;
    }

    /**
     * Extracts the captions from an array of choices.
     *
     * @param choices  array to extract captions from
     * @return  array with extracted captions
     */
    public static String[] captions(Choice[] choices) {
      String[] captions = new String[choices.length];
      for (int i = 0; i < choices.length; i++) {
        captions[i] = choices[i].caption;
      }
      return captions;
    }

    /**
     * Extracts the values from an array of choices.
     *
     * @param choices  array to extract values from
     * @return  array with extracted values
     */
    public static String[] values(Choice[] choices) {
      String[] values = new String[choices.length];
      for (int i = 0; i < choices.length; i++) {
        values[i] = choices[i].value;
      }
      return values;
    }
  }

  // UI for the list of values will be represented by a ListBox
  private final ListBox listbox;

  // Array of choices
  private Choice[] choices;

  /**
   * Creates a new instance of the property editor.
   *
   * @param choices  array of values to choose from
   */
  public ChoicePropertyEditor(Choice[] choices) {
    // Initialize UI
    listbox = new ListBox();
    listbox.setVisibleItemCount(1);
    listbox.addChangeHandler(this);

    initWidget(listbox);

    updateChoices(choices);
  }

  /**
   * Updates the list of available choices.
   *
   * @param choices  list of values to choose from
   */
  public void updateChoices(Choice[] choices) {
    this.choices = choices;

    // Propagate values into listbox
    listbox.clear();
    for (Choice choice : choices) {
      listbox.addItem(choice.caption);
    }
  }

  @Override
  protected void updateValue() {
    String propertyValue = property.getValue();
    for (int i = 0; i < choices.length; i++) {
      String choiceValue = choices[i].value;
      if (choiceValue.equals(propertyValue)) {
        listbox.setItemSelected(i, true);
      }
    }
  }
  
  /**
   * Enables the dropdown selector for this property
   */ 
  public void enable() {
    listbox.setEnabled(true);
  }
  
  /**
   * Disables the dropdown selector for this property
   */ 
  public void disable() {
    listbox.setEnabled(false);
  }

  // ChangeHandler implementation

  @Override
  public void onChange(ChangeEvent event) {
    property.setValue(choices[listbox.getSelectedIndex()].value);
  }
}
