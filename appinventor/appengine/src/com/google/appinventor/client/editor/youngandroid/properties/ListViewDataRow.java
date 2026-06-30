// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.util.List;
import java.util.Map;

/**
 * A single editable row in the ListView ListData editor dialog.
 *
 * <p>Each row is a "thumbnail-forward" card built from real, focusable widgets: an image thumbnail
 * (which opens an asset picker when clicked), a {@link TextBox} for the main text, an optional
 * {@link TextBox} for the detail text and a delete {@link Button}. The detail and image widgets are
 * shown or hidden according to the current ListView layout, mirroring the per-layout column logic the
 * old {@code CellTable} performed.
 *
 * <p>The row owns its backing {@link JSONObject}: {@link #toJsonObject()} writes the current widget
 * values back into it and returns it, preserving any keys that belong to other layouts so the saved
 * JSON stays identical to the previous implementation.
 */
public class ListViewDataRow extends Composite {

  interface ListViewDataRowUiBinder extends UiBinder<FlowPanel, ListViewDataRow> {}

  private static final ListViewDataRowUiBinder UI_BINDER =
      GWT.create(ListViewDataRowUiBinder.class);

  /** Sentinel value used by the ListData JSON for "no image". */
  private static final String NO_IMAGE = "None";

  @UiField FlowPanel container;
  @UiField FlowPanel thumbColumn;
  @UiField FocusPanel thumbTile;
  @UiField Label thumbName;
  @UiField TextBox mainText;
  @UiField TextBox detailText;
  @UiField Button delete;

  private final JSONObject item;
  private final boolean showDetail;
  private final boolean showImage;
  private final List<String> imageChoices;
  private final Map<String, String> imageUrls;
  private final Runnable onDelete;

  /** The currently selected image asset name (or {@link #NO_IMAGE}). */
  private String selectedImage = NO_IMAGE;

