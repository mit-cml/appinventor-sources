package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.components.Icon;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.logging.Logger;

public class ProjectFolderGSoC extends ProjectFolder {
  private static final Logger LOG = Logger.getLogger(ProjectFolderGSoC.class.getName());
  interface ProjectFolderUiBinderGSoC extends UiBinder<FlowPanel, ProjectFolderGSoC> { }

  @UiField protected FlowPanel container;
  @UiField protected FlowPanel childrenContainer;
  @UiField protected Label nameLabel;
  @UiField protected Label dateModifiedLabel;
  @UiField protected Label dateCreatedLabel;
  @UiField protected CheckBox checkBox;
  @UiField protected Icon expandButton;

  public ProjectFolderGSoC(String name, long dateCreated, long dateModified, ProjectFolder parent) {
    super(name, dateCreated, dateModified, parent);
  }

  public ProjectFolderGSoC(String name, long dateCreated, ProjectFolder parent) {
    this(name, dateCreated, dateCreated, parent);
  }

  public ProjectFolderGSoC(JSONObject json, ProjectFolder parent) {
    super(json, parent);
  }

  @Override
  public void bindUI() {
    ProjectFolderUiBinderGSoC UI_BINDER = GWT.create(ProjectFolderUiBinderGSoC.class);
    initWidget(UI_BINDER.createAndBindUi(this));
    super.container = container;
    super.childrenContainer = childrenContainer;
    super.nameLabel = nameLabel;
    super.dateModifiedLabel = dateModifiedLabel;
    super.dateCreatedLabel = dateCreatedLabel;
    super.checkBox = checkBox;
    super.expandButton = expandButton;
  }

  @Override
  public ProjectListItem createProjectListItem(Project p) {
    return new ProjectListItemGSoC(p) ;
  }

  @SuppressWarnings("unused")
  @UiHandler("checkBox")
  protected void toggleFolderSelection(ClickEvent e) {
    super.toggleFolderSelection(e);
  }

  @SuppressWarnings("unused")
  @UiHandler("expandButton")
  protected void toggleExpandedState(ClickEvent e) {
    super.toggleExpandedState(e);
  }
}
