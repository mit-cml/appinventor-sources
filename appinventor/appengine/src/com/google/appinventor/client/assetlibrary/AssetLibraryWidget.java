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
import com.google.appinventor.client.style.neo.ImagesNeo;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetService;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetServiceAsync;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.appinventor.shared.rpc.project.GlobalAssetProjectNode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AssetLibraryWidget extends Composite {
  private VerticalPanel rootPanel;
  private HorizontalPanel headerContainer;
  private TextBox searchBox;
  private ListBox typeFilter;
  private Button uploadButton;
  private Button closeButton;
  private HorizontalPanel mainContentPanel;
  private VerticalPanel sidebarPanel;
  private ScrollPanel assetScrollPanel;
  private FlowPanel assetGridPanel;
  private HorizontalPanel footerPanel;
  private Label statusLabel;

  // Asset management
  private List<GlobalAsset> globalAssets = new ArrayList<>(); 
  private final GlobalAssetServiceAsync globalAssetService = GWT.create(GlobalAssetService.class);
  private static String draggedAssetName;

  // Sidebar state
  private List<String> folders = new ArrayList<>();
  private int selectedFolderIndex = 0;
  private VerticalPanel folderListPanel;

  // Selection management
  private List<CheckBox> assetCheckBoxes = new ArrayList<>();
  private Button addSelectedButton;
  private Button deleteSelectedButton;

  public AssetLibraryWidget(Ode ode) {
    initializeLayout();
    setupEventHandlers();
    loadInitialData();
    injectFolderHoverCSS();
    initWidget(rootPanel);
  }

  private void initializeLayout() {
    // Main root panel - takes full DeckPanel space
    rootPanel = new VerticalPanel();
    rootPanel.setSize("100%", "100%");
    rootPanel.getElement().getStyle().setProperty("margin", "0");
    rootPanel.getElement().getStyle().setProperty("padding", "0");
    rootPanel.getElement().getStyle().setProperty("display", "flex");
    rootPanel.getElement().getStyle().setProperty("flexDirection", "column");
    rootPanel.getElement().getStyle().setProperty("height", "100%");

    createHeader();
    createMainContent();
    createFooter();
  }

  private void createHeader() {
    // Header matching neo design
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
    // Main content area - takes remaining space after header/footer
    mainContentPanel = new HorizontalPanel();
    mainContentPanel.setSize("100%", "100%");
    mainContentPanel.setStyleName("ode-WorkColumns");
    mainContentPanel.getElement().getStyle().setProperty("flex", "1 1 auto");
    mainContentPanel.getElement().getStyle().setProperty("minHeight", "0");
    mainContentPanel.getElement().getStyle().setProperty("display", "flex");
    mainContentPanel.getElement().getStyle().setProperty("overflow", "hidden");

    createSidebar();
    createAssetGrid();

    rootPanel.add(mainContentPanel);
  }

  private void createSidebar() {
    // Sidebar matching App Inventor design with improved spacing
    sidebarPanel = new VerticalPanel();
    sidebarPanel.setWidth("280px");
    sidebarPanel.setHeight("100%");
    sidebarPanel.setStyleName("ode-Designer-LeftColumn");
    sidebarPanel.getElement().getStyle().setProperty("padding", "20px 16px");
    sidebarPanel.getElement().getStyle().setProperty("borderRight", "1px solid #e0e0e0");
    sidebarPanel.getElement().getStyle().setProperty("flexShrink", "0");

    // Folder section header
    HorizontalPanel folderHeader = new HorizontalPanel();
    folderHeader.setWidth("100%");
    folderHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    folderHeader.getElement().getStyle().setProperty("marginBottom", "16px");

    Label folderTitle = new Label("📁 Folders");
    folderTitle.setStyleName("ode-ComponentRowLabel");
    folderTitle.getElement().getStyle().setProperty("fontSize", "16px");
    folderTitle.getElement().getStyle().setProperty("fontWeight", "600");
    folderHeader.add(folderTitle);

    // Small action buttons
    HorizontalPanel folderActions = new HorizontalPanel();
    folderActions.getElement().getStyle().setProperty("marginLeft", "auto");
    folderActions.getElement().getStyle().setProperty("gap", "6px");

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

  private void createAssetGrid() {
    // Asset grid container with improved spacing for full screen utilization
    VerticalPanel assetContainer = new VerticalPanel();
    assetContainer.setWidth("100%");
    assetContainer.setHeight("100%");
    assetContainer.setStyleName("ode-Box-body-padding");
    assetContainer.getElement().getStyle().setProperty("flex", "1 1 auto");
    assetContainer.getElement().getStyle().setProperty("padding", "16px");
    assetContainer.getElement().getStyle().setProperty("display", "block");
    assetContainer.getElement().getStyle().setProperty("boxSizing", "border-box");
    assetContainer.getElement().getStyle().setProperty("overflow", "hidden");
    assetContainer.getElement().getStyle().setProperty("minWidth", "0");
    assetContainer.getElement().getStyle().setProperty("flexGrow", "1");

    // Scrollable grid with optimized full height utilization
    assetScrollPanel = new ScrollPanel();
    assetScrollPanel.setWidth("100%");
    assetScrollPanel.setHeight("100%");
    assetScrollPanel.getElement().getStyle().setProperty("border", "none");
    assetScrollPanel.getElement().getStyle().setProperty("flex", "1 1 auto");
    assetScrollPanel.getElement().getStyle().setProperty("minHeight", "0");
    assetScrollPanel.getElement().getStyle().setProperty("maxHeight", "100%");
    assetScrollPanel.getElement().getStyle().setProperty("overflowY", "auto");
    assetScrollPanel.getElement().getStyle().setProperty("overflowX", "hidden");
    assetScrollPanel.getElement().getStyle().setProperty("width", "100%");
    assetScrollPanel.getElement().getStyle().setProperty("minWidth", "0");

    // Optimized CSS Grid for maximum screen utilization with responsive columns
    assetGridPanel = new FlowPanel();
    assetGridPanel.setWidth("100%");
    assetGridPanel.getElement().getStyle().setProperty("display", "grid");
    assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(auto-fill, minmax(200px, 1fr))");
    assetGridPanel.getElement().getStyle().setProperty("gap", "20px");
    assetGridPanel.getElement().getStyle().setProperty("padding", "20px");
    assetGridPanel.getElement().getStyle().setProperty("alignContent", "start");
    assetGridPanel.getElement().getStyle().setProperty("minHeight", "100%");
    assetGridPanel.getElement().getStyle().setProperty("justifyItems", "stretch");
    assetGridPanel.getElement().getStyle().setProperty("alignItems", "start");
    assetGridPanel.getElement().getStyle().setProperty("backgroundColor", "#f9f9f9");
    assetGridPanel.getElement().getStyle().setProperty("boxSizing", "border-box");
    assetGridPanel.getElement().getStyle().setProperty("gridAutoRows", "max-content");
    assetGridPanel.getElement().getStyle().setProperty("width", "100%");
    assetGridPanel.getElement().getStyle().setProperty("minWidth", "0");
    
    // Add responsive behavior for smaller screens
    addResponsiveStyles();

    assetScrollPanel.add(assetGridPanel);
    assetContainer.add(assetScrollPanel);
    mainContentPanel.add(assetContainer);
  }

  private void addResponsiveStyles() {
    // Add responsive adjustments for different screen sizes
    // This would ideally be done via CSS media queries, but we can add some dynamic adjustments
    int screenWidth = com.google.gwt.user.client.Window.getClientWidth();
    
    if (screenWidth < 768) {
      // Smaller screens: reduce minimum card width and sidebar
      assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(auto-fill, minmax(180px, 1fr))");
      sidebarPanel.setWidth("200px");
      assetGridPanel.getElement().getStyle().setProperty("gap", "16px");
      assetGridPanel.getElement().getStyle().setProperty("padding", "16px");
    } else if (screenWidth < 1024) {
      // Medium screens: moderate adjustments
      assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(auto-fill, minmax(200px, 1fr))");
      assetGridPanel.getElement().getStyle().setProperty("gap", "20px");
    } else {
      // Large screens: optimal grid layout
      assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(auto-fill, minmax(220px, 1fr))");
    }
  }

  private void createFooter() {
    // Footer matching App Inventor style with improved spacing
    footerPanel = new HorizontalPanel();
    footerPanel.setWidth("100%");
    footerPanel.setStyleName("ode-StatusPanel");
    footerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    footerPanel.getElement().getStyle().setProperty("padding", "12px 24px");
    footerPanel.getElement().getStyle().setProperty("minHeight", "40px");
    footerPanel.getElement().getStyle().setProperty("borderTop", "1px solid #e0e0e0");
    footerPanel.getElement().getStyle().setProperty("flexShrink", "0");

    statusLabel = new Label("Loading assets...");
    statusLabel.setStyleName("ode-StatusPanelLabel");
    statusLabel.getElement().getStyle().setProperty("fontSize", "14px");
    statusLabel.getElement().getStyle().setProperty("fontWeight", "500");
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
          Window.alert("Please select a regular folder to rename. Special folders cannot be renamed.");
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
          Window.alert("Please select a regular folder to delete. Special folders cannot be deleted.");
        }
      }
    });
  }

  private boolean isSpecialFolder(String folderName) {
    return "All Assets".equals(folderName) || "Recent".equals(folderName);
  }

  private void injectFolderHoverCSS() {
    // Inject CSS to prevent flickering hover effects
    String css = ".folder-hoverable:hover { " +
                 "  background-color: #f5f5f5 !important; " +
                 "  border-radius: 4px !important; " +
                 "  transform: translateX(2px) !important; " +
                 "  transition: all 0.15s ease !important; " +
                 "} ";
    
    com.google.gwt.dom.client.StyleElement style = com.google.gwt.dom.client.Document.get().createStyleElement();
    style.setInnerText(css);
    com.google.gwt.dom.client.Document.get().getHead().appendChild(style);
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
    folderRow.getElement().getStyle().setProperty("padding", "10px 12px");
    folderRow.getElement().getStyle().setProperty("borderRadius", "4px");
    folderRow.getElement().getStyle().setProperty("cursor", "pointer");
    folderRow.getElement().getStyle().setProperty("marginBottom", "6px");
    folderRow.getElement().getStyle().setProperty("minHeight", "36px");
    folderRow.getElement().getStyle().setProperty("boxSizing", "border-box");

    // Folder icon
    Label icon = new Label("📁");
    icon.getElement().getStyle().setProperty("marginRight", "10px");
    icon.getElement().getStyle().setProperty("fontSize", "16px");
    folderRow.add(icon);

    // Folder name
    Label nameLabel = new Label(folderName);
    nameLabel.getElement().getStyle().setProperty("fontSize", "14px");
    nameLabel.getElement().getStyle().setProperty("fontWeight", "500");
    folderRow.add(nameLabel);

    // Apply selection styling matching App Inventor
    if (index == selectedFolderIndex) {
      folderRow.setStyleName("ode-ComponentRowHighlighted");
      nameLabel.setStyleName("ode-ComponentRowLabel");
    } else {
      folderRow.setStyleName("ode-ComponentRowUnHighlighted");
      nameLabel.setStyleName("ode-ComponentRowLabel");
      
      // Add hover class for CSS-based hover effects (no flickering)
      folderRow.addStyleName("folder-hoverable");
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
    
    // Add virtual folders (only Recent, since Images/Sounds are in type dropdown)
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
    assetGridPanel.clear();
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
    emptyState.getElement().getStyle().setProperty("padding", "60px 20px");
    emptyState.getElement().getStyle().setProperty("textAlign", "center");
    emptyState.getElement().getStyle().setProperty("gridColumn", "1 / -1");
    emptyState.getElement().getStyle().setProperty("display", "flex");
    emptyState.getElement().getStyle().setProperty("flexDirection", "column");
    emptyState.getElement().getStyle().setProperty("justifyContent", "center");
    emptyState.getElement().getStyle().setProperty("minHeight", "300px");

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

    assetGridPanel.add(emptyState);
  }

  private void displayAssets(List<GlobalAsset> assets) {
    for (final GlobalAsset asset : assets) {
      VerticalPanel assetCard = createAssetCard(asset);
      assetGridPanel.add(assetCard);
    }
  }

  private VerticalPanel createAssetCard(final GlobalAsset asset) {
    VerticalPanel card = new VerticalPanel();
    card.setStyleName("ode-Box");
    card.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    card.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    card.getElement().getStyle().setProperty("borderRadius", "8px");
    card.getElement().getStyle().setProperty("padding", "16px");
    card.getElement().getStyle().setProperty("backgroundColor", "white");
    card.getElement().getStyle().setProperty("cursor", "pointer");
    card.getElement().getStyle().setProperty("transition", "all 0.2s ease");
    card.getElement().getStyle().setProperty("textAlign", "center");
    card.getElement().getStyle().setProperty("minHeight", "260px");
    card.getElement().getStyle().setProperty("width", "100%");
    card.getElement().getStyle().setProperty("boxSizing", "border-box");
    card.getElement().getStyle().setProperty("display", "flex");
    card.getElement().getStyle().setProperty("flexDirection", "column");
    card.getElement().getStyle().setProperty("justifyContent", "space-between");

    // Hover effect
    card.addDomHandler(new com.google.gwt.event.dom.client.MouseOverHandler() {
      @Override
      public void onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent event) {
        card.getElement().getStyle().setProperty("boxShadow", "0 2px 4px rgba(0,0,0,0.1)");
        card.getElement().getStyle().setProperty("borderColor", "#ccc");
      }
    }, com.google.gwt.event.dom.client.MouseOverEvent.getType());

    card.addDomHandler(new com.google.gwt.event.dom.client.MouseOutHandler() {
      @Override
      public void onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent event) {
        card.getElement().getStyle().setProperty("boxShadow", "none");
        card.getElement().getStyle().setProperty("borderColor", "#e0e0e0");
      }
    }, com.google.gwt.event.dom.client.MouseOutEvent.getType());

    // Checkbox for selection
    final CheckBox checkBox = new CheckBox();
    checkBox.getElement().getStyle().setProperty("marginBottom", "8px");
    assetCheckBoxes.add(checkBox);
    card.add(checkBox);

    // Asset preview/icon
    String filePath = asset.getFolder() + "/" + asset.getFileName();
    String fileName = asset.getFileName().toLowerCase();
    
    // Create preview widget
    Widget previewWidget = createPreviewWidget(asset);
    previewWidget.getElement().getStyle().setProperty("marginBottom", "8px");
    card.add(previewWidget);

    // Asset name with truncation
    String displayName = asset.getFileName();
    if (displayName.length() > 20) {
      displayName = displayName.substring(0, 17) + "...";
    }
    
    Label nameLabel = new Label(displayName);
    nameLabel.setTitle(asset.getFileName()); // Full name on hover
    nameLabel.setStyleName("ode-ComponentRowLabel");
    nameLabel.getElement().getStyle().setProperty("fontSize", "14px");
    nameLabel.getElement().getStyle().setProperty("fontWeight", "600");
    nameLabel.getElement().getStyle().setProperty("marginTop", "8px");
    nameLabel.getElement().getStyle().setProperty("lineHeight", "1.2");
    nameLabel.getElement().getStyle().setProperty("textAlign", "center");
    nameLabel.getElement().getStyle().setProperty("maxWidth", "180px");
    nameLabel.getElement().getStyle().setProperty("wordBreak", "break-word");
    nameLabel.getElement().getStyle().setProperty("marginBottom", "4px");
    nameLabel.getElement().getStyle().setProperty("textAlign", "center");
    nameLabel.getElement().getStyle().setProperty("wordBreak", "break-all");
    card.add(nameLabel);

    // Asset date
    Label dateLabel = new Label(formatDate(asset.getTimestamp()));
    dateLabel.setStyleName("ode-ComponentRowLabel");
    dateLabel.getElement().getStyle().setProperty("fontSize", "10px");
    dateLabel.getElement().getStyle().setProperty("opacity", "0.7");
    dateLabel.getElement().getStyle().setProperty("marginBottom", "8px");
    card.add(dateLabel);

    // Action buttons with improved spacing
    HorizontalPanel actionPanel = new HorizontalPanel();
    actionPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    actionPanel.getElement().getStyle().setProperty("gap", "6px");
    actionPanel.getElement().getStyle().setProperty("marginTop", "4px");

    // Preview button
    Button previewBtn = new Button("👁");
    previewBtn.setTitle("Preview Asset");
    previewBtn.setStyleName("ode-ProjectListButton");
    previewBtn.getElement().getStyle().setProperty("padding", "4px 8px");
    previewBtn.getElement().getStyle().setProperty("fontSize", "11px");
    previewBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        previewAsset(asset);
      }
    });
    actionPanel.add(previewBtn);

    // Add button
    Button addBtn = new Button("Add");
    addBtn.setStyleName("ode-ProjectListButton");
    addBtn.getElement().getStyle().setProperty("padding", "4px 8px");
    addBtn.getElement().getStyle().setProperty("fontSize", "11px");
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showAddToProjectDialog(asset);
      }
    });
    actionPanel.add(addBtn);

    // Delete button
    Button deleteBtn = new Button("🗑");
    deleteBtn.setTitle("Delete Asset");
    deleteBtn.setStyleName("ode-ProjectListButton");
    deleteBtn.getElement().getStyle().setProperty("padding", "4px 8px");
    deleteBtn.getElement().getStyle().setProperty("fontSize", "11px");
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

    card.add(actionPanel);

    // Drag and drop support
    setupAssetDragDrop(card, asset);

    // Checkbox change handler
    checkBox.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        updateBulkActionButtons();
      }
    });

    return card;
  }

  private Widget createPreviewWidget(GlobalAsset asset) {
    String fileName = asset.getFileName().toLowerCase();
    String filePath = asset.getFolder() + "/" + asset.getFileName();
    
    if (StorageUtil.isImageFile(filePath)) {
      // Create larger image preview for better visibility
      Image img = new Image("/ode/download/globalasset/" + asset.getFileName());
      img.setWidth("100px");
      img.setHeight("100px");
      img.getElement().getStyle().setProperty("objectFit", "cover");
      img.getElement().getStyle().setProperty("border", "1px solid #ddd");
      img.getElement().getStyle().setProperty("borderRadius", "4px");
      img.getElement().getStyle().setProperty("marginBottom", "12px");
      return img;
    } else {
      // Use icon for non-image files
      ImageResource iconRes;
      if (StorageUtil.isAudioFile(filePath)) {
        iconRes = ImagesNeo.INSTANCE.player();
      } else if (StorageUtil.isVideoFile(filePath)) {
        iconRes = ImagesNeo.INSTANCE.image(); // Use image icon for video for now
      } else {
        iconRes = ImagesNeo.INSTANCE.image();
      }
      
      Image icon = new Image(iconRes);
      icon.setWidth("64px");
      icon.setHeight("64px");
      icon.getElement().getStyle().setProperty("marginBottom", "12px");
      return icon;
    }
  }

  private void previewAsset(GlobalAsset asset) {
    // Always use the global asset URL for preview
    // The GlobalAssetServiceImpl.doGet() will handle the download
    GlobalAssetProjectNode projectNode = new GlobalAssetProjectNode(
        asset.getFileName(),
        asset.getFileName()
    );
    
    // Use the existing PreviewFileCommand
    PreviewFileCommand previewCommand = new PreviewFileCommand();
    if (previewCommand.isSupported(projectNode)) {
      previewCommand.execute(projectNode);
    } else {
      Window.alert("Preview not supported for this file type: " + asset.getFileName());
    }
  }

  private void setupAssetDragDrop(VerticalPanel card, GlobalAsset asset) {
    card.getElement().setAttribute("draggable", "true");
    
    card.addDomHandler(new DragStartHandler() {
      @Override
      public void onDragStart(DragStartEvent event) {
        event.setData("text/plain", asset.getFileName());
        draggedAssetName = asset.getFileName();
        card.getElement().getStyle().setProperty("opacity", "0.5");
      }
    }, DragStartEvent.getType());
  }

  private String formatDate(long timestamp) {
    java.util.Date date = new java.util.Date(timestamp);
    String dateStr = date.toString();
    // Return simplified date format
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

  // Dialog methods matching App Inventor style
  private void showUploadDialog() {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Upload Asset to Library");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);
    dialog.setAutoHideEnabled(false);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth("360px");
    dialogPanel.getElement().getStyle().setProperty("padding", "16px");

    // Upload form
    final FormPanel form = new FormPanel();
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);

    VerticalPanel formPanel = new VerticalPanel();
    formPanel.setSpacing(8);

    // File input label
    Label fileLabel = new Label("Select file to upload:");
    fileLabel.setStyleName("ode-ComponentRowLabel");
    formPanel.add(fileLabel);

    // File input
    final FileUpload fileUpload = new FileUpload();
    fileUpload.setName(ServerLayout.UPLOAD_GLOBAL_ASSET_FORM_ELEMENT);
    fileUpload.setStyleName("ode-TextBox");
    fileUpload.getElement().getStyle().setProperty("width", "100%");
    formPanel.add(fileUpload);

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
        
        // Extract just the filename, removing any path (including "fakepath")
        String actualFilename = filename;
        if (filename.contains("\\")) {
          // Windows path separator (handles "C:\fakepath\file.png")
          actualFilename = filename.substring(filename.lastIndexOf("\\") + 1);
        } else if (filename.contains("/")) {
          // Unix path separator
          actualFilename = filename.substring(filename.lastIndexOf("/") + 1);
        }
        
        String lower = actualFilename.toLowerCase();
        if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
              lower.endsWith(".gif") || lower.endsWith(".mp3") || lower.endsWith(".wav") || 
              lower.endsWith(".ogg"))) {
          errorLabel.setText("Invalid file type. Supported: PNG, JPG, GIF, MP3, WAV, OGG");
          return;
        }
        
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
    dialog.setAutoHideEnabled(false);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth("400px");
    dialogPanel.getElement().getStyle().setProperty("padding", "16px");

    // Asset info
    HorizontalPanel assetInfo = new HorizontalPanel();
    assetInfo.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    assetInfo.getElement().getStyle().setProperty("marginBottom", "12px");
    assetInfo.getElement().getStyle().setProperty("padding", "8px");
    assetInfo.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    assetInfo.getElement().getStyle().setProperty("borderRadius", "2px");

    String fileName = asset.getFileName().toLowerCase();
    ImageResource iconRes = fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".ogg")
      ? ImagesNeo.INSTANCE.player() 
      : ImagesNeo.INSTANCE.image();
    
    Image assetIcon = new Image(iconRes);
    assetIcon.setWidth("24px");
    assetIcon.setHeight("24px");
    assetIcon.getElement().getStyle().setProperty("marginRight", "8px");
    assetInfo.add(assetIcon);

    Label assetName = new Label(asset.getFileName());
    assetName.setStyleName("ode-ComponentRowLabel");
    assetName.getElement().getStyle().setProperty("fontWeight", "500");
    assetInfo.add(assetName);

    dialogPanel.add(assetInfo);

    // Options
    Label optionsLabel = new Label("How would you like to add this asset?");
    optionsLabel.setStyleName("ode-ComponentRowLabel");
    optionsLabel.getElement().getStyle().setProperty("fontWeight", "500");
    optionsLabel.getElement().getStyle().setProperty("marginBottom", "8px");
    dialogPanel.add(optionsLabel);

    // Track option
    VerticalPanel trackOption = new VerticalPanel();
    trackOption.getElement().getStyle().setProperty("padding", "8px");
    trackOption.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    trackOption.getElement().getStyle().setProperty("borderRadius", "2px");
    trackOption.getElement().getStyle().setProperty("marginBottom", "6px");

    final RadioButton trackRadio = new RadioButton("addOption", "Track Asset (Recommended)");
    trackRadio.setValue(true);
    trackRadio.setStyleName("ode-ComponentRowLabel");
    trackRadio.getElement().getStyle().setProperty("fontWeight", "500");
    trackOption.add(trackRadio);

    Label trackDesc = new Label("The asset will be updated in your project if the library version changes.");
    trackDesc.setStyleName("ode-ComponentRowLabel");
    trackDesc.getElement().getStyle().setProperty("fontSize", "12px");
    trackDesc.getElement().getStyle().setProperty("opacity", "0.8");
    trackDesc.getElement().getStyle().setProperty("marginTop", "4px");
    trackOption.add(trackDesc);

    dialogPanel.add(trackOption);

    // Copy option
    VerticalPanel copyOption = new VerticalPanel();
    copyOption.getElement().getStyle().setProperty("padding", "8px");
    copyOption.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    copyOption.getElement().getStyle().setProperty("borderRadius", "2px");

    final RadioButton copyRadio = new RadioButton("addOption", "Copy Asset");
    copyRadio.setStyleName("ode-ComponentRowLabel");
    copyRadio.getElement().getStyle().setProperty("fontWeight", "500");
    copyOption.add(copyRadio);

    Label copyDesc = new Label("A copy will be added to your project and will not be updated.");
    copyDesc.setStyleName("ode-ComponentRowLabel");
    copyDesc.getElement().getStyle().setProperty("fontSize", "12px");
    copyDesc.getElement().getStyle().setProperty("opacity", "0.8");
    copyDesc.getElement().getStyle().setProperty("marginTop", "4px");
    copyOption.add(copyDesc);

    dialogPanel.add(copyOption);

    // Button panel
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);
    buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    buttonPanel.setWidth("100%");
    buttonPanel.getElement().getStyle().setProperty("marginTop", "12px");

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
    dialog.setAutoHideEnabled(false);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth("440px");
    dialogPanel.getElement().getStyle().setProperty("padding", "16px");

    // Assets list preview
    Label assetsLabel = new Label("Selected assets (" + assets.size() + "):");
    assetsLabel.setStyleName("ode-ComponentRowLabel");
    assetsLabel.getElement().getStyle().setProperty("fontWeight", "600");
    assetsLabel.getElement().getStyle().setProperty("marginBottom", "8px");
    dialogPanel.add(assetsLabel);

    // Scrollable list of asset names
    ScrollPanel assetListScroll = new ScrollPanel();
    assetListScroll.setHeight("100px");
    assetListScroll.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    assetListScroll.getElement().getStyle().setProperty("borderRadius", "2px");
    assetListScroll.getElement().getStyle().setProperty("padding", "8px");
    assetListScroll.getElement().getStyle().setProperty("marginBottom", "12px");

    VerticalPanel assetList = new VerticalPanel();
    assetList.setWidth("100%");
    for (GlobalAsset asset : assets) {
      Label assetName = new Label("• " + asset.getFileName());
      assetName.setStyleName("ode-ComponentRowLabel");
      assetName.getElement().getStyle().setProperty("fontSize", "12px");
      assetName.getElement().getStyle().setProperty("marginBottom", "2px");
      assetList.add(assetName);
    }
    assetListScroll.add(assetList);
    dialogPanel.add(assetListScroll);

    // Options
    Label optionsLabel = new Label("How would you like to add these assets?");
    optionsLabel.setStyleName("ode-ComponentRowLabel");
    optionsLabel.getElement().getStyle().setProperty("fontWeight", "500");
    optionsLabel.getElement().getStyle().setProperty("marginBottom", "8px");
    dialogPanel.add(optionsLabel);

    // Track option
    VerticalPanel trackOption = new VerticalPanel();
    trackOption.getElement().getStyle().setProperty("padding", "8px");
    trackOption.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    trackOption.getElement().getStyle().setProperty("borderRadius", "2px");
    trackOption.getElement().getStyle().setProperty("marginBottom", "6px");

    final RadioButton trackRadio = new RadioButton("bulkAddOption", "Track Usage (Recommended)");
    trackRadio.setValue(true);
    trackRadio.setStyleName("ode-ComponentRowLabel");
    trackRadio.getElement().getStyle().setProperty("fontWeight", "500");
    trackOption.add(trackRadio);

    Label trackDesc = new Label("Assets will be updated in your project if the library versions change.");
    trackDesc.setStyleName("ode-ComponentRowLabel");
    trackDesc.getElement().getStyle().setProperty("fontSize", "12px");
    trackDesc.getElement().getStyle().setProperty("opacity", "0.8");
    trackDesc.getElement().getStyle().setProperty("marginTop", "4px");
    trackOption.add(trackDesc);

    dialogPanel.add(trackOption);

    // Copy option
    VerticalPanel copyOption = new VerticalPanel();
    copyOption.getElement().getStyle().setProperty("padding", "8px");
    copyOption.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    copyOption.getElement().getStyle().setProperty("borderRadius", "2px");

    final RadioButton copyRadio = new RadioButton("bulkAddOption", "Copy Assets");
    copyRadio.setStyleName("ode-ComponentRowLabel");
    copyRadio.getElement().getStyle().setProperty("fontWeight", "500");
    copyOption.add(copyRadio);

    Label copyDesc = new Label("Copies will be added to your project and will not be updated.");
    copyDesc.setStyleName("ode-ComponentRowLabel");
    copyDesc.getElement().getStyle().setProperty("fontSize", "12px");
    copyDesc.getElement().getStyle().setProperty("opacity", "0.8");
    copyDesc.getElement().getStyle().setProperty("marginTop", "4px");
    copyOption.add(copyDesc);

    dialogPanel.add(copyOption);

    // Progress area (initially hidden)
    final VerticalPanel progressPanel = new VerticalPanel();
    progressPanel.setWidth("100%");
    progressPanel.setVisible(false);
    progressPanel.getElement().getStyle().setProperty("marginTop", "12px");
    progressPanel.getElement().getStyle().setProperty("padding", "12px");
    progressPanel.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    progressPanel.getElement().getStyle().setProperty("borderRadius", "2px");
    progressPanel.getElement().getStyle().setProperty("backgroundColor", "#f9f9f9");

    final Label progressLabel = new Label("Processing assets...");
    progressLabel.setStyleName("ode-ComponentRowLabel");
    progressLabel.getElement().getStyle().setProperty("fontSize", "12px");
    progressPanel.add(progressLabel);

    dialogPanel.add(progressPanel);

    // Button panel
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);
    buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    buttonPanel.setWidth("100%");
    buttonPanel.getElement().getStyle().setProperty("marginTop", "12px");

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
        
        // Show progress
        progressPanel.setVisible(true);
        addBtn.setEnabled(false);
        cancelBtn.setText("Close");
        
        // Collect asset filenames
        List<String> assetFileNames = new ArrayList<>();
        for (GlobalAsset asset : assets) {
          assetFileNames.add(asset.getFileName());
        }
        
        // Use new bulk add method
        globalAssetService.bulkAddAssetsToProject(assetFileNames, projectId, track,
          new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
              progressLabel.setText("✓ All " + assets.size() + " assets added successfully!");
              progressLabel.getElement().getStyle().setProperty("color", "#2e7d32");
              statusLabel.setText(assets.size() + " assets added to project");
              addBtn.setText("Done");
              addBtn.setEnabled(true);
            }
            
            @Override
            public void onFailure(Throwable caught) {
              progressLabel.setText("⚠ " + caught.getMessage());
              progressLabel.getElement().getStyle().setProperty("color", "#d32f2f");
              addBtn.setText("Retry");
              addBtn.setEnabled(true);
              cancelBtn.setText("Cancel");
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
    dialog.setAutoHideEnabled(false);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth("300px");
    dialogPanel.getElement().getStyle().setProperty("padding", "16px");

    Label nameLabel = new Label("Folder name:");
    nameLabel.setStyleName("ode-ComponentRowLabel");
    dialogPanel.add(nameLabel);

    final TextBox nameBox = new TextBox();
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
        
        // Add to folders list (will be persisted when assets are moved to it)
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
    statusLabel.setText("Renaming folder...");
    int assetsToUpdate = 0;
    
    // Count assets to update
    for (GlobalAsset asset : globalAssets) {
      if (oldFolderName.equals(asset.getFolder())) {
        assetsToUpdate++;
      }
    }
    
    if (assetsToUpdate == 0) {
      // Just update the folder list
      int index = folders.indexOf(oldFolderName);
      if (index >= 0) {
        folders.set(index, newFolderName);
        updateFolderList();
      }
      statusLabel.setText("Folder renamed to '" + newFolderName + "'");
      return;
    }

    // Update each asset's folder - this would require individual API calls
    // For now, show a message that this feature needs backend support
    statusLabel.setText("Folder renaming requires server-side implementation");
    Window.alert("Folder renaming with assets requires additional server-side implementation. " +
                "Currently only empty folder operations are supported.");
  }

  private void deleteFolderAndMoveAssets(final String folderName) {
    statusLabel.setText("Deleting folder...");
    
    // Count assets in folder
    int assetsToMove = 0;
    for (GlobalAsset asset : globalAssets) {
      if (folderName.equals(asset.getFolder())) {
        assetsToMove++;
      }
    }
    
    if (assetsToMove == 0) {
      // Just remove from folder list
      folders.remove(folderName);
      if (selectedFolderIndex > 0) {
        selectedFolderIndex = 0; // Reset to "All Assets"
      }
      updateFolderList();
      refreshAssetList();
      statusLabel.setText("Empty folder '" + folderName + "' deleted");
    } else {
      // Moving assets requires server-side support
      statusLabel.setText("Folder deletion with assets requires server-side implementation");
      Window.alert("Deleting folders with assets requires additional server-side implementation. " +
                  "Currently only empty folder operations are supported.");
    }
  }
}