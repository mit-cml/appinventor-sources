// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.workspace;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockStub;
import openblocks.codeblockutil.CGraphite;
import openblocks.codeblockutil.Canvas;
import openblocks.codeblockutil.Navigator;
import openblocks.renderable.FactoryRenderableBlock;
import openblocks.renderable.RenderableBlock;
import openblocks.yacodeblocks.WorkspaceControllerHolder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * ***********************OVERVIEW**************************
 * The FactoryManager manages all block factories in the workspace.
 * It has three main functions:  to control and display all factories
 * in one simple UI design, to manage the additions of new drawers,
 * and to add blocks to those drawers appropriately.
 *
 * The FactoryManager manages two factories: the static factory
 * and dynamic factory.  Each factory has a set of drawers.  NO TWO
 * DRAWERS WITHIN ANY FACTORY MAY SHARE the same name.
 *
 * ********************FACTORY STRUCTURE***********************
 *
 * Let's take a look into the structure of a factory.  Factory is
 * a pallete that sits on the far left side of the workspace.
 * It has a bunch of drawers that slides up and down.  Each
 * drawer contains a bunch of related blocks that can be dragged
 * out.
 *
 * The FactoryManager has two types of drawers: static and dynamic.
 * To add, remove, rename, drawers of either type, users should
 * invoke the name that specifies a particular drawer. Users
 * may also add blocks to the drawers or retrieve the set of blocks
 * that each drawer holds.
 *
 * *************IMPLEMENTATION DETAIL******************
 *
 * How the FactoryManager implements this UI is implementation
 * dependent.  Right now, it uses the Navigator-Explorer-Canvas deisgn.
 * Clients of the FactoryManager should know nothing about the
 * internal GUIs used to control the interface.  Internally,
 * a Canvas (rather than an instance of Drawer) is created for every
 * "drawer" that the user wishes to add.  But this is an implementation
 * detail that the user should not be bothered with.  All the user should
 * know is that a "drawer" specified by some String object was created.
 * The handling of the drawers themselves are dealt with internally.
 * In a previous design of the factories, developers had to create
 * instance of Drawers and pass them along to the the factories.
 * In the NEW design, we remove that burden from the developer and allow the
 * developer to access drawers by calling its name only.  This may
 * limit extensibility but keeps the system more robust.
 *
 * *********************A WORD ON DRAWER**********************
 * Please note that the word "drawer" as it is used by the
 * FactoryManager refers to the object that holds blocks.
 * A factory holds a bunch of drawers, which in turn holds
 * a bunch of blocks.
 *
 * Please do not mix this definition with the CSwing Drawer class.
 * A CSwing Drawer is a low-level component that is used
 * in a CSwing Explorer.  Here, when the documentation refers
 * to drawers, it is NOT referring to the CSwing Drawer.  Rather,
 * when we say "drawer", we are referring to that object that holds blocks.
 *
 * *****************NAMING OF DRAWERS*************************
 * Each factory may have only ONE drawer with a particular name.
 * Two different factories may NOT share a name.   If we have
 * a static drawer named "FOO", we may not have another drawer named
 * "FOO" in the dynamic drawers.
 *
 *
 */
public class FactoryManager implements WorkspaceWidget, ComponentListener, WorkspaceListener{
  /** The string identifier of static drawers */
  private static final String STATIC_NAME = "Built-In";
  /** The string identifier of dynamic drawers */
  private static final String BLOCKS_DYNAMIC_NAME = "My Blocks";
  /** The string identifier of the components dynamic drawer */
  private static final String COMPONENTS_DYNAMIC_NAME = "Advanced";
  /** The string identifier of the definitions drawer, also used in FactoryCanvas */
  public static final String DEFINITIONS_NAME = "My Definitions";
  /** The high-level UI that manages the controlling of internal CWsing components */
  private Navigator navigator;
  /** The high-level UI widget that manages switching between different factories */
  private JComponent factorySwitcher;
  /** the set of static drawers */
  private List<FactoryCanvas> staticCanvases;
  /** The sorted set of dynamic drawers for components */
  private TreeSet<FactoryCanvas> dynamicCanvases;
  /** The sorted set of dynamic drawers for component types */
  private TreeSet<FactoryCanvas> advancedCanvases;
  private static Color ADVANCED_DRAWER_COLOR = new Color(252, 233, 157);
  private static Color BUILTIN_DRAWER_COLOR = new Color(248, 208, 143);

