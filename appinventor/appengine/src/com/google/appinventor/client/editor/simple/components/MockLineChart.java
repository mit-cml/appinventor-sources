package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import org.pepstock.charba.client.LineChart;
import org.pepstock.charba.client.data.LineDataset;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

public final class MockLineChart extends MockVisibleComponent {
    public static final String TYPE = "LineChart";

    private LineChart lineChartWidget;

    static {
        ResourcesType.setClientBundle(EmbeddedResources.INSTANCE);
    }

    /**
     * Creates a new instance of a Mock Line Chart component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockLineChart(SimpleEditor editor) {
        super(editor, TYPE, images.image());

        // Initialize mock label UI
        lineChartWidget = new LineChart();
        LineDataset dataset = lineChartWidget.newDataset();
        dataset.setData(1, 2, 3, 4, 6);
        lineChartWidget.getData().setDatasets(dataset);

        lineChartWidget.setStylePrimaryName("ode-SimpleMockComponent");
        initComponent(lineChartWidget);
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        // TBD
    }
}
