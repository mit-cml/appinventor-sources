// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import com.google.appinventor.shared.rpc.ServerLayout;

import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

@RemoteServiceRelativePath(ServerLayout.COMPONENT_SERVICE)
public interface ComponentService extends RemoteService {

  /**
   * Import the component to the project in the server and
   * return a list of ProjectNode that can be added to the client
   *
   * @param fileOrUrl the url of the component file or filename of temp file
   * @param projectId id of the project to which the component will be added
   * @param folderPath folder to which the component will be stored
   * @return a list of ProjectNode created from the component
   */
  ComponentImportResponse importComponentToProject(String fileOrUrl, long projectId, String folderPath);

  /**
   * Rename the short name of an imported component
   *
   * @param fullyQualifiedName the fully qualified name of the component
   * @param newName new short name
   * @param projectId id of the project where the component was imported
   */
  void renameImportedComponent(String fullyQualifiedName, String newName, long projectId);

  /**
   * Delete the files of an imported component
   *
   * @param fullyQualifiedName the fully qualified name of the component
   * @param projectId id of the project where the component was imported
   */
  void deleteImportedComponent(String fullyQualifiedName, long projectId);

}
