package openblocks.workspace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;

import openblocks.renderable.RenderableBlock;
import openblocks.yacodeblocks.HoverMenu;
import openblocks.yacodeblocks.HoverMenuController;
import openblocks.yacodeblocks.WorkspaceControllerHolder;
import openblocks.codeblockutil.CGraphite;
import openblocks.codeblockutil.CHoverScrollPane;
import openblocks.codeblockutil.CScrollPane;
import openblocks.codeblockutil.CScrollPane.ScrollPolicy;


/**
 * A BlockCanvas is a container of a single Page and is a scrollable
 * panel.
 *
 * A BlockCanvas is also a PageChangeListener.  When a
 * page is changed, the Blockcanvas must update itself
 * appropriately to reflect this change.
 *
 * As of the current implementation, the BlockCanvas must
 * have at least one Page when it becomes visible (that is,
 * when its viewable JComponent becomes visible).
 */
public class BlockCanvas implements PageChangeListener, ISupportMemento  {
  /** serial version ID */
  private static final long serialVersionUID = 7458721329L;
  /** the single page that this BlockCanvas stores */
  private Page thePage = null;
  /** The Swing representation of the page container */
  private JComponent canvas;
  /** The scrollable JComponent representing the graphical part of this BlockCanvas */
  private CScrollPane scrollPane;

  private final HoverMenu hoverMenu = new HoverMenu();
  private final HoverMenuController hoverMenuController = new HoverMenuController(hoverMenu);

  // The width of the vertical scroll bar in pixels and the height of
  // the horiztontal scroll bar in pixels
  private static final int SCROLL_THUMB_SIZE = 18;

  //////////////////////////////
  //Constructor/Destructor    //
  //////////////////////////////

  /**
   * Constructs BlockCanvas and subscribes
   * this BlockCanvas to PageChange events
   */
  public BlockCanvas() {
    canvas = new Canvas();
    scrollPane = new CHoverScrollPane(canvas,
        ScrollPolicy.VERTICAL_BAR_ALWAYS,
        ScrollPolicy.HORIZONTAL_BAR_ALWAYS,
        18,CGraphite.lightgreen, CGraphite.scrollbar_background);
    scrollPane.setScrollingUnit(5);
    canvas.setLayout(null);
    canvas.setBackground(Color.gray);
    canvas.setOpaque(true);
    PageChangeEventManager.addPageChangeListener(this);

    hoverMenuController.addTarget(canvas);
  }


  /**
   * @effects resets BlockCanvas by removing the page (if any), and blocks.
   *          resets HoverMenu
   */
  public void reset() {
    thePage = null;
    canvas.removeAll();
    scrollPane.revalidate();
    hoverMenu.reset();
  }

  //////////////////////////////
  //Rendering View Accessor   //
  //////////////////////////////

  /** @return X Coordinate of BlockCanvas graphical representation */
  public int getX(){
    return scrollPane.getX();
  }

  /** @return Y coordinate of BlockCanvas graphical representation */
  public int getY(){
    return scrollPane.getY();
  }

  /** @return width of BlockCanvas graphical representation */
  public int getWidth(){
    return scrollPane.getWidth();
  }

  /** @return height of BlockCanvas graphical representation */
  public int getHeight(){
    return scrollPane.getHeight();
  }

  /** @return vertical scroll bar bounding range model.  MAY BE NULL */
  public BoundedRangeModel getVerticalModel(){
    return scrollPane.getVerticalModel();
  }

  /** @return horizontal scroll bar bounding range model.  MAY BE NULL */
  public BoundedRangeModel getHorizontalModel(){
    return scrollPane.getHorizontalModel();
  }

  /**
   * @return the Swing Container that holds all the graphical panels of
   *          all the pages in this BlockCanvas
   */
  public JComponent getCanvas(){
    return this.canvas;
  }

  /**
   * @return JComponent representation of this
   * @warning Please take special care in using this method, as it exposes
   *          implementation details.
   */
  public JComponent getJComponent() {
    return scrollPane;
  }

