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

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class GuestbookEntry {

  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private String who;
  
  @Persistent
  private Date when;
  
  @Persistent
  private String message;

  public GuestbookEntry(String who, String message) {
    this.message = message;
    this.who = who;
    this.when = new Date();
  }
  
  public String getWho() {
    return who;
  }

  public Date getWhen() {
    return when;
  }

  public String getMessage() {
    return message;
  }
 
  public static void insert(String who, String message) {
    GuestbookEntry entry = new GuestbookEntry(who, message);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    pm.makePersistent(entry);
  }
  
  public static List<GuestbookEntry> getEntries() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(GuestbookEntry.class);
    query.setOrdering("when DESC");
    List<GuestbookEntry> entries = (List<GuestbookEntry>) query.execute();
    return entries;
  }
}
