// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;
import gnu.lists.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


/**
 * A ChartData2D component represents a single two-dimensional Data Series in the Chart component,
 * for example, a single Line in the case of a Line Chart, or a single Bar in the case of a Bar
 * Chart. The Data component is responsible for handling all the data of the Chart. The entries
 * of the Data component correspond of an x and a y value.
 * The component is attached directly to a Chart component by dragging it onto the Chart.
 */
@DesignerComponent(version = YaVersion.CHART_DATA_2D_COMPONENT_VERSION,
        description = "A component that holds (x, y)-coordinate based data",
        category = ComponentCategory.CHARTS,
        iconName = "images/web.png")
@SimpleObject
@SuppressWarnings("checkstyle:JavadocParagraph")
public final class ChartData2D extends ChartDataBase {
    /**
     * Creates a new Coordinate Data component.
     */
    public ChartData2D(Chart chartContainer) {
        super(chartContainer);
        // Construct default dataFileColumns list with 2 entries
        dataFileColumns = Arrays.asList("", "");
        sheetsColumns = Arrays.asList("", "");
        webColumns = Arrays.asList("", ""); // Construct default webColumns list with 2 entries
    }

    /**
     * Adds an entry with the specified x and y value. Values can be specified as text,
     * or as numbers. For Line, Scatter, Area and Bar Charts, both values should represent a number.
     * For Bar charts, the x value is rounded to the nearest integer.
     * For Pie Charts, the x value is a text value.
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction()
    public void AddEntry(final String x, final String y) {
        // Entry should be added via the Thread Runner asynchronously
        // to guarantee the order of data adding (e.g. CSV data
        // adding could be happening when this method is called,
        // so the task should be queued in the single Thread Runner)
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                // Create a 2-tuple, and add the tuple to the Data Series
                YailList pair = YailList.makeList(Arrays.asList(x, y));
                chartDataModel.addEntryFromTuple(pair);

                // Refresh Chart with new data
                refreshChart();
            }
        });
    }

    /**
     * Removes an entry with the specified x and y value, provided it exists.
     * See {@link #AddEntry(String, String)} for an explanation of the valid entry values.
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction()
    public void RemoveEntry(final String x, final String y) {
        // Entry should be deleted via the Thread Runner asynchronously
        // to guarantee the order of data adding (e.g. CSV data
        // adding could be happening when this method is called,
        // so the task should be queued in the single Thread Runner)
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                // Create a 2-tuple, and remove the tuple from the Data Series
                YailList pair = YailList.makeList(Arrays.asList(x, y));
                chartDataModel.removeEntryFromTuple(pair);

                // Refresh Chart with new data
                refreshChart();
            }
        });
    }

    /**
     * Returns a boolean value specifying whether an entry with the specified x and y
     * values exists. The boolean value of true is returned if the value exists,
     * and a false value otherwise. See {@link #AddEntry(String, String)}
     * for an explanation of the valid entry values.
     *
     * @param x - x value of entry
     * @param y - y value of entry
     * @return true if entry exists
     */
    @SuppressWarnings("TryWithIdenticalCatches")
    @SimpleFunction(description = "Checks whether an (x, y) entry exists in the Coordinate Data."
            + "Returns true if the Entry exists, and false otherwise.")
    public boolean DoesEntryExist(final String x, final String y) {
        try {
            return threadRunner.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    // Create a 2-tuple, and check whether the entry exists
                    YailList pair = YailList.makeList(Arrays.asList(x, y));
                    return chartDataModel.doesEntryExist(pair);
                }
            }).get();
        } catch (InterruptedException e) {
            Log.e(this.getClass().getName(), e.getMessage());
        } catch (ExecutionException e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }

        // Exceptions thrown (behavior undefined): Assume entry not found
        return false;
    }

    /**
     * Draws the line of best fit
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction(description = "Draws the line of best fit.")
    public void DrawLineOfBestFit(final YailList x, final YailList y) {
        List predictions = (List) Regression.ComputeLineOfBestFit(x, y).get(3);
        final List predictionPairs = new ArrayList<Pair>();
        List xValues = (List) x.getCdr();
        for (int i = 0; i < xValues.size(); i++) {
            predictionPairs.add(Arrays.asList(xValues.get(i), predictions.get(i)));
        }
        YailList predictionPairsList = YailList.makeList(predictionPairs);
        chartDataModel.importFromList(predictionPairsList);

        if (chartDataModel.getDataset() instanceof LineDataSet) {
            ((LineDataSet) chartDataModel.getDataset()).setDrawCircles(false);
            ((LineDataSet) chartDataModel.getDataset()).setDrawValues(false);
        }
        refreshChart();
    }
}

