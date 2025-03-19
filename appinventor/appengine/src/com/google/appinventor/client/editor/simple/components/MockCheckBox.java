// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * Mock CheckBox component, inherited from MockToggleBase
 *
 * @author lizlooney@google.com (Liz Looney), srlane@mit.edu (Susan Rati Lane)
 */
public final class MockCheckBox extends MockToggleBase<HorizontalPanel> {


  /**
   * Component type name.
   */
  public static final String TYPE = "CheckBox";

  //Widgets for MockCheckBox
  protected final HorizontalPanel panel;
  public Boolean checked = false;  // the "on" property of the switch is equivalent to "checked"

  public InlineHTML checkBoxLabel;
  public SVGPanel checkBoxGraphic;
  public Boolean isInitialized;

  /**
   * Creates a new MockCheckbox component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockCheckBox(SimpleEditor editor) {
    super(editor, TYPE, images.checkbox());

    panel = new HorizontalPanel();
    checkBoxLabel = new InlineHTML();
    toggleWidget = panel;
    isInitialized = false;
    initWrapper(toggleWidget);
  }

  /**
   * Draw the SVG graphic of the toggle switch. It can be drawn in either checked or
   * unchecked positions, each with their own colors.
   *
   */
  private void paintCheckBox(boolean checked) {
    if (isInitialized) {
      panel.remove(checkBoxGraphic);
    } else {
      isInitialized = true;
    }
    checkBoxGraphic = new SVGPanel();
    int checkBoxHeight = 20;

    int checkBoxWidth = 20;

    if (checked) {
      if (phonePreview.equals("Classic")) {
        classicCheckBox();
      } else if (phonePreview.equals("Android Material")) {
        materialCheckBox();
      } else if (phonePreview.equals("Android Holo")) {
        holoCheckBox();
      } else {
        iosCheckBox();
      }
    } else {
      unchecked();
    }
    checkBoxGraphic.setWidth(checkBoxWidth + "px");
    checkBoxGraphic.setHeight(checkBoxHeight + "px");

    panel.add(checkBoxGraphic);
    panel.setCellWidth(checkBoxGraphic, checkBoxWidth + "px");
    panel.setCellHorizontalAlignment(checkBoxGraphic, HasHorizontalAlignment.ALIGN_LEFT);
    panel.add(checkBoxLabel);
    toggleWidget = panel;
    refreshForm();
  }

  private void classicCheckBox() {
    checkBoxGraphic.setInnerSVG("<g transform=\"translate(35.5 27.5)\">\n"
        + "<path d=\"M6.838,12.553l-1.366-1.53L4.355,12.094l2.407,2.665L12.8,8.5l-1-1.012Z\""
        + " transform=\"translate(-35.466 -29.759)\" fill=\"#179213\" opacity=\"0.54\"/>\n"
        + "<rect width=\"16\" height=\"16\" rx=\"2\" transform=\"translate(-35 -27)\""
        + " fill=\"none\" stroke=\"#707070\" stroke-linejoin=\"round\" stroke-width=\"1\"/>\n"
        + "</g>");
  }

  private void materialCheckBox() {
    String checkBoxColor = MockComponentsUtil.getColor(colorAccent).toString();
    checkBoxGraphic.setInnerSVG("<path d=\"M17.222,3H4.778A1.783,1.783,0,0,0,3,4.778V17.222A1.783,1.783,0,0,0,4.778,19H17.222A1.783,1.783,0,0,0,19,17.222V4.778A1.783,1.783,0,0,0,17.222,3Zm-8,12.444L4.778,11.178l1.244-1.156,3.2,3.111,6.756-6.578,1.244,1.156-8,7.733Z\" transform=\"translate(-3 -3)\" fill=\"" + checkBoxColor + "\"/>");
  }

  private void holoCheckBox() {
    checkBoxGraphic.setInnerSVG("<path d=\"M17.222,3H4.778A1.783,1.783,0,0,0,3,4.778V17.222A1.783,1.783,0,0,0,4.778,19H17.222A1.783,1.783,0,0,0,19,17.222V4.778A1.783,1.783,0,0,0,17.222,3Zm-8,12.444L4.778,11.178l1.244-1.156,3.2,3.111,6.756-6.578,1.244,1.156-8,7.733Z\" transform=\"translate(-3 -3)\"/>");
  }

  private void iosCheckBox() {
    checkBoxGraphic.setInnerSVG("<g transform=\"translate(-574 -295)\">\n"
        + "<rect width=\"16\" height=\"16\" rx=\"2\" transform=\"translate(574 295)\""
        + " fill=\"#4c9c94\"/>\n"
        + "<path d=\"M1.5,4.5,0,6l4,4,9-8.5L11.5,0,4,7Z\" transform=\"translate(575.5 298.5)\""
        + " fill=\"#fff\"/>\n"
        + "</g>");
  }

  private void unchecked() {
    checkBoxGraphic.setInnerSVG("<g fill=\"#fff\" stroke=\"#707070\" stroke-width=\"1\">\n"
        + "<rect width=\"16\" height=\"16\" rx=\"2\" stroke=\"none\"/>\n"
        + "<rect x=\"0.5\" y=\"0.5\" width=\"15\" height=\"15\" rx=\"1.5\" fill=\"none\"/>\n"
        + "</g>");
  }

  /*
   * Sets the checkbox's Text property to a new value.
   */
  protected void setTextProperty(String text) {
    panel.remove(checkBoxLabel);
    checkBoxLabel.setText(text);
    panel.insert(checkBoxLabel, 1);
    toggleWidget = panel;
    updatePreferredSize();
  }

  /*
   * Sets the checkbox's Checked property to a new value.
   */
  private void setCheckedProperty(String text) {
    checked = Boolean.parseBoolean(text);
    paintCheckBox(checked);
  }

  @Override
  protected void setFontSizeProperty(String text) {
    MockForm form = ((YaFormEditor) editor).getForm();
    if (Float.parseFloat(text) == FONT_DEFAULT_SIZE
          && form != null
          && form.getPropertyValue("BigDefaultText").equals("True")) {
      MockComponentsUtil.setWidgetFontSize(toggleWidget.getWidget(1), "24");
    } else {
      MockComponentsUtil.setWidgetFontSize(toggleWidget.getWidget(1), text);
    }

    updatePreferredSize();
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_CHECKED)) {
      setCheckedProperty(newValue);
      refreshForm();
    }
  }
}
