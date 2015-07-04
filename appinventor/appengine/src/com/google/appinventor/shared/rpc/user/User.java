// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.user;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Data Transfer Object representing user data.
 *
 */
public class User implements IsSerializable, UserInfoProvider, Serializable {
  // Unique identifier for the user
  private String id;

  // user email address
  private String email;

  // user display name
  private String name;

  // user introduction link
  private String link;

  // email notification frequency
  private int emailFrequency;

  // whether user has accepted terms of service
  private boolean tosAccepted;
  
  // whether the user has admin priviledges
  private boolean isAdmin;

  // which type the user has
  private int type;
  private String sessionId;        // Used to ensure only one account active at a time

  public final static String usercachekey = "f682688a-1065-4cda-8515-a8bd70200ac9"; // UUID
  // This UUID is prepended to any key lookup for User objects. Memcache is a common
  // key/value store for the entire application. By prepending a unique value, we ensure
  // that other modules that use Memcache will not collide with us. By using a UUID,
  // properly generated, we don't have to worry about allocating specific prefixes and
  // ensuring that they are unique.

  public static final int USER = 0;
  public static final int MODERATOR = 1;
  public static final int DEFAULT_EMAIL_NOTIFICATION_FREQUENCY = 5;

  /**
   * Creates a new user data transfer object.
   *
   * @param id unique user id (from {@link com.google.appengine.api.users.User#getUserId()}
   * @param email user email address
   * @param tosAccepted TOS accepted?
   * @param sessionId client session Id
   */
  public User(String id, String email, String name, String link, int emailFrequency, boolean tosAccepted, boolean isAdmin, int type, String sessionId) {
    this.id = id;
    this.email = email;
    if (name==null)
      this.name = getDefaultName();
    else
      this.name = name;
    this.tosAccepted = tosAccepted;
    this.isAdmin = isAdmin;
    this.link = link;
    this.emailFrequency = emailFrequency;
    this.type = type;
    this.sessionId = sessionId;
  }

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private User() {
  }

  /**
   * Returns the user's unique id.
   *
   * @return user id
   */
  @Override
  public String getUserId() {
    return id;
  }

  /**
   * Returns the user's email address.
   *
   * @return user email address
   */
  @Override
  public String getUserEmail() {
    return email;
  }

  /**
   * Sets the user's email address.
   */
  public void setUserEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the user's name.
   * If user's name is missing (not set yet), return email instead.
   * @return user name
   */
  @Override
  public String getUserName() {
    if (name != null) {
      return name;
    } else {
      return email;
    }
  }

  /**
   * Sets the user's name.
   */
  public void setUserName(String name) {
    this.name = name;
  }

  /**
   * Returns the user's link.
   *
   * @return user link
   */
  @Override
  public String getUserLink() {
    return link;
  }

  /**
   * Sets the user's link.
   */
  public void setUserLink(String link) {
    this.link = link;
  }

  /**
   * Returns the email notification frequency set by user.
   *
   * @return emailFrequency email frequency
   */
  @Override
  public int getUserEmailFrequency() {
    return emailFrequency;
  }

  /**
   * Sets the user's email notification frequency
   */
  public void setUserEmailFrequency(int emailFrequency) {
    this.emailFrequency = emailFrequency;
  }

  /**
   * Sets whether the user has accepted the terms of service.
   *
   * @param tos {@code true} if the user has accepted the terms of service,
   *            {@code false} otherwise
   */
  public void setUserTosAccepted(boolean tos) {
    tosAccepted = tos;
  }

  /**
   * Returns whether the user has accepted the terms of service.
   *
   * @return {@code true} if the user has accepted the terms of service,
   *         {@code false} otherwise
   */
  @Override
  public boolean getUserTosAccepted() {
    return tosAccepted;
  }
  
  @Override
  public boolean getIsAdmin() {
    return isAdmin;
  }
  
  /**
   * Sets whether the user has admin priviledges.
   *
   * @param admin {@code true} if the user has admin priviledges,
   *              {@code false} otherwise
   */
  public void setIsAdmin(boolean admin) {
    isAdmin = admin;
  }

  @Override
  public int getType() {
    return type;
  }

  /**
   * Sets which type the user has.
   *
   * @param type `
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Returns this object.
   *
   * @return user
   */
  @Override
  public User getUser() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof User && ((User) obj).id == id;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public boolean isModerator() {
    if(type == MODERATOR){
      return true;
    }
    return false;
  }

  public static String getDefaultName(String email)
  {
    if (email==null)
      return "user";
    String[] parts = email.split("@");
    if (parts.length>1) {
      return parts[0];
    } else {
      return email;
    }
  }

  public String getDefaultName() {
    return getDefaultName(this.email);
  }

  /**
   * Get the unique session id associated with this user
   * This is used to ensure that only one session is opened
   * per uers. Old sessions are invalidated.
   *
   * @return sessionId
   */
  @Override
  public String getSessionId() {
    return sessionId;
  }

  @Override
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public User copy() {
    return new User(id, email, name, link, emailFrequency, tosAccepted, isAdmin, type, sessionId);
  }
}
