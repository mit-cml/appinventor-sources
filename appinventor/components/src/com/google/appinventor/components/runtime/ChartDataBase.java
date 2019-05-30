package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;

public abstract class ChartDataBase<T extends ChartData> extends AndroidNonvisibleComponent {

    protected T chartData;

    /**
     * Creates a new AndroidNonvisibleComponent.
     *
     * @param form the container that this component will be placed in
     */
    protected ChartDataBase(Form form) {
        super(form);
    }

    /**
     * Getter method for the Data Series of the ChartData component
     *
     * This should be accessed by Charts to know what data to import.
     *
     * @return - ChartData object
     */
    public T getChartData() {
        return chartData;
    }


}
