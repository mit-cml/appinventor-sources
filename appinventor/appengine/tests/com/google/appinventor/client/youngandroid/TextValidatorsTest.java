// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
                                                                    "A b", " ", "foo bar");

  public void testIdentifierFilter(){
    for (String legalIdentifier : legalIdentifierNames) {
      assertTrue(TextValidators.isValidIdentifier(legalIdentifier));
    }
    for (String illegalIdentifier : illegalIdentifierNames) {
      assertFalse(TextValidators.isValidIdentifier(illegalIdentifier));
    }
  }

}
