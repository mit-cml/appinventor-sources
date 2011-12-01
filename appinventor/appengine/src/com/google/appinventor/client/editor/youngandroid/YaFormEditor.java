// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.SimpleVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.components.FormChangeListener;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.PropertiesPanel;
import com.google.appinventor.client.youngandroid.CodeblocksManager;
import com.google.appinventor.client.youngandroid.YoungAndroidFormUpgrader;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.common.base.Preconditions;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Editor for Young Android Form (.scm) files.
 *
 * <p>This editor shows a designer that provides support for visual design of
 * forms.</p>
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 */
public final class YaFormEditor extends SimpleEditor implements FormChangeListener {

  private static class FileContentHolder {
    private String content;

    FileContentHolder(String content) {
      this.content = content;
    }

    void setFileContent(String content) {
      this.content = content;
    }

    String getFileContent() {
      return content;
    }
  }

  // JSON parser
  private static final JSONParser JSON_PARSER = new ClientJsonParser();

  private static final SimpleComponentDatabase COMPONENT_DATABASE =
      SimpleComponentDatabase.getInstance();

  private final YoungAndroidFormNode formNode;

  // Flag to indicate when loading the file is completed. This is needed because building the mock
  // form from the file properties fires events that need to be ignored, otherwise the file will be
  // marked as being modified.
  private boolean loadComplete;

  // References to other panels that we need to control.
  private final SourceStructureExplorer sourceStructureExplorer;

  // Panels that are used as the content of the palette and properties boxes.
  private final YoungAndroidPalettePanel palettePanel;
  private final PropertiesPanel designProperties;

  // UI elements
  private final SimpleVisibleComponentsPanel visibleComponentsPanel;
  private final SimpleNonVisibleComponentsPanel nonVisibleComponentsPanel;

  private MockForm form;  // initialized lazily after the file is loaded from the ODE server

  private boolean codeblocksNeedsToReloadProperties;

  /**
   * Creates a new YaFormEditor.
   *
   * @param projectEditor  the project editor that contains this file editor
   * @param formNode the YoungAndroidFormNode associated with this YaFormEditor
   */
  YaFormEditor(ProjectEditor projectEditor, YoungAndroidFormNode formNode) {
    super(projectEditor, formNode);

    this.formNode = formNode;

    // Get references to the source structure explorer and the assets list.
    sourceStructureExplorer =
        SourceStructureBox.getSourceStructureBox().getSourceStructureExplorer();

    // Create UI elements for the designer panels.
    nonVisibleComponentsPanel = new SimpleNonVisibleComponentsPanel();
    visibleComponentsPanel = new SimpleVisibleComponentsPanel(this, nonVisibleComponentsPanel);
    DockPanel componentsPanel = new DockPanel();
    componentsPanel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
    componentsPanel.add(visibleComponentsPanel, DockPanel.NORTH);
    componentsPanel.add(nonVisibleComponentsPanel, DockPanel.SOUTH);
    componentsPanel.setSize("100%", "100%");

    // Create palettePanel, which will be used as the content of the PaletteBox.
    palettePanel = new YoungAndroidPalettePanel(this);
    palettePanel.loadComponents(new DropTargetProvider() {
      @Override
      public DropTarget[] getDropTargets() {
        // TODO(markf): Figure out a good way to memorize the targets or refactor things so that
        // getDropTargets() doesn't get called for each component.
        // NOTE: These targets must be specified in depth-first order.
        List<DropTarget> dropTargets = form.getDropTargetsWithin();
        dropTargets.add(visibleComponentsPanel);
        dropTargets.add(nonVisibleComponentsPanel);
        return dropTargets.toArray(new DropTarget[dropTargets.size()]);
      }
    });
    palettePanel.setSize("100%", "100%");

    // Create designProperties, which will be used as the content of the PropertiesBox.
    designProperties = new PropertiesPanel();
    designProperties.setSize("100%", "100%");

    initWidget(componentsPanel);
    setSize("100%", "100%");
  }

  // FileEditor methods

  @Override
  public void loadFile(final Command afterFileLoaded) {
    OdeAsyncCallback<String> callback = new OdeAsyncCallback<String>(MESSAGES.loadError()) {
      @Override
      public void onSuccess(String result) {
        final FileContentHolder fileContentHolder = new FileContentHolder(result);
        upgradeFile(fileContentHolder, new Command() {
          @Override
          public void execute() {
            onFileLoaded(fileContentHolder.getFileContent());
            if (afterFileLoaded != null) {
              afterFileLoaded.execute();
            }
          }
        });
      }
    };
    Ode.getInstance().getProjectService().load(getProjectId(), getFileId(), callback);
  }

