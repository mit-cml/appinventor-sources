// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.companion;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Client-side singleton that resolves runtime-read tool calls by sending
 * Scheme expressions through the existing {@code Blockly.ReplMgr.putYail}
 * pipeline with synthetic {@code ai-read-<hex>} blockids, and matches replies
 * via a pending-promise map.
 *
 * <h3>Budget</h3>
 * <ul>
 *   <li>10 reads per AI turn (reset by {@link #resetTurnBudget()})</li>
 *   <li>30 reads per rolling 60-second window</li>
 *   <li>5-second per-read timeout</li>
 * </ul>
 *
 * <h3>Singleton discipline</h3>
 * Production code calls {@link #getInstance()} which lazily creates the
 * singleton with a {@link PutYailTransport}.  Tests construct their own
 * instance via the package-private constructor and should NOT call
 * {@code getInstance()}.
 *
 * <h3>Reply routing</h3>
 * When {@code processRetvals} in replmgr.js sees a blockid starting with
 * {@code ai-read-}, it calls {@code top.BlocklyPanel_resolveCompanionRead}
 * which delegates to {@link #resolvePendingGlobal(String, String, String)}.
 * That method routes the reply to the singleton's pending-promise map.
 */
public final class CompanionBridge {

  // ---- Public callback interface ----

  /** Callback returned to callers of read operations. */
  public interface Callback {
    /** Called when the Companion responded successfully. */
    void onSuccess(String value);
    /** Called on timeout, budget exhaustion, validation error, or Companion error. */
    void onFailure(String error);
  }

  // ---- Budget / timeout constants ----

  private static final int PER_TURN_CAP = 10;
  private static final int PER_MINUTE_CAP = 30;
  private static final int TIMEOUT_MS = 5000;
  private static final long WINDOW_MS = 60_000L;

  // ---- Block-id constants ----

  static final String BLOCK_ID_PREFIX = "ai-read-";

  // ---- Identifier validation — matches CompanionReadValidator (Task 10) ----

  private static final RegExp IDENTIFIER = RegExp.compile("^[A-Za-z_][A-Za-z0-9_]*$");

  // ---- Singleton ----

  private static CompanionBridge instance;

  /**
   * Returns the production singleton, creating it on first call with a real
   * {@link PutYailTransport} and {@link SystemClock}.
   *
   * <p>Tests must NOT call this method — they construct their own instance.</p>
   */
  public static CompanionBridge getInstance() {
    if (instance == null) {
      instance = new CompanionBridge(new PutYailTransport(), new SystemClock());
    }
    return instance;
  }

  /**
   * JSNI-entry static dispatcher called by
   * {@code BlocklyPanel.resolveCompanionRead} (exported to
   * {@code top.BlocklyPanel_resolveCompanionRead} by BlocklyPanel).
   *
   * <p>If the singleton has not yet been created (companion read arrived
   * before any AI turn started) the call is silently dropped — there can
   * be no pending promise to resolve.</p>
   *
   * @param blockId the {@code ai-read-<hex>} blockid from the Scheme reply
   * @param status  {@code "OK"} on success, anything else on error
   * @param value   the return value string, or an error message
   */
  public static void resolvePendingGlobal(String blockId, String status, String value) {
    if (instance != null) {
      instance.resolvePending(blockId, status, value);
    }
  }

  // ---- Instance state ----

  private final ReplTransport transport;
  private final Clock clock;

  /**
   * Maps synthetic blockId → pending read awaiting a Companion reply.
   * Reads are <strong>serialized</strong>: at most one entry at a time.
   *
   * <p>The HTTP/ADB transport's {@code pollphone} loop chunks every item
   * in {@code rs.phoneState.phoneQueue} into a single Scheme
   * {@code (begin ...)} block with one blockid {@code "-2"} and only
   * returns the last expression's value. By keeping exactly one read in
   * flight, {@code phoneQueue} never has more than one AI-read item
   * waiting, so no chunking can discard intermediate results.</p>
   */
  private final Map<String, PendingRead> pending = new HashMap<String, PendingRead>();

  /**
   * Reads waiting behind the currently-in-flight one. Drained in FIFO
   * order whenever a read resolves (success, failure, or timeout).
   */
  private final LinkedList<WaitingRead> waiting = new LinkedList<WaitingRead>();

  /**
   * Timestamps (ms) of reads sent within the rolling 60-second window.
   * Old entries are pruned lazily in {@link #checkBudget}.
   * Using {@link LinkedList} because GWT's JRE emulation does not include
   * {@code java.util.ArrayDeque}.
   */
  private final LinkedList<Long> windowTimestamps = new LinkedList<Long>();

  /** Number of reads dispatched in the current AI turn. */
  private int turnCount = 0;

  // ---- Package-private constructor for tests ----

  /**
   * Creates a bridge with the given transport and clock.
   * Package-private so tests can inject fakes.
   */
  CompanionBridge(ReplTransport transport, Clock clock) {
    this.transport = transport;
    this.clock = clock;
  }

  // ---- Public API ----

  /**
   * Resets the per-turn read counter.  Must be called by the orchestrator at
   * the start of each AI turn so the 10-reads-per-turn cap is per-turn, not
   * cumulative.
   */
  public void resetTurnBudget() {
    turnCount = 0;
  }

  /**
   * Reads a component property at runtime via the Companion.
   *
   * <p>Generates a Scheme expression of the form:
   * {@code (get-property 'componentName 'propertyName)} and sends it through
   * {@link PutYailTransport}. The {@code process-repl-input} macro on the
   * device wraps the result in {@code get-display-representation} itself
   * (see {@code runtime.scm} {@code in-ui}) — calling it here too would
   * double-wrap strings (e.g. {@code trrg} → {@code ""trrg""}).</p>
   *
   * @param componentName the App Inventor component instance name (e.g. {@code Button1})
   * @param propertyName  the property name (e.g. {@code Text})
   * @param cb            callback invoked on success or failure
   */
  public void readComponentProperty(String componentName, String propertyName, Callback cb) {
    if (!validateIdentifier(componentName, "component_name", cb)) return;
    if (!validateIdentifier(propertyName, "property_name", cb)) return;
    if (!checkBudget(cb)) return;
    String blockId = generateBlockId();
    String scheme = "(get-property '" + componentName + " '" + propertyName + ")";
    dispatch(blockId, scheme, cb);
  }

  /**
   * Reads a global variable at runtime via the Companion.
   *
   * <p>Generates a Scheme expression of the form:
   * {@code (get-var g$variableName)} and sends it through
   * {@link PutYailTransport}. The {@code process-repl-input} macro on the
   * device wraps the result in {@code get-display-representation} itself,
   * so we do not call it here (see {@link #readComponentProperty}).</p>
   *
   * @param variableName the App Inventor global variable name (without {@code g$} prefix)
   * @param cb           callback invoked on success or failure
   */
  public void readVariable(String variableName, Callback cb) {
    if (!validateIdentifier(variableName, "variable_name", cb)) return;
    if (!checkBudget(cb)) return;
    String blockId = generateBlockId();
    String scheme = "(get-var g$" + variableName + ")";
    dispatch(blockId, scheme, cb);
  }

  /**
   * Resolves a pending read by blockId.  Called from
   * {@link #resolvePendingGlobal} (which is called from replmgr.js via
   * {@code BlocklyPanel_resolveCompanionRead}).
   *
   * <p>If no pending read exists for {@code blockId} (e.g. it timed out),
   * the call is silently dropped.</p>
   *
   * @param blockId the {@code ai-read-<hex>} blockid
   * @param status  {@code "OK"} on success
   * @param value   the return value, or error message
   */
  public void resolvePending(String blockId, String status, String value) {
    PendingRead read = pending.remove(blockId);
    if (read == null) {
      // Already timed out or unknown id — nothing to do.
      return;
    }
    read.cancelTimeout();
    if ("OK".equals(status)) {
      read.callback.onSuccess(value == null ? "" : value);
    } else {
      // Refund budget — the device rejected the read, so it shouldn't
      // count against the LLM's per-turn quota.
      refundBudget();
      read.callback.onFailure(value == null ? "Unknown Companion error" : value);
    }
    pumpNext();
  }

  // ---- Private helpers ----

  private boolean validateIdentifier(String s, String field, Callback cb) {
    if (s == null || !IDENTIFIER.test(s)) {
      cb.onFailure("Invalid " + field + ": '" + s + "'");
      return false;
    }
    return true;
  }

  /**
   * Checks both the per-turn cap and the rolling per-minute cap.
   * If both pass, increments both counters and returns {@code true}.
   */
  private boolean checkBudget(Callback cb) {
    if (turnCount >= PER_TURN_CAP) {
      cb.onFailure("Runtime read budget exceeded for this turn (max "
          + PER_TURN_CAP + ").");
      return false;
    }
    long now = clock.now();
    // Prune timestamps older than the 60-second window.
    while (!windowTimestamps.isEmpty() && now - windowTimestamps.getFirst() > WINDOW_MS) {
      windowTimestamps.removeFirst();
    }
    if (windowTimestamps.size() >= PER_MINUTE_CAP) {
      cb.onFailure("Runtime read rate limit exceeded (" + PER_MINUTE_CAP
          + " reads per minute).");
      return false;
    }
    turnCount++;
    windowTimestamps.addLast(now);
    return true;
  }

  private String generateBlockId() {
    return BLOCK_ID_PREFIX + randomHex(16);
  }

  /**
   * Generates a random hex string of the requested length.
   * Uses {@code Math.random()} via JSNI — GWT does not provide
   * {@code java.util.UUID} in its JRE emulation.
   */
  private native String randomHex(int len) /*-{
    var s = '';
    while (s.length < len) {
      s += Math.floor(Math.random() * 0x100000000).toString(16);
    }
    return s.substring(0, len);
  }-*/;

  /**
   * Dispatches a read or enqueues it behind the currently-in-flight one.
   *
   * <p>Serialization is required because the HTTP/ADB transport chunks
   * multiple queued items into a single Scheme block with blockid
   * {@code "-2"}, losing per-item return values. Keeping exactly one
   * AI-read in {@code phoneQueue} at a time prevents the chunker from
   * collapsing our reads.</p>
   */
  private void dispatch(String blockId, String schemeBody, Callback cb) {
    if (pending.isEmpty()) {
      sendNow(blockId, schemeBody, cb);
    } else {
      waiting.addLast(new WaitingRead(blockId, schemeBody, cb));
    }
  }

  /** Performs the actual transport send and starts the timeout timer. */
  private void sendNow(String blockId, String schemeBody, Callback cb) {
    PendingRead read = new PendingRead(cb);
    pending.put(blockId, read);
    read.scheduleTimeout(this, blockId);
    transport.send(schemeBody, blockId);
  }

  /**
   * Pops the next waiting read (if any) and sends it. Called from
   * {@link #resolvePending} and {@link #timeoutRead} after the current
   * in-flight read completes.
   */
  private void pumpNext() {
    if (!pending.isEmpty()) {
      // A send for the just-resolved id could be re-entered through the
      // transport under unusual conditions — bail out rather than
      // dispatch two reads at once.
      return;
    }
    WaitingRead next = waiting.pollFirst();
    if (next != null) {
      sendNow(next.blockId, next.schemeBody, next.callback);
    }
  }

  /**
   * Called by the timeout {@link Timer} when it fires for {@code blockId}.
   * Removes the pending entry, refunds the budget (no response = no load
   * on the device), invokes the failure callback, then pumps the next
   * waiting read.
   */
  void timeoutRead(String blockId) {
    PendingRead read = pending.remove(blockId);
    if (read == null) {
      // Already resolved — nothing to do.
      return;
    }
    refundBudget();
    read.callback.onFailure("Companion did not respond within 5 seconds.");
    pumpNext();
  }

  /**
   * Undoes a previous {@link #checkBudget} increment. Used when a read
   * fails (timeout or Companion NOK) so the LLM can retry without being
   * rate-limited by failed attempts.
   *
   * <p>Only the per-turn counter and the most recent rolling-window
   * timestamp are decremented. If the window has already pruned the
   * relevant entry, the pop is a no-op.</p>
   */
  private void refundBudget() {
    if (turnCount > 0) {
      turnCount--;
    }
    if (!windowTimestamps.isEmpty()) {
      windowTimestamps.removeLast();
    }
  }

  // ---- Inner types ----

  /**
   * Abstraction over {@code System.currentTimeMillis()} for testability.
   * GWT emulates {@code System.currentTimeMillis()}.
   */
  interface Clock {
    long now();
  }

  /** Production clock backed by {@code System.currentTimeMillis()}. */
  static final class SystemClock implements Clock {
    @Override
    public long now() {
      return System.currentTimeMillis();
    }
  }

  /**
   * A read waiting behind the currently-in-flight one. Holds the params
   * needed by {@link #sendNow} so the dispatch is just a pop-and-send.
   */
  private static final class WaitingRead {
    final String blockId;
    final String schemeBody;
    final Callback callback;

    WaitingRead(String blockId, String schemeBody, Callback callback) {
      this.blockId = blockId;
      this.schemeBody = schemeBody;
      this.callback = callback;
    }
  }

  /**
   * A read that has been sent to the Companion and is awaiting a reply.
   */
  private static final class PendingRead {
    final Callback callback;
    /** GWT Timer that fires {@link CompanionBridge#timeoutRead} if no reply arrives. */
    Timer timer;

    PendingRead(Callback cb) {
      this.callback = cb;
    }

    void scheduleTimeout(final CompanionBridge bridge, final String blockId) {
      this.timer = new Timer() {
        @Override
        public void run() {
          bridge.timeoutRead(blockId);
        }
      };
      timer.schedule(TIMEOUT_MS);
    }

    void cancelTimeout() {
      if (timer != null) {
        timer.cancel();
        timer = null;
      }
    }
  }
}
