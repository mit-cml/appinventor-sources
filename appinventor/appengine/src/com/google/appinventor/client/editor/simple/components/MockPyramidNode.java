// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;

public class MockPyramidNode extends MockARNodeBase {
  public static final String TYPE = "PyramidNode";

  public MockPyramidNode(SimpleEditor editor) {
    super(editor, TYPE, images.pyramidNode());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(132, 132);
    svgpanel.setInnerSVG("<polygon points=\"23,2 43,25 23,35\" stroke-width=\"1\" stroke=\"black\" fill=\"rgb(255,82,82)\" />" +
    "<polygon points=\"23,2 3,25 23,35\" stroke-width=\"1\" stroke=\"black\" fill=\"red\" />");

    panel.setWidget(svgpanel);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_PYRAMID_PREFERRED_WIDTH;
  }
}
