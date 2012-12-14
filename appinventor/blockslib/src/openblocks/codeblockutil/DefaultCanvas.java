// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;


/**
 * A Canvas is a low-level, mutable, CSwing component.
 * It is the UI component onto which other UI components
 * are added.
 *
 * An explorer, for example, may have a set of canvases that it
 * controls and displays.
 *
 * A Navigator may take a set of canvases and control them
 * in some complex way that mimicks real life experiences
 * (such as sliding, fading, flying).
 *
 * A canvas should be the only part of a high-level CSwing
 * object that users may manipulate.
 *
 * @specfield name
 * @specfield color
 * @specfield buttonHeight
 * @specfield highlight
 *
 */
public class DefaultCanvas extends JPanel implements Canvas{
	private static final long serialVersionUID = 328149080290L;
	/**
	 * constructs a new Canvas which supports all JComponent functions,
	 * and with a default color of blue, and a no highlight values.
	 *
	 */
	public DefaultCanvas() {
		super();
	}
	/**
	 * constructs a new Canvas which supports all JComponent functions,
	 * and with a default color of blue, and a no highlight values.
	 *
	 */
	public DefaultCanvas (LayoutManager m){
		super(m);
	}
	/**
	 * @return color of Canvas or null by default
	 */
	public Color getColor(){
	  return CGraphite.darkgreen;
	}
	/**
	 * @return the JComponent representation of this Canvas.  MAY NOT BE NULL.
	 */
	public JComponent getJComponent(){
		return this;
	}
	/**
	 * If this canvas is to be highlighted (because it might
	 * have focus for example), then getHighlight should return
	 * the color of that highlight.  Otherwise, it should
	 * return null
	 * @return highlighting color or null by default.
	 */
	public Color getHighlight(){
		return null;
	}
}
