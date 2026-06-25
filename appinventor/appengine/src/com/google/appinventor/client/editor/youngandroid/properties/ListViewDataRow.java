// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.core.client.GWT;

import com.google.gwt.dom.client.Style.Unit;

import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import java.util.List;

/**
 * A single editable row in the ListView ListData editor dialog.
 *
 * <p>Replaces the virtualized {@code CellTable} cells with real, focusable widgets (a
 * {@link TextBox} for the main text, an optional {@link TextBox} for the detail text, an optional
 * {@link ListBox} for the image and a delete {@link Button}). The detail and image widgets are
 * shown or hidden according to the current ListView layout, mirroring the per-layout column logic
 * the {@code CellTable} used to perform.
 *
 * <p>The row owns its backing {@link JSONObject}: {@link #toJsonObject()} writes the current widget
 * values back into it and returns it, preserving any keys that belong to other layouts so the saved
 * JSON stays identical to the previous implementation.
 */
public class ListViewDataRow extends Composite {

  interface ListViewDataRowUiBinder extends UiBinder<HorizontalPanel, ListViewDataRow> {}

  private static final ListViewDataRowUiBinder UI_BINDER =
      GWT.create(ListViewDataRowUiBinder.class);

  // Column widths, shared with the dialog header (ListViewAddDataDialog) so the header labels line
  // up with the fields below them. Combined with box-sizing: border-box on the fields, the rendered
  // widths match these values exactly.
  static final String COLUMN_TEXT_WIDTH = "150px";
  static final String COLUMN_IMAGE_WIDTH = "130px";

  @UiField TextBox mainText;
  @UiField TextBox detailText;
  @UiField ListBox imagePicker;
  @UiField Button delete;

  private final JSONObject item;
  private final boolean showDetail;
  private final boolean showImage;

  /**
   * @param item the backing data for this row (mutated in place by {@link #toJsonObject()})
   * @param showDetail whether the detail text field applies to the current layout
   * @param showImage whether the image picker applies to the current layout
   * @param imageChoices the asset names (including "None") to populate the image picker with
   */
  ListViewDataRow(JSONObject item, boolean showDetail, boolean showImage,
      List<String> imageChoices) {
    this.item = item;
    this.showDetail = showDetail;
    this.showImage = showImage;
    initWidget(UI_BINDER.createAndBindUi(this));

    mainText.setWidth(COLUMN_TEXT_WIDTH);
    detailText.setWidth(COLUMN_TEXT_WIDTH);
    imagePicker.setWidth(COLUMN_IMAGE_WIDTH);

    delete.setText(MESSAGES.deleteButton());
    // The dialog stylesheet puts a 10px margin on every button; remove it for the in-row delete
    // button so the rows stay compact.
    delete.getElement().getStyle().setMargin(0, Unit.PX);

    mainText.setText(getString("Text1"));

    if (showDetail) {
      detailText.setText(getString("Text2"));
    } else {
      detailText.setVisible(false);
    }

    if (showImage) {
      for (String choice : imageChoices) {
        imagePicker.addItem(choice);
      }
      selectImage(getString("Image"));
    } else {
      imagePicker.setVisible(false);
    }
  }

  /** Writes the current widget values back into the backing item and returns it. */
  JSONObject toJsonObject() {
    item.put("Text1", new JSONString(mainText.getText()));
    if (showDetail) {
      item.put("Text2", new JSONString(detailText.getText()));
    }
    if (showImage) {
      int selected = imagePicker.getSelectedIndex();
      String image = selected >= 0 ? imagePicker.getItemText(selected) : "None";
      item.put("Image", new JSONString(image));
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

  /** Selects the image-picker entry matching {@code value}, falling back to the first ("None"). */
  private void selectImage(String value) {
    for (int i = 0; i < imagePicker.getItemCount(); i++) {
      if (imagePicker.getItemText(i).equals(value)) {
        imagePicker.setSelectedIndex(i);
        return;
      }
    }
    imagePicker.setSelectedIndex(0);
  }

  @UiHandler("delete")
  void onDelete(ClickEvent event) {
    removeFromParent();
  }
}
