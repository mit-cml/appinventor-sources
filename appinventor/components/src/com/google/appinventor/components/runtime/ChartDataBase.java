// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;

import android.view.MotionEvent;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;

import com.google.appinventor.components.common.LineType;
import com.google.appinventor.components.common.PointStyle;
import com.google.appinventor.components.common.PropertyTypeConstants;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Base class for Chart Data components. Contains functionality common
 * to any Chart Data component. The component corresponds to a single
 * Data Series in a Chart, and can be attached to a Chart component.
 * Right now, the only extension is the ChartData2D component, but the
 * base class was created with future extensions (e.g. 3D data) in mind.
 */
@SuppressWarnings({"TryWithIdenticalCatches", "checkstyle:JavadocParagraph"})
@SimpleObject
public abstract class ChartDataBase extends DataCollection<Chart, Entry, ChartDataModel<?, ?, ?, ?, ?> > implements Component, DataSourceChangeListener,
    OnChartGestureListener, OnChartValueSelectedListener {
 //protected Chart container;

  /**
   * Used to queue & execute asynchronous tasks while ensuring
   * order of method execution (ExecutorService should be a Single Thread runner)
   * In the case of methods which return values and where
   * the result depends on the state of the data, blocking get
   * calls are used to ensure that all the previous async tasks
   * finish before the data is returned.
   */
  protected ExecutorService threadRunner;

  /**
   * Properties used in Designer to import from DataFile.
   * Represents the names of the columns to use,
   * where each index corresponds to a single dimension.
   */
  protected List<String> dataFileColumns;


  protected List<String> sheetsColumns;

  /**
   * Properties used in Designer to import from Web components.
   * Represents the names of the columns to use,
   * where each index corresponds to a single dimension.
   */
  protected List<String> webColumns;

  /**
   * Property used in Designer to import from a Data Source.
   * Represents the key value of the value to use from the
   * attached Data Source.
   */
  protected String dataSourceKey;

  private String label;
  private int color;
  private YailList colors;


  private DataSource<?, ?> dataSource; // Attached Chart Data Source

  private String elements; // Elements Designer property

  private boolean initialized = false; // Keep track whether the Screen has already been initialized


  /**
   * Creates a new Chart Data component.
   */
  protected ChartDataBase(Chart chartContainer) {
    this.container = chartContainer;
    chartContainer.addDataComponent(this);

    // Set default properties and instantiate Chart Data Model
    initChartData();
    DataSourceKey("");

    threadRunner = Executors.newSingleThreadExecutor();
  }

  /**
   * Changes the underlying Executor Service of the threadRunner.
   *
   *   Primarily used for testing to inject test/mock ExecutorService
   * classes.
   *
   * @param service new ExecutorService object to use..
   */
  public void setExecutorService(ExecutorService service) {
    threadRunner = service;
  }

  /**
   * Initializes the Chart Data object by setting
   * the default properties and initializing the
   * corresponding ChartDataModel object instance.
   */
  public void initChartData() {
    // Creates a ChartDataModel based on the current
    // Chart type being used.
    dataModel = container.createChartModel();

    // Set default values
    Color(Component.COLOR_BLACK);
    Label("");
    dataModel.view.chart.setOnChartGestureListener(this);
    dataModel.view.chart.setOnChartValueSelectedListener(this);
  }

  /*
   * SimpleProperties
   */

  /**
   * Returns the data series color as an alpha-red-green-blue integer.
   *
   * @return background RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public int Color() {
    return color;
  }

  /**
   * Specifies the data series color as an alpha-red-green-blue integer.
   *
   * @param argb background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty
  public void Color(int argb) {
    color = argb;
    dataModel.setColor(color);
    refreshChart();
  }

  /**
   * Returns the Chart's colors as a List.
   *
   * @return List of colors
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE
  )
  public YailList Colors() {
    return colors;
  }

  /**
   * Specifies the data series colors as a list of alpha-red-green-blue integers.
   *
   *   If there is more data than there are colors, the colors will be alternated
   * in order. E.g. if there are two colors Red and Blue, the colors will be applied
   * in the order: Red, Blue, Red, Blue, ...
   *
   * @param colors List of argb values
   */
  @SimpleProperty
  public void Colors(YailList colors) {
    // TODO: Perhaps a Designer property selector could be devised here to select
    // TODO: the colors of the Chart.

    // Parse the entries of the YailList
    List<Integer> resultColors = new ArrayList<>();

    for (int i = 0; i < colors.size(); ++i) {
      // Get the element of the YailList as a String
      String color = colors.getString(i);

      try {
        // Parse the color value and add it to the results List
        long colorValue = Long.parseLong(color);
        if (colorValue > Integer.MAX_VALUE) {
          // Convert from positive long to negative int for AARRGGBB format
          colorValue = colorValue + 2L * Integer.MIN_VALUE;
        }
        resultColors.add((int) colorValue);
      } catch (NumberFormatException e) {
        // Skip invalid entry
        this.container.$form().dispatchErrorOccurredEvent(this.container,
            "Colors",
            ErrorMessages.ERROR_INVALID_CHART_DATA_COLOR,
            color);
      }
    }

    // Update the Colors YailList variable
    this.colors = YailList.makeList(resultColors);

    // Set the colors from the constructed List of colors
    // and refresh the Chart.
    dataModel.setColors(resultColors);
    refreshChart();
  }

  /**
   * Returns the label text of the data series.
   *
   * @return label text
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Label() {
    return label;
  }

  /**
   * Specifies the text for the data series label.
   *
   * @param text label text
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty
  public void Label(String text) {
    this.label = text;
    dataModel.setLabel(text);
    refreshChart();
  }

  /**
   * Changes the Point Shape of the Data Series, provided that the
   * Data component is attached to a Chart that has the type set to
   * the Scatter Chart. Valid types include circle, square, triangle, cross, x.
   *
   * @param shape the desired shape of the data points
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_POINT_SHAPE,
      defaultValue = "0")
  @SimpleProperty(userVisible = false)
  public void PointShape(PointStyle shape) {

    // Only change the Point Shape if the Chart Data Model is a
    // ScatterChartDataModel (other models do not support changing
    // the Point Shape)
    if (dataModel instanceof ScatterChartDataModel) {
      ((ScatterChartDataModel) dataModel).setPointShape(shape);
    }
  }

  /**
   * Changes the Line Type of the Data Series, provided that the
   * Data component is attached to a Chart that has the type set to
   * a line-based Chart(applies to area and line Chart types).
    Valid types include linear, curved or stepped.
   *
   * @param type the desired style of line type
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_LINE_TYPE,
      defaultValue = "0")
  @SimpleProperty(userVisible = false)
  public void LineType(LineType type) {

    // Only change the Line Type if the Chart Data Model is a
    // LineChartBaseDataModel (other models do not support changing
    // the Line Type)
    if (dataModel instanceof LineChartBaseDataModel) {
      ((LineChartBaseDataModel<?>) dataModel).setLineType(type);
    }
  }

  /*
   * Helper methods & overrides
   */

  /**
   * Imports data from a Data File component, with the specified column names.
   * The method is run asynchronously.
   *
   * @param dataFile Data File component to import from
   * @param columns  list of column names to import from
   */
  protected void importFromDataFileAsync(final DataFile dataFile, YailList columns) {
    // Get the Future object representing the columns in the DataFile component.
    final Future<YailList> dataFileColumns = dataFile.getDataValue(columns);

    // Import the data from the Data file asynchronously
    threadRunner.execute(new Runnable() {
      @SuppressWarnings("TryWithIdenticalCatches")
      @Override
      public void run() {
        YailList dataResult = null;

        try {
          // Get the columns from the DataFile. The retrieval of
          // the result is blocking, so it will first wait for
          // the reading to be processed.
          dataResult = dataFileColumns.get();
        } catch (InterruptedException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        } catch (ExecutionException e) {
          Log.e(this.getClass().getName(), e.getMessage());
        }

        // Import from Data file with the specified parameters
        dataModel.importFromColumns(dataResult, true);

        // Refresh the Chart after import
        refreshChart();
      }
    });
  }

  /**
   * Refreshes the Chart View object with the current up to date
   * Data Series data.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void refreshChart() {
    // Update the Chart with the Chart Data Model's current
    // data and refresh the Chart itself.
    container.getChartView().refresh((ChartDataModel) dataModel);
  }

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return this.container.getDispatchDelegate();
  }

  /**
   * Links the Data Source component with the Data component, if
   * the Source component has been defined earlier.
   *
   *   The reason this is done is because otherwise exceptions
   * are thrown if the Data is being imported before the component
   * is fully initialized.
   */
  public void Initialize() {
    initialized = true;

    // Data Source should only be imported after the Screen
    // has been initialized, otherwise some exceptions may occur
    // on small data sets with regards to Chart refreshing.
    if (dataSource != null) {
      Source(dataSource);
      refreshChart();
    } else {
      // If no Source is specified, the ElementsFromPairs
      // property can be set instead. Otherwise, this is not
      // set to prevent data overriding.
      ElementsFromPairs(elements);
      refreshChart();
    }
  }

  /**
   * Checks whether the provided key is compatible based on the current set
   * Data Source key.
   *
   * @param key Key to check
   * @return True if the key is equivalent to the current Data Source key
   */
  private boolean isKeyValid(String key) {
    // The key should either be equal to the local key, or null.
    return (key == null || key.equals(dataSourceKey));
  }

  @Override
  public void onChartGestureStart(MotionEvent motionEvent,
      ChartTouchListener.ChartGesture chartGesture) {

  }

  @Override
  public void onChartGestureEnd(MotionEvent motionEvent,
      ChartTouchListener.ChartGesture chartGesture) {

  }

  @Override
  public void onChartLongPressed(MotionEvent motionEvent) {

  }

  @Override
  public void onChartDoubleTapped(MotionEvent motionEvent) {

  }

  @Override
  public void onChartSingleTapped(MotionEvent motionEvent) {

  }

  @Override
  public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

  }

  @Override
  public void onChartScale(MotionEvent motionEvent, float v, float v1) {

  }

  @Override
  public void onChartTranslate(MotionEvent motionEvent, float v, float v1) {

  }

  @Override
  public void onValueSelected(final Entry entry, Highlight highlight) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (entry instanceof PieEntry) {
          EntryClick(((PieEntry) entry).getLabel(), ((PieEntry) entry).getValue());
        } else {
          EntryClick(entry.getX(), entry.getY());
        }
      }
    });
  }

  /**
   * Indicates that the user tapped on a data point in the chart. The x and y values of the
   * tapped entry are reported.
   *
   * @param x the x position of the clicked entry
   * @param y the y position of the clicked entry
   */
  @SimpleEvent()
  public void EntryClick(Object x, double y) {
    EventDispatcher.dispatchEvent(this, "EntryClick", x, y);
    container.EntryClick(this, x, y);
  }

  @Override
  public void onNothingSelected() {

  }
}
