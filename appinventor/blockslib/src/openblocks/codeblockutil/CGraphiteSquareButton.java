// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

public class CGraphiteSquareButton extends CButton{
	private static final long serialVersionUID = 328149080220L;
	public CGraphiteSquareButton(String text){
		super(Color.black, CGraphite.darkgreen,text);
	}
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Set up first layer
		int buttonHeight = this.getHeight();
		int buttonWidth = this.getWidth();
		Color topColoring;
		Color bottomColoring;
		if(this.pressed || this.selected){
			if(this.focus){
				topColoring =  this.selectedColor.darker().darker().darker();
				bottomColoring = this.selectedColor;
			}else{
				topColoring =  this.selectedColor.darker().darker().darker();
				bottomColoring = this.selectedColor;
			}
		}else{
			if(this.focus){
				topColoring =  this.buttonColor;
				bottomColoring = Color.darkGray;
			}else{
				topColoring =  this.buttonColor;
				bottomColoring = this.buttonColor;
			}
		}
		g2.setPaint(new GradientPaint(0, 0, topColoring, 0, buttonHeight, bottomColoring, false));
		g2.fillRect(0,0, buttonWidth, buttonHeight);
		
		// set up paint data fields for second layer
		int highlightHeight = buttonHeight/2-HIGHLIGHT_INSET;
		int highlightWidth = buttonWidth;
		if(this.pressed || this.selected){
			if(this.focus){
				topColoring = Color.white;
				bottomColoring = this.selectedColor;
			}else{
				topColoring = Color.white;
				bottomColoring = this.selectedColor;
			}
		}else{
			if(this.focus){
				topColoring = Color.white;
				bottomColoring = Color.darkGray;
			}else{
				topColoring = Color.gray;
				bottomColoring = Color.darkGray;
			}
		}
		g2.setPaint(new GradientPaint(0,0,topColoring,0,buttonHeight, bottomColoring, false));
		g2.fillRect(0,0,highlightWidth,highlightHeight);
		
		//set border
		g2.setColor(Color.DARK_GRAY);
		g2.setStroke(new BasicStroke(2));
		g2.drawRect(0,0, buttonWidth-1, buttonHeight-1);
		
		// Draw the text (if any)
		if (this.getText() != null){
			g2.setColor(Color.white);
			Font font = g2.getFont().deriveFont((float)(((float)buttonHeight) * .3));
			g2.setFont(font);
			FontMetrics metrics = g2.getFontMetrics();
			Rectangle2D textBounds = metrics.getStringBounds(this.getText(),g2);
			float x = (float)((this.getWidth() / 2) - (textBounds.getWidth() / 2));
			float y = (float)((this.getHeight() / 2) + (textBounds.getHeight() / 2)) - metrics.getDescent();
			g2.drawString(this.getText(),x,y);
		}
	}
}
