// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Frame;

import java.util.HashMap;

public class SubsetJSONPropertyEditor  extends AdditionalChoicePropertyEditor
        implements ProjectChangeListener {

  Frame subsetSelector;
  SimplePanel framePanel;
  Tree selectorTree;

  public SubsetJSONPropertyEditor() {
    super();
    VerticalPanel treePanel = new VerticalPanel();
    selectorTree = new Tree();
    SimpleComponentDatabase db = SimpleComponentDatabase.getInstance();
    HashMap<String, TreeItem> categoryItems = new HashMap<String, TreeItem>();
    for (ComponentCategory cat : ComponentCategory.values()) {
      CheckBox cb = new CheckBox(cat.getName());
      cb.setName(cat.getDocName());
      categoryItems.put(cat.getDocName(), new TreeItem(cb));
    }
    for (String cname : db.getComponentNames()) {

      ComponentDatabaseInterface.ComponentDefinition cd = db.getComponentDefinition(cname);
      if (cd.getCategoryDocUrlString() != "internal" || cd.getCategoryDocUrlString() != "") {

        CheckBox subcb = new CheckBox(cname);
        subcb.setName(cname);
        TreeItem subTree = new TreeItem(subcb);
        for (ComponentDatabaseInterface.BlockPropertyDefinition pdef : cd.getBlockProperties()) {
          CheckBox propcb = new CheckBox(pdef.getName());
          propcb.setName("blockProperties");
          propcb.setFormValue(pdef.getRW());
          subTree.addItem(propcb);
        }
        for (ComponentDatabaseInterface.EventDefinition edef : cd.getEvents()) {
          CheckBox eventcb = new CheckBox(edef.getName());
          eventcb.setName("events");
          eventcb.setFormValue("none");
          subTree.addItem(eventcb);
        }
        for (ComponentDatabaseInterface.MethodDefinition mdef : cd.getMethods()) {
          CheckBox methcb = new CheckBox(mdef.getName());
          methcb.setName("methods");
          methcb.setFormValue("none");
          subTree.addItem(methcb);
        }
        TreeItem t = categoryItems.get(cd.getCategoryDocUrlString());
        t.addItem(subTree);
      }
    }
    for (ComponentCategory cat : ComponentCategory.values()) {
      TreeItem t = categoryItems.get(cat.getDocName());
      if (t.getChildCount() > 0)
        selectorTree.addItem(t);
    }

    treePanel.add(selectorTree);
    initAdditionalChoicePanel(treePanel);

//    subsetSelector = new Frame();
//    framePanel = new SimplePanel();
//
//    subsetSelector.setUrl("JSONGenerator/index.html");
//    subsetSelector.setWidth("100%");
//    subsetSelector.setHeight("100%");
//    framePanel.setWidth("100%");
//    framePanel.setHeight("100%");
//    framePanel.add(subsetSelector);
//    initAdditionalChoicePanel(framePanel);
  }

  // AdditionalChoicePropertyEditor implementation
  @Override
  protected boolean okAction() {
    JSONObject jsonObj = new JSONObject();
    JSONObject jsonShownComponents = new JSONObject();
    JSONObject jsonShownBlockTypes = new JSONObject();
    JSONObject jsonComponents = new JSONObject();
    for (int i = 0; i < selectorTree.getItemCount(); ++i) {
      JSONArray jsonBlocks = new JSONArray();
      TreeItem catItem = selectorTree.getItem(i);
      CheckBox cbcat = (CheckBox)catItem.getWidget();
        for (int j = 0; j < catItem.getChildCount(); ++j) {
          TreeItem compItem = catItem.getChild(j);
          CheckBox cbcomp = (CheckBox)compItem.getWidget();
          if (cbcomp.getValue()) {
            JSONArray jsonComponentBlocks = new JSONArray();
            JSONObject jsonSingleComp = new JSONObject();
            jsonSingleComp.put("type", new JSONString(cbcomp.getName()));
            jsonBlocks.set(j, jsonSingleComp);
            for (int k = 0; k < compItem.getChildCount(); ++k) {
              CheckBox cbprop = (CheckBox) compItem.getChild(k).getWidget();
              int blockCount = jsonComponentBlocks.size();
              if (cbprop.getValue()) {
                JsArray<JavaScriptObject> jsonConvert = convertToJSONObjects(cbprop.getName(), cbcomp.getText(), cbprop.getText(), cbprop.getFormValue());
                for(int l = 0; l < jsonConvert.length(); ++l) {
                  JSONObject fred = new JSONObject(jsonConvert.get(l));
                  jsonComponentBlocks.set(blockCount++, fred);
                }
              }
            }
            jsonComponents.put(cbcomp.getText(), jsonComponentBlocks);
          }
        }
        jsonShownComponents.put(cbcat.getName().toUpperCase(), jsonBlocks);
        jsonShownBlockTypes.put("ComponentBlocks", jsonComponents);
    }
    jsonObj.put("shownComponentTypes", jsonShownComponents);
    jsonObj.put("shownBlockTypes", jsonShownBlockTypes);
    property.setValue(jsonObj.toString());
    return true;
  }

  private native JsArray<JavaScriptObject> convertToJSONObjects(String type, String component, String blockName, String rw)/*-{
    var jsonObjList = [];
    switch(type) {
      case "events":
        var obj = {};
        obj["type"] = "component_event";
        var mutator = {};
        mutator["component_type"] = component;
        mutator["event_name"] = blockName;
        obj["mutatorNameToValue"] = mutator;
        obj["fieldNameToValue"] = {};
        // params??
        jsonObjList[0] = obj;
        break;
      case "blockProperties":
        var rw = "read-write";
        if (rw != "invisible") {
          var obj = {};
          obj["type"] = "component_set_get";
          var mutator = {};
          mutator["component_type"] = component;
          mutator["property_name"] = blockName;
          var fields = {};
          fields["PROP"] = blockName;
          obj["fieldNameToValue"] = fields;
          if (rw == "read-only") {
            mutator["set_or_get"] = "get";
            obj["mutatorNameToValue"] = mutator;
            jsonObjList[0] = obj;
          } else if (rw == "write-only") {
            mutator["set_or_get"] = "set";
            obj["mutatorNameToValue"] = mutator;
            jsonObjList[0] = obj;
          } else if (rw == "read-write") {
            var mutator_get = JSON.parse(JSON.stringify(mutator));
            var obj_get = JSON.parse(JSON.stringify(obj));
            mutator["set_or_get"] = "set";
            mutator_get["set_or_get"] = "get";
            obj["mutatorNameToValue"] = mutator;
            obj_get["mutatorNameToValue"] = mutator_get;
            jsonObjList[0] = obj;
            jsonObjList[1] = obj_get;
          }
        }
        break;
      case "methods":
        var obj = {};
        obj["type"] = "component_method";
        var mutator = {};
        mutator["component_type"] = component;
        mutator["method_name"] = blockName;
        obj["mutatorNameToValue"] = mutator;
        obj["fieldNameToValue"] = {};
        // params??
        jsonObjList[0] = obj;
        break;
    }
    return jsonObjList;
  }-*/;

    private native String getJSON(Document d)/*-{
    var jsonDiv = d.getElementById("jsonStr");
    return jsonDiv.innerText;
  }-*/;

  private native void setJSON(Document d, String s)/*-{
    var jsonDiv = d.getElementById("jsonStr");
    jsonDiv.innerText = s;
  }-*/;

  // ProjectChangeListener implementation
  @Override
  public void onProjectLoaded(Project project) {
  }

  public void onProjectNodeAdded(Project project, ProjectNode node) {
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
  }
}
