// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import junit.framework.TestCase;

/**
 * Tests HtmlEntities.java.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class HtmlEntitiesTest extends TestCase {
  public void testdecodeHtmlText() throws Exception {
    // Empty string
    assertEquals("",
        HtmlEntities.decodeHtmlText(""));

    // No entities
    assertEquals("No entities",
        HtmlEntities.decodeHtmlText("No entities"));

    // Recognized entities
    assertEquals("< less than < less than <",
        HtmlEntities.decodeHtmlText("&lt; less than &lt; less than &lt;"));
    assertEquals("> greater than > greater than >",
        HtmlEntities.decodeHtmlText("&gt; greater than &gt; greater than &gt;"));
    assertEquals("& ampersand & ampersand &",
        HtmlEntities.decodeHtmlText("&amp; ampersand &amp; ampersand &amp;"));
    assertEquals("' apostrophe ' apostrophe '",
        HtmlEntities.decodeHtmlText("&apos; apostrophe &apos; apostrophe &apos;"));
    assertEquals("\" quotes \" quotes \"",
        HtmlEntities.decodeHtmlText("&quot; quotes &quot; quotes &quot;"));

    // Numeric entities
    assertEquals("' apostrophe ' apostrophe '",
        HtmlEntities.decodeHtmlText("&#39; apostrophe &#39; apostrophe &#39;"));
    assertEquals("' apostrophe ' apostrophe '",
        HtmlEntities.decodeHtmlText("&#x27; apostrophe &#x27; apostrophe &#x27;"));

    // Unrecognized entities are not decoded. No exception is thrown.
    assertEquals("No semicolon after ampersand &",
        HtmlEntities.decodeHtmlText("No semicolon after ampersand &"));
    assertEquals("&; not decoded &; not decoded &;",
        HtmlEntities.decodeHtmlText("&; not decoded &; not decoded &;"));
    assertEquals("&abc; not decoded &abc; not decoded &abc;",
      HtmlEntities.decodeHtmlText("&abc; not decoded &abc; not decoded &abc;"));
    assertEquals("&#12A not decoded &#12A not decoded &#12A",  // illegal decimal value
        HtmlEntities.decodeHtmlText("&#12A not decoded &#12A not decoded &#12A"));
    assertEquals("&#x12G; not decoded &#x12G; not decoded &#x12G;",  // illegal hex value
        HtmlEntities.decodeHtmlText("&#x12G; not decoded &#x12G; not decoded &#x12G;"));
  }
}
