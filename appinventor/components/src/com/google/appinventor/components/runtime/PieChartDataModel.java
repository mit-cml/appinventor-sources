package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PieChartDataModel extends ChartDataModel<PieDataSet, PieData> {
  private List<LegendEntry> legendEntries = new ArrayList<LegendEntry>();
  private PieChartView view;

  /**
   * Initializes a new PieChartDataModel object instance.
   *
   * Links the Data Model to the specified Chart, since one
   * Pie Chart instance represents a single ring of a Pie Chart.
   *
   * @param chart  Chart to link Data Model
   * @param data Chart data instance
   */
  protected PieChartDataModel(PieChartView view, PieChart chart, PieData data) {
    super(data);
    dataset = new PieDataSet(new ArrayList<PieEntry>(), "");
    this.data.addDataSet(dataset);
    chart.setData(data);
    setDefaultStylingProperties();
    this.view = view;
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }

  @Override
  public void addEntryFromTuple(YailList tuple) {
    // Construct a PieEntry from the specified tuple and add it to
    // the Data Series.
    PieEntry entry = (PieEntry) getEntryFromTuple(tuple);
    dataset.addEntry(entry);

    LegendEntry legendEntry = new LegendEntry();
    legendEntry.label = tuple.getString(0);
    legendEntry.formColor = dataset.getColors().get(dataset.getColors().size() - 1);

    legendEntries.add(legendEntry);
    view.addLegendEntry(legendEntry);
  }

  @Override
  public void removeEntryFromTuple(YailList tuple) {
    // Construct an entry from the specified tuple
    Entry entry = getEntryFromTuple(tuple);

    if (entry != null) {
      // Get the index of the entry
      int index = findEntryIndex(entry);

      // Entry exists; remove it
      if (index >= 0) {
        getDataset().removeEntry(index);
        LegendEntry removedEntry = legendEntries.remove(index);
        view.removeLegendEntry(removedEntry);
      }
    }
  }

  @Override
  public Entry getEntryFromTuple(YailList tuple) {
    try {
      // Tuple is expected to have at least 2 entries.
      // The first entry is assumed to be the x value, and
      // the second is assumed to be the y value.
      String xValue = tuple.getString(0);
      String yValue = tuple.getString(1);

      try {
        // Attempt to parse the y value String representation
        float y = Float.parseFloat(yValue);

        // The y value is the first argument,
        // the x value is the second (label)
        return new PieEntry(y, xValue);
      } catch (NumberFormatException e) {
        // Nothing happens: Do not add entry on NumberFormatException
      }
    } catch (Exception e) {
      // 2-tuples are invalid when null entries are present, or if
      // the number of entries is not sufficient to form a pair.
      // TODO: Show toast error notification
    }

    return null;
  }

  @Override
  public YailList getTupleFromEntry(Entry entry) {
    // Cast Entry to PieEntry (safe cast)
    PieEntry pieEntry = (PieEntry) entry;

    // Create a list with the X and Y values of the entry, and
    // convert the generic List to a YailList
    List tupleEntries = Arrays.asList(pieEntry.getLabel(), pieEntry.getY());
    return YailList.makeList(tupleEntries);
  }

  @Override
  protected void setDefaultStylingProperties() {
    // Set spacing between each slice
    dataset.setSliceSpace(5f);
  }

  @Override
  protected YailList getDefaultValues(int size) {
    // TODO: Return default values
    return new YailList();
  }
}
