// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.DOM;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Frame;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

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
        for (ComponentDatabaseInterface.PropertyDefinition pdef : cd.getProperties()) {
          CheckBox propcb = new CheckBox(pdef.getName());
          propcb.setName("blockProperties");
          subTree.addItem(propcb);
        }
        for (ComponentDatabaseInterface.EventDefinition edef : cd.getEvents()) {
          CheckBox eventcb = new CheckBox(edef.getName());
          eventcb.setName("events");
          subTree.addItem(eventcb);
        }
        for (ComponentDatabaseInterface.MethodDefinition mdef : cd.getMethods()) {
          CheckBox methcb = new CheckBox(mdef.getName());
          methcb.setName("methods");
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
    for (int i = 0; i < selectorTree.getItemCount(); ++i) {
      JSONArray jsonBlocks = new JSONArray();
      TreeItem catItem = selectorTree.getItem(i);
      CheckBox cbcat = (CheckBox)catItem.getWidget();
        for (int j = 0; j < catItem.getChildCount(); ++j) {
          TreeItem compItem = catItem.getChild(j);
          CheckBox cbcomp = (CheckBox)compItem.getWidget();
          if (cbcomp.getValue()) {
            JSONObject jsonSingleComp = new JSONObject();
            jsonSingleComp.put("type", new JSONString(cbcomp.getName()));
            jsonBlocks.set(j, jsonSingleComp);
            for (int k = 0; k < compItem.getChildCount(); ++k) {
              CheckBox cbprop = (CheckBox) compItem.getChild(k).getWidget();
              if (cbprop.getValue()) {

              }
            }
          }
        }
        jsonShownComponents.put(cbcat.getName().toUpperCase(), jsonBlocks);
    }
    jsonObj.put("shownComponentTypes", jsonShownComponents);
    property.setValue(jsonObj.toString());
    return true;
  }

  private native JSONObject convertToXMLObjects(String type, String component, CheckBox blockObj)/*-{
    var xmlObjList = [];
    switch(type) {
      case "events":
        var obj = {};
        obj["type"] = "component_event";
        var mutator = {};
        mutator["component_type"] = component;
        mutator["event_name"] = blockObj.name;
        obj["mutatorNameToValue"] = mutator;
        obj["fieldNameToValue"] = {};
        // params??
        xmlObjList.push(obj);
        break;
      case "blockProperties":
        var rw = blockObj["rw"];
        if (rw != "invisible") {
          var obj = {};
          obj["type"] = "component_set_get";
          var mutator = {};
          mutator["component_type"] = component;
          mutator["property_name"] = blockObj.name;
          var fields = {};
          fields["PROP"] = blockObj.name;
          obj["fieldNameToValue"] = fields;
          if (rw == "read-only") {
            mutator["set_or_get"] = "get";
            obj["mutatorNameToValue"] = mutator;
            xmlObjList.push(obj);
          } else if (rw == "write-only") {
            mutator["set_or_get"] = "set";
            obj["mutatorNameToValue"] = mutator;
            xmlObjList.push(obj);
          } else if (rw == "read-write") {
            var mutator_get = JSON.parse(JSON.stringify(mutator));
            var obj_get = JSON.parse(JSON.stringify(obj));
            mutator["set_or_get"] = "set";
            mutator_get["set_or_get"] = "get";
            obj["mutatorNameToValue"] = mutator;
            obj_get["mutatorNameToValue"] = mutator_get;
            xmlObjList.push(obj);
            xmlObjList.push(obj_get);
          }
        }
        break;
      case "methods":
        var obj = {};
        obj["type"] = "component_method";
        var mutator = {};
        mutator["component_type"] = component;
        mutator["method_name"] = blockObj.name;
        obj["mutatorNameToValue"] = mutator;
        obj["fieldNameToValue"] = {};
        // params??
        xmlObjList.push(obj);
        break;
    }
    return xmlObjList;
  }-*/;

    private native void generate()/*-{
    var jsonObj = {};
    var jsonCompObj = {};
    var jsonBlockObj = {};
    var jsonString = "";
    jsonBlockObj["ComponentBlocks"] = {};
    componentCategories.forEach(function(category) {
      jsonCompObj[category] = [];
    });
    blockCategories.forEach(function(category) {
      jsonBlockObj[category] = [];
    });
    componentBlockCategories.forEach(function(category) {
      jsonBlockObj["ComponentBlocks"][category] = [];
    });

    // Generates components
    $.getJSON("simple_components.json", function(components) {
      components.forEach(function(component) {
        //checkbox id = component.type;
        var checkedid = component.name;
        if (document.getElementById(checkedid)!= null && document.getElementById(checkedid).checked) {
          var obj = {type: checkedid};
          if (jsonCompObj[component.categoryString]) {
            jsonCompObj[component.categoryString].push(obj);
          } else {
            jsonCompObj[component.categoryString] = [obj];
          }
          component.events.forEach(function(prop) {
            // var blockobj = {name: prop.name};
            var blockPropId = checkedid + 'Blocks-' + prop.name;
            if (document.getElementById(blockPropId) != null && document.getElementById(blockPropId).checked) {
              //jsonBlockObj["ComponentBlocks"][checkedid].push(blockobj);
              var list = convertToXMLObjects("events", checkedid, prop);
              //console.log(list);
              for (var i = 0; i < list.length; i++) {
                var blockObj = list[i];
                jsonBlockObj["ComponentBlocks"][checkedid].push(blockObj);
              }
            }
          });
          component.methods.forEach(function(prop) {
            // var blockobj = {name: prop.name};
            var blockPropId = checkedid + 'Blocks-' + prop.name;
            if (document.getElementById(blockPropId) != null && document.getElementById(blockPropId).checked) {
              //jsonBlockObj["ComponentBlocks"][checkedid].push(blockobj);
              var list = convertToXMLObjects("methods", checkedid, prop);
              //console.log(list);
              for (var i = 0; i < list.length; i++) {
                var blockObj = list[i];
                jsonBlockObj["ComponentBlocks"][checkedid].push(blockObj);
              }
            }
          });

          //Get all the component blocks
          component.blockProperties.forEach(function(prop) {
            // var blockobj = {name: prop.name};
            var blockPropId = checkedid + 'Blocks-' + prop.name;
            if (document.getElementById(blockPropId) != null && document.getElementById(blockPropId).checked) {
              //jsonBlockObj["ComponentBlocks"][checkedid].push(blockobj);
              var list = convertToXMLObjects("blockProperties", checkedid, prop);
              //console.log(list);
              for (var i = 0; i < list.length; i++) {
                var blockObj = list[i];
                jsonBlockObj["ComponentBlocks"][checkedid].push(blockObj);
              }
            }

          });
        }
      });

      // Generates blocks
      $.getJSON("global_blocks.json", function(data) {
        for (key in data) {
          data[key].forEach(function(block) {
            //check if block.type or block.list is checked
            var checkedid = "";
            if (block.type != undefined) {
              checkedid = block.type;
            } else {
              checkedid = block.list;
            }
            if (document.getElementById(checkedid)!=null && document.getElementById(checkedid).checked) {
              jsonBlockObj[key].push(block);
            }
          });
        }

        jsonObj.shownComponentTypes = jsonCompObj;
        jsonObj.shownBlockTypes = jsonBlockObj;
        jsonString = JSON.stringify(jsonObj);
        $('#jsonStr').html(JSON.stringify(jsonObj));
      });
    });
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
