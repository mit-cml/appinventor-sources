// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.Icon;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Class for components without a visual representation.
 *
 * TODO(user) - the following information is no longer accurate and needs to be updated
 *
 * To add a new non-visual component:
 * - Put the image for the component's icon in com/google/appinventor/images/.
 * - In the @DesignerComponent annotation for the component's actual class (device)
 * implementation, include the key/value pairs:
 *
 *     nonVisible = true,
 *     iconName = "images/FILENAME"
 *
 * where FILENAME is the file name (last path element) of the icon image file.
 *
 * For good performance in fetching image files, also do this:
 *   - Add a @Source entry and corresponding method in
 *   com/google/appinventor/client/Images.java
 *   - Add the mapping from iconName to the Images method to
 *   SimpleComponentDescriptor.initBundledImages()
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
