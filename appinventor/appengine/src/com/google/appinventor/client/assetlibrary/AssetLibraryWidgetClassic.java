package com.google.appinventor.client.assetlibrary;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.PreviewFileCommand;
import com.google.appinventor.client.youngandroid.TextValidators;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.appinventor.client.Images;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetService;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetServiceAsync;
import com.google.appinventor.shared.rpc.globalasset.AssetConflictInfo;
import com.google.appinventor.client.assetlibrary.AssetUploadConflictDialog.ConflictResolution;
import com.google.appinventor.client.assetlibrary.AssetUploadConflictDialog.ConflictResolutionCallback;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.appinventor.shared.rpc.project.GlobalAssetProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AssetLibraryWidgetClassic extends Composite {
  private VerticalPanel rootPanel;
  private HorizontalPanel headerContainer;
  private TextBox searchBox;
  private ListBox typeFilter;
  private Button uploadButton;
  private Button closeButton;
  private HorizontalPanel mainContentPanel;
  private VerticalPanel sidebarPanel;
  private ScrollPanel assetScrollPanel;
  private VerticalPanel assetListPanel;
  private HorizontalPanel footerPanel;

  // Asset management
  private List<GlobalAsset> globalAssets = new ArrayList<>(); 
  private final GlobalAssetServiceAsync globalAssetService = GWT.create(GlobalAssetService.class);
  private static String draggedAssetName;
  private final Images images = GWT.create(Images.class);

  // Sidebar state
  private List<String> folders = new ArrayList<>();
  private int selectedFolderIndex = 0;
  private VerticalPanel folderListPanel;

  // Selection management
  private List<CheckBox> assetCheckBoxes = new ArrayList<>();
  private Button addSelectedButton;
  private Button deleteSelectedButton;

  public AssetLibraryWidgetClassic(Ode ode) {
    initializeLayout();
    setupEventHandlers();
    loadInitialData();
    initWidget(rootPanel);
  }
  

  private void initializeLayout() {
    // Main root panel - classic table layout with background
    rootPanel = new VerticalPanel();
    rootPanel.setSize("100%", "100%");
    rootPanel.setStyleName("ode-Box");
    rootPanel.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");
    rootPanel.getElement().getStyle().setProperty("display", "flex");
    rootPanel.getElement().getStyle().setProperty("flexDirection", "column");
    rootPanel.getElement().getStyle().setProperty("height", "100vh");
    rootPanel.getElement().getStyle().setProperty("overflow", "hidden");

    createHeader();
    createMainContent();
    createFooter();
  }

  private void createHeader() {
    // Premium header with modern classic App Inventor styling
    headerContainer = new HorizontalPanel();
    headerContainer.setWidth("100%");
    headerContainer.setStyleName("ode-TopPanel");
    headerContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    headerContainer.getElement().getStyle().setProperty("padding", "16px 24px");
    headerContainer.getElement().getStyle().setProperty("borderBottom", "1px solid #e0e0e0");

    // Left section: Title and search
    HorizontalPanel leftSection = new HorizontalPanel();
    leftSection.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    // Asset Library Title
    Label titleLabel = new Label("Asset Library");
    titleLabel.setStyleName("ode-ProjectNameLabel");
    titleLabel.getElement().getStyle().setProperty("fontSize", "18px");
    titleLabel.getElement().getStyle().setProperty("fontWeight", "600");
    titleLabel.getElement().getStyle().setProperty("marginRight", "20px");
    leftSection.add(titleLabel);

    // Search box matching App Inventor style
    searchBox = new TextBox();
    searchBox.getElement().setPropertyString("placeholder", "Search assets...");
    searchBox.setStyleName("ode-TextBox");
    searchBox.getElement().getStyle().setProperty("width", "280px");
    searchBox.getElement().getStyle().setProperty("height", "36px");
    searchBox.getElement().getStyle().setProperty("fontSize", "14px");
    searchBox.getElement().getStyle().setProperty("marginRight", "16px");
    leftSection.add(searchBox);

    // Type filter matching App Inventor dropdown style
    typeFilter = new ListBox();
    typeFilter.addItem("All Types");
    typeFilter.addItem("Images");
    typeFilter.addItem("Sounds");
    typeFilter.setStyleName("ode-ListBox");
    typeFilter.getElement().getStyle().setProperty("height", "36px");
    typeFilter.getElement().getStyle().setProperty("fontSize", "14px");
    typeFilter.getElement().getStyle().setProperty("minWidth", "120px");
    typeFilter.getElement().getStyle().setProperty("marginRight", "20px");
    leftSection.add(typeFilter);

    headerContainer.add(leftSection);

    // Spacer
    Label spacer = new Label("");
    spacer.getElement().getStyle().setProperty("flex", "1");
    headerContainer.add(spacer);

    // Right section: Action buttons using App Inventor button style
    HorizontalPanel rightSection = new HorizontalPanel();
    rightSection.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    rightSection.getElement().getStyle().setProperty("gap", "12px");
    rightSection.getElement().getStyle().setProperty("display", "flex");
    rightSection.getElement().getStyle().setProperty("alignItems", "center");

    // Bulk action buttons with improved sizing
    addSelectedButton = new Button("Add Selected");
    addSelectedButton.setStyleName("ode-ProjectListButton");
    addSelectedButton.setEnabled(false);
    addSelectedButton.getElement().getStyle().setProperty("height", "36px");
    addSelectedButton.getElement().getStyle().setProperty("fontSize", "14px");
    addSelectedButton.getElement().getStyle().setProperty("marginRight", "8px");
    rightSection.add(addSelectedButton);

    deleteSelectedButton = new Button("Delete Selected");
    deleteSelectedButton.setStyleName("ode-ProjectListButton");
    deleteSelectedButton.getElement().getStyle().setProperty("height", "36px");
    deleteSelectedButton.getElement().getStyle().setProperty("fontSize", "14px");
    deleteSelectedButton.getElement().getStyle().setProperty("marginRight", "8px");
    deleteSelectedButton.setEnabled(false);
    rightSection.add(deleteSelectedButton);

    // Upload button
    uploadButton = new Button("⬆ Upload Asset");
    uploadButton.setStyleName("ode-ProjectListButton");
    uploadButton.getElement().getStyle().setProperty("height", "36px");
    uploadButton.getElement().getStyle().setProperty("fontSize", "14px");
    uploadButton.getElement().getStyle().setProperty("marginRight", "8px");
    rightSection.add(uploadButton);

    // Close button
    closeButton = new Button("✕");
    closeButton.setTitle("Close Asset Library");
    closeButton.setStyleName("ode-ProjectListButton");
    closeButton.getElement().getStyle().setProperty("height", "36px");
    closeButton.getElement().getStyle().setProperty("fontSize", "16px");
    closeButton.getElement().getStyle().setProperty("minWidth", "36px");
    rightSection.add(closeButton);

    headerContainer.add(rightSection);
    rootPanel.add(headerContainer);
  }

  private void createMainContent() {
    // Main content area using classic horizontal split
    mainContentPanel = new HorizontalPanel();
    mainContentPanel.setSize("100%", "100%");
    mainContentPanel.setStyleName("ode-WorkColumns");
    mainContentPanel.getElement().getStyle().setProperty("flex", "1 1 auto");
    mainContentPanel.getElement().getStyle().setProperty("display", "flex");
    mainContentPanel.getElement().getStyle().setProperty("height", "100%");
    mainContentPanel.getElement().getStyle().setProperty("overflow", "hidden");

    createSidebar();
    createAssetList();

    rootPanel.add(mainContentPanel);
  }

  private void createSidebar() {
    // Sidebar matching App Inventor design with improved spacing and background
    sidebarPanel = new VerticalPanel();
    sidebarPanel.setWidth("280px");
    sidebarPanel.setHeight("100%");
    sidebarPanel.setStyleName("ode-Designer-LeftColumn");
    sidebarPanel.getElement().getStyle().setProperty("padding", "20px 16px");
    sidebarPanel.getElement().getStyle().setProperty("borderRight", "1px solid #e0e0e0");
    sidebarPanel.getElement().getStyle().setProperty("flexShrink", "0");
    sidebarPanel.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    sidebarPanel.getElement().getStyle().setProperty("height", "100vh");
    sidebarPanel.getElement().getStyle().setProperty("minHeight", "100vh");
    sidebarPanel.getElement().getStyle().setProperty("maxHeight", "100vh");
    sidebarPanel.getElement().getStyle().setProperty("boxSizing", "border-box");
    sidebarPanel.getElement().getStyle().setProperty("display", "flex");
    sidebarPanel.getElement().getStyle().setProperty("flexDirection", "column");
    sidebarPanel.getElement().getStyle().setProperty("alignItems", "stretch");
    sidebarPanel.getElement().getStyle().setProperty("justifyContent", "flex-start");
    sidebarPanel.getElement().getStyle().setProperty("overflowY", "auto");

    // Folder section header
    HorizontalPanel folderHeader = new HorizontalPanel();
    folderHeader.setWidth("100%");
    folderHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    folderHeader.getElement().getStyle().setProperty("flexShrink", "0");
    folderHeader.getElement().getStyle().setProperty("marginBottom", "8px");
    folderHeader.getElement().getStyle().setProperty("display", "flex");
    folderHeader.getElement().getStyle().setProperty("justifyContent", "space-between");
    folderHeader.getElement().getStyle().setProperty("alignItems", "center");
    folderHeader.getElement().getStyle().setProperty("width", "100%");

    Label folderTitle = new Label("📁 Folders");
    folderTitle.setStyleName("ode-ComponentRowLabel");
    folderTitle.getElement().getStyle().setProperty("fontSize", "16px");
    folderTitle.getElement().getStyle().setProperty("fontWeight", "600");
    folderHeader.add(folderTitle);

    // Small action buttons with proper spacing
    HorizontalPanel folderActions = new HorizontalPanel();
    folderActions.setSpacing(6);
    folderActions.getElement().getStyle().setProperty("marginLeft", "50px");

    Button newFolderBtn = createSmallButton("+", "New Folder");
    Button renameFolderBtn = createSmallButton("✎", "Rename Folder");
    Button deleteFolderBtn = createSmallButton("🗑", "Delete Folder");

    // Add event handlers for folder management
    setupFolderManagementHandlers(newFolderBtn, renameFolderBtn, deleteFolderBtn);

    folderActions.add(newFolderBtn);
    folderActions.add(renameFolderBtn);
    folderActions.add(deleteFolderBtn);
    folderHeader.add(folderActions);

    sidebarPanel.add(folderHeader);

    // Folder list
    folderListPanel = new VerticalPanel();
    folderListPanel.setWidth("100%");
    folderListPanel.getElement().getStyle().setProperty("flex", "1 1 auto");
    folderListPanel.getElement().getStyle().setProperty("overflowY", "auto");
    folderListPanel.getElement().getStyle().setProperty("minHeight", "0");
    folderListPanel.getElement().getStyle().setProperty("width", "100%");
    folderListPanel.getElement().getStyle().setProperty("paddingTop", "4px");
    sidebarPanel.add(folderListPanel);

    mainContentPanel.add(sidebarPanel);
  }

  private Button createSmallButton(String text, String title) {
    Button button = new Button(text);
    button.setTitle(title);
    button.setStyleName("ode-ProjectListButton");
    button.getElement().getStyle().setProperty("padding", "4px 8px");
    button.getElement().getStyle().setProperty("fontSize", "12px");
    button.getElement().getStyle().setProperty("minWidth", "28px");
    button.getElement().getStyle().setProperty("height", "28px");
    return button;
  }



  private void createAssetList() {
    // Asset list container
    VerticalPanel assetContainer = new VerticalPanel();
    assetContainer.setWidth("100vw");
    assetContainer.setHeight("100%");
    assetContainer.setStyleName("ode-Box-body-padding");
    assetContainer.getElement().getStyle().setProperty("flex", "1 1 auto");
    assetContainer.getElement().getStyle().setProperty("display", "flex");
    assetContainer.getElement().getStyle().setProperty("flexDirection", "column");
    assetContainer.getElement().getStyle().setProperty("minHeight", "0");
    assetContainer.getElement().getStyle().setProperty("overflow", "hidden");


    // Scrollable list
    assetScrollPanel = new ScrollPanel();
    assetScrollPanel.setWidth("100%");
    assetScrollPanel.setStyleName("ode-Explorer");
    assetScrollPanel.getElement().getStyle().setProperty("flex", "1 1 auto");
    assetScrollPanel.getElement().getStyle().setProperty("height", "calc(100vh - 180px)");
    assetScrollPanel.getElement().getStyle().setProperty("maxHeight", "calc(100vh - 180px)");
    assetScrollPanel.getElement().getStyle().setProperty("overflowY", "auto");
    assetScrollPanel.getElement().getStyle().setProperty("paddingBottom", "60px");

    // Asset list panel
    assetListPanel = new VerticalPanel();
    assetListPanel.setWidth("100%");
    assetListPanel.setSpacing(0);

    assetScrollPanel.add(assetListPanel);
    assetContainer.add(assetScrollPanel);
    mainContentPanel.add(assetContainer);
    mainContentPanel.setCellWidth(assetContainer, "100%");
    mainContentPanel.setCellHeight(assetContainer, "100%");
  }

  private void createFooter() {
    // Footer matching App Inventor style
    footerPanel = new HorizontalPanel();
    footerPanel.setWidth("100%");
    footerPanel.setStyleName("ode-StatusPanel");
    footerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    footerPanel.getElement().getStyle().setProperty("padding", "8px 24px");
    footerPanel.getElement().getStyle().setProperty("minHeight", "36px");
    footerPanel.getElement().getStyle().setProperty("borderTop", "1px solid #e0e0e0");
    footerPanel.getElement().getStyle().setProperty("flexShrink", "0");
    footerPanel.getElement().getStyle().setProperty("backgroundColor", "white");
    footerPanel.getElement().getStyle().setProperty("position", "fixed");
    footerPanel.getElement().getStyle().setProperty("bottom", "0");
    footerPanel.getElement().getStyle().setProperty("left", "0");
    footerPanel.getElement().getStyle().setProperty("right", "0");
    footerPanel.getElement().getStyle().setProperty("zIndex", "998");
    footerPanel.getElement().getStyle().setProperty("boxSizing", "border-box");

    Label footerText = new Label("Tip: Drag assets to folders to organize them");
    footerText.setStyleName("ode-StatusPanelLabel");
    footerText.getElement().getStyle().setProperty("fontSize", "12px");
    footerText.getElement().getStyle().setProperty("fontStyle", "italic");
    footerPanel.add(footerText);

    rootPanel.add(footerPanel);
  }

  private void setupEventHandlers() {
    searchBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        refreshAssetList();
      }
    });

    typeFilter.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        refreshAssetList();
      }
    });

    uploadButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showUploadDialog();
      }
    });

    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // Get the current project and switch back to the editor
        long currentProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
        if (currentProjectId != 0) {
          Project project = Ode.getInstance().getProjectManager().getProject(currentProjectId);
          if (project != null) {
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
          } else {
            // Fallback to projects view if project not found
            Ode.getInstance().switchToProjectsView();
          }
        } else {
          // Fallback to projects view if no current project
          Ode.getInstance().switchToProjectsView();
        }
      }
    });

    setupBulkActionHandlers();
  }

  private void setupBulkActionHandlers() {
    addSelectedButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<GlobalAsset> selectedAssets = getSelectedAssets();
        if (!selectedAssets.isEmpty()) {
          showBulkAddToProjectDialog(selectedAssets);
        }
      }
    });

    deleteSelectedButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<GlobalAsset> selectedAssets = getSelectedAssets();
        if (!selectedAssets.isEmpty() && 
            Window.confirm("Are you sure you want to delete " + selectedAssets.size() + " selected asset(s)?")) {
          deleteSelectedAssets(selectedAssets);
        }
      }
    });
  }

  private void setupFolderManagementHandlers(Button newFolderBtn, Button renameFolderBtn, Button deleteFolderBtn) {
    newFolderBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showNewFolderDialog();
      }
    });

    renameFolderBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String selectedFolder = selectedFolderIndex >= 0 && selectedFolderIndex < folders.size() 
            ? folders.get(selectedFolderIndex) : null;
        if (selectedFolder != null && !isSpecialFolder(selectedFolder)) {
          showRenameFolderDialog(selectedFolder);
        } else {
          Window.alert("Please select a regular folder to rename.");
        }
      }
    });

    deleteFolderBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String selectedFolder = selectedFolderIndex >= 0 && selectedFolderIndex < folders.size() 
            ? folders.get(selectedFolderIndex) : null;
        if (selectedFolder != null && !isSpecialFolder(selectedFolder)) {
          showDeleteFolderDialog(selectedFolder);
        } else {
          Window.alert("Please select a regular folder to delete.");
        }
      }
    });
  }

  private boolean isSpecialFolder(String folderName) {
    return "All Assets".equals(folderName) || "Recent".equals(folderName);
  }

  private void loadInitialData() {
    // Initialize with default folders
    folders.clear();
    folders.add("All Assets");
    selectedFolderIndex = 0;
    
    updateFolderList();
    refreshGlobalAssets();
  }

  private void updateFolderList() {
    folderListPanel.clear();
    
    for (int i = 0; i < folders.size(); i++) {
      final int index = i;
      HorizontalPanel folderRow = createFolderRow(folders.get(i), index);
      folderListPanel.add(folderRow);
    }
  }

  private HorizontalPanel createFolderRow(String folderName, int index) {
    HorizontalPanel folderRow = new HorizontalPanel();
    folderRow.setWidth("100%");
    folderRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    folderRow.getElement().getStyle().setProperty("padding", "10px 0px");
    folderRow.getElement().getStyle().setProperty("borderRadius", "4px");
    folderRow.getElement().getStyle().setProperty("cursor", "pointer");
    folderRow.getElement().getStyle().setProperty("marginBottom", "6px");
    folderRow.getElement().getStyle().setProperty("minHeight", "36px");
    folderRow.getElement().getStyle().setProperty("boxSizing", "border-box");

    // Folder icon
    Label icon = new Label("📁");
    icon.getElement().getStyle().setProperty("marginLeft", "12px");
    icon.getElement().getStyle().setProperty("marginRight", "10px");
    icon.getElement().getStyle().setProperty("fontSize", "16px");
    folderRow.add(icon);

    // Folder name
    Label nameLabel = new Label(folderName);
    nameLabel.setStyleName("ode-ComponentRowLabel");
    nameLabel.getElement().getStyle().setProperty("fontSize", "14px");
    nameLabel.getElement().getStyle().setProperty("fontWeight", "400");
    nameLabel.getElement().getStyle().setProperty("overflow", "hidden");
    nameLabel.getElement().getStyle().setProperty("textOverflow", "ellipsis");
    nameLabel.getElement().getStyle().setProperty("whiteSpace", "nowrap");
    folderRow.add(nameLabel);
    folderRow.setCellWidth(nameLabel, "100%");

    // Apply selection styling matching App Inventor
    if (index == selectedFolderIndex) {
      folderRow.setStyleName("ode-ComponentRowHighlighted");
      nameLabel.setStyleName("ode-ComponentRowLabel");
    } else {
      folderRow.setStyleName("ode-ComponentRowUnHighlighted");
      nameLabel.setStyleName("ode-ComponentRowLabel");
    }

    // Click handler
    folderRow.addDomHandler(new com.google.gwt.event.dom.client.ClickHandler() {
      @Override
      public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
        selectedFolderIndex = index;
        updateFolderList();
        refreshAssetList();
      }
    }, com.google.gwt.event.dom.client.ClickEvent.getType());

    // Drag and drop support
    setupFolderDragDrop(folderRow, index);

    return folderRow;
  }

  private void setupFolderDragDrop(HorizontalPanel folderRow, int folderIndex) {
    folderRow.addDomHandler(new DragOverHandler() {
      @Override
      public void onDragOver(DragOverEvent event) {
        event.preventDefault();
        folderRow.addStyleName("ode-ComponentRowHighlighted");
      }
    }, DragOverEvent.getType());

    folderRow.addDomHandler(new DropHandler() {
      @Override
      public void onDrop(DropEvent event) {
        event.preventDefault();
        if (folderIndex != selectedFolderIndex) {
          folderRow.removeStyleName("ode-ComponentRowHighlighted");
        }
        
        if (draggedAssetName != null) {
          String folderName = folders.get(folderIndex);
          moveAssetToFolder(draggedAssetName, folderName);
          draggedAssetName = null;
        }
      }
    }, DropEvent.getType());
  }

  private void refreshGlobalAssets() {
    globalAssetService.getGlobalAssets(new AsyncCallback<List<GlobalAsset>>() {
      @Override
      public void onSuccess(List<GlobalAsset> assets) {
        globalAssets.clear();
        if (assets != null) {
          globalAssets.addAll(assets);
        }
        updateFoldersFromAssets();
        refreshAssetList();
      }

      @Override
      public void onFailure(Throwable caught) {
        globalAssets.clear();
        refreshAssetList();
      }
    });
  }

  private void updateFoldersFromAssets() {
    // Preserve current selection
    String currentFolder = selectedFolderIndex >= 0 && selectedFolderIndex < folders.size() 
        ? folders.get(selectedFolderIndex) : "All Assets";
    
    // Clear and rebuild folders
    folders.clear();
    folders.add("All Assets");
    
    // Add unique folders from assets
    Set<String> uniqueFolders = new HashSet<>();
    for (GlobalAsset asset : globalAssets) {
      String folder = asset.getFolder();
      if (folder != null && !folder.isEmpty() && !folder.equals("")) {
        uniqueFolders.add(folder);
      }
    }
    
    // Add folders in alphabetical order
    List<String> sortedFolders = new ArrayList<>(uniqueFolders);
    Collections.sort(sortedFolders);
    folders.addAll(sortedFolders);
    
    // Add virtual folders
    folders.add("Recent");
    
    // Restore selection or default to "All Assets"
    selectedFolderIndex = 0;
    for (int i = 0; i < folders.size(); i++) {
      if (folders.get(i).equals(currentFolder)) {
        selectedFolderIndex = i;
        break;
      }
    }
    
    updateFolderList();
  }

  private void refreshAssetList() {
    assetListPanel.clear();
    assetCheckBoxes.clear();

    String searchText = searchBox.getText().toLowerCase();
    String typeFilter = this.typeFilter.getSelectedValue();
    String folderFilter = selectedFolderIndex >= 0 && selectedFolderIndex < folders.size() 
        ? folders.get(selectedFolderIndex) : "All Assets";
    List<GlobalAsset> filteredAssets = filterAssets(searchText, typeFilter, folderFilter);

    if (filteredAssets.isEmpty()) {
      showEmptyState();
    } else {
      displayAssets(filteredAssets);
    }

    updateBulkActionButtons();
  }

  private List<GlobalAsset> filterAssets(String searchText, String typeFilter, String folderFilter) {
    List<GlobalAsset> filtered = new ArrayList<>();
    
    for (GlobalAsset asset : globalAssets) {
      boolean nameMatch = asset.getFileName().toLowerCase().contains(searchText);
      boolean typeMatch = matchesTypeFilter(asset, typeFilter);
      boolean folderMatch = matchesFolderFilter(asset, folderFilter);
      
      if (nameMatch && typeMatch && folderMatch) {
        filtered.add(asset);
      }
    }
    
    return filtered;
  }

  private boolean matchesFolderFilter(GlobalAsset asset, String folderFilter) {
    if ("All Assets".equals(folderFilter)) {
      return true;
    } else if ("Recent".equals(folderFilter)) {
      // Show assets modified in the last 7 days
      long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
      return asset.getTimestamp() > weekAgo;
    } else {
      // Regular folder match
      String assetFolder = asset.getFolder();
      if (assetFolder == null) assetFolder = "";
      return folderFilter.equals(assetFolder);
    }
  }

  private boolean matchesTypeFilter(GlobalAsset asset, String typeFilter) {
    if ("All Types".equals(typeFilter)) return true;
    
    String fileName = asset.getFileName().toLowerCase();
    if ("Images".equals(typeFilter)) {
      return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif");
    } else if ("Sounds".equals(typeFilter)) {
      return fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".ogg");
    }
    
    return false;
  }

  private void showEmptyState() {
    VerticalPanel emptyState = new VerticalPanel();
    emptyState.setWidth("100%");
    emptyState.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    emptyState.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    emptyState.getElement().getStyle().setProperty("padding", "60px 20px");
    emptyState.getElement().getStyle().setProperty("textAlign", "center");

    // Empty icon
    Label emptyIcon = new Label("📁");
    emptyIcon.getElement().getStyle().setProperty("fontSize", "64px");
    emptyIcon.getElement().getStyle().setProperty("opacity", "0.4");
    emptyIcon.getElement().getStyle().setProperty("marginBottom", "16px");
    emptyState.add(emptyIcon);

    // Message
    Label emptyMessage = new Label("No assets found");
    emptyMessage.setStyleName("ode-ComponentRowLabel");
    emptyMessage.getElement().getStyle().setProperty("fontSize", "18px");
    emptyMessage.getElement().getStyle().setProperty("fontWeight", "500");
    emptyMessage.getElement().getStyle().setProperty("marginBottom", "8px");
    emptyState.add(emptyMessage);

    // Sub message
    Label emptySubMessage = new Label("Try adjusting your search or upload new assets");
    emptySubMessage.setStyleName("ode-ComponentRowLabel");
    emptySubMessage.getElement().getStyle().setProperty("fontSize", "14px");
    emptySubMessage.getElement().getStyle().setProperty("opacity", "0.7");
    emptyState.add(emptySubMessage);

    assetListPanel.add(emptyState);
  }

  private void displayAssets(List<GlobalAsset> assets) {
    for (final GlobalAsset asset : assets) {
      HorizontalPanel assetRow = createAssetRow(asset);
      assetListPanel.add(assetRow);
    }
  }

  private HorizontalPanel createAssetRow(final GlobalAsset asset) {
    HorizontalPanel row = new HorizontalPanel();
    row.setWidth("100%");
    row.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    row.setStyleName("ode-Box");
    row.getElement().getStyle().setProperty("minHeight", "60px");
    row.getElement().getStyle().setProperty("padding", "12px 16px");
    row.getElement().getStyle().setProperty("margin", "4px 0");
    row.getElement().getStyle().setProperty("borderRadius", "4px");
    row.getElement().getStyle().setProperty("cursor", "pointer");

    // Checkbox for selection
    HorizontalPanel checkboxContainer = new HorizontalPanel();
    checkboxContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    checkboxContainer.getElement().getStyle().setProperty("marginRight", "12px");
    
    final CheckBox checkBox = new CheckBox();
    checkBox.getElement().getStyle().setProperty("cursor", "pointer");
    assetCheckBoxes.add(checkBox);
    checkboxContainer.add(checkBox);
    row.add(checkboxContainer);

    // Asset preview/icon with container
    HorizontalPanel previewContainer = new HorizontalPanel();
    previewContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    previewContainer.getElement().getStyle().setProperty("marginRight", "12px");
    
    Widget previewWidget = createPreviewWidget(asset);
    previewContainer.add(previewWidget);
    row.add(previewContainer);

    // Asset name and details
    VerticalPanel detailsPanel = new VerticalPanel();
    detailsPanel.getElement().getStyle().setProperty("flex", "1");
    detailsPanel.setSpacing(2);
    
    // Asset name with type indicator
    HorizontalPanel nameRow = new HorizontalPanel();
    nameRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    Label nameLabel = new Label(asset.getFileName());
    nameLabel.setStyleName("ode-ComponentRowLabel");
    nameLabel.getElement().getStyle().setProperty("fontSize", "15px");
    nameLabel.getElement().getStyle().setProperty("fontWeight", "500");
    nameLabel.getElement().getStyle().setProperty("marginRight", "8px");
    nameRow.add(nameLabel);
    
    // Add file type badge
    String fileExt = getFileExtension(asset.getFileName()).toLowerCase();
    String badgeColor = getFileTypeBadgeColor(fileExt);
    Label typeBadge = new Label(fileExt.toUpperCase());
    typeBadge.getElement().getStyle().setProperty("fontSize", "10px");
    typeBadge.getElement().getStyle().setProperty("fontWeight", "600");
    typeBadge.getElement().getStyle().setProperty("color", "white");
    typeBadge.getElement().getStyle().setProperty("backgroundColor", badgeColor);
    typeBadge.getElement().getStyle().setProperty("padding", "2px 6px");
    typeBadge.getElement().getStyle().setProperty("borderRadius", "3px");
    typeBadge.getElement().getStyle().setProperty("textTransform", "uppercase");
    nameRow.add(typeBadge);
    
    detailsPanel.add(nameRow);

    // Enhanced metadata row with version and usage info
    HorizontalPanel metadataRow = new HorizontalPanel();
    metadataRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    Label dateLabel = new Label(" " + formatDate(asset.getTimestamp()));
    dateLabel.setStyleName("ode-ComponentRowLabel");
    dateLabel.getElement().getStyle().setProperty("fontSize", "12px");
    dateLabel.getElement().getStyle().setProperty("color", "#586069");
    dateLabel.getElement().getStyle().setProperty("marginRight", "12px");
    metadataRow.add(dateLabel);
    
    // Add folder info if exists
    if (asset.getFolder() != null && !asset.getFolder().isEmpty()) {
      Label folderLabel = new Label(" " + asset.getFolder());
      folderLabel.setStyleName("ode-ComponentRowLabel");
      folderLabel.getElement().getStyle().setProperty("fontSize", "12px");
      folderLabel.getElement().getStyle().setProperty("color", "#586069");
      folderLabel.getElement().getStyle().setProperty("marginRight", "12px");
      metadataRow.add(folderLabel);
    }
    
    detailsPanel.add(metadataRow);

    // Project usage status row
    HorizontalPanel statusRow = new HorizontalPanel();
    statusRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    statusRow.getElement().getStyle().setProperty("marginTop", "4px");
    
    // Add project usage indicator with async loading
    final Label usageIndicator = new Label(" Checking usage...");
    usageIndicator.setStyleName("ode-ComponentRowLabel");
    usageIndicator.getElement().getStyle().setProperty("fontSize", "11px");
    usageIndicator.getElement().getStyle().setProperty("color", "#6c757d");
    usageIndicator.getElement().getStyle().setProperty("marginRight", "8px");
    statusRow.add(usageIndicator);
    
    // Load project usage asynchronously
    globalAssetService.getProjectsUsingAsset(asset.getFileName(), new AsyncCallback<List<Long>>() {
      @Override
      public void onSuccess(List<Long> projectIds) {
        if (projectIds != null && !projectIds.isEmpty()) {
          usageIndicator.setText(" Used by " + projectIds.size() + " project" + (projectIds.size() == 1 ? "" : "s"));
          usageIndicator.getElement().getStyle().setProperty("color", "#007bff");
          usageIndicator.setTitle("This asset is linked to " + projectIds.size() + " project(s)");
        } else {
          usageIndicator.setText(" Not in use");
          usageIndicator.getElement().getStyle().setProperty("color", "#6c757d");
          usageIndicator.setTitle("This asset is not currently used by any projects");
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        usageIndicator.setText(" Unknown usage");
        usageIndicator.getElement().getStyle().setProperty("color", "#dc3545");
      }
    });
    
    // Version indicator (based on timestamp for now)
    String versionText = getVersionText(asset.getTimestamp());
    Label versionLabel = new Label(" " + versionText);
    versionLabel.setStyleName("ode-ComponentRowLabel");
    versionLabel.getElement().getStyle().setProperty("fontSize", "11px");
    versionLabel.getElement().getStyle().setProperty("color", getVersionColor(asset.getTimestamp()));
    versionLabel.setTitle("Asset version: " + versionText);
    statusRow.add(versionLabel);
    
    detailsPanel.add(statusRow);

    row.add(detailsPanel);
    row.setCellWidth(detailsPanel, "100%");

    // Action buttons with improved styling
    HorizontalPanel actionPanel = new HorizontalPanel();
    actionPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    actionPanel.setSpacing(4);

    // Preview button
    Button previewBtn = new Button("Preview");
    previewBtn.setStyleName("ode-ProjectListButton");
    previewBtn.getElement().getStyle().setProperty("padding", "4px 8px");
    previewBtn.getElement().getStyle().setProperty("fontSize", "12px");
    previewBtn.getElement().getStyle().setProperty("minWidth", "60px");
    previewBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        previewAsset(asset);
      }
    });
    actionPanel.add(previewBtn);

    // Update version button
    Button updateBtn = new Button("Update");
    updateBtn.setTitle("Upload New Version");
    updateBtn.setStyleName("ode-ProjectListButton");
    updateBtn.getElement().getStyle().setProperty("padding", "4px 8px");
    updateBtn.getElement().getStyle().setProperty("fontSize", "12px");
    updateBtn.getElement().getStyle().setProperty("minWidth", "60px");
    updateBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showUpdateAssetDialog(asset);
      }
    });
    actionPanel.add(updateBtn);

    // Add button
    Button addBtn = new Button("Add");
    addBtn.setStyleName("ode-ProjectListButton");
    addBtn.getElement().getStyle().setProperty("padding", "4px 8px");
    addBtn.getElement().getStyle().setProperty("fontSize", "12px");
    addBtn.getElement().getStyle().setProperty("minWidth", "50px");
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showAddToProjectDialog(asset);
      }
    });
    actionPanel.add(addBtn);

    // Delete button
    Button deleteBtn = new Button("Delete");
    deleteBtn.setStyleName("ode-ProjectListButton");
    deleteBtn.getElement().getStyle().setProperty("padding", "4px 8px");
    deleteBtn.getElement().getStyle().setProperty("fontSize", "12px");
    deleteBtn.getElement().getStyle().setProperty("minWidth", "60px");
    deleteBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        if (Window.confirm("Are you sure you want to delete '" + asset.getFileName() + "'?")) {
          deleteAsset(asset);
        }
      }
    });
    actionPanel.add(deleteBtn);

    row.add(actionPanel);

    // Drag and drop support
    setupAssetDragDrop(row, asset);


    // Checkbox change handler
    checkBox.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        updateBulkActionButtons();
      }
    });

    return row;
  }

  
  private String getFileExtension(String fileName) {
    int lastDot = fileName.lastIndexOf('.');
    return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
  }
  
  private String getFileTypeBadgeColor(String extension) {
    switch (extension.toLowerCase()) {
      case "png":
      case "jpg":
      case "jpeg":
      case "gif":
        return "#28a745"; // Green for images
      case "mp3":
      case "wav":
      case "ogg":
        return "#007bff"; // Blue for audio
      default:
        return "#6c757d"; // Gray for other
    }
  }

  private Widget createPreviewWidget(GlobalAsset asset) {
    String fileName = asset.getFileName().toLowerCase();
    String filePath = asset.getFolder() != null ? asset.getFolder() + "/" + asset.getFileName() : asset.getFileName();
    
    // Preview container
    HorizontalPanel previewContainer = new HorizontalPanel();
    previewContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    previewContainer.getElement().getStyle().setProperty("width", "48px");
    previewContainer.getElement().getStyle().setProperty("height", "48px");
    previewContainer.getElement().getStyle().setProperty("borderRadius", "4px");
    previewContainer.getElement().getStyle().setProperty("border", "1px solid #ddd");
    previewContainer.getElement().getStyle().setProperty("justifyContent", "center");
    previewContainer.getElement().getStyle().setProperty("alignItems", "center");
    previewContainer.getElement().getStyle().setProperty("overflow", "hidden");
    
    if (StorageUtil.isImageFile(filePath)) {
      // Create image preview with cache-busting parameter
      String imageUrl = "/ode/download/globalasset/" + asset.getFileName() + "?t=" + asset.getTimestamp();
      Image img = new Image(imageUrl);
      img.setWidth("44px");
      img.setHeight("44px");
      img.getElement().getStyle().setProperty("objectFit", "cover");
      img.getElement().getStyle().setProperty("borderRadius", "3px");
      previewContainer.add(img);
    } else {
      // Use icon for non-image files
      Image iconImg;
      if (StorageUtil.isAudioFile(filePath)) {
        iconImg = new Image(images.player());
      } else if (StorageUtil.isVideoFile(filePath)) {
        iconImg = new Image(images.image()); // Use image icon for video for now
      } else {
        iconImg = new Image(images.image());
      }
      
      iconImg.setWidth("32px");
      iconImg.setHeight("32px");
      previewContainer.add(iconImg);
    }
    
    return previewContainer;
  }

  private void previewAsset(GlobalAsset asset) {
    GlobalAssetProjectNode projectNode = new GlobalAssetProjectNode(
        asset.getFileName(),
        asset.getFileName()
    );
    
    PreviewFileCommand previewCommand = new PreviewFileCommand();
    if (previewCommand.isSupported(projectNode)) {
      previewCommand.execute(projectNode);
    } else {
      Window.alert("Preview not supported for this file type: " + asset.getFileName());
    }
  }

  private void setupAssetDragDrop(HorizontalPanel row, GlobalAsset asset) {
    row.getElement().setAttribute("draggable", "true");
    
    row.addDomHandler(new DragStartHandler() {
      @Override
      public void onDragStart(DragStartEvent event) {
        event.setData("text/plain", asset.getFileName());
        draggedAssetName = asset.getFileName();
      }
    }, DragStartEvent.getType());
  }

  private String formatDate(long timestamp) {
    java.util.Date date = new java.util.Date(timestamp);
    String dateStr = date.toString();
    return dateStr.substring(4, 10); // "MMM dd"
  }

  private String getVersionText(long timestamp) {
    long now = System.currentTimeMillis();
    long ageMillis = now - timestamp;
    
    // Convert to days
    long ageDays = ageMillis / (24 * 60 * 60 * 1000);
    
    if (ageDays == 0) {
      return "Today";
    } else if (ageDays == 1) {
      return "Yesterday";
    } else if (ageDays < 7) {
      return ageDays + " days ago";
    } else if (ageDays < 30) {
      long weeks = ageDays / 7;
      return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
    } else if (ageDays < 365) {
      long months = ageDays / 30;
      return months + " month" + (months == 1 ? "" : "s") + " ago";
    } else {
      long years = ageDays / 365;
      return years + " year" + (years == 1 ? "" : "s") + " ago";
    }
  }

  private String getVersionColor(long timestamp) {
    long now = System.currentTimeMillis();
    long ageMillis = now - timestamp;
    long ageDays = ageMillis / (24 * 60 * 60 * 1000);
    
    if (ageDays == 0) {
      return "#28a745"; // Green for today
    } else if (ageDays < 7) {
      return "#007bff"; // Blue for this week
    } else if (ageDays < 30) {
      return "#ffc107"; // Yellow for this month
    } else {
      return "#6c757d"; // Gray for older
    }
  }

  private List<GlobalAsset> getSelectedAssets() {
    List<GlobalAsset> selected = new ArrayList<>();
    String searchText = searchBox.getText().toLowerCase();
    String typeFilter = this.typeFilter.getSelectedValue();
    String folderFilter = selectedFolderIndex >= 0 && selectedFolderIndex < folders.size() 
        ? folders.get(selectedFolderIndex) : "All Assets";
    List<GlobalAsset> filteredAssets = filterAssets(searchText, typeFilter, folderFilter);
    
    for (int i = 0; i < assetCheckBoxes.size() && i < filteredAssets.size(); i++) {
      if (assetCheckBoxes.get(i).getValue()) {
        selected.add(filteredAssets.get(i));
      }
    }
    return selected;
  }

  private void updateBulkActionButtons() {
    int selectedCount = 0;
    for (CheckBox checkBox : assetCheckBoxes) {
      if (checkBox.getValue()) {
        selectedCount++;
      }
    }
    
    boolean hasSelection = selectedCount > 0;
    addSelectedButton.setEnabled(hasSelection);
    deleteSelectedButton.setEnabled(hasSelection);
    
    // Update button text with count
    if (hasSelection) {
      addSelectedButton.setText("Add " + selectedCount + " Selected");
      deleteSelectedButton.setText("Delete " + selectedCount + " Selected");
    } else {
      addSelectedButton.setText("Add Selected");
      deleteSelectedButton.setText("Delete Selected");
    }
  }


  // Asset operations
  private void deleteAsset(GlobalAsset asset) {
    globalAssetService.deleteGlobalAsset(asset.getFileName(), new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        refreshGlobalAssets();
      }
      
      @Override
      public void onFailure(Throwable caught) {
        showDeleteError("Cannot Delete Asset", caught.getMessage());
      }
    });
  }

  private void deleteSelectedAssets(List<GlobalAsset> assets) {
    for (GlobalAsset asset : assets) {
      deleteAsset(asset);
    }
  }

  private void moveAssetToFolder(String assetName, String folderName) {
    globalAssetService.updateGlobalAssetFolder(assetName, folderName, new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        refreshGlobalAssets();
      }
      
      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Failed to move asset: " + caught.getMessage());
      }
    });
  }

  // Dialog methods
  private void showUploadDialog() {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Upload Asset to Library");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(8);

    // Upload form
    final FormPanel form = new FormPanel();
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);

    VerticalPanel formPanel = new VerticalPanel();
    formPanel.setSpacing(4);

    Label fileLabel = new Label("Select file to upload:");
    fileLabel.setStyleName("ode-ComponentRowLabel");
    formPanel.add(fileLabel);

    final FileUpload fileUpload = new FileUpload();
    fileUpload.setName(ServerLayout.UPLOAD_GLOBAL_ASSET_FORM_ELEMENT);
    fileUpload.setStyleName("ode-TextBox");
    formPanel.add(fileUpload);

    final Label errorLabel = new Label();
    formPanel.add(errorLabel);

    form.setWidget(formPanel);
    dialogPanel.add(form);

    // Button panel
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(4);

    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyleName("ode-ProjectListButton");

    Button uploadBtn = new Button("Upload");
    uploadBtn.setStyleName("ode-ProjectListButton");

    buttonPanel.add(cancelBtn);
    buttonPanel.add(uploadBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    // Event handlers
    uploadBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String filename = fileUpload.getFilename();
        if (filename == null || filename.isEmpty()) {
          showInlineError(errorLabel, "⚠ Please select a file to upload.");
          return;
        }
        
        String lower = filename.toLowerCase();
        if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
              lower.endsWith(".gif") || lower.endsWith(".mp3") || lower.endsWith(".wav") || 
              lower.endsWith(".ogg") || lower.endsWith(".ttf") || lower.endsWith(".otf") ||
              lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".webm"))) {
          showInlineError(errorLabel, "⚠ Unsupported file type. Supported formats: PNG, JPG, GIF, MP3, WAV, OGG, TTF, OTF, MP4, AVI, WebM");
          return;
        }
        
        // Clear any previous errors
        clearInlineError(errorLabel);
        
        // Check if asset already exists and handle conflicts
        checkAssetExistsAndProceed(filename, dialog, form, fileUpload);
      }
    });

    form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete(SubmitCompleteEvent event) {
        dialog.hide();
        String results = event.getResults();
        if (results != null && results.contains("SUCCESS")) {
          refreshGlobalAssets();
        } else {
          showUploadError(MESSAGES.fileUploadError(), "Please check your file and try again.");
        }
      }
    });

    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    dialog.center();
    dialog.show();
  }

  private void checkAssetExistsAndProceed(String filename, DialogBox uploadDialog, FormPanel form, FileUpload fileUpload) {
    // Check if asset with same name already exists
    GlobalAsset existingAsset = null;
    for (GlobalAsset asset : globalAssets) {
      if (asset.getFileName().equals(filename)) {
        existingAsset = asset;
        break;
      }
    }
    
    if (existingAsset != null) {
      // Show enhanced confirmation dialog with project usage info
      showAssetUpdateConfirmationDialog(filename, existingAsset, uploadDialog, form, fileUpload);
    } else {
      // Proceed with upload
      proceedWithUpload(filename, form);
    }
  }

  private void showAssetUpdateConfirmationDialog(String filename, GlobalAsset existingAsset, 
                                               DialogBox uploadDialog, FormPanel form, FileUpload fileUpload) {
    final DialogBox confirmDialog = new DialogBox();
    confirmDialog.setText("Asset Already Exists");
    confirmDialog.setStyleName("ode-DialogBox");
    confirmDialog.setModal(true);
    confirmDialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.getElement().getStyle().setProperty("minWidth", "450px");

    // Warning header with icon
    HorizontalPanel warningHeader = new HorizontalPanel();
    warningHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    warningHeader.getElement().getStyle().setProperty("backgroundColor", "#fff3cd");
    warningHeader.getElement().getStyle().setProperty("padding", "12px");
    warningHeader.getElement().getStyle().setProperty("borderRadius", "6px");
    warningHeader.getElement().getStyle().setProperty("border", "1px solid #ffeaa7");
    
    Label warningIcon = new Label("[warning]");
    warningIcon.getElement().getStyle().setProperty("fontSize", "24px");
    warningIcon.getElement().getStyle().setProperty("marginRight", "10px");
    warningHeader.add(warningIcon);
    
    VerticalPanel warningText = new VerticalPanel();
    Label warningTitle = new Label("Asset '" + filename + "' already exists");
    warningTitle.setStyleName("ode-ComponentRowLabel");
    warningTitle.getElement().getStyle().setProperty("fontWeight", "600");
    warningTitle.getElement().getStyle().setProperty("fontSize", "16px");
    warningTitle.getElement().getStyle().setProperty("color", "#856404");
    warningText.add(warningTitle);
    
    Label warningSubtitle = new Label("What would you like to do?");
    warningSubtitle.setStyleName("ode-ComponentRowLabel");
    warningSubtitle.getElement().getStyle().setProperty("fontSize", "14px");
    warningSubtitle.getElement().getStyle().setProperty("color", "#856404");
    warningText.add(warningSubtitle);
    
    warningHeader.add(warningText);
    dialogPanel.add(warningHeader);

    // Asset info panel
    HorizontalPanel assetInfo = new HorizontalPanel();
    assetInfo.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    assetInfo.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");
    assetInfo.getElement().getStyle().setProperty("padding", "12px");
    assetInfo.getElement().getStyle().setProperty("borderRadius", "6px");
    assetInfo.getElement().getStyle().setProperty("border", "1px solid #e9ecef");
    
    Widget previewWidget = createPreviewWidget(existingAsset);
    assetInfo.add(previewWidget);
    
    VerticalPanel assetDetails = new VerticalPanel();
    assetDetails.getElement().getStyle().setProperty("marginLeft", "12px");
    
    Label assetName = new Label(existingAsset.getFileName());
    assetName.setStyleName("ode-ComponentRowLabel");
    assetName.getElement().getStyle().setProperty("fontWeight", "600");
    assetName.getElement().getStyle().setProperty("fontSize", "15px");
    assetDetails.add(assetName);
    
    Label assetDate = new Label("Last modified: " + formatDate(existingAsset.getTimestamp()));
    assetDate.setStyleName("ode-ComponentRowLabel");
    assetDate.getElement().getStyle().setProperty("fontSize", "12px");
    assetDate.getElement().getStyle().setProperty("color", "#6c757d");
    assetDetails.add(assetDate);
    
    if (existingAsset.getFolder() != null && !existingAsset.getFolder().isEmpty()) {
      Label assetFolder = new Label("Folder: " + existingAsset.getFolder());
      assetFolder.setStyleName("ode-ComponentRowLabel");
      assetFolder.getElement().getStyle().setProperty("fontSize", "12px");
      assetFolder.getElement().getStyle().setProperty("color", "#6c757d");
      assetDetails.add(assetFolder);
    }
    
    assetInfo.add(assetDetails);
    dialogPanel.add(assetInfo);

    // Project usage info - enhanced with async loading
    VerticalPanel usagePanel = new VerticalPanel();
    usagePanel.setWidth("100%");
    usagePanel.getElement().getStyle().setProperty("backgroundColor", "#e3f2fd");
    usagePanel.getElement().getStyle().setProperty("padding", "12px");
    usagePanel.getElement().getStyle().setProperty("borderRadius", "6px");
    usagePanel.getElement().getStyle().setProperty("border", "1px solid #bbdefb");
    
    Label usageTitle = new Label(" Project Usage Impact");
    usageTitle.setStyleName("ode-ComponentRowLabel");
    usageTitle.getElement().getStyle().setProperty("fontWeight", "600");
    usageTitle.getElement().getStyle().setProperty("fontSize", "14px");
    usageTitle.getElement().getStyle().setProperty("color", "#1565c0");
    usagePanel.add(usageTitle);
    
    final Label usageInfo = new Label("Checking projects using this asset...");
    usageInfo.setStyleName("ode-ComponentRowLabel");
    usageInfo.getElement().getStyle().setProperty("fontSize", "13px");
    usageInfo.getElement().getStyle().setProperty("color", "#1976d2");
    usagePanel.add(usageInfo);
    
    dialogPanel.add(usagePanel);

    // Load project usage info asynchronously
    globalAssetService.getProjectsUsingAsset(filename, new AsyncCallback<List<Long>>() {
      @Override
      public void onSuccess(List<Long> projectIds) {
        if (projectIds != null && !projectIds.isEmpty()) {
          usageInfo.setText("[warning] This asset is used by " + projectIds.size() + 
                          " project(s). Updating will affect all projects using it.");
          usageInfo.getElement().getStyle().setProperty("color", "#d32f2f");
        } else {
          usageInfo.setText(" This asset is not currently used by any projects.");
          usageInfo.getElement().getStyle().setProperty("color", "#388e3c");
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        usageInfo.setText("Unable to check project usage.");
        usageInfo.getElement().getStyle().setProperty("color", "#f57c00");
      }
    });

    // Options section
    Label optionsLabel = new Label("Choose your action:");
    optionsLabel.setStyleName("ode-ComponentRowLabel");
    optionsLabel.getElement().getStyle().setProperty("fontWeight", "600");
    optionsLabel.getElement().getStyle().setProperty("fontSize", "15px");
    optionsLabel.getElement().getStyle().setProperty("marginTop", "8px");
    dialogPanel.add(optionsLabel);

    // Option 1: Replace existing (creates new version)
    final RadioButton replaceRadio = new RadioButton("updateOption", " Replace Existing Asset (Create New Version)");
    replaceRadio.setValue(true);
    replaceRadio.setStyleName("ode-ComponentRowLabel");
    replaceRadio.getElement().getStyle().setProperty("marginTop", "8px");
    dialogPanel.add(replaceRadio);

    Label replaceDesc = new Label(" Updates the asset with your new file");
    replaceDesc.setStyleName("ode-ComponentRowLabel");
    replaceDesc.getElement().getStyle().setProperty("fontSize", "12px");
    replaceDesc.getElement().getStyle().setProperty("color", "#666666");
    replaceDesc.getElement().getStyle().setProperty("marginLeft", "24px");
    dialogPanel.add(replaceDesc);

    Label replaceDesc2 = new Label(" All projects using this asset will get the updated version");
    replaceDesc2.setStyleName("ode-ComponentRowLabel");
    replaceDesc2.getElement().getStyle().setProperty("fontSize", "12px");
    replaceDesc2.getElement().getStyle().setProperty("color", "#666666");
    replaceDesc2.getElement().getStyle().setProperty("marginLeft", "24px");
    dialogPanel.add(replaceDesc2);

    Label replaceDesc3 = new Label(" Previous version will be overwritten (cannot be undone)");
    replaceDesc3.setStyleName("ode-ComponentRowLabel");
    replaceDesc3.getElement().getStyle().setProperty("fontSize", "12px");
    replaceDesc3.getElement().getStyle().setProperty("color", "#d32f2f");
    replaceDesc3.getElement().getStyle().setProperty("marginLeft", "24px");
    dialogPanel.add(replaceDesc3);

    // Option 2: Save with different name
    final RadioButton renameRadio = new RadioButton("updateOption", " Save with Different Name");
    renameRadio.setStyleName("ode-ComponentRowLabel");
    renameRadio.getElement().getStyle().setProperty("marginTop", "12px");
    dialogPanel.add(renameRadio);

    Label renameDesc = new Label(" Creates a new asset without affecting the existing one");
    renameDesc.setStyleName("ode-ComponentRowLabel");
    renameDesc.getElement().getStyle().setProperty("fontSize", "12px");
    renameDesc.getElement().getStyle().setProperty("color", "#666666");
    renameDesc.getElement().getStyle().setProperty("marginLeft", "24px");
    dialogPanel.add(renameDesc);

    Label renameDesc2 = new Label(" Existing projects will continue using the current version");
    renameDesc2.setStyleName("ode-ComponentRowLabel");
    renameDesc2.getElement().getStyle().setProperty("fontSize", "12px");
    renameDesc2.getElement().getStyle().setProperty("color", "#666666");
    renameDesc2.getElement().getStyle().setProperty("marginLeft", "24px");
    dialogPanel.add(renameDesc2);

    // Name input for rename option
    HorizontalPanel namePanel = new HorizontalPanel();
    namePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    namePanel.getElement().getStyle().setProperty("marginLeft", "24px");
    namePanel.getElement().getStyle().setProperty("marginTop", "8px");
    
    Label nameLabel = new Label("New name: ");
    nameLabel.setStyleName("ode-ComponentRowLabel");
    nameLabel.getElement().getStyle().setProperty("fontSize", "12px");
    namePanel.add(nameLabel);
    
    final TextBox nameBox = new TextBox();
    nameBox.setText(suggestNewAssetName(filename));
    nameBox.setStyleName("ode-TextBox");
    nameBox.setEnabled(false);
    nameBox.getElement().getStyle().setProperty("fontSize", "12px");
    nameBox.getElement().getStyle().setProperty("width", "200px");
    namePanel.add(nameBox);
    
    dialogPanel.add(namePanel);

    // Enable/disable name input based on radio selection
    replaceRadio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        nameBox.setEnabled(false);
      }
    });
    
    renameRadio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        nameBox.setEnabled(true);
        nameBox.setFocus(true);
      }
    });

    // Button panel
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);
    buttonPanel.getElement().getStyle().setProperty("marginTop", "20px");

    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyleName("ode-ProjectListButton");
    cancelBtn.getElement().getStyle().setProperty("backgroundColor", "#6c757d");
    cancelBtn.getElement().getStyle().setProperty("color", "white");

    Button proceedBtn = new Button("Proceed");
    proceedBtn.setStyleName("ode-ProjectListButton");
    proceedBtn.getElement().getStyle().setProperty("backgroundColor", "#007bff");
    proceedBtn.getElement().getStyle().setProperty("color", "white");
    proceedBtn.getElement().getStyle().setProperty("fontWeight", "600");

    buttonPanel.add(cancelBtn);
    buttonPanel.add(proceedBtn);
    dialogPanel.add(buttonPanel);

    confirmDialog.setWidget(dialogPanel);

    // Event handlers
    proceedBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (replaceRadio.getValue()) {
          // Replace existing asset
          confirmDialog.hide();
          proceedWithUpload(filename, form);
        } else if (renameRadio.getValue()) {
          // Save with new name
          String newName = nameBox.getText().trim();
          if (newName.isEmpty()) {
            Window.alert("Please enter a new name for the asset.");
            return;
          }
          if (newName.equals(filename)) {
            Window.alert("New name must be different from the existing asset name.");
            return;
          }
          
          // Check if new name already exists
          boolean nameExists = false;
          for (GlobalAsset asset : globalAssets) {
            if (asset.getFileName().equals(newName)) {
              nameExists = true;
              break;
            }
          }
          
          if (nameExists) {
            Window.alert("An asset with the name '" + newName + "' already exists. Please choose a different name.");
            return;
          }
          
          confirmDialog.hide();
          proceedWithUpload(newName, form);
        }
      }
    });

    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        confirmDialog.hide();
      }
    });

    confirmDialog.center();
    confirmDialog.show();
  }

  private String suggestNewAssetName(String originalName) {
    String baseName = originalName;
    String extension = "";
    
    int lastDot = originalName.lastIndexOf('.');
    if (lastDot > 0) {
      baseName = originalName.substring(0, lastDot);
      extension = originalName.substring(lastDot);
    }
    
    // Try numbered variations
    for (int i = 2; i <= 100; i++) {
      String suggestion = baseName + "_" + i + extension;
      boolean exists = false;
      for (GlobalAsset asset : globalAssets) {
        if (asset.getFileName().equals(suggestion)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        return suggestion;
      }
    }
    
    // Fallback with timestamp
    return baseName + "_" + System.currentTimeMillis() + extension;
  }

  private void proceedWithUpload(String filename, FormPanel form) {
    String actualFilename = filename;
    if (filename.contains("\\")) {
      actualFilename = filename.substring(filename.lastIndexOf("\\") + 1);
    } else if (filename.contains("/")) {
      actualFilename = filename.substring(filename.lastIndexOf("/") + 1);
    }
    
    final String validatedFilename = makeValidFilename(actualFilename);
    if (!TextValidators.isValidCharFilename(validatedFilename)) {
      showUploadError(MESSAGES.malformedFilenameTitle(), MESSAGES.malformedFilename());
      return;
    } else if (!TextValidators.isValidLengthFilename(validatedFilename)) {
      showUploadError(MESSAGES.filenameBadSizeTitle(), MESSAGES.filenameBadSize());
      return;
    }
    
    actualFilename = validatedFilename;
    
    // Get the currently selected folder
    String targetFolder = "";
    if (selectedFolderIndex >= 0 && selectedFolderIndex < folders.size()) {
      String selectedFolder = folders.get(selectedFolderIndex);
      // Don't use special folders as target folders - use empty string for root
      if (!isSpecialFolder(selectedFolder)) {
        targetFolder = selectedFolder;
      }
    }
    
    // Construct proper upload path: _global_/folder/filename or _global_/filename
    String uploadPath = "_global_/";
    if (!targetFolder.isEmpty()) {
      uploadPath += targetFolder + "/";
    }
    uploadPath += actualFilename;
    
    form.setAction(GWT.getModuleBaseURL() + "upload/" + ServerLayout.UPLOAD_GLOBAL_ASSET + "/" + uploadPath);
    form.submit();
  }

  private void showAddToProjectDialog(final GlobalAsset asset) {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Add Asset to Project");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(8);

    // Asset info
    HorizontalPanel assetInfo = new HorizontalPanel();
    assetInfo.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    String fileName = asset.getFileName().toLowerCase();
    ImageResource iconRes = fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".ogg")
      ? images.player() 
      : images.image();
    
    Image assetIcon = new Image(iconRes);
    assetInfo.add(assetIcon);

    assetInfo.add(new HTML("&nbsp;"));

    Label assetName = new Label(asset.getFileName());
    assetName.setStyleName("ode-ComponentRowLabel");
    assetInfo.add(assetName);

    dialogPanel.add(assetInfo);

    // Options
    Label optionsLabel = new Label("How would you like to add this asset?");
    optionsLabel.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(optionsLabel);

    final RadioButton trackRadio = new RadioButton("addOption", "Track Asset (Recommended)");
    trackRadio.setValue(true);
    trackRadio.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(trackRadio);

    Label trackDesc = new Label("The asset will be updated in your project if the library version changes.");
    trackDesc.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(trackDesc);

    final RadioButton copyRadio = new RadioButton("addOption", "Copy Asset");
    copyRadio.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(copyRadio);

    Label copyDesc = new Label("A copy will be added to your project and will not be updated.");
    copyDesc.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(copyDesc);

    // Button panel
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(4);

    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyleName("ode-ProjectListButton");

    Button addBtn = new Button("Add to Project");
    addBtn.setStyleName("ode-ProjectListButton");

    buttonPanel.add(cancelBtn);
    buttonPanel.add(addBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    // Event handlers
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean track = trackRadio.getValue();
        long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
        
        globalAssetService.importAssetIntoProject(asset.getFileName(), String.valueOf(projectId), track, 
          new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
              dialog.hide();
              
              // Manually create and add the project node for the imported asset
              Project project = Ode.getInstance().getProjectManager().getProject(projectId);
              YoungAndroidProjectNode projectNode = (YoungAndroidProjectNode) project.getRootNode();
              YoungAndroidAssetsFolder assetsFolder = projectNode.getAssetsFolder();
              
              // Create the asset node with the full imported path
              String assetName = asset.getFileName();
              String fullAssetPath = "assets/_global_/";
              if (asset.getFolder() != null && !asset.getFolder().isEmpty()) {
                fullAssetPath += asset.getFolder() + "/";
              }
              fullAssetPath += assetName;
              
              YoungAndroidAssetNode assetNode = new YoungAndroidAssetNode(assetName, fullAssetPath);
              project.addNode(assetsFolder, assetNode);
              
              // Refresh the project asset list and asset manager to make the asset visible
              Ode.getInstance().getAssetListBox().getAssetList().refreshAssetList(projectId);
              Ode.getInstance().getAssetManager().loadAssets(projectId);
            }
            
            @Override
            public void onFailure(Throwable caught) {
              Window.alert("Failed to add asset to project: " + caught.getMessage());
            }
          });
      }
    });

    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    dialog.center();
    dialog.show();
  }

  private void showBulkAddToProjectDialog(final List<GlobalAsset> assets) {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Add " + assets.size() + " Assets to Project");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(8);

    // Assets list preview
    Label assetsLabel = new Label("Selected assets (" + assets.size() + "):");
    assetsLabel.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(assetsLabel);

    // Scrollable list of asset names
    ScrollPanel assetListScroll = new ScrollPanel();
    assetListScroll.setHeight("100px");

    VerticalPanel assetList = new VerticalPanel();
    for (GlobalAsset asset : assets) {
      Label assetName = new Label(" " + asset.getFileName());
      assetName.setStyleName("ode-ComponentRowLabel");
      assetList.add(assetName);
    }
    assetListScroll.add(assetList);
    dialogPanel.add(assetListScroll);

    // Options
    Label optionsLabel = new Label("How would you like to add these assets?");
    optionsLabel.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(optionsLabel);

    final RadioButton trackRadio = new RadioButton("bulkAddOption", "Track Usage (Recommended)");
    trackRadio.setValue(true);
    trackRadio.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(trackRadio);

    Label trackDesc = new Label("Assets will be updated in your project if the library versions change.");
    trackDesc.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(trackDesc);

    final RadioButton copyRadio = new RadioButton("bulkAddOption", "Copy Assets");
    copyRadio.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(copyRadio);

    Label copyDesc = new Label("Copies will be added to your project and will not be updated.");
    copyDesc.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(copyDesc);

    // Button panel
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(4);

    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyleName("ode-ProjectListButton");

    final Button addBtn = new Button("Add " + assets.size() + " Assets");
    addBtn.setStyleName("ode-ProjectListButton");

    buttonPanel.add(cancelBtn);
    buttonPanel.add(addBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    // Event handlers
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean track = trackRadio.getValue();
        long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
        
        // Collect asset filenames
        List<String> assetFileNames = new ArrayList<>();
        for (GlobalAsset asset : assets) {
          assetFileNames.add(asset.getFileName());
        }
        
        // Use bulk add method
        globalAssetService.bulkAddAssetsToProject(assetFileNames, projectId, track,
          new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
              dialog.hide();
              
              // Manually create and add project nodes for all imported assets
              Project project = Ode.getInstance().getProjectManager().getProject(projectId);
              YoungAndroidProjectNode projectNode = (YoungAndroidProjectNode) project.getRootNode();
              YoungAndroidAssetsFolder assetsFolder = projectNode.getAssetsFolder();
              
              for (GlobalAsset asset : assets) {
                // Create the asset node with the full imported path
                String assetName = asset.getFileName();
                String fullAssetPath = "assets/_global_/";
                if (asset.getFolder() != null && !asset.getFolder().isEmpty()) {
                  fullAssetPath += asset.getFolder() + "/";
                }
                fullAssetPath += assetName;
                
                YoungAndroidAssetNode assetNode = new YoungAndroidAssetNode(assetName, fullAssetPath);
                project.addNode(assetsFolder, assetNode);
              }
              
              // Refresh the project asset list and asset manager to make the assets visible
              Ode.getInstance().getAssetListBox().getAssetList().refreshAssetList(projectId);
              Ode.getInstance().getAssetManager().loadAssets(projectId);
            }
            
            @Override
            public void onFailure(Throwable caught) {
              Window.alert("Failed to add assets: " + caught.getMessage());
            }
          });
      }
    });

    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    dialog.center();
    dialog.show();
  }



  // Folder management dialogs
  private void showNewFolderDialog() {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Create New Folder");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(8);

    Label nameLabel = new Label("Folder name:");
    nameLabel.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(nameLabel);

    final TextBox nameBox = new TextBox();
    nameBox.setStyleName("ode-TextBox");
    dialogPanel.add(nameBox);

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(4);

    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyleName("ode-ProjectListButton");
    
    Button createBtn = new Button("Create");
    createBtn.setStyleName("ode-ProjectListButton");

    buttonPanel.add(cancelBtn);
    buttonPanel.add(createBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    createBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String folderName = nameBox.getText().trim();
        if (folderName.isEmpty()) {
          Window.alert("Please enter a folder name.");
          return;
        }
        if (folders.contains(folderName)) {
          Window.alert("A folder with this name already exists.");
          return;
        }
        
        folders.add(folders.size() - 1, folderName); // Insert before "Recent"
        updateFolderList();
        dialog.hide();
      }
    });

    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    dialog.center();
    dialog.show();
    nameBox.setFocus(true);
  }

  private void showRenameFolderDialog(final String oldFolderName) {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Rename Folder");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);
    dialog.setAutoHideEnabled(false);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth("300px");
    dialogPanel.getElement().getStyle().setProperty("padding", "16px");

    Label nameLabel = new Label("New folder name:");
    nameLabel.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(nameLabel);

    final TextBox nameBox = new TextBox();
    nameBox.setText(oldFolderName);
    nameBox.setStyleName("ode-TextBox");
    nameBox.getElement().getStyle().setProperty("width", "100%");
    dialogPanel.add(nameBox);

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);
    buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    buttonPanel.setWidth("100%");
    buttonPanel.getElement().getStyle().setProperty("marginTop", "12px");

    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyleName("ode-ProjectListButton");
    
    Button renameBtn = new Button("Rename");
    renameBtn.setStyleName("ode-ProjectListButton");

    buttonPanel.add(cancelBtn);
    buttonPanel.add(renameBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    renameBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String newFolderName = nameBox.getText().trim();
        if (newFolderName.isEmpty()) {
          Window.alert("Please enter a folder name.");
          return;
        }
        if (newFolderName.equals(oldFolderName)) {
          dialog.hide();
          return;
        }
        if (folders.contains(newFolderName)) {
          Window.alert("A folder with this name already exists.");
          return;
        }

        // Update all assets in this folder
        renameFolderForAssets(oldFolderName, newFolderName);
        dialog.hide();
      }
    });

    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    dialog.center();
    dialog.show();
    nameBox.selectAll();
    nameBox.setFocus(true);
  }

  private void showDeleteFolderDialog(final String folderName) {
    // Count assets in this folder
    int assetCount = 0;
    for (GlobalAsset asset : globalAssets) {
      if (folderName.equals(asset.getFolder())) {
        assetCount++;
      }
    }

    String message = assetCount > 0 
        ? "Delete folder '" + folderName + "' and move " + assetCount + " asset(s) to root folder?"
        : "Delete empty folder '" + folderName + "'?";

    if (Window.confirm(message)) {
      deleteFolderAndMoveAssets(folderName);
    }
  }

  private void renameFolderForAssets(final String oldFolderName, final String newFolderName) {
    final List<GlobalAsset> assetsToUpdate = new ArrayList<GlobalAsset>();
    
    // Find assets to update
    for (GlobalAsset asset : globalAssets) {
      if (oldFolderName.equals(asset.getFolder())) {
        assetsToUpdate.add(asset);
      }
    }
    
    if (assetsToUpdate.isEmpty()) {
      // Just update the folder list
      int index = folders.indexOf(oldFolderName);
      if (index >= 0) {
        folders.set(index, newFolderName);
        updateFolderList();
      }
      return;
    }

    // Update each asset's folder using the existing API
    final int totalAssets = assetsToUpdate.size();
    final int[] completedCount = {0};
    
    for (final GlobalAsset asset : assetsToUpdate) {
      globalAssetService.updateGlobalAssetFolder(asset.getFileName(), newFolderName, new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          completedCount[0]++;
          if (completedCount[0] == totalAssets) {
            // All assets updated successfully
            int index = folders.indexOf(oldFolderName);
            if (index >= 0) {
              folders.set(index, newFolderName);
              updateFolderList();
            }
            refreshGlobalAssets();
          }
        }
        
        @Override
        public void onFailure(Throwable caught) {
          Window.alert("Failed to rename folder: " + caught.getMessage());
        }
      });
    }
  }

  private void deleteFolderAndMoveAssets(final String folderName) {
    
    final List<GlobalAsset> assetsToMove = new ArrayList<GlobalAsset>();
    for (GlobalAsset asset : globalAssets) {
      if (folderName.equals(asset.getFolder())) {
        assetsToMove.add(asset);
      }
    }
    
    if (assetsToMove.isEmpty()) {
      // Just remove from folder list
      folders.remove(folderName);
      if (selectedFolderIndex > 0) {
        selectedFolderIndex = 0; // Reset to "All Assets"
      }
      updateFolderList();
      refreshAssetList();
    } else {
      // Move all assets to root folder (empty string)
      final int totalAssets = assetsToMove.size();
      final int[] completedCount = {0};
      
      for (final GlobalAsset asset : assetsToMove) {
        globalAssetService.updateGlobalAssetFolder(asset.getFileName(), "", new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            completedCount[0]++;
            if (completedCount[0] == totalAssets) {
              // All assets moved successfully, now remove folder
              folders.remove(folderName);
              if (selectedFolderIndex > 0) {
                selectedFolderIndex = 0; // Reset to "All Assets"
              }
              updateFolderList();
              refreshGlobalAssets();
            }
          }
          
          @Override
          public void onFailure(Throwable caught) {
            Window.alert("Failed to delete folder: " + caught.getMessage());
          }
        });
      }
    }
  }
  /**
   * Enhanced asset upload handler with conflict detection and resolution.
   * Uses the new AssetUploadConflictDialog for better UX.
   */
  private void handleAssetUploadWithConflictCheck(String filename, DialogBox uploadDialog, 
                                                 FormPanel form, FileUpload fileUpload) {
    // First check if asset exists using backend service
    globalAssetService.assetExists(filename, new AsyncCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean exists) {
        if (exists) {
          // Get detailed conflict information
          globalAssetService.getAssetConflictInfo(filename, new AsyncCallback<AssetConflictInfo>() {
            @Override
            public void onSuccess(AssetConflictInfo conflictInfo) {
              showEnhancedConflictDialog(filename, conflictInfo, uploadDialog, form, fileUpload);
            }
            
            @Override
            public void onFailure(Throwable caught) {
              // Fallback to simple existence check
              showSimpleConflictDialog(filename, uploadDialog, form, fileUpload);
            }
          });
        } else {
          // No conflict, proceed with upload
          uploadDialog.hide();
          proceedWithUpload(filename, form);
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        // Fallback to client-side check
        handleAssetUploadFallback(filename, uploadDialog, form, fileUpload);
      }
    });
  }

  /**
   * Shows the enhanced conflict resolution dialog with project impact information.
   */
  private void showEnhancedConflictDialog(String filename, AssetConflictInfo conflictInfo, 
                                         DialogBox uploadDialog, FormPanel form, FileUpload fileUpload) {
    // Extract project names from conflict info
    List<String> projectNames = new ArrayList<String>();
    if (conflictInfo.getAffectedProjects() != null) {
      for (AssetConflictInfo.ProjectInfo projectInfo : conflictInfo.getAffectedProjects()) {
        projectNames.add(projectInfo.getProjectName());
      }
    }
    
    AssetUploadConflictDialog conflictDialog = new AssetUploadConflictDialog(
        filename, 
        conflictInfo.getExistingAsset(), 
        projectNames,
        new ConflictResolutionCallback() {
          @Override
          public void onResolutionSelected(ConflictResolution resolution, 
                                         String newAssetName, boolean notifyProjects) {
            handleConflictResolution(resolution, filename, newAssetName, notifyProjects, 
                                   uploadDialog, form, fileUpload);
          }
        }
    );
    
    conflictDialog.show();
  }

  /**
   * Shows a simple conflict dialog as fallback when detailed info is unavailable.
   */
  private void showSimpleConflictDialog(String filename, DialogBox uploadDialog, 
                                       FormPanel form, FileUpload fileUpload) {
    boolean confirmed = Window.confirm(
        "Asset '" + filename + "' already exists.\n\n" +
        "Do you want to replace it? This may affect projects using this asset.");
    
    if (confirmed) {
      uploadDialog.hide();
      proceedWithUpload(filename, form);
    }
    // If not confirmed, do nothing (keep upload dialog open)
  }

  /**
   * Handles the selected conflict resolution option.
   */
  private void handleConflictResolution(ConflictResolution resolution,
                                       String originalFilename, String newAssetName, 
                                       boolean notifyProjects, DialogBox uploadDialog, 
                                       FormPanel form, FileUpload fileUpload) {
    uploadDialog.hide();
    
    switch (resolution) {
      case REPLACE_EXISTING:
        // TODO: If notifyProjects is true, could send notifications
        proceedWithUpload(originalFilename, form);
        break;
        
      case CREATE_NEW_ASSET:
        if (newAssetName != null && !newAssetName.trim().isEmpty()) {
          proceedWithUpload(newAssetName.trim(), form);
        } else {
          Window.alert("Invalid new asset name provided.");
        }
        break;
        
      case SAVE_AS_DRAFT:
        // TODO: Implement draft functionality
        Window.alert("Draft functionality not yet implemented. Using replace for now.");
        proceedWithUpload(originalFilename, form);
        break;
    }
  }

  /**
   * Fallback conflict handling using client-side asset list.
   */
  private void handleAssetUploadFallback(String filename, DialogBox uploadDialog, 
                                        FormPanel form, FileUpload fileUpload) {
    // Check client-side asset list
    GlobalAsset existingAsset = null;
    for (GlobalAsset asset : globalAssets) {
      if (asset.getFileName().equals(filename)) {
        existingAsset = asset;
        break;
      }
    }
    
    if (existingAsset != null) {
      showSimpleConflictDialog(filename, uploadDialog, form, fileUpload);
    } else {
      uploadDialog.hide();
      proceedWithUpload(filename, form);
  }
    }

  /**
   * Shows version history dialog for an asset.
   */
  private void showVersionHistory(GlobalAsset asset) {
    AssetVersionHistoryDialog historyDialog = new AssetVersionHistoryDialog(asset, 
        new AssetVersionHistoryDialog.VersionActionCallback() {
          @Override
          public void onRollback(long timestamp) {
            // TODO: Implement rollback functionality
            Window.alert("Rollback functionality will be implemented in future versions.");
          }
          
          @Override
          public void onViewVersion(long timestamp) {
            // TODO: Implement version viewing
            Window.alert("Version viewing will be implemented in future versions.");
          }
        });
    
    historyDialog.show();
  }
  
  /**
   * Shows an inline error message with consistent styling
   */
  private void showInlineError(Label errorLabel, String message) {
    errorLabel.setText(message);
    errorLabel.getElement().getStyle().setProperty("color", "#d32f2f");
    errorLabel.getElement().getStyle().setProperty("fontSize", "13px");
    errorLabel.getElement().getStyle().setProperty("fontWeight", "500");
    errorLabel.getElement().getStyle().setProperty("marginTop", "4px");
  }
  
  /**
   * Clears any inline error styling and message
   */
  private void clearInlineError(Label errorLabel) {
    errorLabel.setText("");
    errorLabel.getElement().getStyle().setProperty("color", "");
    errorLabel.getElement().getStyle().setProperty("fontSize", "");
    errorLabel.getElement().getStyle().setProperty("fontWeight", "");
    errorLabel.getElement().getStyle().setProperty("marginTop", "");
  }

  /**
   * Shows a user-friendly error dialog for upload failures
   */
  private void showUploadError(String title, String message) {
    final DialogBox errorDialog = new DialogBox();
    errorDialog.setText(title);
    errorDialog.setModal(true);
    errorDialog.setGlassEnabled(true);
    errorDialog.setStyleName("ode-DialogBox");

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth("400px");

    // Error icon and message
    HorizontalPanel messagePanel = new HorizontalPanel();
    messagePanel.setSpacing(8);
    messagePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    
    Label errorIcon = new Label("⚠");
    errorIcon.getElement().getStyle().setProperty("fontSize", "24px");
    errorIcon.getElement().getStyle().setProperty("color", "#d32f2f");
    messagePanel.add(errorIcon);
    
    Label errorMessage = new Label(message);
    errorMessage.setStyleName("ode-ComponentRowLabel");
    errorMessage.getElement().getStyle().setProperty("wordWrap", "break-word");
    errorMessage.setWidth("340px");
    messagePanel.add(errorMessage);
    
    dialogPanel.add(messagePanel);

    // Buttons
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);
    buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    Button retryButton = new Button("Try Again");
    retryButton.setStyleName("ode-DialogButton");
    retryButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        errorDialog.hide();
        showUploadDialog(); // Reopen the upload dialog
      }
    });

    Button okButton = new Button("OK");
    okButton.setStyleName("ode-DialogButton");
    okButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        errorDialog.hide();
      }
    });

    buttonPanel.add(retryButton);
    buttonPanel.add(okButton);
    dialogPanel.add(buttonPanel);

    errorDialog.setWidget(dialogPanel);
    errorDialog.center();
  }

  /**
   * Creates a valid filename by stripping path and whitespace.
   */
  private String makeValidFilename(String uploadFilename) {
    String filename = uploadFilename.substring(
        Math.max(uploadFilename.lastIndexOf('/'), uploadFilename.lastIndexOf('\\')) + 1);
    filename = filename.replaceAll("\\s", "");
    return filename;
  }

  /**
   * Displays deletion error dialog with better formatting for asset usage information.
   */
  private void showDeleteError(String title, String message) {
    final DialogBox errorDialog = new DialogBox();
    errorDialog.setText(title);
    errorDialog.setStyleName("ode-DialogBox");
    errorDialog.setModal(true);
    errorDialog.setGlassEnabled(true);
    errorDialog.setAutoHideEnabled(false);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(16);
    dialogPanel.setWidth("500px");
    dialogPanel.getElement().getStyle().setProperty("padding", "16px");

    // Error header with icon
    HorizontalPanel errorHeader = new HorizontalPanel();
    errorHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    errorHeader.setSpacing(12);
    errorHeader.getElement().getStyle().setProperty("backgroundColor", "#fef7f7");
    errorHeader.getElement().getStyle().setProperty("padding", "12px");
    errorHeader.getElement().getStyle().setProperty("borderRadius", "6px");
    errorHeader.getElement().getStyle().setProperty("border", "1px solid #f5c6cb");
    errorHeader.getElement().getStyle().setProperty("marginBottom", "12px");
    
    Label errorIcon = new Label("⚠");
    errorIcon.getElement().getStyle().setProperty("fontSize", "24px");
    errorIcon.getElement().getStyle().setProperty("color", "#721c24");
    errorIcon.getElement().getStyle().setProperty("marginRight", "8px");
    errorHeader.add(errorIcon);
    
    VerticalPanel errorText = new VerticalPanel();
    Label errorTitle = new Label("Asset Cannot Be Deleted");
    errorTitle.setStyleName("ode-ComponentRowLabel");
    errorTitle.getElement().getStyle().setProperty("fontWeight", "600");
    errorTitle.getElement().getStyle().setProperty("fontSize", "16px");
    errorTitle.getElement().getStyle().setProperty("color", "#721c24");
    errorText.add(errorTitle);
    
    // Parse the error message to make it more readable
    String displayMessage = message;
    if (message.contains("is currently used by") && message.contains("project(s):")) {
      // Enhanced error message - display it nicely
      displayMessage = message.replace("Cannot delete asset", "This asset");
    }
    
    Label errorMsg = new Label(displayMessage);
    errorMsg.setStyleName("ode-ComponentRowLabel");
    errorMsg.getElement().getStyle().setProperty("fontSize", "14px");
    errorMsg.getElement().getStyle().setProperty("color", "#721c24");
    errorMsg.getElement().getStyle().setProperty("lineHeight", "1.4");
    errorMsg.getElement().getStyle().setProperty("marginTop", "4px");
    errorMsg.getElement().getStyle().setProperty("wordWrap", "break-word");
    errorText.add(errorMsg);
    
    errorHeader.add(errorText);
    dialogPanel.add(errorHeader);
    
    // Help text
    if (message.contains("Please remove the asset from these projects first")) {
      Label helpText = new Label("To delete this asset:");
      helpText.setStyleName("ode-ComponentRowLabel");
      helpText.getElement().getStyle().setProperty("fontWeight", "600");
      helpText.getElement().getStyle().setProperty("marginBottom", "8px");
      dialogPanel.add(helpText);
      
      VerticalPanel stepsList = new VerticalPanel();
      stepsList.getElement().getStyle().setProperty("marginLeft", "16px");
      
      Label step1 = new Label("1. Open each project listed above");
      step1.setStyleName("ode-ComponentRowLabel");
      step1.getElement().getStyle().setProperty("fontSize", "14px");
      step1.getElement().getStyle().setProperty("marginBottom", "4px");
      stepsList.add(step1);
      
      Label step2 = new Label("2. Remove the asset from the project's assets");
      step2.setStyleName("ode-ComponentRowLabel");
      step2.getElement().getStyle().setProperty("fontSize", "14px");
      step2.getElement().getStyle().setProperty("marginBottom", "4px");
      stepsList.add(step2);
      
      Label step3 = new Label("3. Return here to delete the asset");
      step3.setStyleName("ode-ComponentRowLabel");
      step3.getElement().getStyle().setProperty("fontSize", "14px");
      stepsList.add(step3);
      
      dialogPanel.add(stepsList);
    }

    // Button panel
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);
    buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    buttonPanel.setWidth("100%");
    buttonPanel.getElement().getStyle().setProperty("marginTop", "16px");

    Button okButton = new Button("OK");
    okButton.setStyleName("ode-ProjectListButton");
    okButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        errorDialog.hide();
      }
    });
    buttonPanel.add(okButton);
    dialogPanel.add(buttonPanel);

    errorDialog.setWidget(dialogPanel);
    errorDialog.center();
  }

  // Version management methods from Neo version
  private void showUpdateAssetDialog(final GlobalAsset asset) {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Upload New Version - " + asset.getFileName());
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth("400px");

    // Current asset info
    HorizontalPanel currentAssetInfo = new HorizontalPanel();
    currentAssetInfo.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    currentAssetInfo.getElement().getStyle().setProperty("marginBottom", "16px");
    currentAssetInfo.getElement().getStyle().setProperty("padding", "8px");
    currentAssetInfo.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    currentAssetInfo.getElement().getStyle().setProperty("borderRadius", "4px");

    String fileName = asset.getFileName().toLowerCase();
    Image assetIcon;
    if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".ogg")) {
      assetIcon = new Image(images.player());
    } else {
      assetIcon = new Image(images.image());
    }
    assetIcon.setWidth("24px");
    assetIcon.setHeight("24px");
    assetIcon.getElement().getStyle().setProperty("marginRight", "8px");
    currentAssetInfo.add(assetIcon);

    VerticalPanel currentInfo = new VerticalPanel();
    Label currentName = new Label("Current: " + asset.getFileName());
    currentName.setStyleName("ode-ComponentRowLabel");
    currentName.getElement().getStyle().setProperty("fontWeight", "500");
    currentInfo.add(currentName);

    Label currentVersion = new Label("Last updated: " + formatDate(asset.getTimestamp()));
    currentVersion.setStyleName("ode-ComponentRowLabel");
    currentVersion.getElement().getStyle().setProperty("fontSize", "11px");
    currentVersion.getElement().getStyle().setProperty("opacity", "0.7");
    currentInfo.add(currentVersion);
    currentAssetInfo.add(currentInfo);

    dialogPanel.add(currentAssetInfo);

    // Upload form
    final FormPanel form = new FormPanel();
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);

    VerticalPanel formPanel = new VerticalPanel();
    formPanel.setSpacing(8);

    // File input
    Label fileLabel = new Label("Select new version file:");
    fileLabel.setStyleName("ode-ComponentRowLabel");
    formPanel.add(fileLabel);

    final FileUpload fileUpload = new FileUpload();
    fileUpload.setName(ServerLayout.UPLOAD_GLOBAL_ASSET_FORM_ELEMENT);
    fileUpload.setStyleName("ode-TextBox");
    fileUpload.getElement().getStyle().setProperty("width", "100%");
    formPanel.add(fileUpload);

    // Version notes
    Label notesLabel = new Label("Version notes (optional):");
    notesLabel.setStyleName("ode-ComponentRowLabel");
    formPanel.add(notesLabel);

    final TextBox versionNotes = new TextBox();
    versionNotes.setStyleName("ode-TextBox");
    versionNotes.getElement().getStyle().setProperty("width", "100%");
    versionNotes.getElement().setPropertyString("placeholder", "Describe changes in this version...");
    formPanel.add(versionNotes);

    // Auto-update projects checkbox
    final CheckBox autoUpdate = new CheckBox("Automatically update projects using this asset");
    autoUpdate.setValue(true);
    autoUpdate.setStyleName("ode-ComponentRowLabel");
    autoUpdate.getElement().getStyle().setProperty("fontSize", "12px");
    autoUpdate.getElement().getStyle().setProperty("marginTop", "8px");
    formPanel.add(autoUpdate);

    Label autoUpdateDesc = new Label("Projects with tracking enabled will receive this update automatically.");
    autoUpdateDesc.setStyleName("ode-ComponentRowLabel");
    autoUpdateDesc.getElement().getStyle().setProperty("fontSize", "10px");
    autoUpdateDesc.getElement().getStyle().setProperty("opacity", "0.7");
    autoUpdateDesc.getElement().getStyle().setProperty("marginLeft", "20px");
    formPanel.add(autoUpdateDesc);

    // Error label
    final Label errorLabel = new Label();
    errorLabel.getElement().getStyle().setProperty("color", "#d93025");
    errorLabel.getElement().getStyle().setProperty("fontSize", "12px");
    formPanel.add(errorLabel);

    form.setWidget(formPanel);
    dialogPanel.add(form);

    // Button panel
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);
    buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    buttonPanel.setWidth("100%");

    Button cancelBtn = new Button("Cancel");
    cancelBtn.setStyleName("ode-ProjectListButton");

    Button uploadBtn = new Button("Upload New Version");
    uploadBtn.setStyleName("ode-ProjectListButton");

    buttonPanel.add(cancelBtn);
    buttonPanel.add(uploadBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    // Event handlers
    uploadBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String filename = fileUpload.getFilename();
        if (filename == null || filename.isEmpty()) {
          errorLabel.setText("Please select a file.");
          return;
        }
        
        // Extract actual filename
        String actualFilename = filename;
        if (filename.contains("\\")) {
          actualFilename = filename.substring(filename.lastIndexOf("\\") + 1);
        } else if (filename.contains("/")) {
          actualFilename = filename.substring(filename.lastIndexOf("/") + 1);
        }
        
        // Validate file type matches existing asset
        String newExt = getFileExtension(actualFilename).toLowerCase();
        String currentExt = getFileExtension(asset.getFileName()).toLowerCase();
        if (!newExt.equals(currentExt)) {
          errorLabel.setText("New version must have the same file type as the current asset (" + currentExt + ").");
          return;
        }
        
        // Use the original asset name for the upload (maintaining the same filename)
        String uploadPath = "_global_/";
        if (asset.getFolder() != null && !asset.getFolder().isEmpty()) {
          uploadPath += asset.getFolder() + "/";
        }
        uploadPath += asset.getFileName(); // Keep the same filename
        
        form.setAction(GWT.getModuleBaseURL() + "upload/" + ServerLayout.UPLOAD_GLOBAL_ASSET + "/" + uploadPath);
        form.submit();
      }
    });

    form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete(SubmitCompleteEvent event) {
        dialog.hide();
        String results = event.getResults();
        if (results != null && results.contains("SUCCESS")) {
          refreshGlobalAssets();
          
          // If auto-update is enabled, sync all projects using this asset
          if (autoUpdate.getValue()) {
            syncAssetWithProjects(asset.getFileName());
          }
          
          // Show success notification
          Window.alert("Asset updated successfully!" + 
            (autoUpdate.getValue() ? " Projects using this asset will be updated." : ""));
        } else {
          showUploadError(MESSAGES.fileUploadError(), "Failed to upload new version. Please try again.");
        }
      }
    });

    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    dialog.center();
    dialog.show();
  }


  private void syncAssetWithProjects(String assetFileName) {
    
    // Get current project ID if we're in a project
    long currentProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    
    if (currentProjectId != 0) {
      // Sync with current project
      globalAssetService.syncProjectGlobalAsset(assetFileName, currentProjectId, new AsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            // Force refresh of current project's asset list and editors
            refreshCurrentProjectAssets();
          } else {
          }
        }
        
        @Override
        public void onFailure(Throwable caught) {
        }
      });
    }
    
    // Also get list of all projects using this asset and sync them
    globalAssetService.getProjectsUsingAsset(assetFileName, new AsyncCallback<List<Long>>() {
      @Override
      public void onSuccess(List<Long> projectIds) {
        // The server-side should handle the actual synchronization
        // Client-side we just need to refresh if current project is affected
        if (projectIds.contains(Ode.getInstance().getCurrentYoungAndroidProjectId())) {
          refreshCurrentProjectAssets();
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
      }
    });
  }

  private void refreshCurrentProjectAssets() {
    // Force refresh of project assets and any open editors
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId != 0) {
      // Refresh the project manager - this will update asset lists
      Ode.getInstance().getProjectManager().getProject(projectId);
      
      // The server-side sync should handle updating the project assets
      // Client-side we just notify that a refresh might be needed
    }
  }

}
