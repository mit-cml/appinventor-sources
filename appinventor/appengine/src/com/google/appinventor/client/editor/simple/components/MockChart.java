// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;

import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.properties.EditableProperty;

import com.google.appinventor.components.common.ChartType;
import com.google.appinventor.components.common.ComponentConstants;

import java.util.List;

import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

/**
 * Central Chart component available to use for the users.
 * Supports multiple types and handling all property changes.
 *
 * <p>The class handles using the correct Chart view based on
 * the current selected Chart type, hiding/showing properties according
 * to the selected Chart type and contains functionality related to
 * checking compatibility with attaching Chart Data objects to the Chart.
 *
 * <p>The class extends from McokContainer due to the Chart component
 * holding ChartData components as children.
 */
public final class MockChart extends MockContainer {
  public static final String TYPE = "Chart";

  private static final String PROPERTY_NAME_TYPE = "Type";
  private static final String PROPERTY_NAME_DESCRIPTION = "Description";
  private static final String PROPERTY_NAME_LEGEND_ENABLED = "LegendEnabled";
  private static final String PROPERTY_NAME_GRID_ENABLED = "GridEnabled";
  private static final String PROPERTY_NAME_PIE_RADIUS = "PieRadius";
  private static final String PROPERTY_NAME_LABELS_FROM_STRING = "LabelsFromString";

  static {
    ResourcesType.setClientBundle(EmbeddedResources.INSTANCE);
  }

  public interface MockChartClient {
    void addToChart(MockChart chart);
  }

  private MockChartView<?, ?, ?> chartView;

  // Legal values for type are defined in
  // com.google.appinventor.components.common.ComponentConstants.java.
  private ChartType type;

  // Keep track whether the children of the Mock Chart have been
  // reattached. The reattachment has to happen only once, since the Data
  // Series are part of the Chart object itself.
  private boolean childrenReattached = false;

  /**
   * Creates a new instance of a visible component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockChart(SimpleEditor editor) {
    super(editor, TYPE, images.chart(), new MockChartLayout());

    // Since the Mock Chart component is not a container in a normal
    // sense (attached components should not be visible), the Chart Widget
    // is added to the root panel, and the root panel itself is initialized.
    // This is done to ensure that Mock Chart Data components can be dragged
    // onto the Chart itself, rather than outside the Chart component.
    rootPanel.setStylePrimaryName("ode-SimpleMockComponent");

    // Since default type property does not invoke the setter,
    // initially, the Chart's type setter should be invoked
    // with the default value.
    setTypeProperty("0");

    initComponent(rootPanel);
  }

  @Override
  protected void onAttach() {
    super.onAttach();

    // The Children of the Mock Chart have not yet been attached
    // (this happens upon initializing the Chart which has child components)
    if (!childrenReattached) {
      // Attach all children MockComponents
      for (MockComponent child : children) {
        if (child instanceof MockChartData) {
          // Re-add Data Components to the Mock Chart
          ((MockChartData) child).addToChart(MockChart.this);
        }
      }

      // Update the state of children to reattached
      childrenReattached = true;
    }
  }
  
  /**
   * Sets the type of the Chart to the newly specified value.
   *
   * @param value new Chart type
   */
  private void setTypeProperty(String value) {
    // Update type
    type = ChartType.fromUnderlyingValue(Integer.parseInt(value));

    // Keep track whether this is the first time that
    // the Chart view is being initialized
    boolean chartViewExists = (chartView != null);

    // Remove the current Chart Widget from the root panel (if present)
    if (chartViewExists) {
      rootPanel.remove(chartView.getChartWidget());
    }

    // Create a new Chart view based on the supplied type
    chartView = createMockChartViewFromType(type);

    // Add the Chart Widget to the Root Panel (as the first widget)
    rootPanel.insert(chartView.getChartWidget(), 0);

    // Chart view already existed before, so the new Chart view must
    // be reinitialized.
    if (chartViewExists) {
      reinitializeChart();
    }
  }

