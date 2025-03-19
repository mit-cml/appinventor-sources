// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.palette.AbstractPalettePanel;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.wizards.ComponentImportWizard;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Panel showing Simple components which can be dropped onto the Young Android
 * visual designer panel.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidPalettePanel extends AbstractPalettePanel<SimpleComponentDatabase, YaFormEditor> {

  /**
   * Creates a new component palette panel.
   *
   * @param editor parent editor of this panel
   */
  public YoungAndroidPalettePanel(YaFormEditor editor) {
    super(editor, new YoungAndroidComponentFactory(editor), ComponentCategory.USERINTERFACE, ComponentCategory.LAYOUT, ComponentCategory.MEDIA,
        ComponentCategory.ANIMATION, ComponentCategory.MAPS, ComponentCategory.SENSORS, ComponentCategory.SOCIAL, ComponentCategory.STORAGE,
        ComponentCategory.CONNECTIVITY, ComponentCategory.LEGOMINDSTORMS, ComponentCategory.EXPERIMENTAL, ComponentCategory.EXTENSION, ComponentCategory.INTERNAL);

    // If a category has a palette helper, add it to the paletteHelpers map here.
    paletteHelpers.put(ComponentCategory.LEGOMINDSTORMS, new LegoPaletteHelper());
  }

  @Override
  public SimplePalettePanel copy() {
    YoungAndroidPalettePanel copy = new YoungAndroidPalettePanel(this.editor);
    copy.setSize("100%", "100%");
    return copy;
  }

  @Override
  public VerticalPanel addComponentCategory(ComponentCategory category) {
    VerticalPanel panel = super.addComponentCategory(category);
    if (category == ComponentCategory.EXTENSION) {
      initExtensionPanel(panel);
    }
    return panel;
  }

  private void initExtensionPanel(VerticalPanel panel) {
    Anchor addComponentAnchor = new Anchor(MESSAGES.importExtensionMenuItem());
    addComponentAnchor.setStylePrimaryName("ode-ExtensionAnchor");
    addComponentAnchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ComponentImportWizard().center();
      }
    });

    panel.add(addComponentAnchor);
    panel.setCellHorizontalAlignment(addComponentAnchor, HasHorizontalAlignment.ALIGN_CENTER);
  }
}
