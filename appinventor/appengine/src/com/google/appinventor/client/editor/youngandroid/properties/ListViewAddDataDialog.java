// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.wizards.Dialog;

import com.google.appinventor.components.common.ComponentConstants;

import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import com.google.appinventor.shared.storage.StorageUtil;

import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The "Click to Add/Delete Data" dialog for the ListView ListData property.
 *
 * <p>The dialog chrome (title, button bar and the container that holds the data rows) is declared
 * in {@code ListViewAddDataDialog.ui.xml} and wrapped in the accessible {@link Dialog} widget, which
 * provides the ARIA role, modal semantics, Escape-to-close and focus management.
 *
 * <p>The data grid is built from real, focusable widgets: each row is a {@link ListViewDataRow}
 * added to {@code rowsContainer}. The rows are the source of truth, so there is no separate working
 * copy. Which fields a row exposes ({@code Text1}, {@code Text2}, {@code Image}) is derived from the
 * current ListView layout and applied by showing/hiding the row's widgets. The saved JSON is
 * unchanged from the previous {@code CellTable}-based implementation.
 */
public class ListViewAddDataDialog {

  interface ListViewAddDataDialogUiBinder extends UiBinder<Widget, ListViewAddDataDialog> {}

  private static final ListViewAddDataDialogUiBinder UI_BINDER =
      GWT.create(ListViewAddDataDialogUiBinder.class);

  @UiField Dialog dialogBox;
  @UiField FlowPanel rowsContainer;
  @UiField Button addRow;
  @UiField Button save;
  @UiField Button cancel;
  @UiField Button topInvisible;
  @UiField Button bottomInvisible;

  private final YoungAndroidListViewAddDataPropertyEditor owner;
  private final int layout;

  /* Which fields apply to the current layout, derived once from the layout mode. */
  private final boolean showDetail;
  private final boolean showImage;

  /* Asset names (incl. "None") offered by each row's image picker. */
  private final List<String> imageChoices;

  /* Map from asset name to a previewable URL (no entry for "None"). */
  private final Map<String, String> imageUrls;

  ListViewAddDataDialog(YoungAndroidListViewAddDataPropertyEditor owner, int layout) {
    this.owner = owner;
    this.layout = layout;
    this.showDetail = layoutHasDetail(layout);
    this.showImage = layoutHasImage(layout);
    this.imageUrls = new HashMap<String, String>();
    this.imageChoices = buildImageChoices(imageUrls);
    UI_BINDER.createAndBindUi(this);

    dialogBox.setAriaLabel(MESSAGES.listDataAddDataTitle());
    addRow.setText(MESSAGES.listDataAddRowButton());
    save.setText(MESSAGES.saveButton());
    cancel.setText(MESSAGES.cancelButton());

    for (JSONObject item : owner.getItems()) {
      rowsContainer.add(newRow(item));
    }
    updateCount();
  }

  /** Creates a data row wired to the dialog's count refresh and keyboard navigation. */
  private ListViewDataRow newRow(JSONObject item) {
    return new ListViewDataRow(item, showDetail, showImage, imageChoices, imageUrls,
        new ListViewDataRow.Callbacks() {
          @Override
          public void onRowDeleted() {
            updateCount();
          }

          @Override
          public void onEnterAddRow() {
            addBlankRow().focusFirstCell();
          }

          @Override
          public void onMoveFocus(ListViewDataRow from, int deltaRows, int column) {
            int index = rowsContainer.getWidgetIndex(from) + deltaRows;
            if (index >= 0 && index < rowsContainer.getWidgetCount()) {
              ((ListViewDataRow) rowsContainer.getWidget(index)).focusColumn(column);
            }
          }
        });
  }

  /** Renders the title plus the live item count into the draggable caption bar. */
  private void updateCount() {
    String html = "<span class=\"lie-cap-title\">"
        + SafeHtmlUtils.htmlEscape(MESSAGES.listDataAddDataTitle())
        + "</span><span class=\"lie-cap-count\">"
        + SafeHtmlUtils.htmlEscape(MESSAGES.listDataItemCount(rowsContainer.getWidgetCount()))
        + "</span>";
    dialogBox.getCaption().setHTML(SafeHtmlUtils.fromTrustedString(html));
  }

