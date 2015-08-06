// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface ComponentServiceAsync {

  /**
   * @see ComponentService#getComponents()
   */
  void getComponents(AsyncCallback<List<Component>> callback);

  /**
   * @see ComponentService#importComponentToProject(Component, long, String)
   */
  void importComponentToProject(Component info, long projectId,
      String folderPath, AsyncCallback<List<ProjectNode>> callback);

  /**
   * @see ComponentService#importComponentToProject(String, long, String)
   */
  void importComponentToProject(String url, long projectId, String folderPath,
      AsyncCallback<List<ProjectNode>> callback);

  /**
   * @see ComponentService#deleteComponent(Component)
   */
  void deleteComponent(Component component, AsyncCallback<Void> callback);

  /**
   * @see ComponentService#renameImportedComponent(String, String, long)
   */
  void renameImportedComponent(String fullyQualifiedName, String newName,
      long projectId, AsyncCallback<Void> callback);

  /**
   * @see ComponentService#deleteImportedComponent(String, long)
   */
   void deleteImportedComponent(String fullyQualifiedName, long projectId, AsyncCallback<Void> callback);
}
