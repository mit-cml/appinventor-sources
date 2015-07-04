// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;

/**
 * A singleton providing the UserInfoProvider interface information.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class LocalUser implements UserInfoProvider {

  private ThreadLocal<User> user = new ThreadLocal<User>();

  /**
   * Returns the singleton LocalUser instance.
   *
   * @return  localUser instance
   */
  public static LocalUser getInstance() {
    return LocalUserInstanceHolder.INSTANCE;
  }

  private static class LocalUserInstanceHolder {
    private LocalUserInstanceHolder() {} // not to be instantiated
    private static final LocalUser INSTANCE = new LocalUser();
  }

  public void set(User newUser) {
    user.set(newUser);
  }

  @Override
  public User getUser() throws UnsupportedOperationException {
    try {
      return user.get().copy();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  // UserInfoProvider implementation

  @Override
  public String getUserId() throws UnsupportedOperationException {
    try {
      return user.get().getUserId();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public String getUserEmail() throws UnsupportedOperationException {
    try {
      return user.get().getUserEmail();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public String getUserName() throws UnsupportedOperationException {
    try {
      return user.get().getUserName();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public String getUserLink() throws UnsupportedOperationException {
    try {
      return user.get().getUserLink();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public int getUserEmailFrequency() throws UnsupportedOperationException {
    try {
      return user.get().getUserEmailFrequency();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public boolean getUserTosAccepted() throws UnsupportedOperationException {
    try {
      return user.get().getUserTosAccepted();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public boolean getIsAdmin() {
    try {
      return user.get().getIsAdmin();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public int getType() {
    try {
      return user.get().getType();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public String getSessionId() {
    try {
      return user.get().getSessionId();
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

  @Override
  public void setSessionId(String sessionId) {
    try {
      user.get().setSessionId(sessionId);
    } catch (NullPointerException e) {
      // This should never happen, but just in case...
      throw new UnsupportedOperationException("User field should have been initialized.");
    }
  }

}
