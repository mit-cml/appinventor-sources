package openblocks.renderable;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import openblocks.workspace.RBParent;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;

import openblocks.codeblocks.JComponentDragHandler;
import openblocks.codeblockutil.CGraphite;
import openblocks.codeblockutil.CTracklessScrollPane;
import openblocks.codeblockutil.CScrollPane.ScrollPolicy;

import openblocks.yacodeblocks.WorkspaceControllerHolder;

/**
 * BlockNotes are associated with RenderabeBlock
 * It should "tag" along with the block. Note, however, that
 * this feature should be ensured by the parent source.  The
 * parent source can guarantee this by invoking the methods
 * setPosition, translatePosition, and setParent when
 * appropriate.
 */
public abstract class BlockNote extends JPanel {
  private static boolean DEBUG = false;

  private final Color background;
  /**border color*/
  private final Color borderColor;

  /**Text field UIs*/
  protected JTextArea textContent;


  /** Fields used for width computation in subclasses */
  protected static FontMetrics fontMetrics;
  //Amount (measured in ems to horizontally pad balloon text)
  // TODO(halabelson): This is a more padding than you'd expect.
  // It appears that the pane border is taking up some of the internal space.
  // Something to investigate
  protected static int TEXT_PADDING_EMS = 4;
  // Padding with corresponding to the above, for the current font
  protected int textPadding;

  //max  width and height of blocknotes is determined by caller
  private final int maxWidth;
  private final int initialHeight;



  /**ScrollPane UI*/
  private  final CTracklessScrollPane scrollPane;
  /**Dragging handler of this BlockNote*/
  private final JComponentDragHandler jCompDH;
  /**Manager for arrow drawn from this to parent while in editing mode**/
  private final BlockNoteArrow arrow;
  /** The JComponent this BlockNote and BlockNote label is connected to */
  private final RenderableBlock blockNoteSource;
  /** The BlockNoteLabel linked to this BlockNote and placed on the RenderableBlock */
  final BlockNoteLabel blockNoteLabel;

  /** true if  this BlockNote should not be able to have a location outside of its
   *  parent's bounds, false if it may be located outside of its parent's bounds
   */

  static final int FONT_SIZE = 11;
  private static final int MINIMUM_WIDTH = FONT_SIZE * 4;
  private static final int MINIMUM_HEIGHT = FONT_SIZE * 2;
  static final String FONT_NAME = "Arial";

  private boolean resizing = false;
  private int margin = 6;
  private int width;
  private int height;
  private final boolean scroll;
  protected double zoom = 1.0;
  private Shape  body, resize, textArea;
  private boolean pressed = false;
  private boolean active = false;
  private boolean constrainBlockNote = true;