  /** @return string representation of this */
  @Override
  public String toString(){
    return "BlockCanvas " + ( thePage == null ? "no pages." : "one page." ) ;
  }

  //////////////////////////////
  //Block Mutators/Accessors  //
  //////////////////////////////

  /**
   * @return the RendearbleBlocks that are contained within this widget
   *          or an empty Iterable if no blocks exists
   */
  public Iterable<RenderableBlock> getBlocks() {
    ArrayList<RenderableBlock> allPageBlocks = new ArrayList<RenderableBlock>();
    if (thePage != null) {
      allPageBlocks.addAll(thePage.getBlocks());
    }
    return allPageBlocks;
  }
  /**
   * @effects Automatically arranges all the blocks within this.
   */
  public void arrangeAllBlocks(){
    if (thePage != null) {
      thePage.reformBlockOrdering();
    }
  }

  /**
   * @effects Collapse all blocks on this canvas that can be collapsed.
   */
  public void collapseAllBlocks(){
    if (thePage != null) {
      thePage.collapseAllBlocks();
    }
  }

  /**
   * @effects Expand all blocks on this canvas that were previously collapsed.
   */
  public void expandAllBlocks(){
    if (thePage != null) {
      thePage.expandAllBlocks();
    }
  }

  /**
   * @effects Resize the canvas to have no extra X and Y space past the blocks currently placed
   */
  public void resizePage(){
    if (thePage != null) {
      thePage.resizePage(SCROLL_THUMB_SIZE);
    }
  }

  /**
   * @return a collection of top level blocks within this page (blocks with no
   *          parents that and are the first block of each stack) or an empty
   *          collection if no blocks are found on this page.
   */
  public Iterable<RenderableBlock> getTopLevelBlocks(){
    return null;
  }

  /**
   * @param r - the Rectangle to make sure is shown in the viewport
   * @requires r != null
   * @modifies the vertical and horizontal scrollbar boundedRangeModels
   * @effects This method causes the workspace to scroll if needed
   *          to completely show the rectangle -- at least the NorthWest
   *          corner of it if it's too big.
   */

  public void scrollToShowRectangle(Rectangle r) {
    // NOTE(lizlooney) this code doesn't have the desired behavior.
    // scrollPane.scrollRectToVisible(r);
  }

  /**
   * @param block - the RenderableBlock to make sure is shown in the viewport
   * @requires block ! null
   * @modifies the vertical and horizontal scrollbar boundedRangeModel
   * @effects This method causes the workspace to scroll if needed
   *          to complete show the given RenderableBlock.
   */
  public void scrollToShowBlock(RenderableBlock block) {
    // NOTE(lizlooney) this method is never called. Maybe it works; maybe it doesn't.
    scrollPane.scrollRectToVisible(block.getBounds());
  }

  /**
   * @param rect - the rectangle of interest
   * @param hm the horizontal scroll bar state
   * @param vm the vertical scroll bar state
   * @requires c != null
   * @return true iff c is visible now
   *    with respect to the bounded range model. It hm and vm are the
   *    scroll bars of the blocks editor, the user can really see the
   *    rectangle.
   */
  public boolean visibleRightNow(Rectangle rect,
      BoundedRangeModel hm,
      BoundedRangeModel vm) {
    Rectangle visibleArea = new Rectangle(hm.getValue(),
        vm.getValue(),
        hm.getValue() + hm.getExtent(),
        vm.getValue() + vm.getExtent());
    return visibleArea.contains(rect);
  }

  public void scrollToComponent(JComponent c) {
    // NOTE(lizlooney) this method is only called from Workspace.scrollToComponent, which is
    // never called. Maybe it works; maybe it doesn't.
    scrollPane.scrollRectToVisible(c.getBounds());
  }

  //////////////////////////////
  //Page Mutators/Accessors   //
  //////////////////////////////

  // Can return null
  public Page getTheOnlyPage() {
    return thePage;
  }

