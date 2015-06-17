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

    //  Helper used to accurately determine the absolute left and absolute top
    //  of the Element for use in positioning. Pinch Zooming breaks GWT's built-in
    //  equivalents, DOMImpl#getSubPixelAbsoluteLeft(Element) and
    //  DOMImpl#getSubPixelAbsoluteTop(Element).
//    private native JsArrayInteger getAbsolutePosition(Element elem) /*-{
//        var box = elem.getBoundingClientRect();
//
//        var body = $doc.body;
//
//        var docElem = $doc.documentElement;
//
//        //var scrollTop = $wnd.pageYOffset || docElem.scrollTop || body.scrollTop;
//
//        //var scrollLeft = $wnd.pageXOffset || docElem.scrollLeft || body.scrollLeft;
//
//        var clientTop = docElem.clientTop || body.clientTop || 0;
//
//        var clientLeft = docElem.clientLeft || body.clientLeft || 0;
//
//        var top  = box.top; //- clientTop + $wnd.scrollY;
//
//        var left = box.left; //+ scrollLeft - clientLeft;
//
//        //Position parameters used for drawing the rectangle
//        var width = top-box.bottom;
//        var height = left-box.right;
//
//        var rect = box;
//        var tableRectDiv = document.createElement('div');
//        tableRectDiv.style.position = 'absolute';
//        tableRectDiv.style.border = '1px solid red';
//        var scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
//        var scrollLeft = document.documentElement.scrollLeft || document.body.scrollLeft;
//        tableRectDiv.style.margin = tableRectDiv.style.padding = '0';
//        tableRectDiv.style.top = (rect.top + scrollTop) + 'px';
//        tableRectDiv.style.left = (rect.left + scrollLeft) + 'px';
//        // we want rect.width to be the border width, so content width is 2px less.
//        tableRectDiv.style.width = (rect.width - 2) + 'px';
//        tableRectDiv.style.height = (rect.height - 2) + 'px';
//        document.body.appendChild(tableRectDiv);
//
//        return [Math.round(left), Math.round(top)];
//    }-*/;
}
