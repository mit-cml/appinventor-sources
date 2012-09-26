// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A TextBox with a leading label.
 *
 */
public class LabeledTextBox extends Composite {

  // Backing TextBox
  private final TextBox textbox;

  /**
   * Creates a new TextBox with the given leading caption.
   *
   * @param caption  caption for leading label
   */
  public LabeledTextBox(String caption) {
    HorizontalPanel panel = new HorizontalPanel();
    Label label = new Label(caption);
    panel.add(label);
    textbox = new TextBox();
    textbox.setWidth("100%");
    panel.add(textbox);
    panel.setCellWidth(label, "40%");

    initWidget(panel);

    setWidth("100%");
  }

  /**
   * Sets the content of the TextBox.
   *
   * @param text  new TextBox content
   */
  public void setText(String text) {
    textbox.setText(text);
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
}
