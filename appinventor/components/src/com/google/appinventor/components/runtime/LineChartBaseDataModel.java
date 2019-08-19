package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    @Override
    public void addEntryFromTuple(YailList tuple) {
        Entry entry = getEntryFromTuple(tuple);

        if (entry != null) {
            /* TODO: The commented out line should be used, however, it breaks in certain cases.
               When this is fixed in MPAndroidChart, this method should use the commented method instead
               of the current implementation.
               See: https://github.com/PhilJay/MPAndroidChart/issues/4616
            */
            // getDataset().addEntryOrdered(entry);


            // In Line Chart based data series, the data is already pre-sorted.
            // We can thus run binary search by comparing with the x value, and
            // using an x+1 value to find the insertion point
            int index = Collections.binarySearch(entries, // Use the list of entries
                entry, // Search for the same x value as the entry to be added
                new EntryXComparator()); // Compare by x value

            // Value not found: insertion point can be derived from it
            if (index < 0) {
                // result is (-(insertion point) - 1)
                index = -index - 1;
            } else {
                // Get the entry count of the Data Set
                int entryCount = entries.size();

                // Iterate until an entry with a differing (higher) x value is found (this
                // is where the value should be inserted)
                // The reason for a loop is to pass through all the duplicate entries.
                while (index < entryCount && entries.get(index).getX() == entry.getX()) {
                    index++;
                }
            }

            // Since we are adding a value manually to the specified index, we
            // must call notifyDataSetChanged manually here.
            entries.add(index, entry);
        }
    }

    @Override
    public void setColor(int argb) {
        super.setColor(argb);
        getDataset().setCircleColor(argb);
    }

    @Override
    public void setColors(List<Integer> colors) {
        super.setColors(colors);
        getDataset().setCircleColors(colors);
    }

    @Override
    protected void setDefaultStylingProperties() {
        getDataset().setDrawCircleHole(false); // Draw full circle instead of a hollow one
    }

    public void setLineType(int type) {
        switch (type) {
            case ComponentConstants.CHART_LINE_TYPE_LINEAR:
                dataset.setMode(LineDataSet.Mode.LINEAR);
                break;

            case ComponentConstants.CHART_LINE_TYPE_CURVED:
                dataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                break;

            case ComponentConstants.CHART_LINE_TYPE_STEPPED:
                dataset.setMode(LineDataSet.Mode.STEPPED);
                break;

            default:
                dataset.setMode(LineDataSet.Mode.LINEAR);
                break;
        }
    }
}
