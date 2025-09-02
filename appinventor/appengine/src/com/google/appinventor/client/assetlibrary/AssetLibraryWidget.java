package com.google.appinventor.client.assetlibrary;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.PreviewFileCommand;
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
import com.google.appinventor.client.style.neo.ImagesNeo;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetService;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetServiceAsync;
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
    rootPanel.getElement().getStyle().setProperty("height", "100vh");
    rootPanel.getElement().getStyle().setProperty("maxHeight", "100vh");
    rootPanel.getElement().getStyle().setProperty("overflow", "hidden");

    createHeader();
    createMainContent();
    createFooter();
  }

  private void createHeader() {
    // Header matching neo design with fixed positioning
    headerContainer = new HorizontalPanel();
    headerContainer.setWidth("100%");
    headerContainer.setStyleName("ode-TopPanel");
    headerContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    headerContainer.getElement().getStyle().setProperty("padding", "12px 24px");
    headerContainer.getElement().getStyle().setProperty("borderBottom", "1px solid #e0e0e0");
    headerContainer.getElement().getStyle().setProperty("zIndex", "1000");
    headerContainer.getElement().getStyle().setProperty("backgroundColor", "white");
    headerContainer.getElement().getStyle().setProperty("flexShrink", "0");
    headerContainer.getElement().getStyle().setProperty("minHeight", "60px");
    headerContainer.getElement().getStyle().setProperty("boxSizing", "border-box");

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
    mainContentPanel.getElement().getStyle().setProperty("height", "100%");
    mainContentPanel.getElement().getStyle().setProperty("boxSizing", "border-box");
    mainContentPanel.getElement().getStyle().setProperty("backgroundColor", "#f9f9f9");

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
    sidebarPanel.getElement().getStyle().setProperty("backgroundColor", "white");
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

  private void createAssetGrid() {
    // Asset grid container with improved spacing for full screen utilization
    VerticalPanel assetContainer = new VerticalPanel();
    assetContainer.setWidth("100%");
    assetContainer.setHeight("100%");
    assetContainer.setStyleName("ode-Box-body-padding");
    assetContainer.getElement().getStyle().setProperty("flex", "1 1 auto");
    assetContainer.getElement().getStyle().setProperty("padding", "0");
    assetContainer.getElement().getStyle().setProperty("display", "flex");
    assetContainer.getElement().getStyle().setProperty("flexDirection", "column");
    assetContainer.getElement().getStyle().setProperty("boxSizing", "border-box");
    assetContainer.getElement().getStyle().setProperty("overflow", "hidden");
    assetContainer.getElement().getStyle().setProperty("minWidth", "0");
    assetContainer.getElement().getStyle().setProperty("flexGrow", "1");
    assetContainer.getElement().getStyle().setProperty("backgroundColor", "#f9f9f9");

    // Scrollable grid with optimized full height utilization
    assetScrollPanel = new ScrollPanel();
    assetScrollPanel.setWidth("100%");
    assetScrollPanel.getElement().getStyle().setProperty("border", "none");
    assetScrollPanel.getElement().getStyle().setProperty("flex", "1 1 auto");
    assetScrollPanel.getElement().getStyle().setProperty("overflowY", "auto");
    assetScrollPanel.getElement().getStyle().setProperty("overflowX", "hidden");
    assetScrollPanel.getElement().getStyle().setProperty("backgroundColor", "#f9f9f9");
    assetScrollPanel.getElement().getStyle().setProperty("height", "auto");
    assetScrollPanel.getElement().getStyle().setProperty("maxHeight", "calc(100vh - 140px)");

    // Optimized CSS Grid for maximum screen utilization with responsive columns
    assetGridPanel = new FlowPanel();
    assetGridPanel.setWidth("100%");
    assetGridPanel.getElement().getStyle().setProperty("display", "grid");
    assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(auto-fill, minmax(280px, 1fr))");
    assetGridPanel.getElement().getStyle().setProperty("gap", "20px");
    assetGridPanel.getElement().getStyle().setProperty("padding", "20px");
    assetGridPanel.getElement().getStyle().setProperty("alignContent", "start");
    assetGridPanel.getElement().getStyle().setProperty("justifyItems", "stretch");
    assetGridPanel.getElement().getStyle().setProperty("alignItems", "start");
    assetGridPanel.getElement().getStyle().setProperty("backgroundColor", "#f9f9f9");
    assetGridPanel.getElement().getStyle().setProperty("boxSizing", "border-box");
    assetGridPanel.getElement().getStyle().setProperty("gridAutoRows", "max-content");
    assetGridPanel.getElement().getStyle().setProperty("width", "100%");
    assetGridPanel.getElement().getStyle().setProperty("minHeight", "auto");
    
    // Add responsive behavior for smaller screens
    addResponsiveStyles();

    assetScrollPanel.add(assetGridPanel);
    assetContainer.add(assetScrollPanel);
    mainContentPanel.add(assetContainer);
  }

  private void addResponsiveStyles() {
    // Add responsive adjustments for different screen sizes  
    int screenWidth = com.google.gwt.user.client.Window.getClientWidth();
    
    if (screenWidth < 768) {
      // Smaller screens: 2 columns
      assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(2, 1fr)");
      sidebarPanel.setWidth("200px");
      assetGridPanel.getElement().getStyle().setProperty("gap", "16px");
      assetGridPanel.getElement().getStyle().setProperty("padding", "16px");
    } else if (screenWidth < 1200) {
      // Medium screens: 3-4 columns
      assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(3, 1fr)");
      assetGridPanel.getElement().getStyle().setProperty("gap", "20px");
    } else if (screenWidth < 1600) {
      // Large screens: 4-5 columns
      assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(4, 1fr)");
      assetGridPanel.getElement().getStyle().setProperty("gap", "24px");
    } else {
      // Extra large screens: 5+ columns for maximum utilization
      assetGridPanel.getElement().getStyle().setProperty("gridTemplateColumns", "repeat(5, 1fr)");
      assetGridPanel.getElement().getStyle().setProperty("gap", "28px");
    }
  }

  private void createFooter() {
    // Footer matching App Inventor style with improved spacing
    footerPanel = new HorizontalPanel();
    footerPanel.setWidth("100%");
    footerPanel.setStyleName("ode-StatusPanel");
    footerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    footerPanel.getElement().getStyle().setProperty("padding", "8px 24px");
    footerPanel.getElement().getStyle().setProperty("minHeight", "36px");
    footerPanel.getElement().getStyle().setProperty("borderTop", "1px solid #e0e0e0");
    footerPanel.getElement().getStyle().setProperty("flexShrink", "0");
    footerPanel.getElement().getStyle().setProperty("backgroundColor", "white");
    footerPanel.getElement().getStyle().setProperty("boxSizing", "border-box");

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
        // Get the current project and switch back to the editor
        long currentProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
        if (currentProjectId != 0) {
          Project currentProject = Ode.getInstance().getProjectManager().getProject(currentProjectId);
          if (currentProject != null) {
            Ode.getInstance().openYoungAndroidProjectInDesigner(currentProject);
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
          showAddToProjectDialog(selectedAssets);
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
    emptyState.getElement().getStyle().setProperty("minHeight", "400px");
    emptyState.getElement().getStyle().setProperty("alignItems", "center");

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
    card.getElement().getStyle().setProperty("minHeight", "280px");
    card.getElement().getStyle().setProperty("maxHeight", "320px");
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

    // Version indicator and project usage
    HorizontalPanel versionPanel = new HorizontalPanel();
    versionPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    versionPanel.getElement().getStyle().setProperty("marginTop", "4px");
    versionPanel.getElement().getStyle().setProperty("marginBottom", "8px");
    
    Label lastUpdated = new Label("Updated: " + formatDate(asset.getTimestamp()));
    lastUpdated.setStyleName("ode-ComponentRowLabel");
    lastUpdated.getElement().getStyle().setProperty("fontSize", "9px");
    lastUpdated.getElement().getStyle().setProperty("backgroundColor", "#f5f5f5");
    lastUpdated.getElement().getStyle().setProperty("color", "#666");
    lastUpdated.getElement().getStyle().setProperty("padding", "2px 6px");
    lastUpdated.getElement().getStyle().setProperty("borderRadius", "4px");
    lastUpdated.getElement().getStyle().setProperty("fontWeight", "400");
    versionPanel.add(lastUpdated);
    
    card.add(versionPanel);
    
    // Project usage indicator with async loading
    final Label usageIndicator = new Label("Checking usage...");
    usageIndicator.setStyleName("ode-ComponentRowLabel");
    usageIndicator.getElement().getStyle().setProperty("fontSize", "10px");
    usageIndicator.getElement().getStyle().setProperty("color", "#6c757d");
    usageIndicator.getElement().getStyle().setProperty("textAlign", "center");
    usageIndicator.getElement().getStyle().setProperty("marginBottom", "8px");
    usageIndicator.getElement().getStyle().setProperty("fontWeight", "400");
    card.add(usageIndicator);
    
    // Load project usage asynchronously
    globalAssetService.getProjectsUsingAsset(asset.getFileName(), new AsyncCallback<List<Long>>() {
      @Override
      public void onSuccess(List<Long> projectIds) {
        if (projectIds != null && !projectIds.isEmpty()) {
          usageIndicator.setText("Used by " + projectIds.size() + " project" + (projectIds.size() == 1 ? "" : "s"));
          usageIndicator.getElement().getStyle().setProperty("color", "#007bff");
          usageIndicator.setTitle("This asset is linked to " + projectIds.size() + " project(s)");
        } else {
          usageIndicator.setText("Not in use");
          usageIndicator.getElement().getStyle().setProperty("color", "#6c757d");
          usageIndicator.setTitle("This asset is not currently used by any projects");
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        usageIndicator.setText("Unknown usage");
        usageIndicator.getElement().getStyle().setProperty("color", "#dc3545");
      }
    });

    // Action buttons with improved spacing
    HorizontalPanel actionPanel = new HorizontalPanel();
    actionPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    actionPanel.getElement().getStyle().setProperty("gap", "4px");
    actionPanel.getElement().getStyle().setProperty("marginTop", "4px");

    // Preview button
    Button previewBtn = new Button("👁");
    previewBtn.setTitle("Preview Asset");
    previewBtn.setStyleName("ode-ProjectListButton");
    previewBtn.getElement().getStyle().setProperty("padding", "3px 6px");
    previewBtn.getElement().getStyle().setProperty("fontSize", "10px");
    previewBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        previewAsset(asset);
      }
    });
    actionPanel.add(previewBtn);


    // Update button
    Button updateBtn = new Button("⬆");
    updateBtn.setTitle("Upload New Version");
    updateBtn.setStyleName("ode-ProjectListButton");
    updateBtn.getElement().getStyle().setProperty("padding", "3px 6px");
    updateBtn.getElement().getStyle().setProperty("fontSize", "10px");
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
    addBtn.getElement().getStyle().setProperty("padding", "3px 6px");
    addBtn.getElement().getStyle().setProperty("fontSize", "10px");
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showAddToProjectDialog(java.util.Arrays.asList(asset));
      }
    });
    actionPanel.add(addBtn);

    // Delete button
    Button deleteBtn = new Button("🗑");
    deleteBtn.setTitle("Delete Asset");
    deleteBtn.setStyleName("ode-ProjectListButton");
    deleteBtn.getElement().getStyle().setProperty("padding", "3px 6px");
    deleteBtn.getElement().getStyle().setProperty("fontSize", "10px");
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
      // Add cache-busting timestamp parameter to force refresh when asset is updated
      String imageUrl = "/ode/download/globalasset/" + asset.getFileName() + "?t=" + asset.getTimestamp();
      Image img = new Image(imageUrl);
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
    String fileId = asset.getFileName();
    GlobalAssetProjectNode projectNode = new GlobalAssetProjectNode(
        asset.getFileName(),
        fileId
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
        statusLabel.setText("Asset '" + asset.getFileName() + "' deleted successfully");
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
        
        String actualFilename = filename;
        if (filename.contains("\\")) {
          actualFilename = filename.substring(filename.lastIndexOf("\\") + 1);
        } else if (filename.contains("/")) {
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

  private void showAddToProjectDialog(final List<GlobalAsset> assets) {
    final DialogBox dialog = new DialogBox();
    boolean isSingle = assets.size() == 1;
    dialog.setText(isSingle ? "Add Asset to Project" : "Add " + assets.size() + " Assets to Project");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);
    dialog.setAutoHideEnabled(false);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth(isSingle ? "400px" : "440px");
    dialogPanel.getElement().getStyle().setProperty("padding", "16px");

    // Asset info - different for single vs multiple
    if (isSingle) {
      // Single asset info
      GlobalAsset asset = assets.get(0);
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
    } else {
      // Multiple assets list preview
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
    }

    // Options
    final Label optionsLabel = new Label(isSingle ? "How would you like to add this asset?" : "How would you like to add these assets?");
    optionsLabel.setStyleName("ode-ComponentRowLabel");
    optionsLabel.getElement().getStyle().setProperty("fontWeight", "500");
    optionsLabel.getElement().getStyle().setProperty("marginBottom", "8px");
    dialogPanel.add(optionsLabel);

    // Track option
    final VerticalPanel trackOption = new VerticalPanel();
    trackOption.getElement().getStyle().setProperty("padding", "8px");
    trackOption.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    trackOption.getElement().getStyle().setProperty("borderRadius", "2px");
    trackOption.getElement().getStyle().setProperty("marginBottom", "6px");

    final RadioButton trackRadio = new RadioButton("addOption", isSingle ? "Track Asset (Recommended)" : "Track Usage (Recommended)");
    trackRadio.setValue(true);
    trackRadio.setStyleName("ode-ComponentRowLabel");
    trackRadio.getElement().getStyle().setProperty("fontWeight", "500");
    trackOption.add(trackRadio);

    Label trackDesc = new Label(isSingle ? "The asset will be updated in your project if the library version changes." 
                                        : "Assets will be updated in your project if the library versions change.");
    trackDesc.setStyleName("ode-ComponentRowLabel");
    trackDesc.getElement().getStyle().setProperty("fontSize", "12px");
    trackDesc.getElement().getStyle().setProperty("opacity", "0.8");
    trackDesc.getElement().getStyle().setProperty("marginTop", "4px");
    trackOption.add(trackDesc);

    dialogPanel.add(trackOption);

    // Copy option
    final VerticalPanel copyOption = new VerticalPanel();
    copyOption.getElement().getStyle().setProperty("padding", "8px");
    copyOption.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    copyOption.getElement().getStyle().setProperty("borderRadius", "2px");

    final RadioButton copyRadio = new RadioButton("addOption", isSingle ? "Copy Asset" : "Copy Assets");
    copyRadio.setStyleName("ode-ComponentRowLabel");
    copyRadio.getElement().getStyle().setProperty("fontWeight", "500");
    copyOption.add(copyRadio);

    Label copyDesc = new Label(isSingle ? "A copy will be added to your project and will not be updated."
                                        : "Copies will be added to your project and will not be updated.");
    copyDesc.setStyleName("ode-ComponentRowLabel");
    copyDesc.getElement().getStyle().setProperty("fontSize", "12px");
    copyDesc.getElement().getStyle().setProperty("opacity", "0.8");
    copyDesc.getElement().getStyle().setProperty("marginTop", "4px");
    copyOption.add(copyDesc);

    dialogPanel.add(copyOption);

    // Progress/Status area (for both single and multiple assets)
    final VerticalPanel progressPanel = new VerticalPanel();
    progressPanel.setWidth("100%");
    progressPanel.setVisible(false);
    progressPanel.getElement().getStyle().setProperty("marginTop", "12px");
    progressPanel.getElement().getStyle().setProperty("padding", "12px");
    progressPanel.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    progressPanel.getElement().getStyle().setProperty("borderRadius", "2px");
    progressPanel.getElement().getStyle().setProperty("backgroundColor", "#f9f9f9");

    final Label progressLabel = new Label(isSingle ? "Processing asset..." : "Processing assets...");
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

    final Button addBtn = new Button(isSingle ? "Add to Project" : "Add " + assets.size() + " Assets");
    addBtn.setStyleName("ode-ProjectListButton");

    buttonPanel.add(cancelBtn);
    buttonPanel.add(addBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    // Event handlers
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // If button text is "Close", just close the dialog
        if ("Close".equals(addBtn.getText())) {
          dialog.hide();
          return;
        }
        
        boolean track = trackRadio.getValue();
        long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
        
        // Show progress for both single and multiple
        progressPanel.setVisible(true);
        addBtn.setEnabled(false);
        
        if (isSingle) {
          // Single asset
          GlobalAsset asset = assets.get(0);
          globalAssetService.importAssetIntoProject(asset.getFileName(), String.valueOf(projectId), track, 
            new AsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                // Hide options and show success message in main dialog
                trackOption.setVisible(false);
                copyOption.setVisible(false);
                optionsLabel.setVisible(false);
                progressPanel.setVisible(false);
                
                // Add success message directly to main dialog
                Label successMsg = new Label("✓ Asset '" + asset.getFileName() + "' added successfully!");
                successMsg.setStyleName("ode-ComponentRowLabel");
                successMsg.getElement().getStyle().setProperty("color", "#2e7d32");
                successMsg.getElement().getStyle().setProperty("fontWeight", "bold");
                successMsg.getElement().getStyle().setProperty("fontSize", "14px");
                successMsg.getElement().getStyle().setProperty("textAlign", "center");
                successMsg.getElement().getStyle().setProperty("padding", "20px");
                dialogPanel.insert(successMsg, dialogPanel.getWidgetIndex(buttonPanel));
                
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
                
                statusLabel.setText("Asset '" + asset.getFileName() + "' added successfully!");
                addBtn.setText("Close");
                addBtn.setEnabled(true);
                cancelBtn.setVisible(false);
              }
              
              @Override
              public void onFailure(Throwable caught) {
                progressLabel.setText("⚠ Failed to add asset: " + caught.getMessage());
                progressLabel.getElement().getStyle().setProperty("color", "#d32f2f");
                addBtn.setText("Retry");
                addBtn.setEnabled(true);
              }
            });
        } else {
          // Multiple assets
          cancelBtn.setText("Close");
          
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
                // Hide options and show success message in main dialog
                trackOption.setVisible(false);
                copyOption.setVisible(false);
                optionsLabel.setVisible(false);
                progressPanel.setVisible(false);
                
                // Add success message directly to main dialog
                Label successMsg = new Label("✓ All " + assets.size() + " assets added successfully!");
                successMsg.setStyleName("ode-ComponentRowLabel");
                successMsg.getElement().getStyle().setProperty("color", "#2e7d32");
                successMsg.getElement().getStyle().setProperty("fontWeight", "bold");
                successMsg.getElement().getStyle().setProperty("fontSize", "14px");
                successMsg.getElement().getStyle().setProperty("textAlign", "center");
                successMsg.getElement().getStyle().setProperty("padding", "20px");
                dialogPanel.insert(successMsg, dialogPanel.getWidgetIndex(buttonPanel));
                
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
                
                statusLabel.setText(assets.size() + " assets added successfully!");
                addBtn.setText("Close");
                addBtn.setEnabled(true);
                cancelBtn.setVisible(false);
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
      statusLabel.setText("Folder renamed to '" + newFolderName + "'");
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
            statusLabel.setText("Folder renamed to '" + newFolderName + "' (" + totalAssets + " assets updated)");
          }
        }
        
        @Override
        public void onFailure(Throwable caught) {
          statusLabel.setText("Error renaming folder");
          Window.alert("Failed to rename folder: " + caught.getMessage());
        }
      });
    }
  }

  private void deleteFolderAndMoveAssets(final String folderName) {
    statusLabel.setText("Deleting folder...");
    
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
      statusLabel.setText("Empty folder '" + folderName + "' deleted");
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
              statusLabel.setText("Folder '" + folderName + "' deleted (" + totalAssets + " assets moved to root)");
            }
          }
          
          @Override
          public void onFailure(Throwable caught) {
            statusLabel.setText("Error deleting folder");
            Window.alert("Failed to delete folder: " + caught.getMessage());
          }
        });
      }
    }
  }

  // Version management methods

  private void showUpdateAssetDialog(final GlobalAsset asset) {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Upload New Version - " + asset.getFileName());
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);
    dialog.setAutoHideEnabled(false);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.setWidth("400px");
    dialogPanel.getElement().getStyle().setProperty("padding", "16px");

    // Current asset info
    HorizontalPanel currentAssetInfo = new HorizontalPanel();
    currentAssetInfo.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    currentAssetInfo.getElement().getStyle().setProperty("marginBottom", "16px");
    currentAssetInfo.getElement().getStyle().setProperty("padding", "8px");
    currentAssetInfo.getElement().getStyle().setProperty("border", "1px solid #e0e0e0");
    currentAssetInfo.getElement().getStyle().setProperty("borderRadius", "4px");

    String fileName = asset.getFileName().toLowerCase();
    ImageResource iconRes = fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".ogg")
      ? ImagesNeo.INSTANCE.player() 
      : ImagesNeo.INSTANCE.image();
    
    Image assetIcon = new Image(iconRes);
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
        if (!asset.getFolder().isEmpty()) {
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
          statusLabel.setText("New version uploaded successfully");
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

  private String getFileExtension(String filename) {
    int lastDot = filename.lastIndexOf('.');
    return lastDot > 0 ? filename.substring(lastDot) : "";
  }

  private void syncAssetWithProjects(String assetFileName) {
    statusLabel.setText("Syncing asset with projects...");
    
    // Get current project ID if we're in a project
    long currentProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    
    if (currentProjectId != 0) {
      // Sync with current project
      globalAssetService.syncProjectGlobalAsset(assetFileName, currentProjectId, new AsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            statusLabel.setText("Asset synced with current project");
            // Force refresh of current project's asset list and editors
            refreshCurrentProjectAssets();
          } else {
            statusLabel.setText("Asset not used in current project");
          }
        }
        
        @Override
        public void onFailure(Throwable caught) {
          statusLabel.setText("Failed to sync asset: " + caught.getMessage());
        }
      });
    }
    
    // Also get list of all projects using this asset and sync them
    globalAssetService.getProjectsUsingAsset(assetFileName, new AsyncCallback<List<Long>>() {
      @Override
      public void onSuccess(List<Long> projectIds) {
        statusLabel.setText("Asset synced with " + projectIds.size() + " projects");
        // The server-side should handle the actual synchronization
        // Client-side we just need to refresh if current project is affected
        if (projectIds.contains(Ode.getInstance().getCurrentYoungAndroidProjectId())) {
          refreshCurrentProjectAssets();
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        statusLabel.setText("Failed to get project usage info: " + caught.getMessage());
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
      statusLabel.setText("Projects updated - please refresh your designer if needed");
    }
  }


  /**
   * Displays upload error dialog.
   */
  private void showUploadError(String title, String message) {
    final DialogBox errorDialog = new DialogBox();
    errorDialog.setText(title);
    errorDialog.setStyleName("ode-DialogBox");
    errorDialog.setModal(true);
    errorDialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(10);
    dialogPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    Label messageLabel = new Label(message);
    messageLabel.setStyleName("ode-DialogBodyText");
    dialogPanel.add(messageLabel);

    Button okButton = new Button("OK");
    okButton.setStyleName("ode-DialogButton");
    okButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        errorDialog.hide();
      }
    });
    dialogPanel.add(okButton);

    errorDialog.setWidget(dialogPanel);
    errorDialog.center();
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

}