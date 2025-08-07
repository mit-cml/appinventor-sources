// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.explorer.SourceStructureExplorer;

/**
 * Box implementation for source structure explorer (classic style).
 */
public final class SourceStructureBoxClassic implements ISourceStructureBox {
  private final SourceStructureExplorer sourceStructureExplorer = new SourceStructureExplorer();

  public SourceStructureBoxClassic() {
    super();
  }

  public void show(DesignerRootComponent root) {
    sourceStructureExplorer.updateTree(root.buildComponentsTree(),
            root.getLastSelectedComponent().getSourceStructureExplorerItem());
    sourceStructureExplorer.setVisible(true);
  }

  public SourceStructureExplorer getSourceStructureExplorer() {
    return sourceStructureExplorer;
  }
}
