// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;

public class MockConeNode extends MockARNodeBase {
  public static final String TYPE = "ConeNode";

  public MockConeNode(SimpleEditor editor) {
    super(editor, TYPE, images.coneNode());
    final int diameter = ComponentConstants.AR_NODE_PREFERRED_WIDTH;
    // TODO: fix this
    final int center = ComponentConstants.AR_NODE_PREFERRED_WIDTH/2 + 1;

    // TODO: change this to be an image of a cone
    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(diameter + 2, diameter + 2);
    svgpanel.setInnerSVG("<defs>" +
      "<linearGradient id=\"gradcone\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">" +
        "<stop offset=\"0%\" style=\"stop-color:rgb(245,0,0);stop-opacity:1\" />" +
        "<stop offset=\"50%\" style=\"stop-color:rgb(255,200,200);stop-opacity:1\" />" +
        "<stop offset=\"100%\" style=\"stop-color:rgb(245,0,0);stop-opacity:1\" />" +
      "</linearGradient>" +
  "</defs>" +
  "<path id=\"lineA1\" d=\"M 2.5 31 q 15 7 30 0 l -15 -30 l -15 30\" stroke=\"black\" stroke-width=\"1\" fill=\"url(#gradcone)\" fill-rule=\"nonzero\" />");

    panel.setWidget(svgpanel);
  }
}
