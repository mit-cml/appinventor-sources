package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

public class ProjectListGSoC extends ProjectList {
  interface ProjectListUiBinderGSoC extends UiBinder<FlowPanel, ProjectListGSoC> {}

  // UI elements
  @UiField CheckBox selectAllCheckBox;
  @UiField FlowPanel container;
  @UiField InlineLabel projectNameSortDec;
  @UiField InlineLabel projectNameSortAsc;
  @UiField InlineLabel createDateSortDec;
  @UiField InlineLabel createDateSortAsc;
  @UiField InlineLabel modDateSortDec;
  @UiField InlineLabel modDateSortAsc;

  @Override
  public void bindIU() {
    ProjectListUiBinderGSoC UI_BINDER = GWT.create(ProjectListUiBinderGSoC.class);
    initWidget(UI_BINDER.createAndBindUi(this));
    super.selectAllCheckBox = selectAllCheckBox;
    super.container = container;
    super.projectNameSortAsc = projectNameSortAsc;
    super.projectNameSortDec = projectNameSortDec;
    super.createDateSortAsc = createDateSortAsc;
    super.createDateSortDec = createDateSortDec;
    super.modDateSortAsc = modDateSortAsc;
    super.modDateSortDec = modDateSortDec;
    Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);

    // It is important to listen to project manager events as soon as possible.
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
  }

  @UiHandler("selectAllCheckBox")
  protected void toggleAllItemSelection(ClickEvent e) {
    super.toggleAllItemSelection(e);
  }

  @Override
  public ProjectListItemGSoC createProjectListItem(Project p) {
    return new ProjectListItemGSoC(p);
  }
}
