// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;

public class MockPlaneNode extends MockARNodeBase {
  public static final String TYPE = "PlaneNode";

  public MockPlaneNode(SimpleEditor editor) {
    super(editor, TYPE, images.planeNode());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(132, 132);
    svgpanel.setInnerSVG("<path d=\"M2 2 L30 2 L30 22 L2 22Z\" stroke-width=\"1\" stroke=\"black\" fill=\"red\" />");

    panel.setWidget(svgpanel);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_PLANE_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.AR_PLANE_PREFERRED_HEIGHT;
  }
}
