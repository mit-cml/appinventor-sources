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

package com.google.appengine.demos.taskqueueexamples;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * A JDO object representing a counter.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Counter {

  @PrimaryKey
  private String name;

  @Persistent
  private int count;

  public Counter(String name, int count) {
    this.name = name;
    this.count = count;
  }

  public String getName() {
    return name;
  }

  public int getCount() {
    return count;
  }

  public void increment(int delta) {
    count += delta;
  }

  public static void createOrIncrement(String name, int delta) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Counter counter = null;
    try {
      pm.currentTransaction().begin();
      try {
        counter = pm.getObjectById(Counter.class, name);
        counter.increment(delta);
      } catch (JDOObjectNotFoundException e) {
        counter = new Counter(name, delta);
        pm.makePersistent(counter);
      }
      pm.currentTransaction().commit();
    } finally {
      if (pm.currentTransaction().isActive()) {
        pm.currentTransaction().rollback();
      }
    }
  }

  public static int getCount(String name) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Counter counter = null;
    try {
      counter = pm.getObjectById(Counter.class, name);
    } catch (JDOObjectNotFoundException e) {
    }
    if (counter == null) {
      return 0;
    } else {
      return counter.getCount();
    }
  }
}
