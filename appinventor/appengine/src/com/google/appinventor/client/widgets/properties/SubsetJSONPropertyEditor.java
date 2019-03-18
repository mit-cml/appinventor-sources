// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.HashMap;

public class SubsetJSONPropertyEditor  extends AdditionalChoicePropertyEditor
        implements ProjectChangeListener {

  Tree componentTree;
  Tree blockTree;

  public SubsetJSONPropertyEditor() {
    super();
    HorizontalPanel treePanel = new HorizontalPanel();
    VerticalPanel componentPanel = new VerticalPanel();
    final ScrollPanel componentScroll = new ScrollPanel();
    VerticalPanel blockPanel = new VerticalPanel();
    ScrollPanel blockScroll = new ScrollPanel();
    componentTree = new Tree();
    blockTree = new Tree();

    // Build tree of components and their related property/event/method blocks
    SimpleComponentDatabase db = SimpleComponentDatabase.getInstance();
    HashMap<String, TreeItem> categoryItems = new HashMap<String, TreeItem>();
    for (ComponentCategory cat : ComponentCategory.values()) {
      CheckBox cb = new CheckBox(ComponentsTranslation.getCategoryName(cat.getName()));
      cb.setName(cat.getDocName());
      categoryItems.put(cat.getDocName(), createCascadeCheckboxItem(cb));
    }
    for (String cname : db.getComponentNames()) {

      ComponentDatabaseInterface.ComponentDefinition cd = db.getComponentDefinition(cname);
      if (cd.getCategoryDocUrlString() != "internal" && cd.getCategoryDocUrlString() != "") {

        final CheckBox subcb = new CheckBox(ComponentsTranslation.getComponentName(cname));
        final TreeItem subTree = createCascadeCheckboxItem(subcb);
        subcb.setName(cname);
        for (ComponentDatabaseInterface.BlockPropertyDefinition pdef : cd.getBlockProperties()) {
          if (pdef.getRW() != "invisible") {
            CheckBox propcb = new CheckBox(ComponentsTranslation.getPropertyName(pdef.getName()));
            propcb.setName("blockProperties");
            propcb.setFormValue(pdef.getRW());
            subTree.addItem(createCascadeCheckboxItem(propcb));
          }
        }
        for (ComponentDatabaseInterface.EventDefinition edef : cd.getEvents()) {
          CheckBox eventcb = new CheckBox(ComponentsTranslation.getEventName(edef.getName()));
          eventcb.setName("events");
          eventcb.setFormValue("none");
          subTree.addItem(createCascadeCheckboxItem(eventcb));
        }
        for (ComponentDatabaseInterface.MethodDefinition mdef : cd.getMethods()) {
          CheckBox methcb = new CheckBox(ComponentsTranslation.getMethodName(mdef.getName()));
          methcb.setName("methods");
          methcb.setFormValue("none");
          subTree.addItem(createCascadeCheckboxItem(methcb));
        }
        TreeItem t = categoryItems.get(cd.getCategoryDocUrlString());
        t.addItem(subTree);
      }
    }
    for (ComponentCategory cat : ComponentCategory.values()) {
      TreeItem t = categoryItems.get(cat.getDocName());
      if (t.getChildCount() > 0)
        componentTree.addItem(t);
    }

    // Build tree of global blocks by category
    JavaScriptObject barney = getBlockDict();
    JSONObject blockDict = new JSONObject(barney);
    for (String blockCategory:blockDict.keySet()) {

      // There appears to be no centralized method for internationalizing the built-in block category names.
      // Fix if I'm wrong.
      String blockCategoryTranslated;
      if (blockCategory.equals("Control")) {
        blockCategoryTranslated = MESSAGES.builtinControlLabel();
      } else if (blockCategory.equals("Logic")) {
        blockCategoryTranslated = MESSAGES.builtinLogicLabel();
      } else if (blockCategory.equals("Math")) {
        blockCategoryTranslated = MESSAGES.builtinMathLabel();
      } else if (blockCategory.equals("Text")) {
        blockCategoryTranslated = MESSAGES.builtinTextLabel();
      } else if (blockCategory.equals("Lists")) {
        blockCategoryTranslated = MESSAGES.builtinListsLabel();
      } else if (blockCategory.equals("Colors")) {
        blockCategoryTranslated = MESSAGES.builtinColorsLabel();
      } else if (blockCategory.equals("Variables")) {
        blockCategoryTranslated = MESSAGES.builtinVariablesLabel();
      } else if (blockCategory.equals("Procedures")) {
        blockCategoryTranslated = MESSAGES.builtinProceduresLabel();
      } else {
        blockCategoryTranslated = blockCategory;
      }

      CheckBox blockCatCb = new CheckBox(blockCategoryTranslated);
      blockCatCb.setName(blockCategory);
      TreeItem blockCatItem = createCascadeCheckboxItem(blockCatCb);
      JSONValue blockCatDictVal = blockDict.get(blockCategory);
      JSONObject blockCatDict = blockCatDictVal.isObject();
      for (String blockID:blockCatDict.keySet()) {
        CheckBox blockCb = new CheckBox(blockCatDict.get(blockID).isString().stringValue());
        blockCb.setName(blockID);
        blockCatItem.addItem(createCascadeCheckboxItem(blockCb));
      }
      blockTree.addItem(blockCatItem);
    }

    componentPanel.add(new Label(MESSAGES.sourceStructureBoxCaption()));
    componentScroll.add(componentTree);
    componentPanel.add(componentScroll);
    blockPanel.add(new Label(MESSAGES.builtinBlocksLabel()));
    blockScroll.add(blockTree);
    blockPanel.add(blockScroll);
    treePanel.add(componentPanel);
    treePanel.add(blockPanel);
    initAdditionalChoicePanel(treePanel);
  }

  private void loadComponents(JSONObject jsonObj) {
    // TODO: Review JSON format. There has to be a better way to store and retrieve this info.
    JSONObject shownComponents = jsonObj.get("shownComponentTypes").isObject();
    JSONObject shownComponentBlocks = jsonObj.get("shownBlockTypes").isObject().get("ComponentBlocks").isObject();
    for (int i = 0; i < componentTree.getItemCount(); ++i) {
      TreeItem componentCatItem = componentTree.getItem(i);
      CheckBox componentCatCb = (CheckBox)componentCatItem.getWidget();
      String fred = componentCatCb.getName().toUpperCase();
      JSONArray jsonComponentCat = shownComponents.get(fred).isArray();
      if (jsonComponentCat.size() > 0) {
        componentCatCb.setValue(true, false);
        HashMap<String, String> jsonComponentHash = new HashMap<String, String>();
        for (int j = 0; j < jsonComponentCat.size(); ++j) {
          JSONValue jsonComponentHashCat = jsonComponentCat.get(j);
          if (jsonComponentHashCat != null) {
            jsonComponentHash.put(jsonComponentHashCat.isObject().get("type").isString().stringValue(), "type");
          }
        }
        for (int j = 0; j < componentCatItem.getChildCount(); ++j) {
          TreeItem componentItem = componentCatItem.getChild(j);
          CheckBox componentCb = (CheckBox)componentItem.getWidget();
          if (jsonComponentHash.get(componentCb.getName()) != null) {
            componentCb.setValue(true, false);
            JSONArray jsonComponentBlockProps = shownComponentBlocks.get(componentCb.getName()).isArray();
            HashMap<String, String> componentPropHash = new HashMap<String, String>();
            for (int k = 0; k < jsonComponentBlockProps.size(); ++k) {
              JSONObject jsonComponentBlockType = jsonComponentBlockProps.get(k).isObject();
              String componentBlockType = jsonComponentBlockType.get("type").isString().stringValue();
              if (componentBlockType == "component_set_get") {
                componentPropHash.put(jsonComponentBlockType.get("mutatorNameToValue").isObject().get("property_name").isString().stringValue(), "PROP");
              } else if (componentBlockType == "component_event") {
                JSONValue barney = jsonComponentBlockType.get("mutatorNameToValue");
                JSONValue pebbles = barney.isObject().get("event_name");
                componentPropHash.put(pebbles.isString().stringValue(), "EVENT");
              } else if (componentBlockType == "component_method") {
                componentPropHash.put(jsonComponentBlockType.get("mutatorNameToValue").isObject().get("method_name").isString().stringValue(), "METHOD");
              }
            }
            for (int k = 0; k < componentItem.getChildCount(); ++k) {
              TreeItem componentPropItem = componentItem.getChild(k);
              CheckBox componentPropCb = (CheckBox)componentPropItem.getWidget();
              if (componentPropHash.get(componentPropCb.getText()) != null) {
                componentPropCb.setValue(true, false);
              } else {
                componentPropCb.setValue(false, false);
              }
            }

          } else {
            componentCb.setValue(false, false);
            toggleChildren(componentItem, false);
          }
        }
      } else {
        componentCatCb.setValue(false, false);
        toggleChildren(componentCatItem, false);
      }
    }
  }

  private void loadGlobalBlocks(JSONObject jsonObj) {
    JSONObject shownBlocks = jsonObj.get("shownBlockTypes").isObject();
    for (int i = 0; i < blockTree.getItemCount(); ++i) {
      TreeItem catTree = blockTree.getItem(i);
      CheckBox catCb = (CheckBox)catTree.getWidget();
      if (shownBlocks.get(catCb.getName()) != null) {
        JSONArray jsonBlockArr = shownBlocks.get(catCb.getName()).isArray();
        catCb.setValue(true,false);
        HashMap<String, String> blockHash = new HashMap<String, String>();
        for (int j = 0; j< jsonBlockArr.size(); ++j) {
          blockHash.put(jsonBlockArr.get(j).isObject().get("type").isString().stringValue(), "type");
        }
        for (int j = 0; j < catTree.getChildCount(); ++j) {
          TreeItem blockTree = catTree.getChild(j);
          CheckBox blockCb = (CheckBox)blockTree.getWidget();
          if (blockHash.get(blockCb.getName()) != null) {
            blockCb.setValue(true,false);
          } else {
            blockCb.setValue(false,false);
          }
        }
      } else {
        catCb.setValue(false, false);
        toggleChildren(catTree, false);
      }
    }
  }


  @Override
  protected void openAdditionalChoiceDialog() {
    JSONObject jsonSet = JSONParser.parseStrict(property.getValue()).isObject();
    loadComponents(jsonSet);
    loadGlobalBlocks(jsonSet);
    popup.setTitle(MESSAGES.blockSelectorBoxCaption());
    popup.setWidth(300 + "px");
    popup.setHeight(500 + "px");
    popup.show();
    popup.center();

  }

  private void toggleChildren(TreeItem item, Boolean checked) {
    for (int i = 0; i <item.getChildCount(); ++i) {
      TreeItem childItem = item.getChild(i);
      ((CheckBox)childItem.getWidget()).setValue(checked, false);
      if (childItem.getChildCount() > 0) {
        toggleChildren(childItem, checked);
      }
    }
  }

  private TreeItem createCascadeCheckboxItem(CheckBox cb) {
    final TreeItem newItem = new TreeItem();
    cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
        if (newItem.getChildCount() > 0) {
          toggleChildren(newItem, valueChangeEvent.getValue());
        }
        if (valueChangeEvent.getValue() == true) {
          TreeItem parentItem = newItem.getParentItem();
          while (parentItem != null) {
            ((CheckBox)parentItem.getWidget()).setValue(true, false);
            parentItem = parentItem.getParentItem();
          }
        }
      }
    });
    newItem.setWidget(cb);
    return newItem;
  }

  // AdditionalChoicePropertyEditor implementation
  @Override
  protected boolean okAction() {
    JSONObject jsonObj = new JSONObject();
    JSONObject jsonShownComponents = new JSONObject();
    JSONObject jsonShownBlockTypes = new JSONObject();
    JSONObject jsonComponents = new JSONObject();
    for (int i = 0; i < componentTree.getItemCount(); ++i) {
      JSONArray jsonBlocks = new JSONArray();
      TreeItem catItem = componentTree.getItem(i);
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
                  JSONObject jsonCompBlockObj = new JSONObject(jsonConvert.get(l));
                  jsonComponentBlocks.set(blockCount++, jsonCompBlockObj);
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

    // Add Blocks for the blocks editor that are not associated with components
    for (int i = 0; i < blockTree.getItemCount(); ++i){
      TreeItem blockCatItem = blockTree.getItem(i);
      CheckBox cbcat = (CheckBox)blockCatItem.getWidget();
      if (cbcat.getValue()) {
        JSONArray jsonBlockCat = new JSONArray();
        for (int j = 0; j < blockCatItem.getChildCount(); ++j) {
          TreeItem blockItem = blockCatItem.getChild(j);
          CheckBox blockCb = (CheckBox) blockItem.getWidget();
          if (blockCb.getValue()) {
            JSONObject jsonSingleBlock = new JSONObject();
            jsonSingleBlock.put("type", new JSONString(blockCb.getName()));
            jsonBlockCat.set(jsonBlockCat.size(), jsonSingleBlock);
            jsonShownBlockTypes.put(cbcat.getText(), jsonBlockCat);
          }
        }
      }
    }
    jsonObj.put("shownBlockTypes", jsonShownBlockTypes);

    property.setValue(jsonObj.toString());
    return true;
  }

  private native JavaScriptObject getBlockDict()/*-{
    var blockCatDict = {};
    for (var blockName in Blockly.Blocks) {
      if (!Blockly.Blocks.hasOwnProperty(blockName)) continue;
      var block = Blockly.Blocks[blockName];
      // Component blocks are handled in the component tree and don't behave the same as the others
      if (block.category && (block.category !== "Component") && (typeof block.typeblock !== 'undefined')) {
        if (!blockCatDict[block.category]) {
          blockCatDict[block.category] = {};
        }
        blockCatDict[block.category][blockName] = (block.typeblock[0]).translatedName;
      }
    }
    return blockCatDict;
  }-*/;

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
