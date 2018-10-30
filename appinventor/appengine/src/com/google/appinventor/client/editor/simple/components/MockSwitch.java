// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.shared.rpc.component.Component;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Iterator;

/**
 * Mock CheckBox component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockSwitch extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "Switch";
  protected final HorizontalPanel panel;
  public Boolean checked = false;
  public String thumbColorActive = "white";
  public String thumbColorInactive = "gray";
  public String trackColorActive = "lime";
  public String trackColorInactive = "lightgray";

  public InlineHTML switchLabel;
  public SVGPanel switchGraphic;
  public Boolean isInitialized = false;

  private int widgetWidth;
  private int widgetHeight;

  /**
   * Creates a new MockCheckbox component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockSwitch(SimpleEditor editor) {
    super(editor, TYPE, images.toggleswitch());

    panel = new HorizontalPanel();
    panel.setStylePrimaryName("ode-SimpleMockComponent");
    switchLabel = new InlineHTML();
    switchLabel.setText("Hello there");
    panel.add(switchLabel);
    initComponent(panel);

    paintSwitch();
  }

  private void paintSwitch() {
    if (isInitialized) {
      panel.remove(switchGraphic);
    } else {
      isInitialized = true;
    }
    switchGraphic = new SVGPanel();
    final int switchHeight = 14;
    final int switchWidth = (int) Math.round(switchHeight * 1.6);

    switchGraphic.setInnerSVG("<rect x=\"0\" y=\"0\" rx=\"" +
            switchHeight/2 + "\" yx=\"" + switchWidth/2 + "\" stroke-width=\"1\" stroke=\"black\"" +
            "height=\"" + switchHeight + "\" width=\"" + switchWidth + "\" fill=\"" + (checked? trackColorActive : trackColorInactive) + "\" />" +
            "<circle cx=\"" + (checked? switchWidth - switchHeight/2: switchHeight/2) + "\" fill=\"" + (checked? thumbColorActive : thumbColorInactive) + "\" " +
            "cy=\"" + (switchHeight/2) + "\" r=\"" + (switchHeight/2 - 1) + "\"/>");
    panel.add(switchGraphic);
  }

  @Override
  public void onCreateFromPalette() {
    // Change checkbox caption to component name
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  private void setThumbColorActiveProperty(String text) {
    thumbColorActive = MockComponentsUtil.getColor(text).toString();
    paintSwitch();
  }

  private void setThumbColorInactiveProperty(String text) {
    thumbColorInactive = MockComponentsUtil.getColor(text).toString();
    paintSwitch();
  }

  private void setTrackColorActiveProperty(String text) {
    trackColorActive = MockComponentsUtil.getColor(text).toString();
    paintSwitch();
  }

  private void setTrackColorInactiveProperty(String text) {
    trackColorInactive = MockComponentsUtil.getColor(text).toString();
    paintSwitch();
  }

  /*
   * Sets the checkbox's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the checkbox's Checked property to a new value.
   */
  private void setCheckedProperty(String text) {
    checked = Boolean.parseBoolean(text);
    paintSwitch();
  }

  @Override
  int getHeightHint() {
    int hint = super.getHeightHint();
    if (hint == MockVisibleComponent.LENGTH_PREFERRED) {
      float height = Float.parseFloat(getPropertyValue(MockVisibleComponent.PROPERTY_NAME_FONTSIZE));
      return Math.round(height);
    } else {
      return hint;
    }
  }
  private void setTextProperty(String text) {
    panel.remove(switchLabel);
    switchLabel.setText(text);
    panel.insert(switchLabel, 0);
    paintSwitch();
  }
  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_THUMBCOLORACTIVE)) {
      setThumbColorActiveProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_THUMBCOLORINACTIVE)) {
      setThumbColorInactiveProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TRACKCOLORACTIVE)) {
      setTrackColorActiveProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TRACKCOLORINACTIVE)) {
      setTrackColorInactiveProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ENABLED)) {
      setEnabledProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_CHECKED)) {
      setCheckedProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TEXT)) {
      setTextProperty(newValue);
    }
  }

}
