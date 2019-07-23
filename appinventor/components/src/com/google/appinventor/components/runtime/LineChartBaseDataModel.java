package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class LineChartBaseDataModel extends PointChartDataModel<LineDataSet, LineData>  {
    /**
     * Initializes a new LineChartBaseDataModel object instance.
     *
     * @param data  Line Chart Data object instance
     */
    protected LineChartBaseDataModel(LineData data) {
        super(data);
        dataset = new LineDataSet(new ArrayList<Entry>(), "");
        this.data.addDataSet(dataset); // Safe add
        setDefaultStylingProperties();
    }

    /**
     * Adds a (x, y) entry to the Line Data Set.
     *
     * @param x  x value
     * @param y  y value
     */
    @Override
    public void addEntry(float x, float y) {
        Entry entry = new Entry(x, y);

        /* TODO: The commented out line should be used, however, it breaks in certain cases.
           When this is fixed in MPAndroidChart, this method should use the commented method instead
           of the current implementation.
           See: https://github.com/PhilJay/MPAndroidChart/issues/4616
        */
        // getDataset().addEntryOrdered(entry);


        // In Line Chart based data series, the data is already pre-sorted.
        // We can thus run binary search by comparing with the x value, and
        // using an x+1 value to find the insertion point
        int index = Collections.binarySearch(getDataset().getValues(), // Use the list of entries
            new Entry(x+1, y), // We use the x+1 value since that should be the insertion point
            new EntryXComparator()); // Compare by x value

        // Value not found: insertion point can be derived from it
        if (index < 0) {
            // result is (-(insertion point) - 1)
            index = -index - 1;
        } // TODO: Handle case where x+1 value found is a middle value

        // Since we are adding a value manually to the specified index, we
        // must call notifyDataSetChanged manually here.
        getDataset().getValues().add(index, entry);
        getDataset().notifyDataSetChanged();
    }

    @Override
    public void setColor(int argb) {
        super.setColor(argb);
        getDataset().setCircleColor(argb);
    }

    @Override
    protected void setDefaultStylingProperties() {
        getDataset().setDrawCircleHole(false); // Draw full circle instead of a hollow one
    }
}
