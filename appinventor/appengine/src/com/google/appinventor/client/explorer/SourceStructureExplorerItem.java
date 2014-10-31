// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
