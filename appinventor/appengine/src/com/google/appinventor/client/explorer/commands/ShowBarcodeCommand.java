// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Command for displaying a barcode for the target of a project.
 *
 * <p/>This command is often chained with SaveAllEditorsCommand and BuildCommand.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class ShowBarcodeCommand extends ChainableCommand {

  // The build target
  private String target;

  /**
   * Creates a new command for showing a barcode for the target of a project.
   *
   * @param target the build target
   */
  public ShowBarcodeCommand(String target) {
    // Since we don't know when the barcode dialog is finished, we can't
    // support a command after this one.
    super(null); // no next command
    this.target = target;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return false;
  }

  @Override
  public void execute(final ProjectNode node) {
    // Display a barcode for an url pointing at our server's download servlet
    String barcodeUrl = GWT.getHostPageBaseURL()
      + "b/" + Ode.getInstance().getNonce();
    OdeLog.log("Barcode url is: " + barcodeUrl);
    new BarcodeDialogBox(node.getName(), barcodeUrl).center();
  }

  static class BarcodeDialogBox extends DialogBox {

    BarcodeDialogBox(String projectName, String appInstallUrl) {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.barcodeTitle(projectName));

      ClickHandler buttonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
        }
      };

      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addClickHandler(buttonHandler);
      Button okButton = new Button(MESSAGES.okButton());
      okButton.addClickHandler(buttonHandler);
      HTML barcodeQrcode = new HTML("<center>" + BlocklyPanel.getQRCode(appInstallUrl) + "</center>");
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      HTML warningLabel = new HTML(MESSAGES.barcodeWarning(
          "<a href=\"" + "http://appinventor.mit.edu/explore/ai2/share.html" +
          "\" target=\"_blank\">",
          "</a>"));
      warningLabel.setWordWrap(true);
      warningLabel.setWidth("200px");  // set width to get the text to wrap
      HorizontalPanel warningPanel = new HorizontalPanel();
      warningPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
      warningPanel.add(warningLabel);

      // The cancel button is removed from the panel since it has no meaning in this
      // context.  But the logic is still here in case we want to restore it, and as
      // an example of how to code this stuff in GWT.
      // buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      VerticalPanel contentPanel = new VerticalPanel();
      contentPanel.add(barcodeQrcode);
      contentPanel.add(buttonPanel);
      contentPanel.add(warningPanel);
//      contentPanel.setSize("320px", "100%");
      add(contentPanel);
    }
  }
}
