package com.google.appinventor.client.components;

import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.gwt.user.client.ui.TreeItem;

import com.google.appinventor.client.Ode;

public class FolderTreeItem extends TreeItem {

  private static Resources.FolderTreeItemStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.folderTreeItemStyleDark() : Resources.INSTANCE.folderTreeItemStyleLight();

  private ProjectFolder folder;

  public FolderTreeItem(ProjectFolder folder) {
    super();
    style.ensureInjected();
    this.folder = folder;
    setText(folder.getName());
    setStylePrimaryName(style.item());
  }

  public ProjectFolder getFolder() {
    return folder;
  }
}
