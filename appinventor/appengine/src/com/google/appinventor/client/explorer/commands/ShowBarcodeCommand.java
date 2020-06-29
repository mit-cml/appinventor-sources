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
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Command for displaying a barcode for the target of a project.
 * This command is often chained with SaveAllEditorsCommand and BuildCommand.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class ShowBarcodeCommand extends ChainableCommand {

  // The build target
  private String target;
  private boolean isAab;

  /**
   * Creates a new command for showing a barcode for the target of a project.
   *
   * @param target the build target
   */
  public ShowBarcodeCommand(String target, boolean isAab) {
    // Since we don't know when the barcode dialog is finished, we can't
    // support a command after this one.
    super(null); // no next command
    this.target = target;
    this.isAab = isAab;
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
    new BarcodeDialogBox(node.getName(), barcodeUrl, isAab).center();
  }

  static class BarcodeDialogBox extends DialogBox {

    BarcodeDialogBox(String projectName, final String appInstallUrl, boolean isAab) {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");
      setText(isAab ? MESSAGES.downloadAabDialogTitle(projectName) : MESSAGES.downloadApkDialogTitle(projectName));

      // Main layout panel
      VerticalPanel contentPanel = new VerticalPanel();

      // Container
      HorizontalPanel container = new HorizontalPanel();

      // Container > Left
      VerticalPanel left = new VerticalPanel();

      // Container > Left > Download Button
      ClickHandler downloadHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Window.open(appInstallUrl, "_self", "enabled");
        }
      };
      HorizontalPanel downloadPanel = new HorizontalPanel();
      downloadPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      Anchor downloadButton = new Anchor();
      downloadButton.setHref(appInstallUrl);
      downloadButton.addStyleName("gwt-Button");
      downloadButton.addStyleName("download-button");
      // Container > Left > Download Button > Image
      Image downloadIcon = new Image(Ode.getImageBundle().GetApp());
      downloadIcon.setSize("110px", "100px");
      downloadIcon.addStyleName("download-icon");
      downloadButton.getElement().appendChild(downloadIcon.getElement());
      // Container > Left > Download Button > Inner Text
      SpanElement text = Document.get().createSpanElement();
      text.setInnerHTML(isAab ? MESSAGES.barcodeDownloadAab() : MESSAGES.barcodeDownloadApk());
      downloadButton.getElement().appendChild(text);
      downloadButton.addClickHandler(downloadHandler);
      downloadPanel.add(downloadButton);
      downloadPanel.setSize("100%", "30px");
      left.add(downloadPanel);

      // Container > Left
      container.add(left);

      // The Android App Bundle should only be used to publish the app through Google Play Store. Thus,
      // it does not make sense to provide a QR code which the user might think they can scan and directly
      // install in the phone directly.
      if (!isAab) {
        // Container > Right
        VerticalPanel right = new VerticalPanel();

        // Container > Right > Barcode
        HTML barcodeQrcode = new HTML("<center>" + BlocklyPanel.getQRCode(appInstallUrl) + "</center>");
        barcodeQrcode.addStyleName("download-barcode");
        right.add(barcodeQrcode);

        // Container > Right
        container.add(right);
      }

      // Container
      contentPanel.add(container);

      // Warning
      // The warning label is added only in APK files, as there is no QR code for the AAB. It is supposed that
      // users download the bundle just when they get it.
      if (!isAab) {
        HorizontalPanel warningPanel = new HorizontalPanel();
        warningPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        HTML warningLabel = new HTML(MESSAGES.barcodeWarning(
            "<a href=\"" + "http://appinventor.mit.edu/explore/ai2/share.html" +
                "\" target=\"_blank\">",
            "</a>"));
        warningLabel.setWordWrap(true);
        warningLabel.setWidth("400px");  // set width to get the text to wrap
        warningLabel.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        warningPanel.add(warningLabel);
        contentPanel.add(warningPanel);
      }

      // OK button
      ClickHandler buttonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
        }
      };
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      Button okButton = new Button(MESSAGES.dismissButton());
      okButton.addClickHandler(buttonHandler);
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);

      add(contentPanel);
    }
  }
}
