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

import com.google.gwt.core.client.GWT;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A client-side object representing a surface object.
 *
 */
@SuppressWarnings("serial")
public class Surface implements Serializable {

  /**
   * A callback interface for observing updates on a particular instance of
   * {@link Surface}.
   */
  public static interface Observer {
    void onUpdate(Surface surface);
  }

  private static String[] appendAuthorName(String[] original, String name) {
    final int n = original.length;
    final String[] names = new String[n + 1];
    for (int i = 0; i < n; ++i) {
      names[i] = original[i];
    }
    names[n] = name;
    return names;
  }

  /**
   * The primary key which is always assigned by the server.
   */
  private String key;

  /**
   * The title for the surface.
   */
  private String title;

  /**
   * A list of author nick names.
   */
  private String[] authorNames;

  /**
   * The number of notes on this surface.
   */
  private int noteCount;

  /**
   * A date that is managed by the server which indicates the last time this
   * object was saved on the server.
   */
  private Date lastUpdatedAt;

  /**
   * comma-separated list of author's. This value is a cache of the computation
   * that is done in {@link Surface#computeAuthorNamesAsString(Model)}.
   */
  private transient String authorNamesString;

  /**
   * The list of observers that are listening for updates to this object.
   */
  private transient List<Observer> observers;

  /**
   * Used to create a new surface in client-side code.
   *
   * @param model
   *          the model to which this surface is bound
   * @param title
   *          the title of the surface
   */
  public Surface(Model model, String title) {
    assert GWT.isClient();
    this.title = title;
    this.authorNames = new String[] { "You" };
    initialize(model);
  }

  /**
   * Constructs a new surface. This constructor can only be invoked on the
   * server.
   *
   * @param key
   *          the primary key
   * @param title
   *          the surface's title
   * @param authorNames
   *          the names of all the authors
   * @param noteCount
   *          the number of notes on this surface
   * @param lastUpdatedAt
   *          the time the surface was last modified on the server
   */
  public Surface(String key, String title, String[] authorNames, int noteCount,
      Date lastUpdatedAt) {
    assert !GWT.isClient();
    this.key = key;
    this.title = title;
    this.authorNames = authorNames;
    this.noteCount = noteCount;
    this.lastUpdatedAt = lastUpdatedAt;
  }

  /**
   * Needed for RPC serialization.
   */
  @SuppressWarnings("unused")
  private Surface() {
  }

  /**
   * Subscribe an observer to receive update notifications.
   *
   * @param observer
   */
  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  /**
   * Returns the nick names of all authors on a surface.
   *
   * @return
   */
  public String[] getAuthorNames() {
    return authorNames;
  }

  /**
   * Returns a comma-separated and human readable list of authors for this
   * surface.
   *
   * @return
   */
  public String getAuthorNamesAsString() {
    return authorNamesString;
  }

  /**
   * Returns the primary key for this surface.
   *
   * @return
   */
  public String getKey() {
    return key;
  }

  /**
   * Returns the date corresponding to the time this object was last saved on
   * the server.
   *
   * @return
   */
  public Date getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  /**
   * Returns the number of notes on this surface.
   *
   * @return
   */
  public int getNoteCount() {
    return noteCount;
  }

  /**
   * Returns the title for the surface.
   *
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Indicates whether the surface has received a key from the server (objects
   * receive keys after they are initially saved to the server).
   *
   * @return
   */
  public boolean hasKey() {
    return key != null;
  }

  /**
   * Remove an existing observer.
   *
   * @param observer
   */
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  /**
   * Returns a comma-separated, human-readable string containing all of the
   * authors for this surface. This attempts to shorten author names to
   * something more friendly. For instance, knorton@google.com would be
   * shortened to knorton and Kelly Norton would become Kelly.
   *
   * @param model
   *          a model that is needed to determine the current user
   * @return
   */
  private String computeAuthorNamesAsString(Model model) {
    final String currentAuthorName = model.getCurrentAuthor().getName();
    final String[] names = authorNames;
    final int n = names.length;

    assert n > 0;

    if (n == 1) {
      return "Just You";
    }

    final StringBuffer buffer = new StringBuffer();

    final int m = n - 1;
    boolean foundYou = false;
    for (int i = 0; i < n; ++i) {
      String name = names[i];
      if (!foundYou && currentAuthorName.equals(name)) {
        name = "You";
        foundYou = true;
      }
      if (i == m) {
        buffer.append(" & ");
      } else if (i != 0) {
        buffer.append(", ");
      }
      buffer.append(Author.getShortName(name));
    }
    return buffer.toString();
  }

  private void notifyUpdate() {
    assert observers != null;
    for (int i = 0, n = observers.size(); i < n; ++i) {
      observers.get(i).onUpdate(this);
    }
  }

  /**
   * Initializes some transient data in the object. This should be called by the
   * {@link Model} when a surface is first introduced in the client application.
   *
   * @param model
   *          the model to which this object is bound
   */
  void initialize(Model model) {
    assert observers == null;
    authorNamesString = computeAuthorNamesAsString(model);
    observers = new ArrayList<Observer>();
  }

  Surface update(Model model, String authorName, Date updatedAt) {
    assert updatedAt.after(lastUpdatedAt);
    authorNames = appendAuthorName(authorNames, authorName);
    lastUpdatedAt = updatedAt;
    authorNamesString = computeAuthorNamesAsString(model);
    notifyUpdate();
    return this;
  }

  Surface update(Model model, Surface surface) {
    if (!surface.getLastUpdatedAt().equals(lastUpdatedAt)) {
      authorNames = surface.authorNames;
      noteCount = surface.noteCount;
      authorNamesString = computeAuthorNamesAsString(model);
      notifyUpdate();
    }
    return this;
  }

  Surface update(String key, Date updatedAt) {
    this.key = key;
    this.lastUpdatedAt = updatedAt;
    return this;
  }
}
