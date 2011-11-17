// Copyright 2009 Google Inc. All Rights Reserved.
package openblocks.yacodeblocks;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Tests label filters.
 *
 *
 */
public class LabelFilterTest extends TestCase {

  private final List<String> legalNumbers = Arrays.asList("1", "-1", "1.0", "-1.0", ".1", "-.1");

  private final List<String> illegalNumbers = Arrays.asList("a", "+1", "1a", "1.a", " 1.3", "1 ");

  private final List<String> legalIdentifierNames = Arrays.asList("a", "aB", "a_b", "a_B", "Ab",
      "A9", "A9b_c");

  private final List<String> illegalIdentifierNames = Arrays.asList("", "_a", "9A", "ab0-", "-aB");

  public void testNumberFilter(){
    for (String legalNumber : legalNumbers) {
      assertTrue(NumberFilter.NUMBER_FILTER.isLegal(legalNumber));
    }
    for (String illegalNumber : illegalNumbers) {
      assertFalse(NumberFilter.NUMBER_FILTER.isLegal(illegalNumber));
    }
  }

  public void testIdentifierFilter(){
    for (String legalIdentifier : legalIdentifierNames) {
      assertTrue(IdentifierFilter.IDENTIFIER_FILTER.isLegal(legalIdentifier));
    }
    for (String illegalIdentifier : illegalIdentifierNames) {
      assertFalse(IdentifierFilter.IDENTIFIER_FILTER.isLegal(illegalIdentifier));
    }
  }

}
