package com.google.appinventor.components.runtime.client;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.gwt.user.client.ui.Widget;

public abstract class AndroidViewComponent implements Component {
    protected final ComponentContainer container;

    public AndroidViewComponent(ComponentContainer container) {
        this.container = container;
    }

    public abstract Widget getView();
}
