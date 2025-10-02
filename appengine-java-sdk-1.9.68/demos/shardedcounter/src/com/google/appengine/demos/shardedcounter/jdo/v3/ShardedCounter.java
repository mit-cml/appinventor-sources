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

package com.google.appengine.demos.shardedcounter.jdo.v3;

import com.google.appengine.demos.shardedcounter.jdo.PMF;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

/**
 * A counter which can be incremented rapidly.
 *
 * Capable of incrementing the counter and increasing the number of shards.
 * When incrementing, a random shard is selected to prevent a single shard
 * from being written to too frequently. If increments are being made too
 * quickly, increase the number of shards to divide the load. Performs
 * datastore operations using JDO.
 *
 * Lookups are attempted using Memcache (Jcache). If the counter value is
 * not in the cache, the shards are read from the datastore and accumulated
 * to reconstruct the current count.
 *
 */
public class ShardedCounter {
  private String counterName;
  private Cache cache;

  public ShardedCounter(String counterName) {
    this.counterName = counterName;
    cache = null;
    try {
      cache = CacheManager.getInstance().getCacheFactory().createCache(
          Collections.emptyMap());
    } catch (CacheException e) {
    }
  }

  public String getCounterName() {
    return counterName;
  }

  private DatastoreCounter getThisCounter(PersistenceManager pm) {
    DatastoreCounter current = null;
    Query thisCounterQuery = pm.newQuery(DatastoreCounter.class,
        "counterName == nameParam");
    thisCounterQuery.declareParameters("String nameParam");
    List<DatastoreCounter> counter =
        (List<DatastoreCounter>) thisCounterQuery.execute(counterName);
    if (counter != null && !counter.isEmpty()) {
      current = counter.get(0);
    }
    return current;
  }

  public boolean isInDatastore() {
    boolean counterStored = false;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      if (getThisCounter(pm) != null) {
        counterStored = true;
      }
    } finally {
      pm.close();
    }
    return counterStored;
  }

  public int getCount() {
    if (cache != null) {
      Integer cachedCount = (Integer) cache.get("count" + counterName);
      if (cachedCount != null) {
        return cachedCount.intValue();
      }
    }

    int sum = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      Query shardsQuery = pm.newQuery(DatastoreCounterShard.class,
                                      "counterName == nameParam");
      shardsQuery.declareParameters("String nameParam");
      List<DatastoreCounterShard> shards =
          (List<DatastoreCounterShard>) shardsQuery.execute(counterName);
      if (shards != null && !shards.isEmpty()) {
        for (DatastoreCounterShard current : shards) {
          sum += current.getCount();
        }
      }
    } finally {
      pm.close();
    }

    if (cache != null) {
      cache.put("count" + counterName, Integer.valueOf(sum));
    }

    return sum;
  }

  public int getNumShards() {
    if (cache != null) {
      Integer cachedCount = (Integer) cache.get("shards" + counterName);
      if (cachedCount != null) {
        return cachedCount.intValue();
      }
    }

    int numShards = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      DatastoreCounter current = getThisCounter(pm);
      if (current != null) {
        numShards = current.getShardCount().intValue();
      }
    } finally {
      pm.close();
    }

    if (cache != null) {
      cache.put("shards" + counterName, Integer.valueOf(numShards));
    }

    return numShards;
  }

  public int addShard() {
    return addShards(1);
  }

  public int addShards(int count) {
    int numShards = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      DatastoreCounter current = getThisCounter(pm);
      if (current != null) {
        numShards = current.getShardCount().intValue();
        current.setShardCount(numShards + count);
        pm.makePersistent(current);
      }
    } finally {
      pm.close();
    }

    pm = PMF.get().getPersistenceManager();
    try {
      for (int i = 0; i < count; i++) {
        DatastoreCounterShard newShard = new DatastoreCounterShard(
            getCounterName(), numShards);
        pm.makePersistent(newShard);
        numShards++;
      }
    } finally {
      pm.close();
    }

    if (cache != null) {
      cache.put("shards" + counterName, Integer.valueOf(numShards));
    }

    return numShards;
  }

  public void increment() {
    increment(1);
  }

  public void increment(int count) {
    if (cache != null) {
      Integer cachedCount = (Integer) cache.get("count" + counterName);
      if (cachedCount != null) {
        cache.put("count" + counterName,
            Integer.valueOf(count + cachedCount.intValue()));
      }
    }

    int shardCount = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      DatastoreCounter current = getThisCounter(pm);
      shardCount = current.getShardCount();
    } finally {
      pm.close();
    }

    Random generator = new Random();
    int shardNum = generator.nextInt(shardCount);

    pm = PMF.get().getPersistenceManager();
    try {
      Query randomShardQuery = pm.newQuery(DatastoreCounterShard.class);
      randomShardQuery.setFilter(
          "counterName == nameParam && shardNumber == numParam");
      randomShardQuery.declareParameters("String nameParam, int numParam");
      List<DatastoreCounterShard> shards =
          (List<DatastoreCounterShard>) randomShardQuery.execute(
              counterName, shardNum);
      if (shards != null && !shards.isEmpty()) {
        DatastoreCounterShard shard = shards.get(0);
        shard.increment(count);
        pm.makePersistent(shard);
      }
    } finally {
      pm.close();
    }
  }
}
