// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.SourceStructureExplorer;

public interface ISourceStructureBox {
  /**
   * Method to retrieve the rendered source structure explorer from the "child" boxes.
   * @return SourceStructureExplorer
   */
  SourceStructureExplorer getSourceStructureExplorer();

  /**
   * Method render the "child" boxes.
   */
  void show(MockForm form);
}
