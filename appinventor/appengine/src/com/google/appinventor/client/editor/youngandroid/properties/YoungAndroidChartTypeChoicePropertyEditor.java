package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for Chart type.
 */
public class YoungAndroidChartTypeChoicePropertyEditor extends ChoicePropertyEditor {

    // Chart type choices
    private static final Choice[] types = new Choice[] {
            new Choice(MESSAGES.lineChartType(), ComponentConstants.CHART_TYPE_LINE + ""),
            new Choice(MESSAGES.scatterChartType(), ComponentConstants.CHART_TYPE_SCATTER + ""),
            new Choice(MESSAGES.areaChartType(), ComponentConstants.CHART_TYPE_AREA + ""),
            new Choice(MESSAGES.barChartType(), ComponentConstants.CHART_TYPE_BAR + ""),
            new Choice(MESSAGES.pieChartType(), ComponentConstants.CHART_TYPE_PIE + "")
    };

    public YoungAndroidChartTypeChoicePropertyEditor() {
        super(types);
    }
}

