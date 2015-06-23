// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.widgets.boxes.Box;

/**
 * Box implementation for source structure explorer.
 *
 */
public final class SourceStructureBox extends Box {
  // Singleton source structure explorer box instance
  private static final SourceStructureBox INSTANCE = new SourceStructureBox();

  // Source structure explorer
  private final SourceStructureExplorer sourceStructureExplorer;

  /**
   * Return the singleton source structure explorer box.
   *
   * @return  source structure explorer box
   */
  public static SourceStructureBox getSourceStructureBox() {
    return INSTANCE;
  }

  /**
   * Creates new source structure explorer box.
   */
  private SourceStructureBox() {
    super(MESSAGES.sourceStructureBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    sourceStructureExplorer = new SourceStructureExplorer();

    setContent(sourceStructureExplorer);
  }

  /**
   * Returns source structure explorer associated with box.
   *
   * @return  source structure explorer
   */
  public SourceStructureExplorer getSourceStructureExplorer() {
    return sourceStructureExplorer;
  }
}
