// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.shared.rpc.project.ProjectNode;
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
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.Iterator;

public class SubsetJSONPropertyEditor  extends AdditionalChoicePropertyEditor
        implements ProjectChangeListener {

  Frame subsetSelector;
  SimplePanel framePanel;

  public SubsetJSONPropertyEditor() {
    super();
    subsetSelector = new Frame();
    framePanel = new SimplePanel();

    subsetSelector.setUrl("JSONGenerator/index.html");
    subsetSelector.setWidth("100%");
    subsetSelector.setHeight("100%");
    framePanel.setWidth("100%");
    framePanel.setHeight("100%");
    framePanel.add(subsetSelector);
    initAdditionalChoicePanel(framePanel);
  }

  @Override
  protected void openAdditionalChoiceDialog() {
    subsetSelector.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent loadEvent) {
        Document d = IFrameElement.as(subsetSelector.getElement()).getContentDocument();
        setJSON(d, property.getValue());
      }
    });
    popup.setHeight("600px");
    popup.setWidth("1000px");
    popup.show();
    popup.center();
//    popup.setPopupPosition(0,0);
  }

  // AdditionalChoicePropertyEditor implementation
  @Override
  protected boolean okAction() {
    Document d = IFrameElement.as(subsetSelector.getElement()).getContentDocument();
    String e = getJSON(d);
    property.setValue(e);

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
