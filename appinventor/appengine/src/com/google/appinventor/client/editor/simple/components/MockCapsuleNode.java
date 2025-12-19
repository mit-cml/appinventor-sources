// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;

public class MockCapsuleNode extends MockARNodeBase {
  public static final String TYPE = "CapsuleNode";

  public MockCapsuleNode(SimpleEditor editor) {
    super(editor, TYPE, images.capsuleNode());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(132, 132);
    svgpanel.setInnerSVG("<defs>" +
      "<linearGradient id=\"gradcap\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">" +
        "<stop offset=\"0%\" style=\"stop-color:rgb(255,0,0);stop-opacity:1\" />" +
        "<stop offset=\"50%\" style=\"stop-color:rgb(255,170,170);stop-opacity:1\" />" +
        "<stop offset=\"100%\" style=\"stop-color:rgb(255,0,0);stop-opacity:1\" />" +
      "</linearGradient>" +
  "</defs>" +
  "<rect x=\"5\" y=\"2.5\" width=\"20\" height=\"30\" rx=\"45\" ry=\"10\" fill=\"url(#gradcap)\" stroke-width=\"1\" stroke=\"black\" />");

    panel.setWidget(svgpanel);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_CAPSULE_PREFERRED_WIDTH;
  }
}
