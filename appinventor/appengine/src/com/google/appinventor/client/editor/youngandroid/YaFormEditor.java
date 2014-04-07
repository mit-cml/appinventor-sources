// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.AssetListBox;
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
import com.google.appinventor.client.youngandroid.YoungAndroidFormUpgrader;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DockPanel;

import java.util.ArrayList;
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

  /**
   * Creates a new YaFormEditor.
   *
   * @param projectEditor  the project editor that contains this file editor
   * @param formNode the YoungAndroidFormNode associated with this YaFormEditor
   */
  YaFormEditor(ProjectEditor projectEditor, YoungAndroidFormNode formNode) {
    super(projectEditor, formNode);

    this.formNode = formNode;

    // Get reference to the source structure explorer
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
            onFileLoaded(fileContentHolder.getFileContent());
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
    return formNode.getFormName();
  }

  @Override
  public void onShow() {
    OdeLog.log("YaFormEditor: got onShow() for " + getFileId());
    super.onShow();
    loadDesigner();
  }

  @Override
  public void onHide() {
    OdeLog.log("YaFormEditor: got onHide() for " + getFileId());
    // When an editor is detached, if we are the "current" editor,
    // set the current editor to null and clean up the UI.
    // Note: I'm not sure it is possible that we would not be the "current"
    // editor when this is called, but we check just to be safe.
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      super.onHide();
      unloadDesigner();
    } else {
      OdeLog.wlog("YaFormEditor.onHide: Not doing anything since we're not the "
          + "current file editor!");
    }
  }
  
  @Override
  public void onClose() {
    form.removeFormChangeListener(this);
    // Note: our partner YaBlocksEditor will remove itself as a FormChangeListener, even
    // though we added it.
  }

  @Override
  public String getRawFileContent() {
    String encodedProperties = encodeFormAsJsonString();
    JSONObject propertiesObject = JSON_PARSER.parse(encodedProperties).asObject();
    return YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject);
  }

  @Override
  public void onSave() {
  }

  // SimpleEditor methods

  @Override
  public boolean isLoadComplete() {
    return loadComplete;
  }

  @Override
  public Map<String, MockComponent> getComponents() {
    Map<String, MockComponent> map = Maps.newHashMap();
    if (loadComplete) {
      populateComponentsMap(form, map);
    } else {
      OdeLog.log("YaFormEditor: about to return an empty map!!!!!");
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
    return formNode.isScreen1();
  }

  // FormChangeListener implementation

  @Override
  public void onComponentPropertyChanged(MockComponent component,
      String propertyName, String propertyValue) {
    if (loadComplete) {
      // If the property isn't actually persisted to the .scm file, we don't need to do anything.
      if (component.isPropertyPersisted(propertyName)) {
        Ode.getInstance().getEditorManager().scheduleAutoSave(this);
        updatePhone();          // Push changes to the phone if it is connected
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

  public String getComponentInstanceTypeName(String instanceName) {
    return getComponents().get(instanceName).getType();
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

      Ode.getInstance().getProjectService().save(Ode.getInstance().getSessionId(),
          getProjectId(), getFileId(), upgradedContent,
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

    // Add component type to the blocks editor
    YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
    YaBlocksEditor blockEditor = yaProjectEditor.getBlocksFileEditor(formNode.getFormName());
    blockEditor.addComponent(mockComponent.getType(), mockComponent.getName(),
        mockComponent.getUuid());

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
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.setContent(palettePanel);

    // Update the source structure explorer with the tree of this form's components.
    sourceStructureExplorer.updateTree(form.buildComponentsTree(),
        selectedComponent.getSourceStructureExplorerItem());
    SourceStructureBox.getSourceStructureBox().setVisible(true);

    // Show the assets box.
    AssetListBox assetListBox = AssetListBox.getAssetListBox();
    assetListBox.setVisible(true);

    // Set the properties box's content.
    PropertiesBox propertiesBox = PropertiesBox.getPropertiesBox();
    propertiesBox.setContent(designProperties);
    updatePropertiesPanel(selectedComponent);
    propertiesBox.setVisible(true);

    // Listen to changes on the form.
    form.addFormChangeListener(this);
    // Also have the blocks editor listen to changes. Do this here instead
    // of in the blocks editor so that we don't risk it missing any updates.
    OdeLog.log("Adding blocks editor as a listener for " + form.getName());
    form.addFormChangeListener(((YaProjectEditor) projectEditor)
        .getBlocksFileEditor(form.getName()));
  }

  /*
   * Show the given component's properties in the properties panel.
   */
  private void updatePropertiesPanel(MockComponent component) {
    designProperties.setProperties(component.getProperties());
    // need to update the caption after the setProperties call, since
    // setProperties clears the caption!
    designProperties.setPropertiesCaption(component.getName());
  }

  private void onFormStructureChange() {
    Ode.getInstance().getEditorManager().scheduleAutoSave(this);

    // Update source structure panel
    sourceStructureExplorer.updateTree(form.buildComponentsTree(),
        form.getSelectedComponent().getSourceStructureExplorerItem());
    updatePhone();          // Push changes to the phone if it is connected
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
   * Encodes the form's properties as a JSON encoded string. Used by YaBlocksEditor as well,
   * to send the form info to the blockly world during code generation.
   */
  protected String encodeFormAsJsonString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"YaVersion\":\"").append(YaVersion.YOUNG_ANDROID_VERSION).append("\",");
    sb.append("\"Source\":\"Form\",");
    sb.append("\"Properties\":");
    encodeComponentProperties(form, sb);
    sb.append("}");
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
    // The form can still potentially change if the blocks editor is displayed
    // so don't remove the formChangeListener.

    // Clear the palette box.
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.clear();

    // Clear and hide the source structure explorer.
    sourceStructureExplorer.clearTree();
    SourceStructureBox.getSourceStructureBox().setVisible(false);

    // Hide the assets box.
    AssetListBox assetListBox = AssetListBox.getAssetListBox();
    assetListBox.setVisible(false);

    // Clear and hide the properties box.
    PropertiesBox propertiesBox = PropertiesBox.getPropertiesBox();
    propertiesBox.clear();
    propertiesBox.setVisible(false);
  }

  /*
   * Push changes to a connected phone (or emulator).
   */
  private void updatePhone() {
    YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
    YaBlocksEditor blockEditor = yaProjectEditor.getBlocksFileEditor(formNode.getFormName());
    blockEditor.onBlocksAreaChanged(getProjectId() + "_" + formNode.getFormName());
  }

}
