// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.JFrame;

public class CRadioactiveButton extends CButton{
	private static final long serialVersionUID = 328149080258L;
	//To get the shadow effect the text must be displayed multiple times at
	//multiple locations.  x represents the center, white label.
	// o is color values (0,0,0,0.5f) and Y is black.
	//			  o o
	//			o x Y o
	//			o Y o
	//			  o
	//offsetArrays representing the translation movement needed to get from
	// the center location to a specific offset location given in {{x,y},{x,y}....}
	//..........................................grey points.............................................black points
	private final int[][] shadowPositionArray = {{0,-1},{1,-1}, {-1,0}, {2,0},	{-1,1}, {1,1},  {0,2}, 	{1,0},  {0,1}};
	private final float[] shadowColorArray =	{0.5f,	0.5f,	0.5f, 	0.5f, 	0.5f, 	0.5f,	0.5f,	0,		0};
	private double offsetSize = 1;
	public CRadioactiveButton(Color buttonColor, Color selectedColor, String text){
		super(buttonColor, selectedColor, text);
	}
	public void paint(Graphics g){
		// Set up graphics and buffer
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		BufferedImage buffer = GraphicsManager.gc.createCompatibleImage(this.getWidth(), this.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D gb = buffer.createGraphics();
		gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Set up first layer
		int buttonHeight = this.getHeight()-INSET*2;
		int buttonWidth = this.getWidth()-INSET*2;
		int arc = buttonHeight;

		if(this.pressed || this.selected){
			g2.setPaint(new GradientPaint(0, -buttonHeight, Color.darkGray, 0, buttonHeight, buttonColor, false));
			g2.fillRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
			g2.setColor(Color.darkGray);
			g2.drawRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
		}else{
			//paint highlightlayer
			if(this.focus){
				gb.setColor(Color.yellow);
				gb.setStroke(new BasicStroke(3));
				gb.drawRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
				gb.setStroke(new BasicStroke(1));
			}
			// Paint the first layer
			gb.setColor(buttonColor.darker());
			gb.fillRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);
			gb.setColor(Color.darkGray);
			gb.drawRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);

			// set up paint data fields for second layer

			int highlightHeight = buttonHeight*2/3;
			int highlightWidth = buttonWidth;
			int highlightArc = highlightHeight;

			// Paint the second layer
			gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.8f));

			gb.setColor(buttonColor);
			gb.setClip(new RoundRectangle2D.Float(INSET,INSET,highlightWidth,highlightHeight,highlightArc,highlightArc));
			gb.fillRoundRect(INSET, INSET, buttonWidth, buttonHeight, arc, arc);

			// Blur
			ConvolveOp blurOp = new ConvolveOp(new Kernel(3, 3, BLUR));
			BufferedImage blurredImage = blurOp.filter(buffer, null);

			// Draw button
			g2.drawImage(blurredImage, 1, 1, null);
		}
		// Draw the text (if any)
		String text = this.getText();
		if(text != null && buttonHeight > 4){
                        Font font = g2.getFont().deriveFont((float)(((float)buttonHeight) * .6));
			//Font font = g2.getFont().deriveFont((float)(this.getHeight()-INSET*2-2)*.7f);
			g2.setFont(font);
			FontMetrics metrics = g2.getFontMetrics();
			Rectangle2D textBounds = metrics.getStringBounds(this.getText(),g2);
			float x = (float)((this.getWidth() / 2) - (textBounds.getWidth() / 2));
			float y = (float)((this.getHeight() / 2) + (textBounds.getHeight() / 2)) - metrics.getDescent();

			g.setColor(Color.black);
			for (int i = 0; i < shadowPositionArray.length; i++) {
				int dx = shadowPositionArray[i][0];
				int dy = shadowPositionArray[i][1];
				g2.setColor(new Color(0,0,0, shadowColorArray[i]));
				g2.drawString(text, x+(int)((dx)*offsetSize), y+(int)((dy)*offsetSize));
			}
			g2.setColor(Color.white);
			g2.drawString(text, x, y);
		}
	}


	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new FlowLayout());
		f.setSize(500, 300);
		CButton c = new CRadioactiveButton(Color.cyan, Color.green.brighter().brighter().brighter(),"hi");
		c.setPreferredSize(new Dimension(400,200));

		f.add(c);
		f.setVisible(true);
		f.repaint();

	}
}
