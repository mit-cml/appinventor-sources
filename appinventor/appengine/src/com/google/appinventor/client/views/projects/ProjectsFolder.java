package com.google.appinventor.client.views.projects;

import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.project.Project;

import java.util.List;

public interface ProjectsFolder {
  public List<Project> getSelectedProjects();

  public List<Folder> getSelectedFolders();

  public void refresh();

  public void setSelected(boolean selectionState);

  public void setFolder(Folder folder);

  public Folder getFolder();
}
