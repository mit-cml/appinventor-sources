// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.JFrame;


/**
 * A CIconButton is a swing-compatible widget that allows clients
 * to display a circular button with an optional icon.  Clients
 * may choose from the the possible set of of icons: play, pause, step.
 * 
 * To add a particular action to this widget, users should invoke
 * this.addMouseListener( new CButtonListener());
 */
public class CIconButton extends CButton{
	private static final long serialVersionUID = 328149080213L;
	/** Icon choices */
	public static enum Icon{PLAY, PAUSE, STEP, STOP};
	/** icon inset of this button */
	private static final int ICON_INSET = 12;
	/** Button icon */
	private Icon icon;
	
	/**
	 * @effects constructs this with particular icon, a black background,
	 * 			and a blue selected coloring.
	 */
	public CIconButton(Icon icon){
		super(Color.black, Color.blue, null);
		this.icon=icon;
		super.setPreferredSize(new Dimension (40,40));
		super.setSize(new Dimension(40,40));
		
		//Check rep
		if(!(ICON_INSET > 2*INSET)) throw new RuntimeException("insets faulty");
	}
	
	/**
	 *  constructs this with the given icon, background,
	 *  and selected coloring.
	 *  @param backColor the normal "unarmard" background color
	 *  @param selectedColor the color of the background when the button is pressed
	 *  @param icon the CIconButton.Icon for this button
	 */
	public CIconButton(Color backColor, Color selectedColor, Icon icon){
		super(backColor, selectedColor, null);
		this.icon=icon;
		super.setPreferredSize(new Dimension (40,40));
		super.setSize(new Dimension(40,40));
		
		//Check rep
		if(!(ICON_INSET > 2*INSET)) throw new RuntimeException("insets faulty");
	}
	
	/**
	 * @param icon
	 * 
	 * @requires none
	 * @modifies this.icon
	 * @effects Dynamically changes the icon of this button.
	 * 			If the icon is null, no icon will be displayed
	 */
	public void setIcon(Icon icon){
		this.icon = icon;
	}
	
	/**
	 * @return selected fag
	 */
	public boolean isSelected(){
		return this.selected;
	}
	
	/**
	 * This methods maps an icon Enum to an icon Shape
	 * 
	 * @param icon - icon enum
	 * 
	 * @requires none
	 * @return the cooresponding icon shape (to match the icon enum)
	 * 		   or null if the icon enum does not match any known ones.
	 */
	private Shape getIconShape(Icon icon){
		int width = this.getWidth()-2*ICON_INSET;
		if(icon == Icon.PLAY){
			GeneralPath shape = new GeneralPath();
			shape.moveTo(ICON_INSET, ICON_INSET);
			shape.lineTo(ICON_INSET+width, ICON_INSET+width/2);
			shape.lineTo(ICON_INSET, ICON_INSET+width);
			shape.lineTo(ICON_INSET, ICON_INSET);
			shape.closePath();
			return shape;
		}else if(icon == Icon.PAUSE){
			Rectangle2D.Float rect1 = new Rectangle2D.Float(ICON_INSET, ICON_INSET, width/3, width);
			Rectangle2D.Float rect2 = new Rectangle2D.Float(ICON_INSET+width*2/3, ICON_INSET, width/3, width);
			Area shape1 = new Area(rect1);
			Area shape2 = new Area(rect2);
			shape1.add(shape2);
			return shape1;
		}else if(icon == Icon.STEP){
			Rectangle2D.Float rect = new Rectangle2D.Float(ICON_INSET, ICON_INSET, width/5, width);
			
			GeneralPath triangle = new GeneralPath();
			triangle.moveTo(ICON_INSET+width*2/5, ICON_INSET);
			triangle.lineTo(ICON_INSET+width, ICON_INSET+width/2);
			triangle.lineTo(ICON_INSET+width*2/5, ICON_INSET+width);
			triangle.closePath();
			
			Area area1 = new Area(rect);
			Area area2 = new Area(triangle);
			area1.add(area2);
			return area1;
		} else if (icon == Icon.STOP) {
			Rectangle2D.Float rect1 = new Rectangle2D.Float(ICON_INSET, ICON_INSET, width, width);
			Area shape1 = new Area(rect1);
			return shape1;			
		}
		return null;
	}
	
	/** Paints this */
	public void paint(Graphics g){
		//selected color
		Color backgroundColor;
		if(this.pressed){
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
		Color topColoring =  backgroundColor.darker().darker().darker();
		Color bottomColoring = backgroundColor.brighter().brighter().brighter();
		gb.setPaint(new GradientPaint(0, INSET, topColoring, 0, buttonHeight, bottomColoring, false));

		// Paint the first layer
		gb.fillOval(INSET, INSET, buttonWidth, buttonHeight);

		// set up paint data fields for second layer
		int highlightHeight = buttonHeight - (HIGHLIGHT_INSET * 2);
		int highlightWidth = buttonWidth - (HIGHLIGHT_INSET * 2);
		topColoring = Color.WHITE;
		bottomColoring = backgroundColor.brighter();

		// Paint the second layer
		gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.8f));
		gb.setPaint(new GradientPaint(0,INSET+HIGHLIGHT_INSET,topColoring,0,INSET+HIGHLIGHT_INSET+(highlightHeight/2), backgroundColor.brighter(), false));
		gb.setClip(new Ellipse2D.Float(INSET+HIGHLIGHT_INSET,INSET+HIGHLIGHT_INSET,highlightWidth,highlightHeight / 2));
		gb.fillOval(INSET+HIGHLIGHT_INSET,INSET+HIGHLIGHT_INSET,highlightWidth,highlightHeight);

		// Blur
		ConvolveOp blurOp = new ConvolveOp(new Kernel(3, 3, BLUR));
		BufferedImage blurredImage = blurOp.filter(buffer, null);
		
		// Draw button
		g2.drawImage(blurredImage, 0, 0, null);
		
		//draw icon
		if(this.icon != null){
			if (this.focus){
				g2.setColor(Color.white);
			}else{
				g2.setColor(Color.gray);
			}
			g2.fill(this.getIconShape(this.icon));
		}
		
	}
	
	/** debugging */
	public static void main(String[] args)
	{
		JFrame f = new JFrame();
		f.setLayout(new FlowLayout());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(300, 100);
		f.getContentPane().add(new CIconButton(CIconButton.Icon.PAUSE));
		f.setVisible(true);
		
	}
}