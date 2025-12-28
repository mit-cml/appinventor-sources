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
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.io.Serializable;
import java.util.Date;

/**
 * The RPC api available to the client. The asynchronous version that is used
 * directly by the client is {@link ServiceAsync}.
 *
 */
@RemoteServiceRelativePath("service")
public interface Service extends RemoteService {

  /**
   * An exception that is thrown by the server whenever the current user is not
   * logged in, or if the RPC requests an operation that cannot be carried out
   * for the user (i.e. putting notes on someone else's surface).
   */
  @SuppressWarnings("serial")
  static class AccessDeniedException extends Exception {
  }

  /**
   * Encapsulates a response for
   * {@link Service#addAuthorToSurface(String, String)}.
   */
  @SuppressWarnings("serial")
  static class AddAuthorToSurfaceResult implements Serializable {
    private String authorName;

    private Date updatedAt;

    /**
     * Constructs a new result. This constructor can only be invoked on the
     * server.
     *
     * @param authorName
     *          the name of the author that was added to the surface
     * @param updatedAt
     *          the new last updated time for the surface
     */
    public AddAuthorToSurfaceResult(String authorName, Date updatedAt) {
      assert !GWT.isClient();
      this.authorName = authorName;
      this.updatedAt = updatedAt;
    }

    /**
     * Needed for RPC serialization.
     */
    @SuppressWarnings("unused")
    private AddAuthorToSurfaceResult() {
    }

    /**
     * The name of the {@link Author} that was added to the surface.
     *
     * @return
     */
    public String getAuthorName() {
      return authorName;
    }

    /**
     * The new last updated time for the surface that was modified.
     *
     * @return
     */
    public Date getUpdatedAt() {
      return updatedAt;
    }
  }

  /**
   * Encapsulates a response from
   * {@link Service#createNote(user, int, int, int, int)}.
   */
  @SuppressWarnings("serial")
  static class CreateObjectResult implements Serializable {
    private String key;

    private Date updateTime;

    /**
     * Constructs a new result. This constructor can only be invoked on the
     * server.
     *
     * @param key
     *          the key that was assigned to the new {@link Note}
     * @param updateTime
     *          the time assigned to {@link Note#getLastUpdatedAt()}
     */
    public CreateObjectResult(String key, Date updateTime) {
      assert !GWT.isClient();
      this.key = key;
      this.updateTime = updateTime;
    }

    /**
     * Needed for RPC serialization.
     */
    @SuppressWarnings("unused")
    private CreateObjectResult() {
    }

    /**
     * Returns the key that was assigned to the new {@link Note}.
     *
     * @return
     */
    public String getKey() {
      return key;
    }

    /**
     * Returns the {@link Date} that was assigned to
     * {@link Note#getLastUpdatedAt()} by the server.
     *
     * @return
     */
    public Date getUpdateTime() {
      return updateTime;
    }
  }

  /**
   * Encapsulates a response from {@link Service#getNotes(String, String)}.
   */
  @SuppressWarnings("serial")
  static class GetNotesResult implements Serializable {
    private String timestamp;

    private Note[] notes;

    /**
     * Constructs a new result. This constructor can only be invoked on the
     * server.
     *
     * @param timestamp
     *          an opaque timestamp
     * @param notes
     *          the list of notes to return
     */
    public GetNotesResult(String timestamp, Note[] notes) {
      assert !GWT.isClient();
      this.timestamp = timestamp;
      this.notes = notes;
    }

    /**
     * Needed for RPC serialization.
     */
    @SuppressWarnings("unused")
    private GetNotesResult() {
    }

    /**
     * Returns the notes that were returned by the server. This can be
     * zero-length, but will not be null.
     *
     * @return
     */
    public Note[] getNotes() {
      return notes;
    }

    /**
     * Returns an opaque timestamp that should be included in future calls to
     * {@link Service#getNotes(String, String)}.
     *
     * @return
     */
    public String getTimestamp() {
      return timestamp;
    }
  }

  /**
   * Encapsulates a response to {@link Service#getSurfaces(String)}.
   */
  @SuppressWarnings("serial")
  static class GetSurfacesResult implements Serializable {
    private String timestamp;

    private Surface[] surfaces;

    /**
     * Constructs a new result. This constructor can only be invoked on the
     * server.
     *
     * @param timestamp
     *          an opaque timestamp
     * @param surfaces
     *          a list of surfaces for the current author
     */
    public GetSurfacesResult(String timestamp, Surface[] surfaces) {
      assert !GWT.isClient();
      this.timestamp = timestamp;
      this.surfaces = surfaces;
    }

    /**
     * Needed for RPC serialization.
     */
    @SuppressWarnings("unused")
    private GetSurfacesResult() {
    }

    /**
     * Returns a list of surfaces for the current author.
     *
     * @return
     */
    public Surface[] getSurfaces() {
      return surfaces;
    }

    /**
     * Returns an opaque timestamp.
     *
     * @return
     */
    public String getTimestamp() {
      return timestamp;
    }
  }

  /**
   * Encapsulates a response for {@link Service#getUserInfo()}.
   */
  @SuppressWarnings("serial")
  static class UserInfoResult implements Serializable {
    private Author author;

    private Surface surface;

    private String logoutUrl;

    /**
     * Constructs a new response. This constructor can only be invoked on the
     * server.
     *
     * @param author
     *          the current author
     * @param surface
     *          the initially selected {@link Surface}
     * @param logoutUrl
     *          a url that can be used to log the current user out
     */
    public UserInfoResult(Author author, Surface surface, String logoutUrl) {
      assert !GWT.isClient();
      this.author = author;
      this.surface = surface;
      this.logoutUrl = logoutUrl;
    }

    /**
     * Needed for RPC serialization.
     */
    @SuppressWarnings("unused")
    private UserInfoResult() {
    }

    /**
     * Returns the current author.
     *
     * @return
     */
    public Author getAuthor() {
      return author;
    }

    /**
     * Returns a url that can be used to log the author out.
     *
     * @return
     */
    public String getLogoutUrl() {
      return logoutUrl;
    }

    /**
     * Returns the default surface for the author. This is the surface that will
     * be selected when the application first loads.
     *
     * @return
     */
    public Surface getSurface() {
      return surface;
    }
  }

  /**
   * Add an author to the author list of a surface.
   *
   * @param surfaceKey
   *          the key of the surface being modified
   * @param email
   *          the email address of the author being added
   * @return a result object
   * @throws AccessDeniedException
   */
  AddAuthorToSurfaceResult addAuthorToSurface(String surfaceKey, String email)
      throws AccessDeniedException;

  /**
   * Updates the content of a {@link Note}.
   *
   * @param noteKey
   *          they key of the note to modify
   * @param content
   *          the new content to assign
   * @return the new last updated date for the note that was modified
   * @throws AccessDeniedException
   */
  Date changeNoteContent(String noteKey, String content)
      throws AccessDeniedException;

  /**
   * Updates the position for a {@link Note}.
   *
   * @param noteKey
   *          the key of the note to modify
   * @param x
   *          the new x value
   * @param y
   *          the new y value
   * @param width
   *          the new width value
   * @param height
   *          the new height value
   * @return the new last updated date for the note that was modified
   * @throws AccessDeniedException
   */
  Date changeNotePosition(String noteKey, int x, int y, int width, int height)
      throws AccessDeniedException;

  /**
   * Creates a new {@link Note}.
   *
   * @param surfaceKey
   *          the key of the surface where this note will be created
   * @param x
   *          the x position for the new note
   * @param y
   *          the y position for the new note
   * @param width
   *          the width of the new note
   * @param height
   *          the height of the new note
   * @return a result object
   * @throws AccessDeniedException
   */
  CreateObjectResult createNote(String surfaceKey, int x, int y, int width,
      int height) throws AccessDeniedException;

  /**
   * Create a new {@link Surface}.
   *
   * @param title
   *          the title of the surface
   * @return a result object
   * @throws AccessDeniedException
   */
  CreateObjectResult createSurface(String title) throws AccessDeniedException;

  /**
   * Get all notes for the currently logged in author. <code>timestamp</code> is
   * an opaque timestamp used by the server to optimize the set of results that
   * are returned. Callers should pass a timestamp from
   * {@link GetNotesResult#getTimestamp()}. For the initial call, or to simply
   * receive the full set of notes, pass <code>null</code>.
   *
   * @param surfaceKey
   *          the surface to query
   * @param timestamp
   *          an opaque timestamp
   * @return
   * @throws AccessDeniedException
   */
  GetNotesResult getNotes(String surfaceKey, String timestamp)
      throws AccessDeniedException;

  /**
   * Get all the surfaces for the currently logged in author.
   * <code>timestamp</code> is an opaque timestamp user by the server to
   * optimize the number of results that are returned. Callers should pass a
   * timestamp from {@link GetSurfacesResult#getTimestamp()}. For the initial
   * call, or to simply receive the full set of surfaces, pass <code>null</code>
   * .
   *
   * @param timestamp
   *          an opaque timestamp
   * @return a result object
   * @throws AccessDeniedException
   */
  GetSurfacesResult getSurfaces(String timestamp) throws AccessDeniedException;

  /**
   * Returns the information needed to load the application.
   *
   * @return a result object
   * @throws AccessDeniedException
   */
  UserInfoResult getUserInfo() throws AccessDeniedException;
}
