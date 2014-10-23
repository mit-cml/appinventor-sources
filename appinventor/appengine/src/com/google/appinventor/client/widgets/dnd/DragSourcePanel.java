// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.dnd;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel containing a single draggable widget.
 * <p>
 * Extending this class is the easiest way to provide the drag part of
 * drag and drop support for a widget. Clients that do not wish to extend
 * this class may use {@link DragSourceSupport} directly instead to achieve
 * the same effect.
 * <p>
 * Implementations must call {@link #add(Widget)} with the widget to make draggable.
 *
 */
public abstract class DragSourcePanel extends FocusPanel implements DragSource {

  protected final DragSourceSupport dragSourceSupport;

  /**
   * Creates a new drag source panel.
   */
  public DragSourcePanel() {
    // Listen for drag gestures on self
    dragSourceSupport = new DragSourceSupport(this);
    addMouseListener(dragSourceSupport);
  }
}
