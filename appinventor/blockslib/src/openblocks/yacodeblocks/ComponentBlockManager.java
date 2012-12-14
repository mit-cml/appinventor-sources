// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockGenus;
import openblocks.codeblocks.BlockStub;

import openblocks.renderable.BlockUtilities;
import openblocks.renderable.FactoryRenderableBlock;
import openblocks.renderable.RenderableBlock;
import openblocks.workspace.FactoryException;
import openblocks.workspace.FactoryManager;
import openblocks.workspace.Page;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The ComponentBlockManager handles blocks related to Young Android components.
 * It handles automatic creation of component-related blocks and renaming of those
 * blocks when the components are renamed. It maintains subsidiary data
 * structures about the relationship between components and their derived blocks.
 *
 * @author sharon@google.com (Sharon Perl)
 */

public class ComponentBlockManager implements WorkspaceListener {
  private static final boolean DEBUG = false;
  private IWorkspaceController workspaceController;
  private static final String IS_FROM_COMPONENT_TYPE_PROP_NAME = "is-from-component-type";

  /**
   * ComponentInfo collects together all the information about an instance of a component
   */
  private class ComponentInfo {
    private String uuid;  // the unique id for this component
    private String genus; // the component's block genus name
    /*
     * block ids of blocks related to this component, including drawer blocks (properties,
     * events, methods) as well as workspace blocks
     */
    private Set<Long> children;
    /*
     * Map of fully-qualified property names to tooltip descriptions, when they are specified
     * in the blocks language file. Any property not listed in this map should
     * take its description from the block genus.
     * TODO(sharon): move the property descriptions from a per-component-instance
     * map to a per-component-genus map.
     */
    private Map<String, String> propertyDescriptions;

    public ComponentInfo(String uuid, String genus) {
      this.uuid = uuid;
      this.genus = genus;
      this.children = new HashSet<Long>();
      this.propertyDescriptions = new HashMap<String, String>();
    }

    public void addChild(Long childId) {
      children.add(childId);
    }

    public Iterable<Long> getChildren() {
      return children;
    }

    public void removeChild(Long childId) {
      children.remove(childId);
    }

    public String getUuid() {
      return uuid;
    }

    public String getGenus() {
      return genus;
    }
  }

  private Workspace workspace;

  // Map from component UUIDs to component names
  private Map<String, String> uuidToComponentName = new HashMap<String, String>();

  // Map from component names to info about the component
  private Map<String, ComponentInfo> componentNameToInfo;

  // Map from component type names to info about the component
  private Map<String, ComponentInfo> componentTypeNameToInfo;

  // Set of all Genii (i.e. types) for the components in this project.
  private Map<String, Integer> compGenii = new HashMap<String, Integer>();

  /** an equals sign followed by a double quote character*/
  private static final String OPEN_QUOTE = "=\"";
  /** a double quote character */
  private static final String CLOSE_QUOTE = "\" ";

  /**
   * Creates a manager for component blocks
   * @param workspace the current workspace
   * @param workspaceController
   */
  public ComponentBlockManager(Workspace workspace,
                               IWorkspaceController workspaceController) {
    this.workspace = workspace;
    this.workspaceController = workspaceController;
    reset();
  }

  /**
   * Reset all component data structures. Call this when restoring a workspace.
   */
  public synchronized void reset() {
    componentNameToInfo = new HashMap<String, ComponentInfo>();
    componentTypeNameToInfo = new HashMap<String, ComponentInfo>();
    uuidToComponentName = new HashMap<String, String>();
    compGenii = new HashMap<String, Integer>();
  }


  /**
   * Synchronize our idea of the components with the components described
   * in the $JSON section of source. Returns true if the sync succeeded,
   * or false if it failed.
   * @param propertiesJson a JSONObject representing the components and their
   * properties.
   * @return success of the synchronization
   */
  public synchronized boolean syncFromJson(JSONObject propertiesJson) {
    String sourceType;
    JSONObject sourceProperties;
    if (DEBUG) {
      System.out.println("syncFrom Json: " + propertiesJson);
          }
    try {
      sourceType = propertiesJson.getString("Source");
      sourceProperties = propertiesJson.getJSONObject("Properties");
      workspace.setName(sourceProperties.getString("$Name"));
    } catch (JSONException e) {
      FeedbackReporter.showSystemErrorMessage("Error parsing JSON " + propertiesJson.toString());
      return false;
    }
    //System.out.println("Source is\n" + propertiesJson.toString());
    //System.out.println("Before syncing, workspace looks like\n" + describeWorkspace());

    if (sourceType.equals("Form")) {
      // The set of known uuids before syncing. As we sync each one from the properties
      // we remove it from the set. Any remaining uuids in the set after syncing represent
      // deleted components.
      HashSet<String> existingUuids = new HashSet<String>();
      for (String uuid : uuidToComponentName.keySet()) {
        existingUuids.add(uuid);
      }
      boolean success;
      try {
        success = syncComponentsFromProperties(sourceProperties, existingUuids, null);
      } catch (FactoryException e) {
        e.printStackTrace();
        return false;
      }
      // remove any remaining components
      for (String uuid : existingUuids) {
        try {
          removeComponent(uuid);
        } catch (FactoryException e) {
          e.printStackTrace();
          return false;
        }
      }
      //System.out.println("After syncing, workspace looks like\n" + describeWorkspace());
      return success;
    } else {
      //System.out.println("Source type in properties " + sourceType + " is invalid.");
      return false;
    }
  }

