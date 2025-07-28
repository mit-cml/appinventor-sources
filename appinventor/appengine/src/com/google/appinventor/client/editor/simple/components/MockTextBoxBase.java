// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.designer.DesignerChangeListener;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * Abstract superclass for textbox based mock components.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class MockTextBoxBase extends MockWrapper implements DesignerChangeListener {
  protected static final String PROPERTY_NAME_HINTCOLOR = "HintColor";

  // GWT widget used to mock a Simple TextBox
  private final TextBox textBoxWidget;

  /**
   * Creates a new MockTextBox component.
   *
   * @param editor  editor of source file the component belongs to
   */
  MockTextBoxBase(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, icon);

    // Initialize mock textbox UI
    textBoxWidget = new TextBox();
    initWrapper(textBoxWidget);
  }

  @Override
  protected void onAttach() {
        super.onAttach();
        ((YaFormEditor) editor).getForm().addDesignerChangeListener(this);
  }
  @Override
  protected void onDetach() {
        super.onDetach();
        ((YaFormEditor) editor).getForm().removeDesignerChangeListener(this);
  }
  /**
   * Class that extends TextBox so we can use a protected constructor.
   *
   * <p/>The purpose of this class is to create a clone of the TextBox passed to
   * the constructor. It will be used to determine the preferred size of the
   * TextBox, without having the size constrained by its parent, since the
   * cloned TextBox won't have a parent.
   */
  static class ClonedTextBox extends TextBox {
    ClonedTextBox(TextBox tb) {
      // Get the Element from the TextBox.
      // Call DOM.clone to make a deep clone of that element.
      // Pass that cloned element to the super constructor.
      super(DOM.clone(tb.getElement(), true));
    }
  }

  @Override
  protected Widget createClonedWidget() {
    return new ClonedTextBox(textBoxWidget);
  }

  @Override
  public void onCreateFromPalette() {
    // Do not set text; instead set hint (makes more sense for this component)
    // Change textbox hint to component name
    changeProperty(PROPERTY_NAME_HINT, MESSAGES.hintPropertyValue(getName()));
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.TEXTBOX_PREFERRED_WIDTH;
  }

  /*
   * Sets the textbox's TextAlignment property to a new value.
   */
  private void setTextAlignmentProperty(String text) {
    MockComponentsUtil.setWidgetTextAlign(textBoxWidget, text);
  }

  /*
   * Sets the textbox's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      MockForm form = ((YaFormEditor) editor).getForm();
      if (form != null && form.getPropertyValue("HighContrast").equals("True")) {
        text = "&HFF000000";  //black
      } else {
        text = "&HFFFFFFFF";  // white
      }
    }
    MockComponentsUtil.setWidgetBackgroundColor(textBoxWidget, text);
  }

  /*
   * Sets the textbox's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the textbox's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(textBoxWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the textbox's FontItalic property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(textBoxWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the textbox's FontSize property to a new value.
   */
  private void setFontSizeProperty(String text) {
    float convertedText = Float.parseFloat(text);
    MockForm form = ((YaFormEditor) editor).getForm();
    if (convertedText == 14.0 && form != null
        && form.getPropertyValue("BigDefaultText").equals("True")) {
      MockComponentsUtil.setWidgetFontSize(textBoxWidget, "24");
    } else {
      MockComponentsUtil.setWidgetFontSize(textBoxWidget, text);
    }
    updatePreferredSize();
  }

  /*
   * Sets the textbox's FontTypeface property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(this.editor, textBoxWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the textbox's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      MockForm form = ((YaFormEditor) editor).getForm();
      if (form != null && form.getPropertyValue("HighContrast").equals("True")) {
        text = "&HFFFFFFFF";  // white
      } else {
        text = "&HFF000000";  //black

      }

    }

    MockComponentsUtil.setWidgetTextColor(textBoxWidget, text);

  }

  /**
   * Updates the appearance of the textbox text based on the hint, text, and color properties.
   */
  private void updateAppearance() {
    if (hasProperty(PROPERTY_NAME_TEXT) && !getPropertyValue(PROPERTY_NAME_TEXT).isEmpty()) {
      textBoxWidget.setText(getPropertyValue(PROPERTY_NAME_TEXT));
      if (hasProperty(PROPERTY_NAME_TEXTCOLOR)) {
        setTextColorProperty(getPropertyValue(PROPERTY_NAME_TEXTCOLOR));
      }
    } else if (hasProperty(PROPERTY_NAME_HINT)) {
      textBoxWidget.setText(getPropertyValue(PROPERTY_NAME_HINT));
      if (hasProperty(PROPERTY_NAME_HINTCOLOR)) {
        MockComponentsUtil.setWidgetTextColor(textBoxWidget,
            getPropertyValue(PROPERTY_NAME_HINTCOLOR));
      }
    } else {
      textBoxWidget.setText("");
      MockComponentsUtil.setWidgetTextColor(textBoxWidget, "&HFF000000");
    }
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
    } else if (propertyName.equals(PROPERTY_NAME_ENABLED)) {
      setEnabledProperty(newValue);
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
    } else if (propertyName.equals(PROPERTY_NAME_HINT)) {
      updateAppearance();
      updatePreferredSize();
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_HINTCOLOR)) {
      updateAppearance();
    } else if (propertyName.equals(PROPERTY_NAME_TEXT)) {
      updateAppearance();
      updatePreferredSize();
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      setTextColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_WIDTH)) {
      MockComponentsUtil.updateTextAppearances(textBoxWidget, newValue);
      refreshForm();
    }
  }

  @Override
  public void onComponentPropertyChanged(MockComponent component, String propertyName, String propertyValue) {
    if (component.getType().equals(MockForm.TYPE) && propertyName.equals("HighContrast")) {
      setBackgroundColorProperty(getPropertyValue(PROPERTY_NAME_BACKGROUNDCOLOR));
      setTextColorProperty(getPropertyValue(PROPERTY_NAME_TEXTCOLOR));
      //setFontSizeProperty(getPropertyValue(PROPERTY_NAME_FONTSIZE));
      updatePreferredSize();
      refreshForm();
    }
    else if (component.getType().equals(MockForm.TYPE) && propertyName.equals("BigDefaultText")) {
      setFontSizeProperty(getPropertyValue(PROPERTY_NAME_FONTSIZE));
      updatePreferredSize();
      refreshForm();
    }
  }

  @Override
  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {

  }

  @Override
  public void onComponentAdded(MockComponent component) {

  }

  @Override
  public void onComponentRenamed(MockComponent component, String oldName) {

  }

  @Override
  public void onComponentSelectionChange(MockComponent component, boolean selected) {

  }
}
