// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;

import java.util.Collection;
import java.util.logging.Logger;


/**
 * Wizard for uploading individual files.
 *
 */
public class FileUploadWizard {
  interface FileUploadWizardUiBinder extends UiBinder<Dialog, FileUploadWizard> {}

  private static final FileUploadWizard.FileUploadWizardUiBinder uibinder =
      GWT.create(FileUploadWizardUiBinder.class);

  @UiField Dialog uploadDialog;
  @UiField FileUpload upload;
  @UiField Button okButton;
  @UiField Button cancelButton;
  @UiField Button topInvisible;
  @UiField Button bottomInvisible;


  private final FolderNode folderNode;
  private final Collection<String> acceptableTypes;
  private final FileUploadedCallback fileUploadedCallback;

  /**
   * Interface for callback to execute after a file is uploaded.
   */
  public interface FileUploadedCallback {
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
  public FileUploadWizard(FolderNode folderNode, FileUploadedCallback fileUploadedCallback) {
    this(folderNode, null, fileUploadedCallback);
  }

  /**
   * Creates a new file upload wizard.
   *
   * @param folderNode the upload destination folder
   * @param acceptableTypes a collection of acceptable types, or null.
   * @param fileUploadedCallback callback to be executed after upload
   */

  public FileUploadWizard(final FolderNode folderNode,
      final Collection<String> acceptableTypes,
      final FileUploadedCallback fileUploadedCallback) {
    this.folderNode = folderNode;
    this.acceptableTypes = acceptableTypes;
    this.fileUploadedCallback = fileUploadedCallback;

    uibinder.createAndBindUi(this);

    if (this.acceptableTypes != null) {
      upload.getElement().setAttribute("accept", String.join(",", this.acceptableTypes));
    }
  }


  public void show() {
    uploadDialog.center();
    upload.setFocus(true);
  }

  @UiHandler("cancelButton")
  void cancelMove(ClickEvent e) {
    uploadDialog.hide();
  }

  @UiHandler("okButton")
  public void uploadFile(ClickEvent e) {
    String uploadFilename = upload.getFilename();
    uploadDialog.hide();
    if (!uploadFilename.isEmpty()) {
      final String filename = makeValidFilename(uploadFilename);
      if (!TextValidators.isValidCharFilename(filename)) {
        new FileUploadErrorDialog(MESSAGES.malformedFilenameTitle(), MESSAGES.malformedFilename(),
            FileUploadErrorCode.NO_FILE_SELECTED, folderNode, acceptableTypes, fileUploadedCallback);
        return;
      } else if (!TextValidators.isValidLengthFilename(filename)) {
        new FileUploadErrorDialog(MESSAGES.filenameBadSizeTitle(), MESSAGES.filenameBadSize(),
            FileUploadErrorCode.FILENAME_BAD_SIZE, folderNode, acceptableTypes, fileUploadedCallback);
        return;
      }
      int nameLength = uploadFilename.length();
      String fileEnd = uploadFilename.substring(nameLength - 4, nameLength);

      if (".aia".equals(fileEnd.toLowerCase())) {
        new FileUploadErrorDialog(MESSAGES.aiaMediaAssetTitle(), MESSAGES.aiaMediaAsset(),
            FileUploadErrorCode.AIA_MEDIA_ASSET, folderNode, acceptableTypes, fileUploadedCallback);
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
            String filesToClose[] = {node.getFileId()};
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
      String uploadUrl = ServerLayout.getModuleBaseURL() + ServerLayout.UPLOAD_SERVLET + "/" +
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
      new FileUploadErrorDialog(MESSAGES.noFileSelectedTitle(), MESSAGES.noFileSelected(),
          FileUploadErrorCode.NO_FILE_SELECTED, folderNode, acceptableTypes, fileUploadedCallback);
    }
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

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
    okButton.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
    upload.setFocus(true);
  }

}