  public synchronized boolean loadComponents(BlockSaveFile blockSaveFile) {
    Element root = blockSaveFile.getRoot();
    reset();
    NodeList uuidMap = root.getElementsByTagName("YoungAndroidUuidMap");
    NodeList uuidEntries = uuidMap.item(0).getChildNodes();
    if (uuidEntries != null) {
      int numEntries = uuidEntries.getLength();
      // TODO(sharon): why does this for loop start from 1 instead of 0?!
      for (int i = 1; i < numEntries; i++) {
        Node uuidEntryNode = uuidEntries.item(i);
        if (!uuidEntryNode.getNodeName().equals("YoungAndroidUuidEntry")) {
          continue;
        }
        NamedNodeMap uuidEntryAttributes = uuidEntryNode.getAttributes();
        if (uuidEntryAttributes == null) {
          // System.out.println("Attributes for item " + i + " is unexpectedly null");
          continue;
        }
        Node uuidNode = uuidEntryAttributes.getNamedItem("uuid");
        Node componentIdNode = uuidEntryAttributes.getNamedItem("component-id");
        if (uuidNode == null || componentIdNode == null) {
          System.out.println("Skipping item " + i + "uuidNode=" + uuidNode + " componentIdNode="
                             + componentIdNode);
          continue;
        }
        String uuid = uuidNode.getNodeValue();
        // current format block save file has component-id equal to the
        // component's label
        Node componentGenusNode = uuidEntryAttributes.getNamedItem("component-genus");
        if (componentGenusNode == null) {
          System.out.println("item " + i + " componentId="
                             + componentIdNode.getNodeValue() + " has null component-genus");
        }
        uuidToComponentName.put(uuid, componentIdNode.getNodeValue());
        componentNameToInfo.put(componentIdNode.getNodeValue(),
                                new ComponentInfo(uuid, componentGenusNode.getNodeValue()));
        // TODO(markf): Do we need to do anything about the componentTypeNameToInfo field?
      }
    } else {
      FeedbackReporter.logError("Problem with the saved workspace: " +
                                "missing YoungAndroidUuidMap");
    }

    try {
      recreateComponents();
    } catch (FactoryException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Returns the genus of the component name as a String if the component
   * exists in this workspace.  Else, returns an empty String.
   * @param componentName the name of the component
   * @return the genus of the component with the given name, or an empty
   * string if no such component exists.
   */
  protected synchronized String getGenusFromComponentName(String componentName) {
    if (componentNameToInfo.containsKey(componentName)) {
      return componentNameToInfo.get(componentName).getGenus();
    } else if (componentTypeNameToInfo.containsKey(componentName)) {
      return componentTypeNameToInfo.get(componentName).getGenus();
    } else {
      return "";
    }
  }

  /**
   * Handle workspace events on blocks. These operations maintain the
   * representation invariants. This includes, but is not limited to, renaming,
   * connecting, and deleting blocks. For example, if a parent block is renamed,
   * we must fix the names of all its derived blocks that wouldn't be handled by
   * the normal Codeblocks mechanisms
   * @param event the workspace event
   */
  public synchronized void workspaceEventOccurred(WorkspaceEvent event)  {
    if (DEBUG) {
      System.out.println("Workspace event: " + event.toString());
    }
    if (!workspaceController.isLoadingBlocks()) {
      // None of this should happen during loading.
      int eventType = event.getEventType();

      switch (eventType) {
        case WorkspaceEvent.BLOCK_REMOVED:
          // TODO(sharon): handle removal of child blocks
          break;

        case WorkspaceEvent.BLOCK_ADDED:
          long blockID = event.getSourceBlockID();
          Block block = Block.getBlock(blockID);
          if (block != null && (event.getSourceWidget() instanceof Page)) {
           addNewChild(block);
          }
          break;

        default:
          break;
      }
    }
  }

  /**
   * Add a new component with genus type compGenus. Creates a new
   * drawer for the component containing the new derived blocks.
   * @param compGenus The name of the component's genus
   * @param compName The name of the component instance
   * @param uuid The component's uuid
   */
  private void addNewComponent(String compGenus, String compName, String uuid)
      throws FactoryException {
    if (DEBUG) {
      System.out.println("addNewComponent: " + compGenus + ", " + compName + ", " + uuid);
    }
    FactoryManager fm = Workspace.getInstance().getFactoryManager();
    fm.addBlocksDynamicDrawer(compName);
    componentNameToInfo.put(compName, new ComponentInfo(uuid, compGenus));
    uuidToComponentName.put(uuid, compName);
    createComponentDynamicBlocks(compName, fm);
    maybeAddNewComponentType(compGenus, fm);

  }

  private void maybeAddNewComponentType(String compGenus, FactoryManager fm)
      throws FactoryException {
    String compType = getDisplayNameForGenus(compGenus);
    // Add a dynamic drawer for the Genus (i.e. component type)
    Integer numComponentsOfType = compGenii.get(compType);
    if (numComponentsOfType == null || numComponentsOfType < 1) {
      componentTypeNameToInfo.put(compType, new ComponentInfo("don't care", compType));
      if (DEBUG) {
        System.out.println("addNewComponent: adding new component type drawer");
      }
      fm.addComponentsDynamicDrawer(genComponentTypeDrawerName(compType));
      compGenii.put(compType, 1);
      createComponentTypeDynamicBlocks(compType, fm);
    } else {
      compGenii.put(compType, numComponentsOfType + 1);
    }
  }

  private static String genComponentTypeDrawerName(String compType) {
    return "Any " + compType;
  }

  // TODO(markf): Check if this method is still really necessary.  It's possible that everything
  // is already renamed by the time we get to a point where this is called.
  private String getDisplayNameForGenus(String compGenus) {
    // Users don't know what a 'Form' is.  They know them as a 'Screen'.
    return "Form".equals(compGenus) ? "Screen" : compGenus;
  }

  /**
   *  Restore components after reloading a saved workspace.
   *  This recreates the drawer for the component with all of its derived blocks
   * @throws FactoryException
   */
  private void recreateComponents() throws FactoryException {
    // add a new component for each component in componentNameToInfo
    for (String name : componentNameToInfo.keySet()) {
      recreateComponent(name);
    }
    restoreComponentChildMap();
  }


  private void restoreComponentChildMap() {
    for (Block block : Block.getAllBlocks()) {
      addNewChild(block);
    }
  }

  /**
   * Adds a block to its parents child list if it belongs to a component.
   * Also, sets the block's description if it should differ from the block genus
   * description.
   * @param block The block to add.
   */
  private void addNewChild(Block block) {
    ComponentInfo componentInfo = getComponentInfo(block);
    if (componentInfo != null) {
      long blockId = block.getBlockID();
      if (! componentInfo.children.contains(blockId)) {
        componentInfo.addChild(blockId);
        String description = componentInfo.propertyDescriptions.get(block.getBlockLabel());
        if (description != null) {
          block.setBlockDescription(description);
          RenderableBlock renderable = RenderableBlock.getRenderableBlock(block.getBlockID());
          if (renderable != null) {
            renderable.setBlockToolTip(description);
          }
        }
      }
    }
  }

  // If block looks like a component-related block, return the ComponentInfo
  private ComponentInfo getComponentInfo(Block block) {
    String blockKind = block.getProperty("ya-kind");
    // don't mistake text or number blocks with "." in their label for component blocks
    if (blockKind != null && (blockKind.equals("text") || blockKind.equals("number"))) {
      return null;
    }
    String blockLabel = block.getBlockLabel();
    if (blockLabel.contains(".")) {
      String potentialComponentName = blockLabel.substring(0, blockLabel.indexOf('.'));
      if (isComponentTypeRelated(block)) {
        return componentTypeNameToInfo.get(potentialComponentName);
      } else {
        return componentNameToInfo.get(potentialComponentName);
      }
    } else if (blockKind != null && "component".equals(blockKind)) {
      return componentNameToInfo.get(blockLabel);
    }
    return null;
  }

  public static boolean isComponentTypeRelated(Block block) {
    return "true".equals(block.getProperty(IS_FROM_COMPONENT_TYPE_PROP_NAME));
  }

  /**
   * Used to recreate a component's derived drawer blocks (for restoring a
   * saved file). Assume that the uuidToComponentName and componentNameToGenus
   * maps have already been restored.
   * @param name the components name (label)
   * @throws FactoryException
   */
  private void recreateComponent(String name) throws FactoryException {
    FactoryManager fm = workspace.getFactoryManager();
    fm.addBlocksDynamicDrawer(name);
    createComponentDynamicBlocks(name, fm);
    maybeAddNewComponentType(componentNameToInfo.get(name).getGenus(), fm);
  }

  /**
   * Create the derived drawer blocks for a component. These are the block
   * types specified in the language properties section of a component
   * genus with keys of the form ya-method-N and ya-event-N. Assumes that
   * name exists in componentNameToInfo.
   * @param name The component's name
   * @param fm  The FactoryManager for the workspace
   * @throws FactoryException
   */
  private void createComponentDynamicBlocks(String name, FactoryManager fm)
      throws FactoryException {
    String componentGenus = getDisplayNameForGenus(componentNameToInfo.get(name).getGenus());
    BlockGenus genusBlk = BlockGenus.getGenusWithName(componentGenus);
    if (genusBlk == null) {
      FeedbackReporter.showSystemErrorMessage("Can't find genus with name " +
          componentGenus);
      return;
    }
    createComponentProcs(name, genusBlk, fm, "event");
    createComponentProcs(name, genusBlk, fm, "method");
    createComponentProperties(name, genusBlk, fm);
    createComponentBlock(name, fm);
  }

  /**
   * Create the derived drawer blocks for a component type. These are the block
   * types specified in the language properties section of a component
   * genus with keys of the form ya-type-method-N and ya-type-event-N.
   * @param typeName The component's type/genus
   * @param fm  The FactoryManager for the workspace
   * @throws FactoryException
   */
  private void createComponentTypeDynamicBlocks(String typeName, FactoryManager fm)
      throws FactoryException {
    BlockGenus genusBlk = BlockGenus.getGenusWithName(typeName);
    if (genusBlk == null) {
      FeedbackReporter.showSystemErrorMessage("Can't find genus with name " + typeName);
      return;
    }
    // TODO(markf): add component type event handlers that take the component as an argument
    // (with the value supplied by the system).
    createComponentTypeProcs(typeName, genusBlk, fm, "method");
    createComponentTypeProperties(typeName, genusBlk, fm);
  }

  /**
   * Create derived blocks for an event or method
   * @param componentName  The component's unique name
   * @param genusBlk The component's genus
   * @param fm   The Factory manager associated with the workspace
   * @param what Either "method" or "event"
   * @throws FactoryException
   */
  private void createComponentProcs(
      String componentName, BlockGenus genusBlk, FactoryManager fm, String what)
      throws FactoryException {
    // Create events
    String genusName;
    int idx = 1;
    ComponentInfo compInfo = componentNameToInfo.get(componentName);
    while ((genusName = genusBlk.getProperty("ya-" + what + "-" + idx)) != null) {
      Block newBlk = new Block(genusName, childName(componentName, genusName));
      RenderableBlock rendBlock = new FactoryRenderableBlock(fm, newBlk.getBlockID());
      fm.addDynamicBlock(rendBlock, componentName);
      compInfo.addChild(rendBlock.getBlockID());
      idx++;
    }
  }

  /**
   * Construct the name of a derived block as parentLabel.childLabel, where
   * childLabel is the initial label of childGenus.
   * @param parentLabel  Label of parent block
   * @param childGenus   Name of child's genus
   * @return the name with dot object
   */
  private String childName(String parentLabel, String childGenus) {
    BlockGenus genus = BlockGenus.getGenusWithName(childGenus);
    return parentLabel + "." + genus.getInitialLabel();
  }

  /**
   * Create a component block that will be a reference to the component. Add the created
   * block to componentNameToChildIds map
   * @param componentName  The component's name
   * @param fm FactoryManager
   * @throws FactoryException
   */
  private void createComponentBlock(String componentName, FactoryManager fm)
      throws FactoryException {
    Block propBlock = new Block("component", componentName, true);
    fm.addDynamicBlock(new FactoryRenderableBlock(fm, propBlock.getBlockID()),
                                   componentName);
    componentNameToInfo.get(componentName).addChild(propBlock.getBlockID());
  }

  /**
   * @param componentType
   * @param genusBlk The component's genus/type
   * @param fm   The Factory manager associated with the workspace
   * @param what Either "method" or "event"    @throws FactoryException
   */
 private void createComponentTypeProcs(String componentType, BlockGenus genusBlk, FactoryManager fm,
                                       String what) throws FactoryException {
    // Create events
    String genusName;
    int idx = 1;
    ComponentInfo compInfo = componentTypeNameToInfo.get(componentType);
    while ((genusName = genusBlk.getProperty("ya-type-" + what + "-" + idx)) != null) {
      Block newBlk = new Block(genusName, childName(componentType, genusName));
      RenderableBlock rendBlock = new FactoryRenderableBlock(fm, newBlk.getBlockID());
      fm.addAdvancedBlock(rendBlock, genComponentTypeDrawerName(componentType));
      compInfo.addChild(rendBlock.getBlockID());
      idx++;
    }
 }

  /**
   * Create the derived property blocks specified in the component's language
   * spec section with keys of the form "ya-prop-N". The value associated with
   * a key specifies the name of the property and its genus. Adds the created
   * property blocks to componentNameToChildIds map
   * @param componentName  The component's name
   * @parapm genusBlk The component's genus
   * @param fm   FactoryManager
   * @throws FactoryException
   */
  private void createComponentProperties(String componentName, BlockGenus genusBlk,
                                         final FactoryManager fm)
    throws FactoryException {
    String propSpec;
    ComponentInfo compInfo = componentNameToInfo.get(componentName);
    int idx = 1;
    if (DEBUG) {
      System.out.println("Creating dynamic prop blocks for component: " + componentName +
                         ". Block genus: " + genusBlk.toString());
    }
    while ((propSpec = genusBlk.getProperty("ya-prop-" + idx)) != null) {
      // propSpec should have the form: "prop-name/prop-block-genus/type/optional-description"
      // create a property block but don't put it on the page because
      // we no longer show properties on the page. Do create getter and
      // setter stubs for all properties.
      String[] parts = propSpec.split("/");
      String description = null;
      if (parts.length > 3 && parts[3].length() > 0) {
        description = parts[3];
      }
      compInfo.addChild(createProperty(componentName, compInfo.getGenus(),
                                       parts[0], parts[1], parts[2], description, fm));
      if (description != null) {
        // store the property description under the string that is the label
        // of the property block, for easier lookup later
        compInfo.propertyDescriptions.put(makePropertyBlockLabel(componentName, parts[0]),
            description);
      }
      idx++;
    }
  }

  /**
   * Create the derived property blocks specified in the component's language
   * spec section with keys of the form "ya-prop-N". The value associated with
   * a key specifies the name of the property and its genus.
   * @parapm genusBlk The component's genus
   * @param componentTypeName
   * @param fm   FactoryManager
   * @throws FactoryException
   */
  private void createComponentTypeProperties(String componentTypeName, BlockGenus genusBlk,
                                             final FactoryManager fm)
      throws FactoryException {
    String propSpec;
    ComponentInfo compTypeInfo = componentTypeNameToInfo.get(componentTypeName);
    int idx = 1;
    if (DEBUG) {
      System.out.println("Creating dynamic prop blocks for component type: " + componentTypeName +
                         ". Block genus: " + genusBlk.toString());
    }
    while ((propSpec = genusBlk.getProperty("ya-prop-" + idx)) != null) {
      // propSpec should have the form: "prop-name/prop-block-genus/type/optional-description"
      String[] parts = propSpec.split("/");
      String description = null;
      if (parts.length > 3 && parts[3].length() > 0) {
        description = parts[3];
      }
      compTypeInfo.addChild(createPropertyForComponentType(componentTypeName, componentTypeName,
                                                       parts[0], parts[1], parts[2],
                                                       "No description yet", fm));
      idx++;
    }
  }

  // Note: description may be null
  private Long createProperty(String componentName, String componentGenus, String propertyName,
                              String propertyGenus, String type, String description,
                              FactoryManager fm)
      throws FactoryException {
    Block propBlock = new Block(propertyGenus, makePropertyBlockLabel(componentName, propertyName),
        true);
    // create rules for this component property
    BlockRules.createPropertyRules(componentGenus, propertyName, type);
    addPropertyBlockStubsOfGenus(
        componentName, description, fm, propBlock, "componentGetter", false);
    addPropertyBlockStubsOfGenus(
        componentName, description, fm, propBlock, "componentSetter", false);
    return propBlock.getBlockID();
  }

  private void addPropertyBlockStubsOfGenus(String componentName, String description,
                                            FactoryManager fm, Block propBlock, String genus,
                                            boolean forComponentType)
      throws FactoryException {
    for(BlockStub stub : propBlock.getFreshStubsOfGenus(genus)) {
      if (description != null) {
        stub.setBlockDescription(description);
      }
      if (forComponentType) {
        // create the block stubs of the specified genus and add them the component type's drawer
        fm.addAdvancedBlock(new FactoryRenderableBlock(fm, stub.getBlockID()),
                            genComponentTypeDrawerName(componentName));
      } else {
        // create the block stubs of the specified genus and add them the component type's drawer
        fm.addDynamicBlock(new FactoryRenderableBlock(fm, stub.getBlockID()),
                                       componentName);

      }
    }
  }

  // Note: description may be null
  private Long createPropertyForComponentType(String componentName, String componentGenus,
                                              String propertyName, String propertyGenus,
                                              String type, String description, FactoryManager fm)
      throws FactoryException {
    Block propBlock = new Block(propertyGenus, makePropertyBlockLabel(componentName, propertyName),
        true);
    // create rules for this component property
    BlockRules.createPropertyRules(componentGenus, propertyName, type);
    addPropertyBlockStubsOfGenus(componentName, description, fm, propBlock, "componentTypeGetter",
                                 true);
    addPropertyBlockStubsOfGenus(componentName, description, fm, propBlock, "componentTypeSetter",
                                 true);
    return propBlock.getBlockID();
  }

  private String describeWorkspace() {
    StringBuilder sb = new StringBuilder();
    sb.append("uuidToComponentName: " + uuidToComponentName.toString() + '\n');
    sb.append("componentNameToInfo:");
    for (String name : componentNameToInfo.keySet()) {
      sb.append(" " + componentNameToInfo.get(name));
    }
    sb.append("\n");
    sb.append("genusToPropertyRules: " + BlockRules.genusToPropertyRules.toString() + '\n');
    return sb.toString();
  }

  /**
   * Synchronize components from a JSON object. As each component is synced, remove it from
   * uuidSet if it was in there to start with.
   * @param sourceProperties JSON representation of the properties
   * @param oldUuidSet the set of uuids known before the sync. Upon return contains just the set
   *        of uuids not synced.
   * @param newUuidSet the set of uuids that should exist after the sync is done. Should
   *   be null on initial call. Used in recursive calls.
   * @return success of the sync
   * @throws FactoryException
   */
  private boolean syncComponentsFromProperties(JSONObject sourceProperties,
      Set<String> oldUuidSet, Set<String> newUuidSet)
      throws FactoryException {
    if (DEBUG) {
      System.out.println("syncComponentsFromProperties: " + sourceProperties);
    }
    JSONArray components;
    try {
      if (sourceProperties.has("$Components")) {
        components = sourceProperties.getJSONArray("$Components");
        Set<JSONObject> componentSet = new HashSet<JSONObject>();
        newUuidSet = new HashSet<String>();
        for (int i = 0; i < components.length(); i++) {
          JSONObject component = components.getJSONObject(i);
          componentSet.add(component);
          newUuidSet.add(component.getString("Uuid"));
        }
        for (JSONObject component: componentSet) {
          if (! syncComponentsFromProperties(component, oldUuidSet, newUuidSet)) {
            FeedbackReporter.showSystemErrorMessage("Warning: couldn't sync components");
            return false;
          }
        }
      }
      // sync the component
      try {
        if (!syncComponent(sourceProperties, oldUuidSet, newUuidSet)) {
          FeedbackReporter.showSystemErrorMessage("Warning: couldn't sync component " +
              sourceProperties.getString("$Name"));
        }
      } catch (CodeblocksException e) {
        FeedbackReporter.showSystemErrorMessage(
            "Unrecoverable error: multiple components named " +
            sourceProperties.getString("$Name"));
        return false;
      }
    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Synchronize a single component from a JSON component representation.
   * @param component JSON representation of this component
   * @param oldUuidSet the exisiting set of uuids
   * @param newUuidSet the set of uuids not yet synced
   * @return the success of the sync
   * @throws FactoryException
   * @throws CodeblocksException if an unrecoverable error occurs during sync
   *    (two components with the same name)
   */
  private boolean syncComponent(JSONObject component, Set<String> oldUuidSet,
      Set<String> newUuidSet) throws FactoryException, CodeblocksException, JSONException {
    String uuid;
    String newName;
      if (DEBUG) {
        System.out.println("syncComponent: " + component);
            }
    try {
      uuid = component.getString("Uuid");
      newName = component.getString("$Name");
    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
    if (componentNameToInfo.containsKey(newName) &&
        !componentNameToInfo.get(newName).getUuid().equals(uuid)) {
      // Something is going on with renaming components, since the component
      // that currently has the same name as the one we're trying to sync
      // doesn't have the same uuid. We'll either delete the existing component
      // (if it isn't supposed to exist after the sync) or temporarily give
      // it a unique name so that we avoid conflicts. If it is supposed
      // to stay around it should eventually get renamed to its proper new
      // name during the sync process
      String oldUuid = componentNameToInfo.get(newName).getUuid();
      if (oldUuidSet.contains(oldUuid)) {
        // we haven't yet processed the old component
        if (newUuidSet.contains(oldUuid)) {
          // this component will still exist after the sync. Temporarily
          // give it a unique name to avoid conflicts. It will eventually
          // be processed and renamed to its correct new name.
          String tempName = newName + "0";
          while (componentNameToInfo.containsKey(tempName)) {
            tempName = tempName + "0";
          }
          if (DEBUG) {
            System.out.println("Temporarily renaming old component named " + newName +
                " with uuid " + oldUuid + " to " + tempName +
                " to avoid conflict with component with uuid " + uuid);
          }
          // Could this rename end up being visible to users temporarily? I think
          // it is very unlikely that users could see the intermediate state. This
          // code is most likely to be executed if codeblocks has been closed
          // during some component name changes and is just reopening. If it ends up
          // being a problem we can fix it later (not easy to fix).
          renameComponent(oldUuid, newName, tempName);
        } else {
          // this component will be removed after the sync. Remove it now
          // to avoid name conflicts.
          if (DEBUG) {
            System.out.println("Removing old component named " + newName +
                " with uuid " + oldUuid);
          }
          removeComponent(oldUuid);
          oldUuidSet.remove(oldUuid);
        }
      } else {
        // uh oh! We already processed the old component and it still has
        // the same name as another component. This is an unrecoverable error!
        throw new CodeblocksException("Found two components named \"" +
            newName + "\": uuids are " + oldUuid + " and " + uuid);
      }
    }
    // at this point there should be no existing component named newName!

    if (uuidToComponentName.containsKey(uuid)) {
      // component already exists
      String oldName = uuidToComponentName.get(uuid);
      if (!componentNameToInfo.containsKey(oldName)) {
        FeedbackReporter.showSystemErrorMessage("Component " + oldName + " cannot be found");
        uuidToComponentName.remove(uuid);
        return false;
      }
      if (DEBUG) {
        System.out.println("Syncing component with old name " + oldName);
      }
      if (!newName.equals(oldName)) {
        // component's name has changed. need to update drawer names, block names, and maps
        renameComponent(uuid, oldName, newName);
      }
      oldUuidSet.remove(uuid);
    } else {
      String componentGenus = component.getString("$Type");
      if (DEBUG) {
        System.out.println("Adding new component with name " + newName + " and uuid " + uuid);
      }
      addNewComponent(componentGenus, newName, uuid);
    }
    return true;
  }

  /**
   * Rename the component identified by uuid from oldName to newName.
   * Upon return, the component's child blocks will have had their labels changed,
   * the component's drawer will be renamed to newName, and the component maps will
   * be updated.
   * @param uuid    unique id identifying the component
   * @param oldName old name (the one currently used in the component maps)
   * @param newName new name
   * @throws FactoryException
   */
  private void renameComponent(String uuid, String oldName, String newName) throws FactoryException {
    if (DEBUG) {
      System.out.println("Renaming component from " + oldName + " to " + newName);
    }
    // update the children
    for (Long childId : componentNameToInfo.get(oldName).getChildren()) {
      Block childBlk = Block.getBlock(childId);
      if (childBlk == null) {
        System.out.println("renameComponent: childBlk is null for id " + childId);
        continue;
      }
      String childLabel = childBlk.getBlockLabel();
      String newChildLabel;
      String childPrefix = childBlk.getLabelPrefix();
      if (childPrefix == null) { // can getLabelPrefix() ever return null?
        childPrefix = "";
      }
      // If child label starts with the old component name, rewrite it to have the new name.
      if (!childLabel.matches(childPrefix + newName + "\\..*")) {
        if (!childLabel.contains(".")) {
          // childBlk is the block representing the whole component.
          newChildLabel = newName;
          // This call on setBlockLabel will go to the Block class.
          childBlk.setBlockLabel(newChildLabel);
        } else {
          newChildLabel = newName + childLabel.substring(childLabel.indexOf('.'));
          // childBlk is a component method, event handler, property setter, or property getter
          // This call on setBlockLabel will go to one of the subclasses ComponentBlock
          // or ComponentBlockStub
          childBlk.setBlockLabel(newChildLabel);
        }
        if (DEBUG) {
          System.out.println("renameComponent: changing block label from "
              + childLabel + " to " + childPrefix + newChildLabel);
        }
        RenderableBlock rendChildBlock = RenderableBlock.getRenderableBlock(childId);
        if (rendChildBlock != null) {
          rendChildBlock.repaintBlock();
        }
      }
    }
    componentNameToInfo.put(newName, componentNameToInfo.remove(oldName));
    uuidToComponentName.put(uuid, newName);
    workspace.getFactoryManager().renameBlocksDynamicDrawer(oldName, newName);
    workspaceController.componentRenamed(oldName, newName);
  }

  private void removeComponent(String uuid) throws FactoryException {
    String name = uuidToComponentName.get(uuid);
    ComponentInfo componentInfo = componentNameToInfo.get(name);
    FactoryManager factoryManager = workspace.getFactoryManager();
    factoryManager.removeBlocksDynamicDrawer(name);
    // If necessary, remove the dynamic drawer for the Genus (i.e. component type)
    String compGenus = componentInfo.getGenus();
    Integer numComponentsOfGenusObj = compGenii.get(compGenus);
    if (numComponentsOfGenusObj != null) {
      int numComponentsOfGenus = numComponentsOfGenusObj;
      numComponentsOfGenus--;
      compGenii.put(compGenus, numComponentsOfGenus);
      // If there are no more instances of this component type, remove its dynamic drawer and
      // delete its child blocks
      if (numComponentsOfGenus < 1) {
        factoryManager.removeComponentsDynamicDrawer(genComponentTypeDrawerName(compGenus));
        deleteChildBlocks(componentTypeNameToInfo.get(compGenus));
        componentTypeNameToInfo.remove(compGenus);
      }
    }
    deleteChildBlocks(componentInfo);
    componentNameToInfo.remove(name);
    uuidToComponentName.remove(uuid);
    workspaceController.componentRemoved(name);
  }

  private void deleteChildBlocks(ComponentInfo componentInfo) {
    if (DEBUG) {
      System.out.println("Deleting children of " + componentInfo.toString() + " which has "
                         + componentInfo.children.size() + " eligible children.");
    }
    for (Long childId : componentInfo.children) {
      if (!BlockUtilities.isNullBlockInstance(childId)) {
        RenderableBlock rendBlock = RenderableBlock.getRenderableBlock(childId);
        if (DEBUG) {
          System.out.println("Potentially deleting child block " + rendBlock.getBlockID());
        }
        // Only delete the block if it hasn't already been deleted because it was connected to
        // another deleted block.
        if (rendBlock.getParentWidget() != null) {
          if (BlockUtilities.deleteBlockUserConfirm(rendBlock,
                                                    false /* only confirm proc decl deletes */)) {
            BlockUtilities.deleteBlock(rendBlock, false);
          }
        }
      }
    }
  }

  protected synchronized String getSaveString() {
    StringBuilder saveString = new StringBuilder();
    saveString.append("<YoungAndroidMaps>");
    saveString.append("<YoungAndroidUuidMap>\n");
    for (String uuid : uuidToComponentName.keySet()) {
      saveString.append("<YoungAndroidUuidEntry ");
      appendAttribute("uuid", uuid, saveString);
      String compName = uuidToComponentName.get(uuid);
      appendAttribute("component-id", compName, saveString);
      String genus = componentNameToInfo.get(compName).getGenus();
      appendAttribute("component-genus", genus, saveString);
      appendAttribute("component-version", "" + WorkspaceUtils.getComponentVersion(genus),
          saveString);
      saveString.append("></YoungAndroidUuidEntry>\n");
    }
    saveString.append("</YoungAndroidUuidMap>\n");
    saveString.append("</YoungAndroidMaps>\n");
    return saveString.toString();
  }

  private void appendAttribute(String att, String value, StringBuilder buf) {
    buf.append(att);
    buf.append(OPEN_QUOTE);
    buf.append(value);
    buf.append(CLOSE_QUOTE);
  }

  // The following methods are for testing

  // Returns the set of all known component genuses
  public synchronized Set<String> getComponentGenuses() {
    Set<String> genuses = new HashSet<String>();
    for (String genusName : BlockGenus.getAllGenuses()) {
      String kind = BlockGenus.getGenusWithName(genusName).getProperty("ya-kind");
      if (kind != null && kind.equals("component")) {
        genuses.add(genusName);
      }
    }
    return genuses;
  }

  public synchronized Set<String> getComponentNames() {
    return componentNameToInfo.keySet();
  }

  private String makePropertyBlockLabel(String componentName, String propertyName) {
    return componentName + "." + propertyName;
  }
}
