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

import java.util.List;
import java.util.Random;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * A counter which can be incremented rapidly.
 *
 * Capable of incrementing the counter and increasing the number of shards.
 * When incrementing, a random shard is selected to prevent a single shard
 * from being written to too frequently. If increments are being made too
 * quickly, increase the number of shards to divide the load. Performs
 * datastore operations using JDO.
 *
 */
public class ShardedCounter {
  private String counterName;

  public ShardedCounter(String counterName) {
    this.counterName = counterName;
  }

  public String getCounterName() {
    return counterName;
  }

  private Counter getThisCounter(PersistenceManager pm) {
    Counter current = null;
    Query thisCounterQuery = pm.newQuery(Counter.class,
        "counterName == nameParam");
    thisCounterQuery.declareParameters("String nameParam");
    List<Counter> counter = (List<Counter>) thisCounterQuery.execute(
        counterName);
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
    int sum = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      Query shardsQuery = pm.newQuery(CounterShard.class,
                                      "counterName == nameParam");
      shardsQuery.declareParameters("String nameParam");
      List<CounterShard> shards = (List<CounterShard>) shardsQuery.execute(
          counterName);
      if (shards != null && !shards.isEmpty()) {
        for (CounterShard current : shards) {
          sum += current.getCount();
        }
      }
    } finally {
      pm.close();
    }
    return sum;
  }

  public int getNumShards() {
    int numShards = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Counter current = getThisCounter(pm);
      if (current != null) {
        numShards = current.getShardCount().intValue();
      }
    } finally {
      pm.close();
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
      Counter current = getThisCounter(pm);
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
        CounterShard newShard = new CounterShard(getCounterName(), numShards);
        pm.makePersistent(newShard);
        numShards++;
      }
    } finally {
      pm.close();
    }
    return numShards;
  }

  public void increment() {
    increment(1);
  }

  public void increment(int count) {
    int shardCount = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Counter current = getThisCounter(pm);
      shardCount = current.getShardCount();
    } finally {
      pm.close();
    }

    Random generator = new Random();
    int shardNum = generator.nextInt(shardCount);

    pm = PMF.get().getPersistenceManager();
    try {
      Query randomShardQuery = pm.newQuery(CounterShard.class);
      randomShardQuery.setFilter(
          "counterName == nameParam && shardNumber == numParam");
      randomShardQuery.declareParameters("String nameParam, int numParam");
      List<CounterShard> shards = (List<CounterShard>) randomShardQuery
          .execute(counterName, shardNum);
      if (shards != null && !shards.isEmpty()) {
        CounterShard shard = shards.get(0);
        shard.increment(count);
        pm.makePersistent(shard);
      }
    } finally {
      pm.close();
    }
  }
}
