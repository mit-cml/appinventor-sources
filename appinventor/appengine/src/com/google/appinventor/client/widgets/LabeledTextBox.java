// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A TextBox with a leading label.
 *
 */
public class LabeledTextBox extends Composite {

  // Backing TextBox
  private final TextBox textbox;

  private String defaultTextBoxColor;
  private String errorMessage = "";
  private Label errorLabel;
  private Label captionLabel;
  private Validator validator;

  /**
   * Creates a new TextBox with the given leading caption.
   *
   * @param caption  caption for leading label
   */
  public LabeledTextBox() {
    this("Placedholder");
  }

  public LabeledTextBox(String caption) {
    HorizontalPanel panel = new HorizontalPanel();
    captionLabel = new Label(caption);
    panel.add(captionLabel);
    panel.setCellVerticalAlignment(captionLabel, HasVerticalAlignment.ALIGN_MIDDLE);
    textbox = new TextBox();
    textbox.setStylePrimaryName("ode-LabeledTextBox");
    panel.add(textbox);
    panel.setCellWidth(captionLabel, "45%");
    panel.setCellVerticalAlignment(textbox, HasVerticalAlignment.ALIGN_MIDDLE);
    VerticalPanel vp = new VerticalPanel();
    vp.add(panel);

    initWidget(vp);

    setWidth("100%");
  }

  /**
   * Use this TextBox if you want to have text validation while a user is typing
   *
   * @param caption    caption for leading label
   * @param validator  The validator to use for a specific textBox
   */
  public LabeledTextBox(String caption, Validator validator) {
    this(caption);
    setValidator(validator);
    setWidth("100%");
  }

  public void setCaption(String caption) {
    captionLabel.setText(caption);
  }

  /**
   * Sets the content of the TextBox.
   *
   * @param text  new TextBox content
   */
  public void setText(String text) {
    textbox.setText(text);
  }

  public void setValidator(Validator validator) {
    this.validator = validator;
    if (errorLabel == null) {
      defaultTextBoxColor = textbox.getElement().getStyle().getBorderColor();
      HorizontalPanel errorPanel = new HorizontalPanel();
      errorLabel = new Label("");
      errorPanel.add(errorLabel);
      VerticalPanel vp = (VerticalPanel) getWidget();
      vp.add(errorPanel);
    }
  }
  /**
   * Returns the current content of the TextBox.
   *
   * @return  current TextBox content
   */
  public String getText() {
    return textbox.getText();
  }

  /**
   * Explicitly focus/unfocus this widget. Only one widget can have focus at a
   * time, and the widget that does will receive all keyboard events.
   *
   * @param focused  whether this widget should take focus or release it
   */
  public void setFocus(boolean focused) {
    textbox.setFocus(focused);
  }

  /**
   *  Selects all of the text in the TextBox.
   */
  public void selectAll() {
    textbox.selectAll();
  }

  /**
   * Sets whether the textbox is enabled.
   *
   * @param enabled  {@code true} to enable the textbox, {@code false} to
   *                 disable it
   */
  public void setEnabled(boolean enabled) {
    textbox.setEnabled(enabled);
  }

  /**
   * Returns the TextBox.
   *
   * @return  the TextBox
   */
  public TextBox getTextBox() {
    return textbox;
  }

  /**
   * Returns the error message resulting from a specific validation error
   *
   * @return errorMessage
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Set the specific error message for invalid text in a textbox.
   *
   * @param errorMessage to use for textBox
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public void setColor(String color) {
    textbox.getElement().getStyle().setBorderColor(color);
  }

  /**
   * Check to see if a textbox contains valid text. True if text is valid, false otherwise
   *
   * @return validationResult
   */
  public boolean validate() {
    boolean validationResult = validator.validate(getTextBox().getValue());
    setErrorMessage(validator.getErrorMessage());
    setErrorStyles(validationResult);
    return validationResult;
  }

  /**
   * Set the style of the textbox depending on whether the text is currently valid or not
   *
   * @param validationResult
   */
  private void setErrorStyles(boolean validationResult) {
    if (validationResult) {
      if (errorMessage.length() > 0) { // handling warnings
        String warningColor = "yellow";
        textbox.getElement().getStyle().setBorderColor(warningColor);
        errorLabel.setText(errorMessage);
      } else {
        textbox.getElement().getStyle().setBorderColor(defaultTextBoxColor);
        errorLabel.setText("");
      }
    } else {
      String errorColor = "red";
      textbox.getElement().getStyle().setBorderColor(errorColor);
      errorLabel.setText(errorMessage);
    }
  }
}
