// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;

public class MockTubeNode extends MockARNodeBase {
  public static final String TYPE = "TubeNode";

  public MockTubeNode(SimpleEditor editor) {
    super(editor, TYPE, images.tubeNode());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(132, 132);
    svgpanel.setInnerSVG("<defs>" +
      "<linearGradient id=\"gradtube\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">" +
      "<stop offset=\"0%\" style=\"stop-color:rgb(255,0,0);stop-opacity:1\" />" +
      "<stop offset=\"50%\" style=\"stop-color:rgb(255,200,200);stop-opacity:1\" />" +
      "<stop offset=\"100%\" style=\"stop-color:rgb(255,0,0);stop-opacity:1\" />" +
      "</linearGradient>" +
    "</defs>" +
    "<ellipse cx=\"11\" cy=\"20\" rx=\"10\" ry=\"2\" stroke-width=\"1\" stroke=\"black\" fill=\"red\" />" +
    "<rect x=\"1\" y=\"3\" width=\"20\" height=\"16.85\" stroke-width=\"1\" stroke=\"black\" fill=\"url(#gradtube)\" />" +
    "<ellipse cx=\"11\" cy=\"3\" rx=\"10\" ry=\"2\" stroke-width=\"1\" stroke=\"black\" fill=\"rgb(255,102,102)\"  />" +
    "<ellipse cx=\"11\" cy=\"3\" rx=\"5\" ry=\"0.75\" stroke-width=\"0.25\" stroke=\"black\" fill=\"rgb(105,0,0)\"  />" +
    "<ellipse cx=\"11\" cy=\"20\" rx=\"10\" ry=\"1\" stroke-width=\"0\" stroke=\"black\" fill=\"url(#gradtube)\" />");

    panel.setWidget(svgpanel);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_TUBE_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.AR_TUBE_PREFERRED_HEIGHT;
  }
}
