/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.appengine.demos.taskengine.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * JDO persistable TaskData.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Task implements IsSerializable {
  @Persistent
  private String details;

  @Persistent
  private String email;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
  private String id;

  @Persistent
  private boolean isFinished;

  @Persistent
  private int labelPriority;

  @Persistent
  private String title;

  /**
   * Zero arg ctor to work with GWT RPC serialization.
   */
  public Task() {
    this.email = "";
    this.title = "";
    this.details = "";
    this.labelPriority = -1;
    this.isFinished = false;
  }

  public Task(String email, String title, String details,
      int labelPriority, boolean isFinished) {
    this.email = email;
    this.title = title;
    this.details = details;
    this.labelPriority = labelPriority;
    this.isFinished = isFinished;
  }

  public String getDetails() {
    return details;
  }

  public String getEmail() {
    return email;
  }

  public String getId() {
    return id;
  }

  public int getLabelPriority() {
    return labelPriority;
  }

  public String getTitle() {
    return title;
  }

  public boolean isFinished() {
    return isFinished;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setFinished(boolean isFinished) {
    this.isFinished = isFinished;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLabelPriority(int labelPriority) {
    this.labelPriority = labelPriority;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
