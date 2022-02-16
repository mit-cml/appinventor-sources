package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.folder.FolderManagerEventListener;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;

import java.util.ArrayList;
import java.util.List;

public class ProjectsList extends ProjectsFolder implements FolderManagerEventListener,
    ProjectManagerEventListener {

  interface ProjectsListUiBinder extends UiBinder<FlowPanel, ProjectsList> {}
  private static final ProjectsListUiBinder UI_BINDER = GWT.create(ProjectsListUiBinder.class);

  @UiField FlowPanel container;
  @UiField FlowPanel header;
  @UiField CheckBox checkBox;

  @UiField(provided=true)
  Resources.ProjectsListStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.projectsListStyleDark() : Resources.INSTANCE.projectsListStyleLight();

  public ProjectsList() {
    style.ensureInjected();
    initWidget(UI_BINDER.createAndBindUi(this));
    Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
    setDepth(0);
  }

  @Override
  public void refresh() {
    container.clear();
    container.add(header);
    selectedProjectListItems.clear();
    for (final Folder childFolder : folder.getChildFolders()) {
      if ("*trash*".equals(childFolder.getName())) continue;
      createProjectsFolder(childFolder, container);
    }
    for(final Project project : folder.getProjects()) {
      createProjectListItem(project, container);
    }
  }

  @Override
  public void setSelected(boolean selected) {
    checkBox.setValue(selected);
    selectedProjectListItems.clear();
    for(ProjectListItem item : projectListItems) {
      item.setSelected(selected);
      if(selected) {
        selectedProjectListItems.add(item);
      }
    }
    for(ProjectsFolder item : projectsFolders) {
      item.setSelected(selected);
    }
  }

  @Override
  public boolean isSelected() {
    return checkBox.getValue();
  }

  @Override
  public void setIsTrash(boolean isTrash) {
    this.isTrash = isTrash;
    if (isTrash) {
      setFolder(Ode.getInstance().getFolderManager().getTrashFolder());
    } else {
      setFolder(Ode.getInstance().getFolderManager().getGlobalFolder());
    }
    if (folder != null) {
      refresh();
    }
  }

  @Override
  protected void fireSelectionChangeEvent() {
    if (getFolders().size() == getSelectedFolders().size() &&
        getProjects().size() == getSelectedProjects().size()) {
      checkBox.setValue(true);
    } else {
      checkBox.setValue(false);
    }
    super.fireSelectionChangeEvent();
  }

  @UiHandler("checkBox")
  void toggleItemSelection(ClickEvent e) {
    setSelected(checkBox.getValue());
    fireSelectionChangeEvent();
  }

  // FolderManagerEventListener implementation
  @Override
  public void onFolderAdded(Folder folder) {
    refresh();
  }

  @Override
  public void onFolderRemoved(Folder folder) {
    refresh();
  }

  @Override
  public void onFolderRenamed(Folder folder) {
    refresh();
  }

  @Override
  public void onFoldersLoaded() {
    setIsTrash(isTrash);
  }

  @Override
  public void onProjectAdded(Project project) {
    OdeLog.log("Project Added to ProjectList: " + project.getProjectName());
    folder.addProject(project);
    refresh();
  }

  @Override
  public void onTrashProjectRestored(Project project) {
    Ode.getInstance().getFolderManager().getGlobalFolder().addProject(project);
    Ode.getInstance().getFolderManager().getTrashFolder().removeProject(project);
    refresh();
  }

  @Override
  public void onProjectTrashed(Project project) {
    folder.removeProject(project);
    Ode.getInstance().getFolderManager().getTrashFolder().addProject(project);
    refresh();
  }

  @Override
  public void onProjectDeleted(Project project) {
    folder.removeProject(project);
    refresh();
  }

  @Override
  public void onProjectsLoaded() {
    refresh();
  }}
