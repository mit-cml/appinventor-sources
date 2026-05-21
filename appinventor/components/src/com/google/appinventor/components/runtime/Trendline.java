// -*- mode: java -*-; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.DashPathEffect;
import android.util.Log;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.BestFitModel;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.LOBFValues;
import com.google.appinventor.components.common.LinearRegression;
import com.google.appinventor.components.common.OptionList;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.StrokeStyle;
import com.google.appinventor.components.common.TrendlineCalculator;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ExponentialRegression;
import com.google.appinventor.components.runtime.util.HasTrendline;
import com.google.appinventor.components.runtime.util.LogarithmicRegression;
import com.google.appinventor.components.runtime.util.QuadraticRegression;
import com.google.appinventor.components.runtime.util.YailDictionary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Trendline component can be used to visualize the trend of a data series represented by a
 * ChartData2D component. It must be added to a Chart component. To associate a ChartData2D
 * instance, either set the ChartData property in the design view of the app or use the setter
 * block. The Trendline will update automatically if its associated ChartData2D is changed.
 *
 * There are four models available for the Trendline: Linear, Quadratic, Logarithmic, and
 * Exponential. Depending on which model you use, certain properties of the Trendline component
 * will provide relevant values.
 *
 *   * Linear: y = m*x + b, where m is LinearCoefficient and b is YIntercept
 *   * Quadratic: y = a\*x<sup>2</sup> + b*x + c, where a is QuadraticCoefficient, b is
 *     LinearCoefficient, and c is YIntercept
 *   * Logarithmic: y = a + b*ln(x), where a is LogarithmConstant and b is LogarithmCoefficient
 *   * Exponential: y = a*b<sup>x</sup>, where a is the ExponentialCoefficient and b is the
 *     ExponentialBase
 *
 * For all models, the r<sup>2</sup> correlation will be reported through the RSquared property
 * block.
 */
@DesignerComponent(version = YaVersion.TRENDLINE_COMPONENT_VERSION,
    description = "A component that predicts a best fit model for a given data series.",
    category = ComponentCategory.CHARTS,
    iconName = "images/trendline.png")
@UsesLibraries("commons-math3.jar")
public class Trendline implements ChartComponent, DataSourceChangeListener {
  private static final String LOG_TAG = Trendline.class.getSimpleName();
  private static final boolean DEBUG = false;
  private static final YailDictionary.KeyTransformer ENUM_KEY_TRANSFORMER =
      new YailDictionary.KeyTransformer() {
        @Override
        public Object transform(Object key) {
          if (key instanceof OptionList) {
            return ((OptionList<?>) key).toUnderlyingValue();
          }
          return key;
        }
      };

  protected String componentName;

  private final DashPathEffect dashed;
  private final DashPathEffect dotted;
  private final Chart container;
  private ChartData2D chartData = null;
  private int color = COLOR_DEFAULT;
  private boolean extend = true;
  private BestFitModel model = BestFitModel.Linear;
  private double strokeWidth = 1.0;
  private StrokeStyle strokeStyle = StrokeStyle.Solid;
  private boolean visible = true;
  private final LinearRegression regression = new LinearRegression();
  private final QuadraticRegression quadraticRegression = new QuadraticRegression();
  private final ExponentialRegression exponentialRegression = new ExponentialRegression();
  private final LogarithmicRegression logarithmicRegression = new LogarithmicRegression();
  private TrendlineCalculator currentModel = regression;
  private Map<String, Object> lastResults = new HashMap<>();
  private boolean initialized = false;
  private DataModel<?> dataModel = null;
  private double minX = Double.POSITIVE_INFINITY;
  private double maxX = Double.NEGATIVE_INFINITY;
  private final float density;

  /**
   * Constructs a new Trendline component to be rendered on the given {@code chartContainer}.
   *
   * @param chartContainer the chart that contain the line of best fit
   */
  public Trendline(Chart chartContainer) {
    density = chartContainer.$form().deviceDensity();
    container = chartContainer;
    container.addDataComponent(this);
    dashed = new DashPathEffect(new float[]{10f * density, 10f * density}, 0f);
    dotted = new DashPathEffect(new float[]{2f * density, 10f * density}, 0f);
  }

  @Override
  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  /**
   * Called from call-Initialize-of-components, initializes the line of best fit data object
   * if not already done so.
   */
  public void Initialize() {
    initialized = true;
    if (dataModel == null) {
      initChartData();
    }
  }

