package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;
import org.pepstock.charba.client.LineChart;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.Labels;
import org.pepstock.charba.client.data.LineDataset;
import org.pepstock.charba.client.enums.CubicInterpolationMode;
import org.pepstock.charba.client.enums.SteppedLine;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

import java.util.ArrayList;

public final class MockLineChart extends MockVisibleComponent {
    private static final String PROPERTY_DESCRIPTION = "Description";

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

        loadTestData();

        // Chart widget setup
        lineChartWidget.getOptions().setMaintainAspectRatio(false);
        lineChartWidget.getOptions().getTitle().setDisplay(true);
        lineChartWidget.setStylePrimaryName("ode-SimpleMockComponent");
        initComponent(lineChartWidget);
    }

    /**
     * Loads a test data set for the Line Chart widget.
     */
    private void loadTestData() {
        // Initialize data set
        LineDataset dataset = lineChartWidget.newDataset();

        // Construct test data
        dataset.setData(1, 7, 5, 4);

        // Style settings
        dataset.setFill(false);
        dataset.setPointBackgroundColor("black");
        dataset.setBorderColor("black");
        dataset.setBorderWidth(1);
        dataset.setLineTension(0);
        dataset.setLabel("Data");

        // Set the data set to the chart
        lineChartWidget.getData().setDatasets(dataset);

        // Set x value labels
        lineChartWidget.getData().setLabels("1", "2", "3", "4");
    }

    private void setDescriptionProperty(String text) {
        lineChartWidget.getOptions().getTitle().setText(text);
    }

    /*
     * Sets the Line Chart's BackgroundColor property to a new value.
     */
    private void setBackgroundColorProperty(String text) {
        if (MockComponentsUtil.isDefaultColor(text)) {
            text = "&HFFFFFFFF";  // white
        }
        MockComponentsUtil.setWidgetBackgroundColor(lineChartWidget, text);
    }

    @Override
    public int getPreferredWidth() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
    }

    @Override
    public int getPreferredHeight() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        if (propertyName.equals(PROPERTY_DESCRIPTION)) {
            setDescriptionProperty(newValue);
            lineChartWidget.draw();
        } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
            setBackgroundColorProperty(newValue);
        }
    }
}
