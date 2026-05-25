// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

/**
 * Abstract base for context modules that each contribute one section
 * of the LLM context.
 */
public abstract class ContextModule {

  /**
   * Build this module's context text.
   *
   * @param params per-request parameters (may be ignored by static modules)
   * @return the context text produced by this module
   */
  public abstract String build(ContextParams params);
}
