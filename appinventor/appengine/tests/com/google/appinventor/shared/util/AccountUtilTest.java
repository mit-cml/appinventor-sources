// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.util;

import java.security.SecureRandom;
import java.util.Random;

import junit.framework.TestCase;

/**
 * Tests for {@link AccountUtil}.
 *
 */
public class AccountUtilTest extends TestCase {

  private static final long MAX = 17592186044415L;  // 2^44-1

  public void testGenerateAccountIdUsesSuppliedGenerator() {
    assertEquals(AccountUtil.generateAccountId(new Random(42)),
        AccountUtil.generateAccountId(new Random(42)));
  }

  public void testGeneratedAccountIdIsInRangeAndRoundTrips() {
    // Values with the high bits set must still map into the 44-bit code space
    Random allOnes = new Random() {
      @Override
      public long nextLong() {
        return -1L;
      }
    };
    String account = AccountUtil.generateAccountId(allOnes);
    assertEquals("anon-" + MAX, account);
    assertEquals(account, AccountUtil.codeToAccount(AccountUtil.accountToCode(account)));

    SecureRandom random = new SecureRandom();
    for (int i = 0; i < 1000; i++) {
      account = AccountUtil.generateAccountId(random);
      long id = Long.parseLong(account.substring(5));
      assertTrue(id >= 0 && id <= MAX);
      assertEquals(account, AccountUtil.codeToAccount(AccountUtil.accountToCode(account)));
    }
  }
}
