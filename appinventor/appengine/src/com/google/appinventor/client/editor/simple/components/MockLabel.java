// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.properties.EditableProperty;
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

  // Property name for opacity
  protected static final String PROPERTY_NAME_OPACITY = "Opacity";

  // GWT label widget used to mock a Simple Label
  private InlineHTML labelWidget;

  private String savedText = "";     // Saved text, so if we change from
                                     // text to/from html we have the text
                                     // to set

  private float opacity = 1f; // Current opacity

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

    // Update alpha value; The String format is in &HAARRGGBB, so we extract
    // the alpha portion and update the opacity property with it.
    //String alphaValue = Integer.toString(Integer.parseInt(text.substring(2, 4), 16));
    //float alphaFloat = Integer.parseInt(alphaValue) / 255f; // Convert to [0, 1] ranged value
    //properties.changePropertyValue(PROPERTY_NAME_OPACITY, Float.toString(alphaFloat));

    // Update background color with regards to the current color and the set opacity color.
    MockComponentsUtil.setWidgetBackgroundColor(labelWidget, text, opacity);
  }

  /*
   * Sets the labels' opacity property to a new value.
   */
  private void setOpacityProperty(String text) {
    this.opacity = Float.parseFloat(text);

    // Update background color to respond to current opacity
    String backgroundColor = properties.getPropertyValue(PROPERTY_NAME_BACKGROUNDCOLOR);
    MockComponentsUtil.setWidgetBackgroundColor(labelWidget, backgroundColor, opacity);

    // Update text color to respond to current opacity
    if (properties.hasProperty(PROPERTY_NAME_TEXTCOLOR)) {
      String textColor = properties.getPropertyValue(PROPERTY_NAME_TEXTCOLOR);
      MockComponentsUtil.setWidgetTextColor(labelWidget, textColor, opacity);
    }

    // TODO: Previous solution; To be removed/adapted.
    /*int newOpacity = Integer.parseInt(text);

    // Update opacity only if the value is a new value.
    // This is required to prevent infinite recursion when
    // updating the background color property (which could again update the
    // opacity property, and so on).
    if (newOpacity != this.opacity) {
      this.opacity = newOpacity;

      // Generate new string for the background
      String newValue = "&H" + MockComponentsUtil.convertToHex(newOpacity, 2)
          + properties.getProperty(PROPERTY_NAME_BACKGROUNDCOLOR).getValue().substring(4);

      // Change property of the background color value to take the opacity into account.
      // This also changes the widget background color in the callback of changing
      // the background color. The reason it is done this way is to also update
      // the color picker value in Designer to be representative.
      properties.changePropertyValue(PROPERTY_NAME_BACKGROUNDCOLOR, newValue);
    }*/
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
      String sanitizedText = SimpleHtmlSanitizer.sanitizeHtml(text).asString();
      if (sanitizedText != null) {
        sanitizedText = sanitizedText.replaceAll("\\\\n", " ");
      }
      labelWidget.setHTML(sanitizedText);
    } else {
      labelWidget.setHTML(text.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
          .replaceAll(">", "&gt;").replaceAll("\\\\n", "<br>")
      );
    }
  }

  /*
   * Sets the label's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(labelWidget, text, opacity);
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
    } else if (propertyName.equals(PROPERTY_NAME_OPACITY)) {
      setOpacityProperty(newValue);
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
