package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;


public class CGlassScrollPane extends CScrollPane implements KeyListener{
  private static final long serialVersionUID = 328149080222L;
  private static Color defaultTrackColor = new Color(50,50,50,175);

  private int SCROLLINGUNIT =3;
  private GlassVerticalBar verticalbar;
  private GlassHorizontalBar horizontalbar;
  private JScrollPane scrollviewport;
  private ScrollPolicy vpolicy;
  private ScrollPolicy hpolicy;
  private int thumbWidth;

//  private CGlassScrollPane(){}

  /**
   * Constructs a custom CHoverScrollPane with the view port set to "view",
   * with both scroll bar policies set to "ALWAYS" (see
   * javax.swing.JScrollPane for a description on the use of
   * scroll bar policies).  Thumb will have  girth of 10 and an interior
   * color of black, hovering above a grayed-out transparant background.
   *
   * @param view
   *
   * @requires view != null
   * @effects constructs a CScrollPane as described in method overview
   */
  public CGlassScrollPane(JComponent view){
    this(view, ScrollPolicy.VERTICAL_BAR_ALWAYS,
        ScrollPolicy.HORIZONTAL_BAR_ALWAYS);
  }

  /**
   * Constructs a custom CHoverScrollPane with the view port set to "view",
   * and correponding vertical and horizontal bar policies (see
   * javax.swing.JScrollPane for a description on the use of
   * scroll bar policies).  Thumb will have  girth of 10 and an interior
   * color of black, hovering above a grayed-out transparent background.
   *
   * @param view
   * @param verticalPolicy
   * @param horizontalPolicy
   *
   * @requires view != null
   * @effects constructs a CScrollPane as described in method overview
   */
  public CGlassScrollPane(JComponent view,ScrollPolicy verticalPolicy,ScrollPolicy horizontalPolicy){
    this(view, verticalPolicy,
        horizontalPolicy,
        10, Color.darkGray, null);
  }

  /**
   * Constructs a custom CHoverScrollPane with the view port set to "view",
   * with both scroll bar policies set to "ALWAYS" (see
   * javax.swing.JScrollPane for a description on the use of
   * scroll bar policies).  Thumb will have  girth of equal to
   * thumbWidth and an interior color equal to thumbColor.
   *
   * @param view
   *
   * @requires view != null
   * @effects Constructs a CScrollPane as described in method overview.
   *      If thumbColor is null, then the deafault Color.black value
   *      will be used.  If trackColor is null, then the
   *      default Color.white value will be used.
   */
  public CGlassScrollPane(JComponent view,int thumbWidth,Color thumbColor,Color trackColor){
    this(view, ScrollPolicy.VERTICAL_BAR_ALWAYS,
        ScrollPolicy.HORIZONTAL_BAR_ALWAYS,
        thumbWidth, thumbColor, trackColor);
  }

