// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.resources.client.ImageResource;

public abstract class MockPolygonBase extends MockMapFeatureBaseWithFill {

  public MockPolygonBase(SimpleEditor editor, String type, ImageResource image) {
    super(editor, type, image);
  }

}
