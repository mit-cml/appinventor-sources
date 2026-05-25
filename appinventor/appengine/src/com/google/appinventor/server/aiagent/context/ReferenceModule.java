// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

/**
 * Loads the static App Inventor reference from
 * {@code appinventor_reference.md}. Cached on first call.
 */
public class ReferenceModule extends ContextModule {

  private static volatile String cached;

  @Override
  public String build(ContextParams params) {
    if (cached == null) {
      cached = ContextUtils.loadResource("appinventor_reference.md");
    }
    return cached;
  }
}
