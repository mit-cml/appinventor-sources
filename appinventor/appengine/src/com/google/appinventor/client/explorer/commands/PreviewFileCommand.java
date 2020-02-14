// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Command for previewing files.
 *
 */
public class PreviewFileCommand extends ChainableCommand {
  /**
   * Creates a new command for previewing a file.
   */
  public PreviewFileCommand() {
    super(null); // no next command
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return false;
  }

  @Override
  public boolean isSupported(final ProjectNode node) {
    return StorageUtil.isImageFile(node.getFileId()) || StorageUtil.isAudioFile(node.getFileId())
        || StorageUtil.isVideoFile(node.getFileId());
  }

  @Override
  public void execute(final ProjectNode node) {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(node.getName());
    dialogBox.setStylePrimaryName("ode-DialogBox");

    //setting position of dialog box
    dialogBox.center();
    dialogBox.setAnimationEnabled(true);

    //button element
    final Button closeButton = new Button(MESSAGES.closeFilePreview());
    closeButton.getElement().setId("closeButton");
    closeButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          dialogBox.hide();
        }
      });

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    buttonPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
    buttonPanel.add(closeButton);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
    dialogPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

    Widget filePreview = generateFilePreview(node);
    dialogPanel.clear();
    dialogPanel.add(filePreview);

    dialogPanel.add(buttonPanel);
    dialogPanel.setWidth("300px");

    dialogBox.setGlassEnabled(false);
    dialogBox.setModal(false);

    // Set the contents of the Widget
    dialogBox.setWidget(dialogPanel);
    dialogBox.center();
    dialogBox.show();
  }

  /**
   * Generate a file preview to display
   *
   * @param node
   * @return widget
   */
  private Widget generateFilePreview(ProjectNode node) {
    String fileSuffix = node.getProjectId() + "/" + node.getFileId();
    String fileUrl = StorageUtil.getFileUrl(node.getProjectId(), node.getFileId());

    if (StorageUtil.isImageFile(fileSuffix)) { // Image Preview
      String fileType = StorageUtil.getContentTypeForFilePath(fileSuffix);
      // Support preview for file types that all major browser support
      if (fileType.endsWith("png") || fileType.endsWith("jpeg") || fileType.endsWith("gif")
          || fileType.endsWith("bmp") || fileType.endsWith("svg+xml")) {
        Image img = new Image(fileUrl);
        img.getElement().getStyle().setProperty("maxWidth","600px");
        return img;
      }
    } else if (StorageUtil.isAudioFile(fileSuffix)) { // Audio Preview
      String fileType = StorageUtil.getContentTypeForFilePath(fileSuffix);
      if (fileType.endsWith("mp3") || fileType.endsWith("wav") || fileType.endsWith("ogg")) {
        return new HTML("<audio controls><source src='" + fileUrl + "' type='" + fileType
            + "'>" + MESSAGES.filePlaybackError() + "</audio>");
      }
    } else if (StorageUtil.isVideoFile(fileSuffix)) { // Video Preview
      String fileType = StorageUtil.getContentTypeForFilePath(fileSuffix);
      if (fileType.endsWith("avi") || fileType.endsWith("mp4") || fileType.endsWith("webm")) {
        return new HTML("<video width='320' height='240' controls> <source src='" + fileUrl
            + "' type='" + fileType + "'>" + MESSAGES.filePlaybackError() + "</video>");
      }
    }
    return new HTML(MESSAGES.filePreviewError());
  }
}