  /**
   * Constructs a BlockNote
   * with belonging to source, with text of initText, and initial zoom
   * The BlockNote's borders will have the color borderColor.
   *
   * Note that initializing a BlockNote only constructs
   * all of the necessary structures.  To graphically display a BlockNote,
   * the implementor must then add the BlockNote using the proper
   * Swing methods OR through the convenience method BlockNote.setParent()
   *
   * @param textContent  a text-holding Swing component
   * @param blockNoteSource block that the BlockNote is linked to.
   * @param labelText the character to go in the control label
   * @param background of the window
   */
  protected BlockNote(JTextArea textContent, RenderableBlock blockNoteSource,
      String labelText, Color background, boolean scroll, int maxWidth,
      int initialHeight){

    //set up important fields
    this.textContent = textContent;
    this.blockNoteSource = blockNoteSource;
    this.maxWidth = maxWidth;
    this.initialHeight = initialHeight;
    borderColor = blockNoteSource.getActivatedColor();
    this.background = background;
    zoom = blockNoteSource.getZoom();
    this.scroll = scroll;
    Point blocksCorner = blockNoteSource.getLocation();

    setLayout(null);
    setOpaque(false);
    textContent.setFont(new Font(FONT_NAME, Font.PLAIN, (int) (FONT_SIZE * zoom)));
    textContent.setForeground(CGraphite.darkgray);
    textContent.setBackground(background);
    textContent.setVisible(true);

    scrollPane = new CTracklessScrollPane(textContent,
        ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
        ScrollPolicy.HORIZONTAL_BAR_NEVER,
        10, borderColor, background);
    add(scrollPane);

    //set up listeners
    BlockNoteEventListener eventListener = new BlockNoteEventListener();
    boolean headless = WorkspaceControllerHolder.isHeadless();
    jCompDH = headless ? null : new JComponentDragHandler(this);
    if (!headless) {
      addMouseListener(eventListener);
      addMouseMotionListener(eventListener);
      textContent.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          BlockNote blockNote = BlockNote.this;
          blockNote.setPressed(true);
          blockNote.showOnTop();
        }
      });
      textContent.addFocusListener(eventListener);
    }

    // Make the label that goes on the block
    blockNoteLabel = new BlockNoteLabel(blockNoteSource.getBlockID(), this, labelText);

    // Make the arrow that goes between label and balloon
    arrow = new BlockNoteArrow(this);
    arrow.setVisible(true);

    if (!blockNoteSource.isLoading) {
      // If we're not loading this, the parent should be set.
      ((RBParent) blockNoteSource.getParent()).addToBlockArrowLayer(arrow.triangle);
    }

    if (blockNoteSource.getParentWidget() != null) {
      setParent(blockNoteSource.getParentWidget().getJComponent());
    } else {
      setParent(getParent());
    }
    showOnTop();
    reformBlockNote();

    Workspace.getInstance().notifyListeners(new WorkspaceEvent(
        WorkspaceEvent.BLOCK_NOTE_ADDED, blockNoteSource.getBlockID(), this));
    // Subclass will set location.
  }

    /**
     * @modifies textContent
     * @effects saves rawString for getText
     * @param text
     */
    public void setText(String text) {
      // set the report balloon width to accommodate the text
      // TODO(halabelson): Fix this computation to work better to very long lines and
      // for multiline text
      int mywidth = Math.min(maxWidth, fontMetrics.stringWidth(text) + textPadding);
      this.setBlockNoteSize(new Dimension(mywidth, initialHeight));
      textContent.setText(text);
    }

  /**
   * @return the width of the BlockNote label for this BlockNote
   */
  public int getBlockNoteLabelWidth() {
    if (blockNoteLabel == null) {
      return 0;
    }
    return blockNoteLabel.getWidth();
  }

  /**
   * Updates the BlockNote, BlockNoteLabel, and BlockNoteArrow
   */
  public void update() {
    if (blockNoteLabel != null) {
      blockNoteLabel.update();
      arrow.update();
      boolean active = blockNoteLabel.isActive();
      arrow.setVisible(active);
      setVisible(active);
    }
  }

  /**
   * Sets the active state of the BlockNoteLabel and updates the BlockNote and
   * BlockNoteLabel
   * @param visibleState
   */
  public void update(boolean visibleState) {
    if (blockNoteLabel != null) {
      blockNoteLabel.setActive(visibleState);
    }
    update();
  }

  /**
   * Set a new zoom level, changes font size, label size, location, shape of
   * BlockNote, and arrow for this BlockNote
   * @param newZoom
   */
  public void setZoomLevel(double newZoom) {
    // calculates the new position based on the initial position when zoom is at 1.0
    zoom = newZoom;
    textContent.setFont(new Font(FONT_NAME, Font.PLAIN, (int) (FONT_SIZE * zoom)));
    if (blockNoteLabel != null) {
      blockNoteLabel.setZoomLevel(newZoom);
    }
    reformBlockNote();
  }

  protected void setBlockNoteSize(Dimension d) {
    width = d.width;
    height = d.height;
    reformBlockNote();
  }

  /**
   * Recalculate the shape of this BlockNote
   */
  public void reformBlockNote(){
    int w = (int) (width * zoom);
    int h = (int) (height * zoom);
    int m = (int) (margin * zoom);

    GeneralPath path2 = new GeneralPath();
    path2.moveTo(m - 1, m - 1);
    path2.lineTo(w - m, m - 1);
    path2.lineTo(w - m, h - m);
    path2.lineTo(m - 1, h - m);
    path2.closePath();
    textArea = path2;

    body = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 3 * m, 3 * m);

    GeneralPath path3 = new GeneralPath();
    path3.moveTo(w - 3 * m, h);
    path3.lineTo(w, h - 3 * m);
    path3.curveTo(w, h, w, h, w - 3 * m, h);
    resize = path3;

    scrollPane.setBounds(m, m, w - 2 * m, h - 2 * m);
    scrollPane.setThumbWidth(scroll ? 2 * m  : 0);
    setBounds(getX(), getY(), w, h);
    arrow.update();
    revalidate();
    repaint();
  }

  public void setTextBackground(Color color) {
    textContent.setBackground(color);
    repaint();
  }
  /**
   * remove out-riders of block note
   */
  public void delete() {
    //TODO(user) Figure out why parent is sometimes null. Probably a race
    // condition.
    Container parent = getParent();
    if (parent != null) {
      parent.remove(arrow.triangle);
      parent.remove(this);
    } else {
      if (DEBUG) {
        System.out.println("Deleting balloon without parent.");
      }
    }
    blockNoteSource.remove(blockNoteLabel);
    Workspace.getInstance().notifyListeners(new WorkspaceEvent(
        WorkspaceEvent.BLOCK_NOTE_REMOVED, blockNoteSource.getBlockID(), this));
  }

  /**
   * @return the BlockNoteLabel for this BlockNote
   */
  public BlockNoteLabel getBlockNoteLabel() {
    return blockNoteLabel;
  }

  /**
   * @return  the Block for this BlockNote (stored as RenderableBlock)
   */
  RenderableBlock getRenderableBlock() {
    return blockNoteSource;
  }
  /**
   * @param x
   * @return returns what x would be when zoom == 1
   */
  private int descale(double x){
    return (int) (x / zoom);
  }

  /**
   * calculates the x when the zoom is 1.0
   * @return the x when the zoom is 1.0
   */
  public int getUnzoomedX(){
    return (int) (getX() / zoom);
  }

  /**
   * calculates the y when the zoom is 1.0
   * @return the y when the zoom is 1.0
   */
  public int getUnzoomedY(){
    return (int) (getY() / zoom);
  }

  /**
   * @return  the save String for this blockNote
   */
  public String getSaveString(){
    StringBuffer saveString = new StringBuffer();
    saveString.append("<Location>");
    saveString.append("<X>");
    saveString.append(descale(getLocation().getX()));
    saveString.append("</X>");
    saveString.append("<Y>");
    saveString.append(descale(getLocation().getY()));
    saveString.append("</Y>");
    saveString.append("</Location>");
    saveString.append("<BoxSize>");
    saveString.append("<Width>");
    saveString.append(descale(getWidth()));
    saveString.append("</Width>");
    saveString.append("<Height>");
    saveString.append(descale(getHeight()));
    saveString.append("</Height>");
    saveString.append("</BoxSize>");
    if (blockNoteLabel.isActive()) {
      saveString.append("<Visible/>");
    }
    return saveString.toString();
  }

  /**
   * Loads the BlockNote from a NodeList of BlockNote parts
   * @param blockNoteChildren
   * @param rb
   * @param blockNote already created by loader for subclass
   * @effect adjusts various properties of the blockNote for subclass loader
   */
  public static void loadBlockNote(NodeList blockNoteChildren, RenderableBlock rb,
      BlockNote blockNote) {
    boolean visible = false;
    for (int j = 0; j < blockNoteChildren.getLength(); j++) {
      Node blockNoteChild = blockNoteChildren.item(j);
      if (blockNoteChild.getNodeName().equals("Location")) {
        Point blockNoteLoc = new Point(0, 0);
        RenderableBlock.extractLocationInfo(blockNoteChild, blockNoteLoc);
        blockNote.setLocation(blockNoteLoc.x, blockNoteLoc.y);
      } else  if (blockNoteChild.getNodeName().equals("BoxSize")) {
        Dimension boxSize = new Dimension(0, 0);
        RenderableBlock.extractBoxSizeInfo(blockNoteChild, boxSize);
        blockNote.setMyWidth((int) boxSize.getWidth());
        blockNote.setMyHeight((int) boxSize.getHeight());
      } else  if (blockNoteChild.getNodeName().equals("Visible")) {
        visible = true;
      }
    }
    blockNote.reformBlockNote();
    blockNote.update(visible);
  }

  @Override
  public void paint(Graphics g){
    Graphics2D g2 = (Graphics2D) g;
    g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON));

    g2.setColor(getBorderColor());
    g2.fill(body);
    g2.setColor(background);
    g2.fill(textArea);
    g2.draw(textArea);
    g2.setColor(CGraphite.lightgreen);
    g2.fill(resize);

    super.paint(g);
  }

  /**
   *  moves this to a new position at (x, y) but not outside of its parent
   *  Container
   * @modifies location
   * @effect  Set location.x to x, if x is within bounds of parent.
   *           if not, then set location.x to closest boundary value.
   *          Set location.y to y, if y is within bounds of parent.
   *           if not, then set location.y to closest boundary value.
   * Overrides javax.Swing.JComponent.setLocation()
   */
  @Override
  public void setLocation(int x, int y) {
    if (isConstrainBlockNote() && getParent() != null) {
      //If x < 0, set location.x to 0.
      //If 0 < x < parent.width, then set location.x to x.
      //If x > parent.width, then set location.x to parent.width.
      //repeat for y
      if (y < 0) {
        y = 0;
      } else   if (y + getHeight()  > getParent().getHeight()) {
        y = Math.max(getParent().getHeight() - getHeight(), 0);
      }

      if (x < 0) {
        x = 0;
      } else if (x  + getWidth() + 1 >  getParent().getWidth()) {
        x = Math.max(getParent().getWidth() - getWidth() - 1, 0);
      }
    }
    super.setLocation(x, y);
    arrow.update();
    Workspace.getInstance().getMiniMap().repaint();
  }

  /**
   * moves this to a new position at (x, y) but not outside of its parent Container
   * @modifies location
   * @effect  Set location.x to x, if x is within bounds of parent.
   *                    if not, then set location.x to closest boundary value.
   *            Set location.y to y, if y is within bounds of parent.
   *                    if not, then set location.y to closest boundary value.
   *
   * Override javax.Swing.JComponent.setLocation()
   */
  @Override
  public void setLocation(Point p){
    setLocation(p.x, p.y);
  }


  /**
   * @modifies this
   * @effect translate location
   *            by dx in the x-direction and dy in the y-direction
   * @param dx
   * @param dy
   */
  public void translatePosition(int dx, int dy){
    setLocation(getX() + dx, getY() + dy);
  }

  /**
   * Moves this blockNote from it's old parent Container to
   * a new Container.  Removal and addition applies only
   * if the Containers are non-null
   * @modifies the current parent and newparent
   * @effect First, remove this from current parent ONLY if
   *            current parent is non-null.  Second, add this to
   *            newparent container ONLY if newparent is non-null.
   *            Third, repaint both modified parent containers.
   * @param newparent
   */
  public void setParent(Container newparent){
    setParent(newparent, null);
  }
  /**
   * Overrides the standard setVisible to (1) avoid making a balloon visible when
   * its attached block isn't and (2) make the arrow the same as the balloon.
   */
  @Override
  public void setVisible(boolean b) {
    if (!blockNoteSource.isVisible()) {
      b = false;
    }
    super.setVisible(b);
    if (arrow.triangle != null) {
      arrow.setVisible(b);
    }
  }

  /**
   * Moves this blockNote from it's old parent Container to
   * a new Container with given constrain.
   * @modifies the current parent and newparent
   * @effect First, remove this from current parent ONLY if
   *            current parent is non-null.  Second, add this to
   *            newparent container ONLY if newparent is non-null.
   *            Third, repaint both modified parent containers.
   * @param newparent
   * @param constraints
   */
  public void setParent(Container newparent, Object constraints){
    //remove from the current parent Container if non-null
    Container oldParent = getParent();
    if (oldParent  != null){
      oldParent.remove(this);
      oldParent.remove(arrow.triangle);
      oldParent.validate();
      oldParent.repaint();
    }
    //add this to newparent Container if non-null
    if (newparent != null){
      ((RBParent) newparent).addToBlockNoteLayer(this);
      newparent.validate();
      newparent.repaint();
    }
  }

  /**
   * Bumps the blockNote to top of ZOrder of parent if parent exists
   */
  public void showOnTop() {
    if (getParent() != null) {
      getParent().setComponentZOrder(this, 0);
    }
  }

  /**
   * BlockNoteEventListener is an inner class that
   * responds to the various external events,
   * and provides the requires semantic operations
   * for BlockNotes to be moved/focused correctly.
   * It owns, and sends semantic actions to the
   * outer BlockNote class.
   */
  private class BlockNoteEventListener implements FocusListener, MouseListener,
  MouseMotionListener{

    /**When focus lost, force a repaint**/
    public void focusGained(FocusEvent e) {
      active = true;
      repaint();
    }
    /**When focuses gained, force a repaint**/
    public void focusLost(FocusEvent e) {
      active = false;
      repaint();
    }
    /**when clicked upon, switch to editing mode*/
    public void mouseClicked(MouseEvent e) {
      //prevent users from clicking multiple times and crashing the system
      if (e.getClickCount() > 1) {
        return;
      }
    }

    /**highlight this blockNote when a mouse begins to hover over this*/
    public void mouseEntered(MouseEvent e) {
      showOnTop();
      jCompDH.mouseEntered(e);
    }
    /**highlight this blockNote when a mouse hovers over this*/
    public void mouseMoved(MouseEvent e) {
      if (e.getX() > (width - 2 * margin) && e.getY() > (height - 2 * margin)){
        setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
      } else {
        jCompDH.mouseMoved(e);
      }
    }

    /**stop highlighting this blockNote when a mouse leaves this*/
    public void mouseExited(MouseEvent e) {
      jCompDH.mouseExited(e);
    }
    /**prepare for a drag when mouse is pressed down*/
    public void mousePressed(MouseEvent e) {
      requestFocusInWindow();  //atimer.stop();
      showOnTop();
      jCompDH.mousePressed(e);
      if (e.getX() > (width - 2 * margin) && e.getY() > (height - 2 * margin)){
        setResizing(true);
      } else {
        if (e.getY() < margin){
          setPressed(true);
        }
      }
      repaint();
    }

    /**when mouse is released*/
    public void mouseReleased(MouseEvent e) {
      jCompDH.mouseReleased(e);
      setResizing(false);
      setPressed(false);
      repaint();
    }

    /**drag this when mouse is dragged*/
    public void mouseDragged(MouseEvent e) {
      if (isResizing()){
        double ww = e.getX() > MINIMUM_WIDTH * zoom ? e.getX() :
          MINIMUM_WIDTH * zoom;
        double hh = e.getY() > MINIMUM_HEIGHT * zoom ? e.getY() :
          MINIMUM_HEIGHT * zoom;
        width = (int) ww;
        height = (int) hh;
        reformBlockNote();
        Workspace.getInstance().notifyListeners(new WorkspaceEvent(
            WorkspaceEvent.BLOCK_NOTE_RESIZED, blockNoteSource.getBlockID(), BlockNote.this));
      } else {
        jCompDH.mouseDragged(e);
        Workspace.getInstance().notifyListeners(new WorkspaceEvent(
            WorkspaceEvent.BLOCK_NOTE_MOVED, blockNoteSource.getBlockID(), BlockNote.this));
      }
    }
  }

  /**
   * Returns the blockNote background color
   * @return the blockNote background color
   */
  Color getBackgroundColor() {
    return background;
  }

  /**
   * Returns the borderColor of this blockNote
   * @return the borderColor of this blockNote
   */
  Color getBorderColor() {
    return borderColor;
  }

  /**
   * access to the blockNote arrow object
   * @return arrow object
   */
  public BlockNoteArrow getArrow() {
    return arrow;
  }



  /**
   * @return the width
   */
  int getMyWidth() {
    return width;
  }

  /**
   * @param width the width to set
   */
  void setMyWidth(int width) {
    this.width = width;
  }

  /**
   * @return the height
   */
  int getMyHeight() {
    return height;
  }

  /**
   * @param height the height to set
   */
  void setMyHeight(int height) {
    this.height = height;
  }

  /**
   * @return the margin
   */
  int getMargin() {
    return margin;
  }

  /**
   * @param margin the margin to set
   */
  void setMargin(int margin) {
    this.margin = margin;
  }

  /**
   * @return the pressed true, if this blockNote has been pressed
   */
  boolean isPressed() {
    return pressed;
  }

  /**
   * @param pressed true if this blockNote has been pressed
   */
  void setPressed(boolean pressed) {
    this.pressed = pressed;
  }

  /**
   * @return the resizing, true if this blockNote is being resized
   */
  boolean isResizing() {
    return resizing;
  }

  /**
   * @param resizing true if this blockNote is being resized
   */
  void setResizing(boolean resizing) {
    this.resizing = resizing;
  }

  /**
   * returns whether this blockNote should be constrained to its parent's bounds
   * @return the constrainBlockNote
   */
  public boolean isConstrainBlockNote() {
    return constrainBlockNote;
  }

  /**
   * sets whether this blockNote should be constrained to its parent's bounds
   * @param constrainBlockNote the constrainBlockNote to set
   */
  public void setConstrainBlockNote(boolean constrainBlockNote) {
    this.constrainBlockNote = constrainBlockNote;
  }
}
