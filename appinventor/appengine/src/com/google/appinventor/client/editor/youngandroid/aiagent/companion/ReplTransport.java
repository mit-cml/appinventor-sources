// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.companion;

/**
 * Seam over Blockly.ReplMgr.putYail that lets tests substitute a fake.
 *
 * <p>The production implementation ({@link PutYailTransport}) calls
 * {@code Blockly.ReplMgr.putYail(schemeBody, {id: syntheticBlockId})} via JSNI.
 * The transport is responsible for wrapping the body in
 * {@code process-repl-input} — that is done by putYail itself, not here.
 */
public interface ReplTransport {
  /**
   * Send the given inner Scheme body with the given synthetic blockid.
   *
   * @param schemeBody     the inner Scheme expression (e.g.
   *                       {@code (get-display-representation (get-property 'B 'T))})
   * @param syntheticBlockId the {@code ai-read-<hex>} blockid that will be used by
   *                         RetValManager to route the reply back
   */
  void send(String schemeBody, String syntheticBlockId);
}
