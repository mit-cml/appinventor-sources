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
import com.google.gwt.user.client.DOM;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

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
      categoryItems.put(cat.getDocName(), new TreeItem(new CheckBox(cat.getName())));
    }
    for (String cname : db.getComponentNames()) {

      ComponentDatabaseInterface.ComponentDefinition cd = db.getComponentDefinition(cname);
      if (cd.getCategoryDocUrlString() != "internal" || cd.getCategoryString() != "UNINITIALIZED") {
        TreeItem subTree = new TreeItem(new CheckBox(cname));
        subTree.addItem(new CheckBox(cd.getType()));
        for (ComponentDatabaseInterface.PropertyDefinition pdef : cd.getProperties()) {
          subTree.addItem(new CheckBox(pdef.getName()));
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
    Iterator<TreeItem> allItems = selectorTree.treeItemIterator();
    property.setValue(allItems.toString());
    return true;
  }

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
