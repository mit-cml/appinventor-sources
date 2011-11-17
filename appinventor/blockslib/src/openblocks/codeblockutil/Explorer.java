package openblocks.codeblockutil;

import java.util.Collection;

import javax.swing.JComponent;

/**
 * An Explorer explores canvases.
 *
 * The philosophy above simplifies the relationship between the
 * various low-level and high-level components used to display
 * mulitple canvases in a limited space.
 *
 * An explorer commands a set of Canvases and tells them where
 * to go, when to be visbilty, when to not be visible, and how
 * they should "hide" themselves when not being used.
 *
 * There are many ways to achieve this UI.  One way is to
 * exmploy the "stack" loook and feel, where each canvas is
 * stacked on top of each other.  Another more common
 * solution is the tabbed look and feel, where each canvas
 * is a tab.  Other possibilities consists of internal windows,
 * hiding toolpane, etc.
 *
 * Remember, an Explorer is simply a controller of
 * canvases.  Nothing more.  ANY subtype of Explorer
 * should hold this aspect as true.  Never should an Explorer
 * be any less than an explorer of canvases.
 *
 * @specfield set of canvases
 * @specfield JComponent representation
 * @specfield name -- THE NAME MAY ONLY BE SET ONCE
 *
 */
public interface Explorer {
	/**
	 * @return JCompoent representation of this.  MAY NOT BE NULL.
	 */
	public JComponent getJComponent();
	/**
	 * @return name of exlorer or null by default.
	 */
	public String getName();
	/**
	 * sets the name of this exlorer
	 * @param name
	 */
	public void setName(String name);
	/**
	 * Reassigns the set of canvases that this explorer controls.
	 * Though the collection of canvas mnay be empty, it may not be null.
	 * @param items
	 *
	 * @requires items != null &&
	 * 			 for each element in item, element!= null
	 */
	public void setDrawersCard(Collection<? extends Canvas> items);

    public void addDrawersCard(Canvas item, int index);

	/**
	 * Selects the canvas at the specified index.  If the index is
	 * out of bounds, perform no action.
	 * @param index - the index of the canvas to be viewed.
	 *
	 * @requires none (INDEX DOES NOT HAVE TO BE WITHIN BOUNDS)
	 */
	public void selectCanvas(int index);
	/**
	 * Reforms this explorer based on the new size or location of this explorer.
	 * For some explorers whose implementation does not depend on the size of itself,
	 * this method may trigger no action.
	 */
	public void reformView();
	/**
	 * Returns whether any Canvas is currently selected
	 */
	public boolean anyCanvasSelected();
	/**
	 * Returns the width of the Canvas when selected
	 * @return the width of the Canvas when selected
	 */
	public int getSelectedCanvasWidth();
	/**
	 * Adds listener to the explorer
	 * @param gel
	 */
	public void addListener(ExplorerListener gel);
	/**
	 * Removes listener from the explorer
	 * @param gel
	 */
	public void removeListener(ExplorerListener gel);
}
