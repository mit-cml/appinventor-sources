// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;

public class MockTextNode extends MockARNodeBase {
  public static final String TYPE = "TextNode";

  private static final String PROPERTY_NAME_SCALE = "Scale";

  public MockTextNode(SimpleEditor editor) {
    super(editor, TYPE, images.textNode());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(200, 50);
    svgpanel.setInnerSVG("<defs>" +
    "<filter id=\"shadow\" x=\"0\" y=\"0\" width=\"200%\" height=\"200%\">" +
      "<feOffset dx=\"1\" dy=\"1\" />" +
      "<feGaussianBlur stdDeviation=\"0.5 0.5\" result=\"shadow\"/>" +
    "</filter>" +
    "</defs>" +
    "<text x=\"2\" y=\"15\" fill=\"red\" filter=\"url(#shadow)\">TextNode</text>" +
    "<text style=\"filter: url(#shadow); fill: black\" x=\"2\" y=\"15\"> TextNode </text>" +
    "<text x=\"2\" y=\"15\" fill=\"red\">TextNode</text>");

    panel.setWidget(svgpanel);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_TEXT_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.AR_TEXT_PREFERRED_HEIGHT;
  }

  @Override
  public void onCreateFromPalette() {
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the label's TextAlignment property to a new value.
   */
  private void setTextAlignmentProperty(String text) {}

  /*
   * Sets the label's Text property to a new value.
   */
  private void setTextProperty(String text) {}

  /*
   * Sets the label's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_SCALE)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_TEXTALIGNMENT)) {
      setTextAlignmentProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TEXT)) {
      setTextProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      setTextColorProperty(newValue);
    }
  }
}
