package com.google.appinventor.client.views.projects;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resources used by the projects view.
 */
public interface Resources extends ClientBundle {

  public static final Resources INSTANCE =  GWT.create(Resources.class);

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/light.css",
    "com/google/appinventor/client/views/projects/listItem.css"
  })
  ListItemStyle listItemStyleLight();

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/dark.css",
    "com/google/appinventor/client/views/projects/listItem.css"
  })
  ListItemStyle listItemStyleDark();

  public interface ListItemStyle extends CssResource {
      String listItem();
      String selected();
      String field();
      String projectName();
      String selection();
  }

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/light.css",
    "com/google/appinventor/client/views/projects/projectsExplorer.css"
  })
  ProjectsExplorerStyle projectsExplorerStyleLight();

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/dark.css",
    "com/google/appinventor/client/views/projects/projectsExplorer.css"
  })
  ProjectsExplorerStyle projectsExplorerStyleDark();

  public interface ProjectsExplorerStyle extends CssResource {
      String container();
      String projectActions();
      String trashActions();
      String viewTitle();
      String actionGroup();
      String overflowButton();
      String newProjectActions();
  }


  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/light.css",
    "com/google/appinventor/client/views/projects/projectsList.css"
  })
  ProjectsListStyle projectsListStyleLight();

  @Source({
    "com/google/appinventor/client/resources/base.css",
    "com/google/appinventor/client/resources/dark.css",
    "com/google/appinventor/client/views/projects/projectsList.css"
  })
  ProjectsListStyle projectsListStyleDark();

  public interface ProjectsListStyle extends CssResource {
      String container();
      String sticky();
      String listHeader();
      String headerItem();
      String headerItemText();
      String sortIndicator();
      String selection();
  }
}
