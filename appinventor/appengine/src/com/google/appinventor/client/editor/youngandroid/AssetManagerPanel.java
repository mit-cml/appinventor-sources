// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.GlobalAssetProjectNode;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.StyleElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;

/**
 * Panel for managing assets in the App Inventor Designer.
 * Provides functionality for uploading, organizing, and managing project assets.
 */
public class AssetManagerPanel extends Composite {
  interface AssetManagerPanelUiBinder extends UiBinder<Widget, AssetManagerPanel> {}
  private static final AssetManagerPanelUiBinder UI_BINDER = GWT.create(AssetManagerPanelUiBinder.class);

  @UiField
  VerticalPanel mainPanel;
  
  @UiField
  HorizontalPanel toolbarPanel;
  
  @UiField
  TextBox searchBox;
  
  @UiField
  Button uploadButton;
  
  @UiField
  Button createFolderButton;
  
  @UiField
  ListBox typeFilter;
  
  @UiField
  VerticalPanel foldersPanel;
  
  @UiField
  VerticalPanel tagsPanel;
  
  @UiField
  VerticalPanel previewPanel;
  
  @UiField
  VerticalPanel propertiesPanel;

  private ProjectNode selectedAssetNode;

  public AssetManagerPanel() {
    initWidget(UI_BINDER.createAndBindUi(this));
    
    // Initialize UI components
    initializeUI();
    
    // Add event handlers
    addEventHandlers();
  }

  private void initializeUI() {
    // Set up search box
    searchBox.setTitle(MESSAGES.searchAssetsPlaceholder());
    
    // Set up upload button
    uploadButton.setText(MESSAGES.uploadAssetButton());
    
    // Set up create folder button
    createFolderButton.setText(MESSAGES.createFolderButton());
    
    // Set up type filter
    typeFilter.addItem(MESSAGES.allTypesFilter());
    typeFilter.addItem(MESSAGES.imagesFilter());
    typeFilter.addItem(MESSAGES.audioFilter());
    typeFilter.addItem(MESSAGES.videoFilter());
    typeFilter.addItem(MESSAGES.otherFilter());
  }

  private void addEventHandlers() {
    // Search box handler
    searchBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        // TODO: Implement search functionality
      }
    });
    
    // Upload button handler
    uploadButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // TODO: Implement file upload dialog
      }
    });
    
    // Create folder button handler
    createFolderButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // TODO: Implement create folder dialog
      }
    });
    
    // Type filter handler
    typeFilter.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        // TODO: Implement type filtering
      }
    });
    
  }

  /**
   * Updates the folders panel with the current folder structure
   */
  private void updateFoldersPanel() {
    // TODO: Implement folder structure update
  }

  /**
   * Updates the tags panel with the current tags
   */
  private void updateTagsPanel() {
    // TODO: Implement tags update
  }

  /**
   * Updates the preview panel with the selected asset
   */
  public void updatePreviewPanel(ProjectNode node) {
    this.selectedAssetNode = node;
    previewPanel.clear();
    if (node == null) {
      return;
    }

    String fileSuffix;
    String fileUrl;

    if (node instanceof GlobalAssetProjectNode) {
        fileSuffix = node.getFileId();
        fileUrl = "/ode/download/globalasset/" + node.getFileId();
    } else {
        fileSuffix = node.getProjectId() + "/" + node.getFileId();
        fileUrl = StorageUtil.getFileUrl(node.getProjectId(), node.getFileId());
    }

    Widget previewWidget = null;

    if (StorageUtil.isImageFile(fileSuffix)) { // Image Preview
      String fileType = StorageUtil.getContentTypeForFilePath(fileSuffix);
      if (fileType.endsWith("png") || fileType.endsWith("jpeg") || fileType.endsWith("gif")
          || fileType.endsWith("bmp") || fileType.endsWith("svg+xml")) {
        Image img = new Image(fileUrl);
        img.getElement().getStyle().setProperty("maxWidth","600px");
        previewWidget = img;
      }
    } else if (StorageUtil.isAudioFile(fileSuffix)) { // Audio Preview
      String fileType = StorageUtil.getContentTypeForFilePath(fileSuffix);
      if (fileType.endsWith("mp3") || fileType.endsWith("wav") || fileType.endsWith("ogg")) {
        previewWidget = new HTML("<audio controls><source src='" + fileUrl + "' type='" + fileType
            + "'>" + MESSAGES.filePlaybackError() + "</audio>");
      }
    } else if (StorageUtil.isVideoFile(fileSuffix)) { // Video Preview
      String fileType = StorageUtil.getContentTypeForFilePath(fileSuffix);
      if (fileType.endsWith("avi") || fileType.endsWith("mp4") || fileType.endsWith("webm")) {
        previewWidget = new HTML("<video width='320' height='240' controls> <source src='" + fileUrl
            + "' type='" + fileType + "'>" + MESSAGES.filePlaybackError() + "</video>");
      }
    } else if (StorageUtil.isFontFile(fileSuffix))  {  // Font Preview
      String fileType = StorageUtil.getContentTypeForFilePath(fileSuffix);
      if (fileType.endsWith("ttf") || fileType.endsWith("otf")) {
        previewWidget = getFontResourcePreviewPanel(fileUrl);
      }
    }

    if (previewWidget != null) {
      previewPanel.add(previewWidget);
    } else {
      previewPanel.add(new HTML(MESSAGES.filePreviewError()));
    }
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

  /**
   * Updates the properties panel with the selected asset's properties
   */
  private void updatePropertiesPanel() {
    // TODO: Implement properties update
  }
} 