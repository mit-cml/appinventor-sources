// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.TranslationDesignerPallete;
import com.google.appinventor.client.utils.PZAwarePositionCallback;
import com.google.common.base.Strings;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Defines a widget that has the appearance of a question mark and
 * creates a popup with information about a component when it is clicked on.
 *
 */
public final class ComponentHelpWidget extends Image {
  private static ImageResource imageResource = null;

  // Keep track of the last time (in milliseconds) of the last closure
  // so we don't reopen a popup too soon after closing it.  Specifically,
  // if a user clicks on the question-mark icon to close a popup, we
  // don't want the question-mark click to reopen it.
  private long lastClosureTime = 0;

  private class ComponentHelpPopup extends PopupPanel {

    private ComponentHelpPopup(final SimpleComponentDescriptor scd,
                               final Widget sender) {
      // Create popup panel.
      super(true);
      setStyleName("ode-ComponentHelpPopup");
      setTitle(scd.getName());

      // Create title from component name.
      Label titleBar = new Label(TranslationDesignerPallete.getCorrespondingString(scd.getName()));
      setTitle(scd.getName());
      titleBar.setStyleName("ode-ComponentHelpPopup-TitleBar");

      // Create content from help string.
      HTML helpText = new HTML(TranslationDesignerPallete.getCorrespondingString(scd.getName() + "-helpString"));
      helpText.setStyleName("ode-ComponentHelpPopup-Body");

      // Create panel to hold the above three widgets and act as the
      // popup's widget.
      VerticalPanel inner = new VerticalPanel();
      inner.add(titleBar);
      inner.add(helpText);

      // Create link to more information.  This would be cleaner if
      // GWT supported String.format.
      String referenceComponentsUrl = Ode.getInstance().getSystemConfig().getReferenceComponentsUrl();
      if (!Strings.isNullOrEmpty(referenceComponentsUrl)) {
        if (!referenceComponentsUrl.endsWith("/")) {
          referenceComponentsUrl += "/";
        }
        String categoryDocUrlString = scd.getCategoryDocUrlString();
        String url = (categoryDocUrlString == null)
            ? referenceComponentsUrl + "index.html"
            : referenceComponentsUrl + categoryDocUrlString + ".html#" + scd.getName();
        HTML link = new HTML("<a href=\"" + url + "\" target=\"_blank\">" +
            MESSAGES.moreInformation() + "</a>");
        link.setStyleName("ode-ComponentHelpPopup-Link");
        inner.add(link);
      }

      setWidget(inner);

      // When the panel is closed, save the time in milliseconds.
      // This will help us avoid immediately reopening it if the user
      // closed it by clicking on the question-mark icon.
      addPopupListener(new PopupListener() {
          @Override
          public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
            lastClosureTime = System.currentTimeMillis();
          }
        });

      // Use a Pinch Zoom aware PopupPanel.PositionCallback to handle positioning to
      // avoid the Google Chrome Pinch Zoom bug.
      setPopupPositionAndShow(new PZAwarePositionCallback(sender.getElement()) {
        @Override
        public void setPosition(int offsetWidth, int offsetHeight) {
          // Position the upper-left of the panel just to the right of the
          // question-mark icon, unless that would make it too low.
          final int X_OFFSET = 20;
          final int Y_OFFSET = -5;
          if(Window.Navigator.getUserAgent().contains("Chrome") && isPinchZoomed()) {
            setPopupPosition(getTrueAbsoluteLeft() + 1 + X_OFFSET,
                Math.min(getTrueAbsoluteTop() + 1 + Y_OFFSET,
                    Math.max(0, Window.getClientHeight()
                        - offsetHeight + Y_OFFSET)));
          } else {
            setPopupPosition(sender.getAbsoluteLeft() + X_OFFSET,
                Math.min(sender.getAbsoluteTop() + Y_OFFSET,
                    Math.max(0, Window.getClientHeight()
                        - offsetHeight + Y_OFFSET)));
          }
        }
      });
    }
  }

  public ComponentHelpWidget(final SimpleComponentDescriptor scd) {
    if (imageResource == null) {
      Images images = Ode.getImageBundle();
      imageResource = images.help();
    }
    AbstractImagePrototype.create(imageResource).applyTo(this);
    addClickListener(new ClickListener() {
        @Override
        public void onClick(Widget sender) {
          final long MINIMUM_MS_BETWEEN_SHOWS = 250;  // .25 seconds

          if (System.currentTimeMillis() - lastClosureTime >=
              MINIMUM_MS_BETWEEN_SHOWS) {
            new ComponentHelpPopup(scd, sender);
          }
        }
      }
      );
  }
}
