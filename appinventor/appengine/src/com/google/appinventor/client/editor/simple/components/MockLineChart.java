package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;
import org.pepstock.charba.client.LineChart;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.LineDataset;
import org.pepstock.charba.client.enums.CubicInterpolationMode;
import org.pepstock.charba.client.enums.SteppedLine;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

import java.util.ArrayList;

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

        // Initialize Line Chart mock Widget
        lineChartWidget = new LineChart();

        // Initialize data set
        LineDataset dataset = lineChartWidget.newDataset();

        // Construct test data
        ArrayList<DataPoint> dataPoints = new ArrayList<>();

        for (int i = 0; i < 10; ++i) {
            DataPoint point = new DataPoint();
            point.setX(i);
            point.setY(2*i);
            dataPoints.add(point);
        }

        dataset.setDataPoints(dataPoints);

        // Style settings
        dataset.setFill(false);
        dataset.setPointBackgroundColor("black");
        dataset.setBorderColor("black");
        dataset.setCubicInterpolationMode(CubicInterpolationMode.MONOTONE);


        // Chart widget setup
        lineChartWidget.getData().setDatasets(dataset);
        lineChartWidget.getOptions().setMaintainAspectRatio(false);

        lineChartWidget.setStylePrimaryName("ode-SimpleMockComponent");
        initComponent(lineChartWidget);
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        // TBD
    }

    @Override
    public int getPreferredWidth() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
    }

    @Override
    public int getPreferredHeight() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
    }
}
