// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

/**
 * Loads few-shot examples from {@code few_shot_examples.json}.
 * Cached on first call.
 */
public class ExamplesModule extends ContextModule {

  private static volatile String cached;

  @Override
  public String build(ContextParams params) {
    if (cached == null) {
      cached = ContextUtils.loadResource("few_shot_examples.json");
    }
    return cached;
  }
}
