package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

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

public class ListItem extends Composite {
  interface ListItemUiBinder extends UiBinder<FlowPanel, ListItem> {}
  private static final ListItemUiBinder UI_BINDER = GWT.create(ListItemUiBinder.class);

  interface Style extends CssResource {
    String selected();
  }

  @UiField Style style;

  @UiField FlowPanel container;
  @UiField Label nameLabel;
  @UiField Label dateModifiedLabel;
  @UiField Label dateCreatedLabel;
  @UiField CheckBox checkBox;

  private ProjectsFolder parentFolder;
  private Project project;
  private ItemSelectionChangeHandler changeHandler;
  private ClickHandler clickHandler;

  public ListItem() {
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  public ListItem(Project project, ItemSelectionChangeHandler changeHandler) {
    this();

    DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();

    Date dateCreated = new Date(project.getDateCreated());
    Date dateModified = new Date(project.getDateModified());

    nameLabel.setText(project.getProjectName());
    dateModifiedLabel.setText(dateTimeFormat.format(dateModified));
    dateCreatedLabel.setText(dateTimeFormat.format(dateCreated));
    this.project = project;
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

  public static abstract class ItemSelectionChangeHandler {
    public abstract void onSelectionChange(boolean selected);
  }
}
