// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectSelectionChangeHandler;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.Date;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM;

public class ProjectListItem extends Composite {
  interface ProjectListItemUiBinder extends UiBinder<FlowPanel, ProjectListItem> {}

  @UiField protected FlowPanel container;
  @UiField protected Label nameLabel;
  @UiField protected Label dateModifiedLabel;
  @UiField protected Label dateCreatedLabel;
  @UiField protected CheckBox checkBox;
  @UiField protected FocusPanel projectnameFocusPanel;

  private final Project project;
  private ProjectSelectionChangeHandler changeHandler;

  public ProjectListItem(Project project) {
    bindUI();
    this.getElement().setAttribute("data-exporturl",
        "application/octet-stream:" + project.getProjectName() + ".aia:"
            + ServerLayout.getModuleBaseURL() + ServerLayout.DOWNLOAD_SERVLET_BASE
            + ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + project.getProjectId());
    configureDraggable(this.getElement());
    DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DATE_TIME_MEDIUM);
    Date dateCreated = new Date(project.getDateCreated());
    Date dateModified = new Date(project.getDateModified());

    nameLabel.setText(project.getProjectName());
    dateModifiedLabel.setText(dateTimeFormat.format(dateModified));
    dateCreatedLabel.setText(dateTimeFormat.format(dateCreated));
    this.project = project;
  }

  public void bindUI() {
    ProjectListItemUiBinder uibinder = GWT.create(ProjectListItemUiBinder.class);
    initWidget(uibinder.createAndBindUi(this));
  }

  public void setSelectionChangeHandler(ProjectSelectionChangeHandler changeHandler) {
    this.changeHandler = changeHandler;
  }

  public boolean isSelected() {
    return checkBox.getValue();
  }

  public void setSelected(boolean selected) {
    checkBox.setValue(selected);
    if (selected) {
      container.addStyleName("ode-ProjectRowHighlighted");
    } else {
      container.removeStyleName("ode-ProjectRowHighlighted");
    }
  }

  public Project getProject() {
    return project;
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  @SuppressWarnings("unused")
  @UiHandler("checkBox")
  protected void toggleItemSelection(ClickEvent e) {
    setSelected(checkBox.getValue());
    changeHandler.onSelectionChange(checkBox.getValue());
  }

  @UiHandler("projectnameFocusPanel")
  protected void openProject(KeyDownEvent e) {
    if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      Ode.getInstance().openYoungAndroidProjectInDesigner(project);
    }
  }

  @SuppressWarnings("unused")
  @UiHandler("projectnameFocusPanel")
  protected void itemClicked(ClickEvent e) {
    Ode.getInstance().openYoungAndroidProjectInDesigner(project);
  }

  private static native void configureDraggable(Element el)/*-{
    if (el.getAttribute('draggable') != 'true') {
      el.setAttribute('draggable', 'true');
      el.addEventListener('dragstart', function(e) {
        e.dataTransfer.setData('DownloadURL', this.dataset.exporturl);
      });
    }
  }-*/;
}
