package com.google.appinventor.client.components;

import com.google.gwt.user.client.ui.TreeItem;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.folder.Folder;

public class FolderTreeItem extends TreeItem {

  private static Resources.FolderTreeItemStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.folderTreeItemStyleDark() : Resources.INSTANCE.folderTreeItemStyleLight();

  private Folder folder;

  public FolderTreeItem(Folder folder) {
    super();
    style.ensureInjected();
    this.folder = folder;
    setText(folder.getName());
    setStylePrimaryName(style.item());
  }

  public Folder getFolder() {
    return folder;
  }
}
