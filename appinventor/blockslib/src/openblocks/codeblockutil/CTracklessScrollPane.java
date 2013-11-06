// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;


/**
 * The CScrollPane is a swing-compatible widget that
 * allows clients of this CScrollPane to control the
 * width of the thumb, the color of the thumb, and
 * and the color of the track.  Like all swing-compatible
 * scroll panes, the CScrollPane wraps a viewport and must
 * change the viewing space (also known as the visible rectangle)
 * of the scroll pane when users attempts to scroll
 * with the mouse, wheel, or key board.
 */
public class CTracklessScrollPane extends CScrollPane implements KeyListener{
  private static final long serialVersionUID = 328149080244L;
  private int SCROLLINGUNIT =3;
  private VerticalBar verticalbar;
  private HorizontalBar horizontalbar;
  private JScrollPane scrollviewport;

  /**
   * Constructs a custom CScrollPane with the view port set to "view",
   * with both scroll bar policies set to "ALWAYS" (see
   * javax.swing.JScrollPane for a description on the use of
   * scroll bar policies).  Thumb will have  girth of 10 and an interior
   * color of black, hovering above a white background.
   *
   * @param view
   *
   * @requires view != null
   * @effects constructs a CScrollPane as described in method overview
   */
  public CTracklessScrollPane(JComponent view){
    this(view, CScrollPane.ScrollPolicy.VERTICAL_BAR_ALWAYS,
        CScrollPane.ScrollPolicy.HORIZONTAL_BAR_ALWAYS);
  }

  /**
   * Constructs a custom CScrollPane with the view port set to "view",
   * with correponding vertical and horizontal bar policies (see
   * javax.swing.JScrollPane for a description on the use of
   * scroll bar policies).  Thumb will have  girth of 10 and an interior
   * color of black, hovering above a white background.
   *
   * @param view
   * @param verticalPolicy
   * @param horizontalPolicy
   *
   * @requires view != null
   * @effects constructs a CScrollPane as described in method overview
   */
  public CTracklessScrollPane(JComponent view, ScrollPolicy verticalPolicy,ScrollPolicy horizontalPolicy){
    this(view, verticalPolicy,
        horizontalPolicy,
        10, Color.darkGray, Color.gray);
  }

  /**
   * Constructs a custom CScrollPane with the view port set to "view",
   * with both scroll bar policies set to "ALWAYS" (see
   * javax.swing.JScrollPane for a description on the use of
   * scroll bar policies).  Thumb will have  girth of equal to
   * thumbWidth and an interior color equal to thumbColor.
   *
   * @param view
   *
   * @requires view != null
   * @effects Constructs a CScrollPane as described in method overview.
   *          If thumbColor is null, then the deafault Color.black value
   *          will be used.  If trackColor is null, then the
   *          default Color.white value will be used.
   */
  public CTracklessScrollPane(JComponent view,int thumbWidth,Color thumbColor,Color trackColor){
    this(view, CScrollPane.ScrollPolicy.VERTICAL_BAR_ALWAYS,
        CScrollPane.ScrollPolicy.HORIZONTAL_BAR_ALWAYS,
        thumbWidth, thumbColor, trackColor);
  }

