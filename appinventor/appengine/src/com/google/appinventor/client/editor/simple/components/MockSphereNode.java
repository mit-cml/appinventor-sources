// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ARComponentConstants;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.ARComponentConstants.*;

public class MockSphereNode extends MockARNodeBase {
  public static final String TYPE = "SphereNode";

  public MockSphereNode(SimpleEditor editor) {
    super(editor, TYPE, images.sphereNode());
    final int diameter = ComponentConstants.AR_NODE_PREFERRED_WIDTH;
    // TODO: fix this
    final int center = ComponentConstants.AR_NODE_PREFERRED_WIDTH/2 + 1;

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(diameter + 2, diameter + 2);
    // Plain Circle Code (Node Gradient)
    // svgpanel.setInnerSVG("<circle cx=\"16\" cy=\"16\" r= \"14\"  stroke-width=\"1\" stroke=\"black\" fill=\"red\" />");

    svgpanel.setInnerSVG(
    "<defs>" +
      "<radialGradient id=\"gradcirc\" cx=\"50%\" cy=\"50%\" r=\"50%\" fx=\"50%\" fy=\"50%\">" +
      "<stop offset=\"0%\" style=\"stop-color:rgb(255,0,0);stop-opacity:0.25\" />" +
      "<stop offset=\"100%\" style=\"stop-color:rgb(255,0,0);stop-opacity:1\" />" +
      "</radialGradient>" +
    "</defs>" +
    "<circle cx=\"16\" cy=\"16\" r= \"14\"  stroke-width=\"1\" stroke=\"black\" fill=\"url(#gradcirc)\" />");

    panel.setWidget(svgpanel);
  }

  @Override
  public void changeProperty(String propertyName, String newValue) {
    // Call the parent to handle the normal property change
    super.changeProperty(propertyName, newValue);

    // Handle Behavior → Mass dependency
    if ("DefaultBehavior".equals(propertyName)) {
      PhysicsSettingsObject physicsSettingsObject = ARComponentConstants.getSphereDefaultSettings(newValue);
      // This should update both the component state AND the property panel
      super.changeProperty("Mass",String.valueOf(physicsSettingsObject.mass));
      super.changeProperty("StaticFriction",String.valueOf(physicsSettingsObject.staticFriction));
      super.changeProperty("DynamicFriction",String.valueOf(physicsSettingsObject.dynamicFriction));
      super.changeProperty("DragSensitivity",String.valueOf(physicsSettingsObject.dragSensitivity));
      super.changeProperty("Restitution",String.valueOf(physicsSettingsObject.restitution));
    }
  }


  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_SPHERE_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.AR_SPHERE_PREFERRED_HEIGHT;
  }
}
