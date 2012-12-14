// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import junit.framework.TestCase;

/**
 * Tests Escapers.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class EscapersTest extends TestCase {
  public void testEscapeForXml() throws Exception {
    assertEquals("ampersand &amp; less than &lt; greater than &gt; FTW",
        Escapers.escapeForXml("ampersand & less than < greater than > FTW"));
  }

  public void testEncodeInternationalCharacters() throws Exception {
    // Pet the Kitty with i18n characters
    checkEncodeAndDecode("\u1e56\u00e9\u1e97 \u0167\u1e27\u00eb \u1e32\u01d0\u1e6b\u1e71\u1e99");
    checkEncodeAndDecode("Tab\tNewline\nBackslash\\Quote\"Slash/Tilda~Space ");
  }

  private void checkEncodeAndDecode(String original) throws Exception {
    String encoded = Escapers.encodeInternationalCharacters(original);
    String decoded = Escapers.decodeInternationalCharacters(encoded);
    assertEquals(original, decoded);
  }
}
