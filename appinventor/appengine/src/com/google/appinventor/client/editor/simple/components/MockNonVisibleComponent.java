// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.Icon;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Class for components without a visual representation, such as sensors.
 *
 * <p>To add a new non-visual component:
 * <ol>
 *   <li> Put the image for the component's icon in {@code
 *        appengine/src/com/google/appinventor/images/FILENAME}
 *        (substituting the actual filename).
 *   <li> In the class implementing the component (in the directory
 *        {@code components/src/com/google/appinventor/components/runtime/}),
 *        make sure the
 *        {@link com.google.appinventor.components.annotations.DesignerComponent} annotation
 *        includes the following key-value pairs:
 *        <ul>
 *            <li> nonVisible = true
 *            <li> iconName = "images/FILENAME"
 *       </ul>
 *   <li> For good performance in fetching image files:
 *        <ul>
 *            <li> Add a {@link com.google.gwt.resources.client.ClientBundle.Source}
 *                 annotation and corresponding method to
 *                 {@code appengine/src/com/google/appinventor/client/Images.java}.
 *            <li> Add a mapping from the name of the icon to this new method to the method
 *                 {@code initBundledImages()} in {@code
 *                 appengine/src/com/google/appinventor/client/editor/simple/palette/SimpleComponentDescriptor.java}.
 *       </ul>
 *   </ol>
 *
 * @author sharon@google.com (Sharon Perl)
 */
public final class MockNonVisibleComponent extends MockComponent {

  private final Icon iconWidget;

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   */
  public MockNonVisibleComponent(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
    iconWidget = new Icon(iconImage);

    initComponent(iconWidget);
  }

  @Override
  public final void initComponent(Widget widget) {
    super.initComponent(widget);
  }

  /**
   * {@inheritDoc}
   *
   * This is always {@code false} for subclasses of this class.
   */
  @Override
  public final boolean isVisibleComponent() {
    return false;
  }

  @Override
  public final int getPreferredHeight() {
    return 0;
  }

  @Override
  public final int getPreferredWidth() {
    return 0;
  }

  @Override
  protected final void onSelectedChange(boolean selected) {
    super.onSelectedChange(selected);
    if (selected) {
      iconWidget.select();
    } else {
      iconWidget.deselect();
    }
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // No visual changes after property changes (other than icon name).
    if (propertyName.equals(PROPERTY_NAME_NAME)) {
      iconWidget.setCaption(newValue);
    }
  }
}
