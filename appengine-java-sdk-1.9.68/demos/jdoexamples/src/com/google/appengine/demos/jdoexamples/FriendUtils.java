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

package com.google.appengine.demos.jdoexamples;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.ArrayList;
import java.util.List;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class FriendUtils {

  public static Key getKeyForName(String lastName, String firstName) {
    return KeyFactory.createKey(Friend.class.getSimpleName(),
                                lastName + ", " + firstName);
  }

  public static void addFriendTo(String lastName, String firstName,
                                 String friendLastName,
                                 String friendFirstName) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    String meKey = KeyFactory.keyToString(getKeyForName(lastName, firstName));
    String otherKey = KeyFactory.keyToString(
        getKeyForName(friendLastName, friendFirstName));

    Friend other = null;
    try {
      pm.currentTransaction().begin();
      try {
        other = pm.getObjectById(Friend.class, otherKey);
        List<String> replacementFriends = new ArrayList<String>(
            other.getFriendKeys());
        replacementFriends.add(meKey);
        other.setFriendKeys(replacementFriends);
      } catch (JDOObjectNotFoundException e) {
        other = new Friend(friendLastName, friendFirstName);
        List<String> replacementFriends = new ArrayList<String>(
            other.getFriendKeys());
        replacementFriends.add(meKey);
        other.setFriendKeys(replacementFriends);
        pm.makePersistent(other);
      }
      pm.currentTransaction().commit();
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
    }

    pm.close();
    pm = PMF.get().getPersistenceManager();

    Friend me = null;
    try {
      pm.currentTransaction().begin();
      try {
        me = pm.getObjectById(Friend.class, meKey);
        List<String> replacementFriends = new ArrayList<String>(
            me.getFriendKeys());
        replacementFriends.add(otherKey);
        me.setFriendKeys(replacementFriends);
      } catch (JDOObjectNotFoundException e) {
        me = new Friend(lastName, firstName);
        List<String> replacementFriends = new ArrayList<String>(
            me.getFriendKeys());
        replacementFriends.add(otherKey);
        me.setFriendKeys(replacementFriends);
        pm.makePersistent(me);
      }
      pm.currentTransaction().commit();
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
    }
  }

  public static List<Friend> getFriendsOf(String lastName, String firstName) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(Friend.class);
    String myKey = KeyFactory.keyToString(getKeyForName(lastName, firstName));
    query.declareParameters("String myKey");
    query.setFilter("friends == myKey");
    query.setOrdering("lastName ASC, firstName ASC");
    List<Friend> friends = (List<Friend>) query.execute(myKey);

    return friends;
  }
}
