package com.google.appinventor.client.explorer.folder;

import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.components.Icon;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.google.appinventor.client.Ode.MESSAGES;

public class ProjectsFolderListItem extends ProjectsFolder {
  private static final Logger LOG = Logger.getLogger(ProjectsFolderListItem.class.getName());

  interface ProjectsFolderListItemUiBinder extends UiBinder<FlowPanel, ProjectsFolderListItem> {}
  private static final ProjectsFolderListItemUiBinder UI_BINDER = GWT.create(ProjectsFolderListItemUiBinder.class);

  @UiField FlowPanel container;
  @UiField FlowPanel childrenContainer;
  @UiField Label nameLabel;
  @UiField Label dateModifiedLabel;
  @UiField Label dateCreatedLabel;
  @UiField CheckBox checkBox;
  @UiField Icon toggleButton;

  private boolean isExpanded;

  public ProjectsFolderListItem(Folder folder, int depth) {
    initWidget(UI_BINDER.createAndBindUi(this));
    setFolder(folder);
    setDepth(depth);
    refresh();
  }

  @Override
  public void refresh() {
    childrenContainer.clear();
    projectListItems.clear();
    projectsFolderListItems.clear();
    for (final Folder childFolder : folder.getChildFolders()) {
      ProjectsFolderListItem item = createProjectsFolderListItem(childFolder, childrenContainer);
      projectsFolderListItems.add(item);
    }
    for(final Project project : folder.getProjects()) {
      ProjectListItem item = createProjectListItem(project, childrenContainer);
      projectListItems.add(item);
    }
  }

  @Override
  public void setSelected(boolean selected) {
    checkBox.setValue(selected);
    if (isExpanded) {
      for (ProjectsFolderListItem projectsFolderListItem : projectsFolderListItems) {
        projectsFolderListItem.setSelected(selected);
      }
      for (ProjectListItem projectListItem : projectListItems) {
        projectListItem.setSelected(selected);
      }
    } else {
      if (selected) {
        container.addStyleName("ode-ProjectRowHighlighted");
      } else {
        container.removeStyleName("ode-ProjectRowHighlighted");
      }
    }
  }

  @Override
  public boolean isSelected() {
    return checkBox.getValue();
  }

  @Override
  public void setDepth(int depth) {
    super.setDepth(depth);
    checkBox.getElement().setAttribute("style", "margin-left: " + ((depth-1) * 40) + "px");
  }

  @Override
  public void setFolder(Folder folder) {
    super.setFolder(folder);
    nameLabel.setText(folder.getName());
    Date dateCreated = new Date(folder.getDateCreated());
    Date dateModified = new Date(folder.getDateModified());
    DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();
    dateModifiedLabel.setText(dateTimeFormat.format(dateModified));
    dateCreatedLabel.setText(dateTimeFormat.format(dateCreated));
  }

  @Override
  public List<Project> getSelectedProjects() {
    if (isExpanded) {
      return super.getSelectedProjects();
    } else {
      return new ArrayList<Project>();
    }
  }

  public boolean isExpanded() {
    return isExpanded;
  }

  @Override
  public List<Folder> getSelectedFolders() {
    if (isExpanded) {
      return super.getSelectedFolders();
    } else {
      if (isSelected()) {
        return Arrays.asList(new Folder[] {folder});
      } else {
        return new ArrayList<Folder>();
      }
    }
  }

  @Override
  public List<Project> getAllProjects() {
    if (isExpanded) {
      return super.getAllProjects();
    } else {
      return new ArrayList<Project>();
    }
  }

  @Override
  public List<Folder> getAllFolders() {
    return super.getAllFolders();
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  @UiHandler("checkBox")
  void toggleItemSelection(ClickEvent e) {
    setSelected(checkBox.getValue());
    fireSelectionChangeEvent();
  }

  @UiHandler("toggleButton")
  void toggleExpandedState(ClickEvent e) {
    setSelected(false);
    isExpanded = !isExpanded;
    if (isExpanded) {
      toggleButton.setIcon("expand_more");
      childrenContainer.removeStyleName("ode-ProjectRowHidden");
      checkBox.addStyleName("ode-ProjectElementHidden");
      checkBox.setValue(false);
    } else {
      toggleButton.setIcon("chevron_right");
      childrenContainer.addStyleName("ode-ProjectRowHidden");
      checkBox.removeStyleName("ode-ProjectElementHidden");
    }
    fireSelectionChangeEvent();
  }

  @UiHandler("nameLabel")
  void itemClicked(ClickEvent e) {
    toggleExpandedState(e);
  }
}
