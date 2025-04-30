// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.util.Base64Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
   * Send a project to the new Gallery
   *
   * @param userId the user id
   * @param projectId the project ID to send
   */
  public abstract RpcResult sendToGallery(String userId, long projectId);

  /**
   * loadFromGallery -- Load a project from the gallery
   *
   * @param userId the userId to load the project into
   * @param galleryId the unique gallery ID for this project
   */

  public abstract UserProject loadFromGallery(String userId, String galleryId) throws IOException;

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
    return storageIo.uploadRawFileForce(projectId, fileId, userId, new byte[0]);
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
   * Deletes all files and folders that are inside the given directory. The given directory itself is deleted.
   * @param userId the user Id
   * @param projectId project ID
   * @param directoy path of the directory
   */
  public long deleteFolder(String userId, long projectId, String directory) {
    // TODO(user) : This is also not efficient
    for (String fileId : storageIo.getProjectSourceFiles(userId, projectId)) {
      if (fileId.startsWith(directory)) {
        storageIo.deleteFile(userId, projectId, fileId);
        storageIo.removeSourceFilesFromProject(userId, projectId, false, fileId);
      }
    }
    return storageIo.getProjectDateCreated(userId, projectId);
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
   * This is a backwards compatible version that always sets force to true
   * Its primary purpose is to ease the release transition. People running an
   * older Ode at release time won't lose. At some point this function should go
   * away and save2 renamed back to save (through stages).
   *
   * @param userId the user id
   * @param projectId  project root node ID
   * @param fileId  project node whose source should be loaded
   * @param content  content to be saved
   * @return modification date for project
   *
   * @see com.google.appinventor.shared.rpc.project.ProjectService#save(String, long, String, String)
   */
  public long save(String userId, long projectId, String fileId, String content) {
    try {
      return save2(userId, projectId, fileId, true, content);
    } catch (BlocksTruncatedException e) {
      // Won't happen because it isn't thrown when the force argument is true
      // This is here just to keep the Java compiler happy. It isn't smart enough
      // to know that the exception won't be thrown in this case.
      return 0;
    }
  }

  /**
   * Saves the content of the file associated with a node in the project tree.
   * if force is false, an error is thrown if an attempt is made to save a
   * trivial (empty) blocks file workspace that had previously had contents.
   *
   * @param userId the user id
   * @param projectId  project root node ID
   * @param fileId  project node whose source should be loaded
   * @param content  content to be saved
   * @return modification date for project
   *
   * @see com.google.appinventor.shared.rpc.project.ProjectService#save(String, long, String, String)
   */
  public long save2(String userId, long projectId, String fileId, boolean force, String content) throws BlocksTruncatedException {
    if (force) {
      return storageIo.uploadFileForce(projectId, fileId, userId,
          content, StorageUtil.DEFAULT_CHARSET);
    } else {
      return storageIo.uploadFile(projectId, fileId, userId,
          content, StorageUtil.DEFAULT_CHARSET);
    }
  }

  /**
   * Saves a screenshot of a current blocks editor. This is called from the client side
   * whenever the user leaves a blocks editor. The data is shipped to us in base64 encoding
   * which we decode and then store in the project.
   *
   * @param userId user who owns the projectId
   * @param projectId project id for the project
   * @param fileId the filename to store the screenshot in
   * @param content the base64 encoded content
   */

  public RpcResult screenshot(String userId, long projectId, String fileId, String content) {
    byte [] binContent = Base64Util.decodeLines(content);
    try {
      storageIo.uploadRawFile(projectId, fileId, userId, true, binContent);
    } catch (BlocksTruncatedException e) {
      // should never happen because force is set to true
    }
    return RpcResult.createSuccessfulRpcResult("", "");
  }


  /**
   * Invokes a build command for the project.
   *
   * @param user the User that owns the {@code projectId}.
   * @param projectId  project id to be built
   * @param nonce -- random string used to find finished APK
   * @param target  build target (optional, implementation dependent)
   * @param secondBuildserver use second buildserver
   *
   * @return  build results
   */
  public abstract RpcResult build(User user, long projectId, String nonce, String target, boolean secondBuildserver, boolean isAab);

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

  public TextFile importMedia(String userId, long projectId, String urlString, boolean save) throws IOException {
    InputStream is = null;
    try {
      final int BUFSIZE = 4096;
      URL url = new URL(urlString);
      byte[] buffer = new byte[BUFSIZE];
      int read = 0;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      is = url.openStream();
      while ( ( read = is.read(buffer) ) > 0 ) {
        baos.write(buffer, 0, read);
      }
      byte[] bytes = baos.toByteArray();
      baos.flush();
      String filename = null;
      if (save) {
        String[] parts = url.getPath().split("/");
        filename = parts[parts.length - 1];
        if (filename.length() == 0) {
          throw new IOException("Cannot determine name when saving media resource.");
        }
        filename = "assets/" + filename;
        storageIo.uploadRawFileForce(projectId, filename, userId, bytes);
      }
      return new TextFile(filename, Base64Util.encodeLines(bytes));
    } catch(MalformedURLException e) {
      throw new IOException("Unable to import from malformed URL", e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch(IOException e) {
          // suppress error on close
        }
      }
    }
  }
}
