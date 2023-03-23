package com.google.appinventor.client.explorer.folder;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.appinventor.client.views.projects.ProjectSelectionChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class ProjectsFolder extends Composite {
  private static final Logger LOG = Logger.getLogger(ProjectsFolder.class.getName());
  protected Folder folder;
  protected boolean isTrash = false;
  protected int depth;
  protected ProjectSelectionChangeHandler changeHandler;

  protected List<ProjectsFolder> projectsFolders;
  protected List<ProjectListItem> projectListItems;
  protected List<ProjectListItem> selectedProjectListItems;
  protected List<ProjectsFolder> selectedProjectsFolders;

  public ProjectsFolder() {
    projectsFolders = new ArrayList<ProjectsFolder>();
    projectListItems = new ArrayList<ProjectListItem>();
    selectedProjectListItems = new ArrayList<ProjectListItem>();
    selectedProjectsFolders = new ArrayList<ProjectsFolder>();
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
      selectedProjects.addAll(item.getSelectedProjects());
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

  protected ProjectsFolder createProjectsFolder(final Folder folder, final ComplexPanel container) {
    final ProjectsFolder projectsFolder = new ProjectsFolderListItem(folder, depth + 1);
    projectsFolder.setSelectionChangeHandler(new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        if (selected) {
          LOG.warning("ADD selected folder list item: " + folder.getName());
          selectedProjectsFolders.add(projectsFolder);
        } else {
          LOG.warning("REMOVE project list item: " + folder.getName());
          selectedProjectsFolders.remove(projectsFolder);
        }
        fireSelectionChangeEvent();
      }
    });
    container.add(projectsFolder);
    return projectsFolder;
  }

  protected ProjectListItem createProjectListItem(final Project project, final ComplexPanel container) {
    final ProjectListItem projectListItem = new ProjectListItem(project, depth + 1);
    projectListItem.setSelectionChangeHandler(new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        if (selected) {
          LOG.warning("ADD project list item: " + project.getProjectName());
          selectedProjectListItems.add(projectListItem);
        } else {
          LOG.warning("REMOVE project list item: " + project.getProjectName());
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
//    projectListItems.add(projectListItem);
    container.add(projectListItem);
    return projectListItem;
  }

  protected void fireSelectionChangeEvent() {
    if (changeHandler != null) {
      changeHandler.onSelectionChange(isSelected());
    };
    Ode.getInstance().getBindProjectToolbar().updateButtons();
  }
}
