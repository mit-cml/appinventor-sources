/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.sticky.client.model;

import com.google.appengine.demos.sticky.client.model.Model.StatusObserver;
import com.google.appengine.demos.sticky.client.model.Service.AccessDeniedException;
import com.google.appengine.demos.sticky.client.model.Service.GetNotesResult;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.HashMap;
import java.util.Map;

/**
 * Controls all aspects of loading the set of {@link Note}s associated with the
 * selected {@link Surface}. This class takes care of performing (and possibly
 * retrying) a query for the initial set of Notes and then continues polling the
 * server for updates.
 *
 */
class NoteLoader {

  /**
   * Controls the initial load of notes from the server and will retry on
   * failure.
   */
  private class InitialLoader extends RetryTimer implements
      AsyncCallback<Service.GetNotesResult> {

    private final int id;

    public InitialLoader() {
      id = activeId;
      start();
    }

    public void onFailure(Throwable caught) {
      model.onServerFailed(caught instanceof Service.AccessDeniedException);
      if (isActiveSession(id)) {
        retryLater();
      }
    }

    public void onSuccess(GetNotesResult result) {
      model.onServerSucceeded();
      if (!isActiveSession(id)) {
        return;
      }

      model.getStatusObserver().onTaskFinished();

      timestamp = result.getTimestamp();
      final Note[] notes = result.getNotes();
      for (int i = 0, n = notes.length; i < n; ++i) {
        final Note note = notes[i];
        note.initialize(model);
        notesCache.put(note.getKey(), note);
      }
      model.notifySurfaceNotesReceived(notes);

      startPolling();
    }

    private void start() {
      model.getService().getNotes(model.getSelectedSurface().getKey(), null,
          this);
    }

    @Override
    protected void retry() {
      if (isActiveSession(id)) {
        start();
      }
    }
  }

  /**
   * Controls the polling calls to the server.
   */
  private class Poller extends Timer implements
      AsyncCallback<Service.GetNotesResult> {
    private final int id;

    public Poller() {
      this.id = activeId;
      scheduleRepeating(interval);
    }

    public void onFailure(Throwable caught) {
      model.onServerFailed(caught instanceof AccessDeniedException);

      if (!isActiveSession(id)) {
        cancel();
      }
    }

    public void onSuccess(GetNotesResult result) {
      model.onServerSucceeded();

      if (!isActiveSession(id)) {
        cancel();
        return;
      }

      timestamp = result.getTimestamp();

      final Note[] notes = result.getNotes();
      for (int i = 0, n = notes.length; i < n; ++i) {
        final Note note = notes[i];
        final Note existing = notesCache.get(note.getKey());
        if (existing == null) {
          note.initialize(model);
          notesCache.put(note.getKey(), note);
          model.notifyNoteCreated(note);
        } else {
          existing.update(note);
        }
      }
    }

    @Override
    public void run() {
      if (!isActiveSession(id)) {
        cancel();
        return;
      }
      final Surface surface = model.getSelectedSurface();
      if (surface.hasKey()) {
        model.getService().getNotes(model.getSelectedSurface().getKey(),
            timestamp, this);
      }
    }
  }

  private final Model model;

  private final int interval;

  private String timestamp;

  private int activeId;

  private Map<String, Note> notesCache = new HashMap<String, Note>();

  /**
   * Creates a new loader that is bound to the given model.
   *
   * @param model the model to which this loader is bound
   * @param interval the time to wait between polls to the server
   */
  public NoteLoader(Model model, int interval) {
    this.model = model;
    this.interval = interval;
  }

  /**
   * Add a note to the loading cache so the {@link Model} can properly manage
   * duplicate objects.
   *
   * @param key
   *          the notes key
   * @param note
   *          the note
   */
  public void cacheNote(String key, Note note) {
    assert key != null;
    notesCache.put(key, note);
  }

  /**
   * Forces the loader to cancel the active poller, reload the initial load and
   * start restart polling. This is called by the {@link Model} when a new
   * surface is selected.
   */
  public void reset() {
    activeId++;
    notesCache.clear();
    timestamp = null;

    final StatusObserver statusObserver = model.getStatusObserver();
    statusObserver.onTaskStarted("Loading '"
        + model.getSelectedSurface().getTitle() + "' ...");

    if (model.getSelectedSurface().hasKey()) {
      startInitialLoad();
    } else {
      statusObserver.onTaskFinished();
      model.notifySurfaceNotesReceived(new Note[0]);
      startPolling();
    }
  }

  private boolean isActiveSession(int id) {
    return id == activeId;
  }

  private void startInitialLoad() {
    new InitialLoader();
  }

  private void startPolling() {
    new Poller();
  }
}
