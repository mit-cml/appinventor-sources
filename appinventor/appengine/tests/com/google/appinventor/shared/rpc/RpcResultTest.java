// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc;

import junit.framework.TestCase;

/**
 * Unit tests for {@link RpcResult}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class RpcResultTest extends TestCase {
  public void testZero() {
    RpcResult result = new RpcResult(0, "Operation completed successfully.", "");
    assertEquals(0, result.getResult());
    assertEquals(RpcResult.SUCCESS, result.getResult());
    assertTrue(result.succeeded());
    assertFalse(result.failed());
    assertEquals("Operation completed successfully.", result.getOutput());
    assertEquals("", result.getError());
  }

  public void testSuccess() {
    RpcResult result = RpcResult.createSuccessfulRpcResult("Operation completed successfully.", "");
    assertEquals(0, result.getResult());
    assertEquals(RpcResult.SUCCESS, result.getResult());
    assertTrue(result.succeeded());
    assertFalse(result.failed());
    assertEquals("Operation completed successfully.", result.getOutput());
    assertEquals("", result.getError());
  }

  public void testTrue() {
    RpcResult result = new RpcResult(true, "Operation completed successfully.", "");
    assertEquals(0, result.getResult());
    assertEquals(RpcResult.SUCCESS, result.getResult());
    assertTrue(result.succeeded());
    assertFalse(result.failed());
    assertEquals("Operation completed successfully.", result.getOutput());
    assertEquals("", result.getError());
  }

  public void testNonzero() {
    RpcResult result = new RpcResult(1, "", "Complete system failure. Please try again.");
    assertNotSame(RpcResult.SUCCESS, result.getResult());
    assertFalse(result.succeeded());
    assertTrue(result.failed());
    assertEquals("", result.getOutput());
    assertEquals("Complete system failure. Please try again.", result.getError());
  }

  public void testFailure() {
    RpcResult result = RpcResult.createFailingRpcResult(
        "", "Complete system failure. Please try again.");
    assertNotSame(RpcResult.SUCCESS, result.getResult());
    assertFalse(result.succeeded());
    assertTrue(result.failed());
    assertEquals("", result.getOutput());
    assertEquals("Complete system failure. Please try again.", result.getError());
  }

  public void testFalse() {
    RpcResult result = new RpcResult(false, "", "Complete system failure. Please try again.");
    assertNotSame(RpcResult.SUCCESS, result.getResult());
    assertFalse(result.succeeded());
    assertTrue(result.failed());
    assertEquals("", result.getOutput());
    assertEquals("Complete system failure. Please try again.", result.getError());
  }
}
