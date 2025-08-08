// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.designer;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.editor.simple.components.MockComponent.PROPERTY_NAME_NAME;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.SimpleVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.simple.components.MockDesignerRoot;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.components.MockVisibleComponent;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertiesPanel;
import com.google.appinventor.client.widgets.properties.PropertyChangeListener;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.SourceNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.simple.ComponentDatabaseChangeListener;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * DesignerEditor is an ancestor of all designer editors in App Inventor.
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class DesignerEditor<S extends SourceNode, T extends MockDesignerRoot,
    U extends SimplePalettePanel, V extends ComponentDatabaseInterface,
    W extends SimpleVisibleComponentsPanel<T>>
    extends SimpleEditor implements DesignerChangeListener, ComponentDatabaseChangeListener,
        PropertyChangeListener, KeyDownHandler {

  protected static class FileContentHolder {
    private String content;

    FileContentHolder(String content) {
      this.content = content;
    }

    public void setFileContent(String content) {
      this.content = content;
    }

    public String getFileContent() {
      return content;
    }
  }

  public static final String EDITOR_TYPE = DesignerEditor.class.getSimpleName();

  private static final Logger LOG = Logger.getLogger(DesignerEditor.class.getName());

  protected final List<ComponentDatabaseChangeListener> componentDatabaseChangeListeners
      = new ArrayList<>();

  protected final S sourceNode;

  protected final V componentDatabase;

  // References to other panels that we need to control.
  private final SourceStructureExplorer sourceStructureExplorer;

  private final PropertiesPanel designProperties;

  private EditableProperties selectedProperties = null;

  // Flag to indicate when loading the file is completed. This is needed because building the mock
  // form from the file properties fires events that need to be ignored, otherwise the file will be
  // marked as being modified.
  private boolean loadComplete = false;

  protected T root;

  // Panels that are used as the content of the palette and properties boxes.
  protected U palettePanel;

  // UI elements
  protected final W visibleComponentsPanel;
  protected final SimpleNonVisibleComponentsPanel<T> nonVisibleComponentsPanel;

  public DesignerEditor(ProjectEditor projectEditor, S sourceNode,
                        V componentDatabase,
                        W visibleComponentsPanel) {
    super(projectEditor, sourceNode);

    this.sourceNode = sourceNode;
    this.componentDatabase = componentDatabase;

    // Get reference to the source structure explorer
    sourceStructureExplorer =
        SourceStructureBox.getSourceStructureBox().getSourceStructureExplorer();

    // Create UI elements for the designer panels.
    this.visibleComponentsPanel = visibleComponentsPanel;
    nonVisibleComponentsPanel = visibleComponentsPanel.getNonVisibleComponentsPanel();
    componentDatabaseChangeListeners.add(nonVisibleComponentsPanel);
    componentDatabaseChangeListeners.add(visibleComponentsPanel);
    DockPanel componentsPanel = new DockPanel();
    componentsPanel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
    componentsPanel.add(visibleComponentsPanel, DockPanel.NORTH);
    componentsPanel.add(nonVisibleComponentsPanel, DockPanel.SOUTH);
    componentsPanel.setSize("100%", "100%");

    // Create designProperties, which will be used as the content of the PropertiesBox.
    designProperties = new PropertiesPanel();
    designProperties.setSize("100%", "100%");

    root = null;

    initWidget(componentsPanel);
    setSize("100%", "100%");
    registerNativeListeners();
    RootPanel.get().addDomHandler(this, KeyDownEvent.getType());
  }

  @Override
  public void onShow() {
    super.onShow();
    loadDesigner();
    Tracking.trackEvent(Tracking.EDITOR_EVENT, Tracking.EDITOR_ACTION_SHOW_DESIGNER);
  }

  public abstract String getJson();
  protected abstract T newRootObject();

  protected abstract void upgradeFile(FileContentHolder fileContentHolder,
                                      final Command afterUpgradeCompleted);

  public T getRoot() {
    return root;
  }

  public String getEntityName() {
    if (root != null) {
      return root.getName();
    } else {
      return StorageUtil.trimOffExtension(StorageUtil.basename(sourceNode.getFileId()));
    }
  }

  public PropertiesPanel getPropertiesPanel() {
    return designProperties;
  }

  public EditableProperties getProperties() {
    return selectedProperties;
  }

  public String getComponentInstanceTypeName(String instanceName) {
    return getComponents().get(instanceName).getType();
  }

  protected void onStructureChange() {
    Ode.getInstance().getEditorManager().scheduleAutoSave(this);

    // Update source structure panel
    sourceStructureExplorer.updateTree(root.buildComponentsTree(),
        root.getLastSelectedComponent().getSourceStructureExplorerItem());
  }

  protected void loadDesigner() {
    // Update the source structure explorer with the tree of this form's components.
    // TODO: SMRL Refactor links to source structure
    sourceStructureExplorer.updateTree(root.buildComponentsTree(),
        root.getLastSelectedComponent().getSourceStructureExplorerItem());
    SourceStructureBox.getSourceStructureBox().setVisible(true);

    // Show the assets box.
    AssetListBox assetListBox = AssetListBox.getAssetListBox();
    assetListBox.setVisible(true);

    // Set the properties box's content.
    PropertiesBox propertiesBox = PropertiesBox.getPropertiesBox();
    propertiesBox.setContent(designProperties);
    updatePropertiesPanel(root.getSelectedComponents(), true);
    propertiesBox.setVisible(true);

    Ode.getInstance().showComponentDesigner();
  }

  /**
   * Clears the palette, source structure explorer, and properties panel.
   */
  protected void unloadDesigner() {
    // The form can still potentially change if the blocks editor is displayed
    // so don't remove the formChangeListener.

    // Clear the palette box.
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.clear();

    // Clear and hide the source structure explorer.
    sourceStructureExplorer.clearTree();
    SourceStructureBox.getSourceStructureBox().setVisible(false);

    // Clear and hide the properties box.
    PropertiesBox propertiesBox = PropertiesBox.getPropertiesBox();
    propertiesBox.clear();
    propertiesBox.setVisible(false);

    Ode.getInstance().hideComponentDesigner();
  }

  // ComponentDatabaseChangeListener implementation
  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {
    componentDatabase.removeComponentDatabaseListener(this);
    for (ComponentDatabaseChangeListener listener : componentDatabaseChangeListeners) {
      listener.onComponentTypeAdded(componentTypes);
    }
    //Update the Properties Panel
    updatePropertiesPanel(root.getSelectedComponents(), true);
    SourceStructureBox.getSourceStructureBox().show(root);
  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    boolean result = true;
    for (ComponentDatabaseChangeListener listener : componentDatabaseChangeListeners) {
      result = result && listener.beforeComponentTypeRemoved(componentTypes);
    }
    return result;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {
    componentDatabase.removeComponentDatabaseListener(this);
    for (ComponentDatabaseChangeListener cdbChangeListener : componentDatabaseChangeListeners) {
      cdbChangeListener.onComponentTypeRemoved(componentTypes);
    }
  }

  @Override
  public void onResetDatabase() {
    componentDatabase.removeComponentDatabaseListener(this);
    for (ComponentDatabaseChangeListener listener : componentDatabaseChangeListeners) {
      listener.onResetDatabase();
    }
  }

  // DesignerChangeListener implementation
  @Override
  public void onComponentPropertyChanged(MockComponent component, String propertyName, String propertyValue) {
    if (loadComplete) {
      // If the property isn't actually persisted to the .scm file, we don't need to do anything.
      if (component.isPropertyPersisted(propertyName)) {
        Ode.getInstance().getEditorManager().scheduleAutoSave(this);
      }
    } else {
      LOG.severe("onComponentPropertyChanged called when loadComplete is false");
    }
  }

  @Override
  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (loadComplete) {
      if (permanentlyDeleted) {
        onStructureChange();
      }
    } else {
      LOG.severe("onComponentRemoved called when loadComplete is false");
    }
  }

  @Override
  public void onComponentAdded(MockComponent component) {
    if (loadComplete) {
      selectedProperties = component.getProperties();
      onStructureChange();
    } else {
      LOG.severe("onComponentAdded called when loadComplete is false");
    }
  }

  @Override
  public void onComponentRenamed(MockComponent component, String oldName) {
    if (loadComplete) {
      onStructureChange();
      updatePropertiesPanel(root.getSelectedComponents(), true);
    } else {
      LOG.severe("onComponentRenamed called when loadComplete is false");
    }
  }

  @Override
  public void onComponentSelectionChange(MockComponent component, boolean selected) {
    if (loadComplete) {
      // TODO: SMRL Not sure this class should keep a pointer to source structure
      // Select the item in the source structure explorer.
      sourceStructureExplorer.selectItem(component.getSourceStructureExplorerItem());
      SourceStructureBox.getSourceStructureBox().show(root);

      // Show the component properties in the properties panel.
      updatePropertiesPanel(root.getSelectedComponents(), selected);
    } else {
      LOG.severe("onComponentSelectionChange called when loadComplete is false");
    }

  }

  // SimpleEditor implementation
  @Override
  public boolean isLoadComplete() {
    return loadComplete;
  }

  @Override
  public Map<String, MockComponent> getComponents() {
    Map<String, MockComponent> map = Maps.newHashMap();
    if (loadComplete) {
      populateComponentsMap(root.asMockComponent(), map);
    }
    return map;
  }

  @Override
  public List<String> getComponentNames() {
    return new ArrayList<>(getComponents().keySet());
  }

  @Override
  public SimplePalettePanel getComponentPalettePanel() {
    return palettePanel;
  }

  @Override
  public SimpleNonVisibleComponentsPanel<T> getNonVisibleComponentsPanel() {
    return nonVisibleComponentsPanel;
  }

  @Override
  public W getVisibleComponentsPanel() {
    return visibleComponentsPanel;
  }

  @Override
  public boolean isScreen1() {
    return false;
  }

  @Override
  public void refreshPropertiesPanel() {
    designProperties.clear();
    if (selectedProperties != null) {
      designProperties.setProperties(selectedProperties);
    }
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String propertyValue) {
    for (MockComponent selectedComponent : root.getSelectedComponents()) {
      selectedComponent.changeProperty(propertyName, propertyValue);
      // Ensure the editor matches (multiselect)
      selectedComponent.getProperties().getExistingProperty(propertyName).getEditor().refresh();
    }
  }

  // FileEditor implementation
  @Override
  public void loadFile(final Command afterFileLoaded) {
    final long projectId = getProjectId();
    final String fileId = getFileId();
    OdeAsyncCallback<ChecksumedLoadFile> callback = new OdeAsyncCallback<ChecksumedLoadFile>(MESSAGES.loadError()) {
      @Override
      public void onSuccess(ChecksumedLoadFile result) {
        String contents;
        try {
          contents = result.getContent();
        } catch (ChecksumedFileException e) {
          this.onFailure(e);
          return;
        }
        final FileContentHolder fileContentHolder = new FileContentHolder(contents);
        upgradeFile(fileContentHolder, new Command() {
          @Override
          public void execute() {
            try {
              onFileLoaded(fileContentHolder.getFileContent());
            } catch (IllegalArgumentException e) {
              return;
            }
            if (afterFileLoaded != null) {
              afterFileLoaded.execute();
            }
          }
        });
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof ChecksumedFileException) {
          Ode.getInstance().recordCorruptProject(projectId, fileId, caught.getMessage());
        }
        super.onFailure(caught);
      }
    };
    Ode.getInstance().getProjectService().load2(projectId, fileId, callback);
  }

  @Override
  public String getTabText() {
    return sourceNode.getEntityName();
  }

  @Override
  public void onSave() {
  }

  @Override
  public final String getEditorType() {
    return EDITOR_TYPE;
  }

  public BlocksEditor<?, ?> getBlocksEditor() {
    return ((YaProjectEditor) projectEditor).getBlocksFileEditor(sourceNode.getEntityName());
  }

  // private implementation
  /*
   * Show the given component's properties in the properties panel.
   */
  protected void updatePropertiesPanel(List<MockComponent> components, boolean selected) {
    if (components == null || components.size() == 0) {
      throw new IllegalArgumentException("components must be a list of at least 1");
    }
    if (selectedProperties != null) {
      selectedProperties.removePropertyChangeListener(this);
    }
    if (components.size() == 1) {
      selectedProperties = components.get(0).getProperties();
    } else {
      EditableProperties newProperties = new EditableProperties(true);
      Map<String, EditableProperty> propertyMaps = new HashMap<>();
      boolean first = true;
      for (MockComponent component : components) {
        Set<String> properties = new HashSet<>();
        for (EditableProperty property : component.getProperties()) {
          String propertyName = property.getName();
          // Ignore UUID and NAME properties (can't be edited and always unique)
          if ("Uuid".equals(propertyName) || "Name".equals(propertyName)) {
            continue;
          }
          if (first) {
            propertyMaps.put(propertyName + ":" + property.getType(), property);
          } else {
            properties.add(propertyName + ":" + property.getType());
          }
        }
        if (properties.size() > 0) {
          propertyMaps.keySet().retainAll(properties);
        }
        first = false;
      }
      for (EditableProperty property : propertyMaps.values()) {
        String name = property.getName();
        newProperties.addProperty(
            name,
            property.getDefaultValue(),
            property.getCaption(),
            property.getCategory(),
            property.getDescription(),
            PropertiesUtil.createPropertyEditor(property.getEditorType(),
                property.getDefaultValue(), this, property.getEditorArgs()),
            property.getType(),
            property.getEditorType(),
            property.getEditorArgs()
        );

        // Determine if all components have the same value and apply it
        String sharedValue = components.get(0).getPropertyValue(name);
        boolean collision = false;
        for (MockComponent component : components) {
          String propValue = component.getPropertyValue(name);
          if (!sharedValue.equals(propValue)) {
            sharedValue = "";
            collision = true;
            break;
          }
        }
        newProperties.getProperty(name).getEditor().setMultipleValues(collision);
        newProperties.getProperty(name).getEditor().setMultiselectMode(true);
        newProperties.getProperty(name).setValue(sharedValue);
      }
      selectedProperties = newProperties;
    }
    if (selected) {
      selectedProperties.addPropertyChangeListener(this);
    }
    Iterator<EditableProperty> iterator = selectedProperties.iterator();
    while (iterator.hasNext()) {
      EditableProperty property = iterator.next();
      if (property.getName().equals("SlotEditorUsed")) {
        property.getEditor().refresh();
      }
    }
    designProperties.setProperties(selectedProperties);
    if (components.size() > 1) {
      // TODO: Localize
      designProperties.setPropertiesCaption(components.size() + " components selected");
    } else {
      // need to update the caption after the setProperties call, since
      // setProperties clears the caption!
      designProperties.setPropertiesCaption(components.get(0).getName());
    }
  }

  private void populateComponentsMap(MockComponent component, Map<String, MockComponent> map) {
    EditableProperties properties = component.getProperties();
    map.put(properties.getPropertyValue("Name"), component);
    List<MockComponent> children = component.getChildren();
    for (MockComponent child : children) {
      populateComponentsMap(child, map);
    }
  }

  protected void onFileLoaded(String content) {
    // Set loadCompleted to true.
    // From now on, all change events will be taken seriously.
    loadComplete = true;
  }

  public V getComponentDatabase() {
    return componentDatabase;
  }

  protected MockComponent createMockComponent(JSONObject properties, MockContainer container, String rootType) {
    return createMockComponent(properties, container, rootType, null);
  }

  /*
   * Parses the JSON properties and creates the component structure. This method is called
   * recursively for nested components. For the initial invocation parent shall be null.
   */
  protected MockComponent createMockComponent(JSONObject propertiesObject, MockContainer parent,
      String rootType, Map<String, String> substitution) {
    Map<String, JSONValue> properties = propertiesObject.getProperties();

    // Component name and type
    String componentType = properties.get("$Type").asString().getString();

    // Set the name of the component (on instantiation components are assigned a generated name)
    boolean shouldRename = false;
    String componentName = properties.get("$Name").asString().getString();

    if (componentType.equals("Alexa")) {
      componentName = componentName.replaceAll("_", ".");
    }

    // Instantiate a mock component for the visual designer
    MockComponent mockComponent;
    if (componentType.equals(rootType)) {
      Preconditions.checkArgument(parent == null);

      // Instantiate new root component
      mockComponent = (MockComponent) newRootObject();

      //This is for old project which doesn't have the AppName property
      if ("Form".equals(rootType) && !properties.containsKey("AppName")) {
        String fileId = getFileId();
        String projectName = fileId.split("/")[3];
        mockComponent.changeProperty("AppName", projectName);
      }

      if ("Form".equals(rootType)) {
        // A bug in an early version of multiselect resulted in Form gaining Row and Column
        // properties, which are reserved for visible components that can appear in TableArrangements.
        // Form doesn't have these properties, so we need to clean up the properties. The remove
        // call here is idempotent--if the property is present, it is removed. If not present, the
        // map remains unchanged.
        properties.remove(MockVisibleComponent.PROPERTY_NAME_ROW);
        properties.remove(MockVisibleComponent.PROPERTY_NAME_COLUMN);
      }
    } else {
      mockComponent = palettePanel.createMockComponent(componentType, componentDatabase.getComponentType(componentType));

      // Ensure unique name on paste
      if (substitution != null) {
        List<String> names = getComponentNames();
        if (names.contains(componentName)) {
          String oldName = componentName;
          componentName = gensymName(componentType, componentName);
          substitution.put(oldName, componentName);
          shouldRename = true;
        } else if (!mockComponent.getPropertyValue(PROPERTY_NAME_NAME).equals(componentName)) {
          // If the SCD gensyms a name, but it is free, we rename it back.
          shouldRename = true;
        }
        properties.remove(MockComponent.PROPERTY_NAME_UUID);
      }

      // Add the component to its parent component (and if it is non-visible, add it to the
      // nonVisibleComponent panel).
      parent.addComponent(mockComponent);
      if (!mockComponent.isVisibleComponent()) {
        nonVisibleComponentsPanel.addComponent(mockComponent);
      }
    }

    if (shouldRename) {
      mockComponent.rename(componentName);
    } else {
      mockComponent.changeProperty(PROPERTY_NAME_NAME, componentName);
    }

    // Set component properties
    for (String name : properties.keySet()) {
      if (name.charAt(0) != '$') { // Ignore special properties (name, type and nested components)
        mockComponent.changeProperty(name, properties.get(name).asString().getString());
      }
    }

    // Add component type to the blocks editor
    BlocksEditor<?, ?> blockEditor = (BlocksEditor<?, ?>) projectEditor.getFileEditor(sourceNode.getEntityName(), BlocksEditor.EDITOR_TYPE);
    if (blockEditor != null) {
      blockEditor.addComponent(mockComponent.getType(), mockComponent.getName(),
          mockComponent.getUuid());
    }

    // Add nested components
    if (properties.containsKey("$Components")) {
      for (JSONValue nestedComponent : properties.get("$Components").asArray().getElements()) {
        createMockComponent(nestedComponent.asObject(), (MockContainer) mockComponent, rootType, substitution);
      }
    }

    return mockComponent;
  }

  @Override
  public void onKeyDown(KeyDownEvent event) {
    if (!isActiveEditor()) {
      return;  // Not the active editor
    }
    if (event.isAltKeyDown()) {
      List<MockComponent> allComponents = new ArrayList<>(getComponents().values());
      MockComponent selectedComponent = root.getLastSelectedComponent();
      int index = root.getChildren().indexOf(selectedComponent);

      if (selectedComponent.isVisibleComponent()) {
        switch (event.getNativeKeyCode()) {
          case KeyCodes.KEY_DOWN:
            if (index < 0) {
              MockContainer container = selectedComponent.getContainer();
              List<MockComponent> containerComponents = container.getChildren();
              int indexC = containerComponents.indexOf(selectedComponent);
              int indexOfContainer = allComponents.indexOf(container);
              selectedComponent.getContainer().removeComponent(selectedComponent, false);

              if (indexC == containerComponents.size() && container.getContainer().willAcceptComponentType(selectedComponent.getType())) {
                container.getContainer().addVisibleComponent(selectedComponent, indexOfContainer);
              } else {
                container.addVisibleComponent(selectedComponent, indexC + 1);
              }
            } else {
              index++;
              root.removeComponent(selectedComponent, false);
              MockComponent nextComponent = allComponents.get(index + 1);
              int nextComponentindex = root.getChildren().indexOf(nextComponent);
              if(nextComponent instanceof MockContainer && ((MockContainer) nextComponent).willAcceptComponentType(selectedComponent.getType())) {
                ((MockContainer)nextComponent).addVisibleComponent(selectedComponent, 0);
              } else if (nextComponentindex < 0 && ((MockContainer) nextComponent.getContainer()).willAcceptComponentType(selectedComponent.getType())) {
                nextComponent.getContainer().addVisibleComponent(selectedComponent, 0);
              } else {
                root.addVisibleComponent(selectedComponent, index);
              }
            }
            break;

          case KeyCodes.KEY_UP:
            if (index < 0) {
              MockContainer container = selectedComponent.getContainer();
              List<MockComponent> containerComponents = container.getChildren();
              int indexC = containerComponents.indexOf(selectedComponent);
              int indexOfContainer = allComponents.indexOf(container);
              selectedComponent.getContainer().removeComponent(selectedComponent, false);
              if (indexC == 0 && container.getContainer().willAcceptComponentType(selectedComponent.getType())) {
                container.getContainer().addVisibleComponent(selectedComponent, indexOfContainer - 1);
              } else {
                container.addVisibleComponent(selectedComponent, indexC - 1);
              }
            } else {
              index++;
              root.removeComponent(selectedComponent, false);
              MockComponent prevComponent = allComponents.get(index - 1);
              int prevComponentIndex = root.getChildren().indexOf(prevComponent);
              if(prevComponent instanceof MockContainer && ((MockContainer) prevComponent).willAcceptComponentType(selectedComponent.getType())) {
                ((MockContainer)prevComponent).addVisibleComponent(selectedComponent, -1);
              } else if (prevComponentIndex < 0 && ((MockContainer) prevComponent).willAcceptComponentType(selectedComponent.getType())) {
                prevComponent.getContainer().addVisibleComponent(selectedComponent, -1);
              } else {
                root.addVisibleComponent(selectedComponent, index - 2);
              }
            }
            break;

          default:
            break;
        }
      }
    } else if (event.getNativeKeyCode() == KeyCodes.KEY_T && !palettePanel.isTextboxFocused()) {
      SourceStructureBox.getSourceStructureBox().getSourceStructureExplorer().getTree().setFocus(true);
    } else if (event.getNativeKeyCode() == KeyCodes.KEY_P && !palettePanel.isTextboxFocused()) {
      PropertiesBox.getPropertiesBox().getElement().getElementsByTagName("a").getItem(0).focus();
    } else if (event.getNativeKeyCode() == KeyCodes.KEY_M && !palettePanel.isTextboxFocused()) {
      AssetListBox.getAssetListBox().getAssetList().getTree().setFocus(true);
    }
  }

  /*
   * Encodes a component and its properties into a JSON encoded string.
   */
  protected void encodeComponentProperties(MockComponent component, StringBuilder sb, boolean forYail) {
    // The component encoding starts with component name and type
    String componentType = component.getType();
    EditableProperties properties = component.getProperties();
    sb.append("{\"$Name\":\"");
    sb.append(properties.getPropertyValue("Name"));
    sb.append("\",\"$Type\":\"");
    sb.append(componentType);
    sb.append("\",\"$Version\":\"");
    sb.append(componentDatabase.getComponentVersion(componentType));
    sb.append('"');

    // Next the actual component properties
    //
    // NOTE: It is important that these be encoded before any children components.
    String propertiesString = properties.encodeAsPairs(forYail);
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
        encodeComponentProperties(child, sb, forYail);
        separator = ",";
      }
      sb.append(']');
    }

    sb.append('}');
  }

  private String getSelectedComponentJson() {
    T root = getRoot();
    StringBuilder sb = new StringBuilder();
    String sep = "";
    sb.append("[");
    if (root.getSelectedComponents().size() == 1
        && root.getSelectedComponents().get(0) instanceof MockForm) {
      encodeComponentProperties(root, sb, false);
    } else {
      for (MockComponent component : root.getSelectedComponents()) {
        if (component instanceof MockForm) {
          continue;
        }
        sb.append(sep);
        encodeComponentProperties(component, sb, false);
        sep = ",";
      }
    }
    sb.append("]");
    if (sb.length() == 2) {
      return "";  // Only had the designer selected and you can't copy a designer.
    }
    if (root.getLastSelectedComponent() instanceof MockForm) {
      root.setPasteTarget(root);
    } else {
      root.setPasteTarget(root.getLastSelectedComponent().getContainer());
    }
    return sb.toString();
  }

  private JsArrayString getSelectedComponentBlocks() {
    final T root = getRoot();
    JsArrayString code = (JsArrayString) JsArrayString.createArray();
    final BlocksEditor<?, ?> editor = getBlocksEditor();
    for (MockComponent component : root.getSelectedComponents()) {
      code = concat(code, getSelectedComponentBlocks(component, editor));
    }
    return code;
  }

  private JsArrayString getSelectedComponentBlocks(MockComponent component,
      BlocksEditor<?, ?> blocksEditor) {
    JsArrayString blocks = blocksEditor.getTopBlocksForComponentByName(component.getName());
    if (component instanceof MockContainer) {
      for (MockComponent child : ((MockContainer) component).getChildren()) {
        JsArrayString childBlocks = getSelectedComponentBlocks(child, blocksEditor);
        for (int i = 0; i < childBlocks.length(); i++) {
          blocks.push(childBlocks.get(i));
        }
      }
    }
    return blocks;
  }

  private void deleteSelectedComponent() {
    T root = getRoot();
    if (root.getLastSelectedComponent() instanceof MockForm) {
      return;  // Cannot delete MockForm
    }
    root.getLastSelectedComponent().delete();
  }

  private MockComponent pasteComponents(JSONArray components, MockContainer container,
      Map<String, String> substitution) {
    T form = getRoot();
    MockComponent lastComponentCreated = null;
    int insertBefore = -2;
    for (MockComponent component : form.getSelectedComponents()) {
      if (component.isVisibleComponent()) {
        insertBefore = Math.max(insertBefore, container.getChildren().indexOf(component));
      }
    }
    if (insertBefore < 0) {
      insertBefore = container.getShowingVisibleChildren().size();
    } else {
      insertBefore++;
    }
    for (JSONValue element : components.getElements()) {
      JSONObject object = element.asObject();
      String type = object.get("$Type").asString().getString();
      if (container.willAcceptComponentType(type) && container.canPasteComponentOfType(type)) {
        MockComponent pasted = createMockComponent(object, container, "Form", substitution);
        if (pasted.isVisibleComponent()) {
          container.removeComponent(pasted, false);
          container.addVisibleComponent(pasted, insertBefore);
          insertBefore = container.getChildren().indexOf(pasted) + 1;
        }
        container.onPaste(pasted);
        lastComponentCreated = pasted;
      }
    }
    return lastComponentCreated;
  }

  private MockComponent pasteForm(JSONObject prototype, Map<String, String> substitution) {
    T form = getRoot();
    // Copy the properties
    for (Map.Entry<String, JSONValue> property : prototype.getProperties().entrySet()) {
      if (property.getKey().startsWith("$")
          || property.getKey().equals(MockForm.PROPERTY_NAME_UUID)) {
        continue;
      }
      form.getProperties().getExistingProperty(property.getKey())
          .setValue(property.getValue().asString().getString());
    }

    // Clone the children
    MockComponent lastPasted = pasteComponents(prototype.get("$Components").asArray(), form,
        substitution);
    return lastPasted == null ? form : lastPasted;
  }

  private void pasteFromJsni(String jso, boolean dropBlocks) {
    final T root = getRoot();
    final MockContainer container = root.getPasteTarget();
    MockComponent lastComponentCreated = null;
    JSONObject value = new ClientJsonParser().parse(jso).asObject();
    Map<String, String> substitution = new HashMap<>();
    JSONArray components = value.get("$components").asArray();
    JSONArray blocks = value.get("$blocks").asArray();

    // First: Check to see if we are pasting a whole form
    for (JSONValue element : components.getElements()) {
      JSONObject object = element.asObject();
      if (object.get("$Type").asString().getString().equals(MockForm.TYPE)) {
        lastComponentCreated = pasteForm(object, substitution);
        break;
      }
    }

    // Second: If we didn't paste a form, paste components
    if (lastComponentCreated == null) {
      lastComponentCreated = pasteComponents(components, root.getPasteTarget(), substitution);
    }

    // Third: If we pasted anything and the user didn't hold shift, paste the associated blocks
    // with optional substitutions.
    if (lastComponentCreated != null && !dropBlocks) {
      getBlocksEditor().pasteFromJSNI(YaBlocksEditor.toJSO(substitution),
          YaBlocksEditor.toJsArrayString(blocks));
    }
    root.refresh(true);

    final MockComponent componentToSelect = lastComponentCreated;
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (componentToSelect != null) {
          root.setSelectedComponent(componentToSelect, null);
          onComponentSelectionChange(componentToSelect, true);
        }
        root.setPasteTarget(container);
      }
    });
  }

  private static native JsArrayString concat(JsArrayString first, JsArrayString second)/*-{
    return first.concat(second);
  }-*/;

  @SuppressWarnings("checkstyle:LineLength")
  private native void registerNativeListeners()/*-{
    var editor = this;

    function copy(e) {
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
        // don't interfere with copy/pasting input
        return false;
      }
      if ($wnd.getSelection && $wnd.getSelection() && $wnd.getSelection().toString() != '') {
        // user is copying some other selection on the page
        return false;
      }
      if (!editor.@com.google.appinventor.client.editor.FileEditor::isActiveEditor()()) {
        // don't copy/paste in non-active editor
        return false;
      }
      var data = editor.@com.google.appinventor.client.editor.designer.DesignerEditor::getSelectedComponentJson()();
      var xml = editor.@com.google.appinventor.client.editor.designer.DesignerEditor::getSelectedComponentBlocks()();
      data = JSON.parse(data);
      if (data instanceof Array) {
        data = {'$components': data, '$blocks': xml};
      } else {
        data = {'$components': [data], '$blocks': xml};
      }
      data = JSON.stringify(data);
      e.clipboardData.setData("application/json", data);
      e.clipboardData.setData("text/plain", data);
      e.preventDefault();
      return true;
    }

    $wnd.addEventListener('cut', function (e) {
      if (copy(e)) {
        editor.@com.google.appinventor.client.editor.designer.DesignerEditor::deleteSelectedComponent()();
      }
    });

    $wnd.addEventListener('copy', function (e) {
      copy(e);
    });

    $wnd.addEventListener('keydown', function (e) {
      if (e.keyCode === 16) {
        editor.shiftDown = true;
      }
    });

    $wnd.addEventListener('keyup', function (e) {
      if (e.keyCode === 16) {
        editor.shiftDown = false;
      }
    });

    $wnd.addEventListener('paste', function (e) {
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
        // don't interfere with copy/pasting input
        return;
      }
      if (!editor.@com.google.appinventor.client.editor.FileEditor::isActiveEditor()()) {
        // don't copy/paste in non-active editor
        return;
      }
      var data = e.clipboardData.getData('application/json');
      if (data === undefined || data === '') {
        data = e.clipboardData.getData('text/plain');
      }
      try {
        JSON.parse(data);
        e.preventDefault();
      } catch (e) {
        return;  // not valid JSON to paste, abort!
      }
      editor.@com.google.appinventor.client.editor.designer.DesignerEditor::pasteFromJsni(*)(data, editor.shiftDown);
    });
  }-*/;
}