  // region Component implementation

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return container.getDispatchDelegate();
  }

  // endregion

  // region DataSourceChangeListener implementation

  @Override
  public void onDataSourceValueChange(DataSource<?, ?> component, String key, Object newValue) {
    lastResults.clear();
    Object value = component.getDataValue(null);
    if (DEBUG) {
      Log.d(LOG_TAG, "onDataSourceValueChange");
      Log.d(LOG_TAG, "value = " + value);
    }
    if (!(value instanceof List)) {
      return;
    }
    List<?> entries = (List<?>) value;
    List<Double> x = new ArrayList<>();
    List<Double> y = new ArrayList<>();
    minX = Double.POSITIVE_INFINITY;
    maxX = Double.NEGATIVE_INFINITY;
    for (Object o : entries) {
      if (o instanceof Entry) {
        Entry entry = (Entry) o;
        double currentX = entry.getX();
        if (currentX < minX) {
          minX = currentX;
        }
        if (currentX > maxX) {
          maxX = currentX;
        }
        x.add(currentX);
        y.add((double) entry.getY());
      }
    }
    if (x.isEmpty()) {
      Log.w(LOG_TAG, "No entries in the data source");
      return;
    } else if (x.size() < 2) {
      Log.w(LOG_TAG, "Not enough entries in the data source");
      return;
    } else if (x.size() != y.size()) {
      Log.w(LOG_TAG, "Must have equal X and Y data points");
      return;
    }
    lastResults = currentModel.compute(x, y);
    if (DEBUG) {
      Log.d(LOG_TAG, "lastResults = " + lastResults);
    }
    if (initialized) {
      final YailDictionary results = new YailDictionary(lastResults, ENUM_KEY_TRANSFORMER);
      container.$form().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Updated(results);
          if (visible) {
            container.getChartView().getView().invalidate();
          }
        }
      });
    }
  }

  @Override
  public void onReceiveValue(RealTimeDataSource<?, ?> component, String key, Object value) {

  }

  // endregion

  // region Properties

  /**
   * The data series for which to compute the line of best fit.
   *
   * @param chartData the data series for which to compute the line of best fit
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT
      + ":com.google.appinventor.component.runtime.ChartData2D")
  @SimpleProperty(description = "The data series for which to compute the line of best fit.",
      category = PropertyCategory.BEHAVIOR)
  public void ChartData(ChartData2D chartData) {
    if (this.chartData != null) {
      this.chartData.removeDataSourceChangeListener(this);
    }
    this.chartData = chartData;
    if (chartData != null) {
      chartData.addDataSourceChangeListener(this);
    }
  }

  /**
   * The color of the line of best fit.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty(description = "The color of the line of best fit.",
      category = PropertyCategory.APPEARANCE)
  public void Color(int color) {
    this.color = color;
    if (initialized) {
      container.refresh();
    }
  }

  @SimpleProperty
  @IsColor
  public int Color() {
    return color;
  }

  /**
   * The correlation coefficient of the trendline to the data.
   */
  @SimpleProperty
  public double CorrelationCoefficient() {
    return resultOrNan((Double) lastResults.get("correlation coefficient"));
  }

  /**
   * The base of the exponential term in the equation y = a*b^x.
   */
  @SimpleProperty
  public double ExponentialBase() {
    return resultOrNan((Double) lastResults.get("b"));
  }

  /**
   * The coefficient of the exponential term in the equation y = a*b^x.
   */
  @SimpleProperty
  public double ExponentialCoefficient() {
    return resultOrNan((Double) lastResults.get("a"));
  }

  /**
   * Whether to extend the line of best fit beyond the data.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(description = "Whether to extend the line of best fit beyond the data.",
      category = PropertyCategory.BEHAVIOR)
  public void Extend(boolean extend) {
    this.extend = extend;
    if (initialized) {
      container.refresh();
    }
  }

  @SimpleProperty
  public boolean Extend() {
    return extend;
  }

  /**
   * The coefficient of the linear term in the trendline.
   */
  @SimpleProperty
  public double LinearCoefficient() {
    return resultOrNan((Double) lastResults.get("slope"));
  }

  /**
   * The coefficient of the logarithmic term in the equation y = a + b*ln(x).
   */
  @SimpleProperty
  public double LogarithmCoefficient() {
    return resultOrNan((Double) lastResults.get("b"));
  }

  /**
   * The constant term in the logarithmic equation y = a + b*ln(x).
   */
  @SimpleProperty
  public double LogarithmConstant() {
    return resultOrNan((Double) lastResults.get("a"));
  }

  /**
   * The model to use for the line of best fit.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BEST_FIT_MODEL,
      defaultValue = "Linear")
  @SimpleProperty(description = "The model to use for the line of best fit.",
      category = PropertyCategory.BEHAVIOR)
  public void Model(BestFitModel model) {
    this.model = model;
    switch (model) {
      case Linear:
        currentModel = regression;
        break;
      case Quadratic:
        currentModel = quadraticRegression;
        break;
      case Exponential:
        currentModel = exponentialRegression;
        break;
      case Logarithmic:
        currentModel = logarithmicRegression;
        break;
      default:
        throw new IllegalArgumentException("Unknown model: " + model);
    }
    if (initialized) {
      container.refresh();
    }
  }

  @SimpleProperty
  public BestFitModel Model() {
    return model;
  }

  /**
   * The predictions for the trendline.
   */
  @SimpleProperty
  public List<Double> Predictions() {
    Object value = lastResults.get("predictions");

    if (value instanceof List) {
      //noinspection unchecked
      return (List<Double>) value;
    }

    // In theory, we should always have a list of predictions, but in case we don't return a
    // sensible default.
    return new ArrayList<>();
  }

  /**
   * The coefficient of the quadratic term in the trendline, if any.
   */
  @SimpleProperty
  public double QuadraticCoefficient() {
    return resultOrZero((Double) lastResults.get("x^2"));
  }

  /**
   * Obtain a copy of the most recent values computed by the line of best fit.
   */
  @SimpleProperty
  public YailDictionary Results() {
    return new YailDictionary(lastResults, ENUM_KEY_TRANSFORMER);
  }

  /**
   * The r-squared coefficient of determination for the trendline.
   */
  @SimpleProperty
  public double RSquared() {
    return resultOrNan((Double) lastResults.get("r^2"));
  }

  /**
   * The style of the best fit line.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STROKE_STYLE,
      defaultValue = "1")
  @SimpleProperty(description = "The style of the best fit line.",
      category = PropertyCategory.APPEARANCE)
  public void StrokeStyle(StrokeStyle strokeStyle) {
    this.strokeStyle = strokeStyle;
    if (initialized) {
      container.refresh();
    }
  }

  /**
   * Sets the stroke style from its underlying integer value.
   */
  @SuppressWarnings("checkstyle:MethodName")
  public void StrokeStyle(int value) {
    StrokeStyle strokeStyle = StrokeStyle.fromUnderlyingValue(value);
    if (strokeStyle != null) {
      StrokeStyle(strokeStyle);
    }
  }

  /**
   * Sets the stroke style from a string representation, such as by the designer.
   */
  @SuppressWarnings({"unused", "checkstyle:MethodName"})  // called from YAIL
  public void StrokeStyle(String value) {
    try {
      StrokeStyle(Integer.parseInt(value));
    } catch (NumberFormatException e) {
      // Ignore.
    }
  }

  @SimpleProperty
  public StrokeStyle StrokeStyle() {
    return strokeStyle;
  }

  /**
   * The width of the best fit line.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "1.0")
  @SimpleProperty(description = "The width of the best fit line.",
      category = PropertyCategory.APPEARANCE)
  public void StrokeWidth(double strokeWidth) {
    this.strokeWidth = strokeWidth;
    if (initialized) {
      container.refresh();
    }
  }

  @SimpleProperty
  public double StrokeWidth() {
    return strokeWidth;
  }

  /**
   * Whether the line of best fit is visible.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Visible(boolean visible) {
    this.visible = visible;
    if (initialized) {
      container.refresh();
    }
  }

  @SimpleProperty
  public boolean Visible() {
    return visible;
  }

  /**
   * The X-intercepts of the trendline (where the line crosses the X-axis), if any. Possible
   * values are NaN (no intercept), a single value (one intercept), or a list of values.
   */
  @SimpleProperty
  public Object XIntercepts() {
    Object result = lastResults.get("Xintercepts");
    return result == null ? Double.NaN : result;
  }

  /**
   * The Y-intercept of the trendline (constant term).
   */
  @SimpleProperty
  public double YIntercept() {
    if (lastResults.containsKey("Yintercept")) {
      return (Double) lastResults.get("Yintercept");
    } else if (lastResults.containsKey("intercept")) {
      return (Double) lastResults.get("intercept");
    }
    return Double.NaN;
  }

  // endregion

  // region Methods

  /**
   * Disconnect the Trendline from a previously associated ChartData2D.
   */
  @SimpleFunction
  public void DisconnectFromChartData() {
    if (chartData != null) {
      chartData.removeDataSourceChangeListener(this);
    }
    lastResults.clear();
    container.refresh();
  }

  /**
   * Get the field of the most recent values computed by the line of best fit. The available
   * values vary based on the model used. For example, a linear model will have slope and
   * Yintercept fields whereas a quadratic model will have x^2, slope, and intercept fields.
   *
   * @param value the value to get
   * @return the value of the field, or NaN if the field is undefined
   */
  @SimpleFunction
  public Object GetResultValue(@Options(LOBFValues.class) String value) {
    if (lastResults.containsKey(value)) {
      return lastResults.get(value);
    }
    return Double.NaN;
  }

  // endregion

  // region Events

  /**
   * Event indicating that the line of best fit has been updated.
   */
  @SimpleEvent
  public void Updated(YailDictionary results) {
    EventDispatcher.dispatchEvent(this, "Updated", results);
  }

  // endregion

  @Override
  public void initChartData() {
    Log.d(LOG_TAG, "initChartData view is " + container.getChartView());
    if (container.getChartView() instanceof ScatterChartView) {
      dataModel = new ScatterChartBestFitModel();
    } else if (container.getChartView() instanceof PointChartView) {
      dataModel = new LineChartBestFitModel();
    }
  }

  @Override
  public ChartDataModel<?, ?, ?, ?, ?> getDataModel() {
    return null;
  }

  private DashPathEffect getDashPathEffect() {
    switch (strokeStyle) {
      case Dashed:
        return dashed;
      case Dotted:
        return dotted;
      case Solid:
      default:
        return null;
    }
  }

  private float[] getPoints(float xMin, float xMax, int viewWidth) {
    if (!initialized || chartData == null) {
      return new float[0];
    }
    if (!extend) {
      xMin = Math.max(xMin, (float) minX);
      xMax = Math.min(xMax, (float) maxX);
    }
    final int strokeStep;
    switch (strokeStyle) {
      case Dashed:
        strokeStep = 20;
        break;
      case Dotted:
        strokeStep = 12;
        break;
      case Solid:
      default:
        strokeStep = 1;
        break;
    }
    final int steps = (int) Math.ceil(viewWidth / (density * strokeStep));
    return currentModel.computePoints(lastResults, xMin, xMax, viewWidth, steps);
  }

  private int getColor() {
    if (color == COLOR_DEFAULT && chartData != null) {
      int color = chartData.Color();
      int alpha = (color >> 24) & 0xFF;
      return (color & 0x00FFFFFF) | ((alpha / 2) << 24);
    }
    return color;
  }

  private static double resultOrNan(Double value) {
    return value == null ? Double.NaN : value;
  }

  private static double resultOrZero(Double value) {
    return value == null ? 0.0 : value;
  }

  private class ScatterChartBestFitDataSet extends ScatterDataSet implements
      HasTrendline<Entry> {

    public ScatterChartBestFitDataSet() {
      super(new ArrayList<Entry>(), "Best Fit");
    }


    @Override
    public float[] getPoints(float xMin, float xMax, int viewWidth) {
      return Trendline.this.getPoints(xMin, xMax, viewWidth);
    }

    @Override
    public int getColor() {
      return Trendline.this.getColor();
    }

    @Override
    public DashPathEffect getDashPathEffect() {
      return Trendline.this.getDashPathEffect();
    }

    @Override
    public float getLineWidth() {
      return (float) strokeWidth * container.$form().deviceDensity();
    }

    @Override
    public boolean isVisible() {
      return Trendline.this.visible;
    }

    @Override
    public String toString() {
      if (chartData != null) {
        return chartData.dataModel.dataset.toString();
      } else {
        return super.toString();
      }
    }
  }

  private class ScatterChartBestFitModel extends ScatterChartDataModel {
    public ScatterChartBestFitModel() {
      super((ScatterData) container.getChartView().data,
          (ScatterChartView) container.getChartView(),
          new ScatterChartBestFitDataSet());
    }
  }

  private class LineChartBestFitDataSet extends LineDataSet implements HasTrendline<Entry> {

    public LineChartBestFitDataSet() {
      super(new ArrayList<Entry>(), "Best Fit");
    }

    @Override
    public float[] getPoints(float xMin, float xMax, int viewWidth) {
      return Trendline.this.getPoints(xMin, xMax, viewWidth);
    }


    @Override
    public int getColor() {
      return Trendline.this.getColor();
    }

    @Override
    public DashPathEffect getDashPathEffect() {
      return Trendline.this.getDashPathEffect();
    }

    @Override
    public float getLineWidth() {
      return (float) strokeWidth * container.$form().deviceDensity();
    }

    @Override
    public boolean isVisible() {
      return Trendline.this.visible;
    }

    @Override
    public String toString() {
      if (chartData != null) {
        return chartData.dataModel.dataset.toString();
      } else {
        return super.toString();
      }
    }
  }

  private class LineChartBestFitModel extends LineChartDataModel {
    public LineChartBestFitModel() {
      super((LineData) container.getChartView().data,
          (LineChartView) container.getChartView(),
          new LineChartBestFitDataSet());
    }
  }

}
