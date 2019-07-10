package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.ScatterDataset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MockLineChartDataModel extends MockLineChartBaseDataModel {

    /**
     * Creates a new MockLineChartDataModel instance.
     *
     * @param chartData Data object of the Chart View.
     */
    public MockLineChartDataModel(Data chartData) {
        super(chartData);
    }
}
