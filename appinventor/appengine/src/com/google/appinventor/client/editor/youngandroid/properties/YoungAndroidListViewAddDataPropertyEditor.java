// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.PropertyEditor;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
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

public class YoungAndroidListViewAddDataPropertyEditor extends PropertyEditor {

  private Button addData;

  private String layout;
  private ArrayList<JSONObject> ITEMS;
  private ArrayList<JSONObject> ITEMSCopy;

  public YoungAndroidListViewAddDataPropertyEditor() {
    ITEMS = new ArrayList<>();
    ITEMSCopy = new ArrayList<>();
    addData = new Button("Click to Add/Delete Data");

    createButton();
  }

  @Override
  protected void updateValue() {
    String value = property.getValue();
    JSONValue jsonValue = value.isEmpty() ? null : JSONParser.parseStrict(value);
    if(jsonValue != null) {
      JSONArray array = jsonValue.isArray();
      ITEMS.clear();
      for(int i = 0; i < array.size(); ++i) {
        ITEMS.add(i, array.get(i).isObject());
      }
    }
  }

  private void createButton() {
    addData.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        ClickHandle ch = new ClickHandle(layout);
        ch.center();
        ch.show();
      }
    });
    initWidget(addData);
  }

  public void setLayout(String layout) {
    this.layout = layout;
  }

  class ClickHandle extends DialogBox {
    VerticalPanel verticalPanel;
    HorizontalPanel actionButtons;
    Button add, ok, cancel;
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
          ITEMSCopy.remove(i);
          model.refresh();
          table.setRowCount(0);
          table.setRowData(0, ITEMSCopy);
        }
      });
      return column;
    }

    Column<JSONObject, String> createTextBoxes(final String columnValue) {
      Column<JSONObject, String> column = new Column<JSONObject, String>(new TextInputCell()) {
        @Override
        public String getValue(JSONObject jsonObject) {
          if(jsonObject.containsKey(columnValue)) {
            JSONString stringVal = (JSONString)jsonObject.get(columnValue);
            return (stringVal.stringValue());
          } else {
            jsonObject.put(columnValue, new JSONString(""));
            return "";
          }
        }
      };
      column.setFieldUpdater(new FieldUpdater<JSONObject, String>() {
        @Override
        public void update(int i, JSONObject jsonObject, String s) {
          jsonObject.put(columnValue, new JSONString(s));
        }
      });
      return column;
    }

    ClickHandle(final String layoutValue) {

      verticalPanel = new VerticalPanel();
      actionButtons = new HorizontalPanel();
      ITEMSCopy = new ArrayList<>();
      model = new ListDataProvider(ITEMSCopy);
      table = new CellTable<>();
      rows = new JSONArray();
      add  = new Button("Click to Add Row Data");
      ok = new Button("OK");
      cancel = new Button("CANCEL");

      setText("Add Data to the ListView");

      for(int i = 0; i < ITEMS.size(); ++i) {
        ITEMSCopy.add(i, ITEMS.get(i));
      }

      table.setRowData(ITEMSCopy);
      table.setEmptyTableWidget(new Label("No row data available yet!"));
      model.addDataDisplay(table);

      if (layoutValue.equals("0")) {
        table.addColumn(createTextBoxes("Text1"), "Text1");
      } else if (layoutValue.equals("1")) {
        table.addColumn(createTextBoxes("Text1"), "Text1");
        table.addColumn(createTextBoxes("Text2"), "Text2");
      }

      table.addColumn(createDeleteButton());

      add.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          JSONObject data = new JSONObject();
          if(layoutValue.equals("0")) {
            data.put("Text1", new JSONString(""));
          } else if(layoutValue.equals("1")) {
            data.put("Text1", new JSONString(""));
            data.put("Text2", new JSONString(""));
          }
          ITEMSCopy.add(data);
          table.setRowData(ITEMSCopy);
        }
      });

      ok.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          ITEMS.clear();
          for(int i = 0; i < ITEMSCopy.size(); ++i) {
            JSONObject obj = ITEMSCopy.get(i);
            if(layoutValue.equals("0") && obj.containsKey("Text2")) {
              obj.put("Text2", null);
            }
            rows.set(i, obj);
            ITEMS.add(i, obj);
          }
          property.setValue(rows.toString());
          ClickHandle.this.hide();
        }
      });

      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          ITEMSCopy.clear();
          ClickHandle.this.hide();
        }
      });

      verticalPanel.add(table);
      verticalPanel.add(add);
      actionButtons.add(ok);
      actionButtons.add(cancel);
      actionButtons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      verticalPanel.add(actionButtons);
      setWidget(verticalPanel);
    }
  }
}
