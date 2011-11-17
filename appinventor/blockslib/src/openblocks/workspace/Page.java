package openblocks.workspace;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import openblocks.renderable.BlockNote;
import openblocks.renderable.RenderableBlock;
import openblocks.codeblocks.Block;
import openblocks.codeblockutil.CGraphite;
import openblocks.codeblockutil.CToolTip;
import openblocks.yacodeblocks.Escapers;


/**
 * A Page serves as both an abstract container of blocks
 * and also a graphical panel that renders its collection
 * of blocks.  Abstractly, a page has seven abstract fields:
 * a color, a name, a font, a drawer, width, a height,
 * and a set of blocks.  How it renders these abstract fields
 * depends on the state of the page, including: zoom level,
 * and minimumPixelWidth.
 *
 * A Page exists as a WorkspaceWidget, a SearchableContainer,
 * ISupportMemento, an RBParent, a Zoomable object, and a JPanel.
 * As a WorkspaceWidget, it can add, remove, blocks and manage
 * block manipulations within itself.  As a searchableContainer,
 * it can notify users that certain blocks have been queried.
 * As an ISupportMomento, it can undo the current values of
 * abstract fields.  As an RBParent, it can highlight blocks.
 *
 * Since a Page is both a Zoomable object and JPanel, Pages
 * separate its abstract model and view by allowing clients
 * to mutate its abstract fields directly.  But clients must
 * remember to reform the pages in order to synchronize the
 * data between the model and view.
 *
 * A page's abstract color is rendered the same no matter
 * what state the page is in.  A page's abstract name is
 * rendered thrice centered at every fourth of the page.
 * The name is rendered with a size depending on the zoom
 * level of that page (it tries to maintain a constant aspect
 * ratio).  The drawer name is not rendered.  The width and
 * height of the page is rendered differently depending on
 * the zoom level and minimumPixelWidth.  Using the zoom level,
 * it tries to maintain a constant aspect ratio but the
 * absolute sizes varies with a bigger/smaller zoom level.
 * the minimumPixelWidth limits the width from going below
 * a certain size, no matter what the system tries to set
 * the abstract width to be.  Finally the set of blocks are
 * rendered directly onto the page with the same transformation
 * as the ones imposed on the width and height of the page.
 *
 * As an implementation detail, a page tries to maintain a
 * separation between its abstract states and its view.
 * Clients of Pages should use reform*() methods to validate
 * information between the abstract states and view.  Clients
 * of Pages are warned against accessing Page.getJComponent(),
 * as the method provides clients a way to unintentionally mutate
 * an implementation specific detail of Pages.
 *
 * A Page implements ExplorerListener i.e. it listens for possible changes in
 * an explorer that affects the display of the page. When an explorer event
 * happens the page changes its display accordingly
 */
public class Page  implements WorkspaceWidget, SearchableContainer, ISupportMemento{
  /** Width while in collapsed mode */
  private static final int COLLAPSED_WIDTH = 20;
  /** The default abstract width */
  private static final int DEFAULT_ABSTRACT_WIDTH = 700;
  /** The default abstract height */
  public static final int DEFAULT_ABSTRACT_HEIGHT = 1600;
  /** an equals sign followed by a double quote character*/
  private static final String EQ_OPEN_QUOTE = "=\"";
  /** a double quote character */
  private static final String CLOSE_QUOTE ="\" ";
  /** An empty string */
  private static final String emptyString = "";

  /** this.zoomLevel: zoom level state */
  static double zoom = 1.0;
  /** The JComponent of this page */
  private final PageJComponent pageJComponent = new PageJComponent();
  /** The abstract width of this page */
  private double abstractWidth;
  /** The abstract height of this page */
  private double abstractHeight;
  /** The name of the drawer that this page refers to */
  private String pageDrawer;
  /** The default page color.  OVERRIDED BY BLOCK CANVAS */
  private final Color defaultColor;
  /** MouseIn Flag: true if and only if the mouse is in this page */
  private boolean mouseIsInPage = false;
  /** The minimum width of the page in pixels */
  private int minimumPixelWidth = 0;
  /** The minimum height of the page in pixels */
  private int minimumPixelHeight = 0;
  /** The user-time unique id of this page. Once set, cannot be changed. */
  private String pageId = null;
  //////////////////////////////
  //Constructor/ Destructor     //
  //////////////////////////////

  /**
   * Constructs a new Page
   *
   * @param name - name of this page (this.name)
   * @param pageWidth - the abstract width of this page (this.width)
   * @param pageHeight - the abstract height of this page (this.height)
   * @param pageDrawer - the name of the page drawer that this page refers to
   *
   * @requires name != null && pageDrawer != null
   * @effects constructs a new Page such that:
   *                    1) The name of this page equals the argument "name".
   *                    2) The abstract width of this page equals "pageWidth".
   *                       If "pageWidth is <= to zero, then set the
   *                       width to the DEFAULT_ABSTRACT_WIDTH.
   *                    3) The abstract height of this page equals "pageHeight".
   *                       If "pageHeight is <= to zero, then set the
   *                       height to the DEFAULT_ABSTRACT_HEIGHT.
   *                    4) The drawer name equals pageDrawer if and only if Workspace.everyPageHasDrawer==true.
   *                    5) The color of this page is null.
   *                    6) The font of this page is "Default", PLAIN, and 12.
   *                    7) The set of blocks is empty.
   */
  public Page(String name, int pageWidth, int pageHeight, String pageDrawer){
    this(name, pageWidth, pageHeight, pageDrawer, null);
  }
  public Page(String name, int pageWidth, int pageHeight, String pageDrawer, Color defaultColor){
    super();
    this.defaultColor = defaultColor;
    this.pageJComponent.setLayout(null);
    this.pageJComponent.setName(name);
    this.abstractWidth = pageWidth > 0 ? pageWidth : Page.DEFAULT_ABSTRACT_WIDTH;
    this.abstractHeight = pageHeight > 0 ? pageHeight : Page.DEFAULT_ABSTRACT_HEIGHT;
    if(pageDrawer != null){
      this.pageDrawer = pageDrawer;
    }else if(Workspace.everyPageHasDrawer){
      this.pageDrawer = name;
    }
    this.pageJComponent.setOpaque(true);
  }

  /**
   * Constructs a new Page
   *
   * @param name - name of this page (this.name)
   *
   * @requires name != null
   * @effects constructs a new Page such that:
   *                    1) The name of this page equals the argument "name".
   *                    2) The abstract width of this page equals DEFAULT_ABSTRACT_WIDTH.
   *                    3) The abstract height of this page equals DEFAULT_ABSTRACT_HEIGHT.
   *                    4) The drawer name equals "name"
   *                    5) The color of this page is null.
   *                    6) The font of this page is "Default", PLAIN, and 12.
   *                    7) The set of blocks is empty.
   */
  public Page(String name){
    this(name, -1, -1, name);
  }

  /**
   * Constructs a new Page
   *
   * @requires none
   * @effects constructs a new Page such that:
   *                    1) The name of this page equals the argument "".
   *                    2) The abstract width of this page equals DEFAULT_ABSTRACT_WIDTH.
   *                    3) The abstract height of this page equals DEFAULT_ABSTRACT_HEIGHT.
   *                    4) The drawer name equals ""
   *                    5) The color of this page is null.
   *                    6) The font of this page is "Default", PLAIN, and 12.
   *                    7) The set of blocks is empty.
   */
  public static Page getBlankPage(){
    return new Page(emptyString);
  }

  /**
   * TODO: THIS METHOD NOT YET DOCUMENTED OR IMPLEMENTED
   * Removes all the RenderableBlock content of this.
   * Called when the Workspace is being reset.  Does not fire block
   * removed events.
   */
  public void reset(){
    this.pageJComponent.removeAll();
    Page.zoom = 1.0;
  }

  /**
   * Destructs this Page by setting its set of blocks to empty.
   * Does NOT fire block removed events.
   */
  public void clearPage(){
    for(RenderableBlock block : this.getBlocks()){
      this.pageJComponent.remove(block);
    }
  }

  /**
   * Sets the page id. Consider the page id "final" but settable - once
   * set, it cannot be modified or unset.
   */
  public void setPageId(String id) {
    if (pageId == null)
      pageId = id;
    else
      throw new RuntimeException("Tried to set pageId again: " + this);
  }

  //////////////////////////////
  //Public Accessor                     //
  //////////////////////////////

  /**
   * @return all the RenderableBlocks that reside within this page
   */
  public Collection<RenderableBlock> getBlocks(){
    List<RenderableBlock> blocks = new ArrayList<RenderableBlock>();
    for(Component block : this.pageJComponent.getComponents()){
      if(block instanceof RenderableBlock){
        blocks.add((RenderableBlock)block);
      }
    }
    return blocks;
  }

  /**
   * @return a collection of top level blocks within this page (blocks with no
   *                    parents that and are the first block of each stack) or an empty
   *                    collection if no blocks are found on this page.
   */
  public Collection<RenderableBlock> getTopLevelBlocks(){
    List<RenderableBlock> topBlocks = new ArrayList<RenderableBlock>();
    for(RenderableBlock renderable : this.getBlocks()){
      Block block = Block.getBlock(renderable.getBlockID());
      if(block.getPlug() == null || block.getPlugBlockID() == null || block.getPlugBlockID().equals(Block.NULL)){
        if(block.getBeforeConnector() == null || block.getBeforeBlockID() == null || block.getBeforeBlockID().equals(Block.NULL)){
          topBlocks.add(renderable);
          continue;
        }
      }
    }
    return topBlocks;
  }

  /**
   * Returns this page's id. Can be null, if id is not yet set.
   */
  public String getPageId() {
    return pageId;
  }

  /**
   * @return this page's name
   */
  public String getPageName() {
    return this.pageJComponent.getName();
  }

  /**
   * @return this page's color.  MAY RETURN NULL.
   */
  public Color getPageColor() {
    return this.pageJComponent.getBackground();
  }

  /**
   * @return this page's default color.  MAY RETURN NULL.
   */
  public Color getDefaultPageColor() {
    return this.defaultColor;
  }

  /**
   * @return this page's abstract width
   */
  public double getAbstractWidth() {
    return abstractWidth;
  }

  /**
   * @return this page's abstract height
   */
  public double getAbstractHeight(){
    return abstractHeight;
  }
  /**
   * @return this page drawer that this page refers to or null if non exists.
   *                    MAY RETURN NULL.
   */
  public String getPageDrawer() {
    return pageDrawer;
  }
  /**
   * @return icon of this.  MAY BE NULL
   */
  public Image getIcon(){
    return this.pageJComponent.getImage();
  }

  //////////////////////////////
  //Rendering Mutators          //
  //////////////////////////////

  /**
   * @param newName - the new name of this page.
   *
   * @requires newName != null
   * @modifies this.name
   * @effects sets the name of this page to be newName.
   */
  public void setPageName(String newName) {
    if(pageDrawer.equals(this.pageJComponent.getName()))
      pageDrawer = newName;

    this.pageJComponent.setName(newName);

    //iterate through blocks and update the ones that are page label enabled
    for(RenderableBlock block : this.getBlocks()){
      if(Block.getBlock(block.getBlockID()).isPageLabelSetByPage()){
        Block.getBlock(block.getBlockID()).setPageLabel(this.getPageName());
        block.repaintBlock();
      }
    }

    PageChangeEventManager.notifyListeners();
  }

  /**
   * @param image - the new icon of this.  May be null
   *
   * @requires NONE
   * @modifies this.icon
   * @effects change this.icon to specified icon.  The new icon may be null
   */
  public void setIcon(Image image){
    this.pageJComponent.setImage(image);
  }

  /**
   * @param newColor - the new color of this page
   *
   * @requires none
   * @modifies this.color
   * @effects Set the color of this page tobe newColor.
   *                    If newColor is null, sets the color to the deafult gray.
   */
  public void setPageColor(Color newColor) {
    this.pageJComponent.setBackground(newColor);
  }

  /**
   * @param deltaPixelWidth
   *
   * @requires Integer.MIN_VAL <= deltaPixelWidth <= Integer.MAX_VAL
   * @modifies this.width
   * @effects Adds deltaPixelWidth to the abstract width taking into
   *                    account the zoom level.  May need to convert form pixel to abstract model.
   */
  public void addPixelWidth(int deltaPixelWidth){
    this.setPixelWidth((int)(this.getAbstractWidth()*zoom+deltaPixelWidth));
  }

  /**
   * @param deltaPixelHeight
   *
   * @requires Integer.MIN_VAL <= deltaPixelHeight <= Integer.MAX_VAL
   * @modifies this.height
   * @effects Adds deltaPixelHeight to the abstract height taking into
   *            account the zoom level.  May need to convert form pixel to abstract model.
   */
  public void addPixelHeight(int deltaPixelHeight){
    this.setPixelHeight((int)(this.getAbstractHeight()*zoom+deltaPixelHeight));
  }

  /**
   * @requires Integer.MIN_VAL <= pixelWidth <= Integer.MAX_VAL
   * @modifies this.width
   * @effects sets abstract width to pixelWidth taking into account the zoom level.
   *                    May need to convert form pixel to abstract model.

   */
  public void setPixelWidth(int pixelWidth){
    if(pixelWidth < this.minimumPixelWidth){
      this.abstractWidth = this.minimumPixelWidth/zoom;
    }else {
      this.abstractWidth = pixelWidth/zoom;
    }
  }

  /**
   * @requires Integer.MIN_VAL <= pixelHeight <= Integer.MAX_VAL
   * @modifies this.height
   * @effects sets abstract height to pixelHeight taking into account the zoom level.
   *                    May need to convert form pixel to abstract model.

   */
  public void setPixelHeight(int pixelHeight){
    if(pixelHeight < this.minimumPixelHeight){
      this.abstractHeight = this.minimumPixelHeight/zoom;
    }else {
      this.abstractHeight = pixelHeight/zoom;
    }
  }

  public void setMinimumPixelWidth(int pixelWidth) {
    this.minimumPixelWidth = pixelWidth;
  }

  public void setMinimumPixelHeight(int pixelHeight) {
    this.minimumPixelHeight = pixelHeight;
  }


  //////////////////////////////
  //Reforming Mutators          //
  //////////////////////////////

  /**
   * @requires none
   * @return the current dimensions of this page in terms of pixels
   * @modifies this.JComponent.size
   * @effects Reforms this page's JComponent in order to synchronize the
   *                    abstract width and height with the graphical view.
   *                    This process includes moving this page's JComponent to (0,0)
   *                    and setting this page's JComponent size to (this.abstractwidth*zoom, this.abstractheight*zoom)
   */
  public Dimension reformBounds(){
    this.getJComponent().setBounds(
        0,
        0,
        (int)(this.abstractWidth*zoom),
        (int)(this.abstractHeight*zoom));
    this.getJComponent().setFont(new Font("Ariel", Font.PLAIN, (int)(12*zoom)));
    return new Dimension((int)(this.abstractWidth*zoom), (int)(this.abstractHeight*zoom));
  }

  /**
   * @param block - the new block being added whose position must be revalidated
   *
   * @requires block != null
   * @modifies block.location or this page's abstract width
   * @effects increases the width or height of this page to fit the
   *                    new block.  It must then notify listeners that
   *                    the page's size may have changed
   */
  public void reformBlockPosition(RenderableBlock block) {
    //move blocks in
    Point p = SwingUtilities.convertPoint(block.getParent(), block.getLocation(), this.pageJComponent);
    if(p.x<block.getHighlightStrokeWidth() / 2 + 1){
      block.setLocation(block.getHighlightStrokeWidth() / 2 + 1, p.y);
      block.moveConnectedBlocks();
      // the block has moved, so update p
      p = SwingUtilities.convertPoint(block.getParent(), block.getLocation(), this.pageJComponent);
    }else if(p.x+block.getWidth()+block.getHighlightStrokeWidth()/2+1>this.pageJComponent.getWidth()){
      this.setPixelWidth(p.x+block.getWidth()+ block.getHighlightStrokeWidth() / 2 + 1);
    }

    if(p.y<block.getHighlightStrokeWidth() / 2 + 1){
      block.setLocation(p.x, block.getHighlightStrokeWidth() / 2 + 1);
      block.moveConnectedBlocks();
    }else if(p.y+block.getStackBounds().height+block.getHighlightStrokeWidth() / 2 + 1>
             this.pageJComponent.getHeight()){
      this.setPixelHeight(p.y+block.getStackBounds().height+ block.getHighlightStrokeWidth() / 2 + 1);
    }

    for (BlockNote bn : block.getBlockNotes()) {
      //p = SwingUtilities.convertPoint(block.getComment().getParent(), block.getComment().getLocation(), this.pageJComponent);
      p = bn.getLocation();
      if (p.x + bn.getWidth() + 1 > this.pageJComponent.getWidth()) {
        this.setPixelWidth(p.x + bn.getWidth() + 1);
      }
      if (p.y + bn.getHeight() + 1 > this.pageJComponent.getHeight()) {
        this.setPixelHeight(p.x + bn.getHeight() + 1);
      }
    }

    //repaint all pages
    PageChangeEventManager.notifyListeners();
  }

  /**
   * @requires the current set of blocks of this page != null (though it may be empty)
   * @modifies all the block in this page's set of blocks
   * @effects Automatically arranges all the blocks within this page naively.
   */
  public void reformBlockOrdering(){
    BlockStackSorterUtil.sortBlockStacks(this, this.getTopLevelBlocks());
  }

  /**
   * Collapse all blocks that can be collapsed on this page
   */
  public void collapseAllBlocks() {
    for (Component block : this.pageJComponent.getComponents()){
      if (block instanceof RenderableBlock){
        RenderableBlock rendBlock = (RenderableBlock) block;
        rendBlock.setCollapsed(true);
      }
    }
  }

  /**
   * Expand all blocks that were previously collapsed on this page
   */
  public void expandAllBlocks() {
    for (Component block : this.pageJComponent.getComponents()){
      if (block instanceof RenderableBlock){
        RenderableBlock rendBlock = (RenderableBlock) block;
        // The true second arg says to defer resetting the workspace zoom level
        rendBlock.setCollapsed(false, true);
      }
    }
    // Set the zoom level, since it wasn't done on each individual block expansion
    Workspace.getInstance().setWorkspaceZoom(Workspace.getInstance().getCurrentWorkspaceZoom());
  }

  private Point getBlockMaxPoint(RenderableBlock block) {
    //move blocks in
    Point blockTopLeft =
        SwingUtilities.convertPoint(block.getParent(), block.getLocation(), this.pageJComponent);
    Point blockBottomRight = new Point(
        blockTopLeft.x + block.getWidth()+ block.getHighlightStrokeWidth() / 2 + 1,
        blockTopLeft.y + block.getStackBounds().height+ block.getHighlightStrokeWidth() / 2 + 1);
    Point maxP = blockBottomRight;

    for (BlockNote bn : block.getBlockNotes()) {
      Point blockNoteTopLeft = bn.getLocation();
      Point blockNoteBottomRight = new Point(
          blockNoteTopLeft.x + bn.getWidth() + 1,
          blockNoteTopLeft.y + bn.getHeight() + 1);
      if (maxP.x < blockNoteBottomRight.x) {
        maxP.x = blockNoteBottomRight.x;
      }
      if (maxP.y < blockNoteBottomRight.y) {
        maxP.y = blockNoteBottomRight.y;
      }
    }

    return maxP;
  }

  /**
   * Resize the canvas to have no extra X and Y space past the blocks currently placed
   */
  public void resizePage(int padding) {
    Point maxPoint = new Point(0, 0);
    for (Component block : this.pageJComponent.getComponents()){
      if (block instanceof RenderableBlock){
        RenderableBlock rendBlock = (RenderableBlock) block;
        Point p = getBlockMaxPoint(rendBlock);
        if (maxPoint.x < p.x) {
          maxPoint.x = p.x;
        }
        if (maxPoint.y < p.y) {
          maxPoint.y = p.y;
        }
      }
    }
    setPixelWidth(maxPoint.x + (int)(zoom*padding));
    setPixelHeight(maxPoint.y + (int)(zoom*padding));
    //repaint all pages
    PageChangeEventManager.notifyListeners();
  }

  //////////////////////////////
  //Zoomable Interface          //
  //////////////////////////////

  /**
   * @param newZoom - the new zoom level
   *
   * @requires zoom != 0
   * @modifies zoom level
   * @effects Sets all the Zoomable Pages in contained in this BlockCanvas and
   * sets the zoom level to newZoom.
   */
  public static void setZoomLevel(double newZoom) {
    Page.zoom = newZoom;
  }

  /** @override Zoomable.getZoomLevel() */
  public static double getZoomLevel(){
    return Page.zoom;
  }

  //////////////////////////////
  //WORKSPACEWIDGET METHODS     //
  //////////////////////////////

  /** @override WorkspaceWidget.blockDropped() */
  public void blockDropped(RenderableBlock block) {
    //add to view at the correct location
    Component oldParent = block.getParent();
    block.setLocation(SwingUtilities.convertPoint(oldParent,
        block.getLocation(), this.pageJComponent));
    addBlock(block);
  }

  /** @override WorkspaceWidget.blockDragged() */
  public void blockDragged(RenderableBlock block) {
    if (mouseIsInPage == false) {
      mouseIsInPage = true;
      this.pageJComponent.repaint();
    }
  }
  /** @override WorkspaceWidget.blockEntered() */
  public void blockEntered(RenderableBlock block) {
    if (mouseIsInPage == false) {
      mouseIsInPage = true;
      this.pageJComponent.repaint();
    }
  }
  /** @override WorkspaceWidget.blockExited() */
  public void blockExited(RenderableBlock block) {
    mouseIsInPage = false;
    this.pageJComponent.repaint();
  }
  /** @override WorkspaceWidget.addBlock() */
  public void addBlock(RenderableBlock block){
    //update parent widget if dropped block
    WorkspaceWidget oldParent = block.getParentWidget();
    if(oldParent != this){
      if (oldParent != null) {
        oldParent.removeBlock(block);
        for (BlockNote bn : block.getBlockNotes()) {
          bn.getParent().remove(bn);
        }
      }
      block.setParentWidget(this);
      for (BlockNote bn : block.getBlockNotes()) {
        bn.setParent(block.getParentWidget().getJComponent());
      }
    }

    this.getRBParent().addToBlockLayer(block);
    block.setHighlightParent(this.getRBParent());

    //if block has page labels enabled, in other words, if it can, then set page label to this
    if(Block.getBlock(block.getBlockID()).isPageLabelSetByPage())
      Block.getBlock(block.getBlockID()).setPageLabel(this.getPageName());

    //notify block to link default args if it has any
    block.linkDefArgs();

    //fire to workspace that block was added to canvas if oldParent != this
    if(oldParent != this){
      Workspace.getInstance().notifyListeners(new WorkspaceEvent(oldParent, block.getBlockID(), WorkspaceEvent.BLOCK_MOVED));
      Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED, true));
    }

    // if the block is off the edge, shift everything or grow as needed to fully show it
    this.reformBlockPosition(block);
    this.pageJComponent.revalidate();
  }

  /**
   * @param blocks the Collection of RenderableBlocks to add
   *
   * @requires blocks != null
   * @modifies this page's set of blocks
   * @effects Add the collection of blocks internally and graphically,
   *                    delaying graphicalupdates until all of the blocks have been added.
   * @override WorkspaceWidget.blockEntered()
   */
  public void addBlocks(Collection<RenderableBlock> blocks){
    for (RenderableBlock block : blocks) {
      this.addBlock(block);
    }
    //since new components added, need to validate
    this.pageJComponent.revalidate();
  }
  /** @override WorkspaceWidget.removeBlock() */
  public void removeBlock(RenderableBlock block) {
    this.pageJComponent.remove(block);
  }
  /** @override WorkspaceWidget.getJComponent() */
  public JComponent getJComponent() {
    return this.pageJComponent;
  }
  /**
   * @return the RBParent representation of this Page
   */
  public RBParent getRBParent(){
    return this.pageJComponent;
  }
  /** @override WorkspaceWidget.contains() */
  public boolean contains(int x, int y){
    return this.pageJComponent.contains(x, y);
  }
  /** @override WorkspaceWidget.contains() */
  public boolean contains(Point p){
    return this.contains(p.x, p.y);
  }
  /** Returns string representation of this */
  public String toString(){
    return "Page name: "+getPageName()+" page color "+getPageColor()+" page width "+getAbstractWidth()+" page drawer "+pageDrawer+" page height "+getAbstractHeight();
  }

  //////////////////////////////////
  // SearchableContainer Methods        //
  //////////////////////////////////
  /** @override SearchableContainer.getSearchableElements */
  public Iterable<RenderableBlock> getSearchableElements() {
    return getBlocks();
  }
  /** @override SearchableContainer.updateContainerSearchResults */
  public void updateContainsSearchResults(boolean containsSearchResults) {
    // Do nothing, at least for now
  }

  //////////////////////////
  //SAVING AND LOADING  //
  //////////////////////////

  public ArrayList<RenderableBlock> loadPageFrom(Node pageNode, boolean importingPage){
    //note: this code is duplicated in BlockCanvas.loadSaveString().
    NodeList pageChildren = pageNode.getChildNodes();
    Node pageChild;
    ArrayList<RenderableBlock> loadedBlocks = new ArrayList<RenderableBlock>();
    HashMap<Long, Long> idMapping = importingPage ? new HashMap<Long, Long>() : null;
    if (importingPage)
      reset();
    for(int i=0; i<pageChildren.getLength(); i++){
      pageChild = pageChildren.item(i);
      if(pageChild.getNodeName().equals("PageBlocks")){
        NodeList blocks = pageChild.getChildNodes();
        Node blockNode;
        for(int j=0; j<blocks.getLength(); j++){
          blockNode = blocks.item(j);
          RenderableBlock rb = RenderableBlock.loadBlockNode(blockNode, this, idMapping);
          // save the loaded blocks to add later
          loadedBlocks.add(rb);
        }
        /*                              // now that the blocks are loaded, add them to the page.
                                for (RenderableBlock rb : loadedBlocks) {
                                        if(rb != null){
                                                //add graphically
                                                getRBParent().addToBlockLayer(rb);
                                                rb.setHighlightParent(this.getRBParent());
                                                //System.out.println("loading rb to canvas: "+rb+" at: "+rb.getBounds());
                                                //add internallly
                                                Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, rb.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
                                                if (importingPage) {
                                                        Block.getBlock(rb.getBlockID()).setFocus(false);
                                                        rb.resetHighlight();
                                                        rb.clearBufferedImage();
                                                }
                                        }
                                }

                                //now we need to redraw all the blocks now that all renderable blocks
                                //within this page have been loaded, to update the socket dimensions of
                                //blocks, etc.
                                for (RenderableBlock rb : this.getTopLevelBlocks()) {
                                        rb.redrawFromTop();
                                }
                                this.pageJComponent.revalidate();
                                this.pageJComponent.repaint();
         */
        break;  //should only have one set of page blocks
      }
    }
    return loadedBlocks;
  }

  public void addLoadedBlocks(Collection<RenderableBlock> loadedBlocks, boolean importingPage) {
    for (RenderableBlock rb : loadedBlocks) {
      if(rb != null){
        //add graphically
        getRBParent().addToBlockLayer(rb);
        rb.setHighlightParent(this.getRBParent());
        //System.out.println("loading rb to canvas: "+rb+" at: "+rb.getBounds());
        //add internallly
        Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, rb.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
        if (importingPage) {
          Block.getBlock(rb.getBlockID()).setFocus(false);
          rb.resetHighlight();
          rb.clearBufferedImage();
        }
      }
    }


    //now we need to redraw all the blocks now that all renderable blocks
    //within this page have been loaded, to update the socket dimensions of
    //blocks, etc.
    for (RenderableBlock rb : this.getTopLevelBlocks()) {
      rb.redrawFromTop();
      // We had to wait until now to collapse things so that all their descendants are
      // set up and ready to emerge upon uncollapse operations.

      rb.setInitialCollapse();

      // The following code is deprecated. It was for a previous version of collapsing.
      //  if (rb.isCollapsed()) {
      //This insures that blocks connected to a collapsed top level block
      //are located properly and have the proper visibility set.
      //This doesn't work until all blocks are loaded and dimensions are set.
      //  rb.updateCollapse();
      //                }
    }
    this.pageJComponent.revalidate();
    this.pageJComponent.repaint();
  }

  public String getSaveString(){
    StringBuffer buf = new StringBuffer();

    buf.append("<Page ");
    appendAttribute("page-name", this.getPageName(), buf);
    appendAttribute("page-color", this.getPageColor().getRed() + " " +
        this.getPageColor().getGreen() + " " + this.getPageColor().getBlue(), buf);
    appendAttribute("page-width", (int)this.getAbstractWidth()+"", buf);
    appendAttribute("page-height", (int)this.getAbstractHeight()+"", buf);
    if(pageDrawer != null)
      appendAttribute("page-drawer", pageDrawer, buf);
    if (pageId != null)
      appendAttribute("page-id", pageId, buf);

    buf.append(">\n");
    //retrieve save strings of blocks within this Page
    Collection<RenderableBlock> blocks = this.getBlocks();
    if(blocks.size()>0){
      buf.append("<PageBlocks>\n");
      for(RenderableBlock rb : blocks){
        buf.append(rb.getSaveString());
      }
      buf.append("</PageBlocks>\n");
    }
    buf.append("</Page>\n");

    return buf.toString();
  }
  private void appendAttribute(String att, String value, StringBuffer buf){
    buf.append(att);
    buf.append(EQ_OPEN_QUOTE);
    buf.append(Escapers.escapeForXml(value));
    buf.append(CLOSE_QUOTE);
  }

  ////////////////////////////////////
  //State Saving Stuff for Undo/Redo//
  ////////////////////////////////////

  /**
   * a data structure that holds the name, width, color, set of blocks,
   * and set of renderable blocks in this page.
   */
  private class PageState{
    public String name;
    public String id;
    public int width;
    public int height;
    public Color color;
    public Map<Long, Object> blocks = new HashMap<Long, Object>();
    public Map<Long, Object> renderableBlocks = new HashMap<Long, Object>();
  }
  /** @override ISupportMomento.getState */
  public Object getState(){
    PageState state = new PageState();
    //Populate basic page information
    state.name = getPageName();
    state.id = getPageId();
    state.color = getPageColor();
    state.width = this.pageJComponent.getWidth();
    state.height = this.pageJComponent.getHeight();
    //Fill in block information
    for(RenderableBlock rb : this.getBlocks()){
      state.renderableBlocks.put(rb.getBlockID(), rb.getState());
    }
    return state;
  }
  /** @override ISupportMomento.loadState() */
  public void loadState(Object memento){
    assert (memento instanceof PageState) : "ISupportMemento contract violated in Page";
    if(memento instanceof PageState){
      PageState state = (PageState)memento;
      //load basic page information
      this.setPageName(state.name);
      this.setPageId(state.id);
      this.setPageColor(CGraphite.verylightgray); // always use this page color
      this.setPixelWidth(state.width);
      this.setPixelHeight(state.height);
      //Load block information
      Map<Long, Object> renderableBlockStates = state.renderableBlocks;
      List<Long> unloadedRenderableBlockStates = new LinkedList<Long>();
      List<Long> loadedBlocks = new LinkedList<Long>();
      for(Long id : renderableBlockStates.keySet()){
        unloadedRenderableBlockStates.add(id);
      }
      //First, load all the blocks that are in the state to be loaded
      //against all the blocks that already exist.
      for(RenderableBlock existingBlock : getBlocks()){
        Long existingBlockID = existingBlock.getBlockID();
        if(renderableBlockStates.containsKey(existingBlockID)){
          existingBlock.loadState(renderableBlockStates.get(existingBlockID));
          unloadedRenderableBlockStates.remove(existingBlockID);
          loadedBlocks.add(existingBlockID);
        }
      }
      ArrayList<RenderableBlock> blocksToRemove = new ArrayList<RenderableBlock>();
      //Now, find all the blocks that don't exist in the save state and flag them to be removed.
      for(RenderableBlock existingBlock : this.getBlocks()){
        Long existingBlockID = existingBlock.getBlockID();
        if(!loadedBlocks.contains(existingBlockID)){
          blocksToRemove.add(existingBlock);
        }
      }
      //This loop is necessary to avoid a concurrent modification error that occurs
      //if the loop above removes the block while iterating over an unmodifiable
      //iterator.
      for(RenderableBlock toBeRemovedBlock : blocksToRemove){
        this.removeBlock(toBeRemovedBlock);
      }
      //Finally, add all the remaining blocks that weren't there before
      ArrayList<RenderableBlock> blocksToAdd = new ArrayList<RenderableBlock>();
      for(Long newBlockID : unloadedRenderableBlockStates){
        RenderableBlock newBlock = new RenderableBlock(this, newBlockID);
        newBlock.loadState(renderableBlockStates.get(newBlockID));
        blocksToAdd.add(newBlock);
      }
      this.addBlocks(blocksToAdd);
      this.pageJComponent.repaint();
    }
  }
}

/**
 * This class serves as the zoomable JComponent and RBParent of the page
 * that wraps it.
 */
class PageJComponent extends JLayeredPane  implements RBParent {
  private static final long serialVersionUID = 83982193213L;
  private static final Integer HIGHLIGHT_LAYER = 0;
  private static final Integer BLOCK_LAYER = 1;
  private static final Integer BLOCK_ARROW_LAYER = 2;
  private static final Integer BLOCK_NOTE_LAYER = 3;
  private static final int IMAGE_WIDTH = 60;
  private Image image = null;
  public void setImage(Image image){
    this.image=image;
  }
  public Image getImage(){
    return image;
  }
  /**
   * renders this JComponent
   */
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    //paint page
    super.paintComponent(g);
    //set label color
    if (this.getBackground().getBlue() + this.getBackground().getGreen() + this.getBackground().getRed() > 400) {
      g.setColor(Color.DARK_GRAY);
    }
    else {
      g.setColor(Color.LIGHT_GRAY);
    }

    //paint label at correct position
    int xpos = (int)(this.getWidth()*0.5-g.getFontMetrics().getStringBounds(this.getName(), g).getCenterX());
    g.drawString(this.getName(), xpos, getHeight()/2);
    g.drawString(this.getName(), xpos, getHeight()/4);
    g.drawString(this.getName(), xpos, getHeight()*3/4);


    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.33F));
    int imageX = (int)(this.getWidth()/2-IMAGE_WIDTH/2*Page.zoom);
    int imageWidth = (int)(IMAGE_WIDTH*Page.zoom);
    g.drawImage(this.getImage(), imageX, getHeight()/2+5,imageWidth,imageWidth,null);
    g.drawImage(this.getImage(), imageX, getHeight()/4+5,imageWidth,imageWidth,null);
    g.drawImage(this.getImage(), imageX, getHeight()*3/4+5,imageWidth,imageWidth,null);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1));

  }

  //////////////////////////////////
  //RBParent implemented methods        //
  //////////////////////////////////

  /** @override RBParent.addToBlockArrowLayer() */
  public void addToBlockArrowLayer(Component c) {
    super.add(c, BLOCK_ARROW_LAYER);
  }

  /** @override RBParent.addToBlockNoteLayer() */
  public void addToBlockNoteLayer(Component c) {
    super.add(c, BLOCK_NOTE_LAYER, 0);

  }

  /** @override RBParent.addToBlockLayer() */
  public void addToBlockLayer(Component c) {
    super.add(c, BLOCK_LAYER, 0);
  }

  /** @override RBParent.addToHighlightLayer() */
  public void addToHighlightLayer(Component c) {
    super.add(c, HIGHLIGHT_LAYER);
  }

}

