// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock Switch component, inherited from MockToggleBase
 *
 * @author srlane@mit.edu (Susan Rati Lane)
 */
public final class MockSwitch extends MockToggleBase<HorizontalPanel> {

  /**
   * Component type name.
   */
  public static final String TYPE = "Switch";
  protected final HorizontalPanel panel;
  public Boolean checked = false;  // the "on" property of the switch is equivalent to "checked"
  public String thumbColorActive = "white";
  public String thumbColorInactive = "gray";
  public String trackColorActive = "lime";
  public String trackColorInactive = "lightgray";

  public InlineHTML switchLabel;
  public SVGPanel switchGraphic;
  public Boolean isInitialized = false;

  /**
   * Creates a new MockSwitch component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockSwitch(SimpleEditor editor) {

    super(editor, TYPE, images.toggleswitch());

    panel = new HorizontalPanel();
    switchLabel = new InlineHTML();
    panel.add(switchLabel);
    toggleWidget = panel;
    isInitialized = false;
    initWrapper(toggleWidget);
  }

  /**
   * Class that extends Widget so we can use a protected constructor.
   *
   * <p/>The purpose of this class is to create a clone of the Switch
   * passed to the constructor. It will be used to determine the preferred size
   * of the Switch, without having the size constrained by its parent,
   * since the cloned Switch won't have a parent.
   */
  static class ClonedSwitch extends InlineHTML {
    ClonedSwitch(HorizontalPanel ptb) {
      super(DOM.clone(ptb.getElement(), true));
    }
  }

  @Override
  protected Widget createClonedWidget() {
    return new ClonedSwitch(toggleWidget);
  }


  /**
   * Draw the SVG graphic of the toggle switch. It can be drawn in either checked or
   * unchecked positions, each with their own colors.
   *
   */
  private void paintSwitch() {
    if (isInitialized) {
      panel.remove(switchGraphic);
    } else {
      isInitialized = true;
    }
    switchGraphic = new SVGPanel();
    int switchHeight = 14;  // pixels (Android asset is 28 px at 160 dpi)

    int switchWidth = (int) Math.round(switchHeight * 2);
    switchGraphic.setWidth(switchWidth + "px");
    switchGraphic.setHeight(switchHeight + "px");

    switchGraphic.setInnerSVG("<rect x=\"0\" y=\"0\" rx=\"" +
            switchHeight/2 + "\" yx=\"" + switchWidth/2 + "\" stroke-width=\"1\" stroke=\"black\"" +
            "height=\"" + switchHeight + "\" width=\"" + switchWidth + "\" fill=\"" + (checked? trackColorActive : trackColorInactive) + "\" />" +
            "<circle cx=\"" + (checked? switchWidth - switchHeight/2: switchHeight/2) + "\" fill=\"" + (checked? thumbColorActive : thumbColorInactive) + "\" " +
            "cy=\"" + (switchHeight/2) + "\" r=\"" + (switchHeight/2 - 1) + "\"/>");
    panel.add(switchGraphic);
    panel.setCellWidth(switchGraphic, switchWidth + "px");
    panel.setCellHorizontalAlignment(switchGraphic, HasHorizontalAlignment.ALIGN_RIGHT);
    panel.setCellVerticalAlignment(switchGraphic, HasVerticalAlignment.ALIGN_MIDDLE);
    toggleWidget = panel;
    refreshForm();
  }

  /**
   * Set thumb color for switch in checked state
   * Thumb color is the color the color of the button that toggles back and forth
   *
   */
  private void setThumbColorActiveProperty(String text) {
    thumbColorActive = MockComponentsUtil.getColor(text).toString();
    if (checked) {
      DOM.setStyleAttribute(toggleWidget.getWidget(1).getElement().getFirstChildElement().getNextSiblingElement(),
              "fill", thumbColorActive);
    }
  }

  /**
   * Set thumb color for switch in UNhecked state
   * Thumb color is the color the color of the button that toggles back and forth
   *
   */
  private void setThumbColorInactiveProperty(String text) {
    thumbColorInactive = MockComponentsUtil.getColor(text).toString();
    if (!checked) {
      DOM.setStyleAttribute(toggleWidget.getWidget(1).getElement().getFirstChildElement().getNextSiblingElement(),
              "fill", thumbColorInactive);
    }
  }

  /**
   * Set track color for switch in checked state
   * Track color is the color of the track that the toggle button slides back and forth along
   *
   */
  private void setTrackColorActiveProperty(String text) {
    trackColorActive = MockComponentsUtil.getColor(text).toString();
    if (checked) {
      DOM.setStyleAttribute(toggleWidget.getWidget(1).getElement().getFirstChildElement(), "fill",
              trackColorActive);
    }
  }

  /**
   * Set track color for switch in UNchecked state
   * Track color is the color of the track that the toggle button slides back and forth along
   *
   */
  private void setTrackColorInactiveProperty(String text) {
    trackColorInactive = MockComponentsUtil.getColor(text).toString();
    if (!checked) {
      DOM.setStyleAttribute(toggleWidget.getWidget(1).getElement().getFirstChildElement(), "fill",
              trackColorInactive);
    }
  }

  /*
   * Sets the switch's Checked property to a new value.
   */
  private void setOnProperty(String text) {

    checked = Boolean.parseBoolean(text);
    paintSwitch();
  }

  /*
   * Sets the switch's Text property to a new value.
   */
  protected void setTextProperty(String text) {
    panel.remove(switchLabel);
    switchLabel.setText(text);
    panel.insert(switchLabel, 0);
    toggleWidget = panel;
    updatePreferredSize();
  }

  @Override
  protected void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(toggleWidget.getWidget(0), text);
    updatePreferredSize();
  }

  @Override
  protected void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(toggleWidget.getWidget(0), text);
    updatePreferredSize();
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_ON)) {
      setOnProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_THUMBCOLORACTIVE)) {
      setThumbColorActiveProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_THUMBCOLORINACTIVE)) {
      setThumbColorInactiveProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TRACKCOLORACTIVE)) {
      setTrackColorActiveProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TRACKCOLORINACTIVE)) {
      setTrackColorInactiveProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      paintSwitch();
      refreshForm();
    }
  }
}
