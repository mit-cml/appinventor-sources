// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.output;

import com.google.appinventor.client.BugReport;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.Set;

/**
 * Output panel for displaying ODE internal logging messages.
 *
 * <p>Note that logging is only active in hosted mode.
 *
 */
// TODO(user): Make this mesh with the new Logger interface.
public final class OdeLog extends Composite {

  // Singleton logging instance
  private static class SingletonHolder {
    private static final OdeLog INSTANCE = new OdeLog();
  }

  // Message style
  private static final String messageStyle = "style=\"font-size:small\"";

  // UI elements
  private final HTML text;

  /**
   * Returns singleton ODE logging instance.
   *
   * @return  ODE logging instance
   */
  public static OdeLog getOdeLog() {
    if (isLogAvailable()) {
      return SingletonHolder.INSTANCE;
    }

    throw new UnsupportedOperationException("logging not available");
  }

  /**
   * Creates a new output panel for displaying internal messages.
   */
  private OdeLog() {
    // Initialize UI
    Button clearButton = new Button(MESSAGES.clearButton());
    clearButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        clear();
      }
    });

    text = new HTML();
    text.setWidth("100%");

    VerticalPanel panel = new VerticalPanel();
    panel.add(clearButton);
    panel.add(text);
    panel.setSize("100%", "100%");
    panel.setCellHeight(text, "100%");
    panel.setCellWidth(text, "100%");

    initWidget(panel);
  }

  /**
   * Indicates whether logging is available.
   *
   * @return  logging availability
   */
  public static final boolean isLogAvailable() {
    return AppInventorFeatures.hasDebuggingView();
  }

  /**
   * Prints a log message.
   *
   * @param message  message to print
   */
  public static void log(String message) {
    if (isLogAvailable() && !Ode.isWindowClosing()) {
      getOdeLog().println(StringUtils.escape(message));
    }
  }

  /**
   * Prints a log warning message.
   *
   * @param message  message to print
   */
  public static void wlog(String message) {
    if (isLogAvailable() && !Ode.isWindowClosing()) {
      getOdeLog().wprintln(StringUtils.escape(message));
    }
  }

  /**
   * Prints a log error message.
   *
   * @param message  message to print
   */
  public static void elog(String message) {
    if (isLogAvailable() && !Ode.isWindowClosing()) {
      getOdeLog().eprintln(StringUtils.escape(message));
    }
  }

  /**
   * Prints a log message for an exception.
   *
   * @param throwable  exception thrown
   */
  public static void xlog(Throwable throwable) {
    if (isLogAvailable() && !Ode.isWindowClosing()) {
      getOdeLog().eprintln(StringUtils.escape(throwable.toString()) + prepareStackTrace(throwable));
      if (AppInventorFeatures.sendBugReports()) {
        // For this message, we don't escape. We want the bug report link to show as a link.
        getOdeLog().eprintln("File a <a href=\"" + BugReport.getBugReportLink(throwable)
            + "\" target=\"_blank\">bug report</a> for this error.");
      }
    }
  }

  private static String prepareStackTrace(Throwable throwable) {
    StringBuilder html = new StringBuilder();
    html.append("<ul>");
    for (StackTraceElement element : throwable.getStackTrace()) {
      html.append("<li>").append(StringUtils.escape(element.toString())).append("</li>");
    }

    html.append(prepareCause(throwable.getCause()));

    if (throwable instanceof UmbrellaException) {
      Set<Throwable> causes = ((UmbrellaException) throwable).getCauses();
      if (causes != null && !causes.isEmpty()) {
        for (Throwable cause : causes) {
          html.append(prepareCause(cause));
        }
      }
    }

    html.append("</ul>");
    return html.toString();
  }

  private static String prepareCause(Throwable cause) {
    StringBuilder html = new StringBuilder();
    if (cause != null) {
      html.append("<li>Caused by ").append(StringUtils.escape(cause.toString())).append("</li>")
          .append(prepareStackTrace(cause));
    }
    return html.toString();
  }

  /*
   * Prints a log message.
   */
  private void println(String message) {
    text.setHTML(text.getHTML() + "<div " + messageStyle + ">" +
      "<span style=\"color:green\">[INFO] </span>" + message + "</div>");
  }

  /*
   * Prints a log warning message.
   */
  private void wprintln(String message) {
    text.setHTML(text.getHTML() + "<div " + messageStyle + ">" +
        "<span style=\"color:orange\">[WARNING] </span>" + message +
        "</div>");
  }

  /*
   * Prints a log error message.
   */
  private void eprintln(String message) {
    text.setHTML(text.getHTML() + "<div " + messageStyle + ">" +
        "<span style=\"color:red\">[ERROR] </span>" + message + "</div>");
  }

  /*
   * Clears all messages.
   */
  private void clear() {
    if (!Ode.isWindowClosing()) {
      text.setText("");
    }
  }
}
