// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.appinventor.client.widgets.boxes.Box;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A Utility for Dialog Boxes
 *
 * Put up a Dialog Box in the middle of the screen. It must have an
 * "OK" button and can optionally have a "Cancel" button. Also takes
 * an object to call on OK or Cancel after the OK or Cancel button is
 * pressed. This dialog is modal, locking out other activity.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

public class MessageDialog {
  public interface Actions {
    /**
     * Action to perform when OK button is pressed.
     * Note: the dialog box itself is already dismissed
     */
    public void onOK();

    /**
     * Action to peform when the Cancel button is pressed.
     * Note: the dialog box itself is already dismissed
     */
    public void onCancel();
  }

  private MessageDialog() {
  }

  /**
   * Put up a modal dialog box.
   *
   * @param title Title for the dialog, already internationalized
   * @param message Message box content, already internationalized
   * @param OK String for OK button, already internationalized
   * @param Cancel String for Cancel button, null if non, internationalized
   * @param actions Actions object to call upon completion, can be null
   */
  public static void messageDialog(String title, String message, String OK, String Cancel,
    final Actions actions) {
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(title);
    dialogBox.setHeight("100px");
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML messageHtml = new HTML("<p>" + message + "</p>");
    messageHtml.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button okButton = new Button(OK);
    okButton.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          if (actions != null)
            actions.onOK();
        }
      });
    holder.add(okButton);
    if (Cancel != null) {
      Button cancelButton = new Button(Cancel);
      cancelButton.addClickListener(new ClickListener() {
          @Override
          public void onClick(Widget sender) {
            dialogBox.hide();
            if (actions != null)
              actions.onCancel();
          }
        });
      holder.add(cancelButton);
    }
    DialogBoxContents.add(messageHtml);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }
}
