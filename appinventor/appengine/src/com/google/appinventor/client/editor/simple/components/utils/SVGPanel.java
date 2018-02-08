// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;

public class SVGPanel extends ComplexPanel {
  private static final String SVG_NS = "http://www.w3.org/2000/svg";

  private static native Element createElementNS(final String ns, final String name)/*-{
    return document.createElementNS(ns, name);
  }-*/;

  public SVGPanel() {
    setElement(createElementNS(SVG_NS, "svg"));
  }

  public native void setInnerSVG(final String svg)/*-{
    var el = this.@com.google.gwt.user.client.ui.UIObject::getElement()();
    el.innerHTML = svg;
  }-*/;

  public native String getClassName()/*-{
    var el = this.@com.google.gwt.user.client.ui.UIObject::getElement()();
    return el.style.baseVal;
  }-*/;
}
