package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.github.mikephil.charting.charts.Chart;

@SimpleObject
@UsesLibraries(libraries = "mpandroidchart.jar")
public abstract class ChartBase<T extends Chart> extends AndroidViewComponent {

    protected T view;

    /**
     * Creates a new ChartBase component.
     *
     * @param container container, component will be placed in
     */
    protected ChartBase(ComponentContainer container) {
        super(container);
    }

    /**
     * Initializes the Base Chart object instance.
     */
    protected void initChart() {
        // Adds the view to the designated container
        container.$add(this);
    }
}
