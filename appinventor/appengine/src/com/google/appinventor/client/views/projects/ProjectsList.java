package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

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
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProjectsList extends Composite implements ProjectsFolder,
      ProjectManagerEventListener {
  interface ProjectsListUiBinder extends UiBinder<FlowPanel, ProjectsList> {}
  private static final ProjectsListUiBinder UI_BINDER = GWT.create(ProjectsListUiBinder.class);

  @UiField FlowPanel container;
  @UiField FlowPanel header;
  @UiField CheckBox checkBox;

  private List<Project> projects;
  private List<Project> selectedProjects;
  private List<ListItem> listItems;
  private boolean firstLoadInProgress;
  private SelectionChangeHandler selectionChangeHandler;
  private boolean trashList;

  public ProjectsList() {
    projects = new ArrayList<Project>();
    selectedProjects = new ArrayList<Project>();
    listItems = new ArrayList<ListItem>();
    firstLoadInProgress = true;
    initWidget(UI_BINDER.createAndBindUi(this));
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
  }

  public void setSelectionChangeHandler(SelectionChangeHandler handler) {
    this.selectionChangeHandler = handler;
  }

  public void setForTrash(boolean forTrash) {
    trashList = forTrash;
    reloadProjects();
  }

  private void reloadProjects() {
    selectedProjects.clear();
    projects.clear();
    for(Project project : Ode.getInstance().getProjectManager().getProjects("")) {
      if((project.isInTrash() && trashList) || (!project.isInTrash() && !trashList) ) {
        projects.add(project);
      }
    }
    refresh();
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  @UiHandler("checkBox")
  void toggleItemSelection(ClickEvent e) {
    setSelected(checkBox.getValue());
  }

  //  ProjectManagerEventListener implementation

  @Override
  public void onProjectAdded(Project project) {
    if((project.isInTrash() && trashList) || (!project.isInTrash() && !trashList) ) {
      projects.add(project);
    }
    if(!firstLoadInProgress) {
      refresh();
    }
  }

  @Override
  public void onTrashProjectRestored(Project project) {
    if(!trashList) {
      projects.add(project);
    } else {
      projects.remove(project);
    }
    refresh();
  }

  @Override
  public void onProjectTrashed(Project project) {
    if(trashList) {
      projects.add(project);
    } else {
      projects.remove(project);
    }
    refresh();
  }

  @Override
  public void onProjectDeleted(Project project) {
    projects.remove(project);
    refresh();
  }

  @Override
  public void onProjectsLoaded() {
    reloadProjects();
    firstLoadInProgress = false;
  }

  // ProjectsFolder implementation

  @Override
  public List<Project> getSelectedProjects() {
    return selectedProjects;
  }

  @Override
  public void refresh() {
    container.clear();
    container.add(header);
    listItems.clear();

    for(final Project project : projects) {
      ListItem item = createListItem(project);
      listItems.add(item);
      container.add(item);
    }
  }

  @Override
  public void setSelected(boolean selectionState) {
    checkBox.setValue(selectionState);
    selectedProjects.clear();
    for(ListItem item : listItems) {
      item.setSelected(selectionState);
      if(selectionState) {
        selectedProjects.add(item.getProject());
      }
    }
    if(selectionChangeHandler != null) selectionChangeHandler.onSelectionChange(selectedProjects.size());
  }

  public abstract static class SelectionChangeHandler {
    public abstract void onSelectionChange(int selectedItemCount);
  }

  private ListItem createListItem(final Project project) {
    ListItem projectListItem = new ListItem(project, trashList, new ListItem.ItemSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        if(selected) {
          selectedProjects.add(project);
        } else {
          selectedProjects.remove(project);
        }
        checkBox.setValue(false);
        if(selectedProjects.size() == projects.size()) {
          checkBox.setValue(true);
        }
        if(selectionChangeHandler != null) selectionChangeHandler.onSelectionChange(selectedProjects.size());
      }
    });

    if(!trashList) {
      projectListItem.setClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent e) {
          if(!project.isInTrash())
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
        }
      });
    }

    return projectListItem;
  }
}
