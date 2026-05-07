// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.gwt.dom.client.Element;

/**
 * FocusImplSafari extends the GWT class of the same name in order to pass
 * the preventScroll option when an element is focused programmatically.
 */
public class FocusImplSafari extends com.google.gwt.user.client.ui.impl.FocusImplSafari {
  @Override
  public native void focus(Element elem)/*-{
    $wnd.setTimeout(function() {
      elem.focus({'preventScroll': true});
    }, 0);
  }-*/;
}
