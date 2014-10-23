// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Abstract superclass for button based mock components.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class MockButtonBase extends MockVisibleComponent {
  // Property names
  private static final String PROPERTY_NAME_IMAGE = "Image";

  // GWT widget used to mock a Simple Button
  private final Button buttonWidget;
  private int[] preferredSizeOfButton;
  private final Image image;
  private String imagePropValue;
  private boolean hasImage;

  // We need to maintain these so we can show color and shape only when
  // there is no image.
  private String backgroundColor;
  // Legal values for shape are defined in
  // com.google.appinventor.components.runtime.Component.java.
  private int shape;

  /**
   * Creates a new MockButtonBase component.
   *
   * @param editor  editor of source file the component belongs to
   */
  MockButtonBase(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, icon);

    // Initialize mock button UI
    buttonWidget = new Button();
    buttonWidget.addStyleName("ode-SimpleMockButton");
    image = new Image();
    image.addErrorHandler(new ErrorHandler() {
      @Override
      public void onError(ErrorEvent event) {
        if (imagePropValue != null && !imagePropValue.isEmpty()) {
          OdeLog.elog("Error occurred while loading image " + imagePropValue);
        }
        refreshForm();
      }
    });
    image.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent event) {
        refreshForm();
      }
    });
    DeckPanel deckPanel = new DeckPanel();
    deckPanel.setStylePrimaryName("ode-SimpleMockComponent");
    deckPanel.add(buttonWidget);
    deckPanel.add(image);
    deckPanel.showWidget(0);
    initComponent(deckPanel);
  }

  /**
   * Class that extends Button so we can use a protected constructor.
   *
   * <p/>The purpose of this class is to create a clone of the Button passed to
   * the constructor. It will be used to determine the preferred size of the
   * Button, without having the size constrained by its parent, since the
   * cloned Button won't have a parent.
   */
  static class ClonedButton extends Button {
    ClonedButton(Button b) {
      // Get the Element from the Button.
      // Call DOM.clone to make a deep clone of that element.
      // Pass that cloned element to the super constructor.
      super(DOM.clone(b.getElement(), true)); // true for a deep clone
    }
  }

  private Button createClonedButton() {
    return new ClonedButton(buttonWidget);
  }

  @Override
  public void onCreateFromPalette() {
    // Change button caption to component name
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the button's TextAlignment property to a new value.
   */
  private void setTextAlignmentProperty(String text) {
    MockComponentsUtil.setWidgetTextAlign(buttonWidget, text);
  }

  /*
   * Sets the button's Shape property to a new value.
   */
  private void setShapeProperty(String text) {
    shape = Integer.parseInt(text);
    // Android Buttons with images take the shape of the image and do not
    // use one of the defined Shapes.
    if (hasImage) {
      return;
    }
    switch(shape) {
      case 0:
        // Default Button
        DOM.setStyleAttribute(buttonWidget.getElement(), "borderRadius", "0px");
        break;
      case 1:
        // Rounded Button.
        // The corners of the Button are rounded by 10 px.
        // The value 10 px was chosen strictly for style.
        // 10 px is the same as ROUNDED_CORNERS_RADIUS defined in
        // com.google.appinventor.components.runtime.ButtonBase.
        DOM.setStyleAttribute(buttonWidget.getElement(), "borderRadius", "10px");
        break;
      case 2:
        // Rectangular Button
        DOM.setStyleAttribute(buttonWidget.getElement(), "borderRadius", "0px");
        break;
      case 3:
        // Oval Button
        String height = DOM.getStyleAttribute(buttonWidget.getElement(), "height");
        DOM.setStyleAttribute(buttonWidget.getElement(), "borderRadius", height);
        break;
      default:
        // This should never happen
        throw new IllegalArgumentException("shape:" + shape);
    }
  }

  /*
   * Sets the button's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    backgroundColor = text;
    // Android Buttons do not show a background color if they have an image.
    if (hasImage) {
      return;
    }
    if (MockComponentsUtil.isDefaultColor(text)) {
      // CSS background-color for ode-SimpleMockButton (copied from Ya.css)
      text = "&HFFE8E8E8";
    }
    MockComponentsUtil.setWidgetBackgroundColor(buttonWidget, text);
  }

  /*
   * Sets the button's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the button's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(buttonWidget, text);
    updatePreferredSizeOfButton();
  }

  /*
   * Sets the button's FontItalic property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(buttonWidget, text);
    updatePreferredSizeOfButton();
  }

  /*
   * Sets the button's FontSize property to a new value.
   */
  private void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(buttonWidget, text);
    updatePreferredSizeOfButton();
  }

  /*
   * Sets the button's FontTypeface property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(buttonWidget, text);
    updatePreferredSizeOfButton();
  }

  /*
   * Sets the button's Image property to a new value.
   */
  private void setImageProperty(String text) {
    imagePropValue = text;
    String url = convertImagePropertyValueToUrl(text);
    if (url == null) {
      hasImage = false;
      url = "";
      setBackgroundColorProperty(backgroundColor);
      setShapeProperty(Integer.toString(shape));
    } else {
      hasImage = true;
      // Android Buttons do not show a background color if they have an image.
      // The container's background color shows through any transparent
      // portions of the Image, an effect we can get in the browser by
      // setting the widget's background color to COLOR_NONE.
      MockComponentsUtil.setWidgetBackgroundColor(buttonWidget,
          "&H" + COLOR_NONE);
      DOM.setStyleAttribute(buttonWidget.getElement(), "borderRadius", "0px");
    }
    MockComponentsUtil.setWidgetBackgroundImage(buttonWidget, url);
    image.setUrl(url);
  }

  /*
   * Sets the button's Text property to a new value.
   */
  private void setTextProperty(String text) {
    buttonWidget.setText(text);
    updatePreferredSizeOfButton();
  }

  /*
   * Sets the button's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(buttonWidget, text);
  }

  private final void updatePreferredSizeOfButton() {
    preferredSizeOfButton = MockComponentsUtil.getPreferredSizeOfDetachedWidget(
        createClonedButton());
  }

  @Override
  public int getPreferredWidth() {
    // The superclass uses getOffsetWidth of the DeckPanel, which won't work for us.
    if (preferredSizeOfButton == null) {
      updatePreferredSizeOfButton();
    }
    int width = preferredSizeOfButton[0];
    if (hasImage) {
      int imageWidth = image.getWidth();
      if (imageWidth > width) {
        width = imageWidth;
      }
    }
    return width;
  }

  @Override
  public int getPreferredHeight() {
    // The superclass uses getOffsetHeight of the DeckPanel, which won't work for us.
    if (preferredSizeOfButton == null) {
      updatePreferredSizeOfButton();
    }
    int height = preferredSizeOfButton[1];
    if (hasImage) {
      int imageHeight = image.getHeight();
      if (imageHeight > height) {
        height = imageHeight;
      }
    }
    return height;
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
    } else if (propertyName.equals(PROPERTY_NAME_IMAGE)) {
      setImageProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXT)) {
      setTextProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      setTextColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_BUTTONSHAPE)){
      setShapeProperty(newValue);
    }
  }
}
