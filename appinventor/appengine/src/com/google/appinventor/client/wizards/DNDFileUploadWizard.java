package com.google.appinventor.client.wizards;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.FolderNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.moxieapps.gwt.uploader.client.File;
import org.moxieapps.gwt.uploader.client.Uploader;
import org.moxieapps.gwt.uploader.client.events.*;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * This Drag and Drop File Uploader uzes the GWT Uploader from Moxie Group.
 * Examples and documentation can be found here: http://www.moxiegroup.com/moxieapps/gwt-uploader/
 * Created by Aaron on 3/27/2016.
 */
public class DNDFileUploadWizard extends Wizard {
    private File currentFile;
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
    public DNDFileUploadWizard(FolderNode folderNode) {
        this(folderNode, null);
    }

    /**
     * Creates a new file upload wizard.
     *
     * @param folderNode the upload destination folder
     * @param fileUploadedCallback callback to be executed after upload
     */
    public DNDFileUploadWizard(final FolderNode folderNode,
                            final FileUploadedCallback fileUploadedCallback) {
        super(MESSAGES.fileUploadWizardCaption(), true, false);

        // Prepare File Queue UI
        final VerticalPanel queuedFilesPanel = new VerticalPanel();
        Label uploadQueueLabel = new Label("File to be Uploaded:");
        queuedFilesPanel.add(uploadQueueLabel);
        final Tree fileUploadQueue = new Tree();
        setStylePrimaryName("ode-DialogBox");

        // Initalize Uploader
        final Uploader uploader = new Uploader()
                .setButtonText("<span class='gwt-Button'>Select File...</span>")
                .setButtonWidth(150)
                .setButtonHeight(50)
                .setButtonCursor(Uploader.Cursor.HAND)
                .setButtonAction(Uploader.ButtonAction.SELECT_FILE); // Changes to "SELECT_FILES" for multi-upload
        uploader.setFileQueuedHandler(new FileQueuedHandler() {
            @Override
            public boolean onFileQueued(final FileQueuedEvent fileQueuedEvent) {
                currentFile = fileQueuedEvent.getFile();
                String fileName = shortenFilename(currentFile.getName());
                //If single file upload, clear tree. Otherwise, use isInTree() to replace redundant files
                fileUploadQueue.clear();
                TreeItem treeItem = new TreeItem(new HTML("<span>" + fileName + "</span>"));
                fileUploadQueue.addItem(treeItem);
                treeItem.setUserObject(currentFile); // associate File
                return true;
            }
        });
        uploader.setUploadErrorHandler(new UploadErrorHandler() {
            @Override
            public boolean onUploadError(UploadErrorEvent uploadErrorEvent) {
                switch (uploadErrorEvent.getErrorCode()) {
                    case UPLOAD_LIMIT_EXCEEDED:
                        ErrorReporter.reportInfo(MESSAGES.fileTooLargeError());
                        break;
                    default:
                        ErrorReporter.reportError(MESSAGES.fileUploadError());
                        Window.alert(uploadErrorEvent.getMessage());
                        break;
                }
                return true;
            }
        });
        uploader.setUploadSuccessHandler(new UploadSuccessHandler() {
            @Override
            public boolean onUploadSuccess(UploadSuccessEvent uploadSuccessEvent) {
                ErrorReporter.hide();
                Ode.getInstance().updateModificationDate(folderNode.getProjectId(), uploadSuccessEvent.getFile().getModificationDate().getTime());
                finishUpload(folderNode, currentFile.getName(), fileUploadedCallback);
                return true;
            }
        });
        uploader.setFilePostName(ServerLayout.UPLOAD_FILE_FORM_ELEMENT);

        // fileUploadQueue Selection Handler to remove items from queue
//        fileUploadQueue.addSelectionHandler(new SelectionHandler<TreeItem>() {
//            @Override
//            public void onSelection(SelectionEvent<TreeItem> selectionEvent) {
//                File file = (File) selectionEvent.getSelectedItem().getUserObject();
//                uploader.cancelUpload(file.getId(), false);
//                fileUploadQueue.removeItem(selectionEvent.getSelectedItem());
//            }
//        });

        // Prepare Uploader UI
        queuedFilesPanel.add(fileUploadQueue);
        VerticalPanel uploaderPanel = new VerticalPanel();
        uploaderPanel.setStyleName("dnd-fileUploaderPanel");
        uploaderPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        uploaderPanel.add(uploader);

        // Drag and Drop Area
        if (Uploader.isAjaxUploadWithProgressEventsSupported()) {
            final Label dropFilesLabel = new Label("Drag Your File Here");
            dropFilesLabel.setStyleName("dnd-dropFilesLabel");
            dropFilesLabel.addDragOverHandler(new DragOverHandler() {
                public void onDragOver(DragOverEvent event) {
                    if (!uploader.getButtonDisabled()) {
                        dropFilesLabel.addStyleName("dnd-dropFilesLabelHover");
                    }
                }
            });
            dropFilesLabel.addDragLeaveHandler(new DragLeaveHandler() {
                public void onDragLeave(DragLeaveEvent event) {
                    dropFilesLabel.removeStyleName("dnd-dropFilesLabelHover");
                }
            });
            dropFilesLabel.addDropHandler(new DropHandler() {
                public void onDrop(DropEvent event) {
                    dropFilesLabel.removeStyleName("dnd-dropFilesLabelHover");
                    uploader.addFilesToQueue(Uploader.getDroppedFiles(event.getNativeEvent()));
                    event.preventDefault();
                }
            });

            uploaderPanel.add(dropFilesLabel);
        }

        // Assemble final UI
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(uploaderPanel);
        panel.add(queuedFilesPanel);
        panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        panel.setCellHorizontalAlignment(uploader, HorizontalPanel.ALIGN_CENTER);
        panel.setCellHorizontalAlignment(fileUploadQueue, HorizontalPanel.ALIGN_RIGHT);

        addPage(panel);

        // Create finish command (upload a file)
        initFinishCommand(new Command() {
            @Override
            public void execute() {
                    String uploadFilename = currentFile.getName();
                    if (!uploadFilename.isEmpty()) {
                        final String filename = makeValidFilename(uploadFilename);
                        if (!TextValidators.isValidCharFilename(filename)) {
                            Window.alert(MESSAGES.malformedFilename());
                            return;
                        } else if (!TextValidators.isValidLengthFilename(filename)) {
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
                        String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.UPLOAD_SERVLET + "/" +
                                ServerLayout.UPLOAD_FILE + "/" + folderNode.getProjectId() + "/" +
                                folderNode.getFileId() + "/" + filename;
                        uploader.setUploadURL(uploadUrl).startUpload();
                    } else {
                        Window.alert(MESSAGES.noFileSelected());
                        new DNDFileUploadWizard(folderNode, fileUploadedCallback).show();
                    }
            }
        });
    }

    @Override
    public void show() {
        super.show();
        int width = 450;
        int height = 150;
        this.center();

        setPixelSize(width, height);
        super.setPagePanelHeight(150);
    }

    private String shortenFilename(String fileName) {
        if (fileName.length() > 20) {
            return fileName.substring(0, 8) + "..." + fileName.substring(fileName.length() - 9, fileName.length());
        } else {
            return fileName;
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

    private boolean isInTree(Tree tree, String treeItemName) {
        for (int i =0; i < tree.getItemCount(); i++){
            File file = (File) tree.getItem(i).getUserObject();
            if (file.getName()==treeItemName) {
                return true;
            }
        }
        return false;
    }

    private String lastPathComponent (String path) {
        String [] pieces = path.split("/");
        return pieces[pieces.length - 1];
    }

    private boolean confirmOverwrite(FolderNode folderNode, String newFile, String existingFile) {
        return Window.confirm(MESSAGES.confirmOverwrite(newFile, existingFile));
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
