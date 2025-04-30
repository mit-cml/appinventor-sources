// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.project.HasAssetsFolder;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.dom.client.StyleElement;
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
      widget.getElement().getStyle().setBackgroundColor("transparent");
    } else {
      widget.getElement().getStyle().setBackgroundColor("#" + getAlphaHexString(color));
    }
  }

  /**
   * Clears the background color of a widget to its default by CSS rules.
   *
   * @param widget  widget to remove the background color for
   */
  static void resetWidgetBackgroundColor(Widget widget) {
    Element el = widget.getElement();
    if (el != null) {
      el.getStyle().clearBackgroundColor();
    }
  }

  /**
   * Sets the background image for the given widget.
   *
   * @param widget  widget to change background image for
   * @param image  URL
   */
  static void setWidgetBackgroundImage(Widget widget, String image) {
    if (image.isEmpty()) {
      widget.getElement().getStyle().setBackgroundImage("none");
    } else {
      widget.getElement().getStyle().setBackgroundImage("url(" + image + ')');
    }
    widget.getElement().getStyle().setProperty("backgroundRepeat", "no-repeat");
    widget.getElement().getStyle().setProperty("backgroundPosition", "center");
    widget.getElement().getStyle().setProperty("backgroundSize", "100% 100%");
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
    widget
        .getElement()
        .getStyle()
        .setFontWeight(Boolean.parseBoolean(value) ? FontWeight.BOLD : FontWeight.NORMAL);
  }

  /**
   * Sets the text color for the given widget.
   *
   * @param widget  widget to change text color for
   * @param color  new color (RGB value)
   */
  static void setWidgetTextColor(Widget widget, String color) {
    if (isNoneColor(color)) {
      widget.getElement().getStyle().setColor("transparent");
    } else {
      widget.getElement().getStyle().setColor("#" + getAlphaHexString(color));
    }
  }

  /**
   * Clears the text color of a widget to its default by CSS rules
   *
   * @param widget  widget to remove the text color for
   */
  static void resetWidgetTextColor(Widget widget) {
    Element el = widget.getElement();
    if (el != null) {
      el.getStyle().clearColor();
    }
  }

  /**
   * Sets the font style for the given widget (italic or normal).
   *
   * @param widget  widget to change font style for
   * @param value  {@code true} for italic font and {@code false} for normal font
   */
  static void setWidgetFontItalic(Widget widget, String value) {
    widget
        .getElement()
        .getStyle()
        .setFontStyle(Boolean.parseBoolean(value) ? FontStyle.ITALIC : FontStyle.NORMAL);
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
      widget.getElement().getStyle().setFontSize((int) (Float.parseFloat(size) * 0.9), Unit.PX);
    } catch (NumberFormatException e) {
      // Ignore this. If we throw an exception here, the project is unrecoverable.
    }
  }

  /**
   * Returns the asset node of given name.
   *
   * @param editor current project editor
   * @param name   the name of the asset
   * @return the asset node
   */
  static ProjectNode getAssetNode(SimpleEditor editor, String name) {
    Project project = Ode.getInstance().getProjectManager().getProject(editor.getProjectId());
    if (project != null) {
      HasAssetsFolder<YoungAndroidAssetsFolder> hasAssetsFolder =
          (YoungAndroidProjectNode) project.getRootNode();
      for (ProjectNode asset : hasAssetsFolder.getAssetsFolder().getChildren()) {
        if (asset.getName().equals(name)) {
          return asset;
        }
      }
    }
    return null;
  }
  
  /**
   * Converts the given font typeface property value to font resource URL
   *
   * @param editor the editor
   * @param text   asset name
   * @return the string value of font resource URL
   */
  static String convertFontPropertyValueToUrl(SimpleEditor editor, String text) {
    if (text.length() > 0) {
      ProjectNode asset = getAssetNode(editor, text);
      if (asset != null) {
        return StorageUtil.getFileUrl(asset.getProjectId(), asset.getFileId());
      }
    }
    return null;
  }
  
  /**
   * Create font resource into DOM's Head element.
   *
   * @param fontFamily      font family name
   * @param fontResourceURL font resource url
   * @param resourceId      uniq ID of font resource.
   */
  static void createFontResource(String fontFamily, String fontResourceURL, String resourceId) {
    StyleElement resourceElement = Document.get().createStyleElement();
    resourceElement.setId(resourceId);
    String resource = "@font-face {";
    resource += "font-family: ";
    resource += fontFamily + ";";
    resource += "src: url(\"" + fontResourceURL + "\");";
    resource += "}";
    resourceElement.setInnerText(resource);
    Document.get().getHead().appendChild(resourceElement);
  }
  
  /**
   * Sets the font typeface for the given widget.
   *
   * @param editor  current project editor
   * @param widget  widget to change font typeface for
   * @param typeface  default, sans serif, serif, monospace or font file in case of custom
   *                  font typeface
   */
  static void setWidgetFontTypeface(SimpleEditor editor, Widget widget, String typeface) {
    String fontFamily = "";
    if (typeface.equals("0") || typeface.equals("1")) {
      fontFamily = "sans-serif";
    } else if (typeface.equals("2")) {
      fontFamily = "serif";
    } else if (typeface.equals("3")) {
      fontFamily = "monospace";
    } else {
      fontFamily = typeface.substring(0, typeface.lastIndexOf("."));
      String resourceID = typeface.toLowerCase().substring(0, typeface.lastIndexOf("."));
      String resourceURL = convertFontPropertyValueToUrl(editor, typeface);
      if (Document.get().getElementById(resourceID) == null) {
        createFontResource(fontFamily, resourceURL, resourceID);
      }
    }
    widget.getElement().getStyle().setProperty("fontFamily", fontFamily);
  }

  /**
   * Update widget's text content appearances according to width property value.
   *
   * @param widget widget to update text appearances for
   * @param width  widget's width property value -1 for Automatic
   */
  static void updateTextAppearances(Widget widget, String width) {
    if (width.equals("-1")) {
      // for width = Automatic
      widget.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
    } else {
      // for width = Fill Parent, Pixels or Percentage
      widget.getElement().getStyle().setWhiteSpace(WhiteSpace.NORMAL);
    }
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
    widget.getElement().getStyle().setProperty("textAlign", align);
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
   * Converts the hex string representing the color &HAARRGGBB to a hex color in the format RRGGBBAA
   */
  static String getAlphaHexString(String color) {
    color = color.startsWith("&H") ? color.substring(2) : Long.toHexString(Long.parseLong(color));
    int len = color.length();
    if (len < 8) {
      do {
        color = 'F' + color;
      } while (++len < 8);
    }
    return color.substring(2) + color.substring(0, 2);
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
    return clearSizeStyle(w.getElement());
  }

  static String[] clearSizeStyle(Element element) {
    String widthStyle = element.getStyle().getWidth();
    String heightStyle = element.getStyle().getHeight();
    String lineHeightStyle = element.getStyle().getLineHeight();
    if (widthStyle != null) {
      element.getStyle().clearWidth();
    }
    if (heightStyle != null) {
      element.getStyle().clearHeight();
    }
    if (lineHeightStyle != null) {
      element.getStyle().setProperty("lineHeight", "initial");
    }
    return new String[] {widthStyle, heightStyle, lineHeightStyle};
  }

  /*
   * Restores the given size style attributes for the widget.
   *
   * @param w the widget
   * @param style the size style attributes as an array with width at index 0
   *        and height at index 1.
   */
  static void restoreSizeStyle(Widget w, String[] style) {
    restoreSizeStyle(w.getElement(), style);
  }

  static void restoreSizeStyle(Element element, String[] style) {
    if (style[0] != null) {
      element.getStyle().setProperty("width", style[0]);
    }
    if (style[1] != null) {
      element.getStyle().setProperty("width", style[1]);
    }
    if (style[2] != null) {
      element.getStyle().setProperty("width", style[2]);
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
    int height = w.getOffsetHeight() + 6;
    if (height < 26) {          // Do not make the button smaller
      height = 26;              // then 26, as this mimicks what happens
    }                           // on the real device
    restoreSizeStyle(w, style);

    // Detach the widget from the DOM before returning
    RootPanel.get().remove(w);

    return new int[] { width, height };
  }

  /**
   * Returns the preferred size of the specified DOM element in an array of the
   * form {@code [width, height]}.
   *
   * @see #getPreferredSizeOfDetachedWidget(Widget)
   * @param element the DOM element to compute the size for
   * @return the natural width and height of the element
   */
  public static int[] getPreferredSizeOfElement(Element element) {
    Element root = RootPanel.get().getElement();
    root.appendChild(element);

    String[] style = clearSizeStyle(element);
    int width = element.getOffsetWidth() + 4;
    int height = element.getOffsetHeight() + 6;
    if (height < 26) {
      height = 26;
    }
    restoreSizeStyle(element, style);

    root.removeChild(element);
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
