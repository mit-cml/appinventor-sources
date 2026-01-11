// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.wizards.MarkOriginWizard;
import com.google.appinventor.shared.rpc.project.HasAssetsFolder;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

/**
 * Property editor for origin property of ImageSprites.
 *
 */
public class YoungAndroidSpriteOriginPropertyEditor extends PropertyEditor {

  public static final String PROPERTY_NAME_PICTURE = "Picture";
  private final SimpleEditor editor;

  /**
   * Returns the asset node with the given name.
   *
   * @param name  asset name
   * @return  asset node found or {@code null}
   */
  protected ProjectNode getAssetNode(String name) {
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
   * Converts the given image property value to an image url.
   * Returns null if the image property value is blank or not recognized as an
   * asset.
   */
  protected String convertImagePropertyValueToUrl(String text) {
    if (!text.isEmpty()) {
      ProjectNode asset = getAssetNode(text);
      if (asset != null) {
        return StorageUtil.getFileUrl(asset.getProjectId(), asset.getFileId());
      }
    }
    return null;
  }

  /**
   * Creates a new property editor for selecting origin of image sprites.
   *
   * @param editor the editor that this property editor belongs to
   */
  public YoungAndroidSpriteOriginPropertyEditor(final SimpleEditor editor) {
    this.editor = editor;

    Button markOriginButton = new Button(Ode.MESSAGES.markOriginButton());
    markOriginButton.setWidth("100%");

    markOriginButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          MarkOriginWizard.OriginSelectedCallback callback =
              new MarkOriginWizard.OriginSelectedCallback() {
                @Override
                public void onSelected(String value) {
                  property.setValue(value);
                }
              };
          String imageUrl = convertImagePropertyValueToUrl(
                  property.getEditableProperties().getPropertyValue(PROPERTY_NAME_PICTURE));
          if (imageUrl == null || imageUrl.isEmpty()) {
            Window.alert(Ode.MESSAGES.provideImageFirst());
            return;
          }
          MarkOriginWizard dialog = new MarkOriginWizard(imageUrl, property.getValue(), callback);
          dialog.show();
        }
    });

    initWidget(markOriginButton);
  }

  @Override
  protected void updateValue() {

  }
}
