package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;

public abstract class ChartDataBase<T extends ChartData> extends AndroidNonvisibleComponent {
    /**
     * Creates a new AndroidNonvisibleComponent.
     *
     * @param form the container that this component will be placed in
     */
    protected ChartDataBase(Form form) {
        super(form);
    }
}
