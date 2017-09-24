// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.YaVersion;

import android.view.View;
import android.widget.HorizontalScrollView;
import android.view.ViewTreeObserver.*;

/**
 * A horizontal arrangement of components
 * @author sharon@google.com (Sharon Perl)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 * events and methods about scrolling
 * @author 502470184@qq.com (ColinTree, YANG)
 */
@DesignerComponent(version = YaVersion.HORIZONTALSCROLLARRANGEMENT_COMPONENT_VERSION, 
        description = "<p>A formatting element in which to place components " +
        "that should be displayed from left to right.  If you wish to have " +
        "components displayed one over another, use " +
        "<code>VerticalArrangement</code> instead.</p><p>This version is " +
        "scrollable.",
        category = ComponentCategory.LAYOUT)
@SimpleObject
public class HorizontalScrollArrangement extends HVArrangement implements OnScrollChangedListener {
    public HorizontalScrollArrangement(ComponentContainer container) {
        super(container, ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL,
            ComponentConstants.SCROLLABLE_ARRANGEMENT);
        getView().getViewTreeObserver().addOnScrollChangedListener(this);
    }

    private float deviceDensity() {
        return container.$form().deviceDensity();
    }

    private int px2dx(int px) {
        return Math.round(px * deviceDensity());
    }
    
    private int dx2px(int dx) {
        return Math.round(dx / deviceDensity());
    }

    private HorizontalScrollView getScrollView() {
        return (HorizontalScrollView) getView();
    }

    private int oldScrollX = 0;

    @Override
    public void onScrollChanged() {
        int scrollX = dx2px(getView().getScrollX());
        if (scrollX < 0) {
            scrollX = 0;
        }
        if (oldScrollX != scrollX) {
            ScrollChanged(scrollX);
            oldScrollX = scrollX;
        }
    }

    @SimpleEvent
    public void ScrollChanged(int scrollPosition) {
        EventDispatcher.dispatchEvent(this, "ScrollChanged", scrollPosition);
        if (scrollPosition == 0) {
            ReachLeftEnd();
        } else {
            if (MaxScrollPosition() - ScrollPosition() <= 0) {
                ReachRightEnd();
            }
        }
    }

    @SimpleEvent
    public void ReachLeftEnd() {
        EventDispatcher.dispatchEvent(this, "ReachLeftEnd");
    }

    @SimpleEvent
    public void ReachRightEnd() {
        EventDispatcher.dispatchEvent(this, "ReachRightEnd");
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "The scroll position is the same as the number of pixels that "
            + "are hidden from view above the scrollable area. "
            + "If the scroll bar is at the very top, or if the element is not scrollable, this number will be 0.")
    public int ScrollPosition() {
        int dxPosition = getView().getScrollX();
        if (dxPosition < 0) {
            return 0;
        } else if (dxPosition > MaxScrollPosition()) {
            return MaxScrollPosition();
        }
        return dx2px(dxPosition);
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Return the ")
    public int MaxScrollPosition() {
        View view = (View) getScrollView().getChildAt(getScrollView().getChildCount() - 1);
        return dx2px(view.getRight() - getView().getWidth());
    }

    @SimpleFunction
    public void ScrollLeftEnd() {
        getScrollView().fullScroll(HorizontalScrollView.FOCUS_LEFT);
    }

    @SimpleFunction
    public void ScrollRightEnd() {
        getScrollView().fullScroll(HorizontalScrollView.FOCUS_RIGHT);
    }

    @SimpleFunction
    public void ArrowScrollLeftward() {
        getScrollView().arrowScroll(HorizontalScrollView.FOCUS_LEFT);
    }

    @SimpleFunction
    public void ArrowScrollRightward() {
        getScrollView().arrowScroll(HorizontalScrollView.FOCUS_RIGHT);
    }

    @SimpleFunction
    public void PageScrollLeftward() {
        getScrollView().pageScroll(HorizontalScrollView.FOCUS_LEFT);
    }

    @SimpleFunction
    public void PageScrollRightward() {
        getScrollView().pageScroll(HorizontalScrollView.FOCUS_RIGHT);
    }

    @SimpleFunction
    public void ScrollTo(int position) {
        getScrollView().scrollTo(px2dx(position), 0);
    }

    @SimpleFunction
    public void SmoothScrollTo(int position) {
        getScrollView().smoothScrollTo(px2dx(position), 0);
    }

    @SimpleFunction
    public void ScrollBy(int displacement) {
        getScrollView().scrollBy(px2dx(displacement), 0);
    }

    @SimpleFunction
    public void SmoothScrollBy(int displacement) {
        getScrollView().smoothScrollBy(px2dx(displacement), 0);
    }
}