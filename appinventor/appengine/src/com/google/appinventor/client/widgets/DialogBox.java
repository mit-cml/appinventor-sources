// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 Massachusetts Institute of Technology. All Rights Reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.appinventor.client.utils.TableAccessibility;

/**
 * A {@link com.google.gwt.user.client.ui.DialogBox} whose frame is not announced as a table.
 *
 * <p>GWT draws the frame of a dialog box with a 3x3 {@code <table>}. This class marks that
 * table as used only for layout, so screen readers read the caption and the content of the
 * dialog without announcing a table around them.</p>
 */
public class DialogBox extends com.google.gwt.user.client.ui.DialogBox {

  public DialogBox() {
    super();
    setFrameLayoutOnly();
  }

  public DialogBox(boolean autoHide) {
    super(autoHide);
    setFrameLayoutOnly();
  }

  public DialogBox(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    setFrameLayoutOnly();
  }

  public DialogBox(boolean autoHide, boolean modal, Caption captionWidget) {
    super(autoHide, modal, captionWidget);
    setFrameLayoutOnly();
  }

  private void setFrameLayoutOnly() {
    TableAccessibility.setEnclosingLayoutTable(getCellElement(1, 1));
  }
}
