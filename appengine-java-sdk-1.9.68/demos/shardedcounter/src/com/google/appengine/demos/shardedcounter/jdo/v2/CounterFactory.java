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

package com.google.appengine.demos.shardedcounter.jdo.v2;

import com.google.appengine.demos.shardedcounter.jdo.PMF;

import javax.jdo.PersistenceManager;

/**
 * Finds or creates a sharded counter with the desired name.
 *
 */
public class CounterFactory {

  public ShardedCounter getCounter(String name) {
    ShardedCounter counter = new ShardedCounter(name);
    if (counter.isInDatastore()) {
      return counter;
    } else {
      return null;
    }
  }

  public ShardedCounter createCounter(String name) {
    ShardedCounter counter = new ShardedCounter(name);

    Counter counterEntity = new Counter(name, 0);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(counterEntity);
    } finally {
      pm.close();
    }

    return counter;
  }
}
