// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.widgets;

import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget whose appearance and behavior is cloned from an original widget.
 * <p>
 * This type of widget is commonly used as the drag-widget of a {@link DragSource}.
 *
 */
public class ClonedWidget extends Widget {
  public ClonedWidget(Widget origWidget) {
    this(origWidget.getElement());
  }

  private ClonedWidget(Element origElement) {
    setElement(DOM.clone(origElement, true));
  }
}
