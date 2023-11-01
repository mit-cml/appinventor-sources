package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

public class ProjectListGSoC extends ProjectList {
  interface ProjectListUiBinderGSoC extends UiBinder<FlowPanel, ProjectListGSoC> {}
  private static final ProjectListUiBinderGSoC UI_BINDER = GWT.create(ProjectListUiBinderGSoC.class);

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
  public void bindIU(){
    initWidget(UI_BINDER.createAndBindUi(this));
    super.selectAllCheckBox = selectAllCheckBox;
    super.container = container;
    super.projectNameSortAsc = projectNameSortAsc;
    super.projectNameSortDec = projectNameSortDec;
    super.createDateSortAsc = createDateSortAsc;
    super.createDateSortDec = createDateSortDec;
    super.modDateSortAsc = modDateSortAsc;
    super.modDateSortDec = modDateSortDec;
  }

  @Override
  public ProjectListItem createProjectListItem(Project p) {
    return new ProjectListItemGSoC(p) ;
  }
}
