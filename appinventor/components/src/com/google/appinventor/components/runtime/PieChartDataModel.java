// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles the data operations & model-specific styling for Pie
 * Chart data for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartDataModel
 */
public class PieChartDataModel
    extends Chart2DDataModel<PieEntry, IPieDataSet, PieData, PieChart, PieChartView> {
  /* Since a custom legend is used which is shared by all the separate
   * Pie Chart views (rings), for ease of deletion and operations on
   * the entries, the Legend Entries List is kept for this single
   * Data Series. The central view reference is kept to modify the
   * Legend accordingly.
   */
  private final List<LegendEntry> legendEntries = new ArrayList<>();

  /**
   * Initializes a new PieChartDataModel object instance.
   *
   * <p>Links the Data Model to the specified Chart, since one
   * Pie Chart instance represents a single ring of a Pie Chart.
   *
   * @param chart  Chart to link Data Model
   * @param view   Chart View to link Data Model to
   * @param data   Chart data instance
   */
  public PieChartDataModel(PieData data, PieChartView view, PieChart chart) {
    super(data, view);

    // Initialize dataset and add it to the Data object
    dataset = new PieDataSet(new ArrayList<PieEntry>(), "");
    this.data.addDataSet(dataset);

    // Set the data to the Pie Chart (one Pie Chart component
    // can have at most one Dataset in v3.1.0)
    chart.setData(data);

    setDefaultStylingProperties();
    this.view = view;
  }

  @Override
  public void addEntryFromTuple(YailList tuple) {
    // Construct a PieEntry from the specified tuple and add it to
    // the Data Series.
    PieEntry entry = (PieEntry) getEntryFromTuple(tuple);

    if (entry != null) {
      this.entries.add(entry);

      // Construct a new Legend Entry
      LegendEntry legendEntry = new LegendEntry();

      // The label of the Legend Entry should be the x value of the tuple
      legendEntry.label = tuple.getString(0);

      // Get the entry count of the Data series and the
      // colors of the Data Series
      int entriesCount = this.entries.size();
      List<Integer> colors = getDataset().getColors();

      // The index of the color value to use is the
      // last entry (the one which has just been added)
      // modulo the size of the colors List (since
      // there could be less colors than entries)
      int index = (entriesCount - 1) % colors.size();

      // Set the color of the Legend Entry
      legendEntry.formColor = colors.get(index);

      // Add the Legend Entry both to the local Legend Entries List and
      // to the Legend of the view itself.
      legendEntries.add(legendEntry);
      view.addLegendEntry(legendEntry);
    }
  }

  @Override
  public void removeEntry(int index) {
    // Entry exists; remove it
    if (index >= 0) {
      entries.remove(index);

      // Remove the corresponding Legend entry (same index as Data Set index)
      LegendEntry removedEntry = legendEntries.remove(index);
      view.removeLegendEntry(removedEntry);

      // Update the colors of the Legend entries
      updateLegendColors();
    }
  }

  @Override
  public void clearEntries() {
    super.clearEntries();

    // Remove all the corresponding legend entries
    view.removeLegendEntries(legendEntries);
    legendEntries.clear();
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
        this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
            "GetEntryFromTuple",
            ErrorMessages.ERROR_INVALID_CHART_ENTRY_VALUES,
            xValue, yValue);
      }
    } catch (NullPointerException e) {
      this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
          "GetEntryFromTuple",
          ErrorMessages.ERROR_NULL_CHART_ENTRY_VALUES);
    } catch (IndexOutOfBoundsException e) {
      this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
          "GetEntryFromTuple",
          ErrorMessages.ERROR_INSUFFICIENT_CHART_ENTRY_VALUES,
          getTupleSize(), tuple.size());
    }

    return null;
  }

  @Override
  public YailList getTupleFromEntry(Entry entry) {
    // Cast Entry to PieEntry (safe cast)
    PieEntry pieEntry = (PieEntry) entry;

    // Create a list with the X and Y values of the entry, and
    // convert the generic List to a YailList
    List<?> tupleEntries = Arrays.asList(pieEntry.getLabel(), pieEntry.getY());
    return YailList.makeList(tupleEntries);
  }

  @Override
  protected void setDefaultStylingProperties() {
    // Set spacing between each slice
    if (dataset instanceof PieDataSet) {
      ((PieDataSet) dataset).setSliceSpace(3);
    }
  }

  /**
   * Sets the colors of the Data Series from the specified
   * colors List, where each value in the List is an integer.
   *
   * @param colors List of entries expected to contain colors (integers)
   */
  public void setColors(List<Integer> colors) {
    super.setColors(colors);

    // After setting the colors, the colors of the Legend
    // need to be updated.
    updateLegendColors();
  }

  @Override
  public void setColor(int argb) {
    // The setColor method must follow the same procedure
    // as the setColors method due to the need of updating
    // the Legend Entries.
    setColors(Collections.singletonList(argb));
  }

  @Override
  protected boolean areEntriesEqual(Entry e1, Entry e2) {
    // To avoid (unlikely) cast exceptions, check that
    // both the entries are of instance PieEntry, and return
    // false if that is not the case.
    // TODO: Add Entry as a generic?
    if (!(e1 instanceof PieEntry && e2 instanceof PieEntry)) {
      return false;
    }

    // Cast entries to PieEntries to be able to compare labels
    PieEntry p1 = (PieEntry) e1;
    PieEntry p2 = (PieEntry) e2;

    return p1.getLabel().equals(p2.getLabel()) // x value comparison
        && p1.getY() == p2.getY(); // y value comparison
  }

  /**
   * Updates the colors of the Legend Entries based
   * on the colors of the Data Series.
   */
  private void updateLegendColors() {
    // Update the legend
    for (int i = 0; i < legendEntries.size(); ++i) {
      // Since the number of colors might be less, the index
      // of the color to use should be modulo the color count
      int index = i % getDataset().getColors().size();

      // Set the corresponding color to the Legend entry (since
      // LegendEntry is referenced, this will automatically update
      // the central Legend that contains all the entries, and not
      // just the local ones)
      legendEntries.get(i).formColor = getDataset().getColors().get(index);
    }
  }
}
