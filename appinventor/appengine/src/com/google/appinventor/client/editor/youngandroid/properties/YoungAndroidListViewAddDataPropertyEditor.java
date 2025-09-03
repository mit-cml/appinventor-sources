// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import com.google.appinventor.client.explorer.project.Project;

import com.google.appinventor.client.widgets.properties.PropertyEditor;

import com.google.appinventor.components.common.ComponentConstants;

import com.google.appinventor.shared.rpc.project.ProjectNode;

import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextInputCell;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.google.gwt.view.client.ListDataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * AddData property in the ListView component. It is used to add/delete data
 * for different layout types of ListView.
 */

public class YoungAndroidListViewAddDataPropertyEditor extends PropertyEditor {

  private Button addData;

  private int layout;
  private List<JSONObject> items;
  private List<JSONObject> itemsCopy;

  private final SimpleEditor editor;

  public YoungAndroidListViewAddDataPropertyEditor(final SimpleEditor editor) {
    items = new ArrayList<JSONObject>();
    itemsCopy = new ArrayList<JSONObject>();
    addData = new Button("Click to Add/Delete Data");
    this.editor = editor;

    createButton();
  }

  @Override
  protected void updateValue() {
    String value = property.getValue();
    JSONValue jsonValue = value.isEmpty() ? null : JSONParser.parseStrict(value);
    if (jsonValue != null) {
      JSONArray array = jsonValue.isArray();
      items.clear();
      for (int i = 0; i < array.size(); ++i) {
        items.add(i, array.get(i).isObject());
      }
    }
  }

  private void createButton() {
    addData.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        ClickHandle ch = new ClickHandle(layout);
        ch.center();
        ch.setStylePrimaryName("ode-DialogBox");
        ch.show();
      }
    });
    initWidget(addData);
  }

  /**
   * set layout type of ListView so as to display contents AddData dialog box accordingly
   */
  public void setLayout(int layout) {
    this.layout = layout;
  }

  /**
   * class to display data table and add/delete data for AddData property
   */
  class ClickHandle extends DialogBox {
    VerticalPanel verticalPanel;
    HorizontalPanel actionButtons;
    Button add, save, cancel;
    CellTable<JSONObject> table;
    JSONArray rows;

    ListDataProvider<JSONObject> model;

    Column<JSONObject, String> createDeleteButton() {
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

    Column<JSONObject, String> createTextBoxes(final String columnKey) {
      Column<JSONObject, String> column = new Column<JSONObject, String>(new TextInputCell()) {
        @Override
        public String getValue(JSONObject jsonObject) {
          if (jsonObject.containsKey(columnKey)) {
            JSONString stringVal = (JSONString)jsonObject.get(columnKey);
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

    Column<JSONObject, String> createImageSelectionDropDown(final String columnKey) {
      Project project = Ode.getInstance().getProjectManager().getProject(editor.getProjectId());
      YoungAndroidAssetsFolder assetsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();
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
            JSONString stringVal = (JSONString)jsonObject.get(columnKey);
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

    ClickHandle(final int layoutValue) {

      verticalPanel = new VerticalPanel();
      actionButtons = new HorizontalPanel();
      itemsCopy = new ArrayList<JSONObject>();
      model = new ListDataProvider(itemsCopy);
      table = new CellTable<JSONObject>();
      rows = new JSONArray();
      add  = new Button("Click to Add Row Data");
      save = new Button("SAVE");
      cancel = new Button("CANCEL");

      setText(MESSAGES.listDataAddDataTitle());

      for (int i = 0; i < items.size(); ++i) {
        itemsCopy.add(i, items.get(i));
      }

      table.setRowData(itemsCopy);
      table.setEmptyTableWidget(new Label("No row data available yet!"));
      model.addDataDisplay(table);

      /*
       * create table columns and type of each column according to the type of ListView layout
       */
      if (layoutValue == ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT) {
        table.addColumn(createTextBoxes("Text1"), MESSAGES.listDataMainTextHeader());
      } else if (layoutValue == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT ||
            layoutValue == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) {
        table.addColumn(createTextBoxes("Text1"), MESSAGES.listDataMainTextHeader());
        table.addColumn(createTextBoxes("Text2"), MESSAGES.listDataDetailTextHeader());
      } else if (layoutValue == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
        table.addColumn(createTextBoxes("Text1"), MESSAGES.listDataMainTextHeader());
        table.addColumn(createImageSelectionDropDown("Image"), MESSAGES.listDataImageHeader());
      } else if (layoutValue == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT ||
          layoutValue ==ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TOP_TWO_TEXT) {
        table.addColumn(createTextBoxes("Text1"), MESSAGES.listDataMainTextHeader());
        table.addColumn(createTextBoxes("Text2"), MESSAGES.listDataDetailTextHeader());
        table.addColumn(createImageSelectionDropDown("Image"), MESSAGES.listDataImageHeader());
      }

      table.addColumn(createDeleteButton());

      add.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          /*
           * creates a row with default data for the corresponding layout type
           */
          JSONObject data = new JSONObject();
          if (layoutValue == ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT) {
            data.put("Text1", new JSONString(""));
          } else if (layoutValue == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT ||
                layoutValue == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) {
            data.put("Text1", new JSONString(""));
            data.put("Text2", new JSONString(""));
          } else if (layoutValue == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) {
            data.put("Text1", new JSONString(""));
            data.put("Image", new JSONString("None"));
          } else if (layoutValue == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TWO_TEXT ||
              layoutValue == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_TOP_TWO_TEXT) {
            data.put("Text1", new JSONString(""));
            data.put("Text2", new JSONString(""));
            data.put("Image", new JSONString("None"));
          }
          itemsCopy.add(data);
          table.setRowData(itemsCopy);
        }
      });

      /*
       * save the data for the corresponding layout type
       */
      save.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          items.clear();
          for (int i = 0; i < itemsCopy.size(); ++i) {
            JSONObject obj = itemsCopy.get(i);
            if ((layoutValue == ComponentConstants.LISTVIEW_LAYOUT_SINGLE_TEXT ||
                layoutValue == ComponentConstants.LISTVIEW_LAYOUT_IMAGE_SINGLE_TEXT) && obj.containsKey("Text2")) {
              obj.put("Text2", null);
            }
            if ((layoutValue == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT ||
                layoutValue == ComponentConstants.LISTVIEW_LAYOUT_TWO_TEXT_LINEAR) && obj.containsKey("Image")) {
              obj.put("Image", null);
            }
            rows.set(i, obj);
            items.add(i, obj);
          }
          property.setValue(rows.toString());
          ClickHandle.this.hide();
        }
      });

      /*
       * discards changes in the data for the corresponding layout type
       */
      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          final Cancel cancel = new Cancel();
          cancel.center();
          cancel.setStylePrimaryName("ode-DialogBox");
          cancel.show();
          cancel.yes.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
              itemsCopy.clear();
              cancel.hide();
              ClickHandle.this.hide();
            }
          });
          cancel.no.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
              cancel.hide();
            }
          });
        }
      });

      verticalPanel.add(table);
      verticalPanel.add(add);
      actionButtons.add(save);
      actionButtons.add(cancel);
      actionButtons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      verticalPanel.add(actionButtons);
      setWidget(verticalPanel);
    }
  }

  class Cancel extends DialogBox {
    VerticalPanel verticalPanel;
    HorizontalPanel buttonPanel;
    Button yes, no;

    Cancel(){
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
