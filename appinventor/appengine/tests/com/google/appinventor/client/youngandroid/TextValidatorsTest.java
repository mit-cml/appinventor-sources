// Copyright 2009 Google Inc. All Rights Reserved.

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
