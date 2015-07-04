// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.boxes;

import com.google.appinventor.client.widgets.boxes.Box;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Superclass for registries of boxes for layouts.
 *
 */
public abstract class BoxRegistry {

  // Mapping of box types to boxes
  private final Map<String, Box> boxMap;

  /**
   * Creates a new box registry.
   */
  public BoxRegistry() {
    boxMap = new HashMap<String, Box>();
  }

  /**
   * Registers a box.
   */
  protected final void register(Box box) {
    boxMap.put(box.getClass().getName(), box);
  }

  /**
   * Returns the requested box.
   *
   * @param type  box type
   * @return  requested box
   */
  public final Box getBox(String type) {
    return boxMap.get(type);
  }

  /**
   * Returns a set of all registered box types.
   *
   * @return set of registered box types
   */
  public final Set<String> getBoxTypes() {
    return boxMap.keySet();
  }
}