  /**
   * Constructs a custom CHoverScrollPane with the view port set to "view",
   * with correponding vertical and horizontal bar policies (see
   * javax.swing.JScrollPane for a description on the use of
   * scroll bar policies).  The thumb will have a girth equal to
   * "thumbWidth" and an interior color of thumbColor.  The background
   * underneath the thumb will have a color equal to thumbBackground.
   *
   * @param view - the viewport
   * @param verticalPolicy - the vertical scroll bar policy
   * @param horizontalPolicy - the horizontal scroll bar policy
   * @param thumbWidth - the width of the vertical scroll bar in pixels and
   *            the height of the horiztontal scroll bar in pixels
   * @param thumbColor - the interior color of the thumb
   * @param trackColor - the backgorund color under the thumb
   *
   * @requires view != null
   * @effects Creates a JScrollPane that displays the view component
   *     in a viewport whose view position can be controlled with
   *     a pair of scrollbars.
   *        -If the scrollbar policies are null, then it will use the default
   *         "ALWAYS" policy.  That is, the scroll bars will always show.
   *        -If the thumbWidth is null or less than 0, then the scroll bars
   *         will not show.
   *        -If thumbColor is null, then thumbs will dedault on Color.black.
   *        -If trackColor is null, then the default grayed-out transparent color
   *         will be used as the background color.
   */
  public CGlassScrollPane(
      JComponent view,
      ScrollPolicy verticalPolicy,
      ScrollPolicy horizontalPolicy,
      int thumbWidth,
      Color thumbColor,
      Color trackColor){

    //////////////////////////////////////
    // INITIALIZE COMPONENTS
    super();
    this.setLayout(null);
    this.vpolicy = verticalPolicy;
    this.hpolicy = horizontalPolicy;
    this.thumbWidth = thumbWidth;
    this.setOpaque(false);
    scrollviewport = new JScrollPane(view,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER){
      private static final long serialVersionUID = 328149080223L;
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
            case KeyEvent.VK_TAB:
              System.out.println("bl.enter tab");
              return false;
            default:
              return super.processKeyBinding(ks, e, condition, pressed);
            }
          }
    };
    scrollviewport.setOpaque(false);
    scrollviewport.getViewport().setOpaque(false);
    scrollviewport.setBorder(null);
    scrollviewport.setWheelScrollingEnabled(true);
    if(thumbWidth<0) thumbWidth = 0;
    if (thumbColor == null ) thumbColor = Color.black;
    if(trackColor == null) trackColor = defaultTrackColor;

    verticalbar = new GlassVerticalBar(thumbWidth, thumbColor, trackColor,
        scrollviewport.getVerticalScrollBar().getModel(), verticalPolicy);
    horizontalbar = new GlassHorizontalBar(thumbWidth, thumbColor, trackColor,
        scrollviewport.getHorizontalScrollBar().getModel());

    /////////////////////////////////////////
    // SET LISTENERS
    view.addKeyListener(this);
    scrollviewport.addMouseWheelListener(this);

    //////////////////////////////////
    // SET LAYOUT
    add(scrollviewport, JLayeredPane.DEFAULT_LAYER);
    if(verticalPolicy.equals(ScrollPolicy.VERTICAL_BAR_ALWAYS) || verticalPolicy .equals(ScrollPolicy. VERTICAL_BAR_AS_NEEDED)){
      add(verticalbar, JLayeredPane.PALETTE_LAYER);
    }
    if(horizontalPolicy .equals(ScrollPolicy. HORIZONTAL_BAR_ALWAYS) || horizontalPolicy .equals(ScrollPolicy. HORIZONTAL_BAR_AS_NEEDED)){
      add(horizontalbar, JLayeredPane.PALETTE_LAYER);
    }
    this.addComponentListener(new ComponentAdapter(){
      public void componentResized(ComponentEvent e){
        repositionComponents();
      }
    });
    this.repositionComponents();
    this.revalidate();
  }

  public void repositionComponents(){
    scrollviewport.setBounds(0,0,this.getWidth(), this.getHeight());
    if(this.vpolicy .equals(ScrollPolicy. VERTICAL_BAR_ALWAYS) || this.vpolicy .equals(ScrollPolicy. VERTICAL_BAR_AS_NEEDED)){
      verticalbar.setBounds(this.getWidth()-this.thumbWidth,7,
          this.thumbWidth, this.getHeight()-this.thumbWidth-7);
    }else{
      verticalbar.setBounds(this.getWidth()-this.thumbWidth,7,
          0,0);
    }
    if(this.hpolicy .equals(ScrollPolicy. HORIZONTAL_BAR_ALWAYS)){
      horizontalbar.setBounds(7,this.getHeight()-this.thumbWidth,
          this.getWidth()-this.thumbWidth-7, this.thumbWidth);
    }else if(this.hpolicy .equals(ScrollPolicy. HORIZONTAL_BAR_AS_NEEDED)){
      if(this.getWidth()<this.scrollviewport.getViewport().getView().getWidth()-2){
        horizontalbar.setBounds(7,this.getHeight()-this.thumbWidth,
            this.getWidth()-this.thumbWidth-7, this.thumbWidth);
      }else{
        horizontalbar.setBounds(7,this.getHeight()-this.thumbWidth,
            0,0);
      }
    }
    this.revalidate();
  }


  /**
   * @override CScrollPane.getVerticalModel
   */
  public BoundedRangeModel getVerticalModel(){
    return scrollviewport.getVerticalScrollBar().getModel();
  }

  /**
   * @override CScrollPane.getHorizontalModel
   */
  public BoundedRangeModel getHorizontalModel(){
    return scrollviewport.getHorizontalScrollBar().getModel();
  }

  /**
   * @override CScrollPane.scrollRectToVisible
   */
  public void scrollRectToVisible(Rectangle contentRect){
    scrollviewport.getViewport().scrollRectToVisible(contentRect);
  }

  /**
   * @override CScrollPane.setScrollingUnit
   */
  public void setScrollingUnit(int x){
    this.SCROLLINGUNIT = x;
    this.verticalbar.setScrollingUnit(x);
  }


  /**
   * @override CScrollPane.mouseWheelMoved
   * TODO: This is duplicate code (In Hover, Glass, and Tackless ScollPanes)
   * For MACs only: Horizontal scroll events are delivered
   * to JScrollPanes as Shift+ScrollWheel events AUTOMATICALLY,
   * since there is no horizontal scrolling API in Java.
   * Horizontal scrolling mouse events will now move the
   * content view horizontally, along with Shift key modifier events.
   * For WINDOWs: Manually press Shift while scrolling to scroll horizantally
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    if(e.isShiftDown()){
      scrollviewport.getHorizontalScrollBar().getModel().setValue(
          scrollviewport.getHorizontalScrollBar().getModel().getValue()+
          e.getUnitsToScroll()*e.getScrollAmount()*SCROLLINGUNIT);
      horizontalbar.repaint();
    }else {
      scrollviewport.getVerticalScrollBar().getModel().setValue(
          scrollviewport.getVerticalScrollBar().getModel().getValue()+
          e.getUnitsToScroll()*e.getScrollAmount()*SCROLLINGUNIT);
      verticalbar.repaint();
    }
  }

  /**
   * KeyListeners: Should repaint the scrollbar
   * everytime the user presses a key
   */
  public void   keyPressed(KeyEvent e){
    verticalbar.repaint();
    horizontalbar.repaint();
  }
  /**
   * KeyListeners: Should repaint the scrollbar
   * everytime the user presses a key
   */
  public void   keyReleased(KeyEvent e){
    verticalbar.repaint();
    horizontalbar.repaint();
  }
  /**
   * KeyListeners: Should repaint the scrollbar
   * everytime the user presses a key
   */
  public void   keyTyped(KeyEvent e) {
    verticalbar.repaint();
    horizontalbar.repaint();
  }
  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setLayout(new BorderLayout());
    f.setSize(400, 400);
    JTextArea b = new JTextArea(20,30);
    final CHoverScrollPane c = new CHoverScrollPane(
        b,
        ScrollPolicy.VERTICAL_BAR_ALWAYS,
        ScrollPolicy.HORIZONTAL_BAR_ALWAYS,
        20,
        Color.red,
        Color.blue);
    f.getContentPane().add(c);
    f.setVisible(true);
    f.addComponentListener(new ComponentAdapter(){
      public void componentResized(ComponentEvent e){
        c.repositionComponents();
      }
    });
  }

}

