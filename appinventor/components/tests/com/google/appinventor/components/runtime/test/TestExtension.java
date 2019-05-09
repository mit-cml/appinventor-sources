// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.test;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Form;

/**
 * TestExtension is a barebones extension class that can be used for testing
 * different features of the App Inventor extensions mechanism in the REPL.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@SimpleObject(external = true)
public class TestExtension extends AndroidNonvisibleComponent {
  public TestExtension(Form form) {
    super(form);
  }
}
