// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.components;

import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.gwt.user.client.ui.TreeItem;

public class FolderTreeItem extends TreeItem {

//  private static Resources.FolderTreeItemStyle style = Ode.getUserDarkThemeEnabled() ?
//      Resources.INSTANCE.folderTreeItemStyleDark() : Resources.INSTANCE.folderTreeItemStyleLight();

  private ProjectFolder folder;

  public FolderTreeItem(ProjectFolder folder) {
    super();
//    style.ensureInjected();
    this.folder = folder;
    setHTML("<span>" + folder.getName() + "</span>");
//    setText(folder.getName());
//    setStylePrimaryName(style.item());
  }

  public ProjectFolder getFolder() {
    return folder;
  }
}
