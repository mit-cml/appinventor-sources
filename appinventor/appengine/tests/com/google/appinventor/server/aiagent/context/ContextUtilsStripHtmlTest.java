// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import junit.framework.TestCase;

public class ContextUtilsStripHtmlTest extends TestCase {

  public void testStripsScriptElementsWithContents() {
    String html = "<p>Hello</p><script>var x = 1;</script><p>World</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Hello"));
    assertTrue(result.contains("World"));
    assertFalse(result.contains("var x"));
  }

  public void testStripsStyleElementsWithContents() {
    String html = "<p>Hello</p><style>.foo { color: red; }</style><p>World</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Hello"));
    assertFalse(result.contains("color"));
  }

  public void testStripsHeadElement() {
    String html = "<html><head><title>Test</title><meta charset='utf-8'></head>"
        + "<body><p>Content</p></body></html>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Content"));
    assertFalse(result.contains("Test"));
    assertFalse(result.contains("charset"));
  }

  public void testStripsNavFooterHeader() {
    String html = "<header><a>Site Logo</a></header>"
        + "<nav><a>Home</a><a>About</a></nav>"
        + "<main><p>Tutorial step 1</p></main>"
        + "<footer><p>Copyright 2025</p></footer>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Tutorial step 1"));
    assertFalse(result.contains("Site Logo"));
    assertFalse(result.contains("Home"));
    assertFalse(result.contains("Copyright"));
  }

  public void testPreservesParagraphStructure() {
    String html = "<p>First paragraph</p><p>Second paragraph</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("First paragraph"));
    assertTrue(result.contains("Second paragraph"));
    assertTrue(result.contains("\n"));
  }

  public void testCollapsesExcessiveBlankLines() {
    String html = "<p>A</p>\n\n\n\n\n<p>B</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertFalse(result.contains("\n\n\n"));
  }

  public void testDecodesHtmlEntities() {
    String html = "<p>Tom &amp; Jerry &lt;3&gt; &quot;friends&quot;</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Tom & Jerry <3> \"friends\""));
  }

  public void testStripsRemainingHtmlTags() {
    String html = "<div><h1>Title</h1><ul><li>Item 1</li><li>Item 2</li></ul></div>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Title"));
    assertTrue(result.contains("Item 1"));
    assertFalse(result.contains("<"));
  }

  public void testCaseInsensitiveTagRemoval() {
    String html = "<SCRIPT>alert('xss')</SCRIPT><P>Content</P>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Content"));
    assertFalse(result.contains("alert"));
  }

  public void testNullAndEmptyInput() {
    assertEquals("", ContextUtils.stripHtmlForTutorial(null));
    assertEquals("", ContextUtils.stripHtmlForTutorial(""));
  }
}