  /**
   * Sets the pie radius of the Chart if the current type is
   * a Pie Chart, otherwise does nothing.
   *
   * @param newValue new Pie Radius value (as String)
   */
  private void setPieRadiusProperty(String newValue) {
    // Check if the Chart View is a Pie Chart to
    // change the value
    if (chartView instanceof MockPieChartView) {
      // Parse the value to an integer
      int value = Integer.parseInt(newValue);

      // Change the radius of the Pie Chart & re-draw the Chart
      ((MockPieChartView) chartView).setPieRadius(value);
      chartView.getChartWidget().draw();
    }
  }

  /**
   * Reacts to the LegendEnabled property change by changing
   * the Mock Chart accordingly.
   *
   * @param newValue new value of the property (String)
   */
  private void setLegendEnabledProperty(String newValue) {
    boolean enabled = Boolean.parseBoolean(newValue);
    chartView.setLegendEnabled(enabled);

    chartView.getChartWidget().draw(); // Re-draw the Chart to take effect
  }

  /**
   * Reacts to the LabelsFromString property change by
   * changing the Labels of the Mock Chart accordingly,
   * provided that the Mock Chart has an X Axis.
   *
   * @param labels CSV formatted String representing the labels
   *               to apply to the X axis.
   */
  private void setLabelsFromStringProperty(String labels) {
    // Base case: Empty List of Labels should be an empty array.
    // Store the LabelsFromStrings property parsed result
    String[] labelArray;
    if (labels.equals("")) {
      labelArray = new String[0];
    } else {
      // TODO: Use a CSV split method that supports escaping commas, etc?
      labelArray = labels.split(",");
    }

    // Only update the labels to the Chart if the Chart is of type
    // MockAxisChartView, since the labels apply to the X Axis.
    if (chartView instanceof MockAxisChartView) {
      ((MockAxisChartView<?, ?, ?>) chartView).updateLabels(labelArray);
      refreshChart();
    }
  }

  /**
   * Reacts to the GridEnabled property change by changing
   * the Mock Chart accordingly.
   *
   * @param newValue new value of the property (String)
   */
  private void setGridEnabledProperty(String newValue) {
    // The property should only be reflected on the Chart
    // if the Chart View is an Axis Chart View.
    if (chartView instanceof MockAxisChartView) {
      boolean enabled = Boolean.parseBoolean(newValue);
      ((MockAxisChartView<?, ?, ?>) chartView).setGridEnabled(enabled);

      chartView.getChartWidget().draw(); // Re-draw the Chart to take effect
    }
  }

  /**
   * Changes Chart property visibilities depending on the
   * current type of the Chart.
   *
   * <p>Should be invoked after the Type property is changed.
   */
  private void changeChartPropertyVisibilities() {
    // Handle Pie Chart property hiding
    boolean showPieChartProperties = chartView instanceof MockPieChartView;
    showProperty(PROPERTY_NAME_PIE_RADIUS, showPieChartProperties);

    // Handle Axis Chart property hiding
    boolean showAxisChartProperties = chartView instanceof MockAxisChartView;
    showProperty(PROPERTY_NAME_GRID_ENABLED, showAxisChartProperties);
    showProperty(PROPERTY_NAME_LABELS_FROM_STRING, showAxisChartProperties);

    // If the component is currently selected, re-select it to refresh
    // the Properties panel. isSelected() should only be invoked when
    // the view is in a container, hence the additional check here.
    if (getContainer() != null && isSelected()) {
      onSelectedChange(true);
    }
  }

