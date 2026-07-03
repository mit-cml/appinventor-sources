// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.common.testutils.TestUtils;

import static junit.framework.Assert.*;
import junitx.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author markf@google.com (Mark Friedman)
 */
public class OdeAuthFilterTest {
  private final LocalDatastoreTestCase helper = LocalDatastoreTestCase.createHelper();

  private MockedStatic<LocalUser> localUserStatic;
  private FilterChain mockFilterChain;
  private HttpServletRequest mockServletRequest;
  private HttpServletResponse mockServletResponse;
  private LocalUser localUserMock;
  private OdeAuthFilter.UserInfo localUserInfo;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    localUserMock = Mockito.mock(LocalUser.class);
    localUserStatic = Mockito.mockStatic(LocalUser.class);
    localUserStatic.when(LocalUser::getInstance).thenReturn(localUserMock);
    Mockito.when(localUserMock.getUserEmail()).thenReturn("NonSuch");
    localUserInfo = Mockito.mock(OdeAuthFilter.UserInfo.class);
    Mockito.when(localUserInfo.buildCookie(false)).thenReturn("NoCookie");
    Mockito.when(localUserInfo.buildCookie(true)).thenReturn("NoCookie");
    mockFilterChain = Mockito.mock(FilterChain.class);
    mockServletRequest = Mockito.mock(HttpServletRequest.class);
    mockServletResponse = Mockito.mock(HttpServletResponse.class);
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
    localUserStatic.close();
  }

  @Test
  public void testDoFilterShouldContinueFilterChainIfNotWhitelisted() throws Exception {

    final AtomicInteger isUserWhitelistedCounter = new AtomicInteger(0);
    Mockito.when(localUserMock.getUserTosAccepted()).thenReturn(true);

    OdeAuthFilter.useWhitelist.setForTest(false);

    OdeAuthFilter myAuthFilter = new OdeAuthFilter() {
      @Override
      void setUserFromUserId(String userId, boolean isAdmin, boolean isReadOnly, long oneProjectId, String fauxProjectName, String fauxAccoutName) { localUserMock.set(new User("1", "NonSuch", false, false, null)); return;}
      @Override
      void removeUser() {}
      @Override
      boolean isUserWhitelisted() {
        isUserWhitelistedCounter.incrementAndGet();
        return false;
      }
    };

    myAuthFilter.doMyFilter(localUserInfo, false, false,  0, null, null, mockServletRequest, mockServletResponse, mockFilterChain);

    assertEquals(0, isUserWhitelistedCounter.get());
    Mockito.verify(mockFilterChain).doFilter(mockServletRequest, mockServletResponse);
  }

  @Test
  public void testDoFilterShouldNotContinueFilterChainIfNotWhitelisted() throws Exception {

    final AtomicInteger isUserWhitelistedCounter = new AtomicInteger(0);
    final AtomicInteger writeWhitelistErrorMessageCounter = new AtomicInteger(0);

    OdeAuthFilter.useWhitelist.setForTest(true);

    OdeAuthFilter myAuthFilter = new OdeAuthFilter() {
      @Override
      void setUserFromUserId(String userId, boolean isAdmin, boolean isReadOnly, long oneProjectId, String fauxProjectName, String fauxAccoutName) { localUserMock.set(new User("1", "NonSuch", false, false, null)); return;}
      @Override
      void removeUser() {}
      @Override
      boolean isUserWhitelisted() {
        isUserWhitelistedCounter.incrementAndGet();
        return false;
      }
      @Override
      void writeWhitelistErrorMessage(HttpServletResponse response) {
        writeWhitelistErrorMessageCounter.incrementAndGet();
      }
    };

    myAuthFilter.doMyFilter(localUserInfo, false, false, 0, null, null, mockServletRequest, mockServletResponse, mockFilterChain);

    assertEquals(1, isUserWhitelistedCounter.get());
    assertEquals(1, writeWhitelistErrorMessageCounter.get());
    Mockito.verify(mockFilterChain, Mockito.never()).doFilter(Mockito.any(), Mockito.any());
  }
}
