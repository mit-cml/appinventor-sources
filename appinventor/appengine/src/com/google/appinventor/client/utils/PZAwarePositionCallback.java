package com.google.appinventor.client.utils;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Implementation of PopupPanel.PositionCallback that can be alerted
 * when the browser is Pinch Zoomed in Google Chrome. Pinch Zooming in Chrome
 * causes both UIObject#getAbsoluteLeft() and UIObject#getAbsoluteTop() to return
 * inaccurate values. This class contains patched implementations for each.
 *
 * @author William Byrne
 */
public abstract class PZAwarePositionCallback implements PopupPanel.PositionCallback {
    // Pinch Zoom flag which is tripped by a handler in Ode.java
    private static boolean pinchZoomed = false;

    // Element of the enclosing instance
    private final Element elem;

    public PZAwarePositionCallback(Element elem) {
        this.elem = elem;
    }

    public static boolean isPinchZoomed() { return pinchZoomed; }

    public static void setPinchZoomed(boolean pinchZoomed) { PZAwarePositionCallback.pinchZoomed = pinchZoomed; }

    public int getTrueAbsoluteLeft() { return getAbsolutePosition(elem).get(0); }

    public int getTrueAbsoluteTop() { return getAbsolutePosition(elem).get(1); }

    //  Helper used to accurately determine the absolute left and absolute top
    //  of the Element for use in positioning. Pinch Zooming breaks GWT's built-in
    //  equivalents, DOMImpl#getSubPixelAbsoluteLeft(Element) and
    //  DOMImpl#getSubPixelAbsoluteTop(Element).
    private native JsArrayInteger getAbsolutePosition(Element elem) /*-{
        var curr = elem;
        var left = 0;
        var top = 0;

        if ($doc.getElementById) {
            do  {
                left += elem.offsetLeft - elem.scrollLeft;
                top += elem.offsetTop - elem.scrollTop;

                elem = elem.offsetParent;
                curr = curr.parentNode;
                while (curr != elem) {
                    left -= curr.scrollLeft;
                    top -= curr.scrollTop;

                    curr = curr.parentNode;
                }
            } while (elem.offsetParent);
        }

        return [left, top];
    }-*/;
}
