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

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextInputCell;

import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.view.client.ListDataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * The "Click to Add/Delete Data" dialog for the ListView ListData property.
 *
 * <p>This is the UiBinder migration of the former {@code ClickHandle} inner class. The dialog
 * chrome (title, button bar and the container that holds the data rows) is now declared in
 * {@code ListViewAddDataDialog.ui.xml} and wrapped in the accessible {@link Dialog} widget, which
 * provides the ARIA role, modal semantics, Escape-to-close and focus management. The data grid
 * itself is still the original {@link CellTable}; replacing it with real-widget rows is the next
 * step (PR 1.2).
 */
public class ListViewAddDataDialog {

  interface ListViewAddDataDialogUiBinder extends UiBinder<Widget, ListViewAddDataDialog> {}

  private static final ListViewAddDataDialogUiBinder UI_BINDER =
      GWT.create(ListViewAddDataDialogUiBinder.class);

  @UiField Dialog dialogBox;
  @UiField FlowPanel tableContainer;
  @UiField Button addRow;
  @UiField Button save;
  @UiField Button cancel;
  @UiField Button topInvisible;
  @UiField Button bottomInvisible;

  private final YoungAndroidListViewAddDataPropertyEditor owner;
  private final int layout;

  /* Working copy of the rows; only written back to the property on SAVE. */
  private final List<JSONObject> itemsCopy = new ArrayList<JSONObject>();
  private final CellTable<JSONObject> table = new CellTable<JSONObject>();
  private final ListDataProvider<JSONObject> model = new ListDataProvider<JSONObject>(itemsCopy);
  private final JSONArray rows = new JSONArray();

  ListViewAddDataDialog(YoungAndroidListViewAddDataPropertyEditor owner, int layout) {
    this.owner = owner;
    this.layout = layout;
    UI_BINDER.createAndBindUi(this);

    dialogBox.setCaption(MESSAGES.listDataAddDataTitle());
    dialogBox.setAriaLabel(MESSAGES.listDataAddDataTitle());
    addRow.setText(MESSAGES.listDataAddRowButton());
    save.setText(MESSAGES.saveButton());
    cancel.setText(MESSAGES.cancelButton());

    for (JSONObject item : owner.getItems()) {
      itemsCopy.add(item);
    }

    table.setRowData(itemsCopy);
    table.setEmptyTableWidget(new Label("No row data available yet!"));
    model.addDataDisplay(table);

    buildColumns();
    tableContainer.add(table);
  }

  /** Centers and shows the dialog. */
  void show() {
    dialogBox.center();
  }

  /**
   * Creates the table columns for the current ListView layout. The set of columns determines which
   * fields ({@code Text1}, {@code Text2}, {@code Image}) a row exposes for editing.
   */
  private void buildColumns() {
    if (layout == ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT) {
      table.addColumn(createTextBoxes("Text1"), MESSAGES.listDataMainTextHeader());
    } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT ||
          layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) {
      table.addColumn(createTextBoxes("Text1"), MESSAGES.listDataMainTextHeader());
      table.addColumn(createTextBoxes("Text2"), MESSAGES.listDataDetailTextHeader());
    } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
      table.addColumn(createTextBoxes("Text1"), MESSAGES.listDataMainTextHeader());
      table.addColumn(createImageSelectionDropDown("Image"), MESSAGES.listDataImageHeader());
    } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT ||
        layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TOP_TWO_TEXT) {
      table.addColumn(createTextBoxes("Text1"), MESSAGES.listDataMainTextHeader());
      table.addColumn(createTextBoxes("Text2"), MESSAGES.listDataDetailTextHeader());
      table.addColumn(createImageSelectionDropDown("Image"), MESSAGES.listDataImageHeader());
    }

    table.addColumn(createDeleteButton());
  }

  private Column<JSONObject, String> createDeleteButton() {
    Column<JSONObject, String> column = new Column<JSONObject, String>(new ButtonCell()) {
      @Override
      public String getValue(JSONObject jsonObject) {
        return "DELETE";
      }
    };
    column.setFieldUpdater(new FieldUpdater<JSONObject, String>() {
      @Override
      public void update(int i, JSONObject jsonObject, String s) {
        itemsCopy.remove(i);
        model.refresh();
        table.setRowCount(0);
        table.setRowData(0, itemsCopy);
      }
    });
    return column;
  }

  private Column<JSONObject, String> createTextBoxes(final String columnKey) {
    Column<JSONObject, String> column = new Column<JSONObject, String>(new TextInputCell()) {
      @Override
      public String getValue(JSONObject jsonObject) {
        if (jsonObject.containsKey(columnKey)) {
          JSONString stringVal = (JSONString) jsonObject.get(columnKey);
          return (stringVal.stringValue());
        } else {
          jsonObject.put(columnKey, new JSONString(""));
          return "";
        }
      }
    };
    column.setFieldUpdater(new FieldUpdater<JSONObject, String>() {
      @Override
      public void update(int i, JSONObject jsonObject, String s) {
        jsonObject.put(columnKey, new JSONString(s));
      }
    });
    return column;
  }

  private Column<JSONObject, String> createImageSelectionDropDown(final String columnKey) {
    Project project = Ode.getInstance().getProjectManager()
        .getProject(owner.getSimpleEditor().getProjectId());
    YoungAndroidAssetsFolder assetsFolder =
        ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();
    List<String> choices = new ArrayList<String>();
    choices.add(0, "None");
    if (assetsFolder != null) {
      for (ProjectNode node : assetsFolder.getChildren()) {
        choices.add(node.getName());
      }
    }
    Column<JSONObject, String> column = new Column<JSONObject, String>(new SelectionCell(choices)) {
      @Override
      public String getValue(JSONObject jsonObject) {
        if (jsonObject.containsKey(columnKey)) {
          JSONString stringVal = (JSONString) jsonObject.get(columnKey);
          return (stringVal.stringValue());
        } else {
          jsonObject.put(columnKey, new JSONString(""));
          return "";
        }
      }
    };
    column.setFieldUpdater(new FieldUpdater<JSONObject, String>() {
      @Override
      public void update(int i, JSONObject jsonObject, String s) {
        jsonObject.put(columnKey, new JSONString(s));
      }
    });
    return column;
  }

  @UiHandler("addRow")
  void onAddRow(ClickEvent event) {
    // creates a row with default data for the corresponding layout type
    JSONObject data = new JSONObject();
    if (layout == ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT) {
      data.put("Text1", new JSONString(""));
    } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT ||
          layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) {
      data.put("Text1", new JSONString(""));
      data.put("Text2", new JSONString(""));
    } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
      data.put("Text1", new JSONString(""));
      data.put("Image", new JSONString("None"));
    } else if (layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT ||
        layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TOP_TWO_TEXT) {
      data.put("Text1", new JSONString(""));
      data.put("Text2", new JSONString(""));
      data.put("Image", new JSONString("None"));
    }
    itemsCopy.add(data);
    table.setRowData(itemsCopy);
  }

  @UiHandler("save")
  void onSave(ClickEvent event) {
    // save the data for the corresponding layout type
    List<JSONObject> items = owner.getItems();
    items.clear();
    for (int i = 0; i < itemsCopy.size(); ++i) {
      JSONObject obj = itemsCopy.get(i);
      if ((layout == ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT ||
          layout == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) && obj.containsKey("Text2")) {
        obj.put("Text2", null);
      }
      if ((layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT ||
          layout == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) && obj.containsKey("Image")) {
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
        itemsCopy.clear();
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
    cancel.setFocus(true);
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
