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
   * Invoked after one or more components are removed
   */
  void onComponentTypeRemoved(List<String> componentTypes);
  
  /**
   * Called when database is reset to contain only internal components
   */
  void onResetDatabase();
  
  
}
