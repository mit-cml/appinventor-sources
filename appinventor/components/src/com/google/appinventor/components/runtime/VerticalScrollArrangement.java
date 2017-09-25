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
import android.widget.ScrollView;
import android.view.ViewTreeObserver.*;

/**
 * A vertical arrangement of components
 * @author sharon@google.com (Sharon Perl)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */

@DesignerComponent(version = YaVersion.VERTICALSCROLLARRANGEMENT_COMPONENT_VERSION,
        description = "<p>A formatting element in which to place components " +
        "that should be displayed one below another.  (The first child component " +
        "is stored on top, the second beneath it, etc.)  If you wish to have " +
        "components displayed next to one another, use " +
        "<code>HorizontalArrangement</code> instead.</p><p> " +
        "This version is scrollable",
        category = ComponentCategory.LAYOUT)
@SimpleObject
public class VerticalScrollArrangement extends HVArrangement implements OnScrollChangedListener {

    public VerticalScrollArrangement(ComponentContainer container) {
        super(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
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

    private ScrollView getScrollView() {
        return (ScrollView) getView();
    }

    private int oldScrollY = 0;

    @Override
    public void onScrollChanged() {
        int scrollY = dx2px(getView().getScrollY());
        if (scrollY < 0) {
            scrollY = 0;
        }
        if (oldScrollY != scrollY) {
            ScrollChanged(scrollY);
            oldScrollY = scrollY;
        }
    }

    @SimpleEvent(description = "Notice that if blocks like \"ScrollBy\" is excuted, this event would be called")
    public void ScrollChanged(int scrollPosition) {
        EventDispatcher.dispatchEvent(this, "ScrollChanged", scrollPosition);
        if (scrollPosition == 0) {
            ReachTop();
        } else {
            if (MaxScrollPosition() - ScrollPosition() <= 0) {
                ReachBottom();
            }
        }
    }

    @SimpleEvent(description = "Notice that if blocks like \"ScrollTop\" is excuted, this event would be called")
    public void ReachTop() {
        EventDispatcher.dispatchEvent(this, "ReachTop");
    }

    @SimpleEvent(description = "Notice that if blocks like \"ScrollBottom\" is excuted, this event would be called")
    public void ReachBottom() {
        EventDispatcher.dispatchEvent(this, "ReachBottom");
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, 
        description = "The scroll position is the same as the number of pixels that "
            + "are hidden from view above the scrollable area. "
            + "If the scroll bar is at the very top, or if the element is not scrollable, this number will be 0.")
    public int ScrollPosition() {
        int dxPosition = getView().getScrollY();
        if (dxPosition < 0) {
            return 0;
        } else if (dxPosition > MaxScrollPosition()) {
            return MaxScrollPosition();
        }
        return dx2px(dxPosition);
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, 
        description = "Return the maximum position that the ScrollArrangement can reach")
    public int MaxScrollPosition() {
        View view = (View) getScrollView().getChildAt(getScrollView().getChildCount() - 1);
        return dx2px(view.getBottom() - getView().getHeight());
    }

    @SimpleFunction(description = "Scroll to the top of the scroll arrangement (defaut with animation)")
    public void ScrollTop() {
        getScrollView().fullScroll(ScrollView.FOCUS_UP);
    }

    @SimpleFunction(description = "Scroll to the bottom of the scroll arrangement (defaut with animation)")
    public void ScrollBottom() {
        getScrollView().fullScroll(ScrollView.FOCUS_DOWN);
    }

    @SimpleFunction(description = "Scroll upward for half a page (defaut with animation)")
    public void ArrowScrollUpward() {
        getScrollView().arrowScroll(ScrollView.FOCUS_UP);
    }

    @SimpleFunction(description = "Scroll downward for half a page (defaut with animation)")
    public void ArrowScrollDownward() {
        getScrollView().arrowScroll(ScrollView.FOCUS_DOWN);
    }

    @SimpleFunction(description = "Scroll upward for a full page (defaut with animation)")
    public void PageScrollUpward() {
        getScrollView().pageScroll(ScrollView.FOCUS_UP);
    }

    @SimpleFunction(description = "Scroll downward for a full page (defaut with animation)")
    public void PageScrollDownward() {
        getScrollView().pageScroll(ScrollView.FOCUS_DOWN);
    }

    @SimpleFunction(description = "Scroll to a specific position (without animation)")
    public void ScrollTo(int position) {
        getScrollView().scrollTo(0, px2dx(position));
    }

    @SimpleFunction(description = "Scroll to a specific position (with animation)")
    public void SmoothScrollTo(int position) {
        getScrollView().smoothScrollTo(0, px2dx(position));
    }

    @SimpleFunction(description = "Scroll downward so for a specific displacement, scroll upward if diaplacement is negative (without animation)")
    public void ScrollBy(int displacement) {
        getScrollView().scrollBy(0, px2dx(displacement));
    }

    @SimpleFunction(description = "Scroll downward so for a specific displacement, scroll upward if diaplacement is negative (with animation)")
    public void SmoothScrollBy(int displacement) {
        getScrollView().smoothScrollBy(0, px2dx(displacement));
    }

}