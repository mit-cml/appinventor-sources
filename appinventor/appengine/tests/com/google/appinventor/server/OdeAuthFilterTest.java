// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.server;

import static org.easymock.EasyMock.expect;

import com.google.appinventor.common.testutils.TestUtils;

import static junit.framework.Assert.*;
import junitx.framework.Assert;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author markf@google.com (Mark Friedman)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ LocalUser.class, OdeAuthFilter.class })
public class OdeAuthFilterTest {
  // If OdeAuthFilterTest (which uses PowerMock.mockStatic) extends LocalDatastoreTestCase, then
  // it will probably fail with Ant version 1.8.2.
  private final LocalDatastoreTestCase helper = LocalDatastoreTestCase.createHelper();

  private FilterChain mockFilterChain;
  private HttpServletRequest mockServletRequest;
  private HttpServletResponse mockServletResponse;
  private LocalUser localUserMock;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    localUserMock = PowerMock.createMock(LocalUser.class);
    PowerMock.mockStatic(LocalUser.class);
    expect(LocalUser.getInstance()).andReturn(localUserMock).anyTimes();
    mockFilterChain = PowerMock.createNiceMock(FilterChain.class);
    mockServletRequest = PowerMock.createNiceMock(HttpServletRequest.class);
    mockServletResponse = PowerMock.createNiceMock(HttpServletResponse.class);
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
    PowerMock.resetAll();
  }

  @Test
  public void testDoFilterShouldContinueFilterChainIfNotWhitelisted() throws Exception {

    final AtomicInteger isUserWhitelistedCounter = new AtomicInteger(0);
    expect(localUserMock.getUserTosAccepted()).andReturn(true).times(1);

    // This is the key expectation, i.e. that we continue by calling the doFilter method of the
    // internal mocked FilterChain that will be passed into the tested FilterChain
    mockFilterChain.doFilter(mockServletRequest, mockServletResponse);
    EasyMock.expectLastCall().once();
    PowerMock.replayAll();

    // Here's where we say that the whitelist is not active
    OdeAuthFilter.useWhitelist.setForTest(false);

    OdeAuthFilter myAuthFilter = new OdeAuthFilter() {
      @Override
      boolean setUser(HttpServletRequest req) { return true; }
      @Override
      void removeUser() {}
      @Override
      boolean isUserWhitelisted() {
        isUserWhitelistedCounter.incrementAndGet();
        return false;
      }
    };

    myAuthFilter.doMyFilter(mockServletRequest, mockServletResponse, mockFilterChain);

    // isUserWhitelisted should not have been called.
    assertEquals(0, isUserWhitelistedCounter.get());
    // getUserTosAccepted should have been called once.
    PowerMock.verifyAll();
  }

  @Test
  public void testDoFilterShouldNotContinueFilterChainIfNotWhitelisted() throws Exception {

    final AtomicInteger isUserWhitelistedCounter = new AtomicInteger(0);
    final AtomicInteger writeWhitelistErrorMessageCounter = new AtomicInteger(0);

    // Note the lack any expectation of a call to the doFilter method of the
    // internal mocked FilterChain that will be passed into the tested FilterChain, unlike the
    // case in testDoFilterShouldContinueFilterChainIfNotWhitelisted().
    // In other words, if it IS using a whitelist and the user is NOT whitelisted for testing,
    // mockFilterChain.doFilter should never be called.
    PowerMock.replayAll();

    // Here's where we say that it IS a staging server
    OdeAuthFilter.useWhitelist.setForTest(true);

    OdeAuthFilter myAuthFilter = new OdeAuthFilter() {
      @Override
      boolean setUser(HttpServletRequest req) { return true; }
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

    myAuthFilter.doMyFilter(mockServletRequest, mockServletResponse, mockFilterChain);

    // isUserWhitelisted should have been called once.
    assertEquals(1, isUserWhitelistedCounter.get());
    // writeWhitelistErrorMessage should have been called once
    assertEquals(1, writeWhitelistErrorMessageCounter.get());
    // getUserTosAccepted should not have been called.
    PowerMock.verifyAll();
  }
}
