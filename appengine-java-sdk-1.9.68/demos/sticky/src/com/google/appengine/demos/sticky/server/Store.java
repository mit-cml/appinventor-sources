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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * An application specific Api wrapper around the {@link DatastoreService}.
 * Creating a {@link Store} is relatively expensive so one should be mindful not
 * to create these unnecessarily. For example, in a servlet, the Store should be
 * stored as a field in the store to avoid creating it on every request.
 *
 *
 */
public class Store {

  public class Api {

    /**
     * The JDO persistence manager used for all calls.
     */
    private final PersistenceManager manager;

    private Api() {
      manager = factory.getPersistenceManager();
    }

    /**
     * Begin a new transaction.
     *
     * @return the transaction
     */
    public Transaction begin() {
      final Transaction tx = manager.currentTransaction();
      tx.begin();
      return tx;
    }

    /**
     * Close the connection to the data store. Clients are expected to guarantee
     * that close will be called. This will also rollback any active
     * transaction.
     */
    public void close() {
      final Transaction tx = manager.currentTransaction();
      if (tx.isActive()) {
        tx.rollback();
      }
      manager.close();
    }

    /**
     * Gets the author by email.
     *
     * @param email
     *          the author's email
     * @return the author
     * @throws JDOObjectNotFoundException
     */
    public Author getAuthor(String email) {
      return manager.getObjectById(Author.class, email);
    }

    /**
     * Gets a note from the data store.
     *
     * @param key
     *          the note's key
     * @return
     */
    public Note getNote(Key key) {
      return manager.getObjectById(Note.class, key);
    }

    /**
     * Looks in the data store for an author with a matching email. If the
     * author does not exist, a new one will be created. The newly created
     * author will also have access to a newly created surface.
     *
     * @param user
     *          the user for which an author object is needed
     * @return an author object
     */
    public Author getOrCreateNewAuthor(User user) {
      try {
        return getAuthor(user.getEmail());
      } catch (JDOObjectNotFoundException e) {

        final Transaction txA = begin();
        final Surface surface = new Surface("My First Surface");
        surface.addAuthorName(user.getNickname());
        saveSurface(surface);
        txA.commit();

        final Transaction txB = begin();
        final Author author = new Author(user.getEmail(), user.getNickname());
        author.addSurface(surface);
        saveAuthor(author);
        txB.commit();
        return author;
      }
    }

    /**
     * Gets a surface from the data store.
     *
     * @param key
     *          the surface's key
     * @return
     */
    public Surface getSurface(Key key) {
      return manager.getObjectById(Surface.class, key);
    }

    /**
     * Persist an author to the data store.
     *
     * @param author
     *          the author to be persisted
     * @return <code>author</code>, for call chaining
     */
    public Author saveAuthor(Author author) {
      return manager.makePersistent(author);
    }

    /**
     * Persist a note to the data store.
     *
     * @param note
     *          the note to be persisted
     * @return <code>note</code>, for call chaining
     */
    public Note saveNote(Note note) {
      note.lastUpdatedAt = new Date();
      return manager.makePersistent(note);
    }

    /**
     * Persist a surface to the data store.
     *
     * @param surface
     *          the surface to be persisted
     * @return <code>surface</code>, for call chaining
     */
    public Surface saveSurface(Surface surface) {
      surface.lastUpdatedAt = new Date();
      return manager.makePersistent(surface);
    }

    /**
     * Attempts to get the author with the given email. If there is no known
     * author with that email, <code>null</code> will be returned.
     *
     * @param email
     *          the author's email
     * @return the author or <code>null</code> if the author can't be found
     */
    public Author tryGetAuthor(String email) {
      try {
        return getAuthor(email);
      } catch (JDOObjectNotFoundException e) {
        return null;
      }
    }
  }

  /**
   * An ORM object representing an author.
   */
  @PersistenceCapable(identityType = IdentityType.APPLICATION)
  public static class Author {
    /**
     * The author's email. Serves as the primary key for this object.
     */
    @PrimaryKey
    private String email;

