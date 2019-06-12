// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SubsetJSONPropertyEditor  extends PropertyEditor
        implements ProjectChangeListener {

  private static SubsetJSONPropertyEditor INSTANCE;
  Tree componentTree;
  Tree blockTree;
  DropDownButton dropDownButton;
  final FileUpload file = new FileUpload();
  final PopupPanel customPopup = new PopupPanel();
  boolean customPopupShowing = false;

  public SubsetJSONPropertyEditor() {
    buildTrees();
    file.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        if (customPopupShowing) {
          loadJSONfile(file, false);
        }
        else {
          loadJSONfile(file, true);
        }
      }
    });

    // This is an invisible panel holding a FileUpload button. It exists because we want to access
    // the file selection dialog from the subset editor dropdown menu item. There may be a better way
    // to do this.
    PopupPanel invisibleFilePanel = new PopupPanel();
    invisibleFilePanel.add(file);
    invisibleFilePanel.setVisible(false);
    invisibleFilePanel.show();

    List<DropDownButton.DropDownItem> items = Lists.newArrayList();
    items.add(new DropDownButton.DropDownItem("Subset Property Editor", "All", new Command() {
      @Override
      public void execute() {
        property.setValue("");
        updateValue();
      }}));
    items.add(new DropDownButton.DropDownItem("Subset Property Editor", "Match Project", new Command() {
      @Override
      public void execute() {
        matchProject();
        property.setValue(createJSONString());
        updateValue();
      }}));
    items.add(new DropDownButton.DropDownItem("Subset Property Editor", MESSAGES.fileUploadWizardCaption(), new Command() {
      @Override
      public void execute() {
        file.click();
      }}));

    items.add(new DropDownButton.DropDownItem("Subset Property Editor", "View and Modify", new Command() {
      @Override
      public void execute() {
        showCustomSubsetPanel();
      }}));
    dropDownButton = new DropDownButton("Subset Property Editor", "", items, false);
    dropDownButton.setStylePrimaryName("ode-ChoicePropertyEditor");
    initWidget(dropDownButton);
    INSTANCE = this;
    exportJavaMethods();
  }

  protected void showCustomSubsetPanel() {
    if (property.getValue() != "") {
      JSONObject jsonSet = JSONParser.parseStrict(property.getValue()).isObject();
      loadComponents(jsonSet);
      loadGlobalBlocks(jsonSet);
    } else {
      clearSelections();
    }

    if (customPopup.getTitle() != MESSAGES.blocksToolkitTitle()) {
      final DockLayoutPanel treePanel = new DockLayoutPanel(Style.Unit.PCT);
      VerticalPanel componentPanel = new VerticalPanel();
      VerticalPanel blockPanel = new VerticalPanel();
      HorizontalPanel buttonPanel = new HorizontalPanel();
      final ScrollPanel componentScroll = new ScrollPanel(componentPanel);
      ScrollPanel blockScroll = new ScrollPanel(blockPanel);

      componentPanel.add(new Label(MESSAGES.sourceStructureBoxCaption()));
      componentPanel.add(componentTree);
      blockPanel.add(new Label(MESSAGES.builtinBlocksLabel()));
      blockPanel.add(blockTree);

      Button loadButton = new Button(MESSAGES.fileUploadWizardCaption()); // TODO: Need to internationalize
      loadButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          file.click();
        }
      });
      Button saveButton = new Button(MESSAGES.saveAsButton());
      saveButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          saveFile();
        }
      });
      Button clearButton = new Button(MESSAGES.clearButton());
      Button initializeButton = new Button("Match Project"); // TODO: Internationalize
      clearButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          clearSelections();
        }
      });
      initializeButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          matchProject();
        }
      });
      Button cancelButton = new Button(MESSAGES.cancelButton());
      Button okButton = new Button(MESSAGES.okButton());
      buttonPanel.add(saveButton);
      buttonPanel.add(loadButton);
      buttonPanel.add(clearButton);
      buttonPanel.add(initializeButton);
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          customPopup.hide();
        }
      });
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          property.setValue(createJSONString());
          updateValue();
          customPopupShowing = false;
          customPopup.hide();
        }
      });
      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);
      Label customTitle = new Label(MESSAGES.blocksToolkitTitle());
      treePanel.addNorth(customTitle, 5);
      treePanel.addSouth(buttonPanel, 5);
      treePanel.addWest(componentScroll, 50);
      treePanel.addEast(blockScroll, 50);
      customPopup.setTitle(MESSAGES.blocksToolkitTitle());
      customPopup.add(treePanel);
      customPopup.setHeight("600px");
      customPopup.setWidth("600px");
      customPopup.center();
    }
    customPopupShowing = true;
    customPopup.show();
  }

  private void buildTrees() {
    componentTree = new Tree();
    blockTree = new Tree();
    // Build tree of components and their related property/event/method blocks
    SimpleComponentDatabase db = SimpleComponentDatabase.getInstance();
    HashMap<String, TreeItem> categoryItems = new HashMap<String, TreeItem>();
    for (ComponentCategory cat : ComponentCategory.values()) {
      if (cat != ComponentCategory.INTERNAL && cat != ComponentCategory.UNINITIALIZED) {
        CheckBox cb = new CheckBox(ComponentsTranslation.getCategoryName(cat.getName()));
        cb.setName(cat.getDocName());
        categoryItems.put(cat.getDocName(), createCascadeCheckboxItem(cb));
      }
    }
    for (String cname : db.getComponentNames()) {
      ComponentDatabaseInterface.ComponentDefinition cd = db.getComponentDefinition(cname);
      if (categoryItems.containsKey(cd.getCategoryDocUrlString()) && db.getShowOnPalette(cname) ) {
        final CheckBox subcb = new CheckBox(ComponentsTranslation.getComponentName(cname));
        final TreeItem subTree = createCascadeCheckboxItem(subcb);
        subcb.setName(cname);
        // Event, Method, Property order needs to match Blockly.Drawer.prototype.instanceRecordToXMLArray
        // so that component blocks are displayed in the same order with or without a subset defined.
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
        for (ComponentDatabaseInterface.BlockPropertyDefinition pdef : cd.getBlockProperties()) {
          if (pdef.getRW() != "invisible") {
            CheckBox propcb = new CheckBox(ComponentsTranslation.getPropertyName(pdef.getName()));
            propcb.setName("blockProperties");
            propcb.setFormValue(pdef.getRW());
            subTree.addItem(createCascadeCheckboxItem(propcb));
          }
        }
        TreeItem t = categoryItems.get(cd.getCategoryDocUrlString());
        t.addItem(subTree);
      }
    }
    for (TreeItem t : categoryItems.values()) {
      if (t.getChildCount() > 0) {
        componentTree.addItem(t);
      }
    }

    // Build tree of global blocks by category
    JSONObject blockDict = new JSONObject(getBlockDict());
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
        blockCb.setWordWrap(true);
        blockCb.setName(blockID);
        blockCatItem.addItem(createCascadeCheckboxItem(blockCb));
      }
      blockTree.addItem(blockCatItem);
    }
  }

  private void loadComponents(JSONObject jsonObj) {
    // TODO: Review JSON format. There has to be a better way to store and retrieve this info.
    JSONObject shownComponents = jsonObj.get("shownComponentTypes").isObject();
    JSONObject shownComponentBlocks = jsonObj.get("shownBlockTypes").isObject().get("ComponentBlocks").isObject();
    for (int i = 0; i < componentTree.getItemCount(); ++i) {
      TreeItem componentCatItem = componentTree.getItem(i);
      CheckBox componentCatCb = (CheckBox)componentCatItem.getWidget();
      String catName = componentCatCb.getName().toUpperCase();
      if (shownComponents.containsKey(catName)) {
        JSONArray jsonComponentCat = shownComponents.get(catName).isArray();
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
            CheckBox componentCb = (CheckBox) componentItem.getWidget();
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
                  JSONValue mutatorValue = jsonComponentBlockType.get("mutatorNameToValue");
                  JSONValue event_name = mutatorValue.isObject().get("event_name");
                  componentPropHash.put(event_name.isString().stringValue(), "EVENT");
                } else if (componentBlockType == "component_method") {
                  componentPropHash.put(jsonComponentBlockType.get("mutatorNameToValue").isObject().get("method_name").isString().stringValue(), "METHOD");
                }
              }
              for (int k = 0; k < componentItem.getChildCount(); ++k) {
                TreeItem componentPropItem = componentItem.getChild(k);
                CheckBox componentPropCb = (CheckBox) componentPropItem.getWidget();
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
      if (shownBlocks.containsKey(catCb.getName())) {
        JSONArray jsonBlockArr = shownBlocks.get(catCb.getName()).isArray();
        if (jsonBlockArr.size() > 0) {
          catCb.setValue(true, false);
          HashMap<String, String> blockHash = new HashMap<String, String>();
          for (int j = 0; j < jsonBlockArr.size(); ++j) {
            blockHash.put(jsonBlockArr.get(j).isObject().get("type").isString().stringValue(), "type");
          }
          for (int j = 0; j < catTree.getChildCount(); ++j) {
            TreeItem blockTree = catTree.getChild(j);
            CheckBox blockCb = (CheckBox) blockTree.getWidget();
            if (blockHash.get(blockCb.getName()) != null) {
              blockCb.setValue(true, false);
            } else {
              blockCb.setValue(false, false);
            }
          }
        }
      } else {
        catCb.setValue(false, false);
        toggleChildren(catTree, false);
      }
    }
  }

  private void clearSelections() {
    for (int i = 0; i < componentTree.getItemCount(); ++i) {
      TreeItem checkboxItem = componentTree.getItem(i);
      ((CheckBox)checkboxItem.getWidget()).setValue(false, false);
      toggleChildren(checkboxItem, false);
    }
    for (int i = 0; i < blockTree.getItemCount(); ++i) {
      TreeItem checkboxItem = blockTree.getItem(i);
      ((CheckBox)checkboxItem.getWidget()).setValue(false, false);
      toggleChildren(checkboxItem, false);
    }
  }

  private void matchProject() {
    long projID = Ode.getInstance().getCurrentYoungAndroidProjectId();
    YaProjectEditor projEditor = (YaProjectEditor)Ode.getInstance().getEditorManager().getOpenProjectEditor(projID);
    Set<String> componentTypes = projEditor.getUniqueComponentTypes();
    for (int i = 0; i < componentTree.getItemCount(); ++i) {
      TreeItem catItem = componentTree.getItem(i);
      CheckBox catCb = (CheckBox)catItem.getWidget();
      catCb.setValue(false,false);
      for (int j = 0; j < catItem.getChildCount(); ++j) {
        TreeItem compItem = catItem.getChild(j);
        CheckBox compCb = (CheckBox)compItem.getWidget();
        if (componentTypes.contains(compCb.getName())) {
          compCb.setValue(true,true);
          catCb.setValue(true, false);
        } else {
          compCb.setValue(false, true);
        }
      }
    }
    Set<String> blockTypes = projEditor.getUniqueBuiltInBlockTypes();
    for (int i = 0; i < blockTree.getItemCount(); ++i) {
      TreeItem catItem = blockTree.getItem(i);
      CheckBox catCb = (CheckBox)catItem.getWidget();
      catCb.setValue(false,false);
      for (int j = 0; j < catItem.getChildCount(); ++j) {
        TreeItem compItem = catItem.getChild(j);
        CheckBox compCb = (CheckBox)compItem.getWidget();
        if (blockTypes.contains(compCb.getName())) {
          compCb.setValue(true, true);
          catCb.setValue(true,false);
        } else {
          compCb.setValue(false, false);
        }
      }
    }
  }

  private void loadJSONfile(FileUpload filePath, Boolean doUpdate) {
    String uploadFilename = filePath.getFilename();
    if (!uploadFilename.isEmpty()) {
      final String filename = makeValidFilename(uploadFilename);
      if (!TextValidators.isValidCharFilename(filename)) {
        Window.alert(MESSAGES.malformedFilename());
      } else if (!TextValidators.isValidLengthFilename(filename)) {
        Window.alert(MESSAGES.malformedFilename());
      } else if (!filename.endsWith(".json")){
        Window.alert(MESSAGES.malformedFilenameTitle());
      } else {
        loadJSONfileNative(filePath.getElement(), doUpdate);
      }
    }
  }

  public void callLoadGlobalBlocks(String jsonStr) {
    JSONObject jsonObj = JSONParser.parseStrict(jsonStr).isObject();
    INSTANCE.loadComponents(jsonObj);
    INSTANCE.loadGlobalBlocks(jsonObj);
  }

  public void callUpdateValue(String jsonStr) {
    INSTANCE.property.setValue(jsonStr);
    INSTANCE.updateValue();
  }

  public native void exportJavaMethods() /*-{
    var that = this;
    $wnd.load_trees = $entry(that.@com.google.appinventor.client.widgets.properties.SubsetJSONPropertyEditor::callLoadGlobalBlocks(Ljava/lang/String;));
    $wnd.update_value = $entry(that.@com.google.appinventor.client.widgets.properties.SubsetJSONPropertyEditor::callUpdateValue(Ljava/lang/String;));
  }-*/;


  private native void loadJSONfileNative(Element fileElement, boolean doUpdate) /*-{
    var selectedFile = fileElement.files[0];
    if (selectedFile.type == "application/json") {
      var reader = new FileReader();
      reader.onload = function(e) {
        var loadedstr = reader.result;
        $wnd.load_trees(loadedstr);
        if (doUpdate) {
          $wnd.update_value(loadedstr);
        }
      }
      reader.readAsText(selectedFile);
    } else {
      alert("Please select a JSON file");
    }
  }-*/;

  // TODO: This is a copy of a private method in FileUploadWizard. Seems like it should be moved someplace
  // usable outside that one class
  private String makeValidFilename(String uploadFilename) {
    // Strip leading path off filename.
    // We need to support both Unix ('/') and Windows ('\\') separators.
    String filename = uploadFilename.substring(
            Math.max(uploadFilename.lastIndexOf('/'), uploadFilename.lastIndexOf('\\')) + 1);
    // We need to strip out whitespace from the filename.
    filename = filename.replaceAll("\\s", "");
    return filename;
  }

  private void saveFile() {
    // Prompt user for file name, generate the JSON, and save the file
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.saveAsButton());
    final Label saveNameLabel = new Label("Save as file:");  // Todo: Internationalize
    final TextBox saveName = new TextBox();
    final HorizontalPanel savePanel = new HorizontalPanel();
    savePanel.add(saveNameLabel);
    savePanel.add(saveName);
    Button cancelButton = new Button("Cancel");
    cancelButton.addClickListener(new ClickListener() {
      @Override
      public void onClick(Widget sender) {
        dialogBox.hide();
      }
    });
    savePanel.add(cancelButton);
    Button okButton = new Button("OK");
    okButton.addClickListener(new ClickListener() {
      @Override
      public void onClick(Widget sender) {
        String jsonString = createJSONString();
        saveFileNative(saveName.getText(), jsonString);
        dialogBox.hide();
      }
    });
    savePanel.add(okButton);
    dialogBox.setWidget(savePanel);
    dialogBox.center();
    dialogBox.show();
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

  protected void updateValue() {
    if (property.getValue() == "") {
      dropDownButton.setCaption("All");
      dropDownButton.setWidth("");
    } else {
      dropDownButton.setCaption("Toolkit Defined");
    }
  }

  private String createJSONString() {
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
          jsonBlocks.set(jsonBlocks.size(), jsonSingleComp);
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

    if (jsonShownBlockTypes.size() > 1 || jsonComponents.size() > 0) {
      return jsonObj.toString();
    } else {
      return "" ;
    }
  }

  private native void saveFileNative(String fileName, String jsonString) /*-{
    var blob = new Blob([jsonString], {type: "text/plain;charset=utf-8"});
    var anchor = document.createElement('a');
    anchor.href = window.URL.createObjectURL(blob);
    anchor.target = '_blank';
    anchor.download = fileName;
    anchor.click();
  }-*/;


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
        var arrTranslatedNames = new Array();
        for (i = 0; i < block.typeblock.length; ++i) {
          arrTranslatedNames[i] = block.typeblock[i].translatedName;
          // Have not found a way to genericize code where multiple blocks have the exact same translated name.
          // If there's a better way, please fix.
          if (blockName == 'controls_forRange') {
            arrTranslatedNames[i] += Blockly.Msg.LANG_CONTROLS_FORRANGE_INPUT_COLLAPSED_SUFFIX;
          } else if (blockName == 'controls_forEach') {
            arrTranslatedNames[i] += Blockly.Msg.LANG_CONTROLS_FOREACH_INPUT_COLLAPSED_SUFFIX;
          }
        }
        // List all variations on a block, separated by commas
        blockCatDict[block.category][blockName] = arrTranslatedNames.join(", ");
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
