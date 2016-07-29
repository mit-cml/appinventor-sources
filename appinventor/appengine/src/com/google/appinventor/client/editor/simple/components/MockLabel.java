// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * Mock Label component.
 *
 */
public final class MockLabel extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "Label";

  // GWT label widget used to mock a Simple Label
  private InlineHTML labelWidget;

  private String savedText;     // Saved text, so if we change from
                                // text to/from html we have the text
                                // to set

  /**
   * Creates a new MockLabel component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockLabel(SimpleEditor editor) {
    super(editor, TYPE, images.label());

    // Initialize mock label UI
    labelWidget = new InlineHTML();
    labelWidget.setStylePrimaryName("ode-SimpleMockComponent");
    initComponent(labelWidget);
  }

  @Override
  public void onCreateFromPalette() {
    // Change label text to component name
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the label's TextAlignment property to a new value.
   */
  private void setTextAlignmentProperty(String text) {
    MockComponentsUtil.setWidgetTextAlign(labelWidget, text);
  }

  /*
   * Sets the label's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(labelWidget, text);
  }

  /*
   * Sets the label's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(labelWidget, text);
  }

  /*
   * Sets the label's FontItalic property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(labelWidget, text);
  }

  /*
   * Sets the label's FontSize property to a new value.
   */
  private void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(labelWidget, text);
  }

  /*
   * Sets the label's FontTypeface property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(labelWidget, text);
  }

  /*
   * Sets the label's Text property to a new value.
   */
  private void setTextProperty(String text) {
    savedText = text;
    if (getPropertyValue(PROPERTY_NAME_HTMLFORMAT).equals("True")) {
      labelWidget.setHTML(SimpleHtmlSanitizer.sanitizeHtml(text).asString());
    } else {
      labelWidget.setText(text);
    }
  }

  /*
   * Sets the label's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(labelWidget, text);
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_TEXTALIGNMENT)) {
      setTextAlignmentProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_FONTBOLD)) {
      setFontBoldProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_FONTITALIC)) {
      setFontItalicProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_FONTSIZE)) {
      setFontSizeProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_FONTTYPEFACE)) {
      setFontTypefaceProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXT)) {
      setTextProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      setTextColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_HTMLFORMAT)) {
      // Just need to re-set the saved text so it is displayed
      // either as HTML or text as appropriate
      setTextProperty(savedText);
      refreshForm();
    }
  }
}
