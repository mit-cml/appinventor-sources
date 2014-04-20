// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.project;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.util.Base64Util;

import java.util.List;

/**
 * The base class for classes that provide project services for a specific
 * project type.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class CommonProjectService {
  protected final String projectType;
  protected final StorageIo storageIo;

  protected CommonProjectService(String projectType, StorageIo storageIo) {
    this.projectType = projectType;
    this.storageIo = storageIo;
  }

  /**
   * Stores the project settings.
   *
   * @param userId the user id
   * @param projectId  project ID
   * @param settings  project settings
   */
  public void storeProjectSettings(String userId, long projectId, String settings) {
    storageIo.storeProjectSettings(userId, projectId, settings);
  }

  /**
   * Creates a new project.
   *
   * @param userId the user id
   * @param projectName project name
   * @param params optional parameters (project type dependent)
   *
   * @return new project ID
   */
  public abstract long newProject(String userId, String projectName, NewProjectParameters params);

  /**
   * Copies a project with a new name.
   *
   * @param userId the user id
   * @param oldProjectId  old project ID
   * @param newName new project name
   */
  public abstract long copyProject(String userId, long oldProjectId, String newName);

  /**
   * Deletes a project.
   *
   * @param userId the user id
   * @param projectId  project ID as received by
   */
  public void deleteProject(String userId, long projectId) {
    storageIo.deleteProject(userId, projectId);
  }

  /**
   * Returns the project root node for the requested project.
   *
   * @param userId the user id
   * @param projectId  project ID as received by {@link
   *                   com.google.appinventor.shared.rpc.project.ProjectService#getProjects()}
   *
   * @return  root node of project
   */
  public abstract ProjectRootNode getRootNode(String userId, long projectId);

  /**
   * Adds a file to the given project.
   *
   * @param userId the user id
   * @param projectId  project ID
   * @param fileId  ID of file to delete
   * @return modification date for project
   */
  public long addFile(String userId, long projectId, String fileId) {
    List<String> sourceFiles = storageIo.getProjectSourceFiles(userId, projectId);
    if (!sourceFiles.contains(fileId)) {
      storageIo.addSourceFilesToProject(userId, projectId, false, fileId);
    }
    return storageIo.uploadRawFile(projectId, fileId, userId, new byte[0]);
  }

  /**
   * Deletes a file in the given project.
   *
   * @param userId the user id
   * @param projectId  project ID
   * @param fileId  ID of file to delete
   * @return modification date for project
   */
  public long deleteFile(String userId, long projectId, String fileId) {
    final long date = storageIo.deleteFile(userId, projectId, fileId);
    storageIo.removeSourceFilesFromProject(userId, projectId, false, fileId);
    return date;
  }

  /**
   * Deletes all files that are contained directly in the given directory. Files
   * in sub-packages are not deleted.
   *
   * @param userId the user id
   * @param projectId project ID
   * @param directory path of the directory
   */
  public long deleteFiles(String userId, long projectId, String directory) {
    // TODO(user): This is not efficient.
    for (String fileId : storageIo.getProjectSourceFiles(userId, projectId)) {
      if (fileId.startsWith(directory + '/') && fileId.indexOf('/', directory.length() + 1) == -1) {
        storageIo.deleteFile(userId, projectId, fileId);
        storageIo.removeSourceFilesFromProject(userId, projectId, false, fileId);
      }
    }
    return storageIo.getProjectDateModified(userId, projectId);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value depends on the file kind. Source (text) files should
   * typically return their contents. Image files will be more likely to return
   * the URL that the browser can find them at.
   *
   * @param userId the user id
   * @param projectId  project root node ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  implementation dependent
   */
  public String load(String userId, long projectId, String fileId) {
    return storageIo.downloadFile(userId, projectId, fileId, StorageUtil.DEFAULT_CHARSET);
  }

  /**
   * Loads the file information associated with a node in the project tree. The
   * actual return value depends on the file kind. Source (text) files should
   * typically return their contents. Image files will be more likely to return
   * the URL that the browser can find them at.
   *
   * This version returns a ChecksumedLoadFile object which includes the file
   * content and a SHA-1 hash to validate file integrity accross the network.
   *
   * @param userId the user id
   * @param projectId  project root node ID
   * @param fileId  project node whose source should be loaded
   *
   * @return  ChecksumedLoadFile object
   */
  public ChecksumedLoadFile load2(String userId, long projectId, String fileId) throws ChecksumedFileException {
    ChecksumedLoadFile retval = new ChecksumedLoadFile();
    retval.setContent(storageIo.downloadFile(userId, projectId, fileId, StorageUtil.DEFAULT_CHARSET));
    return retval;
  }

  /**
   * Attempt to record the project Id and error message when we detect a corruption
   * while loading a project.
   *
   * @param userId user id
   * @param projectId project id
   * @param message Error message from the thrown exception
   *
   */
  public void recordCorruption(String userId, long projectId, String fileId, String message) {
    storageIo.recordCorruption(userId, projectId, fileId, message);
  }

  /**
   * Loads the raw content of the associated file.
   *
   * @param userId the userid
   * @param projectId the project root node ID
   * @param fileId project node whose content is to be downloaded
   * @return the file contents
   */

  public byte[] loadraw(String userId, long projectId, String fileId) {
    return storageIo.downloadRawFile(userId, projectId, fileId);
  }

  /**
   * Loads the raw content of the associated file, base 64 encodes it
   * and returns the resulting base64 encoded string.
   *
   * @param userId the userid
   * @param projectId the project root node ID
   * @param fileId project node whose content is to be downloaded
   * @param the file contents encoded in base64
   */
  public String loadraw2(String userId, long projectId, String fileId) {
    byte [] filedata = storageIo.downloadRawFile(userId, projectId, fileId);
    return Base64Util.encodeLines(filedata);
  }

  /**
   * Saves the content of the file associated with a node in the project tree.
   *
   * @param userId the user id
   * @param projectId  project root node ID
   * @param fileId  project node whose source should be loaded
   * @param content  content to be saved
   * @return modification date for project
   *
   * @see com.google.appinventor.shared.rpc.project.ProjectService#load(long, String)
   */
  public long save(String userId, long projectId, String fileId, String content) {
    return storageIo.uploadFile(projectId, fileId, userId,
        content, StorageUtil.DEFAULT_CHARSET);
  }

  /**
   * Invokes a build command for the project.
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param nonce -- random string used to find finished APK
   * @param target  build target (optional, implementation dependent)
   *
   * @return  build results
   */
  public abstract RpcResult build(User user, long projectId, String nonce, String target);

  /**
   * Gets the result of a build command for the project.
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param target  build target (optional, implementation dependent.
   * @return  build results.  The following values may be in RpcResult.result:
   *            0: Build is done and was successful
   *            1: Build is done and was unsuccessful
   *           -1: Build is not yet done.
   */
  public abstract RpcResult getBuildResult(User user, long projectId, String target);
}