  private static final boolean DEBUG = false;

  /**
   * Constructs new Factorymanager
   */
  public FactoryManager(){
    navigator = new Navigator();
    navigator.getJComponent().setPreferredSize(new Dimension(175,600));
    navigator.addExplorer(STATIC_NAME, BUILTIN_DRAWER_COLOR);
    navigator.addExplorer(BLOCKS_DYNAMIC_NAME);
    navigator.addExplorer(COMPONENTS_DYNAMIC_NAME, ADVANCED_DRAWER_COLOR);
    factorySwitcher = new JPanel(new BorderLayout()) {
      // draw the line at the bottom of the tabs
      public void paint(Graphics g) {
        g.setColor(CGraphite.button_hoverborder);
        g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
        super.paint(g);
      }
    };
    factorySwitcher.add(navigator.getSwitcher(), BorderLayout.SOUTH);
    factorySwitcher.setOpaque(false);
    factorySwitcher.setPreferredSize(new Dimension(180, 40));
    navigator.getJComponent().addComponentListener(this);
    initCanvases();
  }

  private void initCanvases() {
    staticCanvases = new ArrayList<FactoryCanvas>();
    final Comparator<FactoryCanvas> canvasComparator = new FactoryCanvasComparator();
    dynamicCanvases = new TreeSet<FactoryCanvas>(canvasComparator);
    advancedCanvases = new TreeSet<FactoryCanvas>(canvasComparator);
    if (DEBUG) {
      System.out.println("Initializing FactoryManager canvases");
    }
    if (!WorkspaceControllerHolder.isHeadless()) {
      navigator.setCanvas(staticCanvases, STATIC_NAME);
      navigator.setCanvas(dynamicCanvases, BLOCKS_DYNAMIC_NAME);
      navigator.setCanvas(advancedCanvases, COMPONENTS_DYNAMIC_NAME);
    }
  }
  /**
   * Resets FactoryManager
   */
  public void reset(){
    initCanvases();
  }

  /**
   * prints an error message in red without ending the run process.
   * For debuggin purposes
   * @param m
   */
  private void printError(String m){
    new RuntimeException(m).printStackTrace();
  }

  /////////////////////
  //Reforming methods//
  /////////////////////

  public void componentResized(ComponentEvent e){
    this.relayoutFactory();
    //this.relayoutBlocks();
  }
  public void componentHidden(ComponentEvent e){}
  public void componentMoved(ComponentEvent e){}
  public void componentShown(ComponentEvent e){}
  /**
   * Relayout all factories
   */
  private void relayoutFactory(){
    this.navigator.reformView();
  }
  /**
   * Relayout all the drawers
   */
  public void relayoutBlocks(){
    for(FactoryCanvas canvas : staticCanvases) {
      canvas.layoutBlocks();
    }
    for(FactoryCanvas canvas : dynamicCanvases) {
      canvas.layoutBlocks();
    }
    for(FactoryCanvas canvas : advancedCanvases) {
      canvas.layoutBlocks();
    }
  }

  public Navigator getNavigator(){
    return this.navigator;
  }

  //////////////////
  //Drawer Methods//
  //////////////////


  /**
   * @return the set of searchable contianers in all factories
   */
  public Collection<SearchableContainer> getSearchableContainers(){
    Collection<SearchableContainer> containers = new HashSet<SearchableContainer>();
    for(SearchableContainer con : this.staticCanvases){
      containers.add(con);
    }
    for(SearchableContainer con : this.dynamicCanvases){
      containers.add(con);
    }
    for(SearchableContainer con : this.advancedCanvases){
      containers.add(con);
    }
    return containers;
  }

  /**
   * @return an array containing the set of drawers
   * in no particular order.  If no drawers exists,
   * then an empty set is returned. The return value
   * MAY NOT BE NULL.
   */
  public Collection<String> getStaticDrawers(){
    // kengoldman: changed from HashSet to ArrayList for consistent order
    Collection<String> drawers = new ArrayList<String>();
    for(Canvas canvas : this.staticCanvases){
      String name = canvas.getName();
      if(name == null){
        this.printError("Drawer name may not be null");
      }else if(drawers.contains(name)){
        this.printError("Duplicate Drawer name!");
      }else{
        drawers.add(name);
      }
    }
    return drawers;
  }

