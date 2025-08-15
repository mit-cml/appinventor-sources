// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.StyleElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

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
        || StorageUtil.isVideoFile(node.getFileId()) || StorageUtil.isFontFile(node.getFileId());
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
    } else if (StorageUtil.isFontFile(fileSuffix))  {  // Font Preview
      String fileType = StorageUtil.getContentTypeForFilePath(fileSuffix);
      if (fileType.endsWith("ttf") || fileType.endsWith("otf")) {
        return getFontResourcePreviewPanel(fileUrl);
      }
    }
    return new HTML(MESSAGES.filePreviewError());
  }
  
  private VerticalPanel getFontResourcePreviewPanel(String fontResourceURL) {
    VerticalPanel fontResourcePreviewPanel = new VerticalPanel();
    fontResourcePreviewPanel.setWidth("600px");
    fontResourcePreviewPanel.setHeight("400px");
    
    HorizontalPanel fontPropertiesPanel = new HorizontalPanel();
    fontPropertiesPanel.setHeight("100px");
    fontPropertiesPanel.setWidth("600px");
    fontPropertiesPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    final TextBox textPreviewTextBox = new TextBox();
    textPreviewTextBox.getElement().setPropertyString("placeholder", "Text for Preview");
    
    final TextBox fontSize = new TextBox();
    fontSize.getElement().setPropertyString("placeholder", "Font Size");
    fontSize.setText("16");
    
    CheckBox isFontBold = new CheckBox("Font Bold");
    CheckBox isFontItalic = new CheckBox("Font Italic");
    
    fontPropertiesPanel.add(textPreviewTextBox);
    fontPropertiesPanel.add(fontSize);
    fontPropertiesPanel.add(isFontBold);
    fontPropertiesPanel.add(isFontItalic);
    
    fontResourcePreviewPanel.add(fontPropertiesPanel);
    
    VerticalPanel fontPreviewPanel = new VerticalPanel();
    fontPreviewPanel.setHeight("300px");
    fontPreviewPanel.setWidth("600px");
    fontPreviewPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    fontPreviewPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
  
    StyleElement styleElement = Document.get().createStyleElement();
    String resource = "@font-face {";
    resource += "font-family: testFontFamily;";
    resource += "src: url(\"" + fontResourceURL + "\");";
    resource += "}";
    styleElement.setInnerText(resource);
    
    fontResourcePreviewPanel.getElement().insertFirst(styleElement);
    
    final Label previewText = new Label();
    DOM.setStyleAttribute(previewText.getElement(), "fontFamily", "testFontFamily");
    DOM.setStyleAttribute(previewText.getElement(), "fontSize",
      (int)(16 * 0.9) + "px");
    
    fontPreviewPanel.add(previewText);
    fontResourcePreviewPanel.add(fontPreviewPanel);
    
    textPreviewTextBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        previewText.setText(textPreviewTextBox.getText());
      }
    });
    
    fontSize.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        if (fontSize.getText() != null && !fontSize.getText().equals("")) {
          try {
            DOM.setStyleAttribute(previewText.getElement(), "fontSize",
              (int)(Float.parseFloat(fontSize.getText()) * 0.9) + "px");
          } catch (NumberFormatException e) {
            // Ignore this. If we throw an exception here, the project is unrecoverable.
          }
        }
      }
    });
    
    isFontBold.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
        if (valueChangeEvent.getValue())  {
          DOM.setStyleAttribute(previewText.getElement(), "fontWeight", "bold");
        } else {
          DOM.setStyleAttribute(previewText.getElement(), "fontWeight", "normal");
        }
      }
    });
  
    isFontItalic.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
        if (valueChangeEvent.getValue())  {
          DOM.setStyleAttribute(previewText.getElement(), "fontStyle", "italic");
        } else {
          DOM.setStyleAttribute(previewText.getElement(), "fontStyle", "normal");
        }
      }
    });
    
    return fontResourcePreviewPanel;
  }
}
