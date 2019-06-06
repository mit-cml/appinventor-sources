package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.DataSet;

public abstract class ChartDataBase<D extends DataSet> implements Component {
    protected D chartDataSet;

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return null;
    }
}
