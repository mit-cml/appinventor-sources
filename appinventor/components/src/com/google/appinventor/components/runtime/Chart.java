// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;

import com.google.appinventor.components.common.ChartType;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;

/**
 * The Chart component plots data originating from it's attached Data components. Five different
 * Chart types are available, including Line, Area, Scatter, Bar and Pie, which can be changed by
 * the {@link #Type(ChartType)} property.
 * The Chart component itself has various other properties that change the appearance
 * of the Chart, such as {{@link #Description(String)}, {@link #GridEnabled(boolean)},
 * @link #Labels(YailList)} and {@link #LegendEnabled(boolean)}.
 *
 * @see com.google.appinventor.components.runtime.ChartDataModel
 * @see com.google.appinventor.components.runtime.ChartView
 */
@SimpleObject
@DesignerComponent(version = YaVersion.CHART_COMPONENT_VERSION,
    category = ComponentCategory.CHARTS,
    description = "A component that allows visualizing data")
@UsesLibraries(libraries = "mpandroidchart.jar")
@SuppressWarnings("checkstyle:JavadocParagraph")
public class Chart extends AndroidViewComponent
    implements ComponentContainer, OnInitializeListener {
  // Root layout of the Chart view. This is used to make Chart
  // dynamic removal & adding easier.
  private final RelativeLayout view;

  // Underlying Chart view
  private ChartView<?, ?, ?, ?, ?> chartView;

  // Properties
  private ChartType type;
  private int backgroundColor;
  private String description;
  private int pieRadius;
  private boolean legendEnabled;
  private boolean gridEnabled;
  private boolean zeroX;
  private boolean zeroY;
  private YailList labels;

  // Synced tick value across all Data Series (used for real-time entries)
  // Start the value from 1 (in contrast to starting from 0 as in Chart
  // Data Base) to lessen off-by-one offsets for multiple Chart Data Series.
  private int tick = 1;

  // Attached Data components
  private final List<ChartComponent> dataComponents;

  /**
   * Creates a new Chart component.
   *
   * @param container container, component will be placed in
   */
  public Chart(ComponentContainer container) {
    super(container);

    view = new RelativeLayout(container.$context());

    // Adds the view to the designated container
    container.$add(this);

    dataComponents = new ArrayList<>();

    // Set default values
    Type(ChartType.Line);
    Width(ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH);
    Height(ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT);
    BackgroundColor(Component.COLOR_DEFAULT);
    Description("");
    PieRadius(100);
    LegendEnabled(true);
    GridEnabled(true);
    Labels(new YailList());
    XFromZero(false);
    YFromZero(false);

    // Register onInitialize event of the Chart
    $form().registerForOnInitialize(this);
  }

  @Override
  public View getView() {
    return view;
  }

  @Override
  public Activity $context() {
    return container.$context();
  }

  @Override
  public Form $form() {
    return container.$form();
  }

  @Override
  public void $add(AndroidViewComponent component) {
    throw new UnsupportedOperationException("ChartBase.$add() called");
  }

  @Override
  public void setChildWidth(AndroidViewComponent component, int width) {
    throw new UnsupportedOperationException("ChartBase.setChildWidth called");
  }

  @Override
  public void setChildHeight(AndroidViewComponent component, int height) {
    throw new UnsupportedOperationException("ChartBase.setChildHeight called");
  }

  @Override
  public List<Component> getChildren() {
    return new ArrayList<Component>(dataComponents);
  }

  /**
   * Specifies the type of the Chart, which determines how to visualize the data.
   *
   * @return the current type of the chart
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "Specifies the chart's type (area, bar, pie, scatter), "
          + "which determines how to visualize the data.")
  public ChartType Type() {
    return type;
  }

  /**
   * Specifies the type of the Chart, which determines how to visualize the data.
   *
   * @param type the desired chart type
   * @throws IllegalArgumentException if shape is not a legal value.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_TYPE,
      defaultValue = "0")
  public void Type(ChartType type) {
    // Keep track whether a ChartView already exists,
    // in which case it will have to be reinitialized.
    boolean chartViewExists = (chartView != null);

    // Create a new Chart view based on the supplied type
    ChartView<?, ?, ?, ?, ?> newChartView = createChartViewFromType(type);

    // ChartView currently exists in root layout. Remove it.
    if (chartViewExists) {
      view.removeView(chartView.getView());
    }

    this.type = type;
    chartView = newChartView;

    // Add the new Chart view as the first child of the root RelativeLayout
    view.addView(chartView.getView(), 0);

    // If a ChartView already existed, then the Chart
    // has to be reinitialized.
    if (chartViewExists) {
      reinitializeChart();
    }
  }

  /**
   * Creates and returns a ChartView object based on the type
   * (integer) provided.
   *
   * @param type the desired chart type to render
   * @return new ChartView instance
   */
  private ChartView<?, ?, ?, ?, ?> createChartViewFromType(ChartType type) {
    switch (type) {
      case Line:
        return new LineChartView(this);
      case Scatter:
        return new ScatterChartView(this);
      case Area:
        return new AreaChartView(this);
      case Bar:
        return new BarChartView(this);
      case Pie:
        return new PieChartView(this);
      default:
        // Invalid argument
        throw new IllegalArgumentException("Invalid Chart type specified: " + type);
    }
  }

  /**
   * Reinitializes the Chart view by reattaching all the Data
   * components and setting back all the properties.
   */
  private void reinitializeChart() {
    // Iterate through all attached Data Components and reinitialize them.
    // This is needed since the Type property is registered only after all
    // the Data components are attached to the Chart.
    // This has no effect when the Type property is default (0), since
    // the Data components are not attached yet, making the List empty.
    for (ChartComponent dataComponent : dataComponents) {
      dataComponent.initChartData();
    }

    Description(description);
    BackgroundColor(backgroundColor);
    LegendEnabled(legendEnabled);
    GridEnabled(gridEnabled);
    Labels(labels);
  }

  /**
   * Returns the description label text of the Chart.
   *
   * @return description label
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Description() {
    return description;
  }

  /**
   * Specifies the text displayed by the description label inside the Chart.
   * Specifying an empty string ("") will not display any label.
   *
   * @param text description
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty
  public void Description(String text) {
    this.description = text;
    chartView.setDescription(description);
  }

  /**
   * Returns the chart's background color as an alpha-red-green-blue
   * integer.
   *
   * @return background RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the chart's background color as an alpha-red-green-blue
   * integer.
   *
   * @param argb background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty
  public void BackgroundColor(int argb) {
    if (argb == Component.COLOR_DEFAULT) {
      argb = $form().isDarkTheme() ? Component.COLOR_BLACK : Component.COLOR_WHITE;
    }
    backgroundColor = argb;
    chartView.setBackgroundColor(argb);
  }

  /**
   * Sets the Pie Radius of the Chart. If the current type is
   * not the Pie Chart, the value has no effect.
   *
   * @internaldoc
   *     The value is hidden in the blocks due to it being applicable
   *     to a single Chart only. TODO: Might be better to change this in the future
   *
   *     TODO: Make this an enum selection in the future? (Donut, Full Pie, Small Donut, etc.)
   *
   * @param percent Percentage of the Pie Chart radius to fill
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_PIE_RADIUS,
      defaultValue = "100")
  @SimpleProperty(description = "Sets the Pie Radius of a Pie Chart from 0% to 100%, where the "
      + "percentage indicates the percentage of the hole fill. 100% means that a full Pie Chart "
      + "is drawn, while values closer to 0% correspond to hollow Pie Charts.",
      userVisible = false,
      category = PropertyCategory.APPEARANCE)
  public void PieRadius(int percent) {
    this.pieRadius = percent;

    // Only set the value if the Chart View is a
    // Pie Chart View; Otherwise take no action.
    if (chartView instanceof PieChartView) {
      ((PieChartView) chartView).setPieRadius(percent);
    }
  }

  /**
   * Returns a boolean indicating whether the legend is enabled
   * on the Chart.
   *
   * @return True if legend is enabled, false otherwise
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public boolean LegendEnabled() {
    return this.legendEnabled;
  }

  /**
   * Changes the visibility of the Chart's Legend.
   *
   * @param enabled indicates whether the Chart should be enabled.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void LegendEnabled(boolean enabled) {
    this.legendEnabled = enabled;
    chartView.setLegendEnabled(enabled);
    view.invalidate();
    chartView.refresh();
  }

  /**
   * Returns a boolean indicating whether the grid is enabled
   * on the Chart.
   *
   * @return True if grid is enabled, false otherwise
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public boolean GridEnabled() {
    return this.gridEnabled;
  }

  /**
   * Changes the visibility of the Chart's grid, if the
   * Chart Type is set to a Chart with an Axis (applies for Area, Bar, Line,
   * Scatter Chart types).
   *
   * @param enabled indicates whether the Chart's grid should be enabled.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void GridEnabled(boolean enabled) {
    this.gridEnabled = enabled;

    // Only change grid visibility if the Chart View is an
    // Axis Chart View, since non-axis Charts do not have
    // grids.
    if (chartView instanceof AxisChartView) {
      ((AxisChartView<?, ?, ?, ?, ?>) chartView).setGridEnabled(enabled);
      view.invalidate();
      chartView.refresh();
    }
  }

  /**
   * Returns a List of Labels set to the X Axis.
   *
   * @return List of Labels used for the X Axis
   */
  @SimpleProperty
  public YailList Labels() {
    return labels;
  }

  /**
   * Changes the Chart's X axis labels to the specified List,
   * if the Chart's Type is set to a Chart with an Axis.
   *
   *   The first entry of the List corresponds to the minimum x value of the data,
   * the second to the min x value + 1, and so on.
   *
   *   If a label is not specified for an x value, a default value
   * is used (the x value of the axis tick at that location).
   *
   * @param labels List of labels to set to the X Axis of the Chart
   */
  @SimpleProperty(description = "Changes the Chart's X axis labels to the specified List of "
      + "Strings,  provided that the Chart Type is set to a Chart with an Axis (applies to Area, "
      + "Bar, Line, Scatter Charts). The labels are applied in order, starting from the smallest "
      + "x value on the Chart, and continuing in order. If a label is not specified for an x "
      + "value, a default value is used (the x value of the axis tick at that location).")
  public void Labels(YailList labels) {
    this.labels = labels;

    // Only change the labels if the Chart View is
    // an Axis Chart View, since Charts without an
    // axis will not have an X Axis.
    if (chartView instanceof AxisChartView) {
      List<String> stringLabels = new ArrayList<>();

      for (int i = 0; i < labels.size(); ++i) {
        String label = labels.getString(i);
        stringLabels.add(label);
      }

      ((AxisChartView<?, ?, ?, ?, ?>) chartView).setLabels(stringLabels);
    }
  }


  /**
   * Specifies the labels to set to the Chart's X Axis, provided the current
   * view is a Chart with an X Axis. The labels are specified as a single comma-separated
   * values String (meaning each value is separated by a comma). See {@link #Labels(YailList)}
   * for more details on how the Labels are applied to the Chart.
   *
   * @param labels Comma-separated values, where each value represents a label (in order)
   * @see #Labels(YailList)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(userVisible = false, category = PropertyCategory.APPEARANCE)
  public void LabelsFromString(String labels) {
    // Retrieve the elements from the CSV-formatted String
    YailList labelsList = ElementsUtil.elementsFromString(labels);
    Labels(labelsList); // Set the Labels from the retrieved elements List
  }

  /**
   * Determines whether the X axis origin is set at 0 or the minimum X value
   * across all data series.
   *
   * @param zero true if the X-axis origin should be fixed at zero
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void XFromZero(boolean zero) {
    this.zeroX = zero;

    if (chartView instanceof AxisChartView) {
      ((AxisChartView<?, ?, ?, ?, ?>) chartView).setXMinimum(zero);
    }
  }

  @SimpleProperty
  public boolean XFromZero() {
    return zeroX;
  }

  /**
   * Determines whether the Y axis origin is set at 0 or the minimum y value
   * across all data series.
   *
   * @param zero true if the Y-axis origin should be fixed at zero
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void YFromZero(boolean zero) {
    this.zeroY = zero;

    if (chartView instanceof AxisChartView) {
      ((AxisChartView<?, ?, ?, ?, ?>) chartView).setYMinimum(zero);
    }
  }

  @SimpleProperty
  public boolean YFromZero() {
    return this.zeroY;
  }

  /**
   * Extends the domain of the chart to include the provided x value. If x is already within the
   * bounds of the domain, this method has no effect.
   *
   * @param x the value to show
   */
  @SimpleFunction
  public void ExtendDomainToInclude(double x) {
    if (chartView instanceof AxisChartView) {
      double[] bounds = ((AxisChartView<?, ?, ?, ?, ?>) chartView).getXBounds();
      if (x < bounds[0]) {
        ((AxisChartView<?, ?, ?, ?, ?>) chartView).setXBounds(x, bounds[1]);
      } else if (x > bounds[1]) {
        ((AxisChartView<?, ?, ?, ?, ?>) chartView).setXBounds(bounds[0], x);
      } else {
        return;
      }
      chartView.refresh();
    }
  }

  /**
   * Extends the range of the chart to include the provided y value. If y is already within the
   * bounds of the range, this method has no effect.
   *
   * @param y the value to show
   */
  @SimpleFunction
  public void ExtendRangeToInclude(double y) {
    if (chartView instanceof AxisChartView) {
      double[] bounds = ((AxisChartView<?, ?, ?, ?, ?>) chartView).getYBounds();
      if (y < bounds[0]) {
        ((AxisChartView<?, ?, ?, ?, ?>) chartView).setYBounds(y, bounds[1]);
      } else if (y > bounds[1]) {
        ((AxisChartView<?, ?, ?, ?, ?>) chartView).setYBounds(bounds[0], y);
      } else {
        return;
      }
      chartView.refresh();
    }
  }

  /**
   * Resets the axes of the chart to their original bounds.
   */
  @SimpleFunction
  public void ResetAxes() {
    if (chartView instanceof AxisChartView) {
      ((AxisChartView<?, ?, ?, ?, ?>) chartView).resetAxes();
      refresh();
    }
  }

  /**
   * Sets the minimum and maximum for the domain of the X axis.
   *
   * @param minimum the lower bound for the domain
   * @param maximum the upper bound for the domain
   */
  @SimpleFunction
  public void SetDomain(double minimum, double maximum) {
    this.zeroX = minimum == 0.0;

    if (chartView instanceof AxisChartView) {
      ((AxisChartView<?, ?, ?, ?, ?>) chartView).setXBounds(minimum, maximum);
      refresh();
    }
  }

  /**
   * Sets the minimum and maximum for the range of the Y axis.
   *
   * @param minimum the lower bound for the range
   * @param maximum the upper bound for the range
   */
  @SimpleFunction
  public void SetRange(double minimum, double maximum) {
    this.zeroY = minimum == 0.0;

    if (chartView instanceof AxisChartView) {
      ((AxisChartView<?, ?, ?, ?, ?>) chartView).setYBounds(minimum, maximum);
      refresh();
    }
  }

  /**
   * Indicates that the user clicked on a data entry in the `Chart`. The specific series, along
   * with its x and y values, are reported.
   *
   * @param series the series clicked on
   * @param x the x position of the clicked entry
   * @param y the y position of the clicked entry
   */
  @SimpleEvent
  public void EntryClick(Component series, Object x, double y) {
    EventDispatcher.dispatchEvent(this, "EntryClick", series, x, y);
  }

  /**
   * Creates a new instance of a ChartDataModel, corresponding
   * to the current Chart type.
   *
   * @return new ChartDataModel object instance
   */
  public ChartDataModel<?, ?, ?, ?, ?> createChartModel() {
    return chartView.createChartModel();
  }

  /**
   * Refreshes the Chart View.
   */
  public void refresh() {
    chartView.refresh();
  }

  /**
   * Returns the underlying Chart View object.
   *
   * @return Chart View object
   */
  public ChartView<?, ?, ?, ?, ?> getChartView() {
    return chartView;
  }

  /**
   * Attach a Data Component to the Chart.
   *
   * @param dataComponent Data component object instance to add
   */
  public void addDataComponent(ChartComponent dataComponent) {
    dataComponents.add(dataComponent);
  }

  @Override
  public void onInitialize() {
    // If the Chart View is of type PieChartView, the
    // radius of the Chart has to be set after initialization
    // due to the method relying on retrieving width and height
    // via getWidth() and getHeight(), which only return non-zero
    // values after the Screen is initialized.
    if (chartView instanceof PieChartView) {
      ((PieChartView) chartView).setPieRadius(pieRadius);
      chartView.refresh();
    }
  }

  /**
   * Returns the t value to use for time entries for a
   * Data Series of this Chart component.
   *
   * <p>Takes in the t value of a Data Series as an argument
   * to determine a value tailored to the Data Series, while
   * updating the synced t value of the Chart component.
   *
   * <p>This method primarily takes care of syncing t values
   * across all the Data Series of the Chart for consistency.
   *
   * @param dataSeriesT t value of a Data Series
   * @return t value to use for the next time entry based on the specified parameter
   */
  public int getSyncedTValue(int dataSeriesT) {
    int returnValue;

    // If the difference between the global t and the Data Series' t
    // value is more than one, that means the Data Series' t value
    // is out of sync and must be updated.
    if (tick - dataSeriesT > 1) {
      returnValue = tick;
    } else {
      returnValue = dataSeriesT;
    }

    // Since the returnValue is either bigger or equal to t,
    // the new synchronized t value should be 1 higher than
    // the return value (since immediately after getting the
    // t value, the value will be incremented either way)
    tick = returnValue + 1;

    // Return the calculated t value
    return returnValue;
  }
}
