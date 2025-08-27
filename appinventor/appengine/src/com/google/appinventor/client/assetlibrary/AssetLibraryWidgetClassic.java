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
import com.google.appinventor.shared.rpc.globalasset.AssetConflictInfo;
import com.google.appinventor.client.assetlibrary.AssetUploadConflictDialog.ConflictResolution;
import com.google.appinventor.client.assetlibrary.AssetUploadConflictDialog.ConflictResolutionCallback;
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
    injectPremiumStyles();
  }
  
  private void injectPremiumStyles() {
    // Inject custom CSS for enhanced visual effects
    String css = 
      "<style type='text/css'>" +
      ".asset-library-premium {" +
      "  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;" +
      "}" +
      ".asset-library-premium * {" +
      "  box-sizing: border-box;" +
      "}" +
      ".asset-library-premium input:focus {" +
      "  outline: none;" +
      "  box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.3);" +
      "  border-color: #1a73e8;" +
      "}" +
      ".asset-library-premium select:focus {" +
      "  outline: none;" +
      "  box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.3);" +
      "  border-color: #1a73e8;" +
      "}" +
      ".asset-library-premium button:hover {" +
      "  transform: translateY(-1px);" +
      "  box-shadow: 0 4px 8px rgba(0,0,0,0.15);" +
      "}" +
      ".asset-library-premium button:active {" +
      "  transform: translateY(0px);" +
      "  box-shadow: 0 2px 4px rgba(0,0,0,0.1);" +
      "}" +
      ".asset-library-premium button:disabled {" +
      "  cursor: not-allowed;" +
      "  transform: none;" +
      "}" +
      "@keyframes fadeIn {" +
      "  from { opacity: 0; transform: translateY(10px); }" +
      "  to { opacity: 1; transform: translateY(0px); }" +
      "}" +
      ".asset-item-enter {" +
      "  animation: fadeIn 0.3s ease-out;" +
      "}" +
      "/* Version History Dialog Styles */" +
      ".version-history-dialog {" +
      "  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;" +
      "}" +
      ".version-dialog-content {" +
      "  padding: 20px;" +
      "  min-width: 500px;" +
      "}" +
      ".version-header {" +
      "  padding: 16px;" +
      "  background: #f8f9fa;" +
      "  border-radius: 8px;" +
      "  border: 1px solid #e9ecef;" +
      "}" +
      ".version-asset-name {" +
      "  font-size: 18px;" +
      "  font-weight: 600;" +
      "  color: #333;" +
      "}" +
      ".version-asset-folder {" +
      "  font-size: 14px;" +
      "  color: #666;" +
      "  margin-top: 4px;" +
      "}" +
      ".version-list-panel {" +
      "  margin-top: 16px;" +
      "}" +
      ".version-list-title {" +
      "  font-size: 16px;" +
      "  font-weight: 600;" +
      "  color: #333;" +
      "  margin-bottom: 12px;" +
      "}" +
      ".version-entry {" +
      "  padding: 12px 16px;" +
      "  border: 1px solid #e9ecef;" +
      "  border-radius: 6px;" +
      "  margin-bottom: 8px;" +
      "  background: white;" +
      "}" +
      ".version-entry-current {" +
      "  background: #e8f5e8;" +
      "  border-color: #4caf50;" +
      "}" +
      ".version-status {" +
      "  font-size: 14px;" +
      "  font-weight: 600;" +
      "  color: #333;" +
      "}" +
      ".version-date {" +
      "  font-size: 12px;" +
      "  color: #666;" +
      "  margin-top: 4px;" +
      "}" +
      ".version-action-button {" +
      "  padding: 6px 12px;" +
      "  font-size: 12px;" +
      "  border-radius: 4px;" +
      "  cursor: pointer;" +
      "}" +
      ".rollback-button {" +
      "  background: #dc3545;" +
      "  color: white;" +
      "  border: 1px solid #dc3545;" +
      "}" +
      ".current-version-tag {" +
      "  background: #28a745;" +
      "  color: white;" +
      "  padding: 4px 8px;" +
      "  border-radius: 12px;" +
      "  font-size: 12px;" +
      "  font-weight: 600;" +
      "}" +
      ".version-future-note {" +
      "  font-size: 14px;" +
      "  color: #666;" +
      "  font-style: italic;" +
      "  text-align: center;" +
      "  padding: 20px;" +
      "  background: #f8f9fa;" +
      "  border-radius: 6px;" +
      "  border: 1px dashed #dee2e6;" +
      "}" +
      "</style>";
    
    com.google.gwt.dom.client.Document.get().getHead().insertFirst(
        com.google.gwt.dom.client.Document.get().createStyleElement().cast());
    com.google.gwt.dom.client.StyleElement styleElement = 
        com.google.gwt.dom.client.Document.get().getHead().getFirstChild().cast();
    styleElement.setInnerHTML(css.substring(css.indexOf(">")+1, css.lastIndexOf("<")));
    
    // Add premium class to root panel
    rootPanel.addStyleName("asset-library-premium");
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
    // Premium header with modern classic App Inventor styling
    headerContainer = new HorizontalPanel();
    headerContainer.setWidth("100%");
    headerContainer.setStyleName("ode-TopPanel");
    headerContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    headerContainer.getElement().getStyle().setProperty("padding", "12px 16px");
    headerContainer.getElement().getStyle().setProperty("borderBottom", "3px solid #1a73e8");
    headerContainer.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    headerContainer.getElement().getStyle().setProperty("boxShadow", "0 2px 4px rgba(0,0,0,0.1)");
    headerContainer.getElement().getStyle().setProperty("minHeight", "60px");

    // Left section: Title and search
    HorizontalPanel leftSection = new HorizontalPanel();
    leftSection.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    // Premium Asset Library Title with enhanced icon
    HorizontalPanel titlePanel = new HorizontalPanel();
    titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    titlePanel.getElement().getStyle().setProperty("backgroundColor", "#f0f7ff");
    titlePanel.getElement().getStyle().setProperty("padding", "8px 12px");
    titlePanel.getElement().getStyle().setProperty("borderRadius", "8px");
    titlePanel.getElement().getStyle().setProperty("border", "1px solid #e3f2fd");
    
    // Enhanced library icon with background
    HorizontalPanel iconContainer = new HorizontalPanel();
    iconContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    iconContainer.getElement().getStyle().setProperty("backgroundColor", "#1a73e8");
    iconContainer.getElement().getStyle().setProperty("borderRadius", "6px");
    iconContainer.getElement().getStyle().setProperty("padding", "4px");
    iconContainer.getElement().getStyle().setProperty("minWidth", "32px");
    iconContainer.getElement().getStyle().setProperty("minHeight", "32px");
    
    Image libraryIcon = new Image(images.form());
    libraryIcon.setSize("20px", "20px");
    libraryIcon.getElement().getStyle().setProperty("filter", "brightness(0) invert(1)");
    iconContainer.add(libraryIcon);
    titlePanel.add(iconContainer);
    
    // Enhanced title with subtitle
    VerticalPanel titleTextPanel = new VerticalPanel();
    titleTextPanel.getElement().getStyle().setProperty("marginLeft", "10px");
    
    Label titleLabel = new Label("Asset Library");
    titleLabel.setStyleName("ode-ProjectNameLabel");
    titleLabel.getElement().getStyle().setProperty("fontWeight", "600");
    titleLabel.getElement().getStyle().setProperty("fontSize", "18px");
    titleLabel.getElement().getStyle().setProperty("color", "#1a73e8");
    titleLabel.getElement().getStyle().setProperty("lineHeight", "1.2");
    titleTextPanel.add(titleLabel);
    
    Label subtitleLabel = new Label("Manage your global assets");
    subtitleLabel.getElement().getStyle().setProperty("fontSize", "11px");
    subtitleLabel.getElement().getStyle().setProperty("color", "#666666");
    subtitleLabel.getElement().getStyle().setProperty("fontWeight", "400");
    titleTextPanel.add(subtitleLabel);
    
    titlePanel.add(titleTextPanel);
    leftSection.add(titlePanel);

    // Add enhanced spacing with separator
    Label separator = new Label("");
    separator.getElement().getStyle().setProperty("width", "24px");
    separator.getElement().getStyle().setProperty("height", "40px");
    separator.getElement().getStyle().setProperty("borderRight", "1px solid #e0e0e0");
    separator.getElement().getStyle().setProperty("marginLeft", "16px");
    separator.getElement().getStyle().setProperty("marginRight", "16px");
    leftSection.add(separator);

    // Premium search box with enhanced styling
    HorizontalPanel searchContainer = new HorizontalPanel();
    searchContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    searchContainer.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    searchContainer.getElement().getStyle().setProperty("border", "2px solid #e3f2fd");
    searchContainer.getElement().getStyle().setProperty("borderRadius", "8px");
    searchContainer.getElement().getStyle().setProperty("padding", "2px 4px 2px 8px");
    searchContainer.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.1)");
    searchContainer.getElement().getStyle().setProperty("transition", "all 0.2s ease");
    
    // Search icon
    Label searchIcon = new Label("[search]");
    searchIcon.getElement().getStyle().setProperty("fontSize", "14px");
    searchIcon.getElement().getStyle().setProperty("marginRight", "6px");
    searchIcon.getElement().getStyle().setProperty("color", "#666666");
    searchContainer.add(searchIcon);
    
    searchBox = new TextBox();
    searchBox.getElement().setPropertyString("placeholder", "Search assets...");
    searchBox.setStyleName("ode-TextBox");
    searchBox.getElement().getStyle().setProperty("minWidth", "220px");
    searchBox.getElement().getStyle().setProperty("padding", "8px 4px");
    searchBox.getElement().getStyle().setProperty("border", "none");
    searchBox.getElement().getStyle().setProperty("outline", "none");
    searchBox.getElement().getStyle().setProperty("fontSize", "14px");
    searchContainer.add(searchBox);
    
    leftSection.add(searchContainer);

    // Add refined spacing
    Label spacingLabel = new Label("");
    spacingLabel.getElement().getStyle().setProperty("width", "12px");
    leftSection.add(spacingLabel);

    // Premium type filter with enhanced styling
    HorizontalPanel filterContainer = new HorizontalPanel();
    filterContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    filterContainer.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    filterContainer.getElement().getStyle().setProperty("border", "2px solid #e3f2fd");
    filterContainer.getElement().getStyle().setProperty("borderRadius", "8px");
    filterContainer.getElement().getStyle().setProperty("padding", "2px 4px 2px 8px");
    filterContainer.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.1)");
    
    // Filter icon
    Label filterIcon = new Label("");
    filterIcon.getElement().getStyle().setProperty("fontSize", "14px");
    filterIcon.getElement().getStyle().setProperty("marginRight", "6px");
    filterContainer.add(filterIcon);
    
    typeFilter = new ListBox();
    typeFilter.addItem("All Types");
    typeFilter.addItem(" Images");
    typeFilter.addItem(" Audio");
    typeFilter.addItem(" Other");
    typeFilter.setStyleName("ode-ListBox");
    typeFilter.getElement().getStyle().setProperty("minWidth", "140px");
    typeFilter.getElement().getStyle().setProperty("padding", "8px 4px");
    typeFilter.getElement().getStyle().setProperty("border", "none");
    typeFilter.getElement().getStyle().setProperty("outline", "none");
    typeFilter.getElement().getStyle().setProperty("fontSize", "14px");
    typeFilter.getElement().getStyle().setProperty("backgroundColor", "transparent");
    filterContainer.add(typeFilter);
    
    leftSection.add(filterContainer);

    headerContainer.add(leftSection);

    // Spacer
    Label spacer = new Label("");
    spacer.setWidth("100%");
    headerContainer.add(spacer);
    headerContainer.setCellWidth(spacer, "100%");

    // Premium right section with enhanced button styling
    HorizontalPanel rightSection = new HorizontalPanel();
    rightSection.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    rightSection.getElement().getStyle().setProperty("gap", "12px");

    // Premium bulk action buttons with enhanced icons
    addSelectedButton = createPremiumButton(" Add Selected", "#34a853", "#2d8c47", true);
    addSelectedButton.setEnabled(false);
    addSelectedButton.setTitle("Add selected assets to current project");
    rightSection.add(addSelectedButton);

    Label buttonSpacer1 = new Label("");
    buttonSpacer1.getElement().getStyle().setProperty("width", "8px");
    rightSection.add(buttonSpacer1);

    deleteSelectedButton = createPremiumButton(" Delete Selected", "#ea4335", "#d93025", true);
    deleteSelectedButton.setEnabled(false);
    deleteSelectedButton.setTitle("Delete selected assets permanently");
    rightSection.add(deleteSelectedButton);

    // Separator line
    Label buttonSeparator = new Label("");
    buttonSeparator.getElement().getStyle().setProperty("width", "1px");
    buttonSeparator.getElement().getStyle().setProperty("height", "30px");
    buttonSeparator.getElement().getStyle().setProperty("backgroundColor", "#e0e0e0");
    buttonSeparator.getElement().getStyle().setProperty("margin", "0 12px");
    rightSection.add(buttonSeparator);

    // Premium upload button
    uploadButton = createPremiumButton(" Upload Asset", "#1a73e8", "#1557b0", false);
    uploadButton.setTitle("Upload new asset to library");
    uploadButton.getElement().getStyle().setProperty("fontWeight", "600");
    uploadButton.getElement().getStyle().setProperty("padding", "10px 16px");
    rightSection.add(uploadButton);

    Label buttonSpacer2 = new Label("");
    buttonSpacer2.getElement().getStyle().setProperty("width", "12px");
    rightSection.add(buttonSpacer2);

    // Premium close button
    closeButton = new Button("");
    closeButton.setTitle("Close Asset Library");
    closeButton.setStyleName("ode-ProjectListButton");
    closeButton.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");
    closeButton.getElement().getStyle().setProperty("color", "#666666");
    closeButton.getElement().getStyle().setProperty("border", "2px solid #e0e0e0");
    closeButton.getElement().getStyle().setProperty("borderRadius", "50%");
    closeButton.getElement().getStyle().setProperty("width", "36px");
    closeButton.getElement().getStyle().setProperty("height", "36px");
    closeButton.getElement().getStyle().setProperty("fontSize", "16px");
    closeButton.getElement().getStyle().setProperty("fontWeight", "bold");
    closeButton.getElement().getStyle().setProperty("cursor", "pointer");
    closeButton.getElement().getStyle().setProperty("transition", "all 0.2s ease");
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
    // Premium sidebar with modern classic styling
    sidebarPanel = new VerticalPanel();
    sidebarPanel.setWidth("260px");
    sidebarPanel.setHeight("100%");
    sidebarPanel.setStyleName("ode-Designer-LeftColumn");
    sidebarPanel.getElement().getStyle().setProperty("backgroundColor", "#fafbfc");
    sidebarPanel.getElement().getStyle().setProperty("borderRight", "1px solid #e1e4e8");
    sidebarPanel.getElement().getStyle().setProperty("padding", "16px");
    sidebarPanel.getElement().getStyle().setProperty("boxShadow", "inset -1px 0 0 rgba(0,0,0,0.05)");

    // Premium folder section header with enhanced visual hierarchy
    VerticalPanel folderHeaderContainer = new VerticalPanel();
    folderHeaderContainer.setWidth("100%");
    folderHeaderContainer.getElement().getStyle().setProperty("marginBottom", "16px");
    
    HorizontalPanel folderHeader = new HorizontalPanel();
    folderHeader.setWidth("100%");
    folderHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    folderHeader.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    folderHeader.getElement().getStyle().setProperty("padding", "12px");
    folderHeader.getElement().getStyle().setProperty("borderRadius", "8px");
    folderHeader.getElement().getStyle().setProperty("border", "1px solid #e1e4e8");
    folderHeader.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.05)");

    // Enhanced folder icon and title
    HorizontalPanel titleSection = new HorizontalPanel();
    titleSection.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    Label folderIcon = new Label("[folder]");
    folderIcon.getElement().getStyle().setProperty("fontSize", "16px");
    folderIcon.getElement().getStyle().setProperty("marginRight", "8px");
    titleSection.add(folderIcon);
    
    Label folderTitle = new Label("Folders");
    folderTitle.setStyleName("ode-ComponentRowLabel");
    folderTitle.getElement().getStyle().setProperty("fontWeight", "600");
    folderTitle.getElement().getStyle().setProperty("fontSize", "15px");
    folderTitle.getElement().getStyle().setProperty("color", "#24292e");
    titleSection.add(folderTitle);
    
    folderHeader.add(titleSection);

    // Premium action buttons with enhanced styling
    HorizontalPanel folderActions = new HorizontalPanel();
    folderActions.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    folderActions.getElement().getStyle().setProperty("gap", "6px");

    Button newFolderBtn = createEnhancedButton("+", "#34a853", "Create new folder");
    Button renameFolderBtn = createEnhancedButton("", "#ff9800", "Rename selected folder");  
    Button deleteFolderBtn = createEnhancedButton("", "#f44336", "Delete selected folder");

    // Add event handlers for folder management
    setupFolderManagementHandlers(newFolderBtn, renameFolderBtn, deleteFolderBtn);

    folderActions.add(newFolderBtn);
    folderActions.add(renameFolderBtn);
    folderActions.add(deleteFolderBtn);
    folderHeader.add(folderActions);
    folderHeader.setCellHorizontalAlignment(folderActions, HasHorizontalAlignment.ALIGN_RIGHT);

    folderHeaderContainer.add(folderHeader);
    sidebarPanel.add(folderHeaderContainer);

    // Premium folder list with enhanced styling
    ScrollPanel folderScrollContainer = new ScrollPanel();
    folderScrollContainer.setWidth("100%");
    folderScrollContainer.setHeight("350px");
    folderScrollContainer.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    folderScrollContainer.getElement().getStyle().setProperty("border", "1px solid #e1e4e8");
    folderScrollContainer.getElement().getStyle().setProperty("borderRadius", "8px");
    folderScrollContainer.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.05)");
    
    folderListPanel = new VerticalPanel();
    folderListPanel.setWidth("100%");
    folderListPanel.setSpacing(1);
    folderListPanel.getElement().getStyle().setProperty("padding", "8px");
    
    folderScrollContainer.add(folderListPanel);
    sidebarPanel.add(folderScrollContainer);

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

  private Button createPremiumButton(String text, String bgColor, String hoverColor, boolean isSecondary) {
    Button button = new Button(text);
    button.setStyleName("ode-ProjectListButton");
    
    if (isSecondary) {
      button.getElement().getStyle().setProperty("padding", "8px 12px");
      button.getElement().getStyle().setProperty("fontSize", "13px");
    } else {
      button.getElement().getStyle().setProperty("padding", "10px 16px");
      button.getElement().getStyle().setProperty("fontSize", "14px");
    }
    
    button.getElement().getStyle().setProperty("backgroundColor", bgColor);
    button.getElement().getStyle().setProperty("color", "white");
    button.getElement().getStyle().setProperty("border", "none");
    button.getElement().getStyle().setProperty("borderRadius", "8px");
    button.getElement().getStyle().setProperty("fontWeight", "500");
    button.getElement().getStyle().setProperty("cursor", "pointer");
    button.getElement().getStyle().setProperty("transition", "all 0.2s ease");
    button.getElement().getStyle().setProperty("boxShadow", "0 2px 4px rgba(0,0,0,0.1)");
    button.getElement().getStyle().setProperty("textAlign", "center");
    button.getElement().getStyle().setProperty("userSelect", "none");
    
    return button;
  }

  private Button createEnhancedButton(String text, String color, String tooltip) {
    Button button = new Button(text);
    button.setStyleName("ode-ProjectListButton");
    button.setTitle(tooltip);
    button.getElement().getStyle().setProperty("minWidth", "32px");
    button.getElement().getStyle().setProperty("height", "32px");
    button.getElement().getStyle().setProperty("padding", "6px 8px");
    button.getElement().getStyle().setProperty("backgroundColor", color);
    button.getElement().getStyle().setProperty("color", "white");
    button.getElement().getStyle().setProperty("border", "none");
    button.getElement().getStyle().setProperty("borderRadius", "6px");
    button.getElement().getStyle().setProperty("fontSize", "12px");
    button.getElement().getStyle().setProperty("fontWeight", "500");
    button.getElement().getStyle().setProperty("cursor", "pointer");
    button.getElement().getStyle().setProperty("transition", "all 0.2s ease");
    button.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.1)");
    return button;
  }

  private void createAssetList() {
    // Premium asset list container with modern classic styling
    VerticalPanel assetContainer = new VerticalPanel();
    assetContainer.setWidth("100%");
    assetContainer.setHeight("100%");
    assetContainer.setStyleName("ode-Box-body");
    assetContainer.getElement().getStyle().setProperty("padding", "16px");
    assetContainer.getElement().getStyle().setProperty("backgroundColor", "#fafbfc");

    // Premium assets header section
    HorizontalPanel assetHeader = new HorizontalPanel();
    assetHeader.setWidth("100%");
    assetHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    assetHeader.getElement().getStyle().setProperty("marginBottom", "16px");
    assetHeader.getElement().getStyle().setProperty("padding", "12px 16px");
    assetHeader.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    assetHeader.getElement().getStyle().setProperty("border", "1px solid #e1e4e8");
    assetHeader.getElement().getStyle().setProperty("borderRadius", "8px");
    assetHeader.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.05)");
    
    HorizontalPanel headerLeft = new HorizontalPanel();
    headerLeft.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    Label assetsIcon = new Label("");
    assetsIcon.getElement().getStyle().setProperty("fontSize", "16px");
    assetsIcon.getElement().getStyle().setProperty("marginRight", "8px");
    headerLeft.add(assetsIcon);
    
    Label assetsLabel = new Label("Your Assets");
    assetsLabel.setStyleName("ode-ComponentRowLabel");
    assetsLabel.getElement().getStyle().setProperty("fontWeight", "600");
    assetsLabel.getElement().getStyle().setProperty("fontSize", "15px");
    assetsLabel.getElement().getStyle().setProperty("color", "#24292e");
    headerLeft.add(assetsLabel);
    
    assetHeader.add(headerLeft);
    
    // Add asset count indicator (will be populated dynamically)
    statusLabel = new Label("Loading assets...");
    statusLabel.setStyleName("ode-ComponentRowLabel");
    statusLabel.getElement().getStyle().setProperty("fontSize", "12px");
    statusLabel.getElement().getStyle().setProperty("color", "#586069");
    statusLabel.getElement().getStyle().setProperty("marginLeft", "auto");
    assetHeader.add(statusLabel);
    assetHeader.setCellHorizontalAlignment(statusLabel, HasHorizontalAlignment.ALIGN_RIGHT);
    
    assetContainer.add(assetHeader);

    // Premium scrollable list with enhanced styling
    assetScrollPanel = new ScrollPanel();
    assetScrollPanel.setWidth("100%");
    assetScrollPanel.setHeight("500px");
    assetScrollPanel.setStyleName("ode-Explorer");
    assetScrollPanel.getElement().getStyle().setProperty("border", "1px solid #e1e4e8");
    assetScrollPanel.getElement().getStyle().setProperty("borderRadius", "8px");
    assetScrollPanel.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    assetScrollPanel.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.05)");

    // Premium asset list panel with refined spacing
    assetListPanel = new VerticalPanel();
    assetListPanel.setWidth("100%");
    assetListPanel.setSpacing(0);
    assetListPanel.getElement().getStyle().setProperty("padding", "8px");

    assetScrollPanel.add(assetListPanel);
    assetContainer.add(assetScrollPanel);
    mainContentPanel.add(assetContainer);
    mainContentPanel.setCellWidth(assetContainer, "100%");
    mainContentPanel.setCellHeight(assetContainer, "100%");
  }

  private void createFooter() {
    // Premium footer with modern classic styling
    footerPanel = new HorizontalPanel();
    footerPanel.setWidth("100%");
    footerPanel.setStyleName("ode-StatusPanel");
    footerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    footerPanel.getElement().getStyle().setProperty("padding", "12px 16px");
    footerPanel.getElement().getStyle().setProperty("backgroundColor", "#f6f8fa");
    footerPanel.getElement().getStyle().setProperty("borderTop", "1px solid #e1e4e8");
    footerPanel.getElement().getStyle().setProperty("minHeight", "40px");

    // Move status label creation to asset header section
    Label footerText = new Label("Tip: Drag assets to folders to organize them");
    footerText.setStyleName("ode-StatusPanelLabel");
    footerText.getElement().getStyle().setProperty("fontSize", "12px");
    footerText.getElement().getStyle().setProperty("color", "#586069");
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
    folderRow.getElement().getStyle().setProperty("padding", "10px 12px");
    folderRow.getElement().getStyle().setProperty("borderRadius", "6px");
    folderRow.getElement().getStyle().setProperty("cursor", "pointer");
    folderRow.getElement().getStyle().setProperty("transition", "all 0.2s ease");
    folderRow.getElement().getStyle().setProperty("margin", "2px 0");

    // Folder icon
    String iconText = "All Assets".equals(folderName) ? "" : 
                     "Recent".equals(folderName) ? "[recent]" : "[folder]";
    Label folderIcon = new Label(iconText);
    folderIcon.getElement().getStyle().setProperty("fontSize", "14px");
    folderIcon.getElement().getStyle().setProperty("marginRight", "8px");
    folderIcon.getElement().getStyle().setProperty("minWidth", "20px");
    folderRow.add(folderIcon);

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

    // Apply premium selection styling
    if (index == selectedFolderIndex) {
      folderRow.getElement().getStyle().setProperty("backgroundColor", "#e3f2fd");
      folderRow.getElement().getStyle().setProperty("border", "1px solid #1a73e8");
      nameLabel.getElement().getStyle().setProperty("color", "#1a73e8");
      nameLabel.getElement().getStyle().setProperty("fontWeight", "600");
    } else {
      folderRow.getElement().getStyle().setProperty("backgroundColor", "transparent");
      folderRow.getElement().getStyle().setProperty("border", "1px solid transparent");
      nameLabel.getElement().getStyle().setProperty("color", "#586069");
    }

    // Premium click handler with hover effects
    folderRow.addDomHandler(new com.google.gwt.event.dom.client.MouseOverHandler() {
      @Override
      public void onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent event) {
        if (index != selectedFolderIndex) {
          folderRow.getElement().getStyle().setProperty("backgroundColor", "#f0f7ff");
          folderRow.getElement().getStyle().setProperty("transform", "translateX(2px)");
        }
      }
    }, com.google.gwt.event.dom.client.MouseOverEvent.getType());
    
    folderRow.addDomHandler(new com.google.gwt.event.dom.client.MouseOutHandler() {
      @Override
      public void onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent event) {
        if (index != selectedFolderIndex) {
          folderRow.getElement().getStyle().setProperty("backgroundColor", "transparent");
          folderRow.getElement().getStyle().setProperty("transform", "translateX(0px)");
        }
      }
    }, com.google.gwt.event.dom.client.MouseOutEvent.getType());
    
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
    emptyState.getElement().getStyle().setProperty("padding", "60px 20px");
    emptyState.getElement().getStyle().setProperty("textAlign", "center");

    // Premium empty state icon
    Label emptyIcon = new Label("");
    emptyIcon.getElement().getStyle().setProperty("fontSize", "48px");
    emptyIcon.getElement().getStyle().setProperty("marginBottom", "16px");
    emptyIcon.getElement().getStyle().setProperty("opacity", "0.5");
    emptyState.add(emptyIcon);

    // Premium empty message
    Label emptyMessage = new Label("No assets found");
    emptyMessage.setStyleName("ode-ComponentRowLabel");
    emptyMessage.getElement().getStyle().setProperty("fontSize", "18px");
    emptyMessage.getElement().getStyle().setProperty("fontWeight", "600");
    emptyMessage.getElement().getStyle().setProperty("color", "#586069");
    emptyMessage.getElement().getStyle().setProperty("marginBottom", "8px");
    emptyState.add(emptyMessage);

    // Premium sub message
    Label emptySubMessage = new Label("Try adjusting your search or upload new assets to get started");
    emptySubMessage.setStyleName("ode-ComponentRowLabel");
    emptySubMessage.getElement().getStyle().setProperty("fontSize", "14px");
    emptySubMessage.getElement().getStyle().setProperty("color", "#959da5");
    emptySubMessage.getElement().getStyle().setProperty("lineHeight", "1.5");
    emptyState.add(emptySubMessage);

    assetListPanel.add(emptyState);
  }

  private void displayAssets(List<GlobalAsset> assets) {
    for (final GlobalAsset asset : assets) {
      HorizontalPanel assetRow = createAssetRow(asset);
      assetRow.addStyleName("asset-item-enter");
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
    row.getElement().getStyle().setProperty("borderRadius", "8px");
    row.getElement().getStyle().setProperty("border", "1px solid #e1e4e8");
    row.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    row.getElement().getStyle().setProperty("transition", "all 0.2s ease");
    row.getElement().getStyle().setProperty("cursor", "pointer");
    
    // Add hover effect styling
    row.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.05)");

    // Premium checkbox for selection
    HorizontalPanel checkboxContainer = new HorizontalPanel();
    checkboxContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    checkboxContainer.getElement().getStyle().setProperty("marginRight", "12px");
    
    final CheckBox checkBox = new CheckBox();
    checkBox.getElement().getStyle().setProperty("transform", "scale(1.2)");
    checkBox.getElement().getStyle().setProperty("cursor", "pointer");
    assetCheckBoxes.add(checkBox);
    checkboxContainer.add(checkBox);
    row.add(checkboxContainer);

    // Premium asset preview/icon with container
    HorizontalPanel previewContainer = new HorizontalPanel();
    previewContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    previewContainer.getElement().getStyle().setProperty("marginRight", "12px");
    
    Widget previewWidget = createPreviewWidget(asset);
    previewContainer.add(previewWidget);
    row.add(previewContainer);

    // Premium asset name and details
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
    nameLabel.getElement().getStyle().setProperty("color", "#24292e");
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

    // Premium action buttons with enhanced styling
    HorizontalPanel actionPanel = new HorizontalPanel();
    actionPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    actionPanel.getElement().getStyle().setProperty("gap", "4px");

    // Premium preview button
    Button previewBtn = createActionButton(" Preview", "#6c757d", "#5a6268");
    previewBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        previewAsset(asset);
      }
    });
    actionPanel.add(previewBtn);

    Label actionSpacer1 = new Label("");
    actionSpacer1.getElement().getStyle().setProperty("width", "4px");
    actionPanel.add(actionSpacer1);

    // Version history button
    Button historyBtn = createActionButton(" History", "#17a2b8", "#138496");
    historyBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showVersionHistory(asset);
      }
    });
    actionPanel.add(historyBtn);

    Label actionSpacerHistory = new Label("");
    actionSpacerHistory.getElement().getStyle().setProperty("width", "4px");
    actionPanel.add(actionSpacerHistory);

    // Project Management button (conditional)
    final Button manageBtn = createActionButton(" Manage", "#007bff", "#0056b3");
    manageBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showProjectManagementDialog(asset);
      }
    });
    manageBtn.setVisible(false); // Will be shown if asset is used by projects
    actionPanel.add(manageBtn);

    // Check if asset is used by projects and show manage button
    globalAssetService.getProjectsUsingAsset(asset.getFileName(), new AsyncCallback<List<Long>>() {
      @Override
      public void onSuccess(List<Long> projectIds) {
        if (projectIds != null && !projectIds.isEmpty()) {
          manageBtn.setVisible(true);
          manageBtn.setTitle("Manage usage in " + projectIds.size() + " project(s)");
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        // Keep button hidden on failure
      }
    });

    Label actionSpacer2 = new Label("");
    actionSpacer2.getElement().getStyle().setProperty("width", "4px");
    actionPanel.add(actionSpacer2);

    // Premium add button
    Button addBtn = createActionButton(" Add", "#28a745", "#218838");
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showAddToProjectDialog(asset);
      }
    });
    actionPanel.add(addBtn);

    Label actionSpacer3 = new Label("");
    actionSpacer3.getElement().getStyle().setProperty("width", "4px");
    actionPanel.add(actionSpacer3);

    // Premium delete button
    Button deleteBtn = createActionButton(" Delete", "#dc3545", "#c82333");
    deleteBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        showEnhancedDeleteConfirmation(asset);
      }
    });
    actionPanel.add(deleteBtn);

    row.add(actionPanel);

    // Drag and drop support
    setupAssetDragDrop(row, asset);

    // Premium hover effects for asset rows
    row.addDomHandler(new com.google.gwt.event.dom.client.MouseOverHandler() {
      @Override
      public void onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent event) {
        row.getElement().getStyle().setProperty("backgroundColor", "#f8f9ff");
        row.getElement().getStyle().setProperty("borderColor", "#c6e2ff");
        row.getElement().getStyle().setProperty("boxShadow", "0 2px 8px rgba(0,0,0,0.1)");
        row.getElement().getStyle().setProperty("transform", "translateY(-1px)");
      }
    }, com.google.gwt.event.dom.client.MouseOverEvent.getType());
    
    row.addDomHandler(new com.google.gwt.event.dom.client.MouseOutHandler() {
      @Override
      public void onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent event) {
        row.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
        row.getElement().getStyle().setProperty("borderColor", "#e1e4e8");
        row.getElement().getStyle().setProperty("boxShadow", "0 1px 3px rgba(0,0,0,0.05)");
        row.getElement().getStyle().setProperty("transform", "translateY(0px)");
      }
    }, com.google.gwt.event.dom.client.MouseOutEvent.getType());

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

  private Button createActionButton(String text, String bgColor, String hoverColor) {
    Button button = new Button(text);
    button.setStyleName("ode-ProjectListButton");
    button.getElement().getStyle().setProperty("padding", "6px 10px");
    button.getElement().getStyle().setProperty("fontSize", "12px");
    button.getElement().getStyle().setProperty("fontWeight", "500");
    button.getElement().getStyle().setProperty("backgroundColor", bgColor);
    button.getElement().getStyle().setProperty("color", "white");
    button.getElement().getStyle().setProperty("border", "none");
    button.getElement().getStyle().setProperty("borderRadius", "5px");
    button.getElement().getStyle().setProperty("cursor", "pointer");
    button.getElement().getStyle().setProperty("transition", "all 0.2s ease");
    button.getElement().getStyle().setProperty("boxShadow", "0 1px 2px rgba(0,0,0,0.1)");
    button.getElement().getStyle().setProperty("minWidth", "80px");
    button.getElement().getStyle().setProperty("textAlign", "center");
    return button;
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
    
    // Premium preview container
    HorizontalPanel previewContainer = new HorizontalPanel();
    previewContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    previewContainer.getElement().getStyle().setProperty("width", "48px");
    previewContainer.getElement().getStyle().setProperty("height", "48px");
    previewContainer.getElement().getStyle().setProperty("borderRadius", "8px");
    previewContainer.getElement().getStyle().setProperty("border", "2px solid #e1e4e8");
    previewContainer.getElement().getStyle().setProperty("backgroundColor", "#f6f8fa");
    previewContainer.getElement().getStyle().setProperty("justifyContent", "center");
    previewContainer.getElement().getStyle().setProperty("alignItems", "center");
    previewContainer.getElement().getStyle().setProperty("overflow", "hidden");
    
    if (StorageUtil.isImageFile(filePath)) {
      // Create premium image preview
      Image img = new Image("/ode/download/globalasset/" + asset.getFileName());
      img.setWidth("44px");
      img.setHeight("44px");
      img.getElement().getStyle().setProperty("objectFit", "cover");
      img.getElement().getStyle().setProperty("borderRadius", "6px");
      previewContainer.add(img);
    } else {
      // Premium icon for non-image files
      String iconText;
      String iconColor;
      if (StorageUtil.isAudioFile(filePath)) {
        iconText = "";
        iconColor = "#007bff";
      } else {
        iconText = "";
        iconColor = "#6c757d";
      }
      
      Label iconLabel = new Label(iconText);
      iconLabel.getElement().getStyle().setProperty("fontSize", "24px");
      iconLabel.getElement().getStyle().setProperty("color", iconColor);
      iconLabel.getElement().getStyle().setProperty("textAlign", "center");
      previewContainer.add(iconLabel);
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
      addSelectedButton.setText(" Add " + selectedCount + " Selected");
      deleteSelectedButton.setText(" Delete " + selectedCount + " Selected");
      
      // Enhanced styling for enabled state
      addSelectedButton.getElement().getStyle().setProperty("opacity", "1.0");
      deleteSelectedButton.getElement().getStyle().setProperty("opacity", "1.0");
    } else {
      addSelectedButton.setText(" Add Selected");
      deleteSelectedButton.setText(" Delete Selected");
      
      // Dimmed styling for disabled state
      addSelectedButton.getElement().getStyle().setProperty("opacity", "0.6");
      deleteSelectedButton.getElement().getStyle().setProperty("opacity", "0.6");
    }
  }

  private void updateStatusLabel(int assetCount) {
    String statusText;
    if (assetCount == 0) {
      statusText = "No assets";
    } else if (assetCount == 1) {
      statusText = "1 asset";
    } else {
      statusText = assetCount + " assets";
    }
    statusLabel.setText(statusText);
    statusLabel.getElement().getStyle().setProperty("color", assetCount > 0 ? "#28a745" : "#6c757d");
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
        
        // Check if asset already exists and handle conflicts
        handleAssetUploadWithConflictCheck(filename, dialog, form, fileUpload);
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
    form.setAction(GWT.getModuleBaseURL() + "upload/" + ServerLayout.UPLOAD_GLOBAL_ASSET + "/" + filename);
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

  private void showProjectManagementDialog(final GlobalAsset asset) {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Manage Asset in Projects");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);
    dialogPanel.getElement().getStyle().setProperty("minWidth", "500px");

    // Asset header
    HorizontalPanel assetHeader = new HorizontalPanel();
    assetHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    assetHeader.getElement().getStyle().setProperty("backgroundColor", "#f8f9fa");
    assetHeader.getElement().getStyle().setProperty("padding", "12px");
    assetHeader.getElement().getStyle().setProperty("borderRadius", "6px");
    assetHeader.getElement().getStyle().setProperty("border", "1px solid #e9ecef");
    
    Widget previewWidget = createPreviewWidget(asset);
    assetHeader.add(previewWidget);
    
    VerticalPanel headerDetails = new VerticalPanel();
    headerDetails.getElement().getStyle().setProperty("marginLeft", "12px");
    
    Label assetName = new Label(asset.getFileName());
    assetName.setStyleName("ode-ComponentRowLabel");
    assetName.getElement().getStyle().setProperty("fontWeight", "600");
    assetName.getElement().getStyle().setProperty("fontSize", "16px");
    headerDetails.add(assetName);
    
    Label assetInfo = new Label("Version: " + getVersionText(asset.getTimestamp()));
    assetInfo.setStyleName("ode-ComponentRowLabel");
    assetInfo.getElement().getStyle().setProperty("fontSize", "12px");
    assetInfo.getElement().getStyle().setProperty("color", "#6c757d");
    headerDetails.add(assetInfo);
    
    assetHeader.add(headerDetails);
    dialogPanel.add(assetHeader);

    // Project usage section
    Label usageTitle = new Label(" Project Usage");
    usageTitle.setStyleName("ode-ComponentRowLabel");
    usageTitle.getElement().getStyle().setProperty("fontWeight", "600");
    usageTitle.getElement().getStyle().setProperty("fontSize", "15px");
    usageTitle.getElement().getStyle().setProperty("marginTop", "8px");
    dialogPanel.add(usageTitle);

    final VerticalPanel projectList = new VerticalPanel();
    projectList.setWidth("100%");
    projectList.getElement().getStyle().setProperty("backgroundColor", "#ffffff");
    projectList.getElement().getStyle().setProperty("border", "1px solid #e9ecef");
    projectList.getElement().getStyle().setProperty("borderRadius", "6px");
    projectList.getElement().getStyle().setProperty("maxHeight", "300px");
    projectList.getElement().getStyle().setProperty("overflowY", "auto");
    
    Label loadingLabel = new Label("Loading project information...");
    loadingLabel.setStyleName("ode-ComponentRowLabel");
    loadingLabel.getElement().getStyle().setProperty("padding", "20px");
    loadingLabel.getElement().getStyle().setProperty("textAlign", "center");
    loadingLabel.getElement().getStyle().setProperty("color", "#6c757d");
    projectList.add(loadingLabel);
    
    dialogPanel.add(projectList);

    // Load projects using this asset
    globalAssetService.getProjectsUsingAsset(asset.getFileName(), new AsyncCallback<List<Long>>() {
      @Override
      public void onSuccess(List<Long> projectIds) {
        projectList.clear();
        
        if (projectIds == null || projectIds.isEmpty()) {
          Label noProjectsLabel = new Label("This asset is not currently used by any projects.");
          noProjectsLabel.setStyleName("ode-ComponentRowLabel");
          noProjectsLabel.getElement().getStyle().setProperty("padding", "20px");
          noProjectsLabel.getElement().getStyle().setProperty("textAlign", "center");
          noProjectsLabel.getElement().getStyle().setProperty("color", "#6c757d");
          noProjectsLabel.getElement().getStyle().setProperty("fontStyle", "italic");
          projectList.add(noProjectsLabel);
          return;
        }

        for (final Long projectId : projectIds) {
          HorizontalPanel projectRow = new HorizontalPanel();
          projectRow.setWidth("100%");
          projectRow.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
          projectRow.getElement().getStyle().setProperty("padding", "12px");
          projectRow.getElement().getStyle().setProperty("borderBottom", "1px solid #f8f9fa");
          
          // Project info
          VerticalPanel projectInfo = new VerticalPanel();
          
          Label projectName = new Label("Project #" + projectId);
          projectName.setStyleName("ode-ComponentRowLabel");
          projectName.getElement().getStyle().setProperty("fontWeight", "500");
          projectInfo.add(projectName);
          
          Label statusLabel = new Label(" Linked and tracking updates");
          statusLabel.setStyleName("ode-ComponentRowLabel");
          statusLabel.getElement().getStyle().setProperty("fontSize", "12px");
          statusLabel.getElement().getStyle().setProperty("color", "#28a745");
          projectInfo.add(statusLabel);
          
          projectRow.add(projectInfo);
          projectRow.setCellWidth(projectInfo, "100%");
          
          // Action buttons
          HorizontalPanel actionButtons = new HorizontalPanel();
          actionButtons.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
          actionButtons.getElement().getStyle().setProperty("gap", "8px");
          
          Button syncBtn = createActionButton(" Sync", "#17a2b8", "#117a8b");
          syncBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              syncAssetInProject(asset.getFileName(), projectId);
            }
          });
          actionButtons.add(syncBtn);
          
          Button unlinkBtn = createActionButton(" Unlink", "#ffc107", "#e0a800");
          unlinkBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              if (Window.confirm("Remove '" + asset.getFileName() + "' from Project #" + projectId + "?")) {
                unlinkAssetFromProject(asset.getFileName(), projectId, projectRow);
              }
            }
          });
          actionButtons.add(unlinkBtn);
          
          projectRow.add(actionButtons);
          projectList.add(projectRow);
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        projectList.clear();
        Label errorLabel = new Label("Failed to load project information: " + caught.getMessage());
        errorLabel.setStyleName("ode-ComponentRowLabel");
        errorLabel.getElement().getStyle().setProperty("padding", "20px");
        errorLabel.getElement().getStyle().setProperty("textAlign", "center");
        errorLabel.getElement().getStyle().setProperty("color", "#dc3545");
        projectList.add(errorLabel);
      }
    });

    // Control buttons
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);
    buttonPanel.getElement().getStyle().setProperty("marginTop", "16px");

    Button closeBtn = new Button("Close");
    closeBtn.setStyleName("ode-ProjectListButton");
    closeBtn.getElement().getStyle().setProperty("backgroundColor", "#6c757d");
    closeBtn.getElement().getStyle().setProperty("color", "white");

    Button syncAllBtn = new Button(" Sync All Projects");
    syncAllBtn.setStyleName("ode-ProjectListButton");
    syncAllBtn.getElement().getStyle().setProperty("backgroundColor", "#17a2b8");
    syncAllBtn.getElement().getStyle().setProperty("color", "white");

    buttonPanel.add(closeBtn);
    buttonPanel.add(syncAllBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    // Event handlers
    closeBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    syncAllBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // TODO: Implement sync all functionality
        Window.alert("Sync all functionality coming soon!");
      }
    });

    dialog.center();
    dialog.show();
  }

  private void showEnhancedDeleteConfirmation(final GlobalAsset asset) {
    // First check if asset is used by projects
    globalAssetService.getProjectsUsingAsset(asset.getFileName(), new AsyncCallback<List<Long>>() {
      @Override
      public void onSuccess(List<Long> projectIds) {
        if (projectIds != null && !projectIds.isEmpty()) {
          showDeleteConfirmationWithProjects(asset, projectIds);
        } else {
          showSimpleDeleteConfirmation(asset);
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        // Fallback to simple confirmation
        showSimpleDeleteConfirmation(asset);
      }
    });
  }

  private void showDeleteConfirmationWithProjects(final GlobalAsset asset, List<Long> projectIds) {
    final DialogBox dialog = new DialogBox();
    dialog.setText("Cannot Delete Asset");
    dialog.setStyleName("ode-DialogBox");
    dialog.setModal(true);
    dialog.setGlassEnabled(true);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.setSpacing(12);

    // Error header
    HorizontalPanel errorHeader = new HorizontalPanel();
    errorHeader.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    errorHeader.getElement().getStyle().setProperty("backgroundColor", "#f8d7da");
    errorHeader.getElement().getStyle().setProperty("padding", "12px");
    errorHeader.getElement().getStyle().setProperty("borderRadius", "6px");
    errorHeader.getElement().getStyle().setProperty("border", "1px solid #f5c6cb");
    
    Label errorIcon = new Label("[error]");
    errorIcon.getElement().getStyle().setProperty("fontSize", "24px");
    errorIcon.getElement().getStyle().setProperty("marginRight", "10px");
    errorHeader.add(errorIcon);
    
    VerticalPanel errorText = new VerticalPanel();
    Label errorTitle = new Label("Asset is in use");
    errorTitle.setStyleName("ode-ComponentRowLabel");
    errorTitle.getElement().getStyle().setProperty("fontWeight", "600");
    errorTitle.getElement().getStyle().setProperty("fontSize", "16px");
    errorTitle.getElement().getStyle().setProperty("color", "#721c24");
    errorText.add(errorTitle);
    
    Label errorSubtitle = new Label("'" + asset.getFileName() + "' is used by " + projectIds.size() + " project(s)");
    errorSubtitle.setStyleName("ode-ComponentRowLabel");
    errorSubtitle.getElement().getStyle().setProperty("fontSize", "14px");
    errorSubtitle.getElement().getStyle().setProperty("color", "#721c24");
    errorText.add(errorSubtitle);
    
    errorHeader.add(errorText);
    dialogPanel.add(errorHeader);

    // Instructions
    Label instructions = new Label("To delete this asset, you must first remove it from all projects using it.");
    instructions.setStyleName("ode-ComponentRowLabel");
    instructions.getElement().getStyle().setProperty("fontSize", "14px");
    instructions.getElement().getStyle().setProperty("color", "#495057");
    dialogPanel.add(instructions);

    // Action buttons
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(8);

    Button closeBtn = new Button("Cancel");
    closeBtn.setStyleName("ode-ProjectListButton");

    Button manageBtn = new Button(" Manage Projects");
    manageBtn.setStyleName("ode-ProjectListButton");
    manageBtn.getElement().getStyle().setProperty("backgroundColor", "#007bff");
    manageBtn.getElement().getStyle().setProperty("color", "white");

    buttonPanel.add(closeBtn);
    buttonPanel.add(manageBtn);
    dialogPanel.add(buttonPanel);

    dialog.setWidget(dialogPanel);

    closeBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    manageBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
        showProjectManagementDialog(asset);
      }
    });

    dialog.center();
    dialog.show();
  }

  private void showSimpleDeleteConfirmation(final GlobalAsset asset) {
    if (Window.confirm("Are you sure you want to delete '" + asset.getFileName() + "'?\n\nThis action cannot be undone.")) {
      deleteAsset(asset);
    }
  }

  private void syncAssetInProject(String assetFileName, Long projectId) {
    globalAssetService.syncProjectGlobalAsset(assetFileName, projectId, new AsyncCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean updated) {
        if (updated) {
          statusLabel.setText("Asset synced successfully in project #" + projectId);
        } else {
          statusLabel.setText("Asset in project #" + projectId + " is already up-to-date");
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Failed to sync asset: " + caught.getMessage());
      }
    });
  }

  private void unlinkAssetFromProject(String assetFileName, Long projectId, HorizontalPanel projectRow) {
    globalAssetService.removeAssetFromProject(assetFileName, projectId, new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        projectRow.removeFromParent();
        statusLabel.setText("Asset unlinked from project #" + projectId);
        refreshAssetList(); // Refresh to update usage indicators
      }
      
      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Failed to unlink asset: " + caught.getMessage());
      }
    });
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
        statusLabel.setText("Replacing existing asset...");
        break;
        
      case CREATE_NEW_ASSET:
        if (newAssetName != null && !newAssetName.trim().isEmpty()) {
          proceedWithUpload(newAssetName.trim(), form);
          statusLabel.setText("Uploading new asset as '" + newAssetName + "'...");
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
}
