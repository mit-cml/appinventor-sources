package openblocks.workspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.appinventor.common.version.MercurialBuildId;
import openblocks.renderable.BlockNote;
import openblocks.renderable.BlockUtilities;
import openblocks.renderable.RenderableBlock;
import openblocks.workspace.typeblocking.FocusTraversalManager;
import openblocks.workspace.typeblocking.TypeBlockManager;
import openblocks.codeblocks.Block;
import openblocks.codeblockutil.CGraphite;

/**
 * The Workspace is the main block area, where blocks are manipulated and assembled.
 * This class governs the blocks, the world, the view, drawing, dragging, animating.
 */

public class Workspace extends JLayeredPane
    implements ISupportMemento, RBParent  {

  private static final long serialVersionUID = 328149080422L;
  /**
   * Single Workspace instance
   */
  private static Workspace ws = new Workspace();

  /** WorkspaceListeners that monitor:
   * block: added, removed, dropped, label changed, connected, disconnected
   * workspace: scrolled, zoom changed
   */
  private HashSet<WorkspaceListener> workspaceListeners = new HashSet<WorkspaceListener>();

  /** The reundomanager instance*/
  //private ReundoManager reundoManager;

  /** WorkspaceWidgets are components within the workspace other than blocks that
   * include bars, buttons, factory drawers, and single instance widgets such as
   * the MiniMap and the TrashCan.
   */
  private TreeSet<WorkspaceWidget> workspaceWidgets = new TreeSet<WorkspaceWidget>(
      // store these in a sorted set according to their "draw depth"
      new Comparator<WorkspaceWidget>() {
        public int compare(WorkspaceWidget w1, WorkspaceWidget w2) {
          // by returning the difference in "draw depth", we make this comparitor
          // sort according to ascending "draw depth" (i.e. front to back)
          //System.out.println("widget 1: "+w1);
          //System.out.println("widget 2: "+w2);
          //System.out.println("are they equal: "+w1.equals(w2)+" compare result: "+(ws.getComponentZOrder(w1.getComponent())-ws.getComponentZOrder(w2.getComponent())));
          //System.out.println("comparing "+w1+" with "+w2+" result1: "+(getDrawDepth(w1.getJComponent())>getDrawDepth(w2.getJComponent())));
          double depth1 = getDrawDepth(w1.getJComponent());
          double depth2 = getDrawDepth(w2.getJComponent());

          if (depth1 > depth2) {
            return 1;
          }
          else if (depth1<depth2) {
            return -1;
          }
          //TODO ria should NEVER return zero unless (w1 == w2) otherwise widget will not be added!
          //ask daniel about this
          if(w1 != w2){
            return -1;
          }else{
            //System.err.println("returned 0: this widget will not be added to workspace widgets: "+w1+ "comparing with: "+w2);
            return 0;
          }
        }
      });


  public static boolean everyPageHasDrawer = false;

  /** The Workspace has a BlockCanvas widget on which blocks actually live.
   * The blockCanvas is what takes care of allowing scrolling and drawing pages,
   * so it is controlled by the Workspace, but it is also a regular WorkspaceWidget
   * for the purposes of drag and drop.
   */
  private BlockCanvas blockCanvas = new BlockCanvas();

  /** blockCanvasLayer allows for static components to be laid out beside the block canvas.  One example of
   * such a component would be a static block factory.  In user testing, we found that novice users performed
   * better with a static block factory than one in which they could drag around and toggle the visibility
   * of. */
  private JSplitPane blockCanvasLayer;

  /**
   * MiniMap associated with the blockCanvas
   */
  private MiniMap miniMap;

  private FactoryManager factory;

  private FocusTraversalManager focusManager;

  // The name of the current workspace, i.e., the current form
  private String name;

  /// RENDERING LAYERS ///
  public final static Integer PAGE_LAYER = new Integer(0);
  public final static Integer BLOCK_HIGHLIGHT_LAYER = new Integer(1);
  public final static Integer BLOCK_LAYER = new Integer(2);
  public final static Integer WIDGET_LAYER = new Integer(3);
  public final static Integer DRAGGED_BLOCK_HIGHLIGHT_LAYER = new Integer(4);
  public final static Integer DRAGGED_BLOCK_LAYER = new Integer(5);
  public final static Integer DRAGGED_ARROW_LAYER = new Integer(6);
  public final static Integer DRAGGED_NOTE_LAYER = new Integer(7);

  private Workspace() {
    super();
    setLayout(null);
    setBackground(CGraphite.verylightgray);
    setPreferredSize(new Dimension(1200, 600));

    this.factory = new FactoryManager();
    this.addWorkspaceListener(this.factory);

    this.miniMap = new MiniMap();
    this.addWidget(this.miniMap, true, true);
    this.addComponentListener(new ComponentAdapter(){
      @Override
      public void componentResized(ComponentEvent e){
        miniMap.repositionMiniMap();
        blockCanvas.reformBlockCanvas();
        blockCanvasLayer.setSize(getSize());
        blockCanvasLayer.validate();
      }
    });

    JPanel explorerPanel = new JPanel();
    explorerPanel.setBackground(CGraphite.lightergreen);
    explorerPanel.setLayout(new BorderLayout());
    explorerPanel.add(factory.getFactorySwitcher(), BorderLayout.NORTH);
    explorerPanel.add(factory.getJComponent(), BorderLayout.CENTER);

    // Show the Mercurial build id using a non-editable JTextField instead of a JLabel, so the user
    // can select and copy it.
    JTextField mercurialBuildId = new JTextField("Version: " + MercurialBuildId.getVersion() +
        " Id: " + MercurialBuildId.getId());
    mercurialBuildId.setEditable(false);
    mercurialBuildId.setBorder(null);
    mercurialBuildId.setBackground(CGraphite.lightergreen);
    mercurialBuildId.setForeground(Color.gray);
    mercurialBuildId.setHorizontalAlignment(JTextField.CENTER);
    mercurialBuildId.setFont(new Font("Arial", Font.PLAIN, 13));
    explorerPanel.add(mercurialBuildId, BorderLayout.SOUTH);

    blockCanvasLayer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
        explorerPanel, blockCanvas.getJComponent());
    blockCanvasLayer.setOneTouchExpandable(true);
    blockCanvasLayer.setDividerSize(6);
    add(blockCanvasLayer, BLOCK_LAYER);
    validate();
    addPage(Page.getBlankPage(), false);

    // Increase the amount of time that a tooltip popup stays up.
    ToolTipManager.sharedInstance().setDismissDelay(60*1000);  // one minute

    this.workspaceWidgets.add(factory);

    this.focusManager = new FocusTraversalManager();
  }

  /**
   * Returns the one <code>Workspace</code> instance
   * @return the one <code>Workspace</code> instance
   */
  public static Workspace getInstance(){
    return ws;
  }

  public Dimension getCanvasSize() {
    return blockCanvas.getCanvas().getSize();
  }

  public Dimension getCanvasOffset() {
    return new Dimension(blockCanvas.getHorizontalModel().getValue()-blockCanvas.getJComponent().getX(),
        blockCanvas.getVerticalModel().getValue()-blockCanvas.getJComponent().getY());
  }

  public BlockCanvas getBlockCanvas(){
    return blockCanvas;
  }

  /**
   * @return MiniMap associated with this.blockcanvas
   */
  public MiniMap getMiniMap(){
    return this.miniMap;
  }

  /**
   * Returns the FocusTraversalManager instance
   * @return FocusTraversalManager instance
   */
  public FocusTraversalManager getFocusManager(){
    return focusManager;
  }

  /**
   * Disables the MiniMap from canvas
   *
   */
  public void disableMiniMap(){
    miniMap.hideMiniMap();
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  ////////////////
  // WIDGETS
  ////////////////

  private Point p = new Point(0,0); // this is for speed - faster not to re-create Points

  /**
   * Returns the WorkspaceWidget currently at the specified point
   * @param point the <code>Point2D</code> to get the widget at, given
   *   in Workspace (i.e. window) coordinates
   * @return the WorkspaceWidget currently at the specified point
   */
  public WorkspaceWidget getWidgetAt(Point point){
    Iterator<WorkspaceWidget> it = workspaceWidgets.iterator();
    //TODO: HUGE HACK, get rid of this. bascally, the facotry has priority
    if(factory.contains(
        SwingUtilities.convertPoint(ws, point, factory.getJComponent()).x,
        SwingUtilities.convertPoint(ws, point, factory.getJComponent()).y)) return factory;
    WorkspaceWidget widget = null;
    while(it.hasNext()){
      //convert point to the widgets' coordinate system
      widget = it.next();
      p = SwingUtilities.convertPoint(ws, point, widget.getJComponent());
      //test if widget contains point and widget is visible
      if(widget.contains(p.x, p.y) && widget.getJComponent().isVisible()) {
        return widget; // because these are sorted by draw depth, the first hit is on top
      }
    }

    return null; // hopefully we never get here
  }

  /**
   * This helper method retuns a fractional "depth" representing the overall
   * z-order of a component in the workspace.  For example, a component with
   * a "drawDepth" of 1.9 is most likely the first child (rendered on top of,
   * remember) a component with z-order 2 in this container.  1.99 means the
   * first child of the first child, and so on.
   * @param c - the Component whose draw depth is required.  MUST be an eventual child of the Workspace.
   * @return the fractional "drawDepth" of the Component c.
   */
  private double getDrawDepth(Component c) {
    int treeLevel = 0;
    double depth = 0;
    Container p = c.getParent();
    //System.out.println("getting drawer depth for: "+c);
    if(p != null){ //ria added this condition (when removing widgets while resetting, they must have a parent, but by then they have none.
      // figure out how far down the tree this component is
      while (p != this && p != null) {
        p = p.getParent();
        treeLevel++;
      }
      //System.out.println("tree level for "+c+": "+treeLevel);
      // now walk up the tree, assigning small fractions for distant children,
      // and getting more important closer to the top level.
      p = c.getParent();
      for (int level = treeLevel; level >= 0; level--) {
        if(p == null)
          break;
        if (level > 0) depth -= p.getComponentZOrder(c) / Math.pow(10, level);
        else depth += p.getComponentZOrder(c);
        c = p;
        p = p.getParent();
      }

      //System.out.println("returned depth "+depth);
      return depth;
    }
    return Double.MAX_VALUE;
  }

  /**
   * Adds the specified widget to this Workspace
   * @param widget the desired widget to add
   * @param floatOverCanvas if true, the Workspace will add and render this widget such that it "floats"
   * above the canvas and its set of blocks.  If false, the widget will be laid out beside the canvas.  This feature
   * only applies if the specified widget is added graphically to the workspace (addGraphically = true)
   * @param addGraphically  a Swing dependent parameter to tell the Workspace whether or not to add
   * the specified widget as a child component.  This parameter should be false for widgets that have a
   * parent already specified
   */
  public void addWidget(WorkspaceWidget widget, boolean addGraphically, boolean floatOverCanvas){
    if(addGraphically){
      if(floatOverCanvas){
        this.add((JComponent)widget, WIDGET_LAYER);
        widget.getJComponent().setVisible(true);
        revalidate();
        repaint();
      }else{
        blockCanvas.getJComponent().setPreferredSize(new Dimension(blockCanvas.getWidth() - widget.getJComponent().getWidth(), blockCanvasLayer.getHeight()));
      }
    }
    boolean success = workspaceWidgets.add(widget);
    if(!success)
      System.err.println("not able to add: "+widget);
  }

  /**
   * Removes the widget from this Workspace.
   * @param widget the desired widget to remove
   */
  public void removeWidget(WorkspaceWidget widget){
    workspaceWidgets.remove(widget);
    this.remove((JComponent)widget);
  }

  /**
   * Returns an unmodifiable Iterable over all the WorkspaceWidgets
   * @return an unmodifiable Iterable over all the WorkspaceWidgets
   */
  public Iterable<WorkspaceWidget> getWorkspaceWidgets(){
    return Collections.unmodifiableSet(workspaceWidgets);
  }

  /**
   * Returns the set of all RenderableBlocks in the Workspace.
   * Includes all live blocks on all pages. Does NOT include:
   *    (1) Factory blocks,
   *    (2) dead blocks,
   *    (3) or subset blocks.
   *  If no blocks are found, it returns an empty set.
   * @return all the RenderableBlocks in the Workspace
   *               or an empty set if none exists.
   */
  public Iterable<RenderableBlock> getRenderableBlocks(){
    //TODO: performance issue, must iterate through all blocks
    return blockCanvas.getBlocks();
  }
  /**
   * Returns the set of all Blocks in the Workspace.
   * Includes all live blocks on all pages. Does NOT include:
   *    (1) Factory blocks,
   *    (2) dead blocks,
   *    (3) or subset blocks.
   *  If no blocks are found, it returns an empty set.
   * @return all the Blocks in the Workspace
   *               or an empty set if none exists.
   */
  public Iterable<Block> getBlocks(){
    //TODO: performance issue, must iterate through all blocks
    ArrayList<Block> blocks = new ArrayList<Block>();
    for(RenderableBlock renderable : blockCanvas.getBlocks()){
      blocks.add(Block.getBlock(renderable.getBlockID()));
    }
    return blocks;
  }

  /**
   * Returns all the RenderableBlocks of the specified genus.
   * Include all live blocks on all pages.  Does NOT include:
   *    (1) all blocks of a different genus
   *    (2) Factory blocks,
   *    (3) dead blocks,
   *    (4) or subset blocks.
   *  If no blocks are found, it returns an empty set.
   * @param genusName - the genus name of the blocks to return
   * @return all the RenderableBlocks of the specified genus
   *               or an empty set if none exists.
   */
  public Iterable<RenderableBlock> getRenderableBlocksFromGenus(String genusName){
    //TODO: performance issue, must iterate through all blocks
    ArrayList<RenderableBlock> blocks = new ArrayList<RenderableBlock>();
    for(RenderableBlock block : blockCanvas.getBlocks()){
      if(Block.getBlock(block.getBlockID()).getGenusName().equals(genusName))
        blocks.add(block);
    }
    return blocks;
  }
  /**
   * Returns all the Blocks of the specified genus.
   * Include all live blocks on all pages.  Does NOT include:
   *    (1) all blocks of a different genus
   *    (2) Factory blocks,
   *    (3) dead blocks,
   *    (4) or subset blocks.
   *  If no blocks are found, it returns an empty set.
   * @param genusName - the genus name of the blocks to return
   * @return all the Blocks of the specified genus
   *               or an empty set if none exists.
   */
  public Iterable<Block> getBlocksFromGenus(String genusName){
    //TODO: performance issue, must iterate through all blocks
    ArrayList<Block> blocks = new ArrayList<Block>();
    for(RenderableBlock renderable : blockCanvas.getBlocks()){
      Block block = Block.getBlock(renderable.getBlockID());
      if(block.getGenusName().equals(genusName))
        blocks.add(block);
    }
    return blocks;
  }

  /**
   * Returns the top level blocks in the Workspace (blocks that are
   * parents of stacks)
   * @return the top level blocks in the Workspace
   */
  public Iterable<RenderableBlock> getTopLevelBlocks(){
    return blockCanvas.getTopLevelBlocks();
  }

  /**
   * calls TypeBlockManager to copy the highlighted blocks on the canvas
   */

  public void copyBlocks(){
    TypeBlockManager.copyBlock();
  }

  /**
   * calls TypeBlockManager to pastes the highlighted blocks on the canvas
   */

  public void pasteBlocks(){
    TypeBlockManager.pasteBlock();
  }

  //////////////////////////
  // WORKSPACE LISTENERS
  //////////////////////////
  /**
   * Adds the specified WorkspaceListener
   */
  public void addWorkspaceListener(WorkspaceListener listener){
    if(listener != null) {
      // warn of duplicate adds
      assert (!workspaceListeners.contains(listener)): "WorkspaceListener "+listener.toString()+" has already been added.";
      workspaceListeners.add(listener);
    }
  }

  /**
   * Removes the specified WorkspaceListener
   * @param listener
   */
  public void removeWorkspaceListener(WorkspaceListener listener){
    if(listener != null)
      workspaceListeners.remove(listener);
  }

  /**
   * Notifies all Workspace listeners of the workspace event
   * @param event
   */
  public void notifyListeners(WorkspaceEvent event){
    for(WorkspaceListener wl : workspaceListeners){
      wl.workspaceEventOccurred(event);
    }
  }

  ////////////////////
  //TypeBLockManaging
  ////////////////////
  /**
   * Enables TypeBLocking if and only if enabled == true
   */
  public void enableTypeBlocking(boolean enabled){
    if(enabled){
      TypeBlockManager.enableTypeBlockManager(blockCanvas);
    }else{
      TypeBlockManager.disableTypeBlockManager();
    }
  }

  ///////////////////
  // WORKSPACE ZOOM
  ///////////////////

  private double zoom =1.0;

  /**
   * Sets the Workspace zoom at the specified zoom level
   * @param newZoom the desired zoom level
   */
  public void setWorkspaceZoom(double newZoom){
    double oldZoom = this.zoom;
    int cDX = 0, cDY = 0;
    int chDX = 0, chDY = 0;

    this.zoom = newZoom;

    BlockUtilities.setZoomLevel(newZoom);
    for(RenderableBlock block : Workspace.getInstance().getRenderableBlocks()){
      block.setZoomLevel(newZoom);
    }
    // It seems unnecessary to zoom the factory blocks.  We'll leave this dead code
    // here for reference, since the original Openblocks did the zoom.
    // for(RenderableBlock block : Workspace.getInstance().getFactoryManager().getBlocks()){
    //   block.setZoomLevel(newZoom);
    // }
    Page p = Workspace.getInstance().getBlockCanvas().getTheOnlyPage();
    if (p != null) {
      for(RenderableBlock block : p.getTopLevelBlocks()){

        // checks if the x and y position has not been set yet, this happens when
        // a previously saved project is just opened and the blocks have not been
        // moved yet. otherwise, the unzoomed X and Y are calculated in RenderableBlock
        if (block.getUnzoomedX() == 0.0 && block.getUnzoomedY() == 0.0) {
          if (newZoom == 1.0) {
            block.setUnzoomedX(block.getX());
            block.setUnzoomedY(block.getY());
          } else {
            block.setUnzoomedX(block.calculateUnzoomedX(block.getX()));
            block.setUnzoomedY(block.calculateUnzoomedY(block.getY()));
          }
        } else {}


        // calculates the new position based on the initial position when zoom is at 1.0
        block.setLocation((int)(block.getUnzoomedX() * zoom), (int)(block.getUnzoomedY() * zoom));

        for (BlockNote bn: block.getBlockNotes()) {
          //Set the block note locations
          bn.setLocation((int) (bn.getUnzoomedX() * zoom), (int) (bn.getUnzoomedY() * zoom));
        }
        block.redrawFromTop();
        block.repaint();
      }
    }
    Page.setZoomLevel(newZoom);
  }

  /**
   * Returns the current workspace zoom
   * @return the current workspace zoom
   */
  public double getCurrentWorkspaceZoom(){
    return zoom;
  }

  /**
   * Resets the workspace zoom to the default level
   *
   */
  public void setWorkspaceZoomToDefault(){
    this.setWorkspaceZoom(1.0);
  }

  public void scrollToComponent(JComponent c){
    blockCanvas.scrollToComponent(c);
  }

  public void scrollToShowRectangle(Rectangle r){
    blockCanvas.scrollToShowRectangle(r);
  }

  ////////////////
  //PAGE METHODS (note: these may change)
  ////////////////

  /**
   * Places the specified page on the canvas.
   * If a page already exists,
   * this method will replace it.
   * @param page the Page to place
   */
  public void addPage(Page page){
    removePage();
    addPage(page, true);
  }

  /**
   * Adds a page to the workspace (the only page).  Removes the current page if any.
   * @param page - the desired Page to add
   * @param fireWorkspaceEvent if set to true, will fire a WorkspaceEvent that a
   * Page was added
   */
  private void addPage(Page page, boolean fireWorkspaceEvent){
    blockCanvas.addPage(page);
    workspaceWidgets.add(page);
    if(fireWorkspaceEvent) notifyListeners(new WorkspaceEvent(page, WorkspaceEvent.PAGE_ADDED));
  }

  /**
   * Removes the specified page from the Workspace
   */
  public void removePage(){
    Page page = blockCanvas.getTheOnlyPage();
    if (page != null) {
      boolean success = workspaceWidgets.remove(page);
      if (!success)
        System.out.println("Page: "+page+", was NOT removed successfully");
      notifyListeners(new WorkspaceEvent(page, WorkspaceEvent.PAGE_REMOVED));
      blockCanvas.removePage(page);
    }
  }

  /**
   * Find the page that lies underneath this block
   * CAN RETURN NULL
   * @param block
   */
  public Page getCurrentPage(RenderableBlock block){
    Page page = blockCanvas.getTheOnlyPage();
    if (page != null) {
      if (page.contains(SwingUtilities.convertPoint(block.getParent(), block.getLocation(),
                                                    page.getJComponent()))){
        return page;
      }
    }
    return null;
  }

  public FactoryManager getFactoryManager() {
    return factory;
  }

  /**
   * Returns an unmodifiable Iterable of all the SearchableContainers within this
   * workspace.
   */
  public Iterable<SearchableContainer> getAllSearchableContainers(){
    ArrayList<SearchableContainer> containers = new ArrayList<SearchableContainer>(factory.getSearchableContainers());

    for(WorkspaceWidget w : workspaceWidgets){
      if(w instanceof SearchableContainer)
        containers.add((SearchableContainer)w);
    }

    return Collections.unmodifiableList(containers);
  }

  ////////////////////////
  // SAVING AND LOADING //
  ////////////////////////
  /**
   * Returns the save String of this.  Currently returns the BlockCanvas
   * save String only.
   * @return the save String of this.
   */
  public String getSaveString(){
    return blockCanvas.getSaveString();
  }

  /**
   * Loads the workspace with the following content:
   * - RenderableBlocks and their associated Block instances that reside
   *   within the BlockCanvas
   * @param newRoot the XML Element containing the new desired content.  Some of the
   * content in newRoot may override the content in originalLangRoot.  (For now,
   * pages are automatically overwritten.  In the future, will allow drawers
   * to be optionally overriden or new drawers to be inserted.)
   * @param originalLangRoot the original language/workspace specification content
   * @requires originalLangRoot != null
   */
  public void loadWorkspaceFrom(Element newRoot, Element originalLangRoot){
    if(newRoot != null){
      //load pages, page drawers, and their blocks from save file
      blockCanvas.loadSaveString(newRoot);
      //load the block drawers specified in the file (may contain
      //custom drawers) and/or the lang def file if the contents specify
      PageDrawerLoadingUtils.loadBlockDrawerSets(originalLangRoot, factory);
      PageDrawerLoadingUtils.loadBlockDrawerSets(newRoot, factory);
      loadWorkspaceSettings(newRoot);
    }else{
      //load from original language/workspace root specification
      blockCanvas.loadSaveString(originalLangRoot);
      //load block drawers and their content
      PageDrawerLoadingUtils.loadBlockDrawerSets(originalLangRoot, factory);
      loadWorkspaceSettings(originalLangRoot);
    }

  }

  /**
   * Loads the settings for this Workspace.  Settings include
   * specification of programming environment features such as
   * the search bar, minimap, or zooming.
   * @param root
   */
  private void loadWorkspaceSettings(Element root){
    Pattern attrExtractor=Pattern.compile("\"(.*)\"");
    Matcher nameMatcher;

    NodeList miniMapNodes = root.getElementsByTagName("MiniMap");
    Node miniMapNode;
    for(int i=0; i<miniMapNodes.getLength(); i++){
      miniMapNode = miniMapNodes.item(i);
      if(miniMapNode.getNodeName().equals("MiniMap")){
        nameMatcher=attrExtractor.matcher(miniMapNode.getAttributes().getNamedItem("enabled").toString());
        if (nameMatcher.find() && nameMatcher.group(1).equals("no")){
          this.disableMiniMap();
        }
      }
    }

    NodeList typeNodes = root.getElementsByTagName("Typeblocking");
    Node typeNode;
    for(int i=0; i<typeNodes.getLength(); i++){
      typeNode = typeNodes.item(i);
      if(typeNode.getNodeName().equals("Typeblocking")){
        nameMatcher=attrExtractor.matcher(typeNode.getAttributes().getNamedItem("enabled").toString());
        if (nameMatcher.find() && nameMatcher.group(1).equals("no")){
          this.enableTypeBlocking(false);
        }else{
          this.enableTypeBlocking(true);
        }
      }
    }
  }

  /**
   * Clears the Workspace of:
   * - all the live blocks in the BlockCanvas.
   * - all the pages on the BlockCanvas
   * - all its BlockDrawers and the RB's that reside within them
   * - clears all the BlockDrawer bars of its drawer references and
   *   their associated buttons
   * - clears all RenderableBlock instances (which clears their associated
   *   Block instances.)
   * Note: we want to get rid of all RendereableBlocks and their
   * references.
   *
   * Want to get the Workspace ready to load another workspace
   */
  public void reset(){
    //we can't iterate and remove widgets at the same time so
    //we remove widgets after we've collected all the widgets we want to remove
    //TreeSet.remove() doesn't always work on the TreeSet, so instead,
    //we clear and re-add the widgets we want to keep
    ArrayList<WorkspaceWidget> widgetsToRemove = new ArrayList<WorkspaceWidget>();
    ArrayList<WorkspaceWidget> widgetsToKeep = new ArrayList<WorkspaceWidget>();
    for(WorkspaceWidget w : workspaceWidgets){
      if(w instanceof Page){
        widgetsToRemove.add(w);
      }else{
        widgetsToKeep.add(w);
      }
    }
    workspaceWidgets.clear();
    workspaceWidgets.addAll(widgetsToKeep);
    workspaceWidgets.add(factory);
    //We now reset the widgets we removed.
    //Doing this for each one gets costly.
    //Do not do this for Pages because on repaint,
    //the Page tries to access its parent.
    for (WorkspaceWidget w : widgetsToRemove){
      Container parent = w.getJComponent().getParent();
      if(w instanceof Page){
        ((Page)w).reset();
      }
      if(parent != null){
        parent.remove(w.getJComponent());
        parent.validate();
        parent.repaint();
      }
    }

    //We now reset, the blockcanvas, the factory, and the renderableblock
    blockCanvas.reset();
    addPage(Page.getBlankPage(), false); //TODO: System expects PAGE_ADDED event
    factory.reset();
    RenderableBlock.reset();
    getFocusManager().reset();
    revalidate();
  }

  /***********************************
   * State Saving Stuff for Undo/Redo *
   ***********************************/

  private class WorkspaceState
  {
    public Map<Long, Object> blockStates;
    public Object blockCanvasState;
  }

  public Object getState()
  {
    /*          //To add workspace specific state, put it here
                WorkspaceState state = new WorkspaceState();

                //Get the block states
                Map<Long, Object> blockStates = new HashMap<Long, Object>();
                for(Block block : Block.getAllBlocks())
                {
                        blockStates.put(block.getBlockID(), block.getState());
                }

                //Save the blocks and the canvas state
                state.blockStates = blockStates;
                state.blockCanvasState = blockCanvas.getState();

                return state;
     */         return null;
  }

  public void loadState(Object memento)
  {
    assert memento instanceof WorkspaceState : "";
  if(memento instanceof WorkspaceState)
  {
    WorkspaceState state = (WorkspaceState)memento;
    //Load the blocks state
    for(Long blockID : state.blockStates.keySet())
    {
      Block toBeUpdated = Block.getBlock(blockID);
      toBeUpdated.loadState(state.blockStates.get(blockID));
    }
    //Load the canvas state
    blockCanvas.loadState(state.blockCanvasState);
  }
  }

  public void undo()
  {
    //          reundoManager.undo();
  }

  public void redo()
  {
    //          reundoManager.redo();
  }

  /******************************************
   * RBParent implemented methods
   ******************************************/

  /*
   * These methods seem to be misnamed, but here's an explanation
   * Blocks and Notes live permanently in Pages and Factories, and there these
   * methods place them in the appropriately named layers. Blocks and Notes appear
   * in Workspace only when being dragged, so adding them to the workspace dragged
   * layers is appropriate. What are Workspace layers 0 - 3 used for? I (jmorris)
   * don't know, but I don't feel like experimenting today.
   */
  @Override
  public void addToBlockLayer(Component c) {
    this.add(c, DRAGGED_BLOCK_LAYER);
  }


  public void addToBlockNoteLayer(Component c) {
    this.add(c, DRAGGED_NOTE_LAYER);
  }

  public void addToBlockArrowLayer(Component c) {
    this.add(c, DRAGGED_ARROW_LAYER);
  }

  public void addToHighlightLayer(Component c) {
    this.add(c, DRAGGED_BLOCK_HIGHLIGHT_LAYER);
  }
}
