// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
