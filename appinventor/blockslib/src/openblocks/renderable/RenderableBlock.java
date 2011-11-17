package openblocks.renderable;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;
import openblocks.codeblocks.BlockConnectorShape;
import openblocks.codeblocks.BlockGenus;
import openblocks.codeblocks.BlockLink;
import openblocks.codeblocks.BlockLinkChecker;
import openblocks.codeblocks.BlockShape;
import openblocks.codeblocks.BlockStub;
import openblocks.codeblocks.ComplaintDepartment;
import openblocks.codeblocks.InfixBlockShape;
import openblocks.codeblocks.JComponentDragHandler;
import openblocks.codeblocks.rendering.BlockShapeUtil;
import openblocks.codeblockutil.CToolTip;
import openblocks.codeblockutil.GraphicsManager;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import openblocks.workspace.FactoryCanvas;
import openblocks.workspace.FactoryManager;
import openblocks.workspace.ISupportMemento;
import openblocks.workspace.MiniMap;
import openblocks.workspace.RBParent;
import openblocks.workspace.SearchableElement;
import openblocks.workspace.TrashCan;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceWidget;
import openblocks.yacodeblocks.BlockParser;
import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.WorkspaceController;
import openblocks.yacodeblocks.WorkspaceControllerHolder;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


/**
 * RenderableBlock is responsible for all graphical rendering of a code Block.  This class is also
 * responsible for consuming all mouse and key events on itself.  Each RenderableBlock object is
 * coupled with its associated Block object, and uses information maintained in Block to
 * render the graphical block accordingly.
 */
public class RenderableBlock extends JComponent implements SearchableElement,
            MouseListener, MouseMotionListener, ISupportMemento {

  private static final long serialVersionUID = 1L;
  private static final boolean DEBUG = false;

  private Color color;
  private Color activatedColor;
  private static final Color deactivatedColor = Color.WHITE;

  private class BlockImage {
    // RENDERING RELATED FIELDS
    // Shape components used to draw this block's geometrical shape.
   private BlockShape shape; // the shape which is an abstract outline of the block
   private Area abstractArea; //  a filled in abstract shape
   private Area area = new Area(); // area which is a filled in pixel shape
   private BufferedImage buffImg = null; // static drawing area for unstable blocks
   private List<ConnectorTag> socketTags = new ArrayList<ConnectorTag>(); // the block's sockets
   /**
    * Redraws the entire buffer on a Graphics2D, called by paintCompnent() only
    * if the buffer has been cleared.
    */
   void updateBuffImg() {
     synchronizeLabelsAndSockets();
     reformBlockShape();
     // create image
     // note: need to add twice the highlight stroke width so that the highlight
     // does not get cut off
     GraphicsManager.recycleGCCompatibleImage(buffImg);
     buffImg = GraphicsManager.getGCCompatibleImage(
         area.getBounds().width,
         area.getBounds().height);
     Graphics2D buffImgG2 = (Graphics2D) buffImg.getGraphics();

     // update bounds of this renderableBlock as bounds of the shape
     Dimension updatedDimensionRect = new Dimension(area.getBounds().getSize());

     // get size of block to determine size needed for bevel image
     Image bevelImage = BlockShapeUtil.getBevelImage(
         updatedDimensionRect.width, updatedDimensionRect.height, area);

     // need anti-aliasing to remove color fill artifacts outside the bevel
     buffImgG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

     // ADD BLOCK COLOR
     buffImgG2.setColor(color);
     buffImgG2.fill(area);

     // draw the bevel on the shape -- comment out this line to not apply beveling
     buffImgG2.drawImage(bevelImage, 0, 0, null);

   }
   /**
    * Clears the BufferedImage of this
    */
   public void clearBufferedImage() {
     if (buffImg != null) {
       GraphicsManager.recycleGCCompatibleImage(buffImg);
       buffImg = null;
     }
   }
  }

  final BlockImage normalImage;
  final BlockImage collapsedImage;
  BlockImage blockImage;

  /*
  *
  *The following may be null: parent, lastdragwidget, comment
  */
 // STATIC FIELDS
 /** The maximum distance between blocks that are still considered nearby enough to link */
 //  private static final double NEARBY_RADIUS = 20.0;
  /** The alpha level while dragging - lower means more transparent */
  private static final  float DRAGGING_ALPHA = 0.66F;
  /** Mapping from blockID to the corresponding RenderableBlock instance */
  private static final Map<Long, RenderableBlock> ALL_RENDERABLE_BLOCKS =
      new HashMap<Long, RenderableBlock>();

  // COMPONENT FIELDS
  /** BlockID of this.  MAY BE Block.NULL */
  private final Long blockID;
  /** Parent workspace widget.  May be null */
  private WorkspaceWidget parent;
  /** The previous known WorkspaceWidget this block was dragged over.  May be null */
  private WorkspaceWidget lastDragWidget = null;
  /**
   * An internal JComponent whose functionality is independent of any other
   * functionality. If the block widget is the largest component in the
   * block, then the renderableblock's Shape is determined from the dimensions
   * of this widget.  They should not be related to starlogo or codeblocks.  MAY BE NULL*/
  private JComponent blockWidget = null;
  private final BlockMenu blockMenu;

  // Internal Managers
  /** HighlightManager that manages drawing of highlights around this block */
  private RBHighlightHandler highlighter;
  /** dragHandler keeps the block within the workspace area. It manages relocating the block. */
  private JComponentDragHandler dragHandler;

  //ATTRIBUTE FIELDS
  /** Binary attributes of this RenderableBlocks:
   * (1) popupIconVisible is true if the pop-up icon is visible,
   * (2) isSearchResult is true if this block is being queried by search
   * (3) isPickedUp is true if mousePressed was performed on this block,
   * (4) dragging is true if mouseDragged was performed on this block at least
   * once,
   * (5) linkedDefArgsBefore is any default arguments were never attached
   * (6) isLoading is true if RenderableBlock is still loading- Though its data
   * may have loaded completely, it still may need other connected
   * RenderableBlocks to finish loading as well.  In this case, isLoading would
   * still be false */
  private boolean isSearchResult = false;
  private boolean pickedUp = false;
  private boolean dragging = false;
  private boolean linkedDefArgsBefore = false;
  boolean isLoading = false;
  private boolean loadedAsCollapsed = false;

  private Integer pendingReplMessages = 0;

  ///////////////////////////
  //Sockets and Labels
  /** TODO(user): Documentation does not exist for these components.  Consult author*/
  private final NameLabel blockLabel;
  private final PageLabel pageLabel;
  private final DecoratorLabel decoratorLabel;

  private ArrayList<BlockNote> blockNotes = new ArrayList<BlockNote>();
  private boolean blockNotesChanged = false;

  private final CollapseLabel collapseLabel;
  private final ConnectorTag plugTag;
  private final ConnectorTag afterTag;
  private final ConnectorTag beforeTag;

  // the values of the x and y coordinates of block when zoom = 1.0
  private double unzoomedX;
  private double unzoomedY;

  /**
   * Constructs a new RenderableBlock instance with the specified parent WorkspaceWidget and
   * Long blockID of its associated Block
   * @param parent the WorkspaceWidget containing this
   * @param blockID Long Block id of associated with this
   */
  public RenderableBlock(WorkspaceWidget parent, Long blockID){
    this(parent, blockID, false);
  }

  /**
   * Constructs a new RenderableBlock instance with the specified parent WorkspaceWidget and
   * Long blockID of its associated Block
   * @param parent the WorkspaceWidget containing this
   * @param blockID Long Block id of associated with this
   * @param isLoading indicates if this block is still waiting for all information
   * needed to properly construct it
   */
  private RenderableBlock(WorkspaceWidget parent, Long blockID, boolean isLoading){
    super();
    this.isLoading = isLoading;
    /*
     * Sets whether focus traversal keys are enabled
     * for this Component. Components for which focus
     * traversal keys are disabled receive key events
     * for focus traversal keys.
     */
    setFocusTraversalKeysEnabled(false);

    this.parent = parent;
    this.blockID = blockID;
    ALL_RENDERABLE_BLOCKS.put(blockID, this);

    boolean collapseable = isCollapseable(getBlock().getGenusName());

    normalImage = new BlockImage(); //form basic shape
    blockImage = normalImage;
    if (getBlock().isInfix()) {
      normalImage.shape = new InfixBlockShape(this);
    } else {
      normalImage.shape = new BlockShape(this, getBlock());
    }
    if (collapseable) {
      collapsedImage = new BlockImage();
      Block temp = new Block("collapsed", getBlock().getBlockLabel());
      collapsedImage.shape = new BlockShape(this, temp);
      temp.forget();
    } else {
      collapsedImage = null;
    }
    //set null layout so as to add blockLabels where ever we want
    setLayout(null);

    activatedColor = BlockGenus.getGenusWithName(getGenus()).getColor();
    color = activatedColor;

    boolean headless = WorkspaceControllerHolder.isHeadless();
    if (!headless) {
      dragHandler = new JComponentDragHandler(this); // set up drag handler delegate
      addMouseListener(this);
      addMouseMotionListener(this);
    }

    //initialize tags, labels, and sockets:
    plugTag = new ConnectorTag(getBlock().getPlug());
    afterTag = new ConnectorTag(getBlock().getAfterConnector());
    beforeTag = new ConnectorTag(getBlock().getBeforeConnector());
    blockLabel = new NameLabel(getBlock().getBlockLabel(),
        BlockLabel.Type.NAME_LABEL, getBlock().isLabelEditable(), blockID);
    decoratorLabel = new DecoratorLabel(getBlock().getDecoratorLabel(), getBlockID());
    pageLabel =
      new PageLabel(getBlock().getPageLabel(), BlockLabel.Type.PAGE_LABEL, false, blockID);
    add(pageLabel.getJComponent());
    add(blockLabel.getJComponent(), 0);
    add(decoratorLabel.getJComponent());
    synchronizeSockets();

    if (!isLoading) {
      reformBlockShape(); //to update socket points to position labels and setBounds of this rb
      //to cache image upon instantiation, update buffered image here:
    }

    if (collapseable) {
      collapseLabel = new CollapseLabel(blockID);
      add(collapseLabel);
    } else {
      collapseLabel = null;
    }

    // FactoryRenderableBlocks should not show menus. They don't today because
    // they override the mouseReleased method/
    blockMenu = headless ? null : new BlockMenu(this);

    if (!headless) {
      highlighter = new RBHighlightHandler(this);
    }

    String blockDescription = getBlock().getBlockDescription();
    if (blockDescription != null) {
      setBlockToolTip(getBlock().getBlockDescription().trim());
    }
    if (!headless) {
      setCursor(dragHandler.getDragHintCursor());
    }
  }

  private boolean isCollapseable(String genusName) {
    return genusName.startsWith("define")
      || genusName.equals("def")
      || BlockGenus.getGenusWithName(genusName).getDecorator().equals("when");
  }

  /**
   * Removes the target block from its container and widget. Notifies
   * all WorkspaceListeners with the BLOCK_REMOVED event.
   *
   * Requires that renderable != null && renderable.blockID != null &&
   * renderable.blockID != Block.NULL.
   *
   * @param renderable The target block to remove.
   * @param widget The display widget for renderable.
   * @param container The Swing container that this block is attached to.
   * on the after block of the target block.
   *
   */

  public static void removeBlock(RenderableBlock renderable, WorkspaceWidget widget,
      Container container){
    for (BlockNote bn : renderable.blockNotes) {
      bn.delete();
    }
    if (widget != null) {
      widget.removeBlock(renderable);
    }
    if (container != null) {
      container.remove(renderable);
      container.validate();
      container.repaint();
    }
    renderable.setParentWidget(null);
    // TODO(sharon): figure out how to make the tooltip disappear if it was showing
    Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, renderable.getBlockID(),
        WorkspaceEvent.BLOCK_REMOVED));
  }

  /**
   * Returns the Long id of this
   * @return the Long id of this
   */
  public Long getBlockID(){
    return blockID;
  }

  public ArrayList<BlockNote> getBlockNotes() {
    return blockNotes;
  }

  /**
   * Returns the height of the block shape of this
   * @return the height of the block shape of this
   */
  public int getBlockHeight(){
    return blockImage.area.getBounds().height;
  }

  /**
   * Returns the dimensions of the block shape of this
   * @return the dimensions of the block shape of this
   */
  private Dimension getBlockSize(){
    return blockImage.area.getBounds().getSize();
  }

  /**
   * Returns the width of the block shape of this
   * @return the width of the block shape of this
   */
  public int getBlockWidth(){
    return blockImage.area.getBounds().width;
  }

  /**
   * Returns the BlockShape instance representing this
   * @return the BlockShape instance representing this
   */
  public BlockShape getBlockShape(){
    return blockImage.shape;
  }

  /**
   * @return the abstract BlockArea
   */
  Area getAbstractBlockArea() {
    return blockImage.abstractArea;
  }

  /**
   * Moves this component to a new location. The top-left corner of
   * the new location is specified by the <code>x</code> and <code>y</code>
   * parameters in the coordinate space of this component's parent.
   * @param x the <i>x</i>-coordinate of the new location's
   *          top-left corner in the parent's coordinate space
   * @param y the <i>y</i>-coordinate of the new location's
   *          top-left corner in the parent's coordinate space
   */
  @Override
  public void setLocation(int x, int y) {
    int dx, dy;
    dx = x - getX();
    dy = y - getY();
    if (dx == 0 && dy == 0) {
      return;
    }
    super.setLocation(x, y);
    // The test (dx == x && dy == y) is a way of asking if
    // getX() and  getY() were 0 before the super.setLocation, which is a way
    // of asking whether this call is setting the location away from (0,0)
    // which is true during loading.
    // But what if the block just happens to be at (0,0)? As far as I can tell
    // it's not possible to drag a block to (0,0). (2/4/10)
    // Bogus!
    if (dx == x && dy == y) {
      return;
    }
    for (BlockNote bn : blockNotes) {
      if (bn.getParent() != getParent()) {
        bn.setParent(getParent());
      }
      bn.translatePosition(dx, dy);
    }
  }


  /**
   * Moves this component to a new location. The top-left corner of
   * the new location is specified by point <code>p</code>. Point
   * <code>p</code> is given in the parent's coordinate space.
   * @param p the point defining the top-left corner
   *          of the new location, given in the coordinate space of this
   *          component's parent
   */
  @Override
  public void setLocation(Point p) {
    setLocation(p.x, p.y);
  }


  /**
   * Returns the width of the stroke used to draw the highlight.
   * Note that the highlight will only appear half this width,
   * so the overall width of the block + highlight will be
   * blockWidth + highlightStrokeWidth.
   * @return the width of the stroke used to draw the highlight.
   */
  public int getHighlightStrokeWidth() {
    return RBHighlightHandler.HIGHLIGHT_STROKE_WIDTH;
  }

  /**
   * Returns the bounds of the block stack of this, where this block
   * is at the top of its stack (in other words, it does not take the
   * bounds of the blocks above it into account).
   * @return the bounds of the block stack of this, where this block
   * is at the top of its stack.
   */
  public Rectangle getStackBounds(){
    return new Rectangle(getLocation(), calcStackDimensions(this));
  }

  /**
   * Helper method to calculate the bounds of a stack.  For now this method naively traverses
   * through the entire stack of the specified RenderableBlock rb and calculates the bounds.
   * @param rb the RenderableBlock to calculate the stack bounds of
   * @return Dimensions of the stack of the specified rb
   */
  private Dimension calcStackDimensions(RenderableBlock rb){
    if (rb.getBlock().getAfterBlockID() != Block.NULL){
      Dimension dim = calcStackDimensions(RenderableBlock.getRenderableBlock(rb.getBlock().
          getAfterBlockID()));
      return new Dimension(Math.max(rb.getBlockWidth() + rb.getMaxWidthOfSockets(rb.getBlockID()),
          dim.width),
          rb.getBlockHeight() + dim.height);
    } else {
      return new Dimension(rb.getBlockWidth() + rb.getMaxWidthOfSockets(rb.blockID),
          rb.getBlockHeight());
    }
  }

  /**
   * sets the label to belonging to this renderable block to
   * editing state == true (editing mode)
   */
  public void switchToLabelEditingMode(boolean highlighted){
    if (getBlock().isLabelEditable()){
      if (highlighted){
        blockLabel.setEditingState(true);
        blockLabel.highlightText();
      } else {
        blockLabel.setEditingState(true);
      }
    }
  }

  /**
   * returns the blockWidget for this RenderableBlock
   * @return the blockWidget for this RenderableBlock
   */
  JComponent getBlockWidget() {
    return blockWidget;
  }

  /**
   * @return the dimension of the sole block widget in this block.
   *        May NOT return null.
   */
  public Dimension getBlockWidgetDimension(){
    if (blockWidget == null) {
      return new Dimension(0, 0);
    } else {
      return blockWidget.getSize();
    }
  }

  /**
   * @param blockWidget
   *
   * @requires none
   * @modifies blockWidget
   * @effects sets block widget to the input argument "blockWidget"
   *       and revalidates the JComponent representation of RenderableBlock
   */
  public void setBlockWidget(JComponent blockWidget){
    if (blockWidget != null){
      remove(blockWidget);
    }
    this.blockWidget = blockWidget;
    if (blockWidget != null){
      add(blockWidget);
    }
    revalidate();

  }

  /**
   * Clears all renderable block instances and all
   * block instances
   */
  public static void reset(){
    if (DEBUG) {
      System.out.println("reseting all renderable blocks");
    }
    ALL_RENDERABLE_BLOCKS.clear();
    BlockUtilities.reset();
    Block.reset();
    BlockStub.reset();
    System.gc();
  }

  public JComponentDragHandler getDragHandler() {
    return dragHandler;
  }

  /**
   * Returns the BlockImageIcon instance at the specified location; null if
   * no BlockImageIcon exists at that location
   * @param location the ImageLocation of the desired BlockImageIcon
   * @return the BlockImageIcon instance at the specified location; null if
   * no BlockImageIcon exists at that location
   */
//  private BlockImageIcon getImageIconAt(ImageLocation location){
//    return blockImage.map.get(location);
//  }

  ///////////////////
  // LABEL METHODS //
  ///////////////////

  /**
   * Synchronizes this RenderableBlock's socket components (including tags, labels)
   * with the associated Block's list of sockets.
   * @effects for every socket in Block:
   *         (1) check/add corresponding tag structure,
   *         (2) check/add block label
   *             for every tag in Renderable:
   *         (3) delete any sockets not in Block
   * @complexity   Running time for n Block sockets
   *         and m Renderable tags: O(m+nm)=O(nm)
   */
  private boolean synchronizeSockets(){
    if (isCollapsed()) {
      return false;
    }
    boolean changed = false;
    List<ConnectorTag> newSocketTags = new ArrayList<ConnectorTag>();
    for (ConnectorTag tag : blockImage.socketTags){
      if (tag.getLabel() != null){
        remove(tag.getLabel().getJComponent());
      }
    }
    for (int i = 0; i < getBlock().getNumSockets(); i++) {
      BlockConnector socket = getBlock().getSocketAt(i);
      ConnectorTag tag = getConnectorTag(socket);
      if (tag == null){
        tag = new ConnectorTag(socket);
        if (SocketLabel.ignoreSocket(socket)) {
          tag.setLabel(null); //ignored sockets have no labels
        } else {
          SocketLabel label = new SocketLabel(socket, socket.getLabel(),
              BlockLabel.Type.PORT_LABEL, socket.isLabelEditable(), blockID);
          String argumentToolTip = getBlock().getArgumentDescription(i);
          if (argumentToolTip != null){
            label.setToolTipText(getBlock().getArgumentDescription(i).trim());
          }
          tag.setLabel(label);
          label.setZoomLevel(getZoom());
          label.setText(socket.getLabel());
          add(label.getJComponent());
          changed = true;
        }
      } else {
        SocketLabel label = tag.getLabel();
        if (!SocketLabel.ignoreSocket(socket)) {
          //ignored bottom sockets or sockets with label == ""
          if (label == null){
            label = new SocketLabel(socket, socket.getLabel(),
                BlockLabel.Type.PORT_LABEL, socket.isLabelEditable(), blockID);
            String argumentToolTip = getBlock().getArgumentDescription(i);
            if (argumentToolTip != null){
              label.setToolTipText(getBlock().getArgumentDescription(i).trim());
            }
            tag.setLabel(label);
            label.setText(socket.getLabel());
            add(label.getJComponent());
            changed = true;
          } else {
            label.setText(socket.getLabel());
            add(label.getJComponent());
            changed = true;
          }
          label.setZoomLevel(getZoom());
        }
      }
      newSocketTags.add(tag);
    }
    blockImage.socketTags.clear();
    blockImage.socketTags = newSocketTags;
    return changed;
  }

  /**
   * Change this block's genus
   * @param newGenus the new genus
   * @param preserveLabel if the label should be preserved, or reset to the genus's default
   */
  public void changeGenus(String newGenus, boolean preserveLabel) {
    blockLabel.genusChanged(newGenus, preserveLabel);
  }

  /**
   * Updates all the labels and sockets within this block.
   */
  private void synchronizeLabelsAndSockets(){
    boolean blockLabelChanged = getBlock().getBlockLabel() != null &&
                      !blockLabel.getText().equals(getBlock().getBlockLabel());
    boolean pageLabelChanged = getBlock().getPageLabel() != null &&
                        !pageLabel.getText().equals(getBlock().getPageLabel());
    boolean decoratorLabelChanged = getBlock().getDecoratorLabel() != null &&
               !decoratorLabel.getText().equals(getBlock().getDecoratorLabel());
    boolean socketLabelsChanged = false;

    if (getBlock().getNumSockets() == 0) {
      // in case we just deleted the last socket...
      socketLabelsChanged = synchronizeSockets();
    }

    // If tag label isn't the same as socket label, synchronize.
    // If the block doesn't have an editable socket label, synchronize.
    //
    // Needed to not synchronize the socket if it is label editable so it
    // doesn't synchronize when it gains focus.
    //
    // May possibly be done better if synchronizeSockets is rewritten. It has to
    // be written such that it doesn't remove the sockets' JComponents/remake
    // them. Currently relies on the synchronizeSockets() call in
    // getSocketPixelPoint(BlockConnector) to make sure the dimensions
    // and number of sockets are consistent.
    for (int i = 0; i < getBlock().getNumSockets(); i++) {
      BlockConnector socket = getBlock().getSocketAt(i);
      ConnectorTag tag = getConnectorTag(socket);
      if (tag != null) {
        if (tag.getLabel() != null){
          if (!tag.getLabel().getText().equals(socket.getLabel())){
            socketLabelsChanged = synchronizeSockets();
            break;
          }
        }
      }
      if (!socket.isLabelEditable()){
        socketLabelsChanged = synchronizeSockets();
        break;
      }
    }
    if (blockLabelChanged){
      blockLabel.setText(getBlock().getBlockLabel());
    }
    if (pageLabelChanged){
      pageLabel.setText(getBlock().getPageLabel());
    }
    if (decoratorLabelChanged) {
      decoratorLabel.setText(getBlock().getDecoratorLabel());
    }

    if (blockLabelChanged || pageLabelChanged || socketLabelsChanged || blockNotesChanged ||
        decoratorLabelChanged) {
      reformBlockShape();
      blockNotesChanged = false;
    }
    if (BlockLinkChecker.hasPlugEquivalent(getBlock())){
      BlockConnector plug = BlockLinkChecker.getPlugEquivalent(getBlock());
      Block plugBlock = Block.getBlock(plug.getBlockID());
      if (plugBlock != null) {
        if (plugBlock.getConnectorTo(blockID) == null) {
          throw new RuntimeException("one-sided connection from " + getBlock().getBlockLabel() +
              " to " + Block.getBlock(blockID).getBlockLabel());
        }
        RenderableBlock receiver = RenderableBlock.getRenderableBlock(plug.getBlockID());
        if (!receiver.isCollapsed()) {
          receiver.updateSocketSpace(plugBlock.getConnectorTo(blockID), blockID, true);
        }
      }
    }
  }

  /**
   * Determine the width necessary to accommodate for placed labels.  Used to
   * determine the minimum width of a block.
   * @return int pixel width needed for the labels
   */
   public int accommodateLabelsWidth() {
    int maxSocketWidth = 0;
    int width = 0;

    for (ConnectorTag tag : blockImage.socketTags){
      SocketLabel label = tag.getLabel();
      if (label != null) maxSocketWidth = Math.max(maxSocketWidth, label.getAbstractWidth());
    }
    if (getBlock().hasPageLabel()) {
      width += Math.max(blockLabel.getAbstractWidth(), pageLabel.getAbstractWidth()) +
          maxSocketWidth;
      width += getControlLabelsWidth() + decoratorLabel.getAbstractWidth();
    } else {
      width += blockLabel.getAbstractWidth() + maxSocketWidth;
      width += Math.max(getControlLabelsWidth(), decoratorLabel.getAbstractWidth()
          + getBlockNoteLabelsWidth()) + 4;
    }
    return width;
  }

   /**
    * @return pixel width needed for the comment label
    */
    public int getBlockNoteLabelsWidth() {
     return blockNotes.size() * BlockNoteLabel.BLOCK_NOTE_LABEL_SIZE;
   }

  public int accommodateSocketTagsWidth() {
    int maxSocketWidth = 0;
    for (ConnectorTag tag : blockImage.socketTags){
      SocketLabel label = tag.getLabel();
      if (label != null) {
        maxSocketWidth = Math.max(maxSocketWidth, label.getAbstractWidth());
      }
    }
    return maxSocketWidth + 4;
  }


  /**
   * Returns the minimum block height required to accommodate the page label,
   * decorator label, and block label for this block.  This version assumes
   * that the decorator label and the block label overlap vertically.
   * @return the minimum height
   */
  public int accommodateBlockLabelsHeight() {
    return accommodatePageLabelHeight() +
        Math.max(blockLabel.getAbstractHeight(), getDecoratorLabelHeight()) +
        // add a little padding
        4;
  }

  /**
   * Returns the width of the page label on this block; if page label
   * is not enabled and does not exist, returns 0.
   * @return the width of the page label on this block iff page label
   * is enabled and exists; returns 0 otherwise.
   */
  public int accommodatePageLabelHeight(){
    if (getBlock().hasPageLabel()) {
      return pageLabel.getAbstractHeight();
    } else {
      return 0;
    }
  }

  /**
   * Sets all the labels of this block as uneditable block labels.
   * Useful for Factory blocks.
   */
  public void setBlockLabelUneditable(){
    blockLabel.setEditable(false);
  }

  ////////////////////////////////////////
  // BLOCK IMAGE MANAGEMENT AND METHODS //
  ////////////////////////////////////////

  /**
   * Returns the total height of all the images to draw on this block
   * @return the total height of all the images to draw on this block
   */
  public int accommodateImagesHeight(){
    int maxImgHt = 0;
    for (BlockImageIcon img : getBlock().getInitBlockImageMap().values()){
      maxImgHt += img.getImageIcon().getIconHeight();
    }
    return maxImgHt;
  }

  /**
   * @return the total width of all the images to draw on this block
   */
  public int accommodateImagesWidth(){
    int maxImgWt = 0;
    for (BlockImageIcon img : getBlock().getInitBlockImageMap().values()){
      maxImgWt += img.getImageIcon().getIconWidth();
    }
    return maxImgWt;
  }

  //////////////////////////////////////////////
  //BLOCK LINKING CHECKS ON OTHER RENDERABLES //
  //////////////////////////////////////////////

  /**
   * Looks for links between this RenderableBlock and others.
   * @return a BlockLink object with information on the closest possible linking
   * between this RenderableBlock and another.
   * If block is collapsed it will return null.
   */
  public BlockLink getNearbyLink(){
    return BlockLinkChecker.getLink(this, Workspace.getInstance().getBlockCanvas().getBlocks());
  }

  ///////////////////////
  /// SOCKET METHODS ////
  ///////////////////////

  /**
   * Returns the maximum width between all the socket connectors of this or 0 if this does not
   * have any sockets
   * @return the maximum width between all the socket connectors of this or 0 if this does not
   * have any sockets
   */
  public int getMaxSocketShapeWidth() {
    int maxSocketWidth = 0;
    for (BlockConnector socket : getBlock().getSockets()) {
      int socketWidth = BlockConnectorShape.getConnectorDimensions(socket).width;
      if (socketWidth > maxSocketWidth) {
        maxSocketWidth = socketWidth;
      }
    }
    return maxSocketWidth;
  }

  /**
   * Returns a new Point object that represents the pixel location of this socket's center.
   * Mutating the new Point will not affect future calls to getSocketPoint; that is, this
   * method clones a new Point object.  The new Point object MAY NOT BE NULL.
   *
   * @param socket - the socket whose point we want.  socket MAY NOT BE NULL.
   * @return a Point representing the socket's center
   * @requires socket != null and socket is one of this block's socket
   */
  public Point getSocketPixelPoint(BlockConnector socket) {
    ConnectorTag tag = getConnectorTag(socket);
    if (tag != null)
      return tag.getPixelLocation();

    if (DEBUG) {
      System.out.println("Error, Socket has no connector tag: " + socket);
    }
    return new Point(0, -100); //JBT hopefully this doesn't hurt anything,
              // this is masking a bug that needs to be tracked down, why is the connector tag missing?
  }
  /**
   * Returns a new Point object that represents the abstract location of this socket's center.
   * Mutating the new Point will not affect future calls to getSocketPoint; that is, this
   * method clones a new Point object.  The new Point object MAY NOT BE NULL.
   *
   * @param socket - the socket whose point we want.  socket MAY NOT BE NULL.
   * @return a Point representing the socket's center
   * @requires socket != null and socket is one of this block's socket
   */
  public Point getSocketAbstractPoint(BlockConnector socket) {
    ConnectorTag tag = getConnectorTag(socket);
    return tag.getAbstractLocation();
  }
  /**
   * Updates the center point location of this socket
   *
   * @param socket - the socket whose point we will update.  Socket MAY NOT BE NULL
   * @param point - the ABSTRACT location of socket's center.  ABSTRACT LOCATION!!!
   *
   * @requires socket != null and there exist a matching tag for the socket
   */
  public void updateSocketPoint(BlockConnector socket, Point2D point) {
    ConnectorTag tag = getConnectorTag(socket);
    //TODO(user): what if tag does not exist?  should we throw exception or add new tag?
    tag.setAbstractLocation(point);

  }

  /**
   * Updates the renderable block with the underlying block's before,
   * after, and plug connectors.
   */
  public void updateConnectors() {
    Block b = Block.getBlock(blockID);
    afterTag.setSocket(b.getAfterConnector());
    beforeTag.setSocket(b.getBeforeConnector());
    plugTag.setSocket(b.getPlug());
  }

  /////////////////////////////////////
  // PARENT WORKSPACE WIDGET METHODS //
  /////////////////////////////////////

  /**
   * Returns the parent WorkspaceWidget containing this
   * @return the parent WorkspaceWidget containing this
   */
  public WorkspaceWidget getParentWidget(){
    return parent;
  }

  /**
   * Sets the parent WorkspaceWidget containing this
   * @param widget the desired WorkspaceWidget
   */
  public void setParentWidget(WorkspaceWidget widget){
    parent = widget;
  }

  /**
   * Overriding JComponent.contains(int x, int y) so that this component's
   * boundaries are defined by the actual area occupied by the Renderable
   * Block shape.  Returns true iff the specified coordinates are contained
   * within the area of the BlockShape.
   * @return true iff the specified coordinates are contained within the Area
   * of the BlockShape
   */
  @Override
  public boolean contains(int x, int y) {
    return blockImage.area.contains(x, y);
  }


  //////////////////////
  // BLOCK MANAGEMENT //
  //////////////////////
  /**
   * Shortcut to get block with current BlockID of this renderable block.
   */
  public Block getBlock() {
    return Block.getBlock(blockID);
  }

  public Color getColor(){
    return color;
  }

  public Color getActivatedColor(){
    return activatedColor;
  }

  public void setColor(Color color) {
    this.color = color;
    activatedColor = color;
  }

  public boolean activated() {
    return color == activatedColor;
  }


  /**
   * Links the default arguments of this block if it has any and if this block
   * has not already linked its default args in this session.  Re-linking this
   * block's default args every time it gets dropped/moved within the block
   * canvas can get annoying.
   */
  public void linkDefArgs(){
    if (!linkedDefArgsBefore && getBlock().hasDefaultArgs()){
      Iterator<Long> ids = getBlock().linkAllDefaultArgs().iterator();
      Iterator<BlockConnector> sockets = getBlock().getSockets().iterator();
      Long id;
      BlockConnector socket;

      // Store the ids, sockets, and blocks we need to update.
      List<Long> idList = new ArrayList<Long>();
      List<BlockConnector> socketList = new ArrayList<BlockConnector>();
      List<RenderableBlock> argList = new ArrayList<RenderableBlock>();
      while (ids.hasNext() && sockets.hasNext()){
        id = ids.next();
        socket = sockets.next();
        if (id != Block.NULL){
          //for each block id, create a new RenderableBlock
          RenderableBlock arg = new RenderableBlock(getParentWidget(), id);
          arg.setZoomLevel(zoom);
          //getParentWidget().addBlock(arg);
          //arg.repaint();
          //getParent().add(arg);
          //set the location of the def arg at
          Point myLocation = getLocation();
          Point2D socketPt = getSocketPixelPoint(socket);
          Point2D plugPt = arg.getSocketPixelPoint(arg.getBlock().getPlug());
          arg.setLocation((int)(socketPt.getX()+myLocation.x-plugPt.getX()), (int)(socketPt.getY()+myLocation.y-plugPt.getY()));
          //update the socket space of at this socket
          getConnectorTag(socket).setDimension(new Dimension(
              arg.getBlockWidth()-(int)BlockConnectorShape.NORMAL_DATA_PLUG_WIDTH,
              arg.getBlockHeight()));
          //drop each block to this parent's widget/component
          //getParentWidget().blockDropped(arg);
          getParentWidget().addBlock(arg);

          idList.add(id);
          socketList.add(socket);
          argList.add(arg);
        }
      }

      int size = idList.size();
      for (int i = 0; i < size; i++) {
        Workspace.getInstance().notifyListeners(
            new WorkspaceEvent(getParentWidget(),
                argList.get(i).getBlockID(),
                WorkspaceEvent.BLOCK_ADDED, true));

        //must call this method to update the dimensions of this
        //TODO ria in the future would be good to just link the default args
        //but first creating a block link object and then connecting
        //something like notifying the renderableblock to update its dimensions will be
        //take care of
        blockConnected(socketList.get(i), idList.get(i));
        argList.get(i).repaint();
      }
      redrawFromTop();
      linkedDefArgsBefore = true;
    }
  }

  /**
   * Modifies this RenderableBlock such that default
   * arguments are ignored.  In the future, invoking
   * linkDefArgs() will trigger no action.
   *
   * @requires none
   * @modifies linkedDefArgsBefore;
   * @effects sets linkedDefArgsBefore to false;
   */
  public void ignoreDefaultArguments(){
    linkedDefArgsBefore = true;
  }


  /**
   * Returns the Renderable specified by blockID; null if RenderableBlock does not exist
   * @param blockID the block id of the desired RenderableBlock
   * @return the Renderable specified by blockID; null if RenderableBlock does not exist
   */
  public static RenderableBlock getRenderableBlock(Long blockID){
    return ALL_RENDERABLE_BLOCKS.get(blockID);
  }

  /**
  * @return sll the RenderableBlocks on the canvas
   */
  public static Collection<RenderableBlock> getAllRenderableBlocks(){
    return  ALL_RENDERABLE_BLOCKS.values();
  }

  //// BLOCK RESIZING ////

  /**
   * Returns the dimension associated with a socket.  If a socket dimension has not yet
   * been set, this will return null.
   */
  public Dimension getSocketSpaceDimension(BlockConnector socket) {
    if (getConnectorTag(socket)==null)
      return null;
    else
      return getConnectorTag(socket).getDimension();
  }
  /**
   * Updates the socket socket space of the specified connectedSocket of this after a block
   * connection/disconnection.  The socket space specifies the dimensions of the block
   * with id connectedToBlockID. RenderableBlock will use these dimensions to
   * determine the appropriate bounds to stretch the connectedSocket by.
   * @param connectedSocket BlockConnector which block connection/disconnection occurred
   * @param connectedToBlockID the Long block ID of the block connected/disconnected to the specified connectedSocket
   * @param isConnected boolean flag to determine if a block connected or disconnected to the connectedSocket
   */
  private void updateSocketSpace(BlockConnector connectedSocket,
                                  long connectedToBlockID, boolean isConnected){
    if (DEBUG) {
      System.out.println("updating socket space of :" + connectedSocket.getLabel() + " of rb: " + this);
    }
    if (!isConnected){
      //remove the mapping
      getConnectorTag(connectedSocket).setDimension(null);

    } else {
      // Block connectedToBlock = Block.getBlock(connectedToBlockID);
      // if no before block, then no recursion
      // if command connector with position type bottom (just a control connector socket)
      // and we have a before, then skip and recurse up
      if (getBlock().getBeforeBlockID() != Block.NULL
          && BlockConnectorShape.isCommandConnector(connectedSocket)
          && connectedSocket.getPositionType() == BlockConnector.PositionType.BOTTOM) {

        //get before connector
        Long beforeID = getBlock().getBeforeBlockID();
        BlockConnector beforeSocket = Block.getBlock(beforeID).getConnectorTo(getBlockID());
        RenderableBlock.getRenderableBlock(beforeID).updateSocketSpace(beforeSocket, getBlockID(), true);
        return;
      }

      //if empty before socket, then return
      //if (getBlock().hasBeforeConnector() && getBlock().getBeforeBlockID() == Block.NULL) return;

      //add dimension to the mapping
      getConnectorTag(connectedSocket).setDimension(calcDimensionOfSocket(connectedSocket));
    }

    //reform shape with new socket dimension
    reformBlockShape();
    //next time, redraw with new positions and moving children blocks
    blockImage.clearBufferedImage();

    //after everything on this block has been updated, recurse upward if possible
    BlockConnector plugEquiv = BlockLinkChecker.getPlugEquivalent(getBlock());
    if (plugEquiv != null && plugEquiv.hasBlock()) {
      Long plugID = plugEquiv.getBlockID();
      BlockConnector socketEquiv = Block.getBlock(plugID).getConnectorTo(getBlockID());
      //update the socket space of a connected before/parent block
      RenderableBlock.getRenderableBlock(plugID).updateSocketSpace(socketEquiv, getBlockID(), true);
    }
  }

  /**
   * Calculates the dimensions at the specified socket
   * @param socket BlockConnector to calculate the dimension of
   * @return Dimension of the specified socket
   */
  private Dimension calcDimensionOfSocket(BlockConnector socket){
    Dimension finalDimension = new Dimension(0,0);
    long curBlockID = socket.getBlockID();
    while(curBlockID != Block.NULL) {
      Block curBlock = Block.getBlock(curBlockID);
      if (DEBUG) {
        System.out.println("evaluating block :" + curBlock.getBlockLabel());
      }
      RenderableBlock curRenderableBlock = RenderableBlock.getRenderableBlock(curBlockID);
      Dimension curRBSize = curRenderableBlock.getBlockSize();

      //add height
      finalDimension.height += curRBSize.height;
      //subtract after plug
      if (curBlock.hasAfterConnector()) {
        finalDimension.height -= BlockConnectorShape.CONTROL_PLUG_HEIGHT;
      }
      //set largest width by iterating through to sockets and getting
      //the max width ONLY if curBlockID == connectedToBlockID
      int width = curRBSize.width;

      if (curBlock.getNumSockets() > 0 && !curBlock.isInfix()){
        int maxSocWidth = getMaxWidthOfSockets(curBlockID);
        //need to add the placeholder width within bottom sockets if maxSocWidth is zero
        if (maxSocWidth == 0){
          // Adjust for zoom
          width += 2 * BlockShape.BOTTOM_SOCKET_SIDE_SPACER * curRenderableBlock.getZoom();
        }

        if (maxSocWidth > 0){
          //need to minus the data plug width, otherwise it is counted twice
          maxSocWidth -= BlockConnectorShape.NORMAL_DATA_PLUG_WIDTH;

          // Adjust for zoom
          width += maxSocWidth * curRenderableBlock.getZoom();
        }
      }

      if (width > finalDimension.width) finalDimension.width = width;

      //move down the afters
      curBlockID = Block.getBlock(curBlockID).getAfterBlockID();
    }
    return finalDimension;
  }

  /**
   * Redraws this RenderableBlock along with the RenderableBlocks
   * after it, which include after and socket blocks.  In other words,
   * this method redraws the stack of blocks that begin with this.
   * NOTE: this is inefficient, should only use this if needed
   * NOTE: Must call this after loading of blocks to update the socket
   * dimensions of this and set the isLoading flag to false
   */
  public void redrawFromTop() {
    if (WorkspaceControllerHolder.isHeadless()) {
      return;
    }
    isLoading = false;
    if (!isCollapsed()) {
      for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(getBlock())) {
        if (socket.hasBlock()) {
          //loop through all the afters of the connected block
          long curBlockID = socket.getBlockID();
          // TODO: this is a patch, but we need to fix the root of the problem!
          if (RenderableBlock.getRenderableBlock(curBlockID) == null) {
            System.out.println("does not exist yet, block: "+curBlockID);
            continue;
          }

          RenderableBlock.getRenderableBlock(curBlockID).redrawFromTop();

          //add dimension to the mapping
          getConnectorTag(socket).setDimension(calcDimensionOfSocket(socket));
        } else {
          getConnectorTag(socket).setDimension(null);
        }
      }
    }
    //reform shape with new socket dimension
    reformBlockShape();
    //next time, redraw with new positions and moving children blocks
    blockImage.clearBufferedImage();
  }

  // At end of loading...
  public void setInitialCollapse() {
    if (!WorkspaceControllerHolder.isHeadless()) {
      if (loadedAsCollapsed) {
        setCollapsed(true, true);
      }
    }
  }

  /**
   * Helper method for updateSocketSpace and calcStackDim.
   * Returns the maximum width of the specified blockID's socket blocks
   * @param blockID the Long blockID of the desired block
   */
  public int getMaxWidthOfSockets(Long blockID){
    int width = 0;
    Block block = Block.getBlock(blockID);
    RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);

    for (BlockConnector socket : block.getSockets()){
      Dimension socketDim = rb.getSocketSpaceDimension(socket);
      if (socketDim != null){
        if (socketDim.width > width)
          width = socketDim.width;
      }
    }

    return width;
  }

  /////////////////////
  //BLOCK CONNECTION //
  /////////////////////

  /**
   * Notifies this that its socket connectedSocket was connected to
   * another block with ID connectedBlockID.
   */
  public void blockConnected(BlockConnector connectedSocket, long connectedBlockID) {
    //notify block first so that we will only need to repaint this block once
    getBlock().blockConnected(connectedSocket, connectedBlockID);

    //synchronize sockets
    synchronizeSockets();

    // make sure the connected block is positioned correctly
    moveConnectedBlocks();

    updateSocketSpace(connectedSocket, connectedBlockID, true);

  }

  /**
   * Notifies this renderable block that its socket connectedSocket had a block
   * disconnected from it.
   */
  public void blockDisconnected(BlockConnector disconnectedSocket) {
    //notify block first so that we will only need to repaint this block once
    getBlock().blockDisconnected(disconnectedSocket);

    if (!WorkspaceControllerHolder.isHeadless()) {
      updateSocketSpace(disconnectedSocket, Block.NULL, false);
    }

    //synchronize sockets
    synchronizeSockets();
  }

  ///////////////////
  //BLOCK RENDERING//
  ///////////////////

  public void clearBufferedImage() {
    blockImage.clearBufferedImage();
  }

  /**
   * Clears the BufferedImage of this and repaint this entirely
   */
  public void repaintBlock() {
    blockImage.clearBufferedImage();
    if (isVisible()){
      //NOTE: If it's not visible, this will throw an exception.
      //as during the redraw, it will try to access location information
      //of this
      repaint();
      if (highlighter != null) {
        highlighter.repaint();
      }
    }
  }

  public void showBlock(boolean show) {
    setVisible(show);
    if (getParent() instanceof openblocks.workspace.FactoryCanvas) {
      FactoryCanvas canvas = (FactoryCanvas)getParent();
      canvas.layoutBlocks();
    }
  }

  /**
   * Swing paint method for J-Component
   * Checks to see if the buffer has been cleared (or yet to be created),
   * if so then it redraws the buffer and then draws the image on the graphics2d or
   * else it uses the previous buffer.
   */
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    if (!isLoading){
      // if blockImage.buffImg is null, redraw block shape
      if (blockImage.buffImg == null) {
        blockImage.updateBuffImg(); //this method also moves connected blocks
      }
      if (dragging) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,DRAGGING_ALPHA));
        g2.drawImage(blockImage.buffImg, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1));
      } else {
        g2.drawImage(blockImage.buffImg, 0, 0,  null);
      }
    }
  }

  /**
   * Reforms the blockShape of this renderableBlock and saves it into the block Area while
   * updating the bounds of this RenderableBlock.  Used to update the shape and socket
   * positions while avoiding a full updateBuffImg.
   * TODO(user) Investigate if the many calls on this causes a performance problem.
   */
  private void reformBlockShape() {
    blockImage.abstractArea = blockImage.shape.reformArea();
    AffineTransform at = new AffineTransform();
    at.setToScale(zoom, zoom);
    blockImage.area = blockImage.abstractArea.createTransformedArea(at);

    //note: need to add twice the highlight stroke width so that the highlight does not get cut off
    Rectangle updatedDimensionRect = new Rectangle(
        getX(),
        getY(),
        blockImage.area.getBounds().width,
        blockImage.area.getBounds().height);
    if (!getBounds().equals(updatedDimensionRect)) {
      moveConnectedBlocks(); // bounds have changed, so move connected blocks
    }
    setBounds(updatedDimensionRect);

    //////////////////////////////////////////
    //set position of block labels.
    //////////////////////////////////////////
    if (pageLabel != null && getBlock().hasPageLabel()){
      pageLabel.update();
    }
    if (blockLabel != null){
      blockLabel.update();
      //kengoldman:
      if (blockImage.shape.isIndented()) {
        blockLabel.setPixelLocation(blockLabel.getPixelLocation().x, 4);
      }
    }
    if (collapseLabel != null) {
      collapseLabel.update();
    }
    if (decoratorLabel != null) {
      decoratorLabel.update();
    }
    for (BlockNote bn : blockNotes) {
      bn.getBlockNoteLabel().update();
      bn.getArrow().update();
    }
    for (ConnectorTag tag : blockImage.socketTags){
      BlockConnector  socket = tag.getSocket();
      SocketLabel label = tag.getLabel();
      if (label != null && !SocketLabel.ignoreSocket(socket)){
        label.update(getSocketAbstractPoint(socket));
      }
    }
  }


  /*
   *
   * @return the Area of the block
   */
  public Area getBlockArea() {
    return blockImage.area;
  }

  /**
   * Sets the highlight color of this block.
   * The specified highlight may be overridden if this block has focus,
   * is a search result, or is "bad".  However when those states are no
   * longer active, the color is set back to the specified hlColor, if
   * resetHighlight() was not called in the meantime.
   * @param color the desired highlight Color
   */
  public void setBlockHighlightColor(Color color){
    if (highlighter != null) {
      highlighter.setHighlightColor(color);
    }
  }

  /**
   * Hides highlighting for this block.
   */
  public void resetHighlight(){
    if (highlighter != null) {
      highlighter.resetHighlight();
    }
  }

  /**
   * Tells this RenderableBlock to move its highlight handler to a new parent
   * (should be called after this RB is moved to a new parent)
   * @param blockParent the RBParent that is the RB's new parent
   */
  public void setHighlightParent(RBParent blockParent) {
    if (highlighter != null) {
      highlighter.setParent(blockParent);
    }
  }

  /**
   * Overridden from JComponent.
   * Returns true iff it has a parent, its parent is visible, and itself is
   * visible; false otherwise.
   * @return true iff it has a parent, its parent is visible, and itself is
   * visible; false otherwise.
   */
  @Override
  public boolean isVisible(){
    return super.isVisible() && getParent() != null && getParent().isVisible();
  }

  // BLOCK NOTE  MANAGEMENT //

  private void attach(BlockNote bn) {
    add(bn.getBlockNoteLabel());
    blockNotes.add(bn);
    blockNotesChanged = true;
    adjustForControlLabels();
    bn.update();
  }

  private void removeBlockNote(BlockNote bn){
    bn.delete();
    blockNotes.remove(bn);
    blockNotesChanged = true;
    adjustForControlLabels();
  }

  /**
   *  We want the initial location for balloons to be spread out and
   *  at heights proportional to their sizes.
   * @param width
   * @param height
   */
  void chooseLocationForBalloon(BlockNote bn, int width, int height) {
    Point loc = getLocation();
    Point relLoc = getUnscaledBlockNoteLabelLocation(bn.getBlockNoteLabel());
    loc.translate(relLoc.x, relLoc.y);
    int thisLabelsIndex = blockNotes.size();
    bn.setLocation((int) (loc.x + (thisLabelsIndex - 1) * (width + 5) * zoom),
                (int) (loc.y - (2 * height) * zoom));
  }

  /**
   * @return comment
   */
  public Comment getComment(){
    for (BlockNote bn : blockNotes) {
      if (bn instanceof Comment) {
        return (Comment) bn;
      }
    }
    Comment comment = new Comment(this);
    attach(comment);
    return comment;
  }

  public void removeComment(){
    removeBlockNote(getComment());
    // Yes, it may create a Comment only to delete it, but not often.
  }

 /**
   * @return true iff there is a comment
   */
  public boolean hasComment(){
    for (BlockNote bn : blockNotes) {
      if (bn instanceof Comment) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return complaint
   */
  public Complaint getComplaint() {
    for (BlockNote bn : blockNotes) {
      if (bn instanceof Complaint) {
        return (Complaint) bn;
      }
    }
    Complaint complaint = new Complaint(this);
    attach(complaint);
    return complaint;
  }

  public void removeComplaint(){
   removeBlockNote(getComplaint());
  }

  /**
   * @return true iff there is a complaint
   */
  public boolean hasComplaint() {
    for (BlockNote bn : blockNotes) {
      if (bn instanceof Complaint) {
        return true;
      }
    }
    return false;
  }

  public Report getReport(){
    for (BlockNote bn : blockNotes) {
      if (bn instanceof Report) {
        return (Report) bn;
      }
    }
    if (DEBUG) {
      System.out.println("Creating Report for" + getBlock().toString());
    }
    Report report = new Report(this);
    attach(report);
    return report;
  }

  public boolean hasReport(){
    for (BlockNote bn : blockNotes) {
      if (bn instanceof Report) {
        return true;
      }
    }
    return false;
  }


  public void removeReport(){
    removeBlockNote(getReport());
  }

  private void adjustForControlLabels() {
    if (!isLoading) {
      revalidate();
      if (highlighter != null) {
        highlighter.revalidate();
      }
      Container parent = getParent();
      if (parent != null) {
        parent.repaint();
      }
    }
  }

  // This is what Yail returns to signal the null value
  // It will be replaced by "Done." in REPL balloons
  // TODO(markf):In order to avoid coordination issues between the blocks editor and the REPL,
  // we've generally passed these magic strings into the REPL so that they're not
  // defined in two places.  See, for example, the definitions of
  // yacodeblocks.PhoneCommManager#REPL_OPEN_BRACKET and the constants right after it
  // and their uses below that in REPL_STARTUP_STR (which is sent to the phone REPL
  // in yacodeblocks.PhoneCommManager#psReplControllerRestart).
  private final String YAIL_NULL_VALUE_PRINTED_REP = "*nothing*";

  public void showReplResult(final String purpose, final boolean success,
      final String message) {
    // This method is called from the Read/Write thread from parseAndSendResponse
    // The synchronization is needed only for testing which depends upon reports and
    // complaints for the results of processing repl messages.
    synchronized(this) {
      pendingReplMessages++;
    }
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (!success) {
          ComplaintDepartment.showRuntimeComplaint(RenderableBlock.this, message);
        } else if (shouldDisplay(purpose)) {
          Report report = getReport();
          String finalMessage = message;
          if (finalMessage.equals(YAIL_NULL_VALUE_PRINTED_REP) &&
              !getBlock().hasPlug() && !getBlock().isVariableDeclBlock()) {
            finalMessage = "Done.";
          }
          report.setText(REPLResultToBalloonString(finalMessage));
          report.reformBlockNote();
          report.getBlockNoteLabel().setActive(true);
          //TODO(user) scrollToShowRectangle doesn't seem to work properly at the Swing level
          // and makes canvas jump around occasionally, so skip the following for now.
          // It attempts to insure that the newly displayed results are actually
          // on the screen.
          // Rectangle reportRect = getBounds();
          // reportRect = reportRect.union(r.getBounds());
          // Workspace.getInstance().getBlockCanvas().scrollToShowRectangle(reportRect);
        }
        synchronized(RenderableBlock.this) {
          if (--pendingReplMessages == 0) {
            RenderableBlock.this.notify();
          }
        }
      }
    });
  }



  private boolean shouldDisplay(String purpose) {
    if (purpose.equals(WorkspaceController.REPL_DO_IT)) {
      return true;
    }
    /* This test might cause a report coming back from the phone
     * to be ignored because it were sent between the time the user
     * clicked "Stop Watching" and the new definition of an event handler was
     * installed on the phone.
     */
    if (purpose.equals(BlockParser.REPL_DISPLAY_IT) &&
        getBlock().shouldReceiveReport()) {
      return true;
    }
    return false;
  }

  public int getPendingReplMessages() {
    return pendingReplMessages;
  }

  // MOVEMENT OF CONNECTED BLOCKS //

  /**
   * Aligns all RenderableBlocks plugged into this one with the current location of this RenderableBlock.
   * These RenderableBlocks to move include blocks connected at sockets and the after connector.
   * This is called via paint on the UI Thread, so don't call it gratuitously or races will result.
   */
  public void moveConnectedBlocks() {
    if (DEBUG) {
      System.out.println("move connected block " + this + " on " +
          Thread.currentThread().getName());
    }
    // if this hasn't been added anywhere, asking its location will break stuff
    if (getParent() == null) {
      return;
    }
    Block b = Block.getBlock(blockID);
    Point myScreenOffset = getLocation();
    for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(b)){
      if (socket.hasBlock()) {
        Point socketLocation = getSocketPixelPoint(socket);
        RenderableBlock rb = getRenderableBlock(socket.getBlockID());
        // TODO: djwendel - this is a patch, but the root of the problem
        // needs to be found and fixed!!
        if (rb == null) {
          System.out.println("Block doesn't exist yet: " + socket.getBlockID());
          continue;
        }

        Point plugLocation = rb.getSocketPixelPoint(BlockLinkChecker.getPlugEquivalent(Block.getBlock(socket.getBlockID())));
        Point otherScreenOffset = SwingUtilities.convertPoint(rb.getParent(), rb.getLocation(), getParent());
        otherScreenOffset.translate(-rb.getX(), -rb.getY());
        rb.setLocation((int)Math.round((float)myScreenOffset.getX()+socketLocation.getX()-(float)otherScreenOffset.getX()-plugLocation.getX()),
            (int)Math.round((float)myScreenOffset.getY()+socketLocation.getY()-(float)otherScreenOffset.getY()-plugLocation.getY()));
        rb.moveConnectedBlocks();
      }
    }
  }

  private static void startDragging(RenderableBlock renderable, WorkspaceWidget widget){
    renderable.pickedUp = true;
    renderable.lastDragWidget = widget;
    for (BlockNote bn : renderable.blockNotes){
      bn.setParent(Workspace.getInstance());
      bn.setConstrainBlockNote(false);
    }
    Component oldParent = renderable.getParent();
    Workspace.getInstance().addToBlockLayer(renderable);
    renderable.setLocation(SwingUtilities.convertPoint(oldParent,
        renderable.getLocation(), Workspace.getInstance()));
    renderable.setHighlightParent(Workspace.getInstance());
    for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(
        Block.getBlock(renderable.blockID))) {
      if (socket.hasBlock()) {
        startDragging(getRenderableBlock(socket.getBlockID()), widget);
      }
    }
  }

  /**
   * This method is called when this RenderableBlock is plugged into another
   * RenderableBlock that has finished dragging.
   * @param widget the WorkspaceWidget where this RenderableBlock is being
   * dropped.
   * @param skipDrop if true, don't drop the block on the widget.  useful
   * to prevent things from landing in the trash.
   */
  public static void stopDraggingInternal(RenderableBlock renderable,
                                          WorkspaceWidget widget,
                                          boolean skipDrop) {
    if (DEBUG) {
      if (!renderable.dragging) {
      System.out.println("A block was dropped without prior dragging.");
      //TODO(user) Figure out why this happens and if it matters.
      }
    }
    // notify children
    for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(renderable.getBlock())) {
      if (socket.hasBlock()) {
        stopDraggingInternal(getRenderableBlock(socket.getBlockID()), widget, skipDrop);
      }
    }
    // drop this block on its widget (if w is null it'll throw an exception)
    if (!skipDrop) {
      widget.blockDropped(renderable);
    } else {
      if (widget == null || !(widget instanceof TrashCan)) {
        throw new RuntimeException("Can only skip drop for the trashcan.");
      }
      TrashCan trash = (TrashCan)widget;
      trash.blockDroppedNoDelete();
    }
    // stop rendering as transparent
    renderable.dragging = false;

    for (BlockNote bn : renderable.blockNotes) {
      if (renderable.getParentWidget() != null){
        bn.setParent(renderable.getParentWidget().getJComponent(), 0);
      } else {
        bn.setParent(null, renderable.getBounds());
      }
      bn.setConstrainBlockNote(true);
      bn.setLocation(bn.getLocation());
    }
  }


/**
   * This method is called when this RenderableBlock is plugged into another
   * RenderableBlock that has finished dragging.
   * @param widget the WorkspaceWidget where this RenderableBlock is being
   * dropped.
   */
  public static void stopDragging(RenderableBlock renderable, WorkspaceWidget widget) {
    if (DEBUG) {
      if (!renderable.dragging) {
      System.out.println("Dropped block without prior dragging: " + renderable.toString());
      //TODO(user): Figure out if this should be a throw
      }
    }
    // Unfortunately, need to do a check here to see if we are going to be
    // deleting widgets, because we don't want to delete the inside of
    // procedures before deciding whether or not the procedure itself is
    // safe to be deleted.
    if (widget != null && (widget instanceof TrashCan)
        && (! BlockUtilities.deleteBlockUserConfirm(renderable,
                                false /* only confirm proc decl deletes */))) {
      // Prevent the drop to trash
      stopDraggingInternal(renderable, widget, true /* skip the drop */);
    } else {
      stopDraggingInternal(renderable, widget, false /* don't skip the drop */);
    }
  }


  private static void drag(RenderableBlock renderable, int dx, int dy, WorkspaceWidget widget,
      boolean isTopLevelBlock){
    if (DEBUG) {
      if (renderable.pickedUp) {
      System.out.println("Dragging block without prior pickup");
      }
    }
    // mark this as being dragged
    renderable.dragging = true;
    // move the block by drag amount
    if (!isTopLevelBlock){
      renderable.setLocation(renderable.getX() + dx, renderable.getY() + dy);
    }
    // send blockEntered/blockExited/blogDragged as appropriate
    if (widget != null && ! (widget instanceof FactoryManager)){
      if (!widget.equals(renderable.lastDragWidget)) {
        widget.blockEntered(renderable);
        if (renderable.lastDragWidget != null) {
          renderable.lastDragWidget.blockExited(renderable);
        }
      }
      widget.blockDragged(renderable);
      renderable.lastDragWidget = widget;
    }

    // translate highlight along with the block - this would happen automatically,
    // but putting the call here takes out any lag.
    if (renderable.highlighter != null) {
      renderable.highlighter.repaint();
    }
    // Propagate the drag event to anything plugged into this block
    for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(renderable.getBlock())) {
      if (socket.hasBlock()) {
        drag(getRenderableBlock(socket.getBlockID()),dx,dy, widget, false);
      }
    }
  }

  //MOUSE EVENTS   //
  /**
   * Makes public the protected processMouseEvent() method from Component so
   * that the children within this block may pass mouse events to this
   */
  @Override
  public void processMouseEvent(MouseEvent e){
    super.processMouseEvent(e);
  }

  public void mouseReleased(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      if (DEBUG) {
        if (!pickedUp) {
        System.out.println("dropping without prior dragging");
        }
      }
      dragHandler.mouseReleased(e);

      // if the block was dragged before...then
      if (dragging) {
        tryLinking(lastDragWidget);
      }
    }
    pickedUp = false;

    if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
      // add context menu at right click
      blockMenu.displayMenu(e);
    }
    Workspace.getInstance().getMiniMap().repaint();
  }

  public void tryLinking(WorkspaceWidget widget) {
    BlockLink link = getNearbyLink(); // look for nearby link opportunities
    // if a suitable link wasn't found, just drop the block
    if (link == null) {
      stopDragging(this, widget);
    } else {
      /* Make sure that no matter who's connecting to whom, the block
       * that's being dragged gets dropped on the parent widget of the
       * block that's already on the canvas.
       */
      if (blockID.equals(link.getSocketBlockID())) {
        // dragged block is the socket block, so take plug's parent.
        widget = getRenderableBlock(link.getPlugBlockID()).getParentWidget();
      } else {
        // dragged block is the plug block, so take the socket block's parent.
        widget = getRenderableBlock(link.getSocketBlockID()).getParentWidget();
      }

      // drop the block and connect its link
      stopDragging(this, widget);
      link.connect();
      Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, link,
          WorkspaceEvent.BLOCKS_CONNECTED));
      getRenderableBlock(link.getSocketBlockID()).moveConnectedBlocks();
    }

    // set the locations for X and Y based on zoom at 1.0
    unzoomedX = calculateUnzoomedX(getX());
    unzoomedY = calculateUnzoomedY(getY());

    Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, link,
        WorkspaceEvent.BLOCK_MOVED, true));
    if (widget instanceof MiniMap){
      Workspace.getInstance().getMiniMap().animateAutoCenter(this);
    }

  }

  void setActivate(boolean activate) {
    if (activate && getBlock() instanceof BlockStub) {
      BlockStub temp = (BlockStub) getBlock();
      Set<Long> parents = temp.getParents();
      if(parents != null && !Block.getBlock(parents.iterator().next()).activated()) {
        FeedbackReporter.showErrorMessage("You must activate the declaration of "
            + blockLabel.getText() + " first.");
        return;
      }
    }
    color = activate ? activatedColor : deactivatedColor;
    repaintBlock();
    // Notify workspace so change gets saved, and changed definitions sent
    // to phone.
    Workspace ws = Workspace.getInstance();
    int event = activate ? WorkspaceEvent.BLOCK_ACTIVATED
        : WorkspaceEvent.BLOCK_DEACTIVATED;
    ws.notifyListeners(new WorkspaceEvent(parent, getBlockID(), event));
    if (getBlock().isDeclaration()) {
      for (long bID : BlockStub.getStubsOfParent(getBlockID())) {
        RenderableBlock rb = getRenderableBlock(bID);
        rb.color = color;
        rb.repaintBlock();
        ws.notifyListeners(new WorkspaceEvent(parent, rb.getBlockID(), event));
      }
    }
  }



  public void mouseDragged(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      if (DEBUG) {
        if (!pickedUp) {
        System.out.println("dragging block without prior pickup");
        }
      }
      Point pp = SwingUtilities.convertPoint(this, e.getPoint(), Workspace.getInstance().getMiniMap());
      if (Workspace.getInstance().getMiniMap().contains(pp)) {
        Workspace.getInstance().getMiniMap().blockDragged(this, e.getPoint());
        lastDragWidget=Workspace.getInstance().getMiniMap();
        return;
      }

      // drag this block if appropriate (checks bounds first)
      dragHandler.mouseDragged(e);

      // Find the widget under the mouse
      dragHandler.myLoc.move(getX() + dragHandler.mPressedX, getY() + dragHandler.mPressedY);
      Point p = SwingUtilities.convertPoint(getParent(), dragHandler.myLoc,
          Workspace.getInstance());
      WorkspaceWidget widget = Workspace.getInstance().getWidgetAt(p);

      // if this is the first call to mouseDragged
      if (!dragging) {
        // TODO(sharon): consider making BlockUtilities.disconnectBlock
        // public and calling it here instead of repeating the
        // code. One difference: it calls blockParent.blockDisconnected(socket)
        // and we don't do that here.
        Block block = getBlock();
        BlockConnector plug = BlockLinkChecker.getPlugEquivalent(block);
        if (plug != null && plug.hasBlock()) {
          Block blockParent = Block.getBlock(plug.getBlockID());
          BlockConnector socket = blockParent.getConnectorTo(blockID);
          BlockLink link = BlockLink.getBlockLink(block, blockParent, plug, socket);
          link.disconnect();
          // socket is removed internally from block's socket list if socket is expandable
          RenderableBlock.getRenderableBlock(blockParent.getBlockID()).blockDisconnected(socket);

          // NOTIFY WORKSPACE LISTENERS OF DISCONNECTION
          Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, link,
              WorkspaceEvent.BLOCKS_DISCONNECTED));
        }
        startDragging(this, widget);
      }

      // drag this block and all attached to it
      drag(this, dragHandler.dragDX, dragHandler.dragDY, widget, true);

      Workspace.getInstance().getMiniMap().repaint();
    }
  }

  // show the pulldown icon if hasComboPopup = true
  public void mouseEntered(MouseEvent e) {
    dragHandler.mouseEntered(e);
    // !dragging: don't redraw while dragging
    // !SwingUtilities.isLeftMouseButton: dragging mouse moves into another block because of delay
    // !popupIconVisible: only update if there is a change
    // getBlock().hasSiblings(): only deal with blocks with siblings
    if (!SwingUtilities.isLeftMouseButton(e) && !dragging && getBlock().hasSiblings()) {
      blockLabel.showMenuIcon(true);
    }
  }

  public void mouseExited(MouseEvent e) {
    dragHandler.mouseExited(e);
    // !dragging: don't redraw while dragging
    // !SwingUtilities.isLeftMouseButton: dragging mouse moves into another block because of delay
    // popupIconVisible: only update if there is a change
    // getBlock().hasSiblings(): only deal with blocks with siblings
    if (!SwingUtilities.isLeftMouseButton(e) && !dragging && !blockImage.area.contains(e.getPoint())) {
      blockLabel.showMenuIcon(false);
    }
  }

  public void mouseMoved(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)){
      dragHandler.mouseClicked(e);
      if (e.getClickCount() == 2 && !dragging){
        Workspace.getInstance().notifyListeners(new WorkspaceEvent(getParentWidget(),
            getBlockID(), WorkspaceEvent.BLOCK_DOUBLE_CLICKED));
      }
    }
  }

  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)){
      dragHandler.mousePressed(e);
      pickedUp = true; // mark this block as currently being picked up
    }
  }

  // SEARCHABLE ELEMENT
  public String getKeyword() {
    return getBlock().getBlockLabel();
  }

  public String getGenus() {
    return getBlock().getGenusName();
  }

  public void updateInSearchResults(boolean inSearchResults) {
    isSearchResult = inSearchResults;
    if (highlighter != null) {
      highlighter.setIsSearchResult(isSearchResult);
    }
    // repaintBlock();
  }

  public boolean isSearchResult(){
    return isSearchResult;
  }

  // SAVING AND LOADING

  /**
   * Returns the save string of this
   * @return the save string of this
   */
  public String getSaveString(){
    StringBuilder saveString = new StringBuilder();

    if (isCollapsed()) {
      saveString.append("<Collapsed/>\n");
    }
    saveString.append("<Location>");
    saveString.append("<X>");
    saveString.append(descale(getX()));
    saveString.append("</X>");
    saveString.append("<Y>");
    saveString.append(descale(getY()));
    saveString.append("</Y>");
    saveString.append("</Location>\n");

    for (BlockNote bn : blockNotes){
      saveString.append(bn.getSaveString());
    }
    if (!activated()) {
      saveString.append("<Deactivated/>\n");
    }

    return getBlock().getSaveString(saveString.toString());
  }

  /**
   * Returns whether or not this is still loading data.
   * @return whether or not this is still loading data.
   */
  public boolean isLoading(){
    return isLoading;
  }


  /**
   * Loads a RenderableBlock and its related Block instance from the specified blockNode;
   * returns null if no RenderableBlock was loaded.
   * @param blockNode Node containing information to load into a RenderableBlock instance
   * @param parent WorkspaceWidget to contain the block to load
   * @return RenderableBlock instance holding the information in blockNode; null if no RenderableBlock loaded
   */
  public static RenderableBlock loadBlockNode(Node blockNode, WorkspaceWidget parent,
                                              HashMap<Long, Long> idMapping){
    boolean isBlock = blockNode.getNodeName().equals("Block");
    boolean isBlockStub = blockNode.getNodeName().equals("BlockStub");

    if ( isBlock || isBlockStub){
      RenderableBlock rb = new RenderableBlock(parent,
                  Block.loadBlockFrom(blockNode, idMapping).getBlockID(), true);

      if (isBlockStub){
        // need to get actual block node
        NodeList stubchildren = blockNode.getChildNodes();
        for (int j=0; j<stubchildren.getLength(); j++){
          Node node = stubchildren.item(j);
          if (node.getNodeName().equals("Block")){
            blockNode = node;
            break;
          }
        }
      }


      if (rb.getBlock().labelMustBeUnique()){
        // TODO check the instance number of this block
        // and update instance checker
      }

      Point blockLoc = new Point(0,0);
      NodeList children = blockNode.getChildNodes();
      Node child;


      for (int i=0; i<children.getLength(); i++){
        child = children.item(i);
        if (child.getNodeName().equals("Location")){
          // extract location information
          extractLocationInfo(child, blockLoc);
          rb.setLocation(blockLoc.x,blockLoc.y);
        } else if (child.getNodeName().equals("Comment")){
          rb.attach(Comment.loadComment(child.getChildNodes(), rb));
        } else if (child.getNodeName().equals("Report")){
          rb.attach(Report.loadReport(child.getChildNodes(), rb));
        } else if (child.getNodeName().equals("Complaint")){
          rb.attach(Complaint.loadComplaint(child.getChildNodes(), rb));
        } else if (child.getNodeName().equals("Collapsed")) {
          rb.loadedAsCollapsed = true;  // will complete collapses in Page loading.
        } else if (child.getNodeName().equals("Deactivated")) {
          rb.color = deactivatedColor;  // Don't use setter
        }

      }
      return rb;
    }
    return null;
  }

  /**
   * Read Location Node change loc to location in Node
   * @param location
   * @param loc
   */
  public static void extractLocationInfo(Node location, Point loc){
    NodeList coordinates = location.getChildNodes();
    Node coor;
    for (int j = 0; j<coordinates.getLength(); j++){
      coor = coordinates.item(j);
      if (coor.getNodeName().equals("X")){
        loc.x = Integer.parseInt(coor.getTextContent());
      } else if (coor.getNodeName().equals("Y")){
        loc.y = Integer.parseInt(coor.getTextContent());
      }
    }
  }


  /**
   * Changes Point boxSize (x,y) to the (width,height) of boxSizeNode
   * That is x = width and y = height
   * @param boxSize
   * @param boxSizeNode
   */
  public static void extractBoxSizeInfo(Node boxSizeNode, Dimension boxSize){
    NodeList coordinates = boxSizeNode.getChildNodes();
    Node coor;
    for (int j = 0; j<coordinates.getLength(); j++){
      coor = coordinates.item(j);
      if (coor.getNodeName().equals("Width")){
        boxSize.width = Integer.parseInt(coor.getTextContent());
      } else if (coor.getNodeName().equals("Height")) {
        boxSize.height = Integer.parseInt(coor.getTextContent());
      }
    }
  }


  @Override
  public String toString(){
    StringBuffer buf = new StringBuffer();
    buf.append("RenderableBlock " + getBlockID() + ": " + getBlock().getBlockLabel());
    return buf.toString();
  }

  /***********************************
   * State Saving Stuff for Undo/Redo *
   ***********************************/

  private class RenderableBlockState
  {
    public int x;
    public int y;
  }

  public Object getState()
  {
    RenderableBlockState blockState = new RenderableBlockState();
    blockState.x = getX();
    blockState.y = getY();
    return blockState;
  }

  public void loadState(Object memento)
  {
    assert (memento instanceof RenderableBlockState) : "ISupportMemento contract violated in RenderableBlock";
    if (memento instanceof RenderableBlockState)
    {
      RenderableBlockState state = (RenderableBlockState)memento;
      setLocation(state.x, state.y);
    }
  }

  /***************************************
   * Zoom support methods
   ***************************************/

  private double zoom = 1.0;
  public void setZoomLevel(double newZoom) {
    zoom = newZoom;

    // rescale internal components
    if (pageLabel != null && getBlock().hasPageLabel()) {
      pageLabel.setZoomLevel(newZoom);
    }
    if (blockLabel !=null) {
      blockLabel.setZoomLevel(newZoom);
    }
    if (collapseLabel != null) {
      collapseLabel.setZoomLevel(newZoom);
    }
    if (decoratorLabel != null) {
      decoratorLabel.setZoomLevel(newZoom);
    }
    plugTag.setZoomLevel(newZoom);
    afterTag.setZoomLevel(newZoom);
    beforeTag.setZoomLevel(newZoom);
    for (ConnectorTag tag : blockImage.socketTags) {
      tag.setZoomLevel(newZoom);
    }
    for (BlockNote bn : blockNotes) {
      bn.setZoomLevel(newZoom);
    }
  }

  /**
   * the current zoom for this RenderableBlock
   * @return the zoom
   */
  public double getZoom() {
    return zoom;
  }

  /**
   * @param x
   * @return a new int  x based on the current zoom
   */
  int rescale(int x){
    return (int)(x * zoom);
  }

  /**
   * @param x
   * @return a new double x position based on the current zoom
   */
  int rescale(double x){
    return (int)(x * zoom);
  }

  /**
   * that is given a scaled x it returns what that position would be when zoom == 1
   * @param x
   * @return the descaled x based on the current zoom
   * that is given a scaled x it returns what that position would be when zoom == 1
   */
  private int descale(int x){
    return (int)(x / zoom);
  }

  /**
   * returns the descaled x based on the current zoom
   * that is given a scaled x it returns what that position would be when zoom == 1
   * @param x
   * @return the descaled x based on the current zoom
   */
//  private int descale(double x){
//    return (int)(x / zoom);
//  }
//
  /**
   * calculates the x when the zoom is 1.0
   * @param x of the current position
   * @return the x when the zoom is 1.0
   */
  public int calculateUnzoomedX(int x){
    return (int)(x / zoom);
  }

  /**
   * calculates the y when the zoom is 1.0
   * @param y of the current position
   * @return the y when the zoom is 1.0
   */
  public int calculateUnzoomedY(int y){
    return (int)(y / zoom);
  }


  /**
   * mutator for the initial value of x
   * @param unzoomedX
   */
  public void setUnzoomedX(double unzoomedX) {
    this.unzoomedX = unzoomedX;
  }

  /**
   * mutator for the initial value of y
   * @param unzoomedY
   */
  public void setUnzoomedY(double unzoomedY) {
    this.unzoomedY = unzoomedY;
  }

  /**
   * observer for the initial value of x
   * @return initial value of x coordinate
   */
  public double getUnzoomedX() {
    return unzoomedX;
  }

  /**
   * observer for the initial value of y
   * @return initial value of x coordinate
   */
  public double getUnzoomedY() {
    return unzoomedY;
  }

  public void processKeyPressed(KeyEvent e){
    for (KeyListener l : getKeyListeners()){
      l.keyPressed(e);
    }
  }

  // Tool Tips
  @Override
  public JToolTip createToolTip(){
    return new CToolTip(new Color(255,255,225));
  }
  public void setBlockToolTip(String text){
    setToolTipText(text);
    blockLabel.setToolTipText(text);
    decoratorLabel.setToolTipText(text);
  }

  @Override
  protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
      int condition, boolean pressed){
    switch(e.getKeyCode()){
      case KeyEvent.VK_UP:
        return false;
      case KeyEvent.VK_DOWN:
        return false;
      case KeyEvent.VK_LEFT:
        return false;
      case KeyEvent.VK_RIGHT:
        return false;
      case KeyEvent.VK_ENTER:
        return false;
      default:
        return super.processKeyBinding(ks, e, condition, pressed);
    }
  }

  private ConnectorTag getConnectorTag(BlockConnector socket){
    if (socket == null) {
      throw new RuntimeException("Socket may not be null");
    }
    if (socket.equals(plugTag.getSocket())) {
      return plugTag;
    }
    if (socket.equals(afterTag.getSocket())) {
      return afterTag;
    }
    if (socket.equals(beforeTag.getSocket())) {
      return beforeTag;
    }
    // need to get the tags from the uncollapsed image, in case the block is collapsed
    for (ConnectorTag tag : normalImage.socketTags){
      if (socket.equals(tag.getSocket())) {
        return tag;
      }
    }
    return null;
  }

  /**
   * Override setVisible to make balloons follow directive
   * @param visible
   */
  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    for (BlockNote bn : blockNotes) {
      bn.setVisible(visible && bn.getBlockNoteLabel().isActive());
    }
  }

  /**
   * Returns the collapsed state if the block has a collapseLabel otherwise false.
   */
  public boolean isCollapsed() {
    if (collapseLabel != null) {
      return collapseLabel.isActive();
    }
    return false;
  }

  public void setCollapsed(boolean collapse) {
    setCollapsed(collapse, false);
  }

  /**
   * If this this block is collapsable, set its collapsed state according to
   * collapse.
   * @param collapse whether to collapse or expand
   * @param deferWorkspaceZoomChange if true do not force the Workspace to do
   * a global zoom change (for use in expanding multiple blocks)
   */

  public void setCollapsed(boolean collapse, boolean deferWorkspaceZoomChange) {
    if (collapseLabel == null) return;  // not a collapsable block, so ignore call
    if (collapseLabel.isActive() == collapse) {
      return;
    }
    collapseLabel.setActive(collapse);
    if (highlighter != null) {
      highlighter.setVisible(false);
    }
    if (collapse) {
      makeCollapsed();
    } else {
      setVisible(false);  // hide collapsed block
      blockImage = normalImage;
      // Show clump.
      for (ConnectorTag tag : normalImage.socketTags) {
        add(tag.getLabel().getJComponent());
      }
      add(decoratorLabel.getJComponent());
      mapV(true);
    }
    if (highlighter != null) {
      highlighter.setVisible(true);
    }
    repaintBlock();
    // Note(halabelson):  The purpose of this next line is to deal with the
    // bug where the zooming is changed while a block is collapsed, and then the
    // block gets expanded at a partially inconsistent sizing.  Resetting the workspace
    // zoom level is an awfully large hammer, but I can't find a less drastic way to do this.
    // It's insufficient to just to setZoomLevel on the block and its descendants.
    if (!deferWorkspaceZoomChange) {
      Workspace.getInstance().setWorkspaceZoom(Workspace.getInstance().getCurrentWorkspaceZoom());
    }

    // Nothing seems to look for BLOCK_COLLAPSE_CHANGE events
    Workspace.getInstance().notifyListeners(new WorkspaceEvent(parent, blockID,
        WorkspaceEvent.BLOCK_COLLAPSE_CHANGE));
    Workspace.getInstance().getMiniMap().repaint();
  }

  /**
   * Make collapsed initially; used during loading
   */
  public void makeCollapsed() {
    mapV(false);     // Hide clump.
    for (ConnectorTag tag : normalImage.socketTags) {
      remove(tag.getLabel().getJComponent());
    }
    blockImage = collapsedImage;
    setVisible(true); // expose collapsed version
    Workspace.getInstance().getMiniMap().repaint();
  }

  /**
   * Returns the width of the collapseLabel for this block if there is one, 0
   * otherwise
   * @return the width of the collapseLabel for this block if there is one, 0
   * otherwise
   */
  public int getCollapseLabelWidth() {
    return (collapseLabel == null) ? 0 : collapseLabel.getWidth();
  }

  /**
   * @return the maximum width of any labels at the left end of the block
   */
  int getControlLabelsWidth() {
   return getCollapseLabelWidth(); // Just one today.
  }

  /**
   * @return the abstract width of the decorator label or 0 if the decorator
   * is empty.
   */
  public int getDecoratorLabelWidth(){
    if (!decoratorLabel.getText().equals("")) {
      return decoratorLabel.getAbstractWidth();
    } else {
      return 0;
    }
  }

  /**
   * Returns the abstract height of the decorator label or 0 if the decorator
   * is empty.
   *
   * @return the abstract height of the decorator label or 0 if the decorator
   * is empty.
   */
  public int getDecoratorLabelHeight(){
    if (!decoratorLabel.getText().equals("")) {
      return decoratorLabel.getAbstractHeight();
    } else {
      return 0;
    }
  }

  /**
   * Returns the proper pixel location for the top left of a decorator
   * label in this block.  This will approximately place the decorator
   * label to the upper left of the name label.
   *
   * @return a Point with x and y equal to the unscaled pixel location that
   * a decorator label should be placed at.
   */
  public Point getUnScaledDecoratorLabelLocation(){
    int x = 1;
    int y = 2;
    if (getBlock().hasPlug()) {
      x += 3 + BlockConnectorShape.getConnectorDimensions(getBlock().getPlug()).width;
    }
    if (getBlock().hasBeforeConnector()) {
      y += 1;
    }
    if (getBlock().isInfix() && getBlock().getSocketAt(0) != null){
      if (!getBlock().getSocketAt(0).hasBlock()){
        x += 30;
      } else if (getSocketSpaceDimension(getBlock().getSocketAt(0)) != null) {
        x += getSocketSpaceDimension(getBlock().getSocketAt(0)).width + 2;
      }
      y += 1;
    }
    return new Point(x, y);
  }

  /**
   *
   * @return where the BlockNoteArrow should draw from, the middle of the button
   */
  public Point getArrowStartLocation(BlockNoteLabel whichLabel) {
    Point location = getLocation();
    location.translate(whichLabel.getX() + whichLabel.getWidth() / 2,
                       whichLabel.getY() + whichLabel.getHeight() / 2);
    return location;
  }

  /**
   * Returns the proper pixel location for the top left of a BlockNote label
   * in this block before scaling.  This will place the complaint labels just

   * to the right of any decorator label and BlockComment labels.
   *
   * @return a Point with x and y equal to the unscaled pixel location that
   * a BlockNote label should be placed at.
   */
  public Point getUnscaledBlockNoteLabelLocation(BlockNoteLabel which){
    Point location = getUnScaledDecoratorLabelLocation();
    int x = location.x + getDecoratorLabelWidth() + 1;
    int y = location.y + 2;
    for (BlockNote bn : blockNotes) {
      if (bn.getBlockNoteLabel() == which) {
        break;
      } else {
        x += BlockNoteLabel.BLOCK_NOTE_LABEL_SIZE;
      }
    }
    // If there are no BlockNotes, this report doesn"t mean much.
    return new Point(x, y);
  }

  public Point getUnscaledBlockLabelLocation() {
    int x = Math.max(getUnScaledDecoratorLabelLocation().x + getDecoratorLabelWidth(),
        getControlLabelsWidth());
    x += blockNotes.size() * BlockNoteLabel.BLOCK_NOTE_LABEL_SIZE;
    return new Point(x + 1, 4);
  }

  /*
   * @return the top-most block that leads to this
   */
  public RenderableBlock getTopmost() {
    RenderableBlock rb = this;
    while (true) {
      Block b = Block.getBlock(rb.blockID);
      if (b.hasBeforeConnector()) {
        RenderableBlock nrb = getRenderableBlock(b.getBeforeBlockID());
        if (nrb != null) {
          rb = nrb;
          continue;
        }
      }
      if (b.hasPlug()) {
        RenderableBlock nrb = getRenderableBlock(b.getPlugBlockID());
        if (nrb != null) {
          rb = nrb;
          continue;
        }
      }
      return rb;
    }
  }

  /* Make a family visible or invisible.
   */
  private void mapV(boolean visible) {
    for (RenderableBlock rb : getThisPlusDescendants()) {
      rb.setVisible(visible);
      // If any block in the clump was highlighted, make
      // sure to remove the highlighting.  The resetHighlight needs
      // to be done after the set visible, not before.  This is
      // probably because the Highlighter calls repaint() asynchronously
      // via invokeLater.
      if (!visible) {
        if (rb.highlighter != null) {
          rb.highlighter.resetHighlight();
        }
      }
    }
  }

  /* A descendant includes all the blocks reachable from this  by either sockets
   * or after plugs.
   */
  private Iterable<RenderableBlock> getThisPlusDescendants() {
    ArrayList<RenderableBlock> descendants = new ArrayList<RenderableBlock>();
    addToFamily(descendants);
    return descendants;
  }


  /* Helper for getThisPlus... methods.
   * recursively add this and all dependent blocks to family.
   * @param family the growing list of renderable blocks
   */
  private void addToFamily(ArrayList<RenderableBlock> family) {
    family.add(this);
    Block ab = Block.getBlock(Block.getBlock(blockID).getAfterBlockID());
    if (ab != null) {
      getRenderableBlock(ab.getBlockID()).addToFamily(family);
    }
    for (BlockConnector bc : Block.getBlock(blockID).getSockets()){
      if (bc.hasBlock()) {
        getRenderableBlock(bc.getBlockID()).addToFamily(family);
      }
    }
  }


  // Transform a string as received from the REPL to the string that should appear in the
  // report balloon.
  // This procedure is a no-op for now, but is left here as a level of indirection
  // in case we need it after looking more closely at the need to handle special characters.
  // in the phone/Blocks communication.  One example is how to handle newline characters,
  // this the balloons can't handle formatting, I don't think.
  // TODO(halabelson): Look at this more closely, together with YABlockCompiler.QuotifyForREPL
  // and the transformations in runtime.scm.
  private static String REPLResultToBalloonString(String s) {
    return s;
  }



}
