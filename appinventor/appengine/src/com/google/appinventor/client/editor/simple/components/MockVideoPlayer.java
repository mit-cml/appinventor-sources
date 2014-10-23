// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Mock VideoPlayer component.
 *
 * @author halabelson@google.com (Hal Abelson)
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockVideoPlayer extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "VideoPlayer";

  // GWT panel widget used to mock a Simple VideoPlayer
  private final SimplePanel videoPlayerWidget;

  /**
   * Creates a new MockVideoPlayer component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockVideoPlayer(SimpleEditor editor) {
    super(editor, TYPE, images.videoplayer());

    // Initialize mock video UI
    videoPlayerWidget = new SimplePanel();
    videoPlayerWidget.setStylePrimaryName("ode-SimpleMockComponent");
    videoPlayerWidget.setWidget(getIconImage());
    initComponent(videoPlayerWidget);
  }

  /**
   * Sets the videoPlayer's Source property to a new value.
   */
  private void setSourceProperty(String text) {
    // TODO:
    // we should do something here to set the correct size and
    // show an appropriate image in the designer
    //ProjectNode asset = getAssetNode(text);
  }

  @Override
  public int getPreferredWidth() {
    // The superclass uses getOffsetWidth, which won't work for us.
    return ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    // The superclass uses getOffsetHeight, which won't work for us.
    return ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_SOURCE)) {
      setSourceProperty(newValue);
      // TODO(lizlooney) - If we ever add code to setSourceProperty to show an appropriate image,
      // we'll need to call refreshForm() here.
      //refreshForm();
    }
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    return super.isPropertyVisible(propertyName);
  }
}
