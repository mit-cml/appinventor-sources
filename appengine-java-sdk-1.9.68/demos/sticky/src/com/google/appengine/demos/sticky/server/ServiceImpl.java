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
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.demos.sticky.client.model.Author;
import com.google.appengine.demos.sticky.client.model.Note;
import com.google.appengine.demos.sticky.client.model.Service;
import com.google.appengine.demos.sticky.client.model.Surface;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.jdo.Transaction;

/**
 * The server-side RPC endpoint for {@link Service}.
 *
 *
 */
@SuppressWarnings("serial")
public class ServiceImpl extends RemoteServiceServlet implements Service {

  private static final int TIMESTAMP_PADDING = 1000 * 60;

  private static Date convertTimestampToDate(String timetamp) {
    return new Date(Long.parseLong(timetamp, 16) - TIMESTAMP_PADDING);
  }

  private static String createTimestamp() {
    return Long.toString(System.currentTimeMillis(), 16);
  }

  private static Note[] getNotesSinceTimestamp(Note[] notes, String timestamp) {
    if (timestamp == null) {
      return notes;
    } else {
      final Date since = convertTimestampToDate(timestamp);
      final List<Note> newNotes = new ArrayList<Note>(notes.length);
      for (Note note : notes) {
        if (note.getLastUpdatedAt().after(since)) {
          newNotes.add(note);
        }
      }
      return newNotes.toArray(new Note[newNotes.size()]);
    }
  }

  private static String getSurfaceKey(Store.Note note) {
    return KeyFactory.keyToString(note.getKey().getParent());
  }

  private static Note[] toClientNotes(Collection<Store.Note> notes) {
    final Note[] clients = new Note[notes.size()];
    int i = 0;
    for (Store.Note n : notes) {
      clients[i++] = new Note(KeyFactory.keyToString(n.getKey()), n.getX(), n
          .getY(), n.getWidth(), n.getHeight(), n.getContent(), n
          .getLastUpdatedAt(), n.getAuthorName(), n.getAuthorEmail());
    }
    return clients;
  }

  private static Surface toClientSurface(Store.Surface surface) {
    final List<String> names = surface.getAuthorNames();
    return new Surface(KeyFactory.keyToString(surface.getKey()), surface
        .getTitle(), names.toArray(new String[names.size()]), surface
        .getNotes().size(), surface.getLastUpdatedAt());
  }

  /**
   * A convenient way to get the current user and throw an exception if the user
   * isn't logged in.
   *
   * @param userService
   *          the user service to use
   * @return the current user
   * @throws AccessDeniedException
   */
  private static User tryGetCurrentUser(UserService userService)
      throws AccessDeniedException {
    if (!userService.isUserLoggedIn()) {
      throw new Service.AccessDeniedException();
    }
    return userService.getCurrentUser();
  }

  /**
   * A reference to the data store.
   */
  private final Store store = new Store("transactions-optional");

  /**
   * A reference to a cache service.
   */
  private final Cache cache = new Cache(MemcacheServiceFactory
      .getMemcacheService());

  public AddAuthorToSurfaceResult addAuthorToSurface(final String surfaceKey,
      final String email) throws AccessDeniedException {
    final User user = tryGetCurrentUser(UserServiceFactory.getUserService());
    final Store.Api api = store.getApi();
    try {
      final Key key = KeyFactory.stringToKey(surfaceKey);

      final Store.Author me = api.getOrCreateNewAuthor(user);

      final Store.Author author = api.tryGetAuthor(email);
      if (author == null) {
        return null;
      }

      if (!me.hasSurface(key)) {
        throw new Service.AccessDeniedException();
      }

      final Store.Surface surface = api.getSurface(key);
      if (!author.hasSurface(key)) {

        cache.deleteSurfaceKeys(author.getEmail());
        cache.deleteSurface(surface.getKey());

        final Transaction txA = api.begin();
        author.addSurface(surface);
        api.saveAuthor(author);
        txA.commit();

        final Transaction txB = api.begin();
        surface.addAuthorName(author.getName());
        api.saveSurface(surface);
        txB.commit();
      }
      return new AddAuthorToSurfaceResult(author.getName(), surface
          .getLastUpdatedAt());

    } finally {
      api.close();
    }
  }

  public Date changeNoteContent(final String noteKey, final String content)
      throws AccessDeniedException {
    final User user = tryGetCurrentUser(UserServiceFactory.getUserService());
    final Store.Api api = store.getApi();
    try {
      final Key key = KeyFactory.stringToKey(noteKey);
      final Store.Author me = api.getOrCreateNewAuthor(user);

      final Transaction tx = api.begin();
      final Store.Note note = api.getNote(key);
      if (!note.isOwnedBy(me)) {
        throw new Service.AccessDeniedException();
      }
      note.setContent(content);
      final Date result = api.saveNote(note).getLastUpdatedAt();
      tx.commit();

      cache.deleteNotes(getSurfaceKey(note));
      return result;
    } finally {
      api.close();
    }
  }