  /**
   * Returns the color of the first static drawer with the given name.
   *
   * @param drawerName name of the drawer
   * @return the Color of the static drawer or null if no such drawerName
   * exists.
   */
  public Color getStaticDrawerColor(String drawerName){
    for (Canvas canvas : this.staticCanvases){
      if (canvas.getName().equals(drawerName)){
        return canvas.getColor();
      }
    }
    return null;
  }

  /**
   * Switch view to the set of static drawers
   */
  public void viewStaticDrawers(){
    this.navigator.setView(STATIC_NAME);
  }

  /**
   * Switch view to the set of dynamic drawers for blocks
   *
   */
  public void viewBlocksDynamicDrawers(){
    this.navigator.setView(BLOCKS_DYNAMIC_NAME);
  }

  /**
   * Switch view to the set of dynamic drawers for components
   *
   */
   public void viewComponentsDynamicDrawers(){
    this.navigator.setView(COMPONENTS_DYNAMIC_NAME);
  }

  /**
   * may not have two drawers with the same name
   * @param canvases the collection of canvases to check against
   * @param name
   * @param position
   * @return true if and only if the following conditions are met:
   *                    -specified name is not null,
   *                    -0<=position<canvases.size
   *                    -there is NO other drawers with the same name as the
   *                     specified name in the given set of canvases
   */
  private boolean isValidDrawer(Collection<FactoryCanvas> canvases, String name, int position){
    if (position < 0) return false;
    if (position > canvases.size()) return false;
    for(Canvas canvas : canvases) {
      if (canvas.getName().equals(name)) return false;
    }
    return true;
  }
  /**
   * Adds a static drawer if no drawer with the specified name already exists.
   * If one alreaedy exist, then do ntohing.  If the name is null, do nothing
   * @param name - name os drawer, may not be null
   * @param color
   * @throws FactoryException if drawer "name" already exists
   *
   * @requires name != null &&
   *   drawer to not already exist in BOTH static and dynamic set
   */
  public void addStaticDrawer(String name, Color color) throws FactoryException {
    this.addStaticDrawer(name, staticCanvases.size(), color);
  }
  /**
   * Adds a static drawer if no drawer with the specified name already exists.
   * If one already exists, then do nothing.  If the name is null, do nothing
   * @param name - name of drawer, may not be null
   * @param color
   * @param position
   * @throws FactoryException if "name" is an existing drawer or if position
   *   is out of range
   *
   * @requires name != null &&
   *   drawer to not already exist in BOTH static and dynamic set
   */
  public void addStaticDrawer(String name, int position, Color color) throws FactoryException {
    if (isValidDrawer(staticCanvases, name, position)) {
      FactoryCanvas canvas = new FactoryCanvas(name, color);
      this.staticCanvases.add(position, canvas);
      this.navigator.addCanvas(canvas, position, STATIC_NAME);
    } else {
      throw new FactoryException(
          "Invalid Drawer: trying to add a drawer that already exists: "
          + name);
    }
  }

  /**
   * Adds a dynamic drawer if no drawer with the specified name already exists.
   * If one already exists, then do nothing.  If the name is null, do nothing
   * @param name - name os drawer, may not be null
   * @param canvas
   * @param dynamicDrawerName
   * @param canvasList
   * @param color
   * @throws FactoryException of drawer "name" already exists
   *
   * @requires name != null &&
   *   drawer to not already exist in BOTH static and dynamic set
   */
  private boolean addDynamicDrawerInternal(String name,
                                           FactoryCanvas canvas,
                                           String dynamicDrawerName,
                                           Collection<FactoryCanvas> canvasList,
                                           Color color)
      throws FactoryException {

    if (isValidDrawer(canvasList, name, 0)){
      if (canvas == null) {
        canvas = new FactoryCanvas(name, color);
      } else {
        canvas.setName(name);
      }
      canvasList.add(canvas);
      this.navigator.addCanvas(canvas, drawerIndex(canvasList, canvas), dynamicDrawerName);
      return true;
    } else {
      throw new FactoryException(
          "Invalid Drawer: trying to add a drawer that already exists: "
          +name);
    }
  }
  // note that the collection of canvases should be ordered!
  private int drawerIndex(Collection<FactoryCanvas> canvases, FactoryCanvas canvas) {
    int i = 0;
    for (FactoryCanvas item: canvases) {
      if (item == canvas) {
        return i;
      }
      i++;
    }
    throw new RuntimeException("Couldn't find drawer " + canvas.getName() + " in set");
  }

