// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

public class CTabButton extends CButton{
	private static final long serialVersionUID = 328149080252L;
	public CTabButton(String text){
		super(Color.black, CGraphite.darkgreen, text);
	}
	/**
	 * re paints this
	 */
	public void paint(Graphics g){
		// Set up graphics and buffer
		//super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Set up first layer
		int buttonHeight = this.getHeight() - (INSET * 2);
		int buttonWidth = this.getWidth() - (INSET * 2);
		int arc = buttonHeight/3;
		Color topColoring;
		Color bottomColoring;
		if(this.focus || this.selected){
			topColoring =  this.selectedColor.darker().darker().darker();
			bottomColoring = this.selectedColor;
		}else{
			topColoring =  this.buttonColor;
			bottomColoring = this.buttonColor;
		}
		// Paint the first layer
		g2.setPaint(new GradientPaint(0, 0, topColoring, 0, buttonHeight, bottomColoring, false));
		g2.fillRoundRect(INSET, INSET, buttonWidth, buttonHeight+arc, arc, arc);
		g2.setColor(Color.darkGray);
		g2.drawRoundRect(INSET, INSET, buttonWidth, buttonHeight+arc, arc, arc);
		
		// set up paint data fields for second layer
		int highlightHeight = buttonHeight/2-HIGHLIGHT_INSET;
		int highlightWidth = buttonWidth - (HIGHLIGHT_INSET * 2)+1;
		if(this.focus || this.selected){
			topColoring = Color.white;
			bottomColoring = this.selectedColor;
		}else{
			topColoring = Color.gray;
			bottomColoring = Color.darkGray;
		}
		// Paint the second layer
		g2.setPaint(new GradientPaint(0,0,topColoring,0,buttonHeight, bottomColoring, false));
		g2.fillRoundRect(INSET+HIGHLIGHT_INSET,INSET+HIGHLIGHT_INSET+1,highlightWidth,highlightHeight,arc,arc);
		//g2.setColor(Color.gray);
		//g2.drawRoundRect(INSET+HIGHLIGHT_INSET,INSET+HIGHLIGHT_INSET,highlightWidth,highlightHeight,arc,arc);
		
		
		// Draw the text (if any)
		Font font;
		if (this.getText() != null){
			if(this.selected){
				g2.setColor(Color.white);
				font = new Font("Ariel", Font.BOLD, buttonHeight*2/3);
			}else{
				g2.setColor(Color.white);
				font = new Font("Ariel", Font.PLAIN, buttonHeight*2/3);
				//font = g2.getFont().deriveFont((float)(((float)buttonHeight) * .4));
			}
			g2.setFont(font);
			FontMetrics metrics = g2.getFontMetrics();
			Rectangle2D textBounds = metrics.getStringBounds(this.getText(),g2);
			float x = (float)((this.getWidth() / 2) - (textBounds.getWidth() / 2));
			float y = (float)((this.getHeight() / 2) + (textBounds.getHeight() / 2)) - metrics.getDescent();
			g2.drawString(this.getText(),x,y);
		}
	}
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new FlowLayout());
		f.setSize(500, 300);
		CButton c = new CTabButton("hi");
		c.setPreferredSize(new Dimension(400,200));
		f.add(c);
		f.setVisible(true);
		f.repaint();
		
	}
}