  public Date changeNotePosition(final String noteKey, final int x,
      final int y, final int width, final int height)
      throws AccessDeniedException {
    final User user = tryGetCurrentUser(UserServiceFactory.getUserService());
    final Store.Api api = store.getApi();
    try {
      final Key key = KeyFactory.stringToKey(noteKey);
      final Store.Author me = api.getOrCreateNewAuthor(user);

      final Transaction tx = api.begin();
      final Store.Note note = api.getNote(key);
      if (!note.isOwnedBy(me)) {
        throw new Service.AccessDeniedException();
      }
      note.setX(x);
      note.setY(y);
      note.setWidth(width);
      note.setHeight(height);
      final Date result = api.saveNote(note).getLastUpdatedAt();
      tx.commit();

      cache.deleteNotes(getSurfaceKey(note));
      return result;
    } finally {
      api.close();
    }
  }

  public CreateObjectResult createNote(final String surfaceKey, final int x,
      final int y, final int width, final int height)
      throws AccessDeniedException {
    final User user = tryGetCurrentUser(UserServiceFactory.getUserService());
    final Store.Api api = store.getApi();
    try {
      final Key key = KeyFactory.stringToKey(surfaceKey);
      final Store.Author me = api.getOrCreateNewAuthor(user);

      if (!me.hasSurface(key)) {
        throw new Service.AccessDeniedException();
      }

      final Transaction tx = api.begin();
      final Store.Surface surface = api.getSurface(key);
      final Store.Note note = new Store.Note(me, x, y, width, height);
      surface.getNotes().add(note);
      api.saveSurface(surface);

      final CreateObjectResult result = new CreateObjectResult(KeyFactory
          .keyToString(note.getKey()), note.getLastUpdatedAt());
      tx.commit();

      cache.deleteNotes(surfaceKey);
      return result;
    } finally {
      api.close();
    }
  }

  public CreateObjectResult createSurface(final String title)
      throws AccessDeniedException {
    final User user = tryGetCurrentUser(UserServiceFactory.getUserService());
    final Store.Api api = store.getApi();
    try {
      final Store.Author me = api.getOrCreateNewAuthor(user);

      final Store.Surface surface = new Store.Surface(title);
      surface.addAuthorName(me.getName());
      api.saveSurface(surface);

      final Transaction tx = api.begin();
      me.addSurface(surface);
      api.saveAuthor(me);
      tx.commit();

      cache.deleteSurfaceKeys(me.getEmail());

      return new CreateObjectResult(KeyFactory.keyToString(surface.getKey()),
          surface.getLastUpdatedAt());
    } finally {
      api.close();
    }
  }

  public GetNotesResult getNotes(String surfaceKey, String since)
      throws AccessDeniedException {
    final User user = tryGetCurrentUser(UserServiceFactory.getUserService());
    return new Service.GetNotesResult(createTimestamp(), getNotes(user,
        surfaceKey, since));
  }

  public GetSurfacesResult getSurfaces(String timestamp)
      throws AccessDeniedException {
    final User user = tryGetCurrentUser(UserServiceFactory.getUserService());
    final Store.Api api = store.getApi();
    try {
      final List<Key> keys = getSurfaceKeys(api, user);
      final Surface[] surfaces = new Surface[keys.size()];
      for (int i = 0, n = keys.size(); i < n; ++i) {
        surfaces[i] = getSurface(api, keys.get(i));
      }
      return new GetSurfacesResult(null, surfaces);
    } finally {
      api.close();
    }
  }

  public UserInfoResult getUserInfo() throws AccessDeniedException {
    final UserService userService = UserServiceFactory.getUserService();
    final User user = tryGetCurrentUser(userService);
    final Store.Api api = store.getApi();
    try {
      final Key surfaceKey = getSurfaceKeys(api, user).get(0);
      final UserInfoResult result = new Service.UserInfoResult(new Author(user
          .getEmail(), user.getNickname()), getSurface(api, surfaceKey),
          userService.createLogoutURL(userService.createLoginURL("/")));
      return result;
    } finally {
      api.close();
    }
  }

  private Note[] getNotes(User user, String surfaceKey, String since)
      throws AccessDeniedException {
    final Store.Api api = store.getApi();
    try {
      final Note[] fromCache = cache.getNotes(user, surfaceKey);
      if (fromCache != null) {
        return getNotesSinceTimestamp(fromCache, since);
      }

      final Key key = KeyFactory.stringToKey(surfaceKey);
      final Store.Author me = api.getOrCreateNewAuthor(user);
      if (!me.hasSurface(key)) {
        throw new Service.AccessDeniedException();
      }
      final Store.Surface surface = api.getSurface(key);
      final Note[] notes = cache.putNotes(user, surfaceKey,
          toClientNotes(surface.getNotes()));
      return getNotesSinceTimestamp(notes, since);
    } finally {
      api.close();
    }
  }

  private Surface getSurface(Store.Api api, Key key) {
    final Surface fromCache = cache.getSurface(key);
    if (fromCache != null) {
      return fromCache;
    }

    return cache.putSurface(key, toClientSurface(api.getSurface(key)));
  }

  private List<Key> getSurfaceKeys(Store.Api api, User user) {
    final String email = user.getEmail();

    final List<Key> fromCache = cache.getSurfaceKeys(email);
    if (fromCache != null) {
      return fromCache;
    }

    final Store.Author author = api.getOrCreateNewAuthor(user);
    return cache.putSurfaceKeys(email, author.getSurfaceKeys());
  }

}
