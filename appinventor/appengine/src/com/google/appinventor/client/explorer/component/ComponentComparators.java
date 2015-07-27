// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.component;

import com.google.appinventor.shared.rpc.component.Component;

import java.util.Comparator;

/**
 * Comparators for {@link Component}.
 *
 */
public final class ComponentComparators {
  private ComponentComparators() {
  }

  public static final Comparator<Component> COMPARE_BY_NAME_ASCENDING = new Comparator<Component>() {
    @Override
    public int compare(Component comp1, Component comp2) {
      String comp1Name = comp1.getName();
      String comp2Name = comp2.getName();
      return comp1Name.compareToIgnoreCase(comp2Name);
    }
  };

  public static final Comparator<Component> COMPARE_BY_NAME_DESCENDING = new Comparator<Component>() {
    @Override
    public int compare(Component comp1, Component comp2) {
      String comp1Name = comp1.getName();
      String comp2Name = comp2.getName();
      return comp2Name.compareToIgnoreCase(comp1Name);
    }
  };

  public static final Comparator<Component> COMPARE_BY_VERSION_ASCENDING = new Comparator<Component>() {
    @Override
    public int compare(Component comp1, Component comp2) {
      long version1 = comp1.getVersion();
      long version2 = comp2.getVersion();
      return Long.signum(version1 - version2);
    }
  };

  public static final Comparator<Component> COMPARE_BY_VERSION_DESCENDING = new Comparator<Component>() {
    @Override
    public int compare(Component comp1, Component comp2) {
      long version1 = comp1.getVersion();
      long version2 = comp2.getVersion();
      return Long.signum(version2 - version1);
    }
  };
}