  public void addBlocksDynamicDrawer(String name) throws FactoryException {
    addDynamicDrawerInternal(name, null, BLOCKS_DYNAMIC_NAME, dynamicCanvases,
                             FactoryCanvas.DEFAULT_CANVAS_COLOR);
  }

  public void addComponentsDynamicDrawer(String name) throws FactoryException {
    addDynamicDrawerInternal(name, null, COMPONENTS_DYNAMIC_NAME, advancedCanvases,
                             ADVANCED_DRAWER_COLOR);
  }

  /**
   * Renames drawer from oldName to newName.  Only perform this action if:
   *            (1) there exists a drawer specified by oldName,
   *            (2) there exists no drawers specified by newName
   *            (3) oldName and newName != null
   * @param oldName
   * @param newName
   * @throws FactoryException
   *
   * @requires oldName != null &&
   *   drawer with newName exists in EITHER static or dynamic set &&
   *   drawer with newName to not already exist in BOTH static and dynamic set
   */
  public void renameStaticDrawer(String oldName, String newName) throws FactoryException {
    //check rep
    if( oldName == null || newName ==null){
      throw new FactoryException("Drawers may not have a null instance for a name.");
    }
    for(FactoryCanvas duplicateCanvas : this.staticCanvases){
      if (duplicateCanvas.getName().equals(newName)){
        throw new FactoryException("Drawer already exists with name: " + newName);
      }
    }
    //rename
    for(FactoryCanvas oldCanvas : this.staticCanvases){
      if (oldCanvas.getName().equals(oldName)){
        oldCanvas.setName(newName);
        return;
      }
    }
    throw new FactoryException("No Drawer was found with the name: "+oldName);
  }
  public void renameBlocksDynamicDrawer(String oldName, String newName) throws FactoryException {
    //check rep
    if( oldName == null || newName ==null){
      throw new FactoryException("Drawers may not have a null instance for a name.");
    }
    FactoryCanvas canvas =
        removeDynamicDrawerInternal(oldName, dynamicCanvases, BLOCKS_DYNAMIC_NAME);
    if (canvas != null) {
      // make sure to use the same canvas!
      addDynamicDrawerInternal(newName, canvas, BLOCKS_DYNAMIC_NAME, dynamicCanvases,
                               FactoryCanvas.DEFAULT_CANVAS_COLOR);
    }
    return;
  }
  /**
   * removes drawer with specified name.  Only perform this action if:
   *            (1) there exists a drawer specified by name,
   *            (3) name != null
   * @param name
   * @throws FactoryException
   *
   * @requires name != null && there exists a drawer with specified name
   */
  public void removeStaticDrawer(String name) throws FactoryException {
    FactoryCanvas canvas = null;
    for(FactoryCanvas c : this.staticCanvases){
      if (c.getName().equals(name)){
        canvas = c;
      }
    }
    if(canvas != null){
      this.staticCanvases.remove(canvas);
      if (!WorkspaceControllerHolder.isHeadless()) {
        this.navigator.setCanvas(this.staticCanvases, STATIC_NAME);
      }
      return;
    }
    throw new FactoryException("No Drawer found with name: "+name);
  }
  private FactoryCanvas removeDynamicDrawerInternal(String name,
                                                    Collection<FactoryCanvas> canvases,
                                                    String explorerName)  throws FactoryException {
    FactoryCanvas canvas = null;
    for(FactoryCanvas c : canvases) {
      if (c.getName().equals(name)) {
        canvas = c;
      }
    }
    if (canvas != null) {
      canvases.remove(canvas);
      if (!WorkspaceControllerHolder.isHeadless()) {
        navigator.setCanvas(canvases, explorerName);
      }
      return canvas;
    }
    throw new FactoryException("No Drawer found with name: "+name);
  }

  public void removeBlocksDynamicDrawer(String name) throws FactoryException {
    removeDynamicDrawerInternal(name, dynamicCanvases, BLOCKS_DYNAMIC_NAME);
  }

