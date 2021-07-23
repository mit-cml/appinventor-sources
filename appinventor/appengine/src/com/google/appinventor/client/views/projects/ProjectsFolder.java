package com.google.appinventor.client.views.projects;

import com.google.appinventor.client.explorer.project.Project;

import java.util.List;

public interface ProjectsFolder {
  public List<Project> getSelectedProjects();

  public void refresh();

  public void setSelected(boolean selectionState);
}
