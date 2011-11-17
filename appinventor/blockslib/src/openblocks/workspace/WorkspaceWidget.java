package openblocks.workspace;

import java.util.Collection;

import javax.swing.JComponent;

import openblocks.renderable.RenderableBlock;




/** WorkspaceWidgets are components within the workspace other than blocks that
 * include bars, buttons, factory drawers, and single instance widgets such as
 * the MiniMap and the TrashCan.
 */

public interface WorkspaceWidget {

    /**
     * Called by RenderableBlocks that get "dropped" onto this Widget
     * @param block the RenderableBlock that is "dropped" onto this Widget
     */
	public void blockDropped(RenderableBlock block);
    
	/**
	 * Called by RenderableBlocks as they are dragged over this Widget.
	 * @param block the RenderableBlock being dragged
	 */
	public void blockDragged(RenderableBlock block);
	
	/**
	 * Called when a RenderableBlock is being dragged and goes from being
	 * outside this Widget to being inside the Widget.
	 * @param block the RenderableBlock being dragged
	 */
	public void blockEntered(RenderableBlock block);
	
	/**
	 * Called when a RenderableBlock that was being dragged over this Widget
	 * goes from being inside this Widget to being outside the Widget.
	 * @param block the RenderableBlock being dragged
	 */
	public void blockExited(RenderableBlock block);
	
	/**
	 * Used by RenderableBlocks to tell their originating Widgets that
	 * they're moving somewhere else and so should be removed.
	 * @param block the RenderableBlock
	 */
    public void removeBlock(RenderableBlock block);
    
    /**
     * Adds the specified block to this widget interally and graphically.  The 
     * difference between this method and blockDropped is that blockDropped is
     * activated by user actions, such as mouse drag and drop or typeblocking.
     * Use this method only for single blocks, as it may cause repainting!  For
     * adding several blocks at once use addBlocks, which delays graphical updates
     * until after the blocks have all been added.
     * @param block the desired RenderableBlock to add to this
     */
    public void addBlock(RenderableBlock block);  //TODO ria maybe rename this to putBlock?
    
    /**
     * Adds a collection of blocks to this widget internally and graphically.
     * This method adds blocks internally first, and only updates graphically
     * once all of the blocks have been added.  It is therefore preferable to
     * use this method rather than addBlock whenever multiple blocks will be added.
     * @param blocks the Collection of RenderableBlocks to add
     */
    public void addBlocks(Collection<RenderableBlock> blocks);
    
    /**
     * Widgets must be able to report whether a given point is inside them
     * @param x
     * @param y
     */
    public boolean contains(int x, int y);
    
    /**
     * Very Java Swing dependent method
     * @return the JComponent-ized cast of this widget.
     */
    public JComponent getJComponent();
    
    /**
     * Returns the set of blocks that abstract "lives" inside this widget.
     * Does not return all the blocks that exists in thsi component,
     * or return all the blocks that are handled by this widget.  Rather,
     * the set of blocks returned all the blocks that "lives" in this widget.
     */
	public Iterable<RenderableBlock> getBlocks();
}
