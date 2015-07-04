// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;
import java.util.Date;

/**
 * Data Transfer Object representing motd.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class Nonce implements IsSerializable, Serializable {
  private long id;

  private String nonceValue;
  private String userId;
  private long projectId;
  private Date timestamp;

  /**
   * Creates a new motd data transfer object.
   *
   * @param id  motd's ID
   * @param caption  caption
   * @param content  more detail, if any
   */
  public Nonce(String nonceValue, String userId, long projectId, Date timestamp) {
    this.userId = userId;
    this.projectId = projectId;
    this.nonceValue = nonceValue;
    this.timestamp = timestamp;
  }

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private Nonce() {
  }

  /**
   * Returns the id.
   *
   * @return id
   */
  public long getId() {
    return id;
  }

  /**
   * Returns the userId
   *
   * @return userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Returns the projectId
   *
   * @return projectId
   */

  public long getProjectId() {
    return projectId;
  }

  /**
   * Returns the nonce value.
   *
   * @return nonceValue
   *
   */
   public String getNonceValue() {
     return nonceValue;
   }

  /**
   * Returns this object.
   *
   * @return nonce
   */
  public Nonce getNonce() {
    return this;
  }

  public Date getTimeStamp() {
    return this.timestamp;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Nonce)) return false;
    Nonce nonce = (Nonce) obj;
    if (nonce.getNonceValue().equals(nonceValue)) return true;
    return false;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
