// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;

public class MockTorusNode extends MockARNodeBase {
  public static final String TYPE = "TorusNode";

  public MockTorusNode(SimpleEditor editor) {
    super(editor, TYPE, images.torusNode());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(132, 132);
    svgpanel.setInnerSVG("<defs>" +
      "<radialGradient id=\"gradtor\" cx=\"50%\" cy=\"50%\" r=\"50%\" fx=\"50%\" fy=\"50%\">" +
        "<stop offset=\"10%\" style=\"stop-color:rgb(255,255,255); stop-opacity:0\" />" +
        "<stop offset=\"100%\" style=\"stop-color:rgb(255,0,0); stop-opacity:1\" />" +
      "</radialGradient>" +
    "</defs>" +
    "<ellipse cx=\"20\" cy=\"14\" rx=\"18\" ry=\"12\" fill=\"url(#gradtor)\" stroke-width=\".5\" stroke=\"black\" />" +
    "<ellipse cx=\"20\" cy=\"14\" rx=\"10\" ry=\"5\" stroke-width=\"0.5\" stroke=\"black\" fill=\"white\" />");

    panel.setWidget(svgpanel);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_TORUS_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.AR_TORUS_PREFERRED_HEIGHT;
  }
}
