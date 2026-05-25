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

  public void testNumbersTutorialContentPages() {
    String html = "<div class=\"tutorialContainer\">"
        + "<div class=\"tutorialContentPage\"><h3>Intro</h3><p>First page</p></div>"
        + "<div class=\"tutorialContentPage\"><h3>Setup</h3><p>Second page</p></div>"
        + "</div>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("--- Step 1 ---"));
    assertTrue(result.contains("--- Step 2 ---"));
    assertTrue(result.indexOf("Step 1") < result.indexOf("Step 2"));
  }

  public void testMarksHintButtons() {
    String html = "<div class=\"hintContainer\">"
        + "<button>Give me a hint</button>"
        + "<div class=\"hint hideHint\"><p>Try using a loop</p></div>"
        + "</div>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("[Hint]"));
    assertTrue(result.contains("Try using a loop"));
  }

  public void testMarksSolutionButtons() {
    String html = "<div class=\"hintContainer\">"
        + "<button>Check your solution</button>"
        + "<div class=\"hint hideHint\"><p>answer here</p></div>"
        + "</div>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("[Solution]"));
    assertTrue(result.contains("answer here"));
  }

  public void testPreservesImageAltText() {
    String html = "<p>See the diagram:</p>"
        + "<img class=\"enlargeImage\" src=\"img/diagram.png\" alt=\"Markov matrix diagram\" />"
        + "<p>Next step</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("[Image: Markov matrix diagram]"));
    assertFalse(result.contains("src="));
  }

  public void testRemovesImagesWithoutAlt() {
    String html = "<p>Before</p><img src=\"spacer.gif\" /><p>After</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Before"));
    assertTrue(result.contains("After"));
    assertFalse(result.contains("[Image"));
    assertFalse(result.contains("spacer"));
  }

  public void testConvertsHeadingsToMarkdownStyle() {
    String html = "<h3>Machine Learning</h3><p>content</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("### Machine Learning"));
  }

  public void testConvertsListItemsToBulletPoints() {
    String html = "<ul><li>First item</li><li>Second item</li></ul>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("- First item"));
    assertTrue(result.contains("- Second item"));
  }

  public void testNestedHintContainers() {
    String html = "<div class=\"hintContainer\">"
        + "<button>Give me a hint</button>"
        + "<div class=\"hint hideHint\">"
        + "<p>Think about lists</p>"
        + "<div class=\"hintContainer\">"
        + "<button>Check your solution</button>"
        + "<div class=\"hint hideHint\">"
        + "<img alt=\"solution screenshot\" src=\"sol.png\" />"
        + "</div></div></div></div>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("[Hint]"));
    assertTrue(result.contains("Think about lists"));
    assertTrue(result.contains("[Solution]"));
    assertTrue(result.contains("[Image: solution screenshot]"));
  }

  public void testFullTutorialPageStructure() {
    String html = "<html><head><title>Tutorial</title></head><body>"
        + "<div id=\"youthRadioAccordion\">"
        + "<h3>The Challenge</h3><div><p>Introduction text</p></div>"
        + "<h3>Main Tutorial</h3><div>"
        + "<div class=\"tutorialContainer\">"
        + "<div class=\"tutorialContentPage\"><h3>Intro</h3>"
        + "<p>Welcome to the tutorial</p></div>"
        + "<div class=\"tutorialContentPage\"><h3>UI Setup</h3>"
        + "<p>Add a button</p>"
        + "<div class=\"hintContainer\"><button>Give me a hint</button>"
        + "<div class=\"hint hideHint\"><p>Use the palette</p></div></div>"
        + "</div></div></div></div>"
        + "<script>$(document).ready()</script>"
        + "</body></html>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("--- Step 1 ---"));
    assertTrue(result.contains("--- Step 2 ---"));
    assertTrue(result.contains("### Intro"));
    assertTrue(result.contains("Welcome to the tutorial"));
    assertTrue(result.contains("### UI Setup"));
    assertTrue(result.contains("[Hint]"));
    assertTrue(result.contains("Use the palette"));
    assertFalse(result.contains("$(document)"));
    assertFalse(result.contains("<title>"));
  }
}
