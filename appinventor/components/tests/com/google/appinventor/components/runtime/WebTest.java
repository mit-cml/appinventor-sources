// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests Web.java.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest="tests/AndroidManifest.xml")
public class WebTest {
  private Web web;

  @Before
  public void setUp() throws Exception {
    web = new Web();
  }

  @Test
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

  @Test
  public void testDecodeXMLText() throws Exception {
    Object decodedObject = web.XMLTextDecode("<foo>123</foo>");
    // should be the list of one element, which is a pair of "foo" and 123
    assertTrue(decodedObject instanceof ArrayList);
    ArrayList outerList = (ArrayList) decodedObject;
    assertEquals(1, outerList.size());
    Object pairObject = outerList.get(0);
    assertTrue(pairObject instanceof ArrayList);
    ArrayList pair = (ArrayList) pairObject;
    assertEquals(2, pair.size());
    assertEquals("foo", pair.get(0));
    // check why this isn't actually the string 123
    assertEquals(123, pair.get(1));
  }

  @Test
  public void testDecodeXMLText2() throws Exception {
    Object decodedObject = web.XMLTextDecode("<a><foo>1 2 3</foo><bar>456</bar></a>");
    // should be the list of one element, which is a pair of "a" and a list X.
    // X is a list two pairs.  The first pair is "bar" and 456 and the second pair is
    // "foo" and the string "1 2 3".
    // The order of these is bar before foo because it's alphabetical by according to
    // the tags.
    assertTrue(decodedObject instanceof ArrayList);
    ArrayList outerList = (ArrayList) decodedObject;
    assertEquals(1, outerList.size());
    Object pairObject = outerList.get(0);
    assertTrue(pairObject instanceof ArrayList);
    ArrayList pair = (ArrayList) pairObject;
    assertEquals(2, pair.size());
    assertEquals("a", pair.get(0));
    Object XObject = pair.get(1);
    assertTrue(XObject instanceof ArrayList);
    ArrayList X = (ArrayList) XObject;
    assertEquals(2, X.size());
    Object firstPairObject = X.get(0);
    Object secondPairObject = X.get(1);
    assertTrue(firstPairObject instanceof ArrayList);
    assertTrue(secondPairObject instanceof ArrayList);
    ArrayList firstPair = (ArrayList) firstPairObject;
    ArrayList secondPair = (ArrayList) secondPairObject;
    assertEquals("bar", firstPair.get(0));
    assertEquals(456, firstPair.get(1));
    assertEquals("foo", secondPair.get(0));
    assertEquals("1 2 3", secondPair.get(1));
  }

  @Test
  public void testDecodeXMLCDATA() throws Exception {
    Object decodedObject = web.XMLTextDecode("<xml><![CDATA[foo < bar || bar > baz]]></xml>");
    assertTrue(decodedObject instanceof List);
    List outerList = (List) decodedObject;
    assertEquals(1, outerList.size());
    assertTrue(outerList.get(0) instanceof List);
    List tagValuePair = (List) ((List) decodedObject).get(0);
    assertEquals(2, tagValuePair.size());
    // tag should be xml
    assertEquals("xml", tagValuePair.get(0));
    // value should be decoded CDATA
    assertEquals("foo < bar || bar > baz", tagValuePair.get(1));
  }

  @Test
  public void testbuildRequestData() throws Exception {
    List<Object> list = new ArrayList<Object>();
    list.add(YailList.makeList(new String[] { "First Name", "Barack" }));
    list.add(YailList.makeList(new String[] { "Last Name", "Obama" }));
    list.add(YailList.makeList(new String[] { "Title", "President of the United States" }));
    assertEquals("First+Name=Barack&Last+Name=Obama&Title=President+of+the+United+States",
        web.buildRequestData(YailList.makeList(list)));

    list.clear();
    list.add(YailList.makeList(new String[] { "First Name", "Barack" }));
    list.add("This is not a list!");
    list.add(YailList.makeList(new String[] { "Last Name", "Obama" }));
    list.add(YailList.makeList(new String[] { "Title", "President of the United States" }));
    try {
      web.buildRequestData(YailList.makeList(list));
      fail();
    } catch (Web.BuildRequestDataException e) {
      assertEquals(ErrorMessages.ERROR_WEB_BUILD_REQUEST_DATA_NOT_LIST, e.errorNumber);
      assertEquals(2, e.index);
    }

    list.clear();
    list.add(YailList.makeList(new String[] { "First Name", "Barack" }));
    list.add(YailList.makeList(new String[] { "Last Name", "Obama" }));
    list.add(YailList.makeList(new String[] { "Title", "President of the United States",
        "This list has too many items" }));
    try {
      web.buildRequestData(YailList.makeList(list));
      fail();
    } catch (Web.BuildRequestDataException e) {
      assertEquals(ErrorMessages.ERROR_WEB_BUILD_REQUEST_DATA_NOT_TWO_ELEMENTS, e.errorNumber);
      assertEquals(3, e.index);
    }

    list.clear();
    list.add(YailList.makeList(new String[] { "First Name", "Barack" }));
    list.add(YailList.makeList(new String[] { "Last Name", "Obama" }));
    list.add(YailList.makeList(new String[] { "Title", "President of the United States" }));
    list.add(YailList.makeList(new String[] { "This list has too few items" }));
    try {
      web.buildRequestData(YailList.makeList(list));
      fail();
    } catch (Web.BuildRequestDataException e) {
      assertEquals(ErrorMessages.ERROR_WEB_BUILD_REQUEST_DATA_NOT_TWO_ELEMENTS, e.errorNumber);
      assertEquals(4, e.index);
    }
  }
}
