// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;

/**
 * AbstractPaletteItemWidget provides a common superclass for buttons presented in the
 * {@link SimplePaletteItem} for a component.
 */
public abstract class AbstractPaletteItemWidget extends Image {
  protected final SimpleComponentDescriptor scd;

  AbstractPaletteItemWidget(SimpleComponentDescriptor scd, ImageResource image) {
    this.scd = scd;

    AbstractImagePrototype.create(image).applyTo(this);
    this.addStyleName("ode-SimplePaletteItem-button");

    addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        handleClick();
      }
    });
    addTouchStartHandler(new TouchStartHandler() {
      @Override
      public void onTouchStart(TouchStartEvent touchStartEvent) {
        // Otherwise captured by SimplePaletteItem
        touchStartEvent.stopPropagation();
      }
    });
    addTouchEndHandler(new TouchEndHandler() {
      @Override
      public void onTouchEnd(TouchEndEvent touchEndEvent) {
        handleClick();
      }
    });
  }

  /**
   * Handles when the user clicks (or taps) on the button.
   */
  protected abstract void handleClick();
}
