package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.runtime.util.ChartModelBase;

@SimpleObject
public abstract class ChartDataBase implements Component {
    protected ChartModelBase chartModel = null;

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return null;
    }
}