  /** Centers and shows the dialog. */
  void show() {
    dialogBox.center();
  }

  /** Whether the given layout shows a detail (second) text field. */
  private static boolean layoutHasDetail(int layout) {
    return layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT
        || layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR
        || layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT
        || layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TOP_TWO_TEXT;
  }

  /** Whether the given layout shows an image picker. */
  private static boolean layoutHasImage(int layout) {
    return layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT
        || layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT
        || layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TOP_TWO_TEXT;
  }

  /**
   * Builds the list of selectable image assets ("None" plus every project asset) and fills
   * {@code urls} with a previewable URL per asset name.
   */
  private List<String> buildImageChoices(Map<String, String> urls) {
    Project project = Ode.getInstance().getProjectManager()
        .getProject(owner.getSimpleEditor().getProjectId());
    YoungAndroidAssetsFolder assetsFolder =
        ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();
    List<String> choices = new ArrayList<String>();
    choices.add("None");
    if (assetsFolder != null) {
      for (ProjectNode node : assetsFolder.getChildren()) {
        choices.add(node.getName());
        urls.put(node.getName(), StorageUtil.getFileUrl(node.getProjectId(), node.getFileId()));
      }
    }
    return choices;
  }

  @UiHandler("addRow")
  void onAddRow(ClickEvent event) {
    addBlankRow().focusFirstCell();
  }

  /** Appends a blank row for the current layout, refreshes the count, and returns it. */
  private ListViewDataRow addBlankRow() {
    // creates a row with default data for the corresponding layout type
    JSONObject data = new JSONObject();
    data.put("Text1", new JSONString(""));
    if (showDetail) {
      data.put("Text2", new JSONString(""));
    }
    if (showImage) {
      data.put("Image", new JSONString("None"));
    }
    ListViewDataRow row = newRow(data);
    rowsContainer.add(row);
    updateCount();
    return row;
  }

  @UiHandler("save")
  void onSave(ClickEvent event) {
    // save the data for the corresponding layout type
    List<JSONObject> items = owner.getItems();
    items.clear();
    JSONArray rows = new JSONArray();
    for (int i = 0; i < rowsContainer.getWidgetCount(); ++i) {
      ListViewDataRow row = (ListViewDataRow) rowsContainer.getWidget(i);
      JSONObject obj = row.toJsonObject();
      // null fields that don't belong to the current layout so the saved JSON is unchanged
      if (!showDetail && obj.containsKey("Text2")) {
        obj.put("Text2", null);
      }
      if (!showImage && obj.containsKey("Image")) {
        obj.put("Image", null);
      }
      rows.set(i, obj);
      items.add(i, obj);
    }
    owner.commitListData(rows.toString());
    dialogBox.hide();
  }

  @UiHandler("cancel")
  void onCancel(ClickEvent event) {
    // discards changes in the data for the corresponding layout type
    final Cancel confirm = new Cancel();
    confirm.center();
    confirm.setStylePrimaryName("ode-DialogBox");
    confirm.show();
    confirm.yes.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        confirm.hide();
        dialogBox.hide();
      }
    });
    confirm.no.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        confirm.hide();
      }
    });
  }

  /*
   * The invisible "FocusTrap" buttons at the top and bottom of the dialog keep keyboard focus
   * contained within the popup: tabbing past the last control wraps to the first, and vice versa.
   */
  @UiHandler("topInvisible")
  void focusLast(FocusEvent event) {
    save.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  void focusFirst(FocusEvent event) {
    addRow.setFocus(true);
  }

  /**
   * Confirmation dialog shown when the user clicks CANCEL, asking whether to discard unsaved data.
   */
  static class Cancel extends DialogBox {
    VerticalPanel verticalPanel;
    HorizontalPanel buttonPanel;
    Button yes;
    Button no;

    Cancel() {
      verticalPanel = new VerticalPanel();
      buttonPanel = new HorizontalPanel();
      setText(MESSAGES.cancelButton());
      verticalPanel.add(new Label(MESSAGES.listDataConcelConfirm()));
      yes = new Button(MESSAGES.okButton());
      no = new Button(MESSAGES.cancelButton());
      buttonPanel.add(yes);
      buttonPanel.add(no);
      buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      verticalPanel.add(buttonPanel);
      setWidget(verticalPanel);
    }
  }
}
