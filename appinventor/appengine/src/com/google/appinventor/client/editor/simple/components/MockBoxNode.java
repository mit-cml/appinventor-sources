// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;

public class MockBoxNode extends MockARNodeBase {
  public static final String TYPE = "BoxNode";

  public MockBoxNode(SimpleEditor editor) {
    super(editor, TYPE, images.boxNode());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(132, 132);
    svgpanel.setInnerSVG("<path  fill =\"rgb(255,82,82)\" fill-rule=\"nonzero\" d=\"M 11 2" +
    " h 20 v 20 l -10 10 h -20 v -20 l 10 -10 M 1 12 h 20 v 20 M 31 2 l -10 10\"" +
    " stroke=\"black\" stroke-width=\"1\"/>");

    panel.setWidget(svgpanel);
  }
}
