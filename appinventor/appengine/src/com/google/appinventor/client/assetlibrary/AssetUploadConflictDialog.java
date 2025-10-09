package com.google.appinventor.client.assetlibrary;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.appinventor.shared.rpc.project.GlobalAsset;
import java.util.List;

/**
 * Dialog displayed when user uploads an asset that already exists.
 * Provides options for handling the conflict with clear impact preview.
 */
public class AssetUploadConflictDialog extends DialogBox {
    
    public enum ConflictResolution {
        REPLACE_EXISTING,
        CREATE_NEW_ASSET,
        SAVE_AS_DRAFT
    }
    
    public interface ConflictResolutionCallback {
        void onResolutionSelected(ConflictResolution resolution, String newAssetName, boolean notifyProjects);
    }
    
    private final String assetName;
    private final GlobalAsset existingAsset;
    private final List<String> affectedProjects;
    private final ConflictResolutionCallback callback;
    
    // UI Components
    private RadioButton replaceOption;
    private RadioButton createNewOption;
    private RadioButton draftOption;
    private TextBox newNameBox;
    private CheckBox notifyCheckBox;
    private Label impactLabel;
    private Button confirmButton;
    private Button cancelButton;
    
    public AssetUploadConflictDialog(String assetName, GlobalAsset existingAsset, 
                                   List<String> affectedProjects, ConflictResolutionCallback callback) {
        super(false, true); // non-auto-hide, modal
        this.assetName = assetName;
        this.existingAsset = existingAsset;
        this.affectedProjects = affectedProjects;
        this.callback = callback;
        
        initializeDialog();
        setupEventHandlers();
        updateImpactPreview();
    }
    
    private void initializeDialog() {
        setText("Asset Already Exists");
        setStyleName("asset-conflict-dialog");
        
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setStyleName("conflict-dialog-content");
        mainPanel.setSpacing(12);
        
        // Header with asset info
        mainPanel.add(createHeaderPanel());
        
        // Conflict resolution options
        mainPanel.add(createOptionsPanel());
        
        // Impact preview
        mainPanel.add(createImpactPanel());
        
        // Action buttons
        mainPanel.add(createButtonPanel());
        
        setWidget(mainPanel);
        center();
    }
    
    private Widget createHeaderPanel() {
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setStyleName("conflict-header");
        headerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        headerPanel.setSpacing(8);
        
        // Warning icon
        Label warningIcon = new Label("[WARNING]");
        warningIcon.setStyleName("warning-icon");
        headerPanel.add(warningIcon);
        
        // Message
        VerticalPanel messagePanel = new VerticalPanel();
        Label titleLabel = new Label("\"" + assetName + "\" already exists");
        titleLabel.setStyleName("conflict-title");
        Label subtitleLabel = new Label("Choose how to handle this conflict:");
        subtitleLabel.setStyleName("conflict-subtitle");
        
        messagePanel.add(titleLabel);
        messagePanel.add(subtitleLabel);
        headerPanel.add(messagePanel);
        
        return headerPanel;
    }
    
    private Widget createOptionsPanel() {
        VerticalPanel optionsPanel = new VerticalPanel();
        optionsPanel.setStyleName("conflict-options");
        optionsPanel.setSpacing(8);
        
        // Replace existing option
        replaceOption = new RadioButton("conflictGroup", "Replace existing version");
        replaceOption.setStyleName("conflict-option");
        replaceOption.setValue(true); // Default selection
        
        Label replaceWarning = new Label("[WARNING] This will update projects automatically");
        replaceWarning.setStyleName("option-warning");
        
        optionsPanel.add(replaceOption);
        optionsPanel.add(replaceWarning);
        
        // Create new asset option
        createNewOption = new RadioButton("conflictGroup", "Create new asset with different name");
        createNewOption.setStyleName("conflict-option");
        
        HorizontalPanel newNamePanel = new HorizontalPanel();
        newNamePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        newNamePanel.setSpacing(8);
        
        Label suggestedLabel = new Label("Suggested:");
        suggestedLabel.setStyleName("suggested-label");
        newNameBox = new TextBox();
        newNameBox.setValue(generateSuggestedName(assetName));
        newNameBox.setStyleName("new-name-input");
        newNameBox.setEnabled(false);
        
        newNamePanel.add(suggestedLabel);
        newNamePanel.add(newNameBox);
        
        optionsPanel.add(createNewOption);
        optionsPanel.add(newNamePanel);
        
        // Save as draft option
        draftOption = new RadioButton("conflictGroup", "Save as draft version");
        draftOption.setStyleName("conflict-option");
        
        Label draftInfo = new Label("Review changes before publishing to projects");
        draftInfo.setStyleName("option-info");
        
        optionsPanel.add(draftOption);
        optionsPanel.add(draftInfo);
        
        return optionsPanel;
    }
    
