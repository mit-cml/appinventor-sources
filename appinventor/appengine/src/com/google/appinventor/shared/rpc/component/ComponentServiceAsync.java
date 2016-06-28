// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface ComponentServiceAsync {

  /**
   * @see ComponentService#importComponentToProject(String, long, String)
   */
  void importComponentToProject(String forOrUrl, long projectId, String folderPath,
      AsyncCallback<ComponentImportResponse> callback);

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
