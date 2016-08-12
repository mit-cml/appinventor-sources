// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.common.base.Strings;
import com.google.appinventor.server.storage.ObjectifyStorageIo;
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
    @Nullable String zipName,
    boolean includeYail,
    boolean includeScreenShots,
    boolean fatalError,
    boolean forGallery) throws IOException {
    // Download project source files as a zip.
    if (storageIo instanceof ObjectifyStorageIo) {
      return ((ObjectifyStorageIo)storageIo).exportProjectSourceZip(userId, projectId,
        includeProjectHistory, includeAndroidKeystore, zipName, includeYail, includeScreenShots, forGallery, fatalError);
    } else {
      throw new IllegalArgumentException("Objectify only");
    }
  }

  @Override
  public ProjectSourceZip exportAllProjectsSourceZip(String userId,
      String zipName) throws IOException {
    // Create a zip file for each project's sources.
    List<Long> projectIds = storageIo.getProjects(userId);
    if (projectIds.size() == 0) {
      throw new IllegalArgumentException("No projects to download");
    }

    ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
    ZipOutputStream out = new ZipOutputStream(zipFile);
    int count = 0;
    String metadata = "";
    for (Long projectId : projectIds) {
      try {
        // Note: We never include Yail files when exporting all source projects
        // even for Admins. If you are an admin and want to debug a project, download
        // it explicitly.
        ProjectSourceZip projectSourceZip =
          exportProjectSourceZip(userId, projectId, false, false, null, false, false, false, false);
        byte[] data = projectSourceZip.getContent();
        String name = projectSourceZip.getFileName();

        // If necessary, rename duplicate projects
        while (true) {
          try {
            out.putNextEntry(new ZipEntry(name));
            break;
          } catch (IOException e) {
            name = "duplicate-" + name;
          }
        }
        metadata += projectSourceZip.getMetadata() + "\n";

        out.write(data, 0, data.length);
        out.closeEntry();
        count++;
      } catch (IllegalArgumentException e) {
        System.err.println("No files found for userid: " + userId +
            " for projectid: " + projectId);
        continue;
      } catch (IOException e) {
        System.err.println("IOException while reading files found for userid: " +
            userId + " for projectid: " + projectId);
        continue;
      }
    }
    if (count == 0) {
      throw new IllegalArgumentException("No files to download");
    }

    List<String> userFiles = storageIo.getUserFiles(userId);
    if (userFiles.contains(StorageUtil.ANDROID_KEYSTORE_FILENAME)) {
      byte[] androidKeystoreBytes =
          storageIo.downloadRawUserFile(userId, StorageUtil.ANDROID_KEYSTORE_FILENAME);
      if (androidKeystoreBytes.length > 0) {
        out.putNextEntry(new ZipEntry(StorageUtil.ANDROID_KEYSTORE_FILENAME));
        out.write(androidKeystoreBytes, 0, androidKeystoreBytes.length);
        out.closeEntry();
        count++;
      }
    }

    out.close();

    // Package the big zip file up as a ProjectSourceZip and return it.
    byte[] content = zipFile.toByteArray();
    ProjectSourceZip projectSourceZip = new ProjectSourceZip(zipName, content, count);
    projectSourceZip.setMetadata(metadata);
    return projectSourceZip;
  }

  @Override
  public RawFile exportFile(String userId, long projectId, String filePath) throws IOException {
    // Download a specific project file.
    try {
      byte[] content = storageIo.downloadRawFile(userId, projectId, filePath);
      return new RawFile(StorageUtil.basename(filePath), content);
    } catch (RuntimeException e) {
      
      throw new RuntimeException("Error downloading project file: " + filePath
          + "user=" + userId + ", project=" + projectId, e);
    }
  }

  @Override
  public RawFile exportUserFile(String userId, String filePath) throws IOException {
    // Download a specific user file.
    try {
      byte[] content = storageIo.downloadRawUserFile(userId, filePath);
      return new RawFile(StorageUtil.basename(filePath), content);
    } catch (RuntimeException e) {
      throw new RuntimeException("Error downloading user file: " + filePath
          + "user=" + userId, e);
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
}
