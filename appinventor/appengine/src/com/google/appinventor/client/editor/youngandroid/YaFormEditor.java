// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.ComponentNotFoundException;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.SimpleVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.components.FormChangeListener;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.components.MockVisibleComponent;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.properties.json.ClientJsonString;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.PropertiesPanel;
import com.google.appinventor.client.widgets.properties.PropertyChangeListener;
import com.google.appinventor.client.youngandroid.YoungAndroidFormUpgrader;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.editor.simple.components.MockComponent.PROPERTY_NAME_NAME;

/**
 * Editor for Young Android Form (.scm) files.
 *
 * <p>This editor shows a designer that provides support for visual design of
 * forms.</p>
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 */
public final class YaFormEditor extends SimpleEditor implements FormChangeListener, ComponentDatabaseChangeListener, PropertyChangeListener {

  private static final Logger LOG = Logger.getLogger(YaFormEditor.class.getName());
  HiddenComponentsCheckbox hiddenComponentsCheckbox = HiddenComponentsCheckbox.getCheckbox();

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

  private static final String ERROR_EXISTING_UUID = "Component with UUID \"%1$s\" already exists.";
  private static final String ERROR_NONEXISTENT_UUID = "No component exists with UUID \"%1$s\".";

  // JSON parser
  private static final JSONParser JSON_PARSER = new ClientJsonParser();

  private final SimpleComponentDatabase COMPONENT_DATABASE;

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

  // [lyn, 2014/10/13] Need to remember JSON initially loaded from .scm file *before* it is upgraded
  // by YoungAndroidFormUpgrader within upgradeFile. This JSON contains pre-upgrade component
  // version info that is needed by Blockly.SaveFile.load to perform upgrades in the Blocks Editor.
  // This was unnecessary in AI Classic because the .blk file contained component version info
  // as well as the .scm file. But in AI2, the .bky file contains no component version info,
  // and we rely on the pre-upgraded .scm file for this info.
  private String preUpgradeJsonString;

  private final List<ComponentDatabaseChangeListener> componentDatabaseChangeListeners = new ArrayList<ComponentDatabaseChangeListener>();
  private JSONArray authURL;    // List of App Inventor versions we have been edited on.

  /**
   * A mapping of component UUIDs to mock components in the designer view.
   */
  private final Map<String, MockComponent> componentsDb = new HashMap<String, MockComponent>();

  private static final int OLD_PROJECT_YAV = 150; // Projects older then this have no authURL

  private EditableProperties selectedProperties = null;

  /**
   * Creates a new YaFormEditor.
   *
   * @param projectEditor  the project editor that contains this file editor
   * @param formNode the YoungAndroidFormNode associated with this YaFormEditor
   */
  YaFormEditor(ProjectEditor projectEditor, YoungAndroidFormNode formNode) {
    super(projectEditor, formNode);

    this.formNode = formNode;
    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance(getProjectId());

    // Get reference to the source structure explorer
    sourceStructureExplorer =
        SourceStructureBox.getSourceStructureBox().getSourceStructureExplorer();

    // Create UI elements for the designer panels.
    nonVisibleComponentsPanel = new SimpleNonVisibleComponentsPanel();
    componentDatabaseChangeListeners.add(nonVisibleComponentsPanel);
    visibleComponentsPanel = new SimpleVisibleComponentsPanel(this, nonVisibleComponentsPanel);
    componentDatabaseChangeListeners.add(visibleComponentsPanel);
    DockPanel componentsPanel = new DockPanel();
    componentsPanel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
    componentsPanel.add(visibleComponentsPanel, DockPanel.NORTH);
    componentsPanel.add(nonVisibleComponentsPanel, DockPanel.SOUTH);
    componentsPanel.setSize("100%", "100%");

    // Create designProperties, which will be used as the content of the PropertiesBox.
    designProperties = new PropertiesPanel();
    designProperties.setSize("100%", "100%");

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
    componentDatabaseChangeListeners.add(palettePanel);

    initWidget(componentsPanel);
    setSize("100%", "100%");
    registerNativeListeners();
  }

  public boolean shouldDisplayHiddenComponents() {
    return projectEditor.getScreenCheckboxState(form.getTitle()) != null
               && projectEditor.getScreenCheckboxState(form.getTitle());
  }

