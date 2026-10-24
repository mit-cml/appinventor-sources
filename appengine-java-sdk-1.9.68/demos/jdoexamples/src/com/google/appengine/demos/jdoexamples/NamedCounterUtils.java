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
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

public class NamedCounterUtils {

  public static Key getKeyForName(String name) {
    return KeyFactory.createKey(NamedCounter.class.getSimpleName(), name);
  }

  public static NamedCounter getByName(String name) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    NamedCounter counter = null;

    try {
      counter = pm.getObjectById(NamedCounter.class,
          KeyFactory.keyToString(getKeyForName(name)));
    } catch (JDOObjectNotFoundException e) {
      counter = new NamedCounter(name);
    }
    return counter;
  }

  public static int addAndGet(String name, int delta) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    NamedCounter counter = null;

    try {
      pm.setDetachAllOnCommit(true);
      pm.currentTransaction().begin();
      try {
        counter = pm.getObjectById(NamedCounter.class,
            KeyFactory.keyToString(getKeyForName(name)));
        counter.add(delta);
      } catch (JDOObjectNotFoundException e) {
        counter = new NamedCounter(name);
        counter.add(delta);
        pm.makePersistent(counter);
      }
      pm.currentTransaction().commit();
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
    }
    return counter.getCount();
  }

  public static void reset(String name) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      pm.currentTransaction().begin();
      try {
        NamedCounter counter = pm.getObjectById(NamedCounter.class,
            KeyFactory.keyToString(getKeyForName(name)));
        pm.deletePersistent(counter);
      } catch (JDOObjectNotFoundException e) {
        return;
      }
      pm.currentTransaction().commit();
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
    }
  }
}
