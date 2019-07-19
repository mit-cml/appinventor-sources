package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

import java.util.List;

public final class MockChart extends MockContainer {
    public static final String TYPE = "Chart";

    private static final String PROPERTY_NAME_TYPE = "Type";
    private static final String PROPERTY_NAME_DESCRIPTION = "Description";

    static {
        ResourcesType.setClientBundle(EmbeddedResources.INSTANCE);
    }

    protected MockChartView chartView;

    // Legal values for type are defined in
    // com.google.appinventor.components.runtime.Component.java.
    private int type;

    /**
     * Creates a new instance of a visible component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockChart(SimpleEditor editor) {
        super(editor, TYPE, images.image(), new MockChartLayout());

        // Since the Mcok Chart component is not a container in a normal
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

        // Re-attach all children MockChartData components
        rootPanel.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent arg0) {
                if (arg0.isAttached()) {
                    for (MockComponent child : children) {
                        if (child instanceof MockChartData) {
                            ((MockChartData) child).addToChart(MockChart.this);
                        }
                    }
                }
            }
        });
    }

    /**
     * Sets the type of the Chart to the newly specified value.
     * @param value  new Chart type
     */
    private void setTypeProperty(String value) {
        // Update type
        type = Integer.parseInt(value);

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
     * Creates and returns a new MockChartView object based on the type
     * (integer) provided
     * @param type  Chart type (integer representation)
     * @return new MockChartView object instance
     */
    private MockChartView createMockChartViewFromType(int type) {
        switch(type) {
            case 0:
                // Line Chart
                return new MockLineChartView();
            case 1:
                // Scatter Chart
                return new MockLineChartView();
            case 2:
                // Area Chart
                return new MockAreaChartView();
            case 3:
                // Bar Chart
                return new MockLineChartView();
            case 4:
                // Pie Chart
                return new MockLineChartView();
            default:
                // Invalid argument
                throw new IllegalArgumentException("type:" + type);
        }
    }

    /**
     * Reinitializes the Chart view by reattaching all the Data
     * components and setting back all the properties.
     */
    private void reinitializeChart() {
        // Chart type changing requires setting back Chart-related properties
        chartView.setBackgroundColor(getPropertyValue(PROPERTY_NAME_BACKGROUNDCOLOR));
        chartView.setTitle(getPropertyValue(PROPERTY_NAME_DESCRIPTION));
        chartView.getChartWidget().draw();

        // Re-attach all children MockChartData components.
        // This is needed since the properties of the MockChart
        // are set after the Data components are attached to
        // the Chart, and thus they need to be re-attached.
        for (MockComponent child : children) {
            ((MockChartData) child).addToChart(MockChart.this);
        }
    }

    @Override
    public int getPreferredWidth() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
    }

    @Override
    public int getPreferredHeight() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        if (propertyName.equals(PROPERTY_NAME_TYPE)) {
            setTypeProperty(newValue);
        } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
            chartView.setBackgroundColor(newValue);
        } else if (propertyName.equals(PROPERTY_NAME_DESCRIPTION)) {
            chartView.setTitle(newValue);
            chartView.getChartWidget().draw(); // Title changing requires re-drawing the Chart
        }
    }

    /**
     * Creates Data components from the specified comma separated values,
     * attaches them as children to the Chart and sets the Source property
     * of each instantiated data component to the specified.
     *
     * @param source  Data source name
     * @param csv  CSV values to parse
     */
    /**
     * Adds a new CSVFile component to the Chart, which parses
     * the contents and creates Data components accordingly to the
     * columns of the CSV file.
     *
     * For each column, a Data component is created.
     *
     * TODO: Multi-dimensional Chart data support
     *
     * @param csvSource MockNonVisible component of type CSVFile.
     *                  If the type differs, the method simply returns.
     */
    public void addCSVFile(MockCSVFile csvSource) {
        MockChart.this.onSelectedChange(true); // otherwise the last imported component

        List<String> columnNames = csvSource.getColumnNames();

        String XColumn = ""; // Use default option for the XColumn

        for (String column : columnNames) {
            // Create a new MockCoordinateData component and attach it to the Chart
            // TODO: More data component support
            MockCoordinateData data = new MockCoordinateData(editor);
            addComponent(data);
            data.addToChart(MockChart.this);

            // Change the properties of the instantiated data component
            data.changeProperty("CsvColumns", XColumn + "," + column);
            data.changeProperty("Label", column);
            data.changeProperty("Source", csvSource.getName());
        }
    }

    /**
     * Creates a corresponding MockChartDataModel that
     * represents the current Chart type.
     * @return  new MockChartDataModel instance
     */
    public MockChartDataModel createDataModel() {
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
     * @param source  DragSource instance
     * @return  MockComponent instance
     */
    protected MockComponent getComponentFromDragSource(DragSource source) {
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

        return (component instanceof MockCoordinateData)
                || (isComponentAcceptableCSVFile(component));
    }

    /**
     * Checks whether the component is an acceptable drag source for the Chart.
     * The criterion is that the Component must be of type CSVFile and is
     * already instantiated in a container.
     * @param component  Component to check
     * @return  true if the component is a CSVFile that is an acceptable source
     */
    private boolean isComponentAcceptableCSVFile(MockComponent component) {
        return component instanceof MockNonVisibleComponent // CSVFile component is of instance NonVisibleComponent
                && component.getType().equals("CSVFile") // Type must equal CSVFile
                && component.getContainer() != null; // Component must already be in it's own container
    }
}
