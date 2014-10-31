// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for mock components that need a wrapper around them so that the
 * real component's border is not overridden by our CSS border (see
 * ode-SimpleMockComponent in Ya.css)
 *
 * <p/>For example, this class is extended by MockButtonBase, MockTextBoxBase,
 * and MockPasswordTextBoxBase, so that the 3D border of those components is not
 * replaced by the border specified in the CSS style ode-SimpleMockComponent.
 * We need both the component's 3D border as well as the 2 pixel outline border
 * (red when selected) that comes from the ode-SimpleMockComponent style.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class MockWrapper extends MockVisibleComponent {
  private final SimplePanel wrapper;
  private int[] preferredSize;

  MockWrapper(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, icon);

    // Initialize wrapper UI
    wrapper = new SimplePanel();
    wrapper.setStylePrimaryName("ode-SimpleMockComponent");
  }

  /**
   * Sets the widget that is wrapped by the wrapper.
   */
  protected final void initWrapper(Widget wrappedWidget) {
    // The wrapped widget should always fill up the wrapper.
    wrappedWidget.setSize("100%", "100%");
    wrapper.setWidget(wrappedWidget);
    initComponent(wrapper);
  }

  /**
   * Subclasses must implement createClonedWidget.
   *
   * <p/>It should return a clone of the widget being wrapped that will be used
   * to determine the preferred size, without having the size constrained by
   * its parent, since the cloned widget won't have a parent.
   */
  protected abstract Widget createClonedWidget();

  protected final void updatePreferredSize() {
    preferredSize = MockComponentsUtil.getPreferredSizeOfDetachedWidget(createClonedWidget());
  }

  @Override
  public int getPreferredWidth() {
    // The superclass uses getOffsetWidth, which won't work for us.
    if (preferredSize == null) {
      updatePreferredSize();
    }
    return preferredSize[0];
  }

  @Override
  public int getPreferredHeight() {
    // The superclass uses getOffsetHeight, which won't work for us.
    if (preferredSize == null) {
      updatePreferredSize();
    }
    return preferredSize[1];
  }
}
