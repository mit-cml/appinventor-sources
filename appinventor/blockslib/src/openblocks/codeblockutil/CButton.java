// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * A CButton is a swing-compatible widget that allows clients
 * to display an oval button with an optional text.
 *
 * To add a particular action to this widget, users should invoke
 * this.addCButtonListener( new CButtonListener());
 */
public class CButton extends JButton implements MouseListener{
  private static final long serialVersionUID = 328149080228L;
  /** blur lighting of this button */
  static float[] BLUR = {0.10f, 0.10f, 0.10f, 0.10f, 0.30f, 0.10f, 0.10f, 0.10f, 0.10f};
  /** the inset of this button */
  static final int INSET = 3;
  /** The highlighting inset */
  static final int HIGHLIGHT_INSET = 2;

  /** Focus Flag: true iff mouse is hovering over button */
  boolean focus = false;
  /** Press Flag: true iff button was pressed but has not been released */
  boolean pressed = false;
  /** Selected Flag: true iff button was toggled to selected */
  boolean selected = false;
  /** Color of this button when not pressed */
  Color buttonColor;
  /** Color of this button when pressed */
  Color selectedColor;
  /** Color of the foreground when not hovered */
  Color foregroundColor = Color.white;
  /** Color of the foreground whe hovered */
  Color hoveredColor = Color.red;

  /**
   * Creates a button with text and black buttonColor, and
   * white selectedColor
   * @param text
   */
  public CButton(String text){
    this(new Color(30,30,30), Color.gray, text);
  }
  /**
   * Create a button with text;
   * @param buttonColor - color when not pressed down
   * @param selectedColor - color when pressed down but not released yet
   * @param text - textual label of this
   *
   * @requires buttonColor, selectedColor, text != null
   * @effects constructs new CButton
   */
  public CButton(Color buttonColor, Color selectedColor, String text){
    super();
    this.setOpaque(false);
    this.buttonColor=buttonColor;
    this.selectedColor=selectedColor;
    this.setText(text);
    this.setFont(new Font("Ariel", Font.BOLD, 14));
    this.addMouseListener(this);
                this.setPreferredSize(new Dimension(80, 25));
    this.setCursor(new Cursor(Cursor.HAND_CURSOR));

  }

  /**
   * Dynamically changes the coloring of the buttons when
   * pressed or not pressed.
   *
   * @param buttonColor
   * @param selectedColor
   *
   * @requires buttonColor, selectedColor != null
   * @modifies this.buttonColor && this.seletedColor
   * @effects change button coloring to match to inputs
   */
  public void setLighting(Color buttonColor, Color selectedColor){
    this.buttonColor = buttonColor;
    this.selectedColor = selectedColor;
  }

  public void setTextLighting(Color foregroundColor, Color hoveredColor){
    this.foregroundColor = foregroundColor;
    this.hoveredColor = hoveredColor;
  }


  /**
   * @modifies this.selected
   * @effects toggles selcted flag to value of selected
   */
  public void toggleSelected(boolean selected){
    this.selected=selected;
    this.repaint();
  }


  /////////////////////////////////////////////////////////////////////////
  //Methods below this line should not be
  //modified or overriden and affects the Rendering of this button.
  ///////////////////////

  /**
   * Prevents textual label from display out of the bounds
   * of the this oval shaped button's edges
   */
  public Insets getInsets(){
    //top, left, bottom, right
    return new Insets(0,this.getHeight()/2,0,this.getHeight()/2);
  }
  /**
   * re paints this
   */
  public void paint(Graphics g){
    //super.paint(g);

    //selected color
    Color backgroundColor;
    if(this.pressed || this.selected){
      backgroundColor = this.selectedColor;
    }else{
      backgroundColor = this.buttonColor;
    }

    // Set up graphics and buffer
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    BufferedImage buffer = GraphicsManager.gc.createCompatibleImage(this.getWidth(), this.getHeight(), Transparency.TRANSLUCENT);
    Graphics2D gb = buffer.createGraphics();
    gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Set up first layer
    int buttonHeight = this.getHeight() - (INSET * 2);
    int buttonWidth = this.getWidth() - (INSET * 2);
    int arc = buttonHeight;
    Color topColoring =  backgroundColor.darker();
    Color bottomColoring = backgroundColor.darker();
    gb.setPaint(new GradientPaint(0, INSET, topColoring, 0, buttonHeight, bottomColoring, false));

    // Paint the first layer
    gb.fillRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
    gb.setColor(Color.darkGray);
    gb.drawRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);

    // set up paint data fields for second layer
    int highlightHeight = buttonHeight - (HIGHLIGHT_INSET * 2);
    int highlightWidth = buttonWidth - (HIGHLIGHT_INSET * 2);
    int highlightArc = highlightHeight;
    topColoring = backgroundColor.brighter().brighter().brighter().brighter();
    bottomColoring = backgroundColor.brighter().brighter().brighter().brighter();

    // Paint the second layer
    gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.8f));
    gb.setPaint(new GradientPaint(0,INSET+HIGHLIGHT_INSET,topColoring,0,INSET+HIGHLIGHT_INSET+(highlightHeight/2), backgroundColor.brighter(), false));
    gb.setClip(new RoundRectangle2D.Float(INSET+HIGHLIGHT_INSET,INSET+HIGHLIGHT_INSET,highlightWidth,highlightHeight / 2,highlightHeight / 3,highlightHeight /3));
    gb.fillRoundRect(INSET+HIGHLIGHT_INSET,INSET+HIGHLIGHT_INSET,highlightWidth,highlightHeight,highlightArc,highlightArc);

    // Blur
    ConvolveOp blurOp = new ConvolveOp(new Kernel(3, 3, BLUR));
    BufferedImage blurredImage = blurOp.filter(buffer, null);

    // Draw button
    g2.drawImage(blurredImage, 0, 0, null);

    // Draw the text (if any)
    if (this.getText() != null)
    {
      if(this.focus){
        g2.setColor(hoveredColor);
      }else{
        g2.setColor(foregroundColor);
      }


      Font font = g2.getFont().deriveFont((float)(((float)buttonHeight) * .6));
      g2.setFont(font);

      FontMetrics metrics = g2.getFontMetrics();
      Rectangle2D textBounds = metrics.getStringBounds(this.getText(),g2);

      float x = (float)((this.getWidth() / 2) - (textBounds.getWidth() / 2));
      float y = (float)((this.getHeight() / 2) + (textBounds.getHeight() / 2)) - metrics.getDescent();

      g2.drawString(this.getText(),x,y);
    }
  }



  //////////////////////
  //Mouse Listeners
  //////////////////////

  public void addMouseListener(MouseListener l){
    super.addMouseListener(l);
  }
  public void mouseEntered(MouseEvent e) {
    this.focus=true;
    repaint();
  }
  public void mouseExited(MouseEvent e) {
    this.focus=false;
    repaint();
  }
  public void mousePressed(MouseEvent e) {
    if(SwingUtilities.isLeftMouseButton(e))
      this.pressed=true;
    repaint();

  }
  public void mouseReleased(MouseEvent e) {
    this.pressed=false;
    repaint();
  }
  public void mouseClicked(MouseEvent e) {}
  /** debugging */
  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setLayout(new FlowLayout());
    f.setSize(500, 75);
    CButton c = new CButton("hi");
    c.setPreferredSize(new Dimension(400,50));

    f.add(c);
    f.setVisible(true);
    f.repaint();

  }
}
