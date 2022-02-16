package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import com.google.appinventor.client.explorer.project.Project;

import java.util.Date;

public class ProjectListItem extends Composite {
  interface ProjectListItemUiBinder extends UiBinder<FlowPanel, ProjectListItem> {}
  private static final ProjectListItemUiBinder UI_BINDER = GWT.create(ProjectListItemUiBinder.class);

  private boolean isInTrash;
  private int depth;

  @UiField FlowPanel container;
  @UiField Label nameLabel;
  @UiField Label dateModifiedLabel;
  @UiField Label dateCreatedLabel;
  @UiField CheckBox checkBox;

  @UiField(provided=true)
  Resources.ProjectListItemStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.listItemStyleDark() : Resources.INSTANCE.listItemStyleLight();

  private ProjectsFolder parentFolder;
  private Project project;
  private ProjectSelectionChangeHandler changeHandler;
  private ClickHandler clickHandler;

  public ProjectListItem(Project project, int depth, boolean isInTrash) {
    style.ensureInjected();
    initWidget(UI_BINDER.createAndBindUi(this));
    DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();

    Date dateCreated = new Date(project.getDateCreated());
    Date dateModified = new Date(project.getDateModified());

    nameLabel.setText(project.getProjectName());
    dateModifiedLabel.setText(dateTimeFormat.format(dateModified));
    dateCreatedLabel.setText(dateTimeFormat.format(dateCreated));
    this.project = project;
    this.isInTrash = isInTrash;
    this.depth = depth;
    checkBox.getElement().setAttribute("style", "margin-right: " + (depth * 10) + "px");
  }

  public void setSelectionChangeHandler(ProjectSelectionChangeHandler changeHandler) {
    this.changeHandler = changeHandler;
  }

  public boolean isSelected() {
    return checkBox.getValue();
  }

  public void setSelected(boolean selected) {
    checkBox.setValue(selected);
    if(selected) {
      container.addStyleName(style.selected());
    } else {
      container.removeStyleName(style.selected());
    }
  }

  public void setClickHandler(ClickHandler handler) {
    this.clickHandler = handler;
  }

  public Project getProject() {
    return project;
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  @UiHandler("checkBox")
  void toggleItemSelection(ClickEvent e) {
    setSelected(checkBox.getValue());
    changeHandler.onSelectionChange(checkBox.getValue());
  }

  @UiHandler("nameLabel")
  void itemClicked(ClickEvent e) {
    if (clickHandler != null) {
      clickHandler.onClick(e);
    }
  }
}