  public void removeComponentsDynamicDrawer(String name) throws FactoryException {
    removeDynamicDrawerInternal(name, advancedCanvases, COMPONENTS_DYNAMIC_NAME);
  }

  /////////////////
  //Block Methods//
  /////////////////

  /**
   * @throws FactoryException
   * @return set of blocks found in drawer with the specified name.
   *   If no blocks are found in the drawer, return an empty set.
   *   If no Drawers are found with specified name, return empty set.
   */
  public Collection<RenderableBlock> getStaticBlocks(String name) throws FactoryException {
    ArrayList<RenderableBlock> blocks = new ArrayList<RenderableBlock> ();
    for(FactoryCanvas canvas : this.staticCanvases){
      if(canvas.getName().equals(name)){
        blocks.addAll(canvas.getBlocks());
        return blocks;
      }
    }
    throw new FactoryException("Drawer not found: "+name);
  }

  /**
   * @return all blocks in all drawers.  If no blocks found, return
   *   an empty set.  Ifno drawers exists in either factories,
   *   return an empty set.
   */
  public Collection<RenderableBlock> getBlocks() {
    ArrayList<RenderableBlock> blocks = new ArrayList<RenderableBlock> ();
    for(FactoryCanvas canvas : staticCanvases) {
      blocks.addAll(canvas.getBlocks());
    }
    for(FactoryCanvas canvas : dynamicCanvases) {
      blocks.addAll(canvas.getBlocks());
    }
    for(FactoryCanvas canvas : advancedCanvases) {
      blocks.addAll(canvas.getBlocks());
    }
    return blocks;
  }
  /**
   * Add blocks to drawer if drawer can be found.  Do nothing
   * if no drawer if specified name is found.
   * @param block
   * @param drawer
   * @throws FactoryException
   */
  public void addStaticBlock(RenderableBlock block, String drawer) throws FactoryException {
    for(FactoryCanvas canvas : this.staticCanvases){
      if (canvas.getName().equals(drawer)){
        if(block == null || Block.NULL.equals(block.getBlockID())){
          throw new FactoryException("Attempting to add a null instance of block");
        }else{
          canvas.addBlock(block);
          Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(),
                                                                     WorkspaceEvent.BLOCK_ADDED));
          canvas.layoutBlocks();
          return;
        }
      }
    }
    throw new FactoryException("Drawer not found: "+drawer);
  }

  public void addDynamicBlock(RenderableBlock block, String drawer)
      throws FactoryException {
    addDynamicBlock(block, drawer, dynamicCanvases);
  }

  public void addAdvancedBlock(RenderableBlock block, String drawer)
      throws FactoryException {
    addDynamicBlock(block, drawer, advancedCanvases);
  }

  private void addDynamicBlock(RenderableBlock block, String drawer,
                               TreeSet<FactoryCanvas> dynamicCanvasSet) throws FactoryException {
    for(FactoryCanvas canvas : dynamicCanvasSet){
      if (canvas.getName().equals(drawer)){
        if(block == null || Block.NULL.equals(block.getBlockID())){
          throw new FactoryException("Attempting to add a null instance of block");
        } else {
          if (canvas.getName().equals(DEFINITIONS_NAME)) {
            Long parentID = ((BlockStub) block.getBlock()).getParents().iterator().next();
            Block parent = Block.getBlock(parentID);
            if (parent.isArgument()) {
              BlockStub stub = (BlockStub) block.getBlock();
              for (RenderableBlock rb : canvas.getBlocks()) {
                if (rb.getBlock().getBlockLabel().equals(block.getBlock().getBlockLabel())) {
                  rb.setVisible(false);
                }
              }
            }
          }
          canvas.addBlock(block);
          Workspace.getInstance().notifyListeners(
              new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
          canvas.layoutBlocks();
          return;
        }
      }
    }
    throw new FactoryException("Drawer not found: "+drawer);
  }

  /**
   * Add blocks to drawer if drawer can be found.  Add graphically
   * and alos throw event.  Do nothing if no drawer if specified
   * name is found.
   *
   * @param blocks
   * @param drawer
   * @throws FactoryException
   */
  public void addStaticBlocks(Collection<RenderableBlock> blocks, String drawer) throws FactoryException{
    //find canvas
    for(FactoryCanvas canvas : this.staticCanvases){
      if (canvas.getName().equals(drawer)){
        for(RenderableBlock block : blocks){
          if(block == null || Block.NULL.equals(block.getBlockID())) continue;
          canvas.addBlock(block);
          Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));

        }
        canvas.layoutBlocks();
        return;
      }
    }
    throw new FactoryException("Drawer not found: "+drawer);
  }

  public void addDynamicBlocks(Collection<RenderableBlock> blocks, String drawer)
      throws FactoryException{
    //find canvas
    for (FactoryCanvas canvas : dynamicCanvases) {
      if (canvas.getName().equals(drawer)) {
        for (RenderableBlock block : blocks) {
          if (block == null || Block.NULL.equals(block.getBlockID())) {
            continue;
          }
          canvas.addBlock(block);
          Workspace.getInstance().notifyListeners(
              new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));

        }
        canvas.layoutBlocks();
        return;
      }
    }
    for (FactoryCanvas canvas : advancedCanvases) {
      if (canvas.getName().equals(drawer)) {
        for (RenderableBlock block : blocks) {
          if (block == null || Block.NULL.equals(block.getBlockID())) {
            continue;
          }
          canvas.addBlock(block);
          Workspace.getInstance().notifyListeners(
              new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));

        }
        canvas.layoutBlocks();
        return;
      }
    }
    throw new FactoryException("Drawer not found: "+drawer);
  }

  /**
   * Removes block from specified drawer.  DO nothing if no drawer is found
   * with specified name.
   * @param block
   * @param drawer
   * @throws FactoryException
   */
  public void removeStaticBlock(RenderableBlock block, String drawer) throws FactoryException {
    //find canvas
    for(FactoryCanvas canvas : this.staticCanvases){
      if (canvas.getName().equals(drawer)){
        canvas.removeBlock(block);
        //DO NOT THROW AN EVENT FOR REMOVING DRAWER BLOCKS!!!
        //Workspace.getInstance().notifyListeners(new WorkspaceEvent(FactoryManager.this, block.getBlockID(), WorkspaceEvent.BLOCK_REMOVED));
        canvas.layoutBlocks();
        return;
      }
    }
    throw new FactoryException("Drawer not found: "+drawer);
  }
  public void removeDynamicBlock(RenderableBlock block, String drawer) throws FactoryException {
    //find canvas
    for(FactoryCanvas canvas : this.dynamicCanvases){
      if (canvas.getName().equals(drawer)){
        canvas.removeBlock(block);
        //DO NOT THROW AN EVENT FOR REMOVING DRAWER BLOCKS!!!
        //Workspace.getInstance().notifyListeners(new WorkspaceEvent(FactoryManager.this, block.getBlockID(), WorkspaceEvent.BLOCK_REMOVED));
        canvas.layoutBlocks();
        return;
      }
    }
    throw new FactoryException("Drawer not found: "+drawer);
  }

  //////////////////
  //Widget Methods//
  //////////////////
  //documentation found in WorkspaceWidgets.java

  public void blockEntered(RenderableBlock block) {}
  public void blockExited(RenderableBlock block) {}
  public void blockDragged(RenderableBlock block) {}
  public void removeBlock(RenderableBlock block) {}
  public void addBlock(RenderableBlock block){}
  public void addBlocks(Collection<RenderableBlock> blocks){}
  public void blockDropped(RenderableBlock block) {
    //remove block
    WorkspaceWidget oldParent = block.getParentWidget();
    if(oldParent != null)
      oldParent.removeBlock(block);

    Container parent = block.getParent();
    if(parent != null){
      parent.remove(block);
      parent.validate();
      parent.repaint();
      block.setParentWidget(null);
    }

    //fire to workspace that block was removed
    //DO FIRE AN EVENT IF BLOCK IS REMOVED BY USER!!!!
    //NOTE however that we do not throw na event for adding internally
    Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_REMOVED));
  }
  public JComponent getJComponent() {
    return this.navigator.getJComponent();
  }
  public JComponent getFactorySwitcher(){
    return this.factorySwitcher;
  }
  public boolean contains(int x, int y){
    return this.navigator.getJComponent().contains(x, y);
  }
  public void workspaceEventOccurred(WorkspaceEvent event){
    //THIS ENTIRE METHOD IS A HACK!
    //PLEASE CHANGE WITH CAUTION
    //IT DOES SOME PREETY STRANGE THINGS
    if(event.getEventType() == WorkspaceEvent.BLOCK_ADDED){
      if(event.getSourceWidget() instanceof Page){
        Page page = (Page)event.getSourceWidget();
        Block block = Block.getBlock(event.getSourceBlockID());
        //block may not be null if this is a block added event
        if(block.hasStubs()){
          for(BlockStub stub : block.getFreshStubs()){
            try {
              this.addDynamicBlock(
                  new FactoryRenderableBlock(this, stub.getBlockID()),
                  page.getPageDrawer());
            } catch (FactoryException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }else if(event.getEventType() == WorkspaceEvent.BLOCK_REMOVED){
      //may not be removing a null stanc eof block, so DO NOT check for it
      Block block = Block.getBlock(event.getSourceBlockID());
      if(block.hasStubs()){
        ArrayList<Long> blockIdsToForget = new ArrayList<Long>();
        for(Long stub : BlockStub.getStubsOfParent(block.getBlockID())){
          RenderableBlock rb = RenderableBlock.getRenderableBlock(stub);
          if(rb != null && !rb.getBlockID().equals(Block.NULL) &&
              rb.getParentWidget() != null && rb.getParentWidget().equals(this)){
            // rb.getParent() should not be null
            int numStubs = BlockStub.countBlocksWithName(block);
            if (block.isArgumentDeclBlock() && numStubs >= 1) {
              //delete the first invisible one we find
              if (!rb.isVisible() || numStubs == 1) {
                rb.getParent().remove(rb);
                rb.setParentWidget(null);
                // TODO(sharon): come back and check this when we actually allow
                // arguments with the same names.
                blockIdsToForget.add(stub);
                break;
              }
            } else {
              rb.getParent().remove(rb);
              rb.setParentWidget(null);
              blockIdsToForget.add(stub);
            }
          }
        }
        // forget the stubs in another pass to avoid ConcurrentModificationExceptions
        for(Long otherBlockId: blockIdsToForget) {
          BlockStub.forgetStub(otherBlockId);
        }
      }
      this.relayoutBlocks();
    }else if(event.getEventType() == WorkspaceEvent.BLOCK_MOVED){
      Block block = Block.getBlock(event.getSourceBlockID());
      if(block != null && block.hasStubs()){
        for(Long stub : BlockStub.getStubsOfParent(block.getBlockID())){
          RenderableBlock rb = RenderableBlock.getRenderableBlock(stub);
          if(rb != null && !rb.getBlockID().equals(Block.NULL) &&
              rb.getParentWidget() != null && rb.getParentWidget().equals(this)){
            if (rb.getBlock().getGenusName().equals("getter")) {
              BlockStub blockStub = (BlockStub)rb.getBlock();
              //found duplicate
              rb.setVisible(false);
            } else {
              // rb.getParent() should not be null
              rb.getParent().remove(rb);
              rb.setParentWidget(null);
            }
          }
        }
        this.relayoutBlocks();
      }

    }else if(event.getEventType() == WorkspaceEvent.PAGE_RENAMED){
      //this.relayoutBlocks();
    }
  }

  public String toString(){
    return "FactoryManager: "+this.navigator.getJComponent();
  }

  private static class FactoryCanvasComparator implements Comparator<FactoryCanvas> {

    // Keep My Definitions on top.  Note, though, that not all canvases have a Definitions
    // drawer. In that case, the check bellow is harmless.
    String sortFirst = DEFINITIONS_NAME;

    public int compare(FactoryCanvas f1, FactoryCanvas f2) {
      final String f1Name = f1.getName();
      final String f2Name = f2.getName();
      if (f1Name.equals(sortFirst)) {
        if (f2Name.equals(sortFirst))
          return 0;
        return -1;
      }
      if (f2Name.equals(sortFirst))
        return 1;
      int result = f1Name.compareToIgnoreCase(f2Name);
      if (result != 0)
        return result;
      // Since we allow the user to have names that are
      // the same other than by case, we must keep a
      // case-sensitive check in the comparator.
      // However, this way we prefer case-insensitive
      // comparisons for the "high-bit" of the ordering.
      return f1Name.compareTo(f2Name);
    }
  }
}
