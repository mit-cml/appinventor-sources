package openblocks.workspace;

public interface SearchableElement {

	/**
	 * Returns the keyword representation of the element, which is used to determine whether it belongs
	 * to a particular query.
	 * @return the keyword representation of this Searchable.
	 */
	public String getKeyword();

	/**
	 * Called by the search bar to update the SearchableElement of whether it currently belongs to 
	 * the search bar's results.  Guaranteed to be called on the Swing thread.
	 * 
	 * It is the responsibility of this SearchableElement to update any necessary state and repaint 
	 * any necessary components as a result of a call to this method.
	 * 
	 * @param inSearchResults true when the SearchableElement is part of the current results.
	 */
	public void updateInSearchResults(boolean inSearchResults);
}
