// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.gwt.dom.client.Element;

/**
 * FocusImplStandard extends the GWT class of the same name in order to pass
 * the preventScroll option when an element is focused programmatically.
 */
public class FocusImplStandard extends com.google.gwt.user.client.ui.impl.FocusImplStandard {
  @Override
  public native void focus(Element elem)/*-{
    elem.focus({'preventScroll': true});
  }-*/;
}
