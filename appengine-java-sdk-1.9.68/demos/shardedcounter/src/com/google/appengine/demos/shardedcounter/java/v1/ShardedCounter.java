/* Copyright (c) 2011 Google Inc.
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

package com.google.appengine.demos.shardedcounter.java.v1;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

import java.util.Random;

/**
 * This initial implementation simply counts all instances of the
 * SimpleCounterShard kind in the datastore. The only way to increment the
 * number of shards is to add another shard by creating another entity in the
 * datastore.
 */
public class ShardedCounter {

  private static final DatastoreService ds = DatastoreServiceFactory
      .getDatastoreService();

  /**
   * Default number of shards.
   */
  private static final int NUM_SHARDS = 20;

  /**
   * A random number generator, for distributing writes across shards.
   */
  private final Random generator = new Random();

  /**
   * Retrieve the value of this sharded counter.
   * 
   * @return Summed total of all shards' counts
   */
  public long getCount() {
    long sum = 0;

    Query query = new Query("SimpleCounterShard");
    for (Entity e : ds.prepare(query).asIterable()) {
      sum += (Long) e.getProperty("count");
    }

    return sum;
  }

  /**
   * Increment the value of this sharded counter.
   */
  public void increment() {
    int shardNum = generator.nextInt(NUM_SHARDS);
    Key shardKey = KeyFactory.createKey("SimpleCounterShard",
        Integer.toString(shardNum));

    Transaction tx = ds.beginTransaction();
    Entity shard;
    try {
      shard = ds.get(tx, shardKey);
      long count = (Long) shard.getProperty("count");
      shard.setUnindexedProperty("count", count + 1L);
    } catch (EntityNotFoundException e) {
      shard = new Entity(shardKey);
      shard.setUnindexedProperty("count", 1L);
    }
    ds.put(tx, shard);
    tx.commit();
  }
}
