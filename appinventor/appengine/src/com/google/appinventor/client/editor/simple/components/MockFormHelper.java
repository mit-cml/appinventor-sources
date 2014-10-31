// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Helper class for MockForm to get around a restriction in the
 * Java compiler on calling super
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class MockFormHelper {

  // This class will be called each time we create a form.  We add synchronization now to protect
  // saveLayout, even though there should not be more than one thread creating layouts.
  // but who knows what we might do in the future.


  private static MockFormLayout saveLayout;

  public static synchronized MockFormLayout makeLayout() {
    saveLayout = new MockFormLayout();
    return saveLayout;
  }

  public static synchronized MockFormLayout getLayout() {
    return saveLayout;
  }

}

