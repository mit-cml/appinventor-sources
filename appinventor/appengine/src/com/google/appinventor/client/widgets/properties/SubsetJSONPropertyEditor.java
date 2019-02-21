// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.Iterator;

public class SubsetJSONPropertyEditor  extends AdditionalChoicePropertyEditor
        implements ProjectChangeListener {

  public SubsetJSONPropertyEditor() {
    super();
    Frame subsetSelector = new Frame();
    SimplePanel framePanel = new SimplePanel();

    subsetSelector.setUrl("http://fred.com");
    framePanel.add(subsetSelector);
    initAdditionalChoicePanel(framePanel);

  }

  // AdditionalChoicePropertyEditor implementation
  @Override
  protected boolean okAction() {
    return true;
  }


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