    private Widget createImpactPanel() {
        VerticalPanel impactPanel = new VerticalPanel();
        impactPanel.setStyleName("impact-panel");
        impactPanel.setSpacing(8);
        
        impactLabel = new Label();
        impactLabel.setStyleName("impact-label");
        impactPanel.add(impactLabel);
        
        // Notify checkbox (only visible for replace option)
        notifyCheckBox = new CheckBox("Notify project owners of update");
        notifyCheckBox.setStyleName("notify-checkbox");
        notifyCheckBox.setValue(true);
        impactPanel.add(notifyCheckBox);
        
        return impactPanel;
    }
    
    private Widget createButtonPanel() {
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setStyleName("dialog-buttons");
        buttonPanel.setSpacing(8);
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        
        cancelButton = new Button("Cancel");
        cancelButton.setStyleName("secondary-button");
        
        confirmButton = new Button("Continue");
        confirmButton.setStyleName("primary-button");
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        
        return buttonPanel;
    }
    
    private void setupEventHandlers() {
        // Radio button handlers
        replaceOption.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateImpactPreview();
                newNameBox.setEnabled(false);
            }
        });
        
        createNewOption.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateImpactPreview();
                newNameBox.setEnabled(true);
                newNameBox.setFocus(true);
            }
        });
        
        draftOption.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateImpactPreview();
                newNameBox.setEnabled(false);
            }
        });
        
        // Button handlers
        confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleConfirm();
            }
        });
        
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
    }
    
    private void updateImpactPreview() {
        String impactText;
        boolean showNotifyOption = false;
        
        if (replaceOption.getValue()) {
            impactText = "Impact: " + affectedProjects.size() + " projects will receive the updated asset";
            showNotifyOption = true;
            confirmButton.setText("Replace Asset");
        } else if (createNewOption.getValue()) {
            impactText = "Impact: No existing projects will be affected";
            confirmButton.setText("Create New Asset");
        } else if (draftOption.getValue()) {
            impactText = "Impact: Changes will be saved for review, no projects updated yet";
            confirmButton.setText("Save as Draft");
        } else {
            impactText = "";
        }
        
        impactLabel.setText(impactText);
        notifyCheckBox.setVisible(showNotifyOption);
    }
    
    private void handleConfirm() {
        ConflictResolution resolution;
        String newAssetName = null;
        boolean notifyProjects = notifyCheckBox.getValue();
        
        if (replaceOption.getValue()) {
            resolution = ConflictResolution.REPLACE_EXISTING;
        } else if (createNewOption.getValue()) {
            resolution = ConflictResolution.CREATE_NEW_ASSET;
            newAssetName = newNameBox.getValue().trim();
            if (newAssetName.isEmpty()) {
                showValidationError("Please enter a name for the new asset.");
                return;
            }
        } else {
            resolution = ConflictResolution.SAVE_AS_DRAFT;
        }
        
        hide();
        callback.onResolutionSelected(resolution, newAssetName, notifyProjects);
    }
    
    private void showValidationError(String message) {
        // Simple validation error - could be enhanced with better UI
        newNameBox.addStyleName("input-error");
    }
    
    private String generateSuggestedName(String originalName) {
        String nameWithoutExtension;
        String extension = "";
        
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot > 0) {
            nameWithoutExtension = originalName.substring(0, lastDot);
            extension = originalName.substring(lastDot);
        } else {
            nameWithoutExtension = originalName;
        }
        
        return nameWithoutExtension + "_v2" + extension;
    }
}