// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.workspace;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import openblocks.renderable.Comment;
import openblocks.renderable.RenderableBlock;
import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;

/**
 * A MiniMap is a miniturized representation of the
 * current Workspace's block canvas. Each MiniMap may
 * only be associated with only one block canvas.
 *
 * A MiniMap should display its associated set of
 * RenderableBlocks and Comments at their current position,
 * or render their motion in real time.
 *
 * The Minimap must tranform the block canvas's width:height ratio
 * such that it would fit within the MiniMap's area.
 * This may warp the blocks to appear thinner/wider
 * than their real size.
 *
 * The MiniMap should expand and shrink with some delay
 * as the user mouses over or moves the mouse out of focus.
 *
 * blockCanvas : BlockCanvas      //the block canvas that this MiniMap renders
 * blocks : Set<RenderableBlocks>       //the set of blocks that this renders
 * comments : Set<Comment>          //the set of comments that this renders
 * mapwidth : Integer         //this MiniMap's maximum width
 * mapheight: Integer         //this MiniMap's maximum height
 * ratio : Double               //the aspect ratio that this should maintain
 *                      //        when rendering this.blocks and this.comments
 */
public class MiniMap extends JPanel implements WorkspaceWidget, MouseListener, MouseMotionListener, SearchableContainer, PageChangeListener {
  /*
   * Implementation Notes:
   * -should be initialized BEFORE blockCanvas (that is, should initialize miniMap
   *  before invoking Workspace.initBlockCanvas)
   * -Pointer to the BlockCanvas is "updated" upon every repaint in case
   *  a different BlockCanvas is used half way in the game.
   *
   * Assumptions Made:
   * Workspace.instance not null upon first call to paint
   * Workspace.instance.blockCanvas not null upon first call to point
   *
   * Bug Tracking:
   * Problem: Throws exeption when dropping a stack into MiniMap
   * Solution: invoke w.blockDropped rather than block.stopDragging(w)
   *           in this.blockDropped(RenderableBLock).  Basically, calling
   *           stopDragging throws an error since the block was no longer
   *           draggin after being dropped on the MiniMap
   * Problem: block dots on corner
   * Solution: set miniMap to opaque(false) BUT call super.paint() in this.paint()
   * Problem: page divider; renders an empty slit
   * Solution: set miniMap to opaque(false) BUT call super.paint() in this.paint()
   */
  private static final long serialVersionUID = 328149080271L;
  /**the border width of the this mini map*/
  private static final int BORDER_WIDTH = 5;
  /**the default width of a mini map*/
  private static final int DEFAULT_WIDTH = 150;
  /**the default height of a mini map*/
  private static final int DEFAULT_HEIGHT = 75;
  /**this.width*/
  private int MAPWIDTH = 150;
  /**this.height*/
  private int MAPHEIGHT = 75;
  /**this.blockCanvas*/
  private BlockCanvas blockCanvas;
  private boolean expand = false;
  private final MiniMapEnlargerTimer enlarger;
  /**this.ratio*/
  private double transformX = 1;
  private double transformY = 1;

  private Color viewportOutlineColor = new Color(255, 150, 0);


  /**
   * @effect  constructs a MiniMap, M, such that
   *          M.blockCanvas = The current blockcanvas in Workspace &&
   *          M.blocks = M.blockCanvas.getBlocks &&
   *          M.comments = Set of all M.blockCanvas.getBlocks.getComment &&
   *          M.mapwidth = width parameter &&
   *          M.ratio = M.blockCanvas : M.wdith &&
   *          M.location = ALWAYS 16 pixels away from the upper-right edge corner
   */
  public MiniMap(){
    super();
    //this.blockCanvas=Workspace.getInstance().getBlockCanvas();
    //this.setBounds(500,0, MAPHEIGHT, MAPHEIGHT);
    this.setPreferredSize(new Dimension (MAPHEIGHT, MAPHEIGHT));
    this.setLayout(null);
    this.setOpaque(false);
    this.setFont(new Font("Ariel", Font.PLAIN, 9));
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.enlarger = new MiniMapEnlargerTimer();
    PageChangeEventManager.addPageChangeListener(this);
  }

  /**
   * @modifies this.preferredSize && this.size && this.isVisible
   * @effect mutates this MiniMap by making it invisible
   */
  public void hideMiniMap(){
    this.setSize(0,0);
    this.setPreferredSize(new Dimension(0,0));
    this.setVisible(false);
  }

  public void update(){
    this.repaint();
  }

  /**
   * Specified by codeblocks.workspace.SearchableContainer interface
   */
  public Iterable<RenderableBlock> getSearchableElements(){
    return new ArrayList<RenderableBlock>();
  }

  /**
   * Specified by codeblocks.workspace.SearchableContainer interface
   */
  public void updateContainsSearchResults(boolean containsSearchResults){
    this.repaint();
  }

  public void repositionMiniMap(){
    if(this.getParent() != null){
      this.setBounds(this.getParent().getWidth()-MAPWIDTH-26, 0,
          MAPWIDTH+2*BORDER_WIDTH, MAPHEIGHT+2*BORDER_WIDTH);
    }
  }

  /**
   * @modifies this.bounds && this.blockCanvas && this.blocks && this.comments
   * @effects 1] Point this.blockCanvas to whatever current block
   *       canvas is in Workspace
   *          2] Reset this.bounds to maintain aspect ratio and be
   *       16 pixels away from upper-right edge corner &&
   *          3] Rerender this.blocks and this.comment toreflect
   *       real-time relative positions and dimension
   */
  public void paint(Graphics g){
    //should paint super first then reset canvas.
    //using new canvas, find new height and ratio.
    super.paint(g);

    // draw shadow border
    for(int i = 0; i < BORDER_WIDTH;i++ ){
      g.setColor(new Color(200,200,150, 50*(i+1)));
      g.drawRect(i,i,this.getWidth()-1-2*i, this.getHeight()-1-2*i);
    }

    //Aspect-Ratio Logic
    this.blockCanvas=Workspace.getInstance().getBlockCanvas();
    this.transformX = (double)(MAPWIDTH)/this.getCanvas().getWidth();//MUST CAST MAPHEIGHT TO DOUBLE!!
    this.transformY = (double)(MAPHEIGHT)/this.getCanvas().getHeight();

    g.translate(5,5);
    Page page = this.blockCanvas.getTheOnlyPage();
    if (page != null) {
      Color pageColor = page.getPageColor();
      g.setColor(new Color(pageColor.getRed(), pageColor.getGreen(), pageColor.getBlue(), 200));
      Rectangle pageRect = rescaleRect(page.getJComponent().getBounds());
      g.fillRect(     pageRect.x,pageRect.y, pageRect.width, pageRect.height );
      g.setColor(Color.white);
      g.clipRect(pageRect.x,pageRect.y, pageRect.width, pageRect.height);
      g.drawString(page.getPageName(), pageRect.x+1, pageRect.height-3);
      if(page.getIcon()!=null && expand){
        g.drawImage(page.getIcon(), pageRect.x+1,pageRect.height-28,15,15,null);
      }
      g.setClip(null);
      for(Component component : page.getJComponent().getComponents()){
        //re-render this.blocks and this.comments
        if(component instanceof RenderableBlock && component != null && component.isVisible()){
          if(((RenderableBlock)component).isSearchResult()){
            g.setColor(Color.yellow);
          }else{
            g.setColor(((RenderableBlock)component).getColor());
          }
          drawBoundingBox(g, component);
        }else if(component instanceof Comment && component.isVisible()){
          g.setColor(Color.yellow);
          drawBoundingBox(g, component);
        }
      }

    }
    for(Component component : Workspace.getInstance().getComponentsInLayer(Workspace.DRAGGED_BLOCK_LAYER)){
      if(component instanceof RenderableBlock && component != null && component.isVisible()) {
        g.setColor(((RenderableBlock)component).getColor());
        drawBoundingBox(g, component);
      }else if(component instanceof Comment && component.isVisible()){
        g.setColor(Color.yellow);
        drawBoundingBox(g, component);
      }
    }
    g.setColor(viewportOutlineColor);
    g.drawRect(
        rescaleX(blockCanvas.getHorizontalModel().getValue()),
        rescaleY(blockCanvas.getVerticalModel().getValue()),
        rescaleX(blockCanvas.getWidth()),
        rescaleY(blockCanvas.getHeight()));
  }

  /**
   * @effect Renders a JComponent by drawing a rectangle around
   *     its bounding box (rescaled to fit MiniMap) using
   *     the given graphics context
   */
  private void drawBoundingBox(Graphics g, Component block){
    Rectangle blockRect = block.getBounds();
    blockRect.setLocation(SwingUtilities.convertPoint(
        block.getParent(),
        blockRect.getLocation(),
        getCanvas()));
    blockRect = rescaleRect(blockRect);
    g.fillRect(     (blockRect.x),
        (blockRect.y),
        (blockRect.width),
        (blockRect.height));
    g.setColor(Color.white);
    g.drawRect(     (blockRect.x),
        (blockRect.y),
        (blockRect.width),
        (blockRect.height));
  }

  /**
   * @return viewPort of this.blockCanvas
   */
  private JComponent getCanvas(){
    return blockCanvas.getCanvas();
  }

  /**
   * Scales i to current aspect ratio.
   * @param p
   *
   * @requires p != null
   * @return new Point that is a copy of "p" tranformed
   *           by (this.transformX, this.transformY)
   */
  private int rescaleX(int x){
    return (int)(x*this.transformX);
  }
  private int rescaleY(int y){
    return (int)(y*this.transformY);
  }
  /*      private Point rescale(Point p){
          Point point = new Point((int)(p.x*this.transformX), (int)(p.y*this.transformY));
          return point;
          }*/
  private Rectangle rescaleRect(Rectangle rec){
    return new Rectangle(
        (int)(rec.x*this.transformX),
        (int)(rec.y*this.transformY),
        (int)(rec.width*this.transformX),
        (int)(rec.height*this.transformY));
  }

  /**
   * Scales i to World using curent aspect ratio
   * @param p
   *
   * @requires p != null && this.transformX != 0 && this.transformY != 0
   * @return new Point that is a copy of "p" tranformed
   *           by (1/this.transformX, 1/this.transformY)
   */
  private Point rescaleToWorld(Point p){
    Point point = new Point((int)(p.x/this.transformX), (int)(p.y/this.transformY));
    return point;
  }

  /**
   * Set this.blockCanvas.viewport to be centered around p if posible
   * @param p
   */
  private void scrollToPoint(Point p){
    Point transform = rescaleToWorld(p);
    blockCanvas.getHorizontalModel().setValue((int)(transform.x-0.5*blockCanvas.getWidth()));
    blockCanvas.getVerticalModel().setValue((int)(transform.y-0.5*blockCanvas.getHeight()));
    this.repaint();
  }

  /**
   * When dragging along miniMap, zoom to new point
   */
  public void mouseDragged(MouseEvent e){
    scrollToPoint(e.getPoint());
  }
  /**
   * When releasing a mouse in a MiniMap, scroll to point
   */
  public void mouseReleased(MouseEvent e) {
    scrollToPoint(e.getPoint());
  }
  /**MouseEvent methods not interested by this WorkspceWidget**/
  public void mouseMoved(MouseEvent e){}
  public void mousePressed(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {
    expand = true;
    enlarger.expand();
  }
  public void mouseExited(MouseEvent e) {
    expand = false;
    enlarger.shrink();
  }
  public void mouseClicked(MouseEvent e) {}

  /**
   * @param block
   * @param WorkspacePoint
   * @modifies block
   * @effects  change block.location to canvas point
   *           represented by WorkspacePoint.  In other words,
   *           find what point WorkspacePoint represents on the
   *           MiniMap adn find its corresponding point on the
   *           blockCanvas.  Then set block.location to that point
   *           on the blockCanvas.
   */
  public void blockDragged(RenderableBlock block, Point WorkspacePoint){
    Point mapPoint = SwingUtilities.convertPoint(
        block,
        WorkspacePoint,
        this);
    mapPoint.translate(-6,-6);
    Point worldPoint = SwingUtilities.convertPoint(
        this.getCanvas(),
        rescaleToWorld(mapPoint),
        Workspace.getInstance());

    //checking bounds
    int width = block.getStackBounds().width+3;
    int height = block.getStackBounds().height+3;
    int canvasWidth = this.getCanvas().getWidth()-Workspace.getInstance().getCanvasOffset().width;
    int canvasHeight = this.getCanvas().getHeight()-Workspace.getInstance().getCanvasOffset().height;
    if(worldPoint.y+height>canvasHeight){
      worldPoint.setLocation(worldPoint.x, canvasHeight-height);
    }
    if(worldPoint.x+width>canvasWidth){
      worldPoint.setLocation(canvasWidth-width, worldPoint.y);
    }

    block.setLocation(worldPoint);
    block.getDragHandler().myLoc.setLocation(worldPoint);
    block.moveConnectedBlocks();
    this.repaint();
  }

  /**
   * @param block
   * @modifies block
   * @effects  Set block to whatever page it is on is one exist.
   *       Otherwise, set it to whatever Widget is currently underneath
   *       it.
   */
  public void blockDropped(RenderableBlock block) {
    // TODO - djwendel - this needs to choose the widget of the top block of the stack, not the widget under the block necessarily
    Point location = block.getLocation();
    if(location.getY()<=0)location.setLocation(location.getX(), 1);
    //find page that it should drop on. w could be null.  so watch out.
    WorkspaceWidget w = Workspace.getInstance().getWidgetAt(location);
    Page page = this.blockCanvas.getTheOnlyPage();
    if (page != null) {
      if (page.contains(SwingUtilities.convertPoint(block.getParent(), location, page.getJComponent()))){
        w=page;
      }
    }

    /* before dropping, check if any connected blocks were dropped elsewhere.
       if they were, drop there instead. (this relies on the fact that stacks
       of blocks are dropped bottom-first, so the place to check is at the
       socket blocks.  If stacks were dropped top-first, this would check the
       blocks up the stack rather than down the stack. TODO - this doesn't
       always work, because there can be multiple "branches" that might get
       dropped on different places.  Need to find a way to drop according the
       widget of the top block of the stack...*/
    RenderableBlock socketBlock;
    for (BlockConnector con : (Block.getBlock(block.getBlockID()).getSockets())) {
      socketBlock = RenderableBlock.getRenderableBlock(con.getBlockID());
      if (socketBlock != null) {
        w = socketBlock.getParentWidget();
      }
    }
    w.blockDropped(block);
    this.repaint();
  }

  /**Block action methods not interested by MiniMap.  Does nothing*/
  public void blockEntered(RenderableBlock block){}
  public void blockExited(RenderableBlock block) {}
  public void blockDragged(RenderableBlock block) {}
  public void addBlock(RenderableBlock block) {}
  public void addBlocks(Collection<RenderableBlock> blocks) {}
  public void removeBlock(RenderableBlock block) {}

  /**JComponent representation of this*/
  public JComponent getJComponent() {
    return this;
  }

  /**defined by JComponent.contains()*/
  public boolean contains(int x, int y) {
    return new Rectangle(7,7,this.getWidth()-15, this.getHeight()-15).contains(x, y);
  }

  public Collection<RenderableBlock> getBlocks(){
    return new ArrayList<RenderableBlock>();
  }

  /**
   * Animate A fly to block's location on the MiniMap and BlockCanvas
   * @param block
   */
  public void animateAutoCenter(RenderableBlock block){
    //Zoom to new position slowly.  Let MiniMapAutoCenter manage the zooming
    MiniMapAutoCenterTimer timer = new MiniMapAutoCenterTimer(block);
    timer.start();
  }

  /**
   * Used to zoom to the center of a particular "block".
   * MiniMapAutoCenterTimer moves closer to the center of the block
   * by (dx, dy) for every 5 milliseconds and continues to do so
   * for "count" number of times.
   *
   * It wraps a Timer that stops only when count is less than 0.
   */
  private class MiniMapAutoCenterTimer implements ActionListener{
    /**Internal Timer*/
    private javax.swing.Timer timer;
    /**Number of repititions*/
    private int count;
    /**Change in x per repitition*/
    private int dx;
    /**Change in y per repitition*/
    private int dy;
    /**Constructs this*/
    public MiniMapAutoCenterTimer(RenderableBlock block){
      timer = new javax.swing.Timer(5, this);
      count = 25;
      Point blockPosition = SwingUtilities.convertPoint(
          block,
          new Point((int)(block.getStackBounds().getWidth()/2), (int)(block.getStackBounds().getHeight()/2)),
          getCanvas());
      dx = (blockPosition.x-(blockCanvas.getHorizontalModel().getValue()+blockCanvas.getWidth()/2))/count;
      dy = (blockPosition.y-(blockCanvas.getVerticalModel().getValue()+blockCanvas.getHeight()/2))/count;
    }
    /**starts internal Timer*/
    public void start(){timer.start();}
    /**stops internal Timer*/
    public void stop(){timer.stop();}
    /**Repositions view in blockCanvas iff count>=0; Otherwise stop internal Timer*/
    public void actionPerformed(ActionEvent e){
      if(count<0){
        timer.stop();
      }else{
        blockCanvas.getHorizontalModel().setValue(blockCanvas.getHorizontalModel().getValue()+dx);
        blockCanvas.getVerticalModel().setValue(blockCanvas.getVerticalModel().getValue()+dy);
        count--;
      }
    }
  }

  /**
   * This animator is responsible for enlarging or shrinking the
   * size of the MiniMap when expand() or shrink() is called,
   * respectively.
   */
  private class MiniMapEnlargerTimer implements ActionListener{
    /**Growth count*/
    private int count;
    /**Internal Timer*/
    private javax.swing.Timer timer;
    /**absolute value of width growth*/
    private int dx = DEFAULT_WIDTH/10;;
    /**absolute value of height Growth*/
    private int dy = DEFAULT_HEIGHT/10;;
    /**Indicates whether the MiniMap is/was expanding (true)
     * or skrinking (false)*/
    private boolean expand;
    private javax.swing.Timer delayTimer;

    /**
     * Constuctors an animator that can enlarge or skrink the miniMap
     */
    public MiniMapEnlargerTimer(){
      count = 0;
      this.expand = true;
      timer = new Timer(10, this);
      delayTimer = new Timer(1000,new ActionListener(){
          public void actionPerformed(ActionEvent e){
            delayTimer.stop();
            timer.start();
          }
        });
    }
    /**
     * expands/shrinks the miniMap untill count is 0 or 15.
     * At 0, the map is smallest as possible and at 15, the
     * map is largest as possible
     */
    public void actionPerformed(ActionEvent e){
      if(count<=0 || count>15){
        timer.stop();
      }else{
        if(expand){
          count = count + 1;
        }else{
          count = count -1;
        }
        MAPWIDTH = DEFAULT_WIDTH+count*dx;
        MAPHEIGHT = DEFAULT_HEIGHT+count*dy;
        repositionMiniMap();
        repaint();
      }
    }
    /**
     * enlargest this minimap
     */
    public void expand(){
      this.expand = true;
      count++;
      //this.delayTimer.start();
      this.timer.start();
    }

    /**
     * shrinks this minimap
     */
    public void shrink(){
      count--;
      this.expand=false;
      this.delayTimer.start();
      //this.timer.start();
    }
  }
}
