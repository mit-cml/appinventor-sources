package com.google.appinventor.client;

import com.google.appinventor.client.explorer.folder.FolderJSONKeys;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectManager;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.junit.client.GWTTestCase;

public class OdeTest extends GWTTestCase {
  public void testCompareLocales() {
    assertTrue("Handles default case one is null",
        Ode.compareLocales(null, "en", "en"));
    assertTrue("Handles both cases being null",
        Ode.compareLocales(null, null, "en"));
    assertTrue("Handles when both cases are the same",
        Ode.compareLocales("en", "en", "en"));
    assertFalse("Handles when the cases are different",
        Ode.compareLocales("en", "fr_FR", "en"));
    assertFalse("Handles when the default is different",
        Ode.compareLocales(null, "fr_FR", "en"));
  }

  public void testMovingProjectBetweenFoldersUpdatesSavedTree() {
    ProjectFolder root = new ProjectFolder("Root", 0, null);
    ProjectFolder parent = addFolder(root, "Parent");
    ProjectFolder source = addFolder(parent, "Source");
    ProjectFolder destination = addFolder(parent, "Destination");
    Project project = new ProjectManager().addProject(
        new UserProject(1, "Example", "YoungAndroid", 0));
    source.addProject(project);
    assertEquals(1, projectsIn(root.toJSON(), "Source").size());

    destination.addProject(project);

    JSONObject saved = root.toJSON();
    assertEquals(0, projectsIn(saved, "Source").size());
    JSONArray moved = projectsIn(saved, "Destination");
    assertEquals(1, moved.size());
    assertEquals("1", moved.get(0).isString().stringValue());
  }

  private static ProjectFolder addFolder(ProjectFolder parent, String name) {
    ProjectFolder folder = new ProjectFolder(name, 0, parent);
    parent.addChildFolder(folder);
    return folder;
  }

  private static JSONArray projectsIn(JSONObject root, String folderName) {
    JSONObject parent = root.get(FolderJSONKeys.CHILD_FOLDERS).isArray().get(0).isObject();
    JSONArray folders = parent.get(FolderJSONKeys.CHILD_FOLDERS).isArray();
    for (int i = 0; i < folders.size(); i++) {
      JSONObject folder = folders.get(i).isObject();
      if (folderName.equals(folder.get(FolderJSONKeys.NAME).isString().stringValue())) {
        return folder.get(FolderJSONKeys.PROJECTS).isArray();
      }
    }
    throw new AssertionError("Folder missing from saved tree: " + folderName);
  }

  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient";
  }
}
