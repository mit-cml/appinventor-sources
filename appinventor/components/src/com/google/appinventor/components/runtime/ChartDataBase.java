// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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

/**
 * Base class for Chart Data components. Contains functionality common
 * to any Chart Data component. The component corresponds to a single
 * Data Series in a Chart, and can be attached to a Chart component.
 * Right now, the only extension is the ChartData2D component, but the
 * base class was created with future extensions (e.g. 3D data) in mind.
 */
@SuppressWarnings({"checkstyle:JavadocParagraph"})
@SimpleObject
public abstract class ChartDataBase extends DataCollection<Chart, ChartDataModel<?, ?, ?, ?, ?>>
    implements ChartComponent, OnChartGestureListener, OnChartValueSelectedListener {

  private String label;
  private int color;
  private YailList colors;

  private int dataLabelColor;

  /**
   * Creates a new Chart Data component.
   */
  protected ChartDataBase(Chart chartContainer) {
    super(chartContainer);
    chartContainer.addDataComponent(this);

    // Set default properties and instantiate Chart Data Model
    initChartData();
    Color(Component.COLOR_BLACK);
    DataSourceKey("");
    Label("");
    DataLabelColor(chartContainer.$form().isDarkTheme() ? Component.COLOR_BLACK : Component.COLOR_WHITE);
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
    dataModel.view.chart.setOnChartGestureListener(this);
    dataModel.view.chart.setOnChartValueSelectedListener(this);
  }

  public ChartDataModel<?, ?, ?, ?, ?> getDataModel() {
    return dataModel;
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
    onDataChange();
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
    onDataChange();
  }

  /**
   * Returns the data label color as an alpha-red-green-blue integer.
   *
   * @return background RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public int DataLabelColor() {
    return dataLabelColor;
  }

  /**
   * Specifies the data points label color as an alpha-red-green-blue integer.
   *
   * @param argb background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty
  public void DataLabelColor(int argb) {
    dataLabelColor = argb;
    dataModel.setDataLabelColor(argb);
    onDataChange();
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
    onDataChange();
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
  @SimpleProperty(userVisible = false, category = PropertyCategory.APPEARANCE)
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
  @SimpleProperty(userVisible = false, category = PropertyCategory.APPEARANCE)
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
   * Refreshes the Chart View object with the current up to date
   * Data Series data.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void onDataChange() {
    // Update the Chart with the Chart Data Model's current
    // data and refresh the Chart itself.
    container.getChartView().refresh((ChartDataModel) dataModel);
    for (DataSourceChangeListener listener : listeners) {
      listener.onDataSourceValueChange(this, null, null);
    }
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
