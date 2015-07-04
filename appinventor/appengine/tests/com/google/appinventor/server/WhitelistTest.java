// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.server;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.shared.rpc.user.User;

import org.junit.Test;

import static junit.framework.Assert.*;
import junitx.framework.Assert;

/**
 * @author kerr@google.com (Debby Wallach)
 */
public class WhitelistTest {

  public static final String WHITELIST_ROOT_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/tests/com/google/appinventor/server/";  // must end with a slash

  public static final String ID = "dummyId";

  Whitelist whitelist;
  LocalUser user = LocalUser.getInstance();;

  @Test
  public void testValidWhitelist() throws Exception {
    Whitelist.rootPath.setForTest(WHITELIST_ROOT_PATH);
    whitelist = new Whitelist();
    assertTrue(whitelist.isInWhitelist(makeUser("kerr@google.com")));
    assertFalse(whitelist.isInWhitelist(makeUser("kerry@google.com")));
    assertTrue(whitelist.isInWhitelist(makeUser("junk@google.com")));
    assertTrue(whitelist.isInWhitelist(makeUser("goo@gmail.com")));
    assertFalse(whitelist.isInWhitelist(makeUser("absolutelyNotHere@noway.com")));
    assertTrue(whitelist.isInWhitelist(makeUser("LizLooney@google.com")));
  }

  @Test
  public void testMissingWhitelist() throws Exception {
    Whitelist.rootPath.setForTest(TestUtils.APP_INVENTOR_ROOT_DIR + "/donotexist/");
    whitelist = new Whitelist();
    assertFalse(whitelist.isInWhitelist(makeUser("kerr@google.com")));
    assertFalse(whitelist.isInWhitelist(makeUser("kerry@google.com")));
    assertFalse(whitelist.isInWhitelist(makeUser("fake@google.com")));
  }

  LocalUser makeUser(String email) {
    user.set(new User(ID, email, null, "http://www.appinventor.org/", 5, false, false, 0, "test-session"));
    return user;
  }

}
