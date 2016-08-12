// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
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
   * @see ProjectService#newProjectFromTemplate(String, String, NewProjectParameters, String)
   */
  void newProjectFromTemplate(String projectName, String pathToZip, AsyncCallback<UserProject> callback);

  /**
   * @see ProjectService#newProjectFromExternalTemplate(String, String)
   */
  void newProjectFromExternalTemplate(String projectName, String zipData, AsyncCallback<UserProject> callback);

  /**
   * @see ProjectService#retrieveTemplateData(String)
   */
  void retrieveTemplateData(String pathToTemplates, AsyncCallback<String> callback);

  /**
   * @see ProjectService#copyProject(long, String)
   */
  void copyProject(long oldProjectId, String newName, AsyncCallback<UserProject> callback);

  /**
   * @see ProjectService#deleteProject(long)
   */
  void deleteProject(long projectId, AsyncCallback<Void> callback);

   /**
   * @see ProjectService#setGalleryid
   */
  void setGalleryId(long projectId, long galleryId, AsyncCallback<java.lang.Void> callback);

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
   * @see ProjectService#storeProjectSettings(String, long, String)
   */
  void storeProjectSettings(String sessionId, long projectId, String settings, AsyncCallback<Void> callback);

  /**
   * @see ProjectService#deleteFile(String, long, String)
   */
  void deleteFile(String sessionId, long projectId, String fileId, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#deleteFiles(String, long, String)
   */
  void deleteFiles(String sessionId, long projectId, String directory, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#deleteFolder(String, long, String)
   */
  void deleteFolder(String sessionId, long projectId, String directory, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#load(long, String)
   */
  void load(long projectId, String fileId, AsyncCallback<String> callback);

  /**
   * @see ProjectService#load2(long, String)
   */
  void load2(long projectId, String fileId, AsyncCallback<ChecksumedLoadFile> callback);

  /**
   * @see ProjectService#recordCorruption(long, String, String)
   */
  void recordCorruption(long ProjectId, String fileId, String message, AsyncCallback<Void> callback);

  /**
   * @see ProjectService#loadraw(long, String)
   */
  void loadraw(long projectId, String fileId, AsyncCallback<byte []> callback);

  /**
   * @see ProjectService#loadraw2(long, String)
   */
  void loadraw2(long projectId, String fileId, AsyncCallback<String> callback);

  /**
   * @see ProjectService#load(List)
   */
  void load(List<FileDescriptor> files, AsyncCallback<List<FileDescriptorWithContent>> callback);

  /**
   * @see ProjectService#save(String, long, String, String)
   */
  void save(String sessionId, long projectId, String fileId, String source, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#save2(String, long, String, String)
   */
  void save2(String sessionId, long projectId, String fileId, boolean force, String source, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#save(String, List)
   */
  void save(String sessionId, List<FileDescriptorWithContent> filesAndContent, AsyncCallback<Long> callback);

  /**
   * @see ProjectService#screnshot(String, long, String, String)
   */

  void screenshot(String sessionId, long projectId, String fileId, String content,
    AsyncCallback<RpcResult> callback);

  /**
   * @see ProjectService#build(long, String, String)
   */
  void build(long projectId, String nonce, String target, AsyncCallback<RpcResult> callback);

  /**
   * @see ProjectService#getBuildResult(long, String)
   */
  void getBuildResult(long projectId, String target, AsyncCallback<RpcResult> callback);

  /**
   * @see ProjectService#addFile(long, String)
   */
  void addFile(long projectId, String fileId, AsyncCallback<Long> callback);

  void newProjectFromGallery(String appName, String aiaPath, long attributionId, AsyncCallback<UserProject> callback);

  /**
   * @see ProjectService#log(String)
   */
  void log(String message, AsyncCallback<Void> callback);

}
