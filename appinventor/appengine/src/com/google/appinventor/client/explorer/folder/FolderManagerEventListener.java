// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.folder;

public interface FolderManagerEventListener {

  void onFolderAdded(ProjectFolder folder);
  void onFolderRemoved(ProjectFolder folder);
  void onFolderRenamed(ProjectFolder folder);
  void onFoldersChanged();
  void onFoldersLoaded();

}
