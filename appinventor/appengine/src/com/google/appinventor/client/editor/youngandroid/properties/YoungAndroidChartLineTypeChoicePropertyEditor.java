package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

public class YoungAndroidChartLineTypeChoicePropertyEditor extends ChoicePropertyEditor {

  // Chart Point Shape choices
  private static final Choice[] types = new Choice[]{
      new Choice(MESSAGES.lineTypeLinear(), ComponentConstants.CHART_LINE_TYPE_LINEAR + ""),
      new Choice(MESSAGES.lineTypeCurved(), ComponentConstants.CHART_LINE_TYPE_CURVED + ""),
      new Choice(MESSAGES.lineTypeStepped(), ComponentConstants.CHART_LINE_TYPE_STEPPED + "")
  };

  public YoungAndroidChartLineTypeChoicePropertyEditor() {
    super(types);
  }
}
