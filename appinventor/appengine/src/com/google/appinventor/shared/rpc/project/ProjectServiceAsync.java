// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Interface for the service providing project information. All declarations
 * in this interface are mirrored in {@link ProjectService}. For further
 * information see {@link ProjectService}.
 *
 */
public interface ProjectServiceAsync {

  /**
   * @see ProjectService#newProject(String, String, NewProjectParameters)
   */
  void newProject(String projectType, String projectName, NewProjectParameters params,
      AsyncCallback<UserProject> callback);

  /**
   * @see ProjectService#copyProject(long, String)
   */
  void copyProject(long oldProjectId, String newName, AsyncCallback<UserProject> callback);

  /**
   * @see ProjectService#deleteProject(long)
   */
  void deleteProject(long projectId, AsyncCallback<Void> callback);

  /**
   * @see ProjectService#getProjects()
   */
  void getProjects(AsyncCallback<long[]> callback);

  /**
   * @see ProjectService#getProjectInfos()
   */
  void getProjectInfos(AsyncCallback<List<UserProject>> callback);

  /**
   * @see ProjectService#getProject(long)
   */
  void getProject(long projectId, AsyncCallback<ProjectRootNode> callback);

  /**
   * @see ProjectService#loadProjectSettings(long)
   */
  void loadProjectSettings(long projectId, AsyncCallback<String> callback);

  /**
   * @see ProjectService#storeProjectSettings(long, String)
   */
  void storeProjectSettings(long projectId, String settings, AsyncCallback<Void> callback);

  /**
   * @see ProjectService#deleteFile(long, String)
   */
  void deleteFile(long projectId, String fileId, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#deleteFiles(long, String)
   */
  void deleteFiles(long projectId, String directory, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#load(long, String)
   */
  void load(long projectId, String fileId, AsyncCallback<String> callback);

  /**
   * @see ProjectService#loadraw(long, String)
   */
  void loadraw(long projectId, String fileId, AsyncCallback<byte []> callback);

  /**
   * @see ProjectService#load(List)
   */
  void load(List<FileDescriptor> files, AsyncCallback<List<FileDescriptorWithContent>> callback);

  /**
   * @see ProjectService#save(long, String, String)
   */
  void save(long projectId, String fileId, String source, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#save(List)
   */
  void save(List<FileDescriptorWithContent> filesAndContent, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#build(long, String)
   */
  void build(long projectId, String target, AsyncCallback<RpcResult> callback);

  /**
   * @see ProjectService#getBuildResult(long, String)
   */
  void getBuildResult(long projectId, String target, AsyncCallback<RpcResult> callback);

  /**
   * @see ProjectService#addFile(long, String)
   */
  void addFile(long projectId, String fileId, AsyncCallback<Long> callback);
}
