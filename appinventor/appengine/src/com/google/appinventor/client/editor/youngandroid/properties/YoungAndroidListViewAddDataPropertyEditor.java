// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import com.google.appinventor.client.widgets.properties.PropertyEditor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import com.google.gwt.user.client.ui.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * AddData property in the ListView component. It is used to add/delete data
 * for different layout types of ListView.
 *
 * <p>The button opens {@link ListViewAddDataDialog}, which holds the editing grid. This editor
 * keeps the data layer (the JSON {@code <-> List<JSONObject>} load/save) and hands the dialog the
 * items to edit and a callback to persist them.
 */

public class YoungAndroidListViewAddDataPropertyEditor extends PropertyEditor {

  private Button addData;

  private int layout;
  private List<JSONObject> items;

  private final SimpleEditor editor;

  public YoungAndroidListViewAddDataPropertyEditor(final SimpleEditor editor) {
    items = new ArrayList<JSONObject>();
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
        new ListViewAddDataDialog(YoungAndroidListViewAddDataPropertyEditor.this, layout).show();
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

  /** Returns the editable items list shared with the open dialog. */
  List<JSONObject> getItems() {
    return items;
  }

  /** Returns the editor that owns this property, used to resolve project assets. */
  SimpleEditor getSimpleEditor() {
    return editor;
  }

  /** Persists the edited rows (a JSON array string) back to the ListData property. */
  void commitListData(String value) {
    property.setValue(value);
  }
}
