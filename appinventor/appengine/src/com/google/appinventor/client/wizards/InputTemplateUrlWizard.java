// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Wizard for inputting the Url of a Template repository
 *
 * @author markf@google.com (Mark Friedman)
 * @author ram8647@gmail.com (Ralph Morelli)
 */
public final class InputTemplateUrlWizard extends Wizard {

  /**
   *  UI element for project name.
   */
  private LabeledTextBox urlTextBox;

  /**
   * Creates a new YoungAndroid project wizard.
   */
  public InputTemplateUrlWizard(final NewUrlDialogCallback callback) {
    super(MESSAGES.inputNewUrlCaption(), true, true);

    // Initialize the UI.
    setStylePrimaryName("ode-DialogBox");
    HorizontalPanel panel = new HorizontalPanel();

    urlTextBox = new LabeledTextBox(MESSAGES.newUrlLabel());
    urlTextBox.getTextBox().setWidth("250px");
    urlTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_ENTER) {
          handleOkClick();
        } else if (keyCode == KeyCodes.KEY_ESCAPE) {
          handleCancelClick();
        }
      }
    });

    VerticalPanel page = new VerticalPanel();
    panel.add(urlTextBox);
    page.add(panel);
    addPage(page);

    // Create finish command (create a new Young Android project).
    initFinishCommand(new Command() {
      @Override
      public void execute() {
        String hostUrl = urlTextBox.getText();
        if (TemplateUploadWizard.hasUrl(hostUrl)) {
          Window.alert("The Url " + hostUrl + " already exists.");
        } else {
          callback.updateTemplateOptions(hostUrl);
        }
      }
    });
  }

  @Override
  public void show() {
    super.show();
    // Wizard size (having it resize between page changes is quite annoying)
    int width = 500;
    int height = 40;
    this.center();

    setPixelSize(width, height);
    super.setPagePanelHeight(40);

    DeferredCommand.addCommand(new Command() {
      public void execute() {
        urlTextBox.setFocus(true);
        urlTextBox.selectAll();
      }
    });
  }
}