/**
 * A VerticalBar is a vertical scroll bar that operates in conjuction with
 * the PARALLEL bouding model range passed as an argument in the
 * constructor.  Any changes to this scrollbar's thumb position
 * should perform the same parallel changes to the bounding model range.
 *
 *
 */
class GlassVerticalBar extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener{
  private static final long serialVersionUID = 328149080224L;
  /**
   * Bounding model that this vertical scroll bar will change when the thumb
   * is moved.  This bar also collects information from the bounding
   * model (including the value, maximum, minimum, and extent) in order
   * to render the corect size and location of the thumb.
   */
  private final BoundedRangeModel modelrange;
  /** The thumb color of this vertical scroll bar*/
  private final Color thumbColor;
  /** The track color of this vertical scroll bar */
  private final Color trackColor;
  /** Rendering hints of the thumb border */
  private final RenderingHints renderingHints;
  /** Last location of the mouse press */
  private int pressLocation;
  /** Amount by which the mouse wheel scrolls */
  private int SCROLLINGUNIT = 3;
  /** Vertical Scroll Bar Policy of this */
  private CScrollPane.ScrollPolicy vpolicy;

  /**
   *
   * @param barwidth - the final HEIGHT on the thumb
   * @param thumbColor -  the final color of the thumb's interior
   * @param trackColor - the final color of trh track
   * @param modelrange - the mutating view ranges to control
   *            and be controlled by this HorizontalBar
   *
   * @requires barwidth != null && thumbColor != null && modelrange != null
   * @effects Constructs this to have a thumb of barwidth in size
   *      with a thumb color set to thummbColor.
   */
  public GlassVerticalBar(int barwidth, Color thumbColor, Color trackColor, BoundedRangeModel modelrange, CScrollPane.ScrollPolicy verticalPolicy){
    this.vpolicy = verticalPolicy;
    this.trackColor = trackColor;
    this.modelrange=modelrange;
    this.thumbColor=thumbColor;
    this.renderingHints = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    this.pressLocation = 0;
    this.setOpaque(false);
    this.addMouseMotionListener(this);
    this.addMouseListener(this);
    this.addMouseWheelListener(this);
  }

