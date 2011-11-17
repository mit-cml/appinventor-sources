// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.dnd.DropTarget;

/**
 * DragSources that require external information to determine what their
 * set of permissible drop-targets are may delegate to instances of this
 * interface to acquire that information.
 *
 */
public interface DropTargetProvider {
  /**
   * @see DragSource#getDropTargets()
   */
  public DropTarget[] getDropTargets();
}
