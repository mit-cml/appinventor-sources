// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.utils.Promise;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Command;

import java.util.List;
import java.util.Objects;

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
  private DropDownButton dropDownButton;

  // Array of choices
  private Choice[] choices;

  /**
   * Creates a new instance of the property editor.
   *
   * @param choices  array of values to choose from
   */
  public ChoicePropertyEditor(Choice[] choices) {
    this(choices, null, false);
  }

  /**
   * Creates a new instance of the property editor with a default value.
   *
   * @param choices  array of values to choose from
   * @param defaultValue  the default value, possibly null
   * @param autoupdate  whether to update the property value automatically
   */
  public ChoicePropertyEditor(Choice[] choices, String defaultValue, boolean autoupdate) {
    // Initialize UI
    this.choices = choices;
    List<DropDownItem> items = Lists.newArrayList();
    Choice selected = choices[0];
    for (final Choice choice : choices) {
      items.add(new DropDownItem("Choice Property Editor", choice.caption, new Command() {
        @Override
        public void execute() {
          boolean multiple = isMultipleValues();
          setMultipleValues(false);
          property.setValue(choice.value, multiple);
          if (autoupdate) {
            updateValue();
          }
          dropDownButton.setFocus(true);
        }
      }));
      if (Objects.equals(choice.value, defaultValue)) {
        selected = choice;
      }
    }
    dropDownButton = new DropDownButton("Choice Property Editor", selected.caption, items, false);
    dropDownButton.setStylePrimaryName("ode-ChoicePropertyEditor");

    initWidget(dropDownButton);
  }

  /**
   * Creates a new instance of the property editor.
   *
   * @param choicesPromise A promise that resolves to an array of choices
   */
  public ChoicePropertyEditor(Promise<Choice[]> choicePromise) {
    // We are asynchronous. We won't have real value here until we get an answer
    // from the Chatbot Proxy. So we need to not NPF or otherwise die, so we
    // start with an empty list.
    this.choices = new Choice[0];
    dropDownButton = new DropDownButton("Choice Property Editor", "", Lists.newArrayList(), false);
    dropDownButton.setStylePrimaryName("ode-ChoicePropertyEditor");
    initWidget(dropDownButton);

    choicePromise.then(choices -> {
        this.choices = choices;
        for (final Choice choice : choices) {
          dropDownButton.addItem(
            new DropDownItem("Choice Property Editor", choice.caption, new Command() {
              @Override
              public void execute() {
                boolean multiple = isMultipleValues();
                setMultipleValues(false);
                property.setValue(choice.value, multiple);
              }
              }));
        }
        updateValue();
        return null;
      }).error(caught -> {
          Throwable original = caught.getOriginal();
          ErrorReporter.reportError("Error fetching Chatbot providers/models: " + original.getMessage());
          Ode.CLog("Error fetching choices" + original.getMessage());
          return null;
        });
}

  /**
   * Creates a new instance of the property editor with choice names.
   * Each choice name is treated as both the caption and the value of a choice.
   *
   * @param choiceNames  array of choice names to choose from
   */
  public ChoicePropertyEditor(String[] choiceNames) {
    this.choices = new Choice[choiceNames.length];
    for (int idx = 0; idx < choiceNames.length; idx += 1)
      this.choices[idx] = new Choice(choiceNames[idx], choiceNames[idx]);

    List<DropDownItem> items = Lists.newArrayList();
    for(final Choice choice : choices) {
      items.add(new DropDownItem("Choice Property Editor", choice.caption, new Command() {
        @Override
        public void execute() {
          boolean multiple = isMultipleValues();
          setMultipleValues(false);
          property.setValue(choice.value, multiple);
          dropDownButton.setFocus(true);
        }
      }));
    }
    dropDownButton = new DropDownButton("Choice Property Editor", choices[0].caption, items, false);
    dropDownButton.setStylePrimaryName("ode-ChoicePropertyEditor");

    initWidget(dropDownButton);
  }

  @Override
  protected void updateValue() {
    if (choices == null) {
      return;
    }
    if (isMultipleValues()) {
      dropDownButton.setCaption(MESSAGES.multipleValues());
      return;
    }

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
