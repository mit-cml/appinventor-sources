// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.user;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;
import com.google.appinventor.shared.rpc.RestJSONObject;
import jsinterop.annotations.JsProperty;

/**
 * Data Transfer Object representing user data.
 *
 */
public class SplashConfig extends RestJSONObject implements IsSerializable, Serializable {
  // Unique identifier for the user

    // Needed to make the GWT Compiler happy
    protected SplashConfig() {
    }

    public SplashConfig(int version, int width, int height, String content) {
      this.version = version;
      this.width = width;
      this.height = height;
      this.content = content;
    }

    public SplashConfig(Object json) {
      super(json);
    }

    @JsProperty
    public int version;
    @JsProperty
    public int height;
    @JsProperty
    public int width;
    @JsProperty
    public String content;
}
