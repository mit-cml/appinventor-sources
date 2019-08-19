package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;
import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.enums.CubicInterpolationMode;
import org.pepstock.charba.client.enums.SteppedLine;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Chart Data Model for Mock Line Chart based views.
 */
public abstract class MockLineChartBaseDataModel extends MockPointChartDataModel  {
    /**
     * Creates a new MockLineChartBaseDataModel instance.
     * @param chartData  Data object of the Chart View.
     */
    public MockLineChartBaseDataModel(Data chartData) {
        super(chartData);
    }

    @Override
    protected void setDefaultStylingProperties() {
        dataSeries.setBorderWidth(1);
        dataSeries.setLineTension(0);
        dataSeries.setShowLine(true);
    }

    @Override
    protected void postDataImportAction() {
        // No data points generated, fallback to default option.
        if (dataSeries.getDataPoints().isEmpty()) {
            setDefaultElements();
        } else {
            // Since we are dealing with a Scatter Data Series, sorting
            // is a must, because otherwise, the Chart will not look representative.
            // Consider adding: (1, 2), (5, 3), (2, 5). We want the x = 2
            // value to be continuous on the Line Chart, rather than
            // going outside the Chart, which would happen since we
            // are using a Scatter Chart.
            dataSeries.getDataPoints().sort(Comparator.comparingDouble(DataPoint::getX));
        }
    }

    public void setLineType(int type) {
        switch (type) {
            case ComponentConstants.CHART_LINE_TYPE_LINEAR:
                dataSeries.setSteppedLine(SteppedLine.FALSE);
                dataSeries.setLineTension(0);
                break;

            case ComponentConstants.CHART_LINE_TYPE_CURVED:
                dataSeries.setSteppedLine(SteppedLine.FALSE);
                dataSeries.setLineTension(1); // Set 100% Line Tension (curve)
                break;

            case ComponentConstants.CHART_LINE_TYPE_STEPPED:
                dataSeries.setSteppedLine(SteppedLine.BEFORE);
                dataSeries.setLineTension(0);
                break;
        }
    }
}
