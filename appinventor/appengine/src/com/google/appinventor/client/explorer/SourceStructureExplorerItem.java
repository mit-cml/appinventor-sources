// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer;

/**
 * Defines an interface for an item in the source structure explorer.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface SourceStructureExplorerItem {
  /**
   * Invoked when this item is selected.
   */
  void onSelected();

  /**
   * Invoked when this item is expanded or collapsed
   */
  void onStateChange(boolean open);

  /**
   * Returns true if this item can be renamed.
   */
  boolean canRename();

  /**
   * Invoked when the source structure explorer's Rename button is clicked.
   */
  void rename();

  /**
   * Returns true if this item can be deleted.
   */
  boolean canDelete();

  /**
   * Invoked when the source structure explorer's Delete button is clicked.
   */
  void delete();
}