  /**
   * @param item the backing data for this row (mutated in place by {@link #toJsonObject()})
   * @param showDetail whether the detail text field applies to the current layout
   * @param showImage whether the image picker applies to the current layout
   * @param imageChoices the asset names (including "None") offered by the picker
   * @param imageUrls map from asset name to a previewable URL (no entry for "None")
   * @param onDelete callback invoked after this row removes itself (lets the dialog update its count)
   */
  ListViewDataRow(JSONObject item, boolean showDetail, boolean showImage,
      List<String> imageChoices, Map<String, String> imageUrls, Runnable onDelete) {
    this.item = item;
    this.showDetail = showDetail;
    this.showImage = showImage;
    this.imageChoices = imageChoices;
    this.imageUrls = imageUrls;
    this.onDelete = onDelete;
    initWidget(UI_BINDER.createAndBindUi(this));

    delete.setText("×");  // multiplication sign, used as a compact close/delete glyph
    // Keep the visible glyph short but expose the real action name to assistive tech.
    delete.getElement().setAttribute("aria-label", MESSAGES.deleteButton());
    delete.setTitle(MESSAGES.deleteButton());

    mainText.setText(getString("Text1"));

    if (showDetail) {
      detailText.setText(getString("Text2"));
    } else {
      detailText.setVisible(false);
    }

    if (showImage) {
      selectedImage = normalizeImage(getString("Image"));
      updateThumbnail();
      thumbTile.getElement().setTabIndex(0);
      thumbTile.getElement().setAttribute("aria-label", MESSAGES.listDataImageHeader());
      thumbTile.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          openImagePicker();
        }
      });
      thumbTile.addKeyDownHandler(new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent event) {
          int key = event.getNativeKeyCode();
          if (key == KeyCodes.KEY_ENTER || key == KeyCodes.KEY_SPACE) {
            event.preventDefault();
            openImagePicker();
          }
        }
      });
    } else {
      thumbColumn.setVisible(false);
    }
  }

  /** Writes the current widget values back into the backing item and returns it. */
  JSONObject toJsonObject() {
    item.put("Text1", new JSONString(mainText.getText()));
    if (showDetail) {
      item.put("Text2", new JSONString(detailText.getText()));
    }
    if (showImage) {
      item.put("Image", new JSONString(selectedImage));
    }
    return item;
  }

  /** Returns the string value for {@code key}, or "" if absent or not a string. */
  private String getString(String key) {
    if (item.containsKey(key)) {
      JSONValue value = item.get(key);
      JSONString stringValue = value.isString();
      if (stringValue != null) {
        return stringValue.stringValue();
      }
    }
    return "";
  }

  /** Maps a stored image value to a valid choice, falling back to "None". */
  private String normalizeImage(String value) {
    return imageChoices.contains(value) ? value : NO_IMAGE;
  }

  /** Renders the thumbnail tile and filename label for the current selection. */
  private void updateThumbnail() {
    String url = imageUrls.get(selectedImage);
    if (url != null) {
      Image image = new Image(url);
      thumbTile.setWidget(image);
      thumbTile.addStyleName("lie-thumb-filled");
      thumbName.setText(selectedImage);
      thumbName.setTitle(selectedImage);
    } else {
      thumbTile.setWidget(new InlineLabel("IMG"));
      thumbTile.removeStyleName("lie-thumb-filled");
      thumbName.setText(NO_IMAGE);
      thumbName.setTitle(NO_IMAGE);
    }
  }

  /** Opens the asset picker anchored to the thumbnail tile. */
  private void openImagePicker() {
    AssetImagePicker picker = new AssetImagePicker(imageChoices, imageUrls, selectedImage,
        new AssetImagePicker.Callback() {
          @Override
          public void onImageSelected(String name) {
            selectedImage = name;
            updateThumbnail();
          }
        });
    picker.showRelativeTo(thumbTile);
  }

  @UiHandler("delete")
  void onDeleteClicked(ClickEvent event) {
    removeFromParent();
    if (onDelete != null) {
      onDelete.run();
    }
  }

  /**
   * A small popup that lists "None" plus every project image asset (with a preview swatch) and
   * reports the chosen asset name back through {@link Callback}. This realizes Susan's preferred
   * interaction: clicking the row thumbnail opens this picker rather than using a dropdown.
   */
  static class AssetImagePicker extends PopupPanel {

    interface Callback {
      void onImageSelected(String name);
    }

    AssetImagePicker(List<String> choices, Map<String, String> urls, String current,
        final Callback callback) {
      super(true, false);  // autoHide, non-modal
      setAnimationEnabled(true);

      FlowPanel list = new FlowPanel();
      list.setStyleName("lie-picker");

      for (final String name : choices) {
        FocusPanel item = new FocusPanel();
        item.setStyleName("lie-picker-item");
        if (name.equals(current)) {
          item.addStyleName("lie-picker-selected");
        }
        item.getElement().setTabIndex(0);

        FlowPanel row = new FlowPanel();
        row.setStyleName("lie-picker-itemrow");

        FlowPanel swatch = new FlowPanel();
        swatch.setStyleName("lie-picker-swatch");
        String url = urls.get(name);
        if (url != null) {
          swatch.add(new Image(url));
        }
        row.add(swatch);

        Label nameLabel = new Label(name);
        nameLabel.setStyleName("lie-picker-name");
        row.add(nameLabel);

        item.setWidget(row);
        item.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            callback.onImageSelected(name);
            hide();
          }
        });
        item.addKeyDownHandler(new KeyDownHandler() {
          @Override
          public void onKeyDown(KeyDownEvent event) {
            int key = event.getNativeKeyCode();
            if (key == KeyCodes.KEY_ENTER || key == KeyCodes.KEY_SPACE) {
              event.preventDefault();
              callback.onImageSelected(name);
              hide();
            }
          }
        });
        list.add(item);
      }

      setWidget(list);
    }
  }
}
