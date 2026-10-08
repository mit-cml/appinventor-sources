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

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Represents a counter in the datastore and stores the number of shards.
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class DatastoreCounter {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String counterName;

  @Persistent
  private Integer numShards;

  public DatastoreCounter(String counterName) {
    this.counterName = counterName;
    this.numShards = Integer.valueOf(0);
  }

  public DatastoreCounter(String counterName, Integer numShards) {
    this.counterName = counterName;
    this.numShards = numShards;
  }

  public Long getId() {
    return id;
  }

  public String getCounterName() {
    return counterName;
  }

  public Integer getShardCount() {
    return numShards;
  }

  public void setShardCount(int count) {
    this.numShards = Integer.valueOf(count);
  }
}
