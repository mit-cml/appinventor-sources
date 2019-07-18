package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for Chart type.
 */
public class YoungAndroidChartTypeChoicePropertyEditor extends ChoicePropertyEditor {

    // Chart type choices
    private static final Choice[] types = new Choice[] {
            new Choice(MESSAGES.lineChartType(), "0"),
            new Choice(MESSAGES.scatterChartType(), "1"),
            new Choice(MESSAGES.areaChartType(), "2"),
            new Choice(MESSAGES.barChartType(), "3"),
            new Choice(MESSAGES.pieChartType(), "4")
    };

    public YoungAndroidChartTypeChoicePropertyEditor() {
        super(types);
    }
}

