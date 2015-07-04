// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Implements a UI style icon with caption as seen on the desktops of many
 * operating systems.
 *
 */
public class Icon extends Composite {

  // UI elements
  private final VerticalPanel panel;
  private final Label captionLabel;

  /**
   * Creates a new icon with no caption.
   * <p>
   * It is recommended that the caller specify a caption with a call to
   * {@link #setCaption(String)}.
   *
   * @param image  image shown on icon (preferably 16 x 16px)
   */
  public Icon(Image image) {
    this(image, "");
  }

  /**
   * Creates a new icon with the specified caption.
   *
   * @param image  image shown on icon (preferably 16 x 16px)
   * @param caption  caption shown below image
   */
  public Icon(Image image, String caption) {

    panel = new VerticalPanel() {
      @Override
      public void onBrowserEvent(Event event) {
        Icon.this.onBrowserEvent(event);
      }
    };
    panel.add(image);
    panel.setCellHorizontalAlignment(image, VerticalPanel.ALIGN_CENTER);
    captionLabel = new Label(caption);
    panel.add(captionLabel);

    initWidget(panel);

    setStylePrimaryName("ode-Icon");
  }

  /**
   * Sets a new caption for the icon.
   *
   * @param caption  new caption
   */
  public void setCaption(String caption) {
    captionLabel.setText(caption);
  }

  /**
   * Need to override this method because we need to forward the desired event
   * mask to the panel which is the container for this widget.
   *
   * {@inheritDoc}
   */
  @Override
  public void sinkEvents(int eventBitsToAdd) {
    panel.sinkEvents(eventBitsToAdd);
  }

  /**
   * Visually marks the icon as being selected.
   */
  public void select() {
    setStylePrimaryName("ode-Icon-selected");
  }

  /**
   * Visually deselects the icon.
   */
  public void deselect() {
    setStylePrimaryName("ode-Icon");
  }
}
