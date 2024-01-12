// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.simple.ComponentNotFoundException;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.properties.json.ClientJsonString;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.youngandroid.YoungAndroidFormUpgrader;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Editor for Young Android Form (.scm) files.
 *
 * <p>This editor shows a designer that provides support for visual design of
 * forms.</p>
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public final class YaFormEditor extends DesignerEditor<YoungAndroidFormNode, MockForm,
    YoungAndroidPalettePanel, SimpleComponentDatabase, YaVisibleComponentsPanel> {
  private static final Logger LOG = Logger.getLogger(YaFormEditor.class.getName());

  private static final String ERROR_EXISTING_UUID = "Component with UUID \"%1$s\" already exists.";
  private static final String ERROR_NONEXISTENT_UUID = "No component exists with UUID \"%1$s\".";

  // JSON parser
  private static final JSONParser JSON_PARSER = new ClientJsonParser();

  // [lyn, 2014/10/13] Need to remember JSON initially loaded from .scm file *before* it is upgraded
  // by YoungAndroidFormUpgrader within upgradeFile. This JSON contains pre-upgrade component
  // version info that is needed by Blockly.SaveFile.load to perform upgrades in the Blocks Editor.
  // This was unnecessary in AI Classic because the .blk file contained component version info
  // as well as the .scm file. But in AI2, the .bky file contains no component version info,
  // and we rely on the pre-upgraded .scm file for this info.
  private String preUpgradeJsonString;

  private JSONArray authURL;    // List of App Inventor versions we have been edited on.

  /**
   * A mapping of component UUIDs to mock components in the designer view.
   */
  private final Map<String, MockComponent> componentsDb = new HashMap<String, MockComponent>();

  private static final int OLD_PROJECT_YAV = 150; // Projects older then this have no authURL

  /**
   * Creates a new YaFormEditor.
   *
   * @param projectEditor  the project editor that contains this file editor
   * @param formNode the YoungAndroidFormNode associated with this YaFormEditor
   */
  YaFormEditor(ProjectEditor projectEditor, YoungAndroidFormNode formNode) {
    super(projectEditor, formNode, SimpleComponentDatabase.getInstance(formNode.getProjectId()),
        new YaVisibleComponentsPanel(projectEditor, new SimpleNonVisibleComponentsPanel<MockForm>()));

    // Create palettePanel, which will be used as the content of the PaletteBox.
    palettePanel = new YoungAndroidPalettePanel(this);
    palettePanel.loadComponents(new DropTargetProvider() {
      @Override
      public DropTarget[] getDropTargets() {
        // TODO(markf): Figure out a good way to memorize the targets or refactor things so that
        // getDropTargets() doesn't get called for each component.
        // NOTE: These targets must be specified in depth-first order.
        List<DropTarget> dropTargets = root.getDropTargetsWithin();
        dropTargets.add(getVisibleComponentsPanel());
        dropTargets.add(getNonVisibleComponentsPanel());
        return dropTargets.toArray(new DropTarget[dropTargets.size()]);
      }
    });
    palettePanel.setSize("100%", "100%");
    componentDatabaseChangeListeners.add(palettePanel);
    registerNativeListeners();
  }

  public boolean shouldDisplayHiddenComponents() {
    return visibleComponentsPanel.isHiddenComponentsCheckboxChecked();
  }

  // FileEditor methods

  @Override
  public String getTabText() {
    return sourceNode.getFormName();
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
    root.removeDesignerChangeListener(this);
    // Note: our partner YaBlocksEditor will remove itself as a DesignerChangeListener, even
    // though we added it.
  }

  @Override
  public String getRawFileContent() {
    String encodedProperties = encodeFormAsJsonString(false);
    JSONObject propertiesObject = JSON_PARSER.parse(encodedProperties).asObject();
    return YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject);
  }

  // SimpleEditor methods
  @Override
  public boolean isScreen1() {
    return sourceNode.isScreen1();
  }

  // DesignerChangeListener implementation

  @Override
  public void onComponentPropertyChanged(MockComponent component,
      String propertyName, String propertyValue) {
    super.onComponentPropertyChanged(component, propertyName, propertyValue);
    if (isLoadComplete() && component.isPropertyPersisted(propertyName)) {
      updatePhone();          // Push changes to the phone if it is connected
    }
  }

  // other public methods

  /**
   * Returns the form associated with this YaFormEditor.
   *
   * @return a MockForm
   */
  public MockForm getForm() {
    return root;
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
  protected void upgradeFile(FileContentHolder fileContentHolder,
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

  @Override
  protected void onFileLoaded(String content) {
    JSONObject propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(
        content, JSON_PARSER);
    try {
      root = createMockForm(propertiesObject.getProperties().get("Properties").asObject());
    } catch(ComponentNotFoundException e) {
      Ode.getInstance().recordCorruptProject(getProjectId(), getProjectRootNode().getName(),
          e.getMessage());
      ErrorReporter.reportError(MESSAGES.noComponentFound(e.getComponentName(),
          getProjectRootNode().getName()));
      throw e;
    }

    // Initialize the nonVisibleComponentsPanel and visibleComponentsPanel.
    nonVisibleComponentsPanel.setRoot(root);
    visibleComponentsPanel.setRoot(root);
    root.select(null);

    String subsetjson = root.getPropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET);
    if (subsetjson.length() > 0) {
      reloadComponentPalette(subsetjson);
    }
    super.onFileLoaded(content);

    // Originally this was done in loadDesigner. However, this resulted in
    // the form and blocks editor not being registered for events until after
    // they were opened. This became problematic if the user deleted an extension
    // prior to opening the screen as they would never trigger a save, resulting
    // in a corrupt project.

    // Listen to changes on the form.
    root.addDesignerChangeListener(this);
    // Also have the blocks editor listen to changes. Do this here instead
    // of in the blocks editor so that we don't risk it missing any updates.
    root.addDesignerChangeListener(((YaProjectEditor) projectEditor)
        .getBlocksFileEditor(root.getName()));
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
      shownComponents = componentDatabase.getComponentNames();
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
    return (MockForm) createMockComponent(propertiesObject, null, MockForm.TYPE);
  }

  @Override
  public void getBlocksImage(Callback<String, String> callback) {
    getBlocksEditor().getBlocksImage(callback);
  }

  /*
   * Updates the the whole designer: form, palette, source structure explorer,
   * assets list, and properties panel.
   */
  protected void loadDesigner() {
    root.refresh();
    MockComponent selectedComponent = root.getLastSelectedComponent();

    // Set the palette box's content.
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.setContent(palettePanel);

    super.loadDesigner();
  }

  public void refreshCurrentPropertiesPanel() {
    updatePropertiesPanel(root.getSelectedComponents(), true);
  }

  @Override
  protected void onStructureChange() {
    super.onStructureChange();
    updatePhone();          // Push changes to the phone if it is connected
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
    encodeComponentProperties(root, sb, forYail);
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
  public String getJson() {
    return preUpgradeJsonString;
  }

  @Override
  protected MockForm newRootObject() {
    return new MockForm(this);
  }

  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {
    super.onComponentTypeAdded(componentTypes);
    //Update Mock Components
    updateMockComponents(componentTypes);
  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    boolean result = super.beforeComponentTypeRemoved(componentTypes);
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
    MockForm form = getForm();
    if (form.getLastSelectedComponent() instanceof MockForm) {
      return;  // Cannot delete MockForm
    }
    form.getLastSelectedComponent().delete();
  }

  private native JsArrayString concat(JsArrayString first, JsArrayString second)/*-{
    return first.concat(second);
  }-*/;

  private YaBlocksEditor getBlocksEditor() {
    return (YaBlocksEditor) ((YaProjectEditor) projectEditor).getBlocksFileEditor(sourceNode.getFormName());
  }

  private JsArrayString getSelectedComponentBlocks() {
    final MockForm form = getRoot();
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
    MockForm form = getRoot();
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
    MockForm form = getForm();
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
        MockComponent pasted = createMockComponent(object, container, "Form", substitution);
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
    MockForm form = getRoot();
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
    final MockForm form = getRoot();
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
