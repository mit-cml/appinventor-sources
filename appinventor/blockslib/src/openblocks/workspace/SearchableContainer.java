// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.workspace;


/**
 * Denotes objects that contain SearchableElements that may be searched by a search bar.
 */
public interface SearchableContainer {

	/**
	 * Returns all of the searchable elements within this Searchable.  For example, the MiniMap contains
	 * RenderableBlocks, which may be searched.
	 * @return the SearchableElements contained within this SearchableContainer.
	 */
	public Iterable<? extends SearchableElement> getSearchableElements();
			
	/**
	 * Called by the search bar to update the Searchable of whether it currently belongs to 
	 * the search bar's results.  Guaranteed to be called on the Swing thread.
	 * 
	 * It is the responsibility of this SearchableContainer to update any necessary state and repaint 
	 * any necessary components as a result of a call to this method.
	 * 
	 * @param containsSearchResults true when the SearchableContainer is part of the current results.
	 */
	public void updateContainsSearchResults(boolean containsSearchResults);
}
