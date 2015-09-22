// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.widgets.DropDownButton;
import static com.google.appinventor.client.widgets.DropDownButton.DropDownItem;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.Command;

import java.util.List;

/**
 * Property editor for a list of values.
 *
 */
public class ChoicePropertyEditor extends PropertyEditor {

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

  // UI for the drop-down list of values will be represented by a DropDownButton
  private final DropDownButton dropDownButton;

  // Array of choices
  private Choice[] choices;

  /**
   * Creates a new instance of the property editor.
   *
   * @param choices  array of values to choose from
   */
  public ChoicePropertyEditor(Choice[] choices) {
    // Initialize UI
    this.choices = choices;
    List<DropDownItem> items = Lists.newArrayList();
    for(final Choice choice : choices) {
      items.add(new DropDownItem("Choice Property Editor", choice.caption, new Command() {
        @Override
        public void execute() {
          property.setValue(choice.value);
        }
      }));
    }
    dropDownButton = new DropDownButton("Choice Property Editor", choices[0].caption, items, false);
    dropDownButton.setStylePrimaryName("ode-ChoicePropertyEditor");

    initWidget(dropDownButton);
  }

  @Override
  protected void updateValue() {
    String propertyValue = property.getValue();
    for (Choice choice : choices) {
      String choiceValue = choice.value;
      if (choiceValue.equals(propertyValue)) {
        dropDownButton.setCaption(choice.caption);
      }
    }
  }
  
  /**
   * Enables the dropdown selector for this property
   */
  public void enable() {
    for (Choice c : choices) {
      dropDownButton.setItemEnabled(c.caption, true);
    }
  }

  /**
   * Disables the dropdown selector for this property
   */
  public void disable() {
    String currentValue = property.getValue();
    for (Choice c : choices) {
      if(!c.value.equals(currentValue)) {
        dropDownButton.setItemEnabled(c.caption, false);
      } else {
        dropDownButton.setItemEnabled(c.caption, true);
      }
    }
  }

  public void setVisible(boolean visible) {
    dropDownButton.setVisible(visible);
  }
}