  @Override
  public String getTabText() {
    return getFormName();
  }

  @Override
  public void onShow() {
    // When this editor is shown, update the "current" editor.
    Ode.getInstance().setCurrentYaFormEditor(this);

    loadDesigner();

    CodeblocksManager.getCodeblocksManager().loadPropertiesAndBlocks(formNode, null);

    super.onShow();
  }

  @Override
  public void onHide() {
    CodeblocksManager.getCodeblocksManager().saveCodeblocksSource(null);

    unloadDesigner();

    // When an editor is detached, clear the "current" editor.
    Ode.getInstance().setCurrentYaFormEditor(null);

    super.onHide();
  }

  @Override
  public String getRawFileContent() {
    String encodedProperties = '{' + encodeFormAsJsonString() + '}';
    JSONObject propertiesObject = JSON_PARSER.parse(encodedProperties).asObject();
    return YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject);
  }

  @Override
  public void onSave() {
    CodeblocksManager codeblocksManager = CodeblocksManager.getCodeblocksManager();
    if (codeblocksNeedsToReloadProperties && codeblocksManager.getCurrentFormNode() == formNode) {
      // Tell codeblocks to reload the properties from the ODE server.
      codeblocksManager.reloadProperties(new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          codeblocksNeedsToReloadProperties = false;
        }

        @Override
        public void onFailure(Throwable caught) {
          // The error has already been reported in CodeblocksManager.
        }
      });
    }
  }

  // SimpleEditor methods

  @Override
  public boolean isLoadComplete() {
    return loadComplete;
  }

  @Override
  public Map<String, MockComponent> getComponents() {
    Map<String, MockComponent> map = new HashMap<String, MockComponent>();
    if (loadComplete) {
      populateComponentsMap(form, map);
    }
    return map;
  }

  @Override
  public List<String> getComponentNames() {
    return new ArrayList<String>(getComponents().keySet());
  }

  @Override
  public SimplePalettePanel getComponentPalettePanel() {
    return palettePanel;
  }

  @Override
  public SimpleNonVisibleComponentsPanel getNonVisibleComponentsPanel() {
    return nonVisibleComponentsPanel;
  }

  @Override
  public boolean isScreen1() {
    return isScreen1(getFormName());
  }

  // FormChangeListener implementation

  @Override
  public void onComponentPropertyChanged(MockComponent component,
      String propertyName, String propertyValue) {
    if (loadComplete) {
      // If the property isn't actually persisted to the .scm file, we don't need to do anything.
      if (component.isPropertyPersisted(propertyName)) {
        Ode.getInstance().getEditorManager().scheduleAutoSave(this);
        if (!codeblocksNeedsToReloadProperties) {
          syncPropertyChangeToCodeblocks(component.getName(), component.getType(),
              propertyName, propertyValue);
        }
      }
    } else {
      OdeLog.elog("onComponentPropertyChanged called when loadComplete is false");
    }
  }

  @Override
  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (loadComplete) {
      if (permanentlyDeleted) {
        onFormStructureChange();
      }
    } else {
      OdeLog.elog("onComponentRemoved called when loadComplete is false");
    }
  }

  @Override
  public void onComponentAdded(MockComponent component) {
    if (loadComplete) {
      onFormStructureChange();
    } else {
      OdeLog.elog("onComponentAdded called when loadComplete is false");
    }
  }

  @Override
  public void onComponentRenamed(MockComponent component, String oldName) {
    if (loadComplete) {
      onFormStructureChange();
    } else {
      OdeLog.elog("onComponentRenamed called when loadComplete is false");
    }
  }

  @Override
  public void onComponentSelectionChange(MockComponent component, boolean selected) {
    if (loadComplete) {
      if (selected) {
        // Select the item in the source structure explorer.
        sourceStructureExplorer.selectItem(component.getSourceStructureExplorerItem());

        // Show the component properties in the properties panel.
        updatePropertiesPanel(component);
      } else {
        // Unselect the item in the source structure explorer.
        sourceStructureExplorer.unselectItem(component.getSourceStructureExplorerItem());
      }
    } else {
      OdeLog.elog("onComponentSelectionChange called when loadComplete is false");
    }
  }

  // other public methods

  /**
   * Returns the form associated with this YaFormEditor.
   *
   * @return a MockForm
   */
  public MockForm getForm() {
    return form;
  }

  /**
   * Returns the form node associated with this YaFormEditor.
   *
   * @return a YoungAndroidFormNode
   */
  public YoungAndroidFormNode getFormNode() {
    return formNode;
  }

  /**
   * Returns the form name associated with this YaFormEditor.
   *
   * @return the form name
   */
  public String getFormName() {
    return StorageUtil.trimOffExtension(StorageUtil.basename(getFileId()));
  }

  /**
   * Returns true if the given formName is Screen1, false otherwise.
   */
  public static boolean isScreen1(String formName) {
    return formName.equals("Screen1");
  }

  // private methods

  /*
   * Upgrades the given file content, saves the upgraded content back to the
   * ODE server, and calls the afterUpgradeComplete command after the save
   * operation succeeds.
   *
   * If no upgrade is necessary, the afterSavingFiles command is called
   * immediately.
   *
   * @param fileContentHolder  holds the file content
   * @param afterUpgradeComplete  optional command to be executed after the
   *                              file has upgraded and saved back to the ODE
   *                              server
   */
  private void upgradeFile(FileContentHolder fileContentHolder,
      final Command afterUpgradeComplete) {
    JSONObject propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(
        fileContentHolder.getFileContent(), JSON_PARSER);
    if (YoungAndroidFormUpgrader.upgradeSourceProperties(propertiesObject.getProperties())) {
      String upgradedContent = YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject);
      fileContentHolder.setFileContent(upgradedContent);

      Ode.getInstance().getProjectService().save(getProjectId(), getFileId(), upgradedContent,
          new OdeAsyncCallback<Long>(MESSAGES.saveError()) {
            @Override
            public void onSuccess(Long result) {
              // Execute the afterUpgradeComplete command if one was given.
              if (afterUpgradeComplete != null) {
                afterUpgradeComplete.execute();
              }
            }
          });
    } else {
      // No upgrade was necessary.
      // Execute the afterUpgradeComplete command if one was given.
      if (afterUpgradeComplete != null) {
        afterUpgradeComplete.execute();
      }
    }
  }

  private void onFileLoaded(String content) {
    JSONObject propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(
        content, JSON_PARSER);
    form = createMockForm(propertiesObject.getProperties().get("Properties").asObject());

    // Initialize the nonVisibleComponentsPanel and visibleComponentsPanel.
    nonVisibleComponentsPanel.setForm(form);
    visibleComponentsPanel.setForm(form);
    form.select();

    // Set loadCompleted to true.
    // From now on, all change events will be taken seriously.
    loadComplete = true;
  }

  /*
   * Parses the JSON properties and creates the form and its component structure.
   */
  private MockForm createMockForm(JSONObject propertiesObject) {
    return (MockForm) createMockComponent(propertiesObject, null);
  }

  /*
   * Parses the JSON properties and creates the component structure. This method is called
   * recursively for nested components. For the initial invocation parent shall be null.
   */
  private MockComponent createMockComponent(JSONObject propertiesObject, MockContainer parent) {
    Map<String, JSONValue> properties = propertiesObject.getProperties();

    // Component name and type
    String componentType = properties.get("$Type").asString().getString();

    // Instantiate a mock component for the visual designer
    MockComponent mockComponent;
    if (componentType.equals(MockForm.TYPE)) {
      Preconditions.checkArgument(parent == null);

      // Instantiate new root component
      mockComponent = new MockForm(this);
    } else {
      mockComponent = SimpleComponentDescriptor.createMockComponent(componentType, this);

      // Add the component to its parent component (and if it is non-visible, add it to the
      // nonVisibleComponent panel).
      parent.addComponent(mockComponent);
      if (!mockComponent.isVisibleComponent()) {
        nonVisibleComponentsPanel.addComponent(mockComponent);
      }
    }

    // Set the name of the component (on instantiation components are assigned a generated name)
    String componentName = properties.get("$Name").asString().getString();
    mockComponent.changeProperty("Name", componentName);

    // Set component properties
    for (String name : properties.keySet()) {
      if (name.charAt(0) != '$') { // Ignore special properties (name, type and nested components)
        mockComponent.changeProperty(name, properties.get(name).asString().getString());
      }
    }

    // Add nested components
    if (properties.containsKey("$Components")) {
      for (JSONValue nestedComponent : properties.get("$Components").asArray().getElements()) {
        createMockComponent(nestedComponent.asObject(), (MockContainer) mockComponent);
      }
    }

    return mockComponent;
  }

  /*
   * Updates the the whole designer: form, palette, source structure explorer,
   * assets list, and properties panel.
   */
  private void loadDesigner() {
    form.refresh();
    MockComponent selectedComponent = form.getSelectedComponent();

    // Set the palette box's content.
    PaletteBox.getPaletteBox().setContent(palettePanel);

    // Update the source structure explorer with the tree of this form's components.
    sourceStructureExplorer.updateTree(form.buildComponentsTree(),
        selectedComponent.getSourceStructureExplorerItem());

    // Set the properties box's content.
    PropertiesBox.getPropertiesBox().setContent(designProperties);
    updatePropertiesPanel(selectedComponent);

    // Listen to changes on the form.
    form.addFormChangeListener(this);
  }

  /*
   * Show the given component's properties in the properties panel.
   */
  private void updatePropertiesPanel(MockComponent component) {
    designProperties.setPropertiesCaption(component.getVisibleTypeName());
    designProperties.setProperties(component.getProperties());
  }

  /**
   * When a component property is changed, send the information over to
   * codeblocks so that it can, in turn, update the property on the phone
   * if it's connected.
   *
   * @param componentName the name of the component
   * @param componentType the type of the component
   * @param propertyName the name of the changed property
   * @param propertyValue the new value of the property
   */
  private void syncPropertyChangeToCodeblocks(final String componentName, String componentType,
      final String propertyName, String propertyValue) {

    CodeblocksManager codeblocksManager = CodeblocksManager.getCodeblocksManager();
    if (codeblocksManager.getCurrentFormNode() == formNode) {
      codeblocksManager.syncProperty(componentName, componentType,
          propertyName, propertyValue, null);
    }
  }

  private void onFormStructureChange() {
    Ode.getInstance().getEditorManager().scheduleAutoSave(this);

    // Update source structure panel
    sourceStructureExplorer.updateTree(form.buildComponentsTree(),
        form.getSelectedComponent().getSourceStructureExplorerItem());

    // Set a flag so that properties will be synced to codeblocks.
    codeblocksNeedsToReloadProperties = true;
  }

  private void populateComponentsMap(MockComponent component, Map<String, MockComponent> map) {
    EditableProperties properties = component.getProperties();
    map.put(properties.getPropertyValue("Name"), component);
    List<MockComponent> children = component.getChildren();
    for (MockComponent child : children) {
      populateComponentsMap(child, map);
    }
  }

  /*
   * Encodes the form's properties as a JSON encoded string.
   */
  private String encodeFormAsJsonString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\"YaVersion\":\"").append(YaVersion.YOUNG_ANDROID_VERSION).append("\",");
    sb.append("\"Source\":\"Form\",");
    sb.append("\"Properties\":");
    encodeComponentProperties(form, sb);
    return sb.toString();
  }

  /*
   * Encodes a component and its properties into a JSON encoded string.
   */
  private void encodeComponentProperties(MockComponent component, StringBuilder sb) {
    // The component encoding starts with component name and type
    String componentType = component.getType();
    EditableProperties properties = component.getProperties();
    sb.append("{\"$Name\":\"");
    sb.append(properties.getPropertyValue("Name"));
    sb.append("\",\"$Type\":\"");
    sb.append(componentType);
    sb.append("\",\"$Version\":\"");
    sb.append(COMPONENT_DATABASE.getComponentVersion(componentType));
    sb.append('"');

    // Next the actual component properties
    //
    // NOTE: It is important that these be encoded before any children components.
    String propertiesString = properties.encodeAsPairs();
    if (propertiesString.length() > 0) {
      sb.append(',');
      sb.append(propertiesString);
    }

    // Finally any children of the component
    List<MockComponent> children = component.getChildren();
    if (!children.isEmpty()) {
      sb.append(",\"$Components\":[");
      String separator = "";
      for (MockComponent child : children) {
        sb.append(separator);
        encodeComponentProperties(child, sb);
        separator = ",";
      }
      sb.append(']');
    }

    sb.append('}');
  }

  /*
   * Clears the palette, source structure explorer, and properties panel.
   */
  private void unloadDesigner() {
    // Stop listening to changes on the form.
    form.removeFormChangeListener(this);

    // Clear the palette box.
    PaletteBox.getPaletteBox().clear();

    // Clear source structure explorer.
    sourceStructureExplorer.clearTree();

    // Clear the properties box.
    PropertiesBox.getPropertiesBox().clear();
  }
}
