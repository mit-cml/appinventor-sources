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
import java.util.Date;

/**
 * A client side data object representing a Sticky note.
 *
 */
@SuppressWarnings("serial")
public class Note implements Serializable {

  public interface Observer {
    void onUpdate(Note note);
  }

  /**
   * The primary key which is always assigned by the server.
   */
  private String key;

  /**
   * The key of the Surface to which this note belongs.
   */
  private String surfaceKey;

  /**
   * The dimensions of the sticky note.
   */
  private int x, y, width, height;

  /**
   * The text content of the note.
   */
  private String content;

  /**
   * The time of the most recent update. This value is always supplied by the
   * server.
   */
  private Date lastUpdatedAt;

  /**
   * The name of the author in a form that can be displayed in the Ui.
   */
  private String authorName;

  private String authorEmail;

  /**
   * An observer to receive callbacks whenever this {@link Note} is updated.
   */
  private transient Observer observer;

  /**
   * Indicates whether a sticky is editable by the current author.
   */
  private transient boolean ownedByCurrentUser;

  /**
   * A constructor to be used on client-side only.
   *
   * @param model
   * @param x
   * @param y
   * @param width
   * @param height
   */
  public Note(Model model, int x, int y, int width, int height) {
    assert GWT.isClient();
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    ownedByCurrentUser = true;
  }

  /**
   * A constructor to be used on server-side only.
   *
   * @param key
   * @param x
   * @param y
   * @param width
   * @param height
   * @param content
   * @param lastUpdatedAt
   * @param authorName
   * @param ownedByCurrentUser
   */
  public Note(String key, int x, int y, int width, int height, String content,
      Date lastUpdatedAt, String authorName, String authorEmail) {
    assert !GWT.isClient();
    this.key = key;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.content = content;
    this.lastUpdatedAt = lastUpdatedAt;
    this.authorName = authorName;
    this.authorEmail = authorEmail;
  }

  /**
   * A default constructor to allow these objects to be serialized with GWT's
   * RPC.
   */
  @SuppressWarnings("unused")
  private Note() {
  }

  public String getAuthorName() {
    return (ownedByCurrentUser) ? "You" : authorName;
  }

  public String getContent() {
    return content;
  }

  public int getHeight() {
    return height;
  }

  public String getKey() {
    return key;
  }

  public Date getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  /**
   * Gets the observer that is receiving notification when the note is modified.
   *
   * @return
   */
  public Observer getObserver() {
    return observer;
  }

  public String getSurfaceKey() {
    return surfaceKey;
  }

  public int getWidth() {
    return width;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  /**
   * Indicates whether this note is owned by the current user.
   *
   * @return <code>true</code> if the note is owned by the current user,
   *         <code>false</code> otherwise
   */
  public boolean isOwnedByCurrentUser() {
    return ownedByCurrentUser;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Sets the observer that will receive notification when this note is
   * modified.
   *
   * @param observer
   */
  public void setObserver(Observer observer) {
    this.observer = observer;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  /**
   * Initializes transient data structures in the object. This will be called
   * directly by the controlling model when the note is first received.
   *
   * @param model
   *          the model that owns this {@link Note}
   */
  void initialize(Model model) {
    ownedByCurrentUser = model.getCurrentAuthor().getEmail()
        .equals(authorEmail);
  }

  /**
   * Invoked when the note has been saved to the server.
   *
   * @param lastUpdatedAt
   *          the time that the server reported for the save
   * @return <code>this</code>, for chaining purposes
   */
  Note update(Date lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;

    return this;
  }

  /**
   * Invoked when the model receives notification from the server that this note
   * has been modified.
   *
   * @param note
   *          a note containing up-to-date information about <code>this</code>
   * @return <code>this</code>, for chaining purposes
   */
  Note update(Note note) {
    if (!note.getLastUpdatedAt().equals(lastUpdatedAt)) {
      key = note.key;
      surfaceKey = note.surfaceKey;
      x = note.x;
      y = note.y;
      width = note.width;
      height = note.height;
      content = note.content;
      ownedByCurrentUser = note.ownedByCurrentUser;
      authorName = note.authorName;
      lastUpdatedAt = note.lastUpdatedAt;
      observer.onUpdate(this);
    }
    return this;
  }

  Note update(String key, Date lastUpdatedAt) {
    this.key = key;
    this.lastUpdatedAt = lastUpdatedAt;

    return this;
  }
}