/**
 * A BlockStackSortUtil is a utilities class that serves to order
 * blocks from closest to furthest blocks (relative to the x=0 axis).
 */
class BlockStackSorterUtil {
  /** The minimum bounds between blocks */
  private static final int BUFFER_BETWEEN_BLOCKS = 20;
  /** A helper rectangle that maintains the bounds between blocks */
  private static final Rectangle positioningBounds = new Rectangle(BUFFER_BETWEEN_BLOCKS,BUFFER_BETWEEN_BLOCKS,0,0);
  /** An ordered set of blocks.  Blocks are ordered from closest to furthest (relative to x=0 axis) */
  private static final TreeSet<RenderableBlock> blocksToArrange = new TreeSet<RenderableBlock>(
      //TODO ria for now they are ordered in y-coor order
      //this naive ordering will also fail if two blocks have the same coordinates
      new Comparator<RenderableBlock>(){
        public int compare(RenderableBlock rb1, RenderableBlock rb2) {
          if(rb1 == rb2)
            return 0;
          else{
            //translate points to a common reference: the parent of rb1
            Point pt1 = rb1.getLocation();
            Point pt2 = SwingUtilities.convertPoint(rb2.getParentWidget().getJComponent(),
                rb2.getLocation(), rb1.getParentWidget().getJComponent());
            if(pt1.getY() < pt2.getY())
              return -1;
            else
              return 1;
          }
        }
      }
  );

  /**
   * This method serves to help clients sort blocks within a page
   * in some manner.
   *
   * @param page
   * @param topLevelBlocks
   *
   * @requires page != null && topLevelBlocks != null
   * @modifies the location of all topLevelBlocks
   * @effects sort the topLevelBlocks and move them to an order location on the page
   */
  protected static void sortBlockStacks(Page page, Collection<RenderableBlock> topLevelBlocks){
    blocksToArrange.clear();
    positioningBounds.setBounds(BUFFER_BETWEEN_BLOCKS,BUFFER_BETWEEN_BLOCKS,0,BUFFER_BETWEEN_BLOCKS);
    //created an ordered list of blocks based on x-coordinate position
    blocksToArrange.addAll(topLevelBlocks);

    //Naively places blocks from top to bottom, left to right.
    for(RenderableBlock block : blocksToArrange){
      Rectangle bounds = block.getStackBounds();
      // Note that what we are doing here is keeping the current
      // height, and expanding right if necessary to place all the
      // blocks.
      if(positioningBounds.height + bounds.height > page.getJComponent().getHeight()){
        //need to go to next column
        positioningBounds.x = positioningBounds.x + positioningBounds.width + BUFFER_BETWEEN_BLOCKS;
        positioningBounds.width = 0;
        positioningBounds.height = BUFFER_BETWEEN_BLOCKS;
      }
      block.setLocation(positioningBounds.x, positioningBounds.height);

      //sets the x and y position for when workspace is unzoomed
      block.setUnzoomedX(block.calculateUnzoomedX(positioningBounds.x));
      block.setUnzoomedY(block.calculateUnzoomedY(positioningBounds.height));
      block.moveConnectedBlocks();

      //update positioning bounds
      positioningBounds.width = Math.max(positioningBounds.width, bounds.width);
      positioningBounds.height = positioningBounds.height + bounds.height + BUFFER_BETWEEN_BLOCKS;

      if(positioningBounds.x + positioningBounds.width > page.getJComponent().getWidth()){
        //resize page to the difference
        page.addPixelWidth(positioningBounds.x + positioningBounds.width - page.getJComponent().getWidth());
      }
    }
  }
}