  // FileEditor methods

  @Override
  public DropTargetProvider getDropTargetProvider() {
    return new DropTargetProvider() {
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
    };
  }

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
            } catch(IllegalArgumentException e) {
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

  public SourceStructureExplorer getSourceStructureExplorer() {
    return sourceStructureExplorer;
  }

  @Override
  public String getTabText() {
    return formNode.getFormName();
  }

  @Override
  public void onShow() {
    LOG.info("YaFormEditor: got onShow() for " + getFileId());
    super.onShow();
    hiddenComponentsCheckbox.show(form);
    loadDesigner();
    Tracking.trackEvent(Tracking.EDITOR_EVENT, Tracking.EDITOR_ACTION_SHOW_DESIGNER);
  }

  @Override
  public void onHide() {
    LOG.info("YaFormEditor: got onHide() for " + getFileId());
    // When an editor is detached, if we are the "current" editor,
    // set the current editor to null and clean up the UI.
    // Note: I'm not sure it is possible that we would not be the "current"
    // editor when this is called, but we check just to be safe.
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      super.onHide();
      unloadDesigner();
    } else {
      LOG.warning("YaFormEditor.onHide: Not doing anything since we're not the "
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
    String encodedProperties = encodeFormAsJsonString(false);
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

  public SimpleVisibleComponentsPanel getVisibleComponentsPanel() {
    return visibleComponentsPanel;
  }

  @Override
  public boolean isScreen1() {
    return formNode.isScreen1();
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
    LOG.info("Property change " + propertyName + " to " + propertyValue);
    for (MockComponent selectedComponent : form.getSelectedComponents()) {
      selectedComponent.changeProperty(propertyName, propertyValue);
      // Ensure the editor matches (multiselect)
      selectedComponent.getProperties().getExistingProperty(propertyName).getEditor().refresh();
    }
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
      LOG.severe("onComponentPropertyChanged called when loadComplete is false");
    }
  }

  @Override
  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (loadComplete) {
      if (permanentlyDeleted) {
        onFormStructureChange();
      }
    } else {
      LOG.severe("onComponentRemoved called when loadComplete is false");
    }
  }

  @Override
  public void onComponentAdded(MockComponent component) {
    if (loadComplete) {
      selectedProperties = component.getProperties();
      onFormStructureChange();
    } else {
      LOG.severe("onComponentAdded called when loadComplete is false");
    }
  }

  @Override
  public void onComponentRenamed(MockComponent component, String oldName) {
    if (loadComplete) {
      onFormStructureChange();
    } else {
      LOG.severe("onComponentRenamed called when loadComplete is false");
    }
  }

  @Override
  public void onComponentSelectionChange(MockComponent component, boolean selected) {
    if (loadComplete) {
      // TODO: SMRL Not sure this class should keep a pointer to source structure
      sourceStructureExplorer.selectItem(component.getSourceStructureExplorerItem());
      SourceStructureBox.getSourceStructureBox().show(form);
      PropertiesBox.getPropertiesBox().show(this, true);
    } else {
      LOG.severe("onComponentSelectionChange called when loadComplete is false");
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

    // BEGIN PROJECT TAGGING CODE

    // |-------------------------------------------------------------------|
    // | Project Tagging Code:                                             |
    // | Because of the likely proliferation of various versions of App    |
    // | Inventor, we want to mark a project with the history of which     |
    // | versions have seen it. We do that with the "authURL" tag which we |
    // | add to the Form files. It is a JSON array of versions identified  |
    // | by the hostname portion of the URL of the service editing the     |
    // | project. Older projects will not have this field, so if we detect |
    // | an older project (YAV < OLD_PROJECT_YAV) we create the list and   |
    // | add ourselves. If we read in a project where YAV >=               |
    // | OLD_PROJECT_YAV *and* there is no authURL, we assume that it was  |
    // | created on a version of App Inventor that doesn't support project |
    // | tagging and we add an "*UNKNOWN*" tag to indicate this. So for    |
    // | example if you examine a (newer) project and look in the          |
    // | Screen1.scm file, you should just see an authURL that looks like  |
    // | ["ai2.appinventor.mit.edu"]. This would indicate a project that   |
    // | has only been edited on MIT App Inventor. If instead you see      |
    // | something like ["localhost", "ai2.appinventor.mit.edu"] it        |
    // | implies that at some point in its history this project was edited |
    // | using the local dev server on someone's own computer.             |
    // |-------------------------------------------------------------------|

    authURL = (JSONArray) propertiesObject.get("authURL");
    String ourHost = Window.Location.getHostName();
    JSONValue us = new ClientJsonString(ourHost);
    if (authURL != null) {
      List<JSONValue> values = authURL.asArray().getElements();
      boolean foundUs = false;
      for (JSONValue value : values) {
        if (value.asString().getString().equals(ourHost)) {
          foundUs = true;
          break;
        }
      }
      if (!foundUs) {
        authURL.asArray().getElements().add(us);
      }
    } else {
      // Kludgey way to create an empty JSON array. But we cannot call ClientJsonArray ourselves
      // because it is not a public class. So rather then make it public (and violate an abstraction
      // barrier). We create the array this way. Sigh.
      authURL = JSON_PARSER.parse("[]").asArray();
      // Warning: If YaVersion isn't present, we will get an NPF on
      // the line below. But it should always be there...
      // Note: YaVersion although a numeric value is stored as a Json String so we have
      // to parse it as a string and then convert it to a number in Java.
      int yav = Integer.parseInt(propertiesObject.get("YaVersion").asString().getString());
      // If yav is > OLD_PROJECT_YAV, and we still don't have an
      // authURL property then we likely originated from a non-MIT App
      // Inventor instance so add an *Unknown* tag before our tag
      if (yav > OLD_PROJECT_YAV) {
        authURL.asArray().getElements().add(new ClientJsonString("*UNKNOWN*"));
      }
      authURL.asArray().getElements().add(us);
    }

    // END OF PROJECT TAGGING CODE

    preUpgradeJsonString =  propertiesObject.toJson(); // [lyn, [2014/10/13] remember pre-upgrade component versions.
    if (YoungAndroidFormUpgrader.upgradeSourceProperties(propertiesObject.getProperties())) {
      String upgradedContent = YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject);
      fileContentHolder.setFileContent(upgradedContent);
      Ode ode = Ode.getInstance();
      if (ode.isReadOnly()) {   // Do not attempt to save out the project if we are in readonly mode
        if (afterUpgradeComplete != null) {
          afterUpgradeComplete.execute(); // But do call the afterUpgradeComplete call
        }
      } else {
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
      }
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
    try {
      form = createMockForm(propertiesObject.getProperties().get("Properties").asObject());
    } catch(ComponentNotFoundException e) {
      Ode.getInstance().recordCorruptProject(getProjectId(), getProjectRootNode().getName(),
          e.getMessage());
      ErrorReporter.reportError(MESSAGES.noComponentFound(e.getComponentName(),
          getProjectRootNode().getName()));
      throw e;
    }

    // Initialize the nonVisibleComponentsPanel and visibleComponentsPanel.
    nonVisibleComponentsPanel.setForm(form);
    visibleComponentsPanel.setForm(form);
    form.select(null);

    String subsetjson = form.getPropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET);
    if (subsetjson.length() > 0) {
      reloadComponentPalette(subsetjson);
    }
    // Set loadCompleted to true.
    // From now on, all change events will be taken seriously.
    loadComplete = true;

    // Originally this was done in loadDesigner. However, this resulted in
    // the form and blocks editor not being registered for events until after
    // they were opened. This became problematic if the user deleted an extension
    // prior to opening the screen as they would never trigger a save, resulting
    // in a corrupt project.

    // Listen to changes on the form.
    form.addFormChangeListener(this);
    // Also have the blocks editor listen to changes. Do this here instead
    // of in the blocks editor so that we don't risk it missing any updates.
    form.addFormChangeListener(((YaProjectEditor) projectEditor)
        .getBlocksFileEditor(form.getName()));
  }

  public void reloadComponentPalette(String subsetjson) {
    LOG.info(subsetjson);
    Set<String> shownComponents = new HashSet<String>();
    if (subsetjson.length() > 0) {
      try {
        String shownComponentsStr = getShownComponents(subsetjson);
        if (shownComponentsStr.length() > 0) {
          shownComponents = new HashSet<String>(Arrays.asList(shownComponentsStr.split(",")));
        }
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "invalid subset string", e);
      }
      // Toolkit does not currently support Extensions. The Extensions palette should be left alone.
      palettePanel.clearComponentsExceptExtension();
    } else {
      shownComponents = COMPONENT_DATABASE.getComponentNames();
      palettePanel.clearComponents();
    }
    for (String component : shownComponents) {
      palettePanel.addComponent(component);
    }
  }

  private native String getShownComponents(String subsetString)/*-{
    var jsonObj = JSON.parse(subsetString);
    var shownComponentTypes = jsonObj["shownComponentTypes"];
    var shownString = "";
    for (var category in shownComponentTypes) {
      var categoryArr = shownComponentTypes[category];
      for (var i = 0; i < categoryArr.length; i++) {
        shownString = shownString + "," + categoryArr[i]["type"];
        //console.log(categoryArr[i]["type"]);
      }
    }
    var shownCompStr = shownString.substring(1);
    //console.log(shownCompStr);
    return shownCompStr;
  }-*/;

  /*
   * Parses the JSON properties and creates the form and its component structure.
   */
  private MockForm createMockForm(JSONObject propertiesObject) {
    return (MockForm) createMockComponent(propertiesObject, null);
  }

  private MockComponent createMockComponent(JSONObject properties, MockContainer container) {
    return createMockComponent(properties, container, null);
  }

  /*
   * Parses the JSON properties and creates the component structure. This method is called
   * recursively for nested components. For the initial invocation parent shall be null.
   */
  private MockComponent createMockComponent(JSONObject propertiesObject, MockContainer parent, Map<String, String> substitution) {
    Map<String, JSONValue> properties = propertiesObject.getProperties();

    // Component name and type
    String componentType = properties.get("$Type").asString().getString();

    // Set the name of the component (on instantiation components are assigned a generated name)
    boolean shouldRename = false;
    String componentName = properties.get("$Name").asString().getString();

    // Instantiate a mock component for the visual designer
    MockComponent mockComponent;
    if (componentType.equals(MockForm.TYPE)) {
      Preconditions.checkArgument(parent == null);

      // Instantiate new root component
      mockComponent = new MockForm(this);
    } else {
      mockComponent = SimpleComponentDescriptor.createMockComponent(componentType,
          COMPONENT_DATABASE.getComponentType(componentType), this);

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

    if (mockComponent instanceof MockForm) {
      // A bug in an early version of multiselect resulted in Form gaining Row and Column
      // properties, which are reserved for visible components that can appear in TableArrangements.
      // Form doesn't have these properties, so we need to clean up the properties. The remove
      // call here is idempotent--if the property is present, it is removed. If not present, the
      // map remains unchanged.
      properties.remove(MockVisibleComponent.PROPERTY_NAME_ROW);
      properties.remove(MockVisibleComponent.PROPERTY_NAME_COLUMN);
    }

    // Set component properties
    for (String name : properties.keySet()) {
      if (name.charAt(0) != '$') { // Ignore special properties (name, type and nested components)
        mockComponent.changeProperty(name, properties.get(name).asString().getString());
      }
    }



    //This is for old project which doesn't have the AppName property
    if (mockComponent instanceof MockForm) {
      if (!properties.keySet().contains("AppName")) {
        String fileId = getFileId();
        String projectName = fileId.split("/")[3];
        mockComponent.changeProperty("AppName", projectName);
      }
    }

    // Add component type to the blocks editor
    getBlocksEditor().addComponent(mockComponent.getType(), mockComponent.getName(),
        mockComponent.getUuid());

    // Add nested components
    if (properties.containsKey("$Components")) {
      for (JSONValue nestedComponent : properties.get("$Components").asArray().getElements()) {
        createMockComponent(nestedComponent.asObject(), (MockContainer) mockComponent, substitution);
      }
    }

    return mockComponent;
  }

  @Override
  public void getBlocksImage(Callback<String, String> callback) {
    getBlocksEditor().getBlocksImage(callback);
  }

  /*
   * Updates the the whole designer: form, palette, source structure explorer,
   * assets list, and properties panel.
   */
  private void loadDesigner() {
    form.refresh();
    MockComponent selectedComponent = form.getLastSelectedComponent();

    // Set the palette box's content.
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.setContent(palettePanel);

    // Update the source structure explorer with the tree of this form's components.
    // TODO: SMRL Refactor links to source structure
    sourceStructureExplorer.updateTree(form.buildComponentsTree(),
        selectedComponent.getSourceStructureExplorerItem());
    SourceStructureBox.getSourceStructureBox().setVisible(true);

    // Set the properties box's content.
    SourceStructureBox.getSourceStructureBox().show(form);
    PropertiesBox.getPropertiesBox().show(this, true);

    Ode.getInstance().showComponentDesigner();
  }

  public void refreshCurrentPropertiesPanel() {
    PropertiesBox.getPropertiesBox().show(this, true);
  }

  private void onFormStructureChange() {
    Ode.getInstance().getEditorManager().scheduleAutoSave(this);

    // Update source structure panel
    sourceStructureExplorer.updateTree(form.buildComponentsTree(),
        form.getLastSelectedComponent().getSourceStructureExplorerItem());
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
  protected String encodeFormAsJsonString(boolean forYail) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    // Include authURL in output if it is non-null
    if (authURL != null) {
      sb.append("\"authURL\":").append(authURL.toJson()).append(",");
    }
    sb.append("\"YaVersion\":\"").append(YaVersion.YOUNG_ANDROID_VERSION).append("\",");
    sb.append("\"Source\":\"Form\",");
    sb.append("\"Properties\":");
    encodeComponentProperties(form, sb, forYail);
    sb.append("}");
    return sb.toString();
  }

  // [lyn, 2014/10/13] returns the *pre-upgraded* JSON for this form.
  // needed to allow associated blocks editor to get this info.
  protected String preUpgradeJsonString() {
    return preUpgradeJsonString;
  }

  /*
   * Encodes a component and its properties into a JSON encoded string.
   */
  private void encodeComponentProperties(MockComponent component, StringBuilder sb, boolean forYail) {
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

    Ode.getInstance().hideComponentDesigner();
  }

  /**
   * Runs through all the Mock Components and upgrades if its corresponding Component was Upgraded
   * @param componentTypes the Component Types that got upgraded
   */
  private void updateMockComponents(List<String> componentTypes) {
    Map<String, MockComponent> componentMap = getComponents();
    for (MockComponent mockComponent : componentMap.values()) {
      if (componentTypes.contains(mockComponent.getType())) {
        mockComponent.upgrade();
        mockComponent.upgradeComplete();
      }
    }
  }

  /*
   * Push changes to a connected phone (or emulator).
   */
  private void updatePhone() {
    getBlocksEditor().sendComponentData();
  }

  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {
    COMPONENT_DATABASE.removeComponentDatabaseListener(this);
    for (ComponentDatabaseChangeListener cdbChangeListener : componentDatabaseChangeListeners) {
      // TODO: Refactor calls to to Source Structure
      cdbChangeListener.onComponentTypeAdded(componentTypes);
    }
    updateMockComponents(componentTypes);
    PropertiesBox.getPropertiesBox().show(this, true);
    SourceStructureBox.getSourceStructureBox().show(form);
  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    boolean result = true;
    for (ComponentDatabaseChangeListener cdbChangeListener : componentDatabaseChangeListeners) {
      result = result & cdbChangeListener.beforeComponentTypeRemoved(componentTypes);
    }
    List<MockComponent> mockComponents = new ArrayList<MockComponent>(getForm().getChildren());
    for (String compType : componentTypes) {
      for (MockComponent mockComp : mockComponents) {
        if (mockComp.getType().equals(compType)) {
          mockComp.delete();
        }
      }
    }
    return result;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {
    COMPONENT_DATABASE.removeComponentDatabaseListener(this);
    for (ComponentDatabaseChangeListener cdbChangeListener : componentDatabaseChangeListeners) {
      cdbChangeListener.onComponentTypeRemoved(componentTypes);
    }
  }

  @Override
  public void onResetDatabase() {
    COMPONENT_DATABASE.removeComponentDatabaseListener(this);
    for (ComponentDatabaseChangeListener cdbChangeListener : componentDatabaseChangeListeners) {
      cdbChangeListener.onResetDatabase();
    }
  }

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
      var data = editor.@com.google.appinventor.client.editor.youngandroid.YaFormEditor::getSelectedComponentJson()();
      var xml = editor.@com.google.appinventor.client.editor.youngandroid.YaFormEditor::getSelectedComponentBlocks()();
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
        editor.@com.google.appinventor.client.editor.youngandroid.YaFormEditor::deleteSelectedComponent()();
      }
    });

    $wnd.addEventListener('copy', function (e) {
      copy(e);
    });

    $wnd.addEventListener('keydown', function(e) {
      if (e.keyCode === 16) {
        editor.shiftDown = true;
      }
    });

    $wnd.addEventListener('keyup', function(e) {
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
      } catch(e) {
        return;  // not valid JSON to paste, abort!
      }
      editor.@com.google.appinventor.client.editor.youngandroid.YaFormEditor::pasteFromJsni(*)(data, editor.shiftDown);
    });
  }-*/;

  private void deleteSelectedComponent() {
    if (form.getLastSelectedComponent() instanceof MockForm) {
      return;  // Cannot delete MockForm
    }
    form.getLastSelectedComponent().delete();
  }

  private native JsArrayString concat(JsArrayString first, JsArrayString second)/*-{
    return first.concat(second);
  }-*/;

  private YaBlocksEditor getBlocksEditor() {
    return ((YaProjectEditor) projectEditor).getBlocksFileEditor(formNode.getFormName());
  }

  private JsArrayString getSelectedComponentBlocks() {
    JsArrayString code = (JsArrayString) JsArrayString.createArray();
    final YaBlocksEditor editor = getBlocksEditor();
    for (MockComponent component : form.getSelectedComponents()) {
      code = concat(code, getSelectedComponentBlocks(component, editor));
    }
    return code;
  }

  private JsArrayString getSelectedComponentBlocks(MockComponent component,
      YaBlocksEditor blocksEditor) {
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

  private String getSelectedComponentJson() {
    StringBuilder sb = new StringBuilder();
    String sep = "";
    sb.append("[");
    if (form.getSelectedComponents().size() == 1
        && form.getSelectedComponents().get(0) instanceof MockForm) {
      encodeComponentProperties(form, sb, false);
    } else {
      for (MockComponent component : form.getSelectedComponents()) {
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
      return "";  // Only had the MockForm selected and you can't copy a form.
    }
    if (form.getLastSelectedComponent() instanceof MockForm) {
      form.setPasteTarget(form);
    } else {
      form.setPasteTarget(form.getLastSelectedComponent().getContainer());
    }
    return sb.toString();
  }

  private MockComponent pasteComponents(JSONArray components, MockContainer container,
      Map<String, String> substitution) {
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
      if (container.willAcceptComponentType(type)) {
        MockComponent pasted = createMockComponent(object, container, substitution);
        if (pasted.isVisibleComponent()) {
          container.removeComponent(pasted, false);
          container.addVisibleComponent(pasted, insertBefore);
          insertBefore = container.getChildren().indexOf(pasted) + 1;
        }
        lastComponentCreated = pasted;
      }
    }
    return lastComponentCreated;
  }

  private MockComponent pasteForm(JSONObject prototype, Map<String, String> substitution) {
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
    final MockContainer container = form.getPasteTarget();
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
      lastComponentCreated = pasteComponents(components, form.getPasteTarget(), substitution);
    }

    // Third: If we pasted anything and the user didn't hold shift, paste the associated blocks
    // with optional substitutions.
    if (lastComponentCreated != null && !dropBlocks) {
      getBlocksEditor().pasteFromJSNI(YaBlocksEditor.toJSO(substitution),
          YaBlocksEditor.toJsArrayString(blocks));
    }
    form.doRefresh();

    final MockComponent componentToSelect = lastComponentCreated;
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        if (componentToSelect != null) {
          form.setSelectedComponent(componentToSelect, null);
          onComponentSelectionChange(componentToSelect, true);
        }
        form.setPasteTarget(container);
      }
    });
  }
}
