// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Defines a widget that has the appearance of a red close button.
 * The Widget is clicked to delete the associated component
 */
public class ComponentRemoveWidget extends Image {
  private static ImageResource imageResource = null;

  private static Ode ode = Ode.getInstance();

  private final SimpleComponentDescriptor scd;

  public ComponentRemoveWidget(SimpleComponentDescriptor simpleComponentDescriptor) {
    if (imageResource == null) {
      Images images = Ode.getImageBundle();
      imageResource = images.deleteComponent();
    }
    this.scd = simpleComponentDescriptor;
    AbstractImagePrototype.create(imageResource).applyTo(this);
    addClickListener(new ClickListener() {

        @Override
        public void onClick(Widget widget) {
          if (Window.confirm(MESSAGES.reallyRemoveComponent())) {
            long projectId = ode.getCurrentYoungAndroidProjectId();
            YaProjectEditor projectEditor = (YaProjectEditor) ode.getEditorManager().getOpenProjectEditor(projectId);
            SimpleComponentDatabase componentDatabase = SimpleComponentDatabase.getInstance();
            componentDatabase.addComponentDatabaseListener(projectEditor);
            componentDatabase.removeComponent(scd.getName());
          }
        }
      });
  }
}