    /**
     * The author's name.
     */
    @Persistent
    private String name;

    /**
     * The keys of all surfaces this author has access to.
     */
    @Persistent(defaultFetchGroup = "true")
    @Element(dependent = "true")
    private List<Key> surfaceKeys = new ArrayList<Key>();

    /**
     * Construct a new author.
     *
     * @param email
     *          the author's email
     * @param name
     *          the author's name
     */
    public Author(String email, String name) {
      this.name = name;
      setEmail(email);
    }

    /**
     * Give this author access to a surface.
     *
     * @param surface
     *          the surface the author is being granted access to.
     */
    public void addSurface(Surface surface) {
      final List<Key> keys = new ArrayList<Key>(surfaceKeys);
      keys.add(surface.getKey());
      setSurfaceKeys(keys);
    }

    /**
     * Gets the author's email.
     *
     * @return
     */
    public String getEmail() {
      return email;
    }

    /**
     * Gets the author's name.
     *
     * @return
     */
    public String getName() {
      return name;
    }

    /**
     * Returns the keys for each surface that the author has access to.
     *
     * @return
     */
    public List<Key> getSurfaceKeys() {
      return surfaceKeys;
    }

    /**
     * Returns whether this author has access to a particular surface.
     *
     * @param surfaceKey
     *          the key of the surface
     * @return <code>true</code> if the author does have access,
     *         <code>false</code> otherwise
     */
    public boolean hasSurface(Key surfaceKey) {
      for (Key key : surfaceKeys) {
        if (key.equals(surfaceKey)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Sets the author's email.
     *
     * @param email
     */
    public void setEmail(String email) {
      this.email = email;
    }

    /**
     * Sets the author's name.
     *
     * @param name
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Reassigns the collection of surface keys. This is required to ensure that
     * the ORM will persist element collections.
     *
     * @param keys
     */
    private void setSurfaceKeys(List<Key> keys) {
      surfaceKeys = keys;
    }
  }

  /**
   * An ORM object representing a note.
   */
  @PersistenceCapable(identityType = IdentityType.APPLICATION)
  public static class Note {
    /**
     * An auto-generated primary key for this object. This key will be a child
     * key of the owning surface's key.
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    /**
     * The x position of the note.
     */
    @Persistent
    private int x;

    /**
     * The y position of the note.
     */
    @Persistent
    private int y;

    /**
     * The width of the note.
     *
     * <p>
     * NOTE: The application does not currently provide the ability to resize
     * notes.
     * </p>
     */
    @Persistent
    private int width;

    /**
     * The height of the note
     *
     * <p>
     * NOTE: The application does not currently provide the ability to resize
     * notes.
     * </p>
     */
    @Persistent
    private int height;

    /**
     * The text content of the note.
     */
    @Persistent
    private String content;

    /**
     * The date of the last time this object was persisted.
     */
    @Persistent
    private Date lastUpdatedAt = new Date();

    /**
     * The email of the author created this note.
     */
    @Persistent
    private String authorEmail;

    /**
     * The name of the author who created this note.
     */
    @Persistent
    private String authorName;

    /**
     * Create a new note.
     *
     * @param owner
     *          the author who created this note
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Note(Author owner, int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      authorEmail = owner.getEmail();
      authorName = owner.getName();
    }

    /**
     * The author's email.
     *
     * @return
     */
    public String getAuthorEmail() {
      return authorEmail;
    }

    /**
     * The author's name.
     *
     * @return
     */
    public String getAuthorName() {
      return authorName;
    }

    /**
     * The text of the note. This value is not escaped in anyway and should
     * never be used as html on the client.
     *
     * @return unsafe text content
     */
    public String getContent() {
      return content;
    }

    /**
     * Gets the height of the note.
     *
     * @return
     */
    public int getHeight() {
      return height;
    }

    /**
     * Gets the object's primary key.
     *
     * @return
     */
    public Key getKey() {
      return key;
    }

    /**
     * Gets the date of the last time this object was persisted.
     *
     * @return
     */
    public Date getLastUpdatedAt() {
      return lastUpdatedAt;
    }

    /**
     * Gets the width of the note.
     *
     * @return
     */
    public int getWidth() {
      return width;
    }

    /**
     * Gets the x position of the note.
     *
     * @return
     */
    public int getX() {
      return x;
    }

    /**
     * Gets the y position of the note.
     *
     * @return
     */
    public int getY() {
      return y;
    }

    /**
     * Indicates whether the given author is the owner of this note.
     *
     * @param author
     *          the author
     * @return <code>true</code> if <code>author</code> is the owner of the
     *         note, <code>false</code> otherwise.
     */
    public boolean isOwnedBy(Author author) {
      return author.getEmail().equals(authorEmail);
    }

    /**
     * Sets the content.
     *
     * @param content
     */
    public void setContent(String content) {
      this.content = content;
    }

    /**
     * Sets the height of the note.
     *
     * @param height
     */
    public void setHeight(int height) {
      this.height = height;
    }

    /**
     * Sets the width of the note.
     *
     * @param width
     */
    public void setWidth(int width) {
      this.width = width;
    }

    /**
     * Sets the x position of the note.
     *
     * @param x
     */
    public void setX(int x) {
      this.x = x;
    }

    /**
     * Sets the y position of the note.
     *
     * @param y
     */
    public void setY(int y) {
      this.y = y;
    }
  }

  /**
   * A JDO object representing a surface.
   */
  @PersistenceCapable(identityType = IdentityType.APPLICATION)
  public static class Surface {
    /**
     * An auto-generated primary key.
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    /**
     * The title of the surface.
     */
    @Persistent
    private String title;

    /**
     * The date of the last time this surface was persisted.
     */
    @Persistent
    private Date lastUpdatedAt;

    /**
     * The name of each author that has access to this surface.
     */
    @Persistent(defaultFetchGroup = "true")
    private List<String> authorNames = new ArrayList<String>();

    /**
     * The notes that are stuck to this surface.
     */
    @Element(dependent = "true")
    private List<Note> notes = new ArrayList<Note>();

    /**
     * Create a new surface.
     *
     * @param title
     */
    public Surface(String title) {
      this.title = title;
    }

    /**
     * Add the name to the author names. Calls to this method are generally
     * paired with a call to {@link Author#addSurface(Surface)}.
     *
     * @param name
     */
    public void addAuthorName(String name) {
      final List<String> names = new ArrayList<String>(authorNames);
      names.add(name);
      setAuthorNames(names);
    }

    /**
     * Gets the collection of author names.
     *
     * @return
     */
    public List<String> getAuthorNames() {
      return authorNames;
    }

    /**
     * Gets the primary key for this object.
     *
     * @return
     */
    public Key getKey() {
      return key;
    }

    /**
     * Gets the date of the last time this object was persisted.
     *
     * @return
     */
    public Date getLastUpdatedAt() {
      return lastUpdatedAt;
    }

    /**
     * Gets all the notes that are stuck to this surface.
     *
     * @return
     */
    public List<Note> getNotes() {
      return notes;
    }

    /**
     * Get the surface's title.
     *
     * @return
     */
    public String getTitle() {
      return title;
    }

    /**
     * Sets the surface's title.
     *
     * @param title
     */
    public void setTitle(String title) {
      this.title = title;
    }

    /**
     * Reassigns the collection of author names. This is required to ensure that
     * the ORM persists element collections.
     *
     * @param names
     */
    private void setAuthorNames(List<String> names) {
      authorNames = names;
    }
  }

  private final PersistenceManagerFactory factory;

  /**
   * Create a new Store based on a particular config.
   *
   * @param config
   */
  public Store(String config) {
    this.factory = JDOHelper.getPersistenceManagerFactory(config);
  }

  /**
   * Starts a data store session and returns an Api object to use.
   *
   * @return
   */
  public Api getApi() {
    return new Api();
  }
}
