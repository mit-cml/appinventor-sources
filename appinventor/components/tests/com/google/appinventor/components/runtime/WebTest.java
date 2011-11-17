// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;


/**
 * Tests Web.java.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class WebTest extends TestCase {
  private Web web;

  @Override
  protected void setUp() throws Exception {
    web = new Web();
  }

  public void testDecodeJsonText() throws Exception {
    // String values.
    assertEquals("\t tab \t tab \t",
        web.decodeJsonText("\"\\t tab \\t tab \\t\""));
    assertEquals("\n newline \n newline \n",
        web.decodeJsonText("\"\\n newline \\n newline \\n\""));
    assertEquals("/ slash / slash /",
        web.decodeJsonText("\"\\/ slash \\/ slash \\/\""));
    assertEquals("\\ backslash \\ backslash \\",
        web.decodeJsonText("\"\\\\ backslash \\\\ backslash \\\\\""));
    assertEquals("\" quote \" quote \"",
        web.decodeJsonText("\"\\\" quote \\\" quote \\\"\""));
    assertEquals("~ encoded tilda ~ encoded tilda ~",
        web.decodeJsonText("\"\\u007E encoded tilda \\u007E encoded tilda \\u007E\""));
    assertEquals("A normal string without quotes.",
        web.decodeJsonText("A normal string without quotes."));

    // Boolean values.
    assertEquals(Boolean.TRUE, web.decodeJsonText("True"));
    assertEquals(Boolean.FALSE, web.decodeJsonText("False"));

    // Numeric values.
    assertEquals(new Integer(1), web.decodeJsonText("1"));
    assertEquals(new Double(57.43), web.decodeJsonText("57.43"));

    // A JSON encoded object.
    Object decodedObject = web.decodeJsonText("{\"YaVersion\":\"41\",\"Source\":\"Form\"}");
    assertTrue(decodedObject instanceof ArrayList);
    ArrayList outerList = (ArrayList) decodedObject;
    assertEquals(2, outerList.size());
    // The items are sorted by the field name, so Source comes before YaVersion
    Object item0 = outerList.get(0);
    assertTrue(item0 instanceof ArrayList);
    ArrayList firstNameValuePair = (ArrayList) item0;
    assertEquals(2, firstNameValuePair.size());
    assertEquals("Source", firstNameValuePair.get(0));
    assertEquals("Form", firstNameValuePair.get(1));
    Object item1 = outerList.get(1);
    assertTrue(item1 instanceof ArrayList);
    ArrayList secondNameValuePair = (ArrayList) item1;
    assertEquals(2, secondNameValuePair.size());
    assertEquals("YaVersion", secondNameValuePair.get(0));
    assertEquals("41", secondNameValuePair.get(1));

    // A JSON encoded array.
    Object decodedArray = web.decodeJsonText("[\"Billy\",\"Sam\",\"Bobby\",\"Fred\"]");
    assertTrue(decodedArray instanceof ArrayList);
    ArrayList list = (ArrayList) decodedArray;
    assertEquals(4, list.size());
    assertEquals("Billy", list.get(0));
    assertEquals("Sam", list.get(1));
    assertEquals("Bobby", list.get(2));
    assertEquals("Fred", list.get(3));

    try {
      web.decodeJsonText("{\"not\":\"valid\":\"json\"}");
      fail();
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }

  public void testbuildPostData() throws Exception {
    List<Object> list = new ArrayList<Object>();
    list.add(YailList.makeList(new String[] { "First Name", "Barack" }));
    list.add(YailList.makeList(new String[] { "Last Name", "Obama" }));
    list.add(YailList.makeList(new String[] { "Title", "President of the United States" }));
    assertEquals("First+Name=Barack&Last+Name=Obama&Title=President+of+the+United+States",
        web.buildPostData(YailList.makeList(list)));

    list.clear();
    list.add(YailList.makeList(new String[] { "First Name", "Barack" }));
    list.add("This is not a list!");
    list.add(YailList.makeList(new String[] { "Last Name", "Obama" }));
    list.add(YailList.makeList(new String[] { "Title", "President of the United States" }));
    try {
      web.buildPostData(YailList.makeList(list));
      fail();
    } catch (Web.BuildPostDataException e) {
      assertEquals(ErrorMessages.ERROR_WEB_BUILD_POST_DATA_NOT_LIST, e.errorNumber);
      assertEquals(2, e.index);
    }

    list.clear();
    list.add(YailList.makeList(new String[] { "First Name", "Barack" }));
    list.add(YailList.makeList(new String[] { "Last Name", "Obama" }));
    list.add(YailList.makeList(new String[] { "Title", "President of the United States",
        "This list has too many items" }));
    try {
      web.buildPostData(YailList.makeList(list));
      fail();
    } catch (Web.BuildPostDataException e) {
      assertEquals(ErrorMessages.ERROR_WEB_BUILD_POST_DATA_NOT_TWO_ELEMENTS, e.errorNumber);
      assertEquals(3, e.index);
    }

    list.clear();
    list.add(YailList.makeList(new String[] { "First Name", "Barack" }));
    list.add(YailList.makeList(new String[] { "Last Name", "Obama" }));
    list.add(YailList.makeList(new String[] { "Title", "President of the United States" }));
    list.add(YailList.makeList(new String[] { "This list has too few items" }));
    try {
      web.buildPostData(YailList.makeList(list));
      fail();
    } catch (Web.BuildPostDataException e) {
      assertEquals(ErrorMessages.ERROR_WEB_BUILD_POST_DATA_NOT_TWO_ELEMENTS, e.errorNumber);
      assertEquals(4, e.index);
    }
  }
}
