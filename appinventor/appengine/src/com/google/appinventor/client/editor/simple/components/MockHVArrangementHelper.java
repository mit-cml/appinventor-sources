// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Helper class for MockHVArrangement to get around a restriction in the
 * Java compiler on calling super
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class MockHVArrangementHelper {

  // This class will be called each time we create an arrangement.  We add synchronization
  // now to protect saveLayout, even though there should not be more than one thread creating
  // layouts.  But who knows what we might do in the future.


  private static MockHVLayout saveLayout;

  public static synchronized MockHVLayout makeLayout(int orientation) {
      saveLayout = new MockHVLayout(orientation);
      return saveLayout;
  }

  public static synchronized MockHVLayout getLayout() {
    return saveLayout;
  }

}

