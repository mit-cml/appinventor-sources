// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * Mock Slider component.
 *
 * @author M. Hossein Amerkashi - kkashi01@gmail.com
 */
public final class MockSlider extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "Slider";

  protected static final String PROPERTY_NAME_COLORLEFT = "ColorLeft";
  protected static final String PROPERTY_NAME_COLORRIGHT = "ColorRight";
  protected static final String PROPERTY_NAME_THUMBCOLOR = "ThumbColor";

  // Widget for showing the mock slider
  protected final HorizontalPanel panel;
  private int preferredSliderWidth;
  private int sliderHeight;
  private int currentSliderWidth;
  public String trackColorActive = "orange";
  public String trackColorInactive = "gray";
  public String thumbColor = "darkgray";

  public SVGPanel sliderGraphic;

  /**
   * Creates a new MockSlider component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockSlider(SimpleEditor editor) {
    super(editor, TYPE, images.slider());

    // Initialize mock slider UI
    panel = new HorizontalPanel();
    panel.setStylePrimaryName("ode-SimpleMockComponent");
    initComponent(panel);
    sliderGraphic = new SVGPanel();
    panel.add(sliderGraphic);
    paintSlider();
  }

  /**
   * Draw the SVG graphic of the slider. It displays the left and
   * right sides of the slider, each with their own colors.
   *
   */
  private void paintSlider() {
    if (phonePreview.equals("Classic")) {
      sliderHeight = 20;
      preferredSliderWidth = 120;
    } else if (phonePreview.equals("Android Material")) {
      sliderHeight = 15;
      preferredSliderWidth = 80;
    } else if (phonePreview.equals("Android Holo")) {
      sliderHeight = 28;
      preferredSliderWidth = 80;
    } else {
      sliderHeight = 30;
      preferredSliderWidth = 80;
    }

    updateSliderGraphic(preferredSliderWidth);
    refreshForm();
  }

  private void updateSliderGraphic(int width) {
    currentSliderWidth = Math.max(1, width);
    sliderGraphic.setWidth(currentSliderWidth + "px");
    sliderGraphic.setHeight(sliderHeight + "px");
    panel.setCellWidth(sliderGraphic, currentSliderWidth + "px");

    if (phonePreview.equals("Classic")) {
      classicSlider();
    } else if (phonePreview.equals("Android Material")) {
      materialSlider();
    } else if (phonePreview.equals("Android Holo")) {
      holoSlider();
    } else {
      iosSlider();
    }
  }

  private void classicSlider() {
    int midpoint = currentSliderWidth / 2;
    int thumbWidth = 14;
    int thumbX = Math.max(0, midpoint - thumbWidth / 2);
    sliderGraphic.setInnerSVG("<rect x=\"0\" y=\"3\" width=\"" + midpoint
        + "\" height=\"14\" rx=\"4\" fill=\"" + trackColorActive + "\"/>\n"
        + "<rect x=\"" + midpoint + "\" y=\"3\" width=\"" + (currentSliderWidth - midpoint)
        + "\" height=\"14\" rx=\"4\" fill=\"" + trackColorInactive + "\"/>\n"
        + "<rect x=\"" + thumbX + "\" y=\"0\" width=\"" + thumbWidth
        + "\" height=\"20\" rx=\"2\" fill=\"" + thumbColor + "\"/>");
  }

  private void holoSlider() {
    int midpoint = currentSliderWidth / 2;
    sliderGraphic.setInnerSVG("<rect x=\"0\" y=\"14\" width=\"" + midpoint
        + "\" height=\"4\" fill=\"" + trackColorActive + "\"/>\n"
        + "<rect x=\"" + midpoint + "\" y=\"14\" width=\"" + (currentSliderWidth - midpoint)
        + "\" height=\"4\" fill=\"" + trackColorInactive + "\"/>\n"
        + "<circle cx=\"" + midpoint + "\" cy=\"16\" r=\"10\" fill=\"" + thumbColor
        + "\" opacity=\"0.64\"/>\n"
        + "<circle cx=\"" + midpoint + "\" cy=\"16\" r=\"4\" fill=\"" + thumbColor + "\"/>");
  }

  private void materialSlider() {
    int midpoint = currentSliderWidth / 2;
    sliderGraphic.setInnerSVG("<rect x=\"0\" y=\"5\" width=\"" + midpoint
        + "\" height=\"4\" rx=\"2\" fill=\"" + trackColorActive + "\"/>\n"
        + "<rect x=\"" + midpoint + "\" y=\"5\" width=\"" + (currentSliderWidth - midpoint)
        + "\" height=\"4\" rx=\"2\" fill=\"" + trackColorInactive + "\"/>\n"
        + "<circle cx=\"" + midpoint + "\" cy=\"7\" r=\"7\" fill=\"" + thumbColor + "\"/>");
  }

  private void iosSlider() {
    int midpoint = currentSliderWidth / 2;
    sliderGraphic.setInnerSVG("<rect x=\"0\" y=\"14\" width=\"" + currentSliderWidth
        + "\" height=\"1\" rx=\"0.5\" fill=\"" + trackColorInactive + "\"/>\n"
        + "<rect x=\"0\" y=\"14\" width=\"" + midpoint
        + "\" height=\"1\" rx=\"0.5\" fill=\"" + trackColorActive + "\"/>\n"
        + "<circle cx=\"" + midpoint + "\" cy=\"14\" r=\"14\" fill=\"" + thumbColor
        + "\" stroke=\"rgba(0,0,0,0.03)\" stroke-width=\"0.5\"/>");
  }

  /**
   * Set track color for slider on the left side of the thumb
   * Thumb is the button that slides back and forth on the slider
   *
   */
  private void setTrackColorActiveProperty(String text) {
    if (sliderGraphic != null) {
      trackColorActive = MockComponentsUtil.getColor(text).toString();
      updateSliderGraphic(currentSliderWidth);
    }
  }

  /**
   * Set track color for slider on the right side of the thumb
   * Thumb is the button that slides back and forth on the slider
   *
   */
  private void setTrackColorInactiveProperty(String text) {
    if (sliderGraphic != null) {
      trackColorInactive = MockComponentsUtil.getColor(text).toString();
      updateSliderGraphic(currentSliderWidth);
    }
  }

  /**
   * Set thumb color for slider
   */
  private void setThumbColorProperty(String text) {
    if (sliderGraphic != null) {
      thumbColor = MockComponentsUtil.getColor(text).toString();
      updateSliderGraphic(currentSliderWidth);
    }
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    //We don't want to allow user to change the slider height. S/he can only change the
    //slider width
    if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  @Override
  public void setPixelSize(int width, int height) {
    super.setPixelSize(width, height);
    updateSliderGraphic(width);
  }

  @Override
  public int getPreferredWidth() {
    return preferredSliderWidth;
  }

  @Override
  public int getPreferredHeight() {
    return sliderHeight;
  }

  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_WIDTH)) {
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_COLORLEFT)) {
      setTrackColorActiveProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_COLORRIGHT)) {
      setTrackColorInactiveProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_THUMBCOLOR)) {
      setThumbColorProperty(newValue);
      refreshForm();
    }

  }
}
