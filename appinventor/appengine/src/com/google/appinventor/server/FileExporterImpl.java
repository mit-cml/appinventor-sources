// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.common.base.Strings;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.storage.StorageUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;

/**
 * Implementation of {@link FileExporter} based on {@link StorageIo}
 *
 */
public final class FileExporterImpl implements FileExporter {

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
  private static final String ANDROID_KEYSTORE_FILENAME = "android.keystore";

  @Override
  public RawFile exportProjectOutputFile(String userId, long projectId, @Nullable String target)
      throws IOException {
    // Download project output file.
    List<String> files = storageIo.getProjectOutputFiles(userId, projectId);
    if (target != null) {
      // Target given - filter file list
      files = filterByFilePrefix(files, "build/" + target + '/');
    }

    // We expect the files List to contain:
    //   build/Android/<project>.apk
    //   build/Android/build.out
    //   build/Android/build.err
    // There should never be more than one .apk file.

    for (String fileName : files) {
      if (fileName.endsWith(".apk")) {
        byte[] content = storageIo.downloadRawFile(userId, projectId, fileName);
        return new RawFile(StorageUtil.basename(fileName), content);
      }
    }

    throw new IllegalArgumentException("No files to download");
  }

  @Override
  public ProjectSourceZip exportProjectSourceZip(String userId, long projectId,
                                                 boolean includeProjectHistory,
                                                 boolean includeAndroidKeystore,
                                                 @Nullable String zipName) throws IOException {
    // Download project source files as a zip.
    List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
    return createZipDownloadable(userId, files, projectId,
                                 includeProjectHistory, includeAndroidKeystore, zipName);
  }

  @Override
  public ProjectSourceZip exportAllProjectsSourceZip(String userId,
      String zipName) throws IOException {
    // Create a zip file for each project's sources.
    List<ProjectSourceZip> projectSourceZips = new ArrayList<ProjectSourceZip>();
    List<Long> projectIds = storageIo.getProjects(userId);
    if (projectIds.size() == 0) {
      throw new IllegalArgumentException("No projects to download");
    }
    for (Long projectId : projectIds) {
      projectSourceZips.add(exportProjectSourceZip(
          userId,
          projectId,
          false,   // includeProjectHistory
          false,   // includeAndroidKeystore
          null));  // zipName
    }

    // Create one big zip file containing each of the project sources zip files.
    ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
    ZipOutputStream out = new ZipOutputStream(zipFile);
    for (ProjectSourceZip projectSourceZip : projectSourceZips) {
      byte[] data = projectSourceZip.getContent();
      out.putNextEntry(new ZipEntry(projectSourceZip.getFileName()));
      out.write(data, 0, data.length);
      out.closeEntry();
    }
    out.close();

    // Package the big zip file up as a ProjectSourceZip and return it.
    byte[] content = zipFile.toByteArray();
    return new ProjectSourceZip(zipName, content, projectSourceZips.size());
  }

  @Override
  public RawFile exportFile(String userId, long projectId, String filePath) throws IOException {
    // Download a specific file.
    try {
      byte[] content = storageIo.downloadRawFile(userId, projectId, filePath);
      return new RawFile(StorageUtil.basename(filePath), content);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Unknown download file: " + filePath, e);
    }
  }

  /*
   * Filters a list of file names, removing those that don't start with the given prefix.
   */
  private List<String> filterByFilePrefix(List<String> files, String prefix) {
    List<String> filteredFiles = new ArrayList<String>();
    for (String file : files) {
      if (file.startsWith(prefix)) {
        filteredFiles.add(file);
      }
    }
    return filteredFiles;
  }

  private ProjectSourceZip createZipDownloadable(String userId, List<String> files,
      long projectId, boolean includeProjectHistory, boolean includeAndroidKeystore,
      @Nullable String zipName) throws IOException {
    if (files.size() == 0) {
      throw new IllegalArgumentException("No files to download");
    }

    int fileCount = 0;
    ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
    ZipOutputStream out = new ZipOutputStream(zipFile);
    for (String file : files) {
      if (file.equals(REMIX_INFORMATION_FILE_PATH)) {
        // Skip legacy remix history files that were previous stored with the project
        continue;
      }
      byte[] data = storageIo.downloadRawFile(userId, projectId, file);
      out.putNextEntry(new ZipEntry(file));
      out.write(data, 0, data.length);
      out.closeEntry();
      fileCount++;
    }
    if (includeProjectHistory) {
      String remixInfo = storageIo.getProjectHistory(userId, projectId);
      if (!Strings.isNullOrEmpty(remixInfo)) {
        byte[] data = remixInfo.getBytes(StorageUtil.DEFAULT_CHARSET);
        out.putNextEntry(new ZipEntry(REMIX_INFORMATION_FILE_PATH));
        out.write(data, 0, data.length);
        out.closeEntry();
        fileCount++;
      }
    }
    if (includeAndroidKeystore) {
      List<String> userFiles = storageIo.getUserFiles(userId);
      if (userFiles.contains(ANDROID_KEYSTORE_FILENAME)) {
        byte[] androidKeystoreBytes =
            storageIo.downloadRawUserFile(userId, ANDROID_KEYSTORE_FILENAME);
        if (androidKeystoreBytes.length > 0) {
          out.putNextEntry(new ZipEntry(ANDROID_KEYSTORE_FILENAME));
          out.write(androidKeystoreBytes, 0, androidKeystoreBytes.length);
          out.closeEntry();
          fileCount++;
        }
      }
    }
    out.close();

    byte[] content = zipFile.toByteArray();
    if (zipName == null) {
      String projectName = storageIo.getProjectName(userId, projectId);
      zipName = projectName + ".zip";
    }
    return new ProjectSourceZip(zipName, content, fileCount);
  }
}
