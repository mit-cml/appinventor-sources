package com.google.appinventor.client.explorer.folder;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.appinventor.client.views.projects.ProjectSelectionChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.logging.Logger;

public abstract class ProjectsFolder extends Composite {
  private static final Logger LOG = Logger.getLogger(ProjectsFolder.class.getName());
  protected Folder folder;
  protected boolean isTrash = false;
  protected int depth;
  protected ProjectSelectionChangeHandler changeHandler;

  protected HashSet<ProjectsFolderListItem> projectsFolderListItems;
  protected HashSet<ProjectListItem> projectListItems;

  public ProjectsFolder() {
    projectsFolderListItems = new HashSet<>();
    projectListItems = new HashSet<>();
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
    for (ProjectListItem item : projectListItems) {
      if (item.isSelected()) {
        selectedProjects.add(item.getProject());
      }
    }
    for (ProjectsFolderListItem item : projectsFolderListItems) {
      if (item.isExpanded())
      {
        selectedProjects.addAll(item.getSelectedProjects());
      }
    }
    return selectedProjects;
  }

  public List<Folder> getSelectedFolders() {
    List<Folder> selectedFolders = new ArrayList<Folder>();
    for (ProjectsFolder item : projectsFolderListItems) {
      selectedFolders.addAll(item.getSelectedFolders());
    }
    return selectedFolders;
  }

  public List<Project> getAllProjects() {
    List<Project> projects = new ArrayList<Project>();
    for (Project item : folder.getProjects()) {
      projects.add(item);
    }
    for (ProjectsFolder item : projectsFolderListItems) {
      projects.addAll(item.getAllProjects());
    }
    return projects;
  }

  public List<Project> getVisibleProjects() {
    List<Project> projects = new ArrayList<Project>();
    for (Project item : folder.getProjects()) {
      projects.add(item);
    }
    for (ProjectsFolderListItem folderItem : projectsFolderListItems) {
      if (folderItem.isExpanded()) {
        projects.addAll(folderItem.getVisibleProjects());
      }
    }
    return projects;
  }

  public List<Folder> getAllFolders() {
    List<Folder> folders = new ArrayList<Folder>();
    for (ProjectsFolder item : projectsFolderListItems) {
      folders.addAll(item.getAllFolders());
    }
    return folders;
  }

  public List<Folder> getSelectableFolders() {
    List<Folder> folders = new ArrayList<Folder>();
    for (ProjectsFolderListItem item : projectsFolderListItems) {
      if (item.isExpanded()) {
        folders.addAll(item.getSelectableFolders());
      } else {
        folders.add(getFolder());
      }
    }
    return folders;
  }

  protected ProjectsFolderListItem createProjectsFolderListItem(final Folder folder, final ComplexPanel container) {
    final ProjectsFolderListItem projectsFolder = new ProjectsFolderListItem(folder, depth + 1);
    projectsFolder.setSelectionChangeHandler(new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
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
    return projectListItem;
  }

  protected void fireSelectionChangeEvent() {
    if (changeHandler != null) {
      changeHandler.onSelectionChange(isSelected());
    };
    Ode.getInstance().getBindProjectToolbar().updateButtons();
  }
}
