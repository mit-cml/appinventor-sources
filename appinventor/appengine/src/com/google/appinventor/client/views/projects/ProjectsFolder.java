package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.project.Project;

import java.util.ArrayList;
import java.util.List;

public abstract class ProjectsFolder extends Composite {
  protected Folder folder;
  protected boolean isTrash = false;
  protected int depth;
  protected ProjectSelectionChangeHandler changeHandler;

  protected List<ProjectsFolder> projectsFolders;
  protected List<ProjectListItem> projectListItems;
  protected List<ProjectListItem> selectedProjectListItems;

  public ProjectsFolder() {
    projectsFolders = new ArrayList<ProjectsFolder>();
    projectListItems = new ArrayList<ProjectListItem>();
    selectedProjectListItems = new ArrayList<ProjectListItem>();
    this.isTrash = isTrash;
  }

  public abstract void refresh();
  public abstract void setSelected(boolean selected);
  public abstract boolean isSelected();

  public void setIsTrash(boolean isTrash) {
    this.isTrash = isTrash;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public void setSelectionChangeHandler(ProjectSelectionChangeHandler changeHandler) {
    this.changeHandler = changeHandler;
  }


  public void setFolder(Folder folder) {
    this.folder = folder;
  }

  public Folder getFolder() {
    return folder;
  }

  public List<Project> getSelectedProjects() {
    List<Project> selectedProjects = new ArrayList<Project>();
    for (ProjectListItem item : selectedProjectListItems) {
      selectedProjects.add(item.getProject());
    }
    for (ProjectsFolder item : projectsFolders) {
      selectedProjects.addAll(item.getProjects());
    }
    return selectedProjects;
  }

  public List<Folder> getSelectedFolders() {
    List<Folder> selectedFolders = new ArrayList<Folder>();
    for (ProjectsFolder item : projectsFolders) {
      selectedFolders.addAll(item.getSelectedFolders());
    }
    return selectedFolders;
  }

  public List<Project> getProjects() {
    List<Project> projects = new ArrayList<Project>();
    for (ProjectListItem item : projectListItems) {
      projects.add(item.getProject());
    }
    for (ProjectsFolder item : projectsFolders) {
      projects.addAll(item.getProjects());
    }
    return projects;
  }

  public List<Folder> getFolders() {
    List<Folder> folders = new ArrayList<Folder>();
    for (ProjectsFolder item : projectsFolders) {
      folders.addAll(item.getFolders());
    }
    return folders;
  }

  protected void createProjectsFolder(final Folder folder, final ComplexPanel container) {
    final ProjectsFolder projectsFolder = new ProjectsFolderListItem(folder, depth + 1, isTrash);
    projectsFolder.setSelectionChangeHandler(new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        fireSelectionChangeEvent();
      }
    });
    projectsFolders.add(projectsFolder);
    container.add(projectsFolder);
  }

  protected void createProjectListItem(final Project project, final ComplexPanel container) {
    final ProjectListItem projectListItem = new ProjectListItem(project, depth + 1, isTrash);
    projectListItem.setSelectionChangeHandler(new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        if (selected) {
          selectedProjectListItems.add(projectListItem);
        } else {
          selectedProjectListItems.remove(projectListItem);
        }
        fireSelectionChangeEvent();
      }
    });

    if(!isTrash) {
      projectListItem.setClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent e) {
          if(!project.isInTrash())
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
        }
      });
    }
    projectListItems.add(projectListItem);
    container.add(projectListItem);
  }

  protected void fireSelectionChangeEvent() {
    if (changeHandler != null) {
      changeHandler.onSelectionChange(isSelected());
    };
  }
}
