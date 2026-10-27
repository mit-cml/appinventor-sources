// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.SRC_FOLDER;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.project.youngandroid.YoungAndroidSettingsBuilder;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.ZipSigner;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;

/**
 * Implementation of {@link FileImporter} based on {@link StorageIo}
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class FileImporterImpl implements FileImporter {

  // Maximum size of an uploaded asset, in megabytes.
  private static final Flag<Float> maxAssetSizeMegs = Flag.createFlag("max.asset.size.megs", 15f);
  private static final Flag<String> verifyKey = Flag.createFlag("aia.verify_key", "");

  private static final Logger LOG = Logger.getLogger(FileImporterImpl.class.getName());

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  public UserProject importProject(String userId, String projectName,
      InputStream uploadedFileStream) throws FileImporterException, IOException {
    return importProject(userId, projectName, uploadedFileStream, null);
  }

  @Override
  public UserProject importProject(String userId, String projectName,
                                   InputStream uploadedFileStream, @Nullable String projectHistory)
      throws FileImporterException, IOException {
    // The projectName parameter has already been validated, including checking for an
    // existing project with the same name. (See TextValidators.checkNewProjectName).

    // Begin creating the project.
    Project project = new Project(projectName);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);

    // As we process the ZipEntry for each file, we'll adjust the directory structure so that it is
    // appropriate for this user.
    // Here we get the information (such as the qualified form name) that we'll need to do that.
    String qualifiedFormName = StringUtils.getQualifiedFormName(
        storageIo.getUser(userId).getUserEmail(), projectName);
    String srcDirectory = YoungAndroidProjectService.getSourceDirectory(qualifiedFormName);
    String projectSettings = new YoungAndroidSettingsBuilder().build();

    int sigResult = -1;         // Default value if no verify key
    ZipInputStream zin;
    if (verifyKey.get().isEmpty()) {
      zin = new ZipInputStream(uploadedFileStream);
    } else {
      byte [] zipBinary = uploadedFileStream.readAllBytes();
      sigResult = ZipSigner.verifyZip(zipBinary, verifyKey.get());
      zin = new ZipInputStream(new ByteArrayInputStream(zipBinary));
    }
    boolean isProjectArchive = false;  // have we found at least one project properties file?
    String lastOpened = "Screen1";
    try {
      // Extract files
      while (true) {
        ZipEntry entry;
        try {
          entry = zin.getNextEntry();
          if (entry == null) {
            break;
          }
        } catch (ZipException e) {
          // The uploaded file is not a valid zip file
          LOG.log(Level.SEVERE, "Invalid Project Archive Format", e);
          throw new FileImporterException(UploadResponse.Status.NOT_PROJECT_ARCHIVE);
        }

        if (!entry.isDirectory()) {
          String fileName = entry.getName();

          if (fileName.equals(YoungAndroidProjectService.PROJECT_PROPERTIES_FILE_NAME)) {
            // The content for the youngandroidproject/project.properties file must be regenerated
            // so that it contains the correct entries for "main" and "name", which are dependent on
            // the projectName and qualifiedFormName.

            Properties props = new Properties();
            props.load(zin);
            lastOpened = props.getProperty("lastopened", "Screen1");
            String lastStatus = props.getProperty("Status", "unknown");
            String newStatus = determineStatus(lastStatus, sigResult);
            String projectColors = props.getProperty("projectcolors", "{}");
            YoungAndroidSettingsBuilder settingsBuilder =
                new YoungAndroidSettingsBuilder()
                    .setProjectName(projectName)
                    .setQualifiedFormName(qualifiedFormName)
                    .setDefaultLastOpened(lastOpened)
                    .setStatus(newStatus)
                    .setProjectColors(projectColors);
            String content = settingsBuilder.toProperties();
            projectSettings = settingsBuilder.build();
            project.addTextFile(new TextFile(fileName, content));
            isProjectArchive = true;

          } else if (fileName.equals(FileExporter.REMIX_INFORMATION_FILE_PATH) ||
              fileName.equals(StorageUtil.ANDROID_KEYSTORE_FILENAME)) {
            // If the remix information file is present, we ignore it. In the past, a remix
            // information file was saved in the zip when project source was downloaded and
            // retrieved from the zip when it was uploaded. However, we no longer do that because
            // we don't have a way to verify that the contents of the remix information file is
            // accurate during the upload.
            // If a keystore file is present we ignore that too for now, since
            // we don't have per-project keystores. The only way to get such a
            // source zip at the moment is using the admin functionality to
            // download another user's project source.
            continue;

          } else if (fileName.startsWith("build")) {
            // Don't import aab, apk and build.out files!
            continue;
          } else {

            if (fileName.startsWith(SRC_FOLDER)) {
              // For files within the src folder, we need to update the directory that we put files
              // in. Adjust the fileName so that it corresponds to this project's package.
              fileName = srcDirectory + '/' + StorageUtil.basename(fileName);
            }

            // Get the file content from the ZipEntry.
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            ByteStreams.copy(zin, contentStream);

            project.addRawFile(new RawFile(fileName, contentStream.toByteArray()));
          }
        }
      }
    } finally {
      zin.close();
    }

    if (!isProjectArchive) {
      // The uploaded file seems to be a valid zip file, but it doesn't contain the project
      // properties file.
      throw new FileImporterException(UploadResponse.Status.NOT_PROJECT_ARCHIVE);
    }

    // Set project history if provided
    if (projectHistory != null) {
      project.setProjectHistory(projectHistory);
    }
    long projectId = storageIo.createProject(userId, project, projectSettings);
    return storageIo.getUserProject(userId, projectId);
  }

  @VisibleForTesting
  public long importFile(String userId, long projectId, String fileName,
      InputStream uploadedFileStream) throws FileImporterException, IOException {
    int maxAssetSizeBytes = (int) (maxAssetSizeMegs.get() * 1024 * 1024);
    int maxSizeBytes = Math.min(maxAssetSizeBytes, storageIo.getMaxJobSizeBytes());

    BufferedInputStream bis = new BufferedInputStream(uploadedFileStream);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    BufferedOutputStream bos = new BufferedOutputStream(os);

    // Alledgedly since it is buffered, reading in units of one byte
    // should be as fast as many bytes, but we can always adjust this.
    int bytes = 0;
    long fileLength = 0;
    byte[] buffer = new byte[1];
    while ((bytes = bis.read(buffer, 0, buffer.length)) != -1) {
      bos.write(buffer, 0, bytes);
      fileLength += bytes;
      if (fileLength > maxSizeBytes) {
        // Read the rest of the stream, but throw it away
        // and throw an error, so we do not consume memory storing
        // a large object
        while((bytes = bis.read(buffer, 0, buffer.length)) != -1) {
        }
        throw new FileImporterException(UploadResponse.Status.FILE_TOO_LARGE);
      }
    }
    bos.flush();

    byte[] content = os.toByteArray();

    // If the file already exists, we will overwrite the content.
    List<String> sourceFiles = storageIo.getProjectSourceFiles(userId, projectId);
    if (!sourceFiles.contains(fileName)) {
      storageIo.addSourceFilesToProject(userId, projectId, false, fileName);
    }
    return storageIo.uploadRawFileForce(projectId, fileName, userId, content);
  }

  @Override
  public void importUserFile(String userId, String fileName, InputStream uploadedFileStream)
      throws IOException {
    byte[] content = ByteStreams.toByteArray(uploadedFileStream);

    // If the file already exists, we will overwrite the content.
    List<String> userFiles = storageIo.getUserFiles(userId);
    if (!userFiles.contains(fileName)) {
      storageIo.addFilesToUser(userId, fileName);
    }
    storageIo.uploadRawUserFile(userId, fileName, content);
  }

  @Override
  public String importTempFile(InputStream inStream) throws IOException {
    return storageIo.uploadTempFile(ByteStreams.toByteArray(inStream));
  }

  @Override
  public Set<String> getProjectNames(final String userId) {
    List<Long> projectIds = storageIo.getProjects(userId);
    Iterable<String> names = Iterables.transform(projectIds, new Function<Long, String>() {
      @Override
      public String apply(Long projectId) {
        return storageIo.getProjectName(userId, projectId);
      }
    });
    return ImmutableSet.copyOf(names);
  }

  // Determine new status based on stored status and whether or not
  // the signature on the imported AIA file is present, valid or invalid

  ////////////////////////////////////////////////////////////////////////////
  // Status can be one of "valid", "invalid" or "unknown." Unknown pretty   //
  // much means that this is a new project or a project loaded from a       //
  // non-signed template. Exporting it will give it a valid signature,      //
  // though the status will still be "known." "Valid" means that when the   //
  // project was imported, it had a valid signature. "Invalid" means that a //
  // broken signature was found. This status is sticky, in the sense that   //
  // once a project is marked invalid, it will stay that way, even if       //
  // imported with a valid signature.                                       //
  //                                                                        //
  // Note: this may change in the future...                                 //
  ////////////////////////////////////////////////////////////////////////////


  private String determineStatus(String oldStatus, int sigResult) {
    switch(sigResult) {
      case 0:                  // Valid Signature
        if (oldStatus.equals("unknown") || oldStatus.equals("valid")) {
          return "valid";
        } else {
          return oldStatus;
        }
      case 1:                   // Invalid Signature
        return "invalid";
      default:
        return oldStatus;
    }
  }

}