  /**
   * Creates and returns a new MockChartView object based on the type
   * (integer) provided.
   *
   * @param type Chart type (integer representation)
   * @return new MockChartView object instance
   */
  private MockChartView<?, ?, ?> createMockChartViewFromType(ChartType type) {
    switch (type) {
      case Line:
        return new MockLineChartView();
      case Scatter:
        return new MockScatterChartView();
      case Area:
        return new MockAreaChartView();
      case Bar:
        return new MockBarChartView();
      case Pie:
        return new MockPieChartView();
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
    // Re-set all Chart properties to take effect on
    // the newly instantiated Chart View
    for (EditableProperty property : properties) {
      // The Type property should not be re-set, since
      // this method call is part of the Type setting process.
      if (!property.getName().equals(PROPERTY_NAME_TYPE)) {
        onPropertyChange(property.getName(), property.getValue());
      }
    }

    chartView.getChartWidget().draw();

    // Re-attach all children MockChartData components.
    // This is needed since the properties of the MockChart
    // are set after the Data components are attached to
    // the Chart, and thus they need to be re-attached.
    for (MockComponent child : children) {
      ((MockChartClient) child).addToChart(this);
    }
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.CHART_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.CHART_PREFERRED_HEIGHT;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_TYPE)) {
      setTypeProperty(newValue);
      changeChartPropertyVisibilities();
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      chartView.setBackgroundColor(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_DESCRIPTION)) {
      chartView.setTitle(newValue);
      chartView.getChartWidget().draw(); // Title changing requires re-drawing the Chart
    } else if (propertyName.equals(PROPERTY_NAME_LEGEND_ENABLED)) {
      setLegendEnabledProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_GRID_ENABLED)) {
      setGridEnabledProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_PIE_RADIUS)) {
      setPieRadiusProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_LABELS_FROM_STRING)) {
      setLabelsFromStringProperty(newValue);
    }
  }

  /**
   * Creates Data components from the contents of the specified MockDataFile component.
   * The Data components are then attached as children to the Chart and the Source property
   * of each individual Data component is set accordingly.
   *
   * @param dataFileSource MockDataFile component to instantiate components from
   */
  public void addDataFile(MockDataFile dataFileSource) {
    List<String> columnNames = dataFileSource.getColumnNames();

    for (String column : columnNames) {
      // Create a new MockChartData2D component and attach it to the Chart
      // TODO: More data component support
      MockChartData2D data = new MockChartData2D(editor);
      addComponent(data);
      data.addToChart(this);

      // Change the properties of the instantiated data component
      data.changeProperty("DataFileYColumn", column);
      data.changeProperty("Label", column);
      data.changeProperty("Source", dataFileSource.getName());
    }
  }

  /**
   * Creates a corresponding MockChartDataModel that
   * represents the current Chart type.
   *
   * @return new MockChartDataModel instance
   */
  public MockChartDataModel<?, ?> createDataModel() {
    return chartView.createDataModel();
  }

  /**
   * Refreshes the Chart view.
   */
  public void refreshChart() {
    chartView.getChartWidget().update();
  }

  /**
   * Returns the Mock Component of the Drag Source.
   *
   * @param source DragSource instance
   * @return MockComponent instance
   */
  private MockComponent getComponentFromDragSource(DragSource source) {
    MockComponent component = null;
    if (source instanceof MockComponent) {
      component = (MockComponent) source;
    } else if (source instanceof SimplePaletteItem) {
      component = (MockComponent) source.getDragWidget();
    }

    return component;
  }

  @Override
  protected boolean acceptableSource(DragSource source) {
    MockComponent component = getComponentFromDragSource(source);

    return (component instanceof MockChartData2D)
        || (component instanceof MockTrendline)
        || (isComponentAcceptableDataFileSource(component));
  }

  /**
   * Checks whether the component is an acceptable DataFile drag source for the Chart.
   * The criterion is that the Component must be of type DataFile and is
   * already instantiated in a container.
   *
   * @param component Component to check
   * @return true if the component is a DataFile that is an acceptable source
   */
  private boolean isComponentAcceptableDataFileSource(MockComponent component) {
    return component instanceof MockDataFile
        && component.getContainer() != null; // DataFile must already be in it's own container
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    // Pie Radius property should be invisible by default, since
    // the default Chart Type is a Line Chart
    if (propertyName.equals(PROPERTY_NAME_PIE_RADIUS)) {
      return type == ChartType.Pie;
    }

    return super.isPropertyVisible(propertyName);
  }
}
