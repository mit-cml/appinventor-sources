// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks what still has to reach the server and what is already on its way.
 *
 * <p>Two saves of one thing must not run at the same time. They are separate requests, they
 * can land in either order, and the older one landing last would undo the newer one. So
 * something that is already on its way is held back rather than sent again, and it is sent by
 * the next save instead.
 *
 * <p>Holding something back means a save can finish while work is still waiting, which is why
 * {@link #heldBack()} exists. A caller that stops its timer when it starts saving has to look
 * at that and start the timer again, or the held back work would sit there with nothing
 * coming for it.
 *
 * <p>This holds no reference to a browser, a timer, or a request, so what it decides can be
 * read and tested on its own. The editor manager owns the requests and asks this what to send.
 *
 * @param <T> what is being saved, a file editor or a project settings
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
@VisibleForTesting
class PendingSaves<T> {

  /** How two saves of one thing are recognised as being of one thing. */
  interface Key<T> {
    Object of(T item);
  }

  private final Key<T> key;

  /** Waiting to be sent, in the order it became dirty. */
  private final Map<Object, T> waiting = new LinkedHashMap<Object, T>();

  /** Sent and not answered yet. */
  private final Map<Object, T> inFlight = new LinkedHashMap<Object, T>();

  PendingSaves(Key<T> key) {
    this.key = key;
  }

  /** Notes that this needs saving, replacing any earlier note of the same thing. */
  void add(T item) {
    waiting.put(key.of(item), item);
  }

  /** Forgets about this entirely, for something that is going away rather than being saved. */
  void discard(T item) {
    Object id = key.of(item);
    waiting.remove(id);
    inFlight.remove(id);
  }

  /**
   * Takes everything that can be sent now and records it as on its way.
   *
   * @return what to send, which is everything waiting that is not already on its way
   */
  List<T> take() {
    List<T> sending = new ArrayList<T>();
    Iterator<Map.Entry<Object, T>> entries = waiting.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry<Object, T> entry = entries.next();
      if (inFlight.containsKey(entry.getKey())) {
        continue;
      }
      inFlight.put(entry.getKey(), entry.getValue());
      sending.add(entry.getValue());
      entries.remove();
    }
    return sending;
  }

  /** Whether anything was held back because it is already on its way. */
  boolean heldBack() {
    return !waiting.isEmpty();
  }

  /**
   * Takes everything waiting, even what an unanswered save would normally hold back.
   *
   * <p>Only for a window that is closing. No later save could ever come for held back work
   * there, so holding it would drop it, and sending it beside the unanswered save is the
   * lesser risk. The newer send takes the slot, and the identity checks in {@link #answered}
   * and {@link #failed} keep a late answer from the older save from clearing it.
   */
  List<T> takeEvenInFlight() {
    List<T> sending = new ArrayList<T>(waiting.values());
    for (Map.Entry<Object, T> entry : waiting.entrySet()) {
      inFlight.put(entry.getKey(), entry.getValue());
    }
    waiting.clear();
    return sending;
  }

  /**
   * Notes that a save answered and did what it was asked.
   *
   * <p>Only the save that owns the slot may clear it. After a discard, a newer save of the
   * same thing can be on its way, and a late answer from the discarded one must not take the
   * newer save's place out from under it.
   */
  void answered(T item) {
    Object id = key.of(item);
    if (inFlight.get(id) == item) {
      inFlight.remove(id);
    }
  }

  /**
   * Notes that a save answered and did not do what it was asked, so it waits to be sent again.
   * Anything that became dirty while it was on its way is newer and is left as it is, and a
   * late answer from a save that was discarded changes nothing.
   */
  void failed(T item) {
    Object id = key.of(item);
    if (inFlight.get(id) != item) {
      return;
    }
    inFlight.remove(id);
    if (!waiting.containsKey(id)) {
      waiting.put(id, item);
    }
  }

  /**
   * Wraps a completion so the save it belongs to is marked answered before anything else runs.
   *
   * <p>A save that is never marked answered stays on its way forever, and everything that asks
   * whether work is outstanding would wait on it for good. Wrapping the completion here rather
   * than at each call site keeps that promise in one tested place.
   */
  static <T> com.google.gwt.user.client.Command answering(final PendingSaves<T> queue,
      final T item, final com.google.gwt.user.client.Command then) {
    return new com.google.gwt.user.client.Command() {
      @Override
      public void execute() {
        queue.answered(item);
        then.execute();
      }
    };
  }

  /** Whether anything at all is unfinished, whether waiting or already on its way. */
  boolean isEmpty() {
    return waiting.isEmpty() && inFlight.isEmpty();
  }

  /** How many are on their way, which is how many answers a caller is waiting for. */
  @VisibleForTesting
  int inFlightCount() {
    return inFlight.size();
  }
}
