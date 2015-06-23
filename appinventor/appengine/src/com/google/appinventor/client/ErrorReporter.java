// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.WindowScrollListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * This class is used to display popup error messages along the center
 * top edge of the UI.  It can also display the messages in an
 * alternate CSS style, so they can serve as information messages, as
 * opposed to error messages.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
public final class ErrorReporter {
  /**
   * Popup that shows error or info messages
   */

  // Styles for error and info messages.  These are defined in Ya.css
  private static final String ERROR_SPAN_ENCLOSER_START = "<span class=\"ode-ErrorMessage\">";
  private static final String INFO_SPAN_ENCLOSER_START = "<span class=\"ode-InfoMessage\">";
  private static final String SPAN_ENCLOSER_END = "</span>";


  private static class ErrorPopup extends PopupPanel {

    // Label holding error message
    private final HTML messageLabel;

    /**
     * Initializes the ErrorPopup.
     */
    ErrorPopup() {
      super(true); //constructor for PopupPanel class. "True" initializes the "auto-hide" variable
      
      
      // I'm leaving this setSTyleName line here as a comment, to show the typical way to define
      // the style.
      // I would have preferred to use this method rather than constructing the HTML by hand,
      // but it seems that GWT doesn't let the StyleName to be switched dynamically.
      // setStyleName("ode-ErrorMessage");

      messageLabel = new HTML();
      setWidget(messageLabel);

      // Recenter the message when the window is resized.
      // TODO(halabelson): replace the deprecated methods
      Window.addWindowResizeListener(new WindowResizeListener() {
        @Override
        public void onWindowResized(int width, int height) {
          centerTopPopup();
        }
      });

      // Reposition the message to the top of the screen when the
      // window is scrolled
      // TODO(halabelson): get rid of the flashing on vertical scrolling
      Window.addWindowScrollListener(new WindowScrollListener() {
        @Override
        public void onWindowScrolled(int scrollLeft, int scrollTop) {
          centerTopPopup();
        }
      });
    }
    /**
     * Sets the message to be shown
     *
     * @param message message
     */
    void setMessageHTML(String message) {
      messageLabel.setHTML(message);
    }



    /*
     * Centers the popup along the top edge of the UI.
     */
    private void centerTopPopup() {
      setPopupPosition(
          // make sure the popup is on-screen
          // if the top of the window is scrolled off the screen
          (Window.getClientWidth() - getOffsetWidth()) / 2,
          Window.getScrollTop());
    }
  }

  // Singleton instance of the popup
  private static final ErrorPopup POPUP = new ErrorPopup();


  private static void reportMessage(String message) {
    if (!Ode.isWindowClosing()) {
      POPUP.setMessageHTML(message);

      // Position the popup before showing it to prevent flashing.
      POPUP.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
        @Override
        public void setPosition(int offsetWidth, int offsetHeight) {
          POPUP.centerTopPopup();
        }
      });
    }
  }

   /**
   * Reports an error along the top edge center of the UI.
   *
   * @param message  error message
   */
  public static void reportError(String message) {
    reportMessage(ERROR_SPAN_ENCLOSER_START + message + SPAN_ENCLOSER_END);
  }

  /**
   * Reports information  along the top edge center of the UI.
   *
   * @param message  information message
   */

  // The reportInfo methods overloads the error message system to display messages that
  // aren't errors.    This is a bit of a hack, but it's convenient because it guarantees
  // that the information messages won't collide with error messages.

  public static void reportInfo(String message) {
    reportMessage(INFO_SPAN_ENCLOSER_START + message + SPAN_ENCLOSER_END);
  }

  /**
   * Hides the error popup.
   */
  public static void hide() {
    if (!Ode.isWindowClosing()) {
      POPUP.hide();
    }
  }
}
