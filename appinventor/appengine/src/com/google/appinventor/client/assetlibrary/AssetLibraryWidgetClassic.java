package com.google.appinventor.client.assetlibrary;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.PreviewFileCommand;
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
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.appinventor.shared.rpc.project.GlobalAssetProjectNode;
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
  private Label statusLabel;

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
    // Main root panel - classic table layout
    rootPanel = new VerticalPanel();
    rootPanel.setSize("100%", "100%");
    rootPanel.setStyleName("ode-Box");

    createHeader();
    createMainContent();
    createFooter();
  }

  private void createHeader() {
    // Enhanced header with improved classic App Inventor styling
    headerContainer = new HorizontalPanel();
    headerContainer.setWidth("100%");
    headerContainer.setStyleName("ode-TopPanel");
    headerContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    headerContainer.getElement().getStyle().setProperty("padding", "8px 12px");
    headerContainer.getElement().getStyle().setProperty("borderBottom", "2px solid #1a73e8");
    headerContainer.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");

    // Left section: Title and search
    HorizontalPanel leftSection = new HorizontalPanel();
    leftSection.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    // Enhanced Asset Library Title with icon
    HorizontalPanel titlePanel = new HorizontalPanel();
    titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    // Add library icon
    Image libraryIcon = new Image(images.form());
    libraryIcon.setSize("24px", "24px");
    titlePanel.add(libraryIcon);
    
    Label titleLabel = new Label("Asset Library");
    titleLabel.setStyleName("ode-ProjectNameLabel");
    titleLabel.getElement().getStyle().setProperty("fontWeight", "bold");
    titleLabel.getElement().getStyle().setProperty("fontSize", "16px");
    titleLabel.getElement().getStyle().setProperty("marginLeft", "8px");
    titleLabel.getElement().getStyle().setProperty("color", "#1a73e8");
    titlePanel.add(titleLabel);
    leftSection.add(titlePanel);

    // Add enhanced spacing
    leftSection.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"));

    // Enhanced search box with search icon styling
    searchBox = new TextBox();
    searchBox.getElement().setPropertyString("placeholder", "🔍 Search assets...");
    searchBox.setStyleName("ode-TextBox");
    searchBox.getElement().getStyle().setProperty("minWidth", "200px");
    searchBox.getElement().getStyle().setProperty("padding", "6px 12px");
    searchBox.getElement().getStyle().setProperty("border", "2px solid #e0e0e0");
    searchBox.getElement().getStyle().setProperty("borderRadius", "4px");
    leftSection.add(searchBox);

    // Add spacing
    leftSection.add(new HTML("&nbsp;&nbsp;"));

    // Enhanced type filter
    typeFilter = new ListBox();
    typeFilter.addItem("All Types");
    typeFilter.addItem("Images");
    typeFilter.addItem("Audio");
    typeFilter.addItem("Other");
    typeFilter.setStyleName("ode-ListBox");
    typeFilter.getElement().getStyle().setProperty("minWidth", "120px");
    typeFilter.getElement().getStyle().setProperty("padding", "6px");
    typeFilter.getElement().getStyle().setProperty("border", "2px solid #e0e0e0");
    typeFilter.getElement().getStyle().setProperty("borderRadius", "4px");
    leftSection.add(typeFilter);

    headerContainer.add(leftSection);

    // Spacer
    Label spacer = new Label("");
    spacer.setWidth("100%");
    headerContainer.add(spacer);
    headerContainer.setCellWidth(spacer, "100%");

    // Enhanced right section with improved button styling
    HorizontalPanel rightSection = new HorizontalPanel();
    rightSection.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    rightSection.getElement().getStyle().setProperty("gap", "8px");

    // Enhanced bulk action buttons with icons
    addSelectedButton = new Button("✓ Add Selected");
    addSelectedButton.setStyleName("ode-ProjectListButton");
    addSelectedButton.setEnabled(false);
    addSelectedButton.getElement().getStyle().setProperty("backgroundColor", "#34a853");
    addSelectedButton.getElement().getStyle().setProperty("color", "white");
    addSelectedButton.getElement().getStyle().setProperty("border", "none");
    addSelectedButton.getElement().getStyle().setProperty("borderRadius", "4px");
    addSelectedButton.getElement().getStyle().setProperty("padding", "6px 12px");
    rightSection.add(addSelectedButton);

    rightSection.add(new HTML("&nbsp;"));

    deleteSelectedButton = new Button("🗑 Delete Selected");
    deleteSelectedButton.setStyleName("ode-ProjectListButton");
    deleteSelectedButton.setEnabled(false);
    deleteSelectedButton.getElement().getStyle().setProperty("backgroundColor", "#ea4335");
    deleteSelectedButton.getElement().getStyle().setProperty("color", "white");
    deleteSelectedButton.getElement().getStyle().setProperty("border", "none");
    deleteSelectedButton.getElement().getStyle().setProperty("borderRadius", "4px");
    deleteSelectedButton.getElement().getStyle().setProperty("padding", "6px 12px");
    rightSection.add(deleteSelectedButton);

    rightSection.add(new HTML("&nbsp;"));

    // Enhanced upload button
    uploadButton = new Button("⬆ Upload Asset");
    uploadButton.setStyleName("ode-ProjectListButton");
    uploadButton.getElement().getStyle().setProperty("backgroundColor", "#1a73e8");
    uploadButton.getElement().getStyle().setProperty("color", "white");
    uploadButton.getElement().getStyle().setProperty("border", "none");
    uploadButton.getElement().getStyle().setProperty("borderRadius", "4px");
    uploadButton.getElement().getStyle().setProperty("padding", "6px 12px");
    uploadButton.getElement().getStyle().setProperty("fontWeight", "bold");
    rightSection.add(uploadButton);

    rightSection.add(new HTML("&nbsp;"));

    // Enhanced close button
    closeButton = new Button("✕");
    closeButton.setTitle("Close Asset Library");
    closeButton.setStyleName("ode-ProjectListButton");
    closeButton.getElement().getStyle().setProperty("backgroundColor", "#f44336");
    closeButton.getElement().getStyle().setProperty("color", "white");
    closeButton.getElement().getStyle().setProperty("border", "none");
    closeButton.getElement().getStyle().setProperty("borderRadius", "50%");
    closeButton.getElement().getStyle().setProperty("width", "32px");
    closeButton.getElement().getStyle().setProperty("height", "32px");
    rightSection.add(closeButton);

    headerContainer.add(rightSection);
    rootPanel.add(headerContainer);
  }

  private void createMainContent() {
    // Main content area using classic horizontal split
    mainContentPanel = new HorizontalPanel();
    mainContentPanel.setSize("100%", "100%");
    mainContentPanel.setStyleName("ode-WorkColumns");

    createSidebar();
    createAssetList();

    rootPanel.add(mainContentPanel);
  }

  private void createSidebar() {
    // Enhanced sidebar with improved classic styling
    sidebarPanel = new VerticalPanel();
    sidebarPanel.setWidth("220px");
    sidebarPanel.setHeight("100%");
    sidebarPanel.setStyleName("ode-Designer-LeftColumn");
    sidebarPanel.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");
    sidebarPanel.getElement().getStyle().setProperty("borderRight", "1px solid #e0e0e0");
    sidebarPanel.getElement().getStyle().setProperty("padding", "12px");

    // Enhanced folder section header with better visual hierarchy
    HorizontalPanel folderHeader = new HorizontalPanel();
    folderHeader.setWidth("100%");
    folderHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    folderHeader.getElement().getStyle().setProperty("marginBottom", "12px");
    folderHeader.getElement().getStyle().setProperty("paddingBottom", "8px");
    folderHeader.getElement().getStyle().setProperty("borderBottom", "2px solid #1a73e8");

    Label folderTitle = new Label("📁 Folders");
    folderTitle.setStyleName("ode-ComponentRowLabel");
    folderTitle.getElement().getStyle().setProperty("fontWeight", "bold");
    folderTitle.getElement().getStyle().setProperty("fontSize", "14px");
    folderTitle.getElement().getStyle().setProperty("color", "#1a73e8");
    folderHeader.add(folderTitle);

    // Enhanced action buttons with better styling
    HorizontalPanel folderActions = new HorizontalPanel();
    folderActions.getElement().getStyle().setProperty("gap", "4px");

    Button newFolderBtn = createEnhancedButton("+", "#34a853", "Create folder");
    Button renameFolderBtn = createEnhancedButton("✎", "#ff9800", "Rename folder");  
    Button deleteFolderBtn = createEnhancedButton("✕", "#f44336", "Delete folder");

    // Add event handlers for folder management
    setupFolderManagementHandlers(newFolderBtn, renameFolderBtn, deleteFolderBtn);

    folderActions.add(newFolderBtn);
    folderActions.add(renameFolderBtn);
    folderActions.add(deleteFolderBtn);
    folderHeader.add(folderActions);
    folderHeader.setCellHorizontalAlignment(folderActions, HasHorizontalAlignment.ALIGN_RIGHT);

    sidebarPanel.add(folderHeader);

    // Enhanced folder list with styling
    folderListPanel = new VerticalPanel();
    folderListPanel.setWidth("100%");
    folderListPanel.setSpacing(2);
    folderListPanel.getElement().getStyle().setProperty("maxHeight", "300px");
    folderListPanel.getElement().getStyle().setProperty("overflowY", "auto");
    sidebarPanel.add(folderListPanel);

    mainContentPanel.add(sidebarPanel);
  }

  private Button createSmallButton(String text) {
    Button button = new Button(text);
    button.setStyleName("ode-ProjectListButton");
    button.getElement().getStyle().setProperty("minWidth", "24px");
    button.getElement().getStyle().setProperty("height", "24px");
    button.getElement().getStyle().setProperty("padding", "2px");
    return button;
  }

  private Button createEnhancedButton(String text, String color, String tooltip) {
    Button button = new Button(text);
    button.setStyleName("ode-ProjectListButton");
    button.setTitle(tooltip);
    button.getElement().getStyle().setProperty("minWidth", "28px");
    button.getElement().getStyle().setProperty("height", "28px");
    button.getElement().getStyle().setProperty("padding", "4px");
    button.getElement().getStyle().setProperty("backgroundColor", color);
    button.getElement().getStyle().setProperty("color", "white");
    button.getElement().getStyle().setProperty("border", "none");
    button.getElement().getStyle().setProperty("borderRadius", "4px");
    button.getElement().getStyle().setProperty("fontSize", "12px");
    return button;
  }

  private void createAssetList() {
    // Enhanced asset list container with improved classic styling
    VerticalPanel assetContainer = new VerticalPanel();
    assetContainer.setWidth("100%");
    assetContainer.setHeight("100%");
    assetContainer.setStyleName("ode-Box-body");
    assetContainer.getElement().getStyle().setProperty("padding", "12px");

    // Add assets count header
    HorizontalPanel assetHeader = new HorizontalPanel();
    assetHeader.setWidth("100%");
    assetHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    assetHeader.getElement().getStyle().setProperty("marginBottom", "12px");
    assetHeader.getElement().getStyle().setProperty("paddingBottom", "8px");
    assetHeader.getElement().getStyle().setProperty("borderBottom", "1px solid #e0e0e0");
    
    Label assetsLabel = new Label("📄 Assets");
    assetsLabel.setStyleName("ode-ComponentRowLabel");
    assetsLabel.getElement().getStyle().setProperty("fontWeight", "bold");
    assetsLabel.getElement().getStyle().setProperty("fontSize", "14px");
    assetHeader.add(assetsLabel);
    
    assetContainer.add(assetHeader);

    // Enhanced scrollable list with better styling
    assetScrollPanel = new ScrollPanel();
    assetScrollPanel.setWidth("100%");
    assetScrollPanel.setHeight("450px");  // Slightly taller for better usability
    assetScrollPanel.setStyleName("ode-Explorer");
    assetScrollPanel.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    assetScrollPanel.getElement().getStyle().setProperty("borderRadius", "4px");
    assetScrollPanel.getElement().getStyle().setProperty("backgroundColor", "white");

    // Enhanced asset list panel with better spacing
    assetListPanel = new VerticalPanel();
    assetListPanel.setWidth("100%");
    assetListPanel.setSpacing(1);  // Tighter spacing for cleaner look
    assetListPanel.getElement().getStyle().setProperty("padding", "4px");

    assetScrollPanel.add(assetListPanel);
    assetContainer.add(assetScrollPanel);
    mainContentPanel.add(assetContainer);
    mainContentPanel.setCellWidth(assetContainer, "100%");
    mainContentPanel.setCellHeight(assetContainer, "100%");
  }

  private void createFooter() {
    // Footer with classic styling
    footerPanel = new HorizontalPanel();
    footerPanel.setWidth("100%");
    footerPanel.setStyleName("ode-StatusPanel");
    footerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    statusLabel = new Label("Loading assets...");
    statusLabel.setStyleName("ode-StatusPanelLabel");
    footerPanel.add(statusLabel);

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
        Ode.getInstance().switchToProjectsView();
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

    // Folder name
    Label nameLabel = new Label(folderName);
    nameLabel.setStyleName("ode-ComponentRowLabel");
    folderRow.add(nameLabel);

    // Apply selection styling
    if (index == selectedFolderIndex) {
      folderRow.setStyleName("ode-ComponentRowHighlighted");
    } else {
      folderRow.setStyleName("ode-ComponentRowUnHighlighted");
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
    statusLabel.setText("Loading assets...");
    globalAssetService.getGlobalAssets(new AsyncCallback<List<GlobalAsset>>() {
      @Override
      public void onSuccess(List<GlobalAsset> assets) {
        globalAssets.clear();
        if (assets != null) {
          globalAssets.addAll(assets);
        }
        statusLabel.setText("Assets loaded successfully");
        updateFoldersFromAssets();
        refreshAssetList();
      }

      @Override
      public void onFailure(Throwable caught) {
        statusLabel.setText("Failed to load assets: " + caught.getMessage());
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
    updateStatusLabel(filteredAssets.size());
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

    // Empty message
    Label emptyMessage = new Label("No assets found");
    emptyMessage.setStyleName("ode-ComponentRowLabel");
    emptyState.add(emptyMessage);

    // Sub message
    Label emptySubMessage = new Label("Try adjusting your search or upload new assets");
    emptySubMessage.setStyleName("ode-ComponentRowLabel");
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
    row.getElement().getStyle().setProperty("minHeight", "40px");
    row.getElement().getStyle().setProperty("padding", "8px");
    row.getElement().getStyle().setProperty("borderBottom", "1px solid #eee");

    // Checkbox for selection
    final CheckBox checkBox = new CheckBox();
    assetCheckBoxes.add(checkBox);
    row.add(checkBox);

    // Asset preview/icon
    Widget previewWidget = createPreviewWidget(asset);
    row.add(previewWidget);

    // Spacing
    row.add(new HTML("&nbsp;"));

    // Asset name and details
    VerticalPanel detailsPanel = new VerticalPanel();
    
    Label nameLabel = new Label(asset.getFileName());
    nameLabel.setStyleName("ode-ComponentRowLabel");
    detailsPanel.add(nameLabel);

    Label dateLabel = new Label(formatDate(asset.getTimestamp()));
    dateLabel.setStyleName("ode-ComponentRowLabel");
    detailsPanel.add(dateLabel);

    row.add(detailsPanel);
    row.setCellWidth(detailsPanel, "100%");

    // Action buttons
    HorizontalPanel actionPanel = new HorizontalPanel();

    // Preview button
    Button previewBtn = new Button("Preview");
    previewBtn.setStyleName("ode-ProjectListButton");
    previewBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        previewAsset(asset);
      }
    });
    actionPanel.add(previewBtn);

    actionPanel.add(new HTML("&nbsp;"));

    // Add button
    Button addBtn = new Button("Add");
    addBtn.setStyleName("ode-ProjectListButton");
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showAddToProjectDialog(asset);
      }
    });
    actionPanel.add(addBtn);

    actionPanel.add(new HTML("&nbsp;"));

    // Delete button
    Button deleteBtn = new Button("Delete");
    deleteBtn.setStyleName("ode-ProjectListButton");
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

  private Widget createPreviewWidget(GlobalAsset asset) {
    String fileName = asset.getFileName().toLowerCase();
    String filePath = asset.getFolder() != null ? asset.getFolder() + "/" + asset.getFileName() : asset.getFileName();
    
    if (StorageUtil.isImageFile(filePath)) {
      // Create image preview for better visibility
      Image img = new Image("/ode/download/globalasset/" + asset.getFileName());
      img.setWidth("32px");
      img.setHeight("32px");
      img.getElement().getStyle().setProperty("objectFit", "cover");
      img.getElement().getStyle().setProperty("border", "1px solid #ddd");
      img.getElement().getStyle().setProperty("borderRadius", "2px");
      img.getElement().getStyle().setProperty("marginRight", "8px");
      return img;
    } else {
      // Use icon for non-image files
      ImageResource iconRes;
      if (StorageUtil.isAudioFile(filePath)) {
        iconRes = images.player();
      } else if (StorageUtil.isVideoFile(filePath)) {
        iconRes = images.image(); // Use image icon for video
      } else {
        iconRes = images.image();
      }
      
      Image icon = new Image(iconRes);
      icon.setWidth("32px");
      icon.setHeight("32px");
      icon.getElement().getStyle().setProperty("marginRight", "8px");
      return icon;
    }
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
    boolean hasSelection = false;
    for (CheckBox checkBox : assetCheckBoxes) {
      if (checkBox.getValue()) {
        hasSelection = true;
        break;
      }
    }
    
    addSelectedButton.setEnabled(hasSelection);
    deleteSelectedButton.setEnabled(hasSelection);
  }

  private void updateStatusLabel(int assetCount) {
    statusLabel.setText(assetCount + " asset" + (assetCount != 1 ? "s" : "") + " shown");
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
        Window.alert("Failed to delete asset: " + caught.getMessage());
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
          errorLabel.setText("Please select a file.");
          return;
        }
        
        String lower = filename.toLowerCase();
        if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
              lower.endsWith(".gif") || lower.endsWith(".mp3") || lower.endsWith(".wav") || 
              lower.endsWith(".ogg"))) {
          errorLabel.setText("Invalid file type. Supported: PNG, JPG, GIF, MP3, WAV, OGG");
          return;
        }
        
        form.setAction(GWT.getModuleBaseURL() + "upload/" + ServerLayout.UPLOAD_GLOBAL_ASSET + "/" + filename);
        form.submit();
      }
    });

    form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete(SubmitCompleteEvent event) {
        dialog.hide();
        String results = event.getResults();
        if (results != null && results.contains("SUCCESS")) {
          statusLabel.setText("Asset uploaded successfully");
          refreshGlobalAssets();
        } else {
          Window.alert("Upload failed: " + (results != null ? results.replaceAll("<[^>]*>", "") : "Unknown error"));
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
              statusLabel.setText("Asset '" + asset.getFileName() + "' added to project");
              dialog.hide();
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
      Label assetName = new Label("• " + asset.getFileName());
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
              statusLabel.setText(assets.size() + " assets added to project");
              dialog.hide();
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
        statusLabel.setText("Folder '" + folderName + "' created");
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

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(8);

    Label nameLabel = new Label("New folder name:");
    nameLabel.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(nameLabel);

    final TextBox nameBox = new TextBox();
    nameBox.setText(oldFolderName);
    nameBox.setStyleName("ode-TextBox");
    dialogPanel.add(nameBox);

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(4);

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

        // Update folder list
        int index = folders.indexOf(oldFolderName);
        if (index >= 0) {
          folders.set(index, newFolderName);
          updateFolderList();
          statusLabel.setText("Folder renamed to '" + newFolderName + "'");
        }
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
    if (Window.confirm("Delete folder '" + folderName + "'?")) {
      folders.remove(folderName);
      if (selectedFolderIndex > 0) {
        selectedFolderIndex = 0; // Reset to "All Assets"
      }
      updateFolderList();
      refreshAssetList();
      statusLabel.setText("Folder '" + folderName + "' deleted");
    }
  }
}