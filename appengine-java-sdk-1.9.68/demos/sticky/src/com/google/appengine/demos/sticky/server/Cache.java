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

package com.google.appengine.demos.sticky.server;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.User;
import com.google.appengine.demos.sticky.client.model.Note;
import com.google.appengine.demos.sticky.client.model.Surface;
import java.util.List;

/**
 * A more typesafe wrapper around a {@link MemcacheService}.
 *
 */
public class Cache {
  private static String createNotesId(String surfaceKey) {
    return NOTES_PREFIX + surfaceKey;
  }

  private static String createNotesId(User user, String surfaceKey) {
    return NOTES_PREFIX + user.getEmail() + surfaceKey;
  }

  private static String createSurfaceId(Key surfaceKey) {
    return SURFACE_PREFIX + KeyFactory.keyToString(surfaceKey);
  }

  private static String createSurfaceKeysId(String email) {
    return SURFACEKEYS_PRFIX + email;
  }

  /**
   * The memcache service to use for caching.
   */
  private final MemcacheService memcache;

  private static final String NOTES_PREFIX = "NOTES/";

  private static final String SURFACE_PREFIX = "SURFACE/";

  private static final String SURFACEKEYS_PRFIX = "SURFACES/";

  /**
   * Creates a new cache.
   *
   * @param memcache
   *          the memcache service to use for caching objects
   */
  public Cache(MemcacheService memcache) {
    this.memcache = memcache;
  }

  /**
   * Deletes the collection of notes that are cached for a surface. This is used
   * to invalidate cache entries.
   *
   * @param surfaceKey
   *          the key for the surface
   */
  public void deleteNotes(String surfaceKey) {
    memcache.delete(NOTES_PREFIX + surfaceKey);
  }

  /**
   * Deletes a {@link Surface} from the cache. This is used to invalidate cache
   * entries.
   *
   * @param surfaceKey
   */
  public void deleteSurface(Key surfaceKey) {
    memcache.delete(createSurfaceId(surfaceKey));
  }

  /**
   * Deletes a collection of surface keys for a user.
   *
   * @param email
   *          the user's email
   */
  public void deleteSurfaceKeys(String email) {
    memcache.delete(createSurfaceKeysId(email));
  }

  /**
   * Attempts to fetch the collection of notes contained in a surface for a
   * particular user. If there is a value, but the user is not known to have
   * access to this surface, <code>null</code> will be returned.
   *
   * @param user
   *          the user requesting access to the cache entry
   * @param surfaceKey
   *          they key for the surface
   * @return a collection of notes if there is a cache entry, <code>null</code>
   *         otherwise
   */
  public Note[] getNotes(User user, String surfaceKey) {
    if (!canUserAccessNotes(user, surfaceKey)) {
      return null;
    }
    return (Note[]) memcache.get(createNotesId(surfaceKey));
  }

  /**
   * Attempts to fetch a surface from cache.
   *
   * @param surfaceKey
   *          the key of the surface
   * @return the surface if there is a cache entry, <code>null</code> otherwise
   */
  public Surface getSurface(Key surfaceKey) {
    return (Surface) memcache.get(createSurfaceId(surfaceKey));
  }

  /**
   * Attempts to fetch a collection of surface keys for a particular user.
   *
   * @param email
   *          the email for the user
   * @return a collection of keys if there is a cache entry, <code>null</code>
   *         otherwise
   */
  @SuppressWarnings("unchecked")
  public List<Key> getSurfaceKeys(String email) {
    return (List<Key>) memcache.get(createSurfaceKeysId(email));
  }

  /**
   * Adds the collection of notes in a surface to the cache and grants the user
   * access to that entry.
   *
   * @param user
   *          the user that should be given access to the cache entry
   * @param surfaceKey
   *          the key for the surface
   * @param notes
   *          the collection of notes to cache
   * @return <code>notes</code>, for call chaining
   */
  public Note[] putNotes(User user, String surfaceKey, Note[] notes) {
    memcache.put(createNotesId(surfaceKey), notes);
    allowUserToAccessNotes(user, surfaceKey);
    return notes;
  }

  /**
   * Adds a surface object to the cache.
   *
   * @param surfaceKey
   *          the key of the surface
   * @param surface
   *          the surface object
   * @return <code>surface</code>, for call chaining
   */
  public Surface putSurface(Key surfaceKey, Surface surface) {
    memcache.put(createSurfaceId(surfaceKey), surface);
    return surface;
  }

  /**
   * Adds the collection of surface keys to the cache for a user (by email).
   *
   * @param email
   *          the user's email
   * @param surfaceKeys
   *          a collection of surface keys
   * @return <code>surfaceKeys</code>, for call chaining
   */
  public List<Key> putSurfaceKeys(String email, List<Key> surfaceKeys) {
    memcache.put(createSurfaceKeysId(email), surfaceKeys);
    return surfaceKeys;
  }

  private void allowUserToAccessNotes(User user, String surfaceKey) {
    memcache.put(createNotesId(user, surfaceKey), Boolean.TRUE);
  }

  private boolean canUserAccessNotes(User user, String surfaceKey) {
    return memcache.contains(createNotesId(user, surfaceKey));
  }
}
