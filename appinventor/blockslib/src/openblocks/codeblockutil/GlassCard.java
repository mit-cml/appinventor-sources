package openblocks.codeblockutil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import openblocks.codeblockutil.CScrollPane.ScrollPolicy;

/**
 * A GlassCard is used by glass explorers explorers as a
 * mediator to their canvases.
 * 
 * It wraps a button, a scrollpane, and a invoker.
 * 
 * The button uses information about the current color
 * and highlight of the canvas to depict itself.  The
 * scrollpane takes the canvas and puts it inside a scroll
 * pane so that users can navigate a very large canvas
 * in small space.  The invoker responds to button presses
 * and invokes the right method in the explorer.
 */
public class GlassCard implements MouseListener, PropertyChangeListener {
  /** the index of the button in the explorer */
  private int index;
  /** the parent explorer */
  private Explorer explorer;
  /** the canvas that is wrapped by this card */
  private Canvas canvas;
  /** The button of this */
  private CCategoryButton button;
  /** The scroll that canvas lives in */
  private CScrollPane scroll;
  private final static int SCROLLBAR_WIDTH = 18;
  
  /**
   * constructor
   * @param i
   * @param canvas
   * @param ex
   */
  GlassCard(int i, Canvas canvas, GlassExplorer ex) {
    this.index= i;
    this.explorer = ex;
    this.canvas = canvas;
    this.button = new CCategoryButton(canvas.getColor(), canvas.getName(), 9);
    this.button.setPreferredSize(new Dimension(0, 33));
    this.scroll = new CGlassScrollPane(canvas.getJComponent(),
                                       ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
                                       ScrollPolicy.HORIZONTAL_BAR_NEVER,
                                       SCROLLBAR_WIDTH, canvas.getColor(), 
                                       CGraphite.scrollbar_background);
    canvas.getJComponent().setOpaque(false);
    button.addMouseListener(this);
    canvas.getJComponent().addPropertyChangeListener(this);
    this.scroll.setPreferredSize(new Dimension(
        canvas.getJComponent().getPreferredSize().width + SCROLLBAR_WIDTH, 
        canvas.getJComponent().getPreferredSize().height));
  }
  
  public void setIndex(int newIndex) {
    index = newIndex;
  }
  
  public void toggleButtonSelected(boolean selected) {
    button.toggleSelected(selected);
  }
  
  public void mouseClicked(MouseEvent e) {
    // do nothing
  }

  public void mouseEntered(MouseEvent e) {
    // do nothing
  }

  public void mouseExited(MouseEvent e) {
    // do nothing
  }

  public void mousePressed(MouseEvent e) {
    if (explorer instanceof GlassExplorer) {
      ((GlassExplorer)explorer).cardsMousePressed(index);
    }
  }

  /**
   * When the user releases the button, the explorer selects the
   * corresponding canvas.  Use the release, not the press, because
   * the press triggers the loss of focus that the explorer uses to
   * determine that the drawer should close.  Use mouse event instead
   * of action to ensure that a slow mouse button press/release
   * doesn't trigger another drawer open (bounce).
   */
  public void mouseReleased(MouseEvent e) {
    explorer.selectCanvas(index);         
  }
  
  /**
   * @return the button
   */
  JComponent getButton() {
    return button;
  }

  /**
   * @return the scroll
   */
  JComponent getScroll() {
    return scroll;
  }

  /**
   * @return the background color of the glass pane
   */
  Color getBackgroundColor() {
    return new Color(CGraphite.lightergreen.getRed(),
                     CGraphite.lightergreen.getGreen(),
                     CGraphite.lightergreen.getBlue(), 200);
  }

  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals(Canvas.LABEL_CHANGE)) {
      button.repaint();
    }
    if (e.getSource().equals(canvas) &&
        e.getPropertyName().equals("preferredSize")) {
      this.scroll.setPreferredSize(new Dimension(
          canvas.getJComponent().getPreferredSize().width + SCROLLBAR_WIDTH, 
          canvas.getJComponent().getPreferredSize().height));
    }
  }
}
