// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.youngandroid;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class TextValidatorsTest extends TestCase {

  private final List<String> legalIdentifierNames = Arrays.asList("a", "aB", "a_b", "a_B", "Ab",
                                                                  "A9", "A9b_c");

  private final List<String> illegalIdentifierNames = Arrays.asList("", "_a", "9A", "ab0-", "-aB",
                                                                    "A b", " ", "foo bar", "foo,bar");

  private final List<String> legalComponentIdentifierNames = Arrays.asList("你好吗", "按钮1", "图片2",
		  "图_片2", "图3片_2", "botón1", "botón_2");

  private final List<String> illegalComponentIdentifierNames = Arrays.asList("", "!你好吗", "2你好吗",
		  "123按_钮", "1按2钮 ", "1botón", "!botón2");

  public void testIdentifierFilter(){
    for (String legalIdentifier : legalIdentifierNames) {
      assertTrue(TextValidators.isValidIdentifier(legalIdentifier));
    }
    for (String illegalIdentifier : illegalIdentifierNames) {
      assertFalse(TextValidators.isValidIdentifier(illegalIdentifier));
    }
  }

  public void testComponentIdentifierFilter(){
	    for (String legalComponentIdentifier : legalComponentIdentifierNames) {
	      assertTrue(TextValidators.isValidComponentIdentifier(legalComponentIdentifier));
	    }
	    for (String illegalComponentIdentifier : illegalComponentIdentifierNames) {
	      assertFalse(TextValidators.isValidComponentIdentifier(illegalComponentIdentifier));
	    }
  }
}
