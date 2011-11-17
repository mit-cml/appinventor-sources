package openblocks.codeblockutil;

import java.awt.Color;

import javax.swing.JComponent;

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
public interface Canvas {
	public String LABEL_CHANGE = "label_change_property";
	/**
	 * @return name of Canvas or an empty String by default.  MAY NOT BE NULL.
	 */
	public String getName();
	/**
	 * @return color of Canvas or null by default
	 */
	public Color getColor();
	/**
	 * @return the JComponent representation of this Canvas.  MAY NOT BE NULL.
	 */
	public JComponent getJComponent();
	/**
	 * If this canvas is to be highlighted (because it might
	 * have focus for example), then getHighlight should return
	 * the color of that highlight.  Otherwise, it should
	 * return null
	 * @return highlighting color or null by default.
	 */
	public Color getHighlight();
}
