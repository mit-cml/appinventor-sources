// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.io.File;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.FolderNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Wizard for uploading individual files.
 *
 */
public class FileUploadWizard extends Wizard {
  /**
   * Interface for callback to execute after a file is uploaded.
   */
  public static interface FileUploadedCallback {
    /**
     * Will be invoked after a file is uploaded.
     *
     * @param folderNode the upload destination folder
     * @param fileNode the file just uploaded
     */
    void onFileUploaded(FolderNode folderNode, FileNode fileNode);
  }

  /**
   * Creates a new file upload wizard.
   *
   * @param folderNode the upload destination folder
   */
  public FileUploadWizard(FolderNode folderNode) {
    this(folderNode, null);
  }

  /**
   * Creates a new file upload wizard.
   *
   * @param folderNode the upload destination folder
   * @param fileUploadedCallback callback to be executed after upload
   */
  public FileUploadWizard(final FolderNode folderNode,
      final FileUploadedCallback fileUploadedCallback) {
    super(MESSAGES.fileUploadWizardCaption(), true, false);

    // Initialize UI
    final FileUpload upload = new FileUpload();
    upload.setName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);
    setStylePrimaryName("ode-DialogBox");
    VerticalPanel panel = new VerticalPanel();
    panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    panel.add(upload);
    addPage(panel);

    // Create finish command (upload a file)
    initFinishCommand(new Command() {
      @Override
      public void execute() {
        String uploadFilename = upload.getFilename();
        if (!uploadFilename.isEmpty()) {
          final String filename = makeValidFilename(uploadFilename);
          if(!TextValidators.isValidCharFilename(filename)){
            Window.alert(MESSAGES.malformedFilename());
            return;
          } else if (!TextValidators.isValidLengthFilename(filename)){
            Window.alert(MESSAGES.filenameBadSize());
            return;
          }
          String fn = conflictingExistingFile(folderNode, filename);
          if (fn != null && !confirmOverwrite(folderNode, fn, filename)) {
            return;
          } else {
            String fileId = folderNode.getFileId() + "/" + filename;
            // We delete all the conflicting files.
            for (ProjectNode child : folderNode.getChildren()) {
              if (fileId.equalsIgnoreCase(child.getFileId()) && !fileId.equals(child.getFileId())) {
                final ProjectNode node = child;
                String filesToClose [] = { node.getFileId()};
                Ode ode = Ode.getInstance();
                ode.getEditorManager().closeFileEditors(node.getProjectId(), filesToClose);
                ode.getProjectService().deleteFile(ode.getSessionId(),
                    node.getProjectId(), node.getFileId(),
                    new OdeAsyncCallback<Long>(
                        // message on failure
                        MESSAGES.deleteFileError()) {
                      @Override
                      public void onSuccess(Long date) {
                        Ode.getInstance().getProjectManager().getProject(node).deleteNode(node);
                        Ode.getInstance().updateModificationDate(node.getProjectId(), date);

                      }
                    });
              }
            }
          }
          ErrorReporter.reportInfo(MESSAGES.fileUploadingMessage(filename));

          // Use the folderNode's project id and file id in the upload URL so that the file is
          // uploaded into that project and that folder in our back-end storage.
          String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.UPLOAD_SERVLET + "/" +
              ServerLayout.UPLOAD_FILE + "/" + folderNode.getProjectId() + "/" +
              folderNode.getFileId() + "/" + filename;
          Uploader.getInstance().upload(upload, uploadUrl,
              new OdeAsyncCallback<UploadResponse>(MESSAGES.fileUploadError()) {
            @Override
            public void onSuccess(UploadResponse uploadResponse) {
              switch (uploadResponse.getStatus()) {
              case SUCCESS:
                ErrorReporter.hide();
                onUploadSuccess(folderNode, filename, uploadResponse.getModificationDate(),
                    fileUploadedCallback);
                break;
              case FILE_TOO_LARGE:
                // The user can resolve the problem by
                // uploading a smaller file.
                ErrorReporter.reportInfo(MESSAGES.fileTooLargeError());
                break;
              default:
                ErrorReporter.reportError(MESSAGES.fileUploadError());
                break;
              }
            }
          });
        } else {
          Window.alert(MESSAGES.noFileSelected());
          new FileUploadWizard(folderNode, fileUploadedCallback).show();
        }
      }
    });
  }

  @Override
  public void show() {
    super.show();
    int width = 320;
    int height = 40;
    this.center();

    setPixelSize(width, height);
    super.setPagePanelHeight(40);
  }

  private String makeValidFilename(String uploadFilename) {
    // Strip leading path off filename.
    // We need to support both Unix ('/') and Windows ('\\') separators.
    String filename = uploadFilename.substring(
        Math.max(uploadFilename.lastIndexOf('/'), uploadFilename.lastIndexOf('\\')) + 1);
    // We need to strip out whitespace from the filename.
    filename = filename.replaceAll("\\s", "");
    return filename;
  }

  private String conflictingExistingFile(FolderNode folderNode, String filename) {
    String fileId = folderNode.getFileId() + "/" + filename;
    for (ProjectNode child : folderNode.getChildren()) {
      if (fileId.equalsIgnoreCase(child.getFileId())) {
        // we want to return kitty.png rather than assets/kitty.png
        return lastPathComponent(child.getFileId());
      }
    }
    return null;
  }

  private String lastPathComponent (String path) {
    String [] pieces = path.split("/");
    return pieces[pieces.length - 1];
  }

  private boolean confirmOverwrite(FolderNode folderNode, String newFile, String existingFile) {
    return Window.confirm(MESSAGES.confirmOverwrite(newFile, existingFile));
  }

  private void onUploadSuccess(final FolderNode folderNode, final String filename,
      long modificationDate, final FileUploadedCallback fileUploadedCallback) {
    Ode.getInstance().updateModificationDate(folderNode.getProjectId(), modificationDate);
    finishUpload(folderNode, filename, fileUploadedCallback);
  }

  private void finishUpload(FolderNode folderNode, String filename,
      FileUploadedCallback fileUploadedCallback) {
    String uploadedFileId = folderNode.getFileId() + "/" + filename;
    FileNode uploadedFileNode;
    if (folderNode instanceof YoungAndroidAssetsFolder) {
      uploadedFileNode = new YoungAndroidAssetNode(filename, uploadedFileId);
    } else {
      uploadedFileNode = new FileNode(filename, uploadedFileId);
    }

    Project project = Ode.getInstance().getProjectManager().getProject(folderNode);
    uploadedFileNode = (FileNode) project.addNode(folderNode, uploadedFileNode);

    if (fileUploadedCallback != null) {
      fileUploadedCallback.onFileUploaded(folderNode, uploadedFileNode);
    }
  }
}
