// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

// import com.google.gwt.event.dom.client.LoadEvent;
// import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.graphics.client.Color;

/**
 * Helper methods for working with mock components.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockComponentsUtil {

  private MockComponentsUtil() {
  }

  /**
   * Sets the background color for the given widget.
   *
   * @param widget  widget to change background color for
   * @param color  new color (RGB value)
   */
  static void setWidgetBackgroundColor(Widget widget, String color) {
    if (isNoneColor(color)) {
      DOM.setStyleAttribute(widget.getElement(), "backgroundColor", "transparent");
    } else {
      DOM.setStyleAttribute(widget.getElement(), "backgroundColor", "#" + getHexString(color, 6));
    }
  }

  /**
   * Sets the background image for the given widget.
   *
   * @param widget  widget to change background image for
   * @param image  URL
   */
  static void setWidgetBackgroundImage(Widget widget, String image) {
    DOM.setStyleAttribute(widget.getElement(), "backgroundImage", "url(" + image + ')');
    DOM.setStyleAttribute(widget.getElement(), "backgroundRepeat", "no-repeat");
    DOM.setStyleAttribute(widget.getElement(), "backgroundPosition", "center");
    DOM.setStyleAttribute(widget.getElement(), "backgroundSize", "100% 100%");
  }

  /**
   * Sets the background image for the given widget.
   *
   * @param container the MockContainer to refresh when image is loaded
   * @param widget  widget to change background image for
   * @param image  URL
   */
  static void setWidgetBackgroundImage(final MockContainer container, Widget widget, String image) {
    // Problem: When we change the background image via a Style referencing a "url"
    // the browser doesn't initially know the size of the image. We need to know it
    // when the container layout height and/or width is "Automatic." If we query right
    // away, we will be told the image is 0 x 0 because it isn't loaded yet.
    // I have not been able to figure out how to get the browser to give us a onLoad (or
    // similar event) when the image is loaded. If we could get such an event, we can
    // call refreshForm in the container and win.
    //
    // The code below fudges this by setting up a time to fire after 1 second with the
    // hope that the image will have been loaded by then and its dimensions known.
    // The code commented out immediately below this code is what I would like to use,
    // but it doesn't seem to work!   -JIS
    Timer t = new Timer() {
        @Override
        public void run() {
          container.refreshForm();
        }
      };
//    widget.addHandler(new LoadHandler() {
//        @Override
//        public void onLoad(LoadEvent event) {
//          container.refreshForm();
//        }
//      }, new Type<LoadHandler>());
    setWidgetBackgroundImage(widget, image);
    t.schedule(1000);           // Fire in one second
  }

  /**
   * Sets the font weight for the given widget (bold or normal).
   *
   * @param widget  widget to change font weight for
   * @param value  {@code true} for bold font and {@code false} for normal font
   */
  static void setWidgetFontBold(Widget widget, String value) {
    DOM.setStyleAttribute(widget.getElement(), "fontWeight",
        Boolean.parseBoolean(value) ? "bold" : "normal");
  }

  /**
   * Sets the text color for the given widget.
   *
   * @param widget  widget to change text color for
   * @param color  new color (RGB value)
   */
  static void setWidgetTextColor(Widget widget, String color) {
    if (isNoneColor(color)) {
      DOM.setStyleAttribute(widget.getElement(), "color", "transparent");
    } else {
      DOM.setStyleAttribute(widget.getElement(), "color", "#" + getHexString(color, 6));
    }
  }

  /**
   * Sets the font style for the given widget (italic or normal).
   *
   * @param widget  widget to change font style for
   * @param value  {@code true} for italic font and {@code false} for normal font
   */
  static void setWidgetFontItalic(Widget widget, String value) {
    DOM.setStyleAttribute(widget.getElement(), "fontStyle",
        Boolean.parseBoolean(value) ? "italic" : "normal");
  }

  /**
   * Sets the font size for the given widget.
   *
   * @param widget  widget to change font size for
   * @param size  new font size (in scaled px)
   */
  static void setWidgetFontSize(Widget widget, String size) {
    // Fonts on Android are in scaled pixels...
    try {
      DOM.setStyleAttribute(widget.getElement(), "fontSize",
          (int)(Float.parseFloat(size) * 0.9) + "px");
    } catch (NumberFormatException e) {
      // Ignore this. If we throw an exception here, the project is unrecoverable.
    }
  }

  /**
   * Sets the font typeface for the given widget.
   *
   * @param widget  widget to change font typeface for
   * @param typeface  "0" for normal, "1" for sans serif, "2" for serif and
   *                  "3" for monospace
   */
  static void setWidgetFontTypeface(Widget widget, String typeface) {
    switch (Integer.parseInt(typeface)) {
      default:
        // This should never happen
        throw new IllegalArgumentException("Typeface:" + typeface);

      case 0:
      case 1:
        typeface = "sans-serif";
        break;

      case 2:
        typeface = "serif";
        break;

      case 3:
        typeface = "monospace";
        break;
    }
    DOM.setStyleAttribute(widget.getElement(), "fontFamily", typeface);
  }

  /**
   * Sets the text alignment for the given widget.
   *
   * @param widget  widget to change text alignment for
   * @param align  one of "0" for left, "1" for center or "2" for right
   */
  static void setWidgetTextAlign(Widget widget, String align) {
    switch (Integer.parseInt(align)) {
      default:
        // This should never happen
        throw new IllegalArgumentException("align:" + align);

      case 0:
        align = "left";
        break;

      case 1:
        align = "center";
        break;

      case 2:
        align = "right";
        break;
    }
    DOM.setStyleAttribute(widget.getElement(), "textAlign", align);
  }

  /**
   * Indicates whether the given color value describes the "None" color.
   */
  static boolean isNoneColor(String color) {
    return getHexString(color, 8).equals(MockVisibleComponent.COLOR_NONE);
  }

  /**
   * Indicates whether the given color value describes the "Default" color.
   */
  static boolean isDefaultColor(String color) {
    return getHexString(color, 8).equals(MockVisibleComponent.COLOR_DEFAULT);
  }

  /**
   * Returns a new Color object for the given color value.
   */
  static Color getColor(String color) {
    String hex = getHexString(color, 8);
    int alpha = Integer.parseInt(hex.substring(0, 2), 16);
    int red = Integer.parseInt(hex.substring(2, 4), 16);
    int green = Integer.parseInt(hex.substring(4, 6), 16);
    int blue = Integer.parseInt(hex.substring(6, 8), 16);
    return new Color(red, green, blue, alpha / 255.0F);
  }

  /*
   * Converts a string containing a number to a string containing the requested amount of least\
   * significant digits of the equivalent hex number.
   */
  private static String getHexString(String color, int digits) {
    // When receiving the property values from the server hex numbers were converted to decimal
    // numbers
    color = color.startsWith("&H") ? color.substring(2) : Long.toHexString(Long.parseLong(color));
    int len = color.length();
    if (len < digits) {
      do {
        color = '0' + color;
      } while (++len < digits);

      return color;
    }

    return color.substring(len - digits);
  }

  /*
   * Retrieves the size style attributes of the given widgets and then clears
   * them.
   *
   * @param w the widget
   * @return the previous size style attributes as an array with width at index
   *         0 and height at index 1.
   */
  static String[] clearSizeStyle(Widget w) {
    Element element = w.getElement();
    String widthStyle = DOM.getStyleAttribute(element, "width");
    String heightStyle = DOM.getStyleAttribute(element, "height");
    if (widthStyle != null) {
      DOM.setStyleAttribute(element, "width", null);
    }
    if (heightStyle != null) {
      DOM.setStyleAttribute(element, "height", null);
    }
    return new String[] { widthStyle, heightStyle };
  }

  /*
   * Restores the given size style attributes for the widget.
   *
   * @param w the widget
   * @param style the size style attributes as an array with width at index 0
   *        and height at index 1.
   */
  static void restoreSizeStyle(Widget w, String[] style) {
    Element element = w.getElement();
    if (style[0] != null) {
      DOM.setStyleAttribute(element, "width", style[0]);
    }
    if (style[1] != null) {
      DOM.setStyleAttribute(element, "height", style[1]);
    }
  }

  /**
   * Returns the width of the given MockComponent after temporarily setting its
   * width and height styles to null.
   */
  static int getPreferredWidth(MockComponent component) {
    String[] style = clearSizeStyle(component);
    int width = component.getOffsetWidth() + 4;
    restoreSizeStyle(component, style);
    // We want the size without the MockComponent's CSS border.
    return width - MockComponent.BORDER_SIZE;
  }

  /**
   * Returns the height of the given MockComponent after temporarily setting its
   * width and height styles to null.
   */
  static int getPreferredHeight(MockComponent component) {
    String[] style = clearSizeStyle(component);
    int height = component.getOffsetHeight();
    restoreSizeStyle(component, style);
    // We want the size without the MockComponent's CSS border.
    return height - MockComponent.BORDER_SIZE;
  }

  /**
   * Returns the preferred size of the specified widget,
   * in an array of the form {@code [width, height]}.
   * <p>
   * It is assumed that:
   * <ul>
   *   <li>{@code w} has no parent</li>
   *   <li>{@code w} has not been configured to be invisible</li>
   * </ul>
   */
  public static int[] getPreferredSizeOfDetachedWidget(Widget w) {
    // Attach the widget to the DOM, so that its preferred size is calculated correctly
    RootPanel.get().add(w);

    String[] style = clearSizeStyle(w);
    int width = w.getOffsetWidth() + 4;
    int height = w.getOffsetHeight();
    restoreSizeStyle(w, style);

    // Detach the widget from the DOM before returning
    RootPanel.get().remove(w);

    return new int[] { width, height };
  }

  static void setEnabled(MockComponent component, String value) {
    // Can't disable GWT control because then you wouldn't be able to select it anymore because it
    // would not receive any browser events.
    if (Boolean.parseBoolean(value)) {
      component.removeStyleDependentName("disabled");
    } else {
      component.addStyleDependentName("disabled");
    }
  }
}