  /**
   * Constructs a custom CScrollPane with the view port set to "view",
   * with correponding vertical and horizontal bar policies (see
   * javax.swing.JScrollPane for a description on the use of
   * scroll bar policies).  The thumb will have a girth equal to
   * "thumbWidth" and an interior color of thumbColor.  The background
   * underneath the thumb will have a color equal to thumbBackground.
   *
   * @param view - the viewport
   * @param verticalPolicy - the vertical scroll bar policy (always or never)
   * @param horizontalPolicy - the horizontal scroll bar policy (always or never)
   * @param thumbWidth - the width of the vertical scroll bar in pixels and
   *          the height of the horiztontal scroll bar in pixels
   * @param thumbColor - the interior color of the thumb
   * @param trackColor - the backgorund color under the thumb
   *
   * @requires view != null
   * @effects Creates a JScrollPane that displays the view component
   *         in a viewport whose view position can be controlled with
   *         a pair of scrollbars.
   *            -If the scrollbar policies are null, then it will use the default
   *             "ALWAYS" policy.  That is, the scroll bars will always show.
   *            -If the thumbWidth is null or less than 0, then the scroll bars
   *             will not show.
   *            -If thumbColor is null, then thumbs will dedault on Color.black.
   *            -If trackColor is null, then the default Color.white value
   *             will be used as the background color.
   */
  public CTracklessScrollPane(
      JComponent view,
      ScrollPolicy verticalPolicy,
      ScrollPolicy horizontalPolicy,
      int thumbWidth,
      Color thumbColor,
      Color trackColor){

    //////////////////////////////////////
    // INITIALIZE COMPONENTS
    super();
    this.setLayout(new BorderLayout());
    this.setOpaque(true);
    scrollviewport = new JScrollPane(view,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER){
        private static final long serialVersionUID = 328149080245L;
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
    scrollviewport.setBorder(null);
    scrollviewport.setWheelScrollingEnabled(true);
    if(thumbWidth<0) thumbWidth = 0;
    if (thumbColor == null ) thumbColor = Color.black;
    if(trackColor == null) trackColor = Color.white;

    verticalbar = new VerticalBar(thumbWidth, thumbColor,
        scrollviewport.getVerticalScrollBar().getModel());
    verticalbar.setBackground(trackColor);
    horizontalbar = new HorizontalBar(thumbWidth, thumbColor,
        scrollviewport.getHorizontalScrollBar().getModel());
    horizontalbar.setBackground(trackColor);

    /////////////////////////////////////////
    // SET LISTENERS
    view.addKeyListener(this);
    scrollviewport.addMouseWheelListener(this);

    //////////////////////////////////
    // SET LAYOUT
    if(verticalPolicy.equals(ScrollPolicy.VERTICAL_BAR_NEVER)){
      if(horizontalPolicy.equals(ScrollPolicy.HORIZONTAL_BAR_NEVER)){
        add(scrollviewport, BorderLayout.CENTER);
      }else{
        add(horizontalbar, BorderLayout.SOUTH);
        add(scrollviewport, BorderLayout.CENTER);
      }
    }else{
      if(horizontalPolicy.equals(ScrollPolicy.HORIZONTAL_BAR_NEVER)){
        add(verticalbar, BorderLayout.EAST);
        add(scrollviewport, BorderLayout.CENTER);
      }else{
        add(verticalbar, BorderLayout.EAST);
        add(scrollviewport, BorderLayout.CENTER);
        JPanel horizontalScrollBarPane = new JPanel(new BorderLayout());
        horizontalScrollBarPane.add(horizontalbar, BorderLayout.CENTER);
        horizontalScrollBarPane.add(new LowerRightCorner(thumbWidth, trackColor), BorderLayout.EAST);
        add(horizontalScrollBarPane, BorderLayout.SOUTH);
      }
    }
    this.revalidate();
  }

  /**
   * @return vertical scroll bar bounding range model.  May be null
   */
  public BoundedRangeModel getVerticalModel(){
    return scrollviewport.getVerticalScrollBar().getModel();
  }

  /**
   * @return horizontal scroll bar bounding range model.  May be null
   */
  public BoundedRangeModel getHorizontalModel(){
    return scrollviewport.getHorizontalScrollBar().getModel();
  }

  public void setThumbWidth(int thumbWidth){
    this.verticalbar.setPreferredSize(new Dimension(thumbWidth, 0));
    this.horizontalbar.setPreferredSize(new Dimension(0, thumbWidth));
    this.repaint();
  }

  /**
   * @override CScrollPane.scrollRectToVisible
   */
  public void scrollRectToVisible(Rectangle contentRect){
    scrollviewport.getViewport().scrollRectToVisible(contentRect);
  }

  /**
   * Set the amount by which the mouse wheel scrolls
   * @requires INTEGER_MIN<x<INTEGER_MAX
   * @modifies this.SCROLLINGUNIT
   * @effects set this.scrollingunit to x
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
   * For WINDOWs: Manually press Shift while scrolling to scroll horizontally
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
  public void     keyPressed(KeyEvent e){
    verticalbar.repaint();
    horizontalbar.repaint();
  }
  /**
   * KeyListeners: Should repaint the scrollbar
   * everytime the user presses a key
   */
  public void     keyReleased(KeyEvent e){
    verticalbar.repaint();
    horizontalbar.repaint();
  }
  /**
   * KeyListeners: Should repaint the scrollbar
   * everytime the user presses a key
   */
  public void     keyTyped(KeyEvent e) {
    verticalbar.repaint();
    horizontalbar.repaint();
  }
  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setLayout(new BorderLayout());
    f.setSize(400, 400);
    JTextArea b = new JTextArea(20,30);
    f.getContentPane().add(new CTracklessScrollPane(
        b,
        ScrollPolicy.VERTICAL_BAR_ALWAYS,
        ScrollPolicy.HORIZONTAL_BAR_ALWAYS,
        20,
        Color.red,
        Color.blue));
    f.setVisible(true);
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
class VerticalBar extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener{
  static final long serialVersionUID = 873498899234L;
  /**
   * Bounding model that this vertical scroll bar will change when the thumb
   * is moved.  This bar also collects information from the bounding
   * model (including the value, maximum, minimum, and extent) in order
   * to render the corect size and location of the thumb.
   */
  private final BoundedRangeModel modelrange;
  /** The thumb color of this vertical scroll bar*/
  private final Color thumbColor;
  /** Rendering hints of the thumb border */
  private final RenderingHints renderingHints;
  /** Last location of the mouse press */
  private int pressLocation;
  /** Amount by which the mouse wheel scrolls */
  private int SCROLLINGUNIT = 3;

  /**
   *
   * @param barwidth - the final HEIGHT on the thumb
   * @param thumbColor -  the final color of the thumb's interior
   * @param modelrange - the mutating view ranges to control
   *          and be controlled by this HorizontalBar
   *
   * @requires barwidth != null && thumbColor != null && modelrange != null
   * @effects Constructs this to have a thumb of barwidth in size
   *          with a thumb color set to thummbColor.
   */
  public VerticalBar(int barwidth, Color thumbColor, BoundedRangeModel modelrange){
    this.modelrange=modelrange;
    this.thumbColor=thumbColor;
    this.renderingHints = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    this.pressLocation = 0;
    this.setBackground(Color.black);
    this.setPreferredSize(new Dimension(barwidth,0));
    this.setBorder(null);
    this.addMouseMotionListener(this);
    this.addMouseListener(this);
    this.addMouseWheelListener(this);
  }

  /**
   * paints scrollbar
   */
  public void paint(Graphics g){
    super.paint(g);
    Graphics2D g2 = (Graphics2D)g;
    g2.addRenderingHints(renderingHints);
    g2.setColor(Color.gray);
    g2.drawRoundRect(0,0,this.getWidth()-1, this.getHeight()-1, this.getWidth(), this.getWidth());

    double viewValue = modelToView(modelrange.getValue());
    double viewExtent = modelToView(modelrange.getExtent());
    if(viewValue<this.getHeight()-0.5f*this.getWidth()){
      g2.translate(0,viewValue);
    }else{
      g2.translate(0, this.getHeight()-0.5f*this.getWidth());
    }
    g2.setColor(Color.lightGray);
    g2.setPaint(new GradientPaint(0,0,thumbColor,this.getWidth()+10, 0, Color.black, true));
    g2.fill(reformThumbShape(this.getWidth(), this.getHeight(), (int)viewExtent));
  }

  /**
   * @requires w != null && 0<=w<=this.width
   *     && e != null && 0<=e<=this.height
   * @param w
   * @param e
   * @return an oval thumb shape such that
   *       the height of the thumb is always
   *       greater or equal to half the width:
   *
   *       thumb.height >= 1/2 thumb.width
   */
  private Shape reformThumbShape(int w, int h, int e){
    if(e>=h-2){
      return new RoundRectangle2D.Double(0,0,0,0,0,0);
    }else if(e>=w){
      Shape shape = new RoundRectangle2D.Double(0,0,w,e,w,w);
      return shape;
    }else if(e>=0.5f*w){
      return new Ellipse2D.Float(0,0,w,e);
    }else{
      return new Ellipse2D.Float(0,0,w,0.5f*w);
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
class HorizontalBar extends JPanel implements MouseListener, MouseMotionListener{
  static final long serialVersionUID = 98231984183L;
  /**
   * Bounding model that this horizontal scroll bar will change when the thumb
   * is moved.  This bar also collects information from the bounding
   * model (including the value, maximum, minimum, and extent) in order
   * to render the corect size and location of the thumb.
   */
  private final BoundedRangeModel modelrange;
  /** The thumb color of this vertical scroll bar*/
  private final Color thumbColor;
  /** Rendering hints of the thumb border */
  private final RenderingHints renderingHints;
  /** First Location of a mouse press */
  private int pressLocation;

  /**
   *
   * @param barwidth - the final HEIGHT on the thumb
   * @param thumbColor -  the final color of the thumb's interior
   * @param modelrange - the mutating view ranges to control
   *          and be controlled by this HorizontalBar
   *
   * @requires barwidth != null && thumbColor != null && modelrange != null
   * @effects Constructs this to have a thumb of barwidth in size
   *          with a thumb color set to thummbColor.
   */
  public HorizontalBar(int barwidth, Color thumbColor, BoundedRangeModel modelrange){
    this.modelrange=modelrange;
    this.thumbColor=thumbColor;
    this.renderingHints = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    this.pressLocation = 0;
    this.setMinimumSize(new Dimension(100, barwidth));
    this.setPreferredSize(new Dimension(100,barwidth));
    this.setBorder(null);
    this.addMouseMotionListener(this);
    this.addMouseListener(this);
  }

  /**
   * paints scrollbar
   */
  public void paint(Graphics g){
    super.paint(g);
    Graphics2D g2 = (Graphics2D)g;
    g2.addRenderingHints(renderingHints);
    g2.setColor(Color.gray);
    g2.drawRoundRect(0,0,this.getWidth()-1, this.getHeight()-1, this.getHeight(), this.getHeight());

    double viewValue = modelToView(modelrange.getValue());
    double viewExtent = modelToView(modelrange.getExtent());
    if(viewValue<this.getWidth()-0.5f*this.getHeight()){
      g2.translate(viewValue, 0);
    }else{
      g2.translate(this.getWidth()-0.5f*this.getHeight(), 0);
    }
    g2.setColor(Color.lightGray);
    g2.setPaint(new GradientPaint(0,0,thumbColor,0, this.getHeight()+10, Color.black, true));
    g2.fill(reformThumbShape(this.getWidth(), this.getHeight(), (int)viewExtent));
  }

  /**
   * @requires w != null && 0<=w<=this.width
   *     && e != null && 0<=e<=this.height
   * @param w
   * @param e
   * @return an oval thumb shape such that
   *       the height of the thumb is always
   *       greater or equal to half the width:
   *
   *       thumb.height >= 1/2 thumb.width
   */
  private Shape reformThumbShape(int w, int h, int e){
    if(e>=w-2){
      return new RoundRectangle2D.Double(0,0,0,0,0,0);
    }else if(e>=h){
      Shape shape = new RoundRectangle2D.Double(0,0,e,h,h,h);
      return shape;
    }else if(e>=0.5f*h){
      return new Ellipse2D.Float(0,0,e,h);
    }else{
      return new Ellipse2D.Float(0,0,e,0.5f*h);
    }
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

/**
 * A Graphical Square Pane with the same same color as the thumb.
 * This class should ONLY be used with the CScrollPane.  It appears
 * when the user has both the vertical and horizontal scroll policy
 * set to on
 *
 */
class LowerRightCorner extends JPanel{
  private static final long serialVersionUID = 328149080248L;
  /**
   * Constructs a LowerRightCorner Square
   *
   * @param barwidth - the MAXIMUM width of this coner square
   * @param thumbBackground - the color of the this coner square
   *
   * @requires barwidth != nul && barwidth>0 && thumbBackground != null
   * @effects constructs a LowerRightConer square
   */
  public LowerRightCorner(int barwidth, Color thumbBackground){
    super();
    this.setPreferredSize(new Dimension(barwidth, barwidth));
    this.setBackground(thumbBackground);
  }
}