  /**
   * paints scrollbar
   */
  public void paint(Graphics g){
    //paint super
    super.paint(g);

    //set up values
    double viewValue = modelToView(modelrange.getValue());
    double viewExtent = modelToView(modelrange.getExtent());
    int vWidth;
    if(!vpolicy.equals(CScrollPane.ScrollPolicy.VERTICAL_BAR_AS_NEEDED) || (modelrange.getMaximum()>this.getHeight()+7+this.getWidth())){
      vWidth = this.getWidth()/2;

      Graphics2D g2 = (Graphics2D)g;
      g2.addRenderingHints(renderingHints);
      g2.translate(vWidth/2, 0);
      g2.setColor(trackColor);
      g2.fillRoundRect(0,0,vWidth, this.getHeight(), vWidth, vWidth);
      g2.setColor(new Color(150,150,150));
      g2.drawRoundRect(0,0,vWidth, this.getHeight()-1, vWidth, vWidth);

      if(viewValue<this.getHeight()-0.5f*this.getWidth()){
        g2.translate(0,viewValue);
      }else{
        g2.translate(0, this.getHeight()-0.5f*this.getWidth());
      }
      g2.setPaint(new GradientPaint(
          0,0,this.thumbColor,
          this.getWidth()+10,0, Color.black, true));
      g2.fillRoundRect(0,0,vWidth, (int)viewExtent, vWidth, vWidth);
      g2.setColor(new Color(250,250,250,100));
      g2.drawRoundRect(0,0,vWidth, (int)viewExtent, vWidth, vWidth);

    }
  }
  /**
   * @param view - view value to tranform
   *
   * @requires view != null
   * @return a tranformed value from view coordinates to model coordinates
   */
  private double viewToModel(int view){
    return view*modelrange.getMaximum()/this.getHeight();
  }
  /**
   * @param model - model value to tranform
   *
   * @requires model != null
   * @return a tranformed value from model coordinates to view coordinates
   */
  private double modelToView(int model){
    return model*this.getHeight()/(double)modelrange.getMaximum();
  }
  /**
   * MouseListener: Should either scroll by some drag distance
   * or, if the user presses outside the thumb, it should jump
   * directly to the location of the mouse press and THEN
   * scroll by some drag distance.
   */
  public void mousePressed(MouseEvent e) {
    double viewValue = modelToView(modelrange.getValue());
    double viewExtent = modelToView(modelrange.getExtent());
    if (e.getY()<viewValue || e.getY()>(viewExtent+viewValue)){
      this.pressLocation = (int)(viewExtent/2);
      modelrange.setValue((int)viewToModel(e.getY())-modelrange.getExtent()/2);
      this.repaint();
    }else{
      this.pressLocation=e.getY()-(int)viewValue;
    }
  }
  /**
   * Drag scroll bar by same drag distance as mouse drag
   */
  public void mouseDragged(MouseEvent e){
    modelrange.setValue((int)viewToModel(e.getY()-this.pressLocation));
    this.repaint();
  }
  /**
   * Drops the thumb
   */
  public void mouseReleased(MouseEvent e) {
    this.pressLocation = 0;
  }
  public void mouseMoved(MouseEvent e){}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {}

  /**
   * Translate the viewport by same amount of wheel scroll
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    modelrange.setValue(modelrange.getValue()+e.getUnitsToScroll()*e.getScrollAmount()*SCROLLINGUNIT);
    this.repaint();
  }

  public void setScrollingUnit(int x){
    this.SCROLLINGUNIT=x;
  }
}

/**
 * A HorizontalBar is a vertical scroll bar that operates in conjuction with
 * the PARALLEL bouding model range passed as an argument in the
 * constructor.  Any changes to this scrollbar's thumb position
 * should perform the same parallel changes to the bounding model range.
 *
 *
 */
class GlassHorizontalBar extends JPanel implements MouseListener, MouseMotionListener{
  private static final long serialVersionUID = 328149080225L;
  /**
   * Bounding model that this horizontal scroll bar will change when the thumb
   * is moved.  This bar also collects information from the bounding
   * model (including the value, maximum, minimum, and extent) in order
   * to render the corect size and location of the thumb.
   */
  private final BoundedRangeModel modelrange;
  /** The thumb color of this vertical scroll bar*/
  private final Color thumbColor;
  /** The track color os thif vertical scroll bar */
  private final Color trackColor;
  /** Rendering hints of the thumb border */
  private final RenderingHints renderingHints;
  /** First Location of a mouse press */
  private int pressLocation;