  /**
   * @param page the page to add to the BlockCanvas
   *
   * @requires page != null and there is no current page
   * @modifies this.thePage
   * @effects Makes the specified page the only page.
   * @throws RuntimeException if (page == null)
   */
  public void addPage(Page page) {
    if(page == null){
      throw new RuntimeException("Invariant Violated: May not add null Pages");
    }
    if (thePage != null) {
      throw new RuntimeException("Invariant Violated: May not add a page; one already exists.");
    }
    thePage = page;
    canvas.add(page.getJComponent(), 0);
    PageChangeEventManager.notifyListeners();
  }

  /**
   * @param page - the page to be removed
   *
   * @requires page == thePage
   * @modifies this.thePage
   * @effects Removes the given Page from the BlockCanvas.
   *          If specified page not found in BlockCanvas, do nothing.
   *          if more than one page equals the specified page, then remove
   *          the first equal() instance.
   */
  public Page removePage(Page page) {
    if (page != null){
      // clear the blocks from the page and remove it internally
      page.clearPage();
      if (thePage != page) {
        throw new RuntimeException("Invariant Violated: May not remove page we don't have");
      }
      thePage = null;

      // remove the page from the canvas and revalidate so it looks okay
      canvas.remove(page.getJComponent());
      canvas.revalidate();
      canvas.repaint();
      PageChangeEventManager.notifyListeners();
    }
    return page;
  }

  ////////////////////////////////
  //PageChangeListener Interface//
  ////////////////////////////////

  /** @override PageChangeListener.update() */
  public void update() {
    this.reformBlockCanvas(); // just repaint and it'll all look right again
  }
  /**
   * @modifies the single page in this BlockCanvas as well as the canvas
   * @effects re-synchronize model and view, resize, reposition, and set color
   *          of the single page in the BlockCanvas.  Note that
   *          reforming must perform ALL FIVE ACTIONS when invoked.
   */
  public void reformBlockCanvas(){
    Page p = thePage;
    if (p != null) {
      if(p.getDefaultPageColor() == null){
        p.setPageColor(CGraphite.lightgreen);
      }else{
        p.setPageColor(p.getDefaultPageColor());
      }
      Dimension dim = p.reformBounds();
      canvas.setPreferredSize(dim);
    } else {
      canvas.setPreferredSize(new Dimension(0, 0));
    }
    scrollPane.revalidate();
    scrollPane.repaint();
  }

  //////////////////////////////
  //Saving and Loading        //
  //////////////////////////////

  /**
   * Returns an XML String describing all the blocks and the page within
   * the BlockCanvas
   */
  public String getSaveString(){
    StringBuilder saveString = new StringBuilder();

    //get save string of the page
    if(thePage != null){  //TODO ria just do BLOCKS, CHECK OUT HOW SAVING WILL BE LIKE WITH REFACTORING
      saveString.append("<Pages>"); //should we include drawer-with-page flag?
      saveString.append(thePage.getSaveString());
      saveString.append("\n");
      saveString.append("</Pages>");
    }
    return saveString.toString();
  }

  /**
   * Loads all the RenderableBlocks and their associated Blocks that
   * reside within the block canvas.  All blocks will have their necessary
   * data populated including connection information, stubs, etc.
   * Note: This method should only be called if this language only uses the
   * BlockCanvas to work with blocks and no pages. Otherwise, workspace live blocks
   * are loaded from Pages.
   * @param root the Document Element containing the desired information
   */
  protected void loadSaveString(Element root){
    //Extract canvas blocks and load

    //load page, page drawers, and their blocks from save file
    //PageDrawerManager.loadPagesAndDrawers(root);
    PageDrawerLoadingUtils.loadPagesAndDrawers(root, Workspace.getInstance().getFactoryManager());
    if (thePage != null && !WorkspaceControllerHolder.isHeadless()) {
      // Note that screen size is actually the dimension of the user's
      // screen, not the windows on their screen
      Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
      Dimension canvasSize = canvas.getPreferredSize();
      boolean changed = false;
      if(canvasSize.width < screenSize.width){
        thePage.addPixelWidth(screenSize.width-canvasSize.width);
        changed = true;
      }
      if(canvasSize.height < screenSize.height){
        thePage.addPixelHeight(screenSize.height-canvasSize.height);
        changed = true;
      }
      if (changed) {
        PageChangeEventManager.notifyListeners();
      }
      thePage.setMinimumPixelWidth(screenSize.width);
      thePage.setMinimumPixelHeight(screenSize.height);
    }
  }

