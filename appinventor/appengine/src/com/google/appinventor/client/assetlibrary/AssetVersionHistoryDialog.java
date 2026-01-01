package com.google.appinventor.client.assetlibrary;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.appinventor.shared.rpc.project.GlobalAsset;
import java.util.Date;

/**
 * Dialog for displaying version history of an asset.
 * Shows timestamp-based versioning information and allows rollback functionality.
 */
public class AssetVersionHistoryDialog extends DialogBox {
    
    public interface VersionActionCallback {
        void onRollback(long timestamp);
        void onViewVersion(long timestamp);
    }
    
    private final GlobalAsset asset;
    private final VersionActionCallback callback;
    
    public AssetVersionHistoryDialog(GlobalAsset asset, VersionActionCallback callback) {
        super(false, true); // non-auto-hide, modal
        this.asset = asset;
        this.callback = callback;
        
        initializeDialog();
    }
    
    private void initializeDialog() {
        setText("Version History: " + asset.getFileName());
        setStyleName("version-history-dialog");
        
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setStyleName("version-dialog-content");
        mainPanel.setSpacing(12);
        
        // Header with asset info
        mainPanel.add(createHeaderPanel());
        
        // Version information (for now, just current version)
        mainPanel.add(createVersionListPanel());
        
        // Action buttons
        mainPanel.add(createButtonPanel());
        
        setWidget(mainPanel);
        center();
    }
    
    private Widget createHeaderPanel() {
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setStyleName("version-header");
        headerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        headerPanel.setSpacing(12);
        
        // Asset info
        VerticalPanel assetInfo = new VerticalPanel();
        
        Label assetName = new Label(asset.getFileName());
        assetName.setStyleName("version-asset-name");
        assetInfo.add(assetName);
        
        if (asset.getFolder() != null && !asset.getFolder().isEmpty()) {
            Label folderLabel = new Label("Folder: " + asset.getFolder());
            folderLabel.setStyleName("version-asset-folder");
            assetInfo.add(folderLabel);
        }
        
        headerPanel.add(assetInfo);
        
        return headerPanel;
    }
    
    private Widget createVersionListPanel() {
        VerticalPanel versionPanel = new VerticalPanel();
        versionPanel.setStyleName("version-list-panel");
        versionPanel.setSpacing(8);
        
        Label versionTitle = new Label("📋 Version History");
        versionTitle.setStyleName("version-list-title");
        versionPanel.add(versionTitle);
        
        // Current version entry
        versionPanel.add(createVersionEntry(asset.getTimestamp(), true));
        
        // Note about future versions
        Label futureNote = new Label("💡 Future versions will appear here as assets are updated");
        futureNote.setStyleName("version-future-note");
        versionPanel.add(futureNote);
        
        return versionPanel;
    }
    
    private Widget createVersionEntry(long timestamp, boolean isCurrent) {
        HorizontalPanel versionEntry = new HorizontalPanel();
        versionEntry.setStyleName("version-entry");
        versionEntry.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        versionEntry.setSpacing(12);
        
        if (isCurrent) {
            versionEntry.addStyleName("version-entry-current");
        }
        
        // Version info
        VerticalPanel versionInfo = new VerticalPanel();
        
        String versionLabel = isCurrent ? "[check] Current Version" : "📄 Version";
        Label versionStatus = new Label(versionLabel);
        versionStatus.setStyleName("version-status");
        versionInfo.add(versionStatus);
        
        Date versionDate = new Date(timestamp);
        Label dateLabel = new Label("Modified: " + versionDate.toString());
        dateLabel.setStyleName("version-date");
        versionInfo.add(dateLabel);
        
        versionEntry.add(versionInfo);
        
        // Actions (only for non-current versions in the future)
        if (!isCurrent) {
            HorizontalPanel actions = new HorizontalPanel();
            actions.setSpacing(8);
            
            Button viewBtn = new Button("👁 View");
            viewBtn.setStyleName("version-action-button");
            viewBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (callback != null) {
                        callback.onViewVersion(timestamp);
                    }
                }
            });
            
            Button rollbackBtn = new Button("[rollback] Rollback");
            rollbackBtn.setStyleName("version-action-button rollback-button");
            rollbackBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (confirmRollback()) {
                        if (callback != null) {
                            callback.onRollback(timestamp);
                        }
                        hide();
                    }
                }
            });
            
            actions.add(viewBtn);
            actions.add(rollbackBtn);
            versionEntry.add(actions);
        } else {
            Label currentTag = new Label("📍 Active");
            currentTag.setStyleName("current-version-tag");
            versionEntry.add(currentTag);
        }
        
        return versionEntry;
    }
    
    private boolean confirmRollback() {
        return com.google.gwt.user.client.Window.confirm(
            "Are you sure you want to rollback to this version?\n\n" +
            "This will replace the current version and may affect projects using this asset."
        );
    }
    
    private Widget createButtonPanel() {
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setStyleName("dialog-buttons");
        buttonPanel.setSpacing(8);
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        
        Button closeButton = new Button("Close");
        closeButton.setStyleName("primary-button");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        buttonPanel.add(closeButton);
        
        return buttonPanel;
    }
}