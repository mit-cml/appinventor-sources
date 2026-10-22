// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.util;

import java.lang.reflect.Field;
import java.security.SecureRandom;

import junit.framework.TestCase;

/**
 * Tests for {@link AccountUtil}.
 *
 */
public class AccountUtilTest extends TestCase {

  public void testAccountIdGeneratorIsSecureRandom() throws Exception {
    Field field = AccountUtil.class.getDeclaredField("random");
    field.setAccessible(true);
    assertTrue(field.get(null) instanceof SecureRandom);
  }
}
