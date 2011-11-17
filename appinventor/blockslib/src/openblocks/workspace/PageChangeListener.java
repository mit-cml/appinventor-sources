package openblocks.workspace;

/**
 * The PageChangedListener interface must be implemented by anything
 * that wants to be notified when a page's internal state changes
 * (i.e. due to resize, rename, etc.)
 */
public interface PageChangeListener {
	/**
	 * notifies this PageChangeListener that at least one
	 * of the pages have changed states.
	 */
	public void update();
}