  //////////////////////////////
  //REDO/UNDO                 //
  //////////////////////////////

  /** @override ISupportMomento.getState */
  public Object getState(){
    Map<String, Object> pageStates = new HashMap<String, Object>();
    if (thePage != null) {
      pageStates.put(thePage.getPageName(), thePage.getState());
    }
    return pageStates;
  }

  /** @override ISupportMomento.loadState() */
  @SuppressWarnings("unchecked")
  public void loadState(Object memento){
    assert (memento instanceof HashMap) : "ISupportMemento contract violated in BlockCanvas";
    if(memento instanceof HashMap){
      Map<String, Object> pageStates = (HashMap<String, Object>) memento;
      List<String> unloadedPages = new LinkedList<String>();
      List<String> loadedPages = new LinkedList<String>();

      for(String name : pageStates.keySet()){
        unloadedPages.add(name);
      }

      //First, load the page, if any, that is in the state to be loaded
      //against the page that already exists, if any.
      if (thePage != null) {
        String existingPageName = thePage.getPageName();

        if(pageStates.containsKey(existingPageName)){
          thePage.loadState(pageStates.get(existingPageName));
          unloadedPages.remove(existingPageName);
          loadedPages.add(existingPageName);
        }
      }

      //Now, remove all the pages that don't exist in the save state
      if (thePage != null) {
        String existingPageName = thePage.getPageName();

        if(!loadedPages.contains(existingPageName)){
          thePage = null;
        }
      }

      //Finally, add all the remaining pages that weren't there before
      for(String newPageName : unloadedPages){
        Page newPage = new Page(newPageName);
        newPage.loadState(pageStates.get(newPageName));
        if (thePage != null) {
          // we're screwed
          throw new RuntimeException("Invariant Violated: May not have more than one page");
        }
        thePage = newPage;
      }
    }
  }

  /**
   * The graphical representation of the block canvas's Swing Container of pages.
   * Note that this is not the graphical scrollable JComponent that represents
   * the BlockCanvas.
   */
  public class Canvas extends JLayeredPane implements MouseListener, MouseMotionListener{
    private static final long serialVersionUID = 438974092314L;
    private Point p;
    public Canvas() {
      super();
      this.p = null;
      this.addMouseListener(this);
      this.addMouseMotionListener(this);
    }
    public void mousePressed(MouseEvent e) {
      p=e.getPoint();
    }
    public void mouseClicked(MouseEvent e) {
      if(SwingUtilities.isRightMouseButton(e) || e.isControlDown()){
        //pop up context menu
        PopupMenu popup = ContextMenu.getContextMenuFor(BlockCanvas.this);
        this.add(popup);
        popup.show(this, e.getX(), e.getY());
      }
    }

    public void mouseDragged(MouseEvent e){
      if(p == null){
        //do nothing
      }else{
        BoundedRangeModel hModel = scrollPane.getHorizontalModel();
        BoundedRangeModel vModel = scrollPane.getVerticalModel();
        hModel.setValue(hModel.getValue()+(p.x-e.getX()));
        vModel.setValue(vModel.getValue()+(p.y-e.getY()));
      }
    }

    public void mouseReleased(MouseEvent e) {
      this.p=null;
    }

    public void mouseMoved(MouseEvent e){  //MOUSE_MOVED_ON_CANVAS
      hoverMenuController.mouseMovedOnCanvas(e);
      WorkspaceControllerHolder.get().mouseMovedOnCanvas(e);
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}
  }
}