  /**
   *
   * @param barwidth - the final HEIGHT on the thumb
   * @param thumbColor -  the final color of the thumb's interior
   * @param trackColor - the final color of the track
   * @param modelrange - the mutating view ranges to control
   *            and be controlled by this HorizontalBar
   *
   * @requires barwidth != null && thumbColor != null && modelrange != null
   * @effects Constructs this to have a thumb of barwidth in size
   *      with a thumb color set to thummbColor.
   */
  public GlassHorizontalBar(int barwidth, Color thumbColor, Color trackColor, BoundedRangeModel modelrange){
    this.modelrange=modelrange;
    this.thumbColor=thumbColor;
    this.trackColor = trackColor;
    this.renderingHints = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    this.pressLocation = 0;
    this.setOpaque(false);
    this.addMouseMotionListener(this);
    this.addMouseListener(this);

  }

  /**
   * paints scrollbar
   */
  public void paint(Graphics g){
    //paint super
    super.paint(g);

    //set up values
    double viewValue = modelToView(modelrange.getValue());
    double viewExtent = modelToView(modelrange.getExtent());
    int vHeight = this.getHeight()/2;

    Graphics2D g2 = (Graphics2D)g;
    g2.addRenderingHints(renderingHints);
    g2.translate(0, vHeight/2);

    g2.setColor(trackColor);
    g2.fillRoundRect(0,0,this.getWidth(), vHeight, vHeight, vHeight);
    g2.setColor(new Color(150,150,150));
    g2.drawRoundRect(0,0,this.getWidth()-1, vHeight, vHeight, vHeight);

    if(viewValue<this.getWidth()-0.5f*this.getHeight()){
      g2.translate(viewValue, 0);
    }else{
      g2.translate(this.getWidth()-0.5f*this.getHeight(), 0);
    }
    g2.setPaint(new GradientPaint(
        0,0,this.thumbColor,
        0, this.getHeight()+10, Color.black, true));
    g2.fillRoundRect(0,0,(int)viewExtent, vHeight, vHeight, vHeight);
    g2.setColor(new Color(250,250,250,100));
    g2.drawRoundRect(0,0,(int)viewExtent,vHeight, vHeight, vHeight);
  }
  /**
   * @param view - view value to tranform
   *
   * @requires view != null
   * @return a tranformed value from view coordinates to model coordinates
   */
  private double viewToModel(int view){
    return view*modelrange.getMaximum()/this.getWidth();
  }
  /**
   * @param model - model value to tranform
   *
   * @requires model != null
   * @return a tranformed value from model coordinates to view coordinates
   */
  private double modelToView(int model){
    return model*this.getWidth()/(double)modelrange.getMaximum();
  }
  /**
   * MouseListener: Should either scroll by some drag distance
   * or, if the user presses outside the thumb, it should jump
   * directly to the location of the mouse press and THEN
   * scroll by some drag distance.
   */
  public void mousePressed(MouseEvent e) {
    double viewValue = modelToView(modelrange.getValue());
    double viewExtent = modelToView(modelrange.getExtent());
    if (e.getX()<viewValue || e.getX()>(viewExtent+viewValue)){
      this.pressLocation = (int)(viewExtent/2);
      modelrange.setValue((int)viewToModel(e.getX())-modelrange.getExtent()/2);
      this.repaint();
    }else{
      this.pressLocation=e.getX()-(int)viewValue;
    }
  }
  /**
   * Drag scroll bar by same drag distance as mouse drag
   */
  public void mouseDragged(MouseEvent e){
    modelrange.setValue((int)viewToModel(e.getX()-this.pressLocation));
    this.repaint();
  }
  /**
   * Drops the thumb
   */
  public void mouseReleased(MouseEvent e) {
    this.pressLocation = 0;
  }
  public void mouseMoved(MouseEvent e){}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {}
}
