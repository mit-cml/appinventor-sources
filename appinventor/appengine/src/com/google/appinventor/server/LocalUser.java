// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
