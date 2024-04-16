package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class ProjectListItemNeo extends ProjectListItem {

  interface ProjectListItemUiBinderNeo extends UiBinder<FlowPanel, ProjectListItemNeo> {}

  private static final ProjectListItemUiBinderNeo UI_BINDER =
      GWT.create(ProjectListItemUiBinderNeo.class);

  @UiField FlowPanel container;
  @UiField
  Label nameLabel;
  @UiField Label dateModifiedLabel;
  @UiField Label dateCreatedLabel;
  @UiField CheckBox checkBox;

  public ProjectListItemNeo(Project project) {
    super(project);
  }

  @Override
  public void bindUI() {
    initWidget(UI_BINDER.createAndBindUi(this));
    super.container = container;
    super.checkBox = checkBox;
    super.dateCreatedLabel = dateCreatedLabel;
    super.dateModifiedLabel = dateModifiedLabel;
    super.nameLabel = nameLabel;
  }

  @UiHandler("checkBox")
  protected void toggleItemSelection(ClickEvent e) {
    super.toggleItemSelection(e);
  }

  @UiHandler("nameLabel")
  protected void itemClicked(ClickEvent e) {
    super.itemClicked(e);
  }
}
