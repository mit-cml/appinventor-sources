// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.youngandroid.ComponentList;
import com.google.appinventor.client.widgets.boxes.Box;

/**
 * Box implementation for component list.
 *
 */
public final class ComponentListBox extends Box {

  private static final ComponentListBox INSTANCE = new ComponentListBox();

  private final ComponentList componentList;

  /**
   * Returns the singleton components list box.
   *
   * @return  component list box
   */
  public static ComponentListBox getComponentListBox() {
    return INSTANCE;
  }

  private ComponentListBox() {
    super(MESSAGES.componentListBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    componentList = new ComponentList();
    setContent(componentList);
  }

  /**
   * Returns component list associated with components explorer box.
   *
   * @return  component list
   */
  public ComponentList getComponentList() {
     return componentList;
  }
}
