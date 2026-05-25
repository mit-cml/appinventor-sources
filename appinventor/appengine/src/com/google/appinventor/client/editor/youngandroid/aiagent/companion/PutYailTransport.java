// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.companion;

/**
 * Production {@link ReplTransport} that delegates to
 * {@code Blockly.ReplMgr.putYail} via JSNI.
 *
 * <p>putYail wraps the inner Scheme body in
 * {@code (begin (require ...) (process-repl-input "<blockid>" (begin <body>)))},
 * so this class only passes the raw body and a synthetic block object whose
 * {@code id} property matches the pending-promise map key.</p>
 */
public final class PutYailTransport implements ReplTransport {

  @Override
  public native void send(String schemeBody, String syntheticBlockId) /*-{
    var block = { id: syntheticBlockId };
    $wnd.Blockly.ReplMgr.putYail(schemeBody, block);
  }-*/;
}
