package com.google.appinventor.components.runtime;

import android.view.View;

public abstract class AndroidViewComponent extends VisibleComponent {

    protected final ComponentContainer container;

    private int lastSetWidth = LENGTH_UNKNOWN;
    private int lastSetHeight = LENGTH_UNKNOWN;

    protected AndroidViewComponent(ComponentContainer container) {
        this.container = container;
    }

    public abstract View getView();

    public boolean Visible() {
        return getView().getVisibility() == View.VISIBLE;
    }

    public void Visible(boolean visibility) {
        getView().setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public int Width() {
        return getView().getWidth();
    }

    public void Width(int width) {
        container.setChildWidth(this, width);
    }

    public void setLastWidth(int width) {
        this.lastSetWidth = width;
    }

    public int Height() {
        return getView().getHeight();
    }

    public void Height(int height) {
        container.setChildHeight(this, height);
    }

    public void setLastHeight(int height) {
        this.lastSetHeight = height;
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return container.$form();
    }

    @Override
    public void WidthPercent(int percent) {
        // TODO(lroman10): Real implementation
    }

    @Override
    public void HeightPercent(int percent) {
        // TODO(lroman10): Real implementation
    }
}
