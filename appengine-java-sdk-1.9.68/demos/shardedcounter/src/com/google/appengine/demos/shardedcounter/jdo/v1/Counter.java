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

package com.google.appengine.demos.shardedcounter.jdo.v1;

import com.google.appengine.demos.shardedcounter.jdo.PMF;

import java.util.List;
import javax.jdo.PersistenceManager;

/**
 * This initial implementation simply counts all instances of the
 * LimitedCounterShard class in the datastore. The only way to increment the
 * counter is to add another shard (creating another entity in the datastore).
 * This is not the correct approach, since we're limited to the number of
 * objects we can fetch with the query, but this is a simple foundation showing
 * how to add a new shard.
 *
 */
public class Counter {

  public Counter() {
  }

  public int getCount() {
    int sum = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<LimitedCounterShard> shards = null;
    try {
      String query = "select from " + LimitedCounterShard.class.getName();
      shards = (List<LimitedCounterShard>) pm.newQuery(query).execute();
      if (shards != null && !shards.isEmpty()) {
        for (LimitedCounterShard shard : shards) {
          sum += shard.getCount();
        }
      }
    } finally {
      pm.close();
    }
    return sum;
  }

  public void addShard() {
    addShards(1);
  }

  public void addShards(int count) {
    LimitedCounterShard newShard = new LimitedCounterShard();
    newShard.setCount(Integer.valueOf(1));

    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(newShard);
    } finally {
      pm.close();
    }
  }
}
