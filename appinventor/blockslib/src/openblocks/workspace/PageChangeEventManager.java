package openblocks.workspace;

import java.util.HashSet;
import java.util.Set;

/**
 * The PageChangeEventmanager is in charged of
 * handling all page-changed events triggered by
 * Pages and notifying page-change listeners when
 * such an event is triggered.
 * 
 * A page-change event is thrown by invoking the
 * manager through the static method
 * PageChangeEventManager.notifyListeners().
 * 
 * An object can subscribe to page-change events
 * by delegating itself through the static method
 * PageChangeEventManager.addPageChangeListener(). 
 */
public class PageChangeEventManager {
	/** A NON-REPEATING set of page-change listeners */
	private static Set<PageChangeListener> observers = new HashSet<PageChangeListener>();
	
	/**
	 * @param l - the listener to be added
	 * 
	 * @requires l != null
	 * @modifies the set of observers handled by this event manager
	 * @effects subscribes a new listener to page changed events
	 * @throws RuntimeException if l is null
	 */
	
	public static void addPageChangeListener(PageChangeListener l){
		if(l == null) throw new RuntimeException("May not subsribe a null listener to PageChanged events");
		observers.add(l);
	}
	
	/**
	 * @requires none
	 * @modifies all subscribing page change listeners
	 * @effects notifies all observers of Page Changed events to update themselves
	 */
	public static void notifyListeners(){
		for(PageChangeListener l : observers){
			l.update();
		}
	}
	
}
