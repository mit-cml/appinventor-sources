// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslationTable;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.utils.PZAwarePositionCallback;
import com.google.common.base.Strings;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Defines a widget that has the appearance of a question mark and
 * creates a popup with information about a component when it is clicked on.
 *
 */
public final class ComponentHelpWidget extends AbstractPaletteItemWidget {
  private static final ImageResource imageResource = Ode.getImageBundle().help();

  // Keep track of the last time (in milliseconds) of the last closure
  // so we don't reopen a popup too soon after closing it.  Specifically,
  // if a user clicks on the question-mark icon to close a popup, we
  // don't want the question-mark click to reopen it.
  private long lastClosureTime = 0;

  private class ComponentHelpPopup extends DialogBox {

    private ComponentHelpPopup() {
      // Create popup panel.
      super(true, true);
      setStyleName("ode-DialogBox");
      setText(scd.getName());

      // Create content from help string.
      String helpTextKey = scd.getExternal() ? scd.getHelpString() : scd.getName();
      String translatedHelpText = ComponentTranslationTable.getComponentHelpString(helpTextKey);
      if (!scd.getExternal() && translatedHelpText.equals(scd.getName())
          && !scd.getHelpString().isEmpty()) {
        translatedHelpText = scd.getHelpString();
      }
      HTML helpText = new HTML(translatedHelpText);
      helpText.setStyleName("ode-ComponentHelpPopup-Body");

      // Create panel to hold the above three widgets and act as the
      // popup's widget.
      VerticalPanel inner = new VerticalPanel();
      inner.add(helpText);

      // Create link to more information.  This would be cleaner if
      // GWT supported String.format.
      String referenceComponentsUrl = Ode.getSystemConfig().getReferenceComponentsUrl();
      String url = null;
      int version = -1;
      if (scd.getExternal()) {  // extensions will not have documentation hosted in ai2
        url = scd.getHelpUrl().isEmpty() ? null : scd.getHelpUrl();
        if (url != null) {
          if (!url.startsWith("http:") && !url.startsWith("https:")) {
            url = null;
          } else {
            // prevent embedded HTML tags, e.g. <script> in the URL
            url = url.replaceAll("<", "%3C")
                .replaceAll(">", "%3E")
                .replaceAll("\"", "%22");
          }
        }
        version = scd.getVersion();
      } else if (!Strings.isNullOrEmpty(referenceComponentsUrl)) {
        if (!referenceComponentsUrl.endsWith("/")) {
          referenceComponentsUrl += "/";
        }
        String categoryDocUrlString = scd.getCategoryDocUrlString();
        url = (categoryDocUrlString == null)
            ? referenceComponentsUrl + "index.html"
            : referenceComponentsUrl + categoryDocUrlString + ".html#" + scd.getName();
      }
      if (!scd.getVersionName().equals("")) {
        HTML html = new HTML("<b>" + MESSAGES.externalComponentVersion() + "</b> " +
            scd.getVersionName());
        html.setStyleName("ode-ComponentHelpPopup-Body");
        inner.add(html);
      } else if (version > 0) {
        HTML html = new HTML("<b>" + MESSAGES.externalComponentVersion() + "</b> " + version);
        html.setStyleName("ode-ComponentHelpPopup-Body");
        inner.add(html);
      }
      if (scd.getExternal() && scd.getDateBuilt() != null && !scd.getDateBuilt().equals("")) {
        String date = scd.getDateBuilt().split("T")[0];
        HTML dateCreatedHtml = new HTML("<b>" + MESSAGES.dateBuilt() + "</b> <time datetime=\"" + scd.getDateBuilt() + "\">" + date + "</time>");
        dateCreatedHtml.setStyleName("ode-ComponentHelpPopup-Body");
        inner.add(dateCreatedHtml);
      }
      if (url != null) {  // only show if there is a relevant URL
        HTML link = new HTML("<a href=\"" + url + "\" target=\"_blank\">" +
            MESSAGES.moreInformation() + "</a>");
        link.setStyleName("ode-ComponentHelpPopup-Link");
        inner.add(link);
      }
      if (scd.getExternal() && !"".equals(scd.getLicense())) {
        String license = scd.getLicense();
        HTML viewLicenseHTML = new HTML("<a href=\"" + license + "\" target=\"_blank\">" +
            MESSAGES.viewLicense() + "</a>");
        viewLicenseHTML.setStyleName("ode-ComponentHelpPopup-Link");
        inner.add(viewLicenseHTML);
      }

      add(inner);

      // When the panel is closed, save the time in milliseconds.
      // This will help us avoid immediately reopening it if the user
      // closed it by clicking on the question-mark icon.
      addCloseHandler(new CloseHandler<PopupPanel>() {
          @Override
          public void onClose(CloseEvent<PopupPanel> event) {
            lastClosureTime = System.currentTimeMillis();
          }
        });
      
      showRelativeTo(ComponentHelpWidget.this);
    }
  }

  public ComponentHelpWidget(final SimpleComponentDescriptor scd) {
    super(scd, imageResource);
  }

  @Override
  protected void handleClick() {
    final long MINIMUM_MS_BETWEEN_SHOWS = 250;  // .25 seconds

    if (System.currentTimeMillis() - lastClosureTime >=
        MINIMUM_MS_BETWEEN_SHOWS) {
      new ComponentHelpPopup();
    }
  }
}
