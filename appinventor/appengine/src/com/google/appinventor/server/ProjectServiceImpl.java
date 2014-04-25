// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;

import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.server.project.CommonProjectService;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.InvalidSessionException;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.FileDescriptor;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectService;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.logging.Logger;

/**
 * The implementation of the RPC service which runs on the server.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class ProjectServiceImpl extends OdeRemoteServiceServlet implements ProjectService {

  private static final Logger LOG = Logger.getLogger(ProjectServiceImpl.class.getName());

  private static final long serialVersionUID = -8316312003804169166L;

  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  // RPC implementation for YoungAndroid projects
  private final transient YoungAndroidProjectService youngAndroidProject =
      new YoungAndroidProjectService(storageIo);

  /**
   * Creates a new project.
   * @param projectType  type of new project
   * @param projectName  name of new project
   * @param params  optional parameter (project type dependent)
   *
   * @return  a {@link UserProject} for new project
   */
  @Override
  public UserProject newProject(String projectType, String projectName,
                                NewProjectParameters params) {
    final String userId = userInfoProvider.getUserId();
    long projectId = getProjectRpcImpl(userId, projectType).
        newProject(userId, projectName, params);
    return makeUserProject(userId, projectId);
  }

  /**
   * Copies a project with a new name.
   * @param oldProjectId  old project ID
   * @param newName new project name
   *
   * @return  a {@link UserProject} for new project
   */
  @Override
  public UserProject copyProject(long oldProjectId, String newName){
    final String userId = userInfoProvider.getUserId();
    long projectId = getProjectRpcImpl(userId, oldProjectId).
        copyProject(userId, oldProjectId, newName);
    return makeUserProject(userId, projectId);
  }

  /**
   * Deletes a project.
   * @param projectId  project ID
   */
  @Override
  public void deleteProject(long projectId) {
    final String userId = userInfoProvider.getUserId();
    getProjectRpcImpl(userId, projectId).deleteProject(userId, projectId);
  }

  /**
   * Returns an array with project IDs.
   *
   * @return  IDs of projects found by the back-end
   */
  @Override
  public long[] getProjects() {
    List<Long> projects = storageIo.getProjects(userInfoProvider.getUserId());
    long[] projectIds = new long[projects.size()];
    int i = 0;
    for (Long project : projects) {
      projectIds[i++] = project;
    }
    return projectIds;
  }

  /**
   * Returns a list with pairs of project id and name.
   *
   * @return list of pairs of project IDs names found by backend
   */
  @Override
  public List<UserProject> getProjectInfos() {
    String userId = userInfoProvider.getUserId();
    List<Long> projectIds = storageIo.getProjects(userId);
    List<UserProject> projectInfos = Lists.newArrayListWithExpectedSize(projectIds.size());
    for (Long projectId : projectIds) {
      projectInfos.add(makeUserProject(userId, projectId));
    }
    return projectInfos;
  }

  /**
   * Returns the root node for the given project.
   * @param projectId  project ID as received by {@link #getProjects()}
   *
   * @return  root node of project
   */
  @Override
  public ProjectRootNode getProject(long projectId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).getRootNode(userId, projectId);
  }

  /**
   * Returns a string with the project settings.
   * @param projectId  project ID
   *
   * @return  settings
   */
  @Override
  public String loadProjectSettings(long projectId) {
    String userId = userInfoProvider.getUserId();
    return storageIo.loadProjectSettings(userId, projectId);
  }

  /**
   * Stores a string with the project settings.
   * @param sessionId session id
   * @param projectId  project ID
   * @param settings  project settings
   */
  @Override
  public void storeProjectSettings(String sessionId, long projectId, String settings) throws InvalidSessionException {
    validateSessionId(sessionId);
    String userId = userInfoProvider.getUserId();
    getProjectRpcImpl(userId, projectId).storeProjectSettings(userId, projectId, settings);
  }

  /**
   * Deletes a file in the given project.
   * @param sessionId session id
   * @param projectId  project ID
   * @param fileId  ID of file to delete
   * @return modification date for project
   */
  @Override
  public long deleteFile(String sessionId, long projectId, String fileId) throws InvalidSessionException {
    validateSessionId(sessionId);
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).deleteFile(userId, projectId, fileId);
  }

  /**
   * Deletes all files that are contained directly in the given directory. Files
   * in subdirectories are not deleted.
   * @param sessionId session id
   * @param projectId project ID
   * @param directory path of the directory
   * @return modification date for project
   */
  @Override
  public long deleteFiles(String sessionId, long projectId, String directory) throws InvalidSessionException {
    validateSessionId(sessionId);
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).deleteFiles(userId, projectId,
        directory);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value depends on the file kind. Source (text) files should
   * typically return their contents. Image files will be more likely to return
   * the URL that the browser can find them at.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  implementation dependent
   */
  @Override
  public String load(long projectId, String fileId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).load(userId, projectId, fileId);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value depends on the file kind. Source (text) files should
   * typically return their contents. Image files will be more likely to return
   * the URL that the browser can find them at.
   *
   * This version returns a ChecksumedLoadFile which contains the file content
   * and a MD5 checksum.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  implementation dependent
   */
  @Override
  public ChecksumedLoadFile load2(long projectId, String fileId) throws ChecksumedFileException {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).load2(userId, projectId, fileId);
  }

  /**
   * Attempt to record the project Id and error message when we detect a corruption
   * while loading a project.
   *
   * @param projectId project id
   * @param message Error message from the thrown exception
   *
   */
  @Override
  public void recordCorruption(long projectId, String fileId, String message) {
    final String userId = userInfoProvider.getUserId();
    getProjectRpcImpl(userId, projectId).recordCorruption(userId, projectId, fileId, message);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value is the raw file contents.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  raw file content
   */
  @Override
  public byte [] loadraw(long projectId, String fileId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).loadraw(userId, projectId, fileId);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value is the raw file contents encoded as base64.
   *
   * @param projectId  project ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  raw file content as base 64
   */
  @Override
  public String loadraw2(long projectId, String fileId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).loadraw2(userId, projectId, fileId);
  }

  /**
   * Loads the contents of multiple files.
   *
   * @param files  list containing file descriptor of files to be loaded
   * @return  list containing file descriptors and their associated content
   */
  @Override
  public List<FileDescriptorWithContent> load(List<FileDescriptor> files) {
    List<FileDescriptorWithContent> result = Lists.newArrayList();
    final String userId = userInfoProvider.getUserId();
    for (FileDescriptor file : files) {
      long projectId = file.getProjectId();
      String fileId = file.getFileId();
      result.add(new FileDescriptorWithContent(
          projectId, fileId,
          getProjectRpcImpl(userId, projectId).load(userId, projectId, fileId)));
    }
    return result;
  }

  /**
   * Saves the content of the file associated with a node in the project tree.
   *
   * @param sessionId session id
   * @param projectId  project ID
   * @param fileId  project node whose source should be saved
   * @param content  content to be saved
   * @return modification date for project
   *
   * @see #load(long, String)
   */
  @Override
  public long save(String sessionId, long projectId, String fileId, String content) throws InvalidSessionException {
    validateSessionId(sessionId);
    // Log parameters except for content
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).save(userId, projectId, fileId,
        content);
  }

  /**
   * Saves the content of the file associated with a node in the project tree.
   * This version takes a "force" argument which if false will result in an
   * exception of a trivial (empty) blocks workspace is attempted to be saved
   *
   * @param sessionId session id
   * @param projectId  project ID
   * @param fileId  project node whose source should be saved
   * @param force whether to write an empty blocks workspace
   * @param content  content to be saved
   * @return modification date for project
   *
   * @see #load(long, String)
   */
  @Override
  public long save2(String sessionId, long projectId, String fileId, boolean force, String content) throws InvalidSessionException,
      BlocksTruncatedException {
    validateSessionId(sessionId);
    // Log parameters except for content
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).save2(userId, projectId, fileId, force,
        content);
  }

  /**
   * Saves the contents of multiple files.
   *
   * @param sessionId session id
   * @param filesAndContent  list containing file descriptors and their
   *                         associated content
   * @return modification date for last modified project of list
   */
  @Override
  public long save(String sessionId, List<FileDescriptorWithContent> filesAndContent) throws InvalidSessionException,
      BlocksTruncatedException {
    validateSessionId(sessionId);
    final String userId = userInfoProvider.getUserId();
    long date = 0;
    for (FileDescriptorWithContent fileAndContent : filesAndContent) {
     long projectId = fileAndContent.getProjectId();
     date = getProjectRpcImpl(userId, projectId).
         save(userId, projectId, fileAndContent.getFileId(), fileAndContent.getContent());
    }
    return date;
  }

  /**
   * Invokes a build command for the project on the back-end.
   *
   * @param projectId  project ID
   * @param target  build target (optional, implementation dependent)
   *
   * @return  results of build
   */
  @Override
  public RpcResult build(long projectId, String nonce, String target) {
    // Dispatch
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).build(
      userInfoProvider.getUser(), projectId, nonce, target);
  }

  /**
   * Gets the result of a build command for the project.
   *
   * @param projectId  project ID
   * @param target  build target (optional, implementation dependent)
   *
   * @return  results of build. The following values may be in RpcResult.result:
   *            0: Build is done and was successful
   *            1: Build is done and was unsuccessful
   *           -1: Build is not yet done.
   */
  @Override
  public RpcResult getBuildResult(long projectId, String target) {
    // Dispatch
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).getBuildResult(
        userInfoProvider.getUser(), projectId, target);
  }

  /*
   * Write the serialized response out to stdout. This is a very unusual thing
   * to do, but it allows us to create a static file version of the response
   * without deploying a servlet.
   *
   * Commented out by JIS 11/12/13
   */
  @Override
  protected void onAfterResponseSerialized(String serializedResponse) {
    // System.out.println(serializedResponse);  // COV_NF_LINE
  }

  private UserProject makeUserProject(String userId, long projectId) {
    return storageIo.getUserProject(userId, projectId);
  }

  /*
   * Returns the RPC implementation for the given project type.
   */
  private CommonProjectService getProjectRpcImpl(final String userId, long projectId) {
    String projectType = storageIo.getProjectType(userId, projectId);
    if (!projectType.isEmpty()) {
      return getProjectRpcImpl(userId, projectType);
    } else {
      throw CrashReport.createAndLogError(LOG, getThreadLocalRequest(),
          "user=" + userId + ", project=" + projectId,
          new IllegalArgumentException("Can't find project " + projectId));
    }
  }

  private CommonProjectService getProjectRpcImpl(final String userId, String projectType) {
    if (projectType.equals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE)) {
      return youngAndroidProject;
    } else {
      throw CrashReport.createAndLogError(LOG, getThreadLocalRequest(), null,
          new IllegalArgumentException("Unknown project type:" + projectType));
    }
  }

  @Override
  public long addFile(long projectId, String fileId) {
    final String userId = userInfoProvider.getUserId();
    return getProjectRpcImpl(userId, projectId).addFile(userId, projectId, fileId);
  }

  @Override
  public void log(String message) {
    LOG.warning(message);
  }

  private void validateSessionId(String sessionId) throws InvalidSessionException {
    String storedSessionId = userInfoProvider.getSessionId();
    if (storedSessionId == null) {
      LOG.info("storedSessionId is null");
    } else {
      LOG.info("storedSessionId = " + storedSessionId);
    }
    if (sessionId == null) {
      LOG.info("sessionId is null");
    } else {
      LOG.info("sessionId = " + sessionId);
    }
    if (sessionId.equals("force")) { // If we are forcing our way -- no check
      return;
    }
    if (!storedSessionId.equals(sessionId))
      if (AppInventorFeatures.requireOneLogin()) {
        throw new InvalidSessionException("A more recent login has occurred since we started. No further changes will be saved.");
      }
  }

}
