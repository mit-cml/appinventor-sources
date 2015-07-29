package com.google.appinventor.client.explorer.project;

import java.util.List;


/**
 * Listener interface for ComponentDatabase changes
 */
public interface ComponentDatabaseChangeListener {
  
  /**
   * Invoked after one or more components are added
   */
  void onComponentTypeAdded(List<String> componentTypes);

  /**
   * Invoked just before one or more components are removed <br>
   * Must Return true for successful removal of Component!
   */
  boolean beforeComponentTypeRemoved(List<String> componentTypes);

  /**
   * Invoked after one or more components are removed <br>
   * Invoked only after beforeComponentTypeRemoved returns true
   */
  void onComponentTypeRemoved(List<String> componentTypes);
  
  /**
   * Called when database is reset to contain only internal components
   */
  void onResetDatabase();
  
  
}
