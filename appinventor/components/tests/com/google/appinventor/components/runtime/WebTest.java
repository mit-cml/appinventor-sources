// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
  public void setUp() {
    web = new Web();
  }

  @Test
  public void testDecodeJsonText() {
    // String values.
    assertEquals("\t tab \t tab \t",
        Web.decodeJsonText("\"\\t tab \\t tab \\t\"", true));
    assertEquals("\n newline \n newline \n",
        Web.decodeJsonText("\"\\n newline \\n newline \\n\"", true));
    assertEquals("/ slash / slash /",
        Web.decodeJsonText("\"\\/ slash \\/ slash \\/\"", true));
    assertEquals("\\ backslash \\ backslash \\",
        Web.decodeJsonText("\"\\\\ backslash \\\\ backslash \\\\\"", true));
    assertEquals("\" quote \" quote \"",
        Web.decodeJsonText("\"\\\" quote \\\" quote \\\"\"", true));
    assertEquals("~ encoded tilda ~ encoded tilda ~",
        Web.decodeJsonText("\"\\u007E encoded tilda \\u007E encoded tilda \\u007E\"", true));

    // Boolean values.
    assertEquals(Boolean.TRUE, Web.decodeJsonText("True", true));
    assertEquals(Boolean.FALSE, Web.decodeJsonText("False", true));

    // Numeric values.
    assertEquals(1, Web.decodeJsonText("1", true));
    assertEquals(57.43, Web.decodeJsonText("57.43", true));

    // A JSON encoded object.
    Object decodedObject = Web.decodeJsonText("{\"YaVersion\":\"41\",\"Source\":\"Form\"}", true);
    assertTrue(decodedObject instanceof Map);
    YailDictionary outerList = (YailDictionary) decodedObject;
    assertEquals(2, outerList.size());
    // The items are sorted by the field name, so Source comes before YaVersion
    Iterator<Entry<Object, Object>> it = outerList.entrySet().iterator();
    Entry<Object, Object> item0 = it.next();
    assertEquals("Source", item0.getKey());
    assertEquals("Form", item0.getValue());
    Entry<Object, Object> item1 = it.next();
    assertEquals("YaVersion", item1.getKey());
    assertEquals("41", item1.getValue());

    // A JSON encoded array.
    Object decodedArray = Web.decodeJsonText("[\"Billy\",\"Sam\",\"Bobby\",\"Fred\"]", true);
    assertTrue(decodedArray instanceof ArrayList);
    @SuppressWarnings("unchecked")
    ArrayList<Object> list = (ArrayList<Object>) decodedArray;
    assertEquals(4, list.size());
    assertEquals("Billy", list.get(0));
    assertEquals("Sam", list.get(1));
    assertEquals("Bobby", list.get(2));
    assertEquals("Fred", list.get(3));

    try {
      Web.decodeJsonText("{\"not\":\"valid\":\"json\"}", true);
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
    ArrayList<?> outerList = (ArrayList<?>) decodedObject;
    assertEquals(1, outerList.size());
    Object pairObject = outerList.get(0);
    assertTrue(pairObject instanceof ArrayList);
    ArrayList<?> pair = (ArrayList<?>) pairObject;
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
    ArrayList<?> outerList = (ArrayList<?>) decodedObject;
    assertEquals(1, outerList.size());
    Object pairObject = outerList.get(0);
    assertTrue(pairObject instanceof ArrayList);
    ArrayList<?> pair = (ArrayList<?>) pairObject;
    assertEquals(2, pair.size());
    assertEquals("a", pair.get(0));
    Object XObject = pair.get(1);
    assertTrue(XObject instanceof ArrayList);
    ArrayList<?> X = (ArrayList<?>) XObject;
    assertEquals(2, X.size());
    Object firstPairObject = X.get(0);
    Object secondPairObject = X.get(1);
    assertTrue(firstPairObject instanceof ArrayList);
    assertTrue(secondPairObject instanceof ArrayList);
    ArrayList<?> firstPair = (ArrayList<?>) firstPairObject;
    ArrayList<?> secondPair = (ArrayList<?>) secondPairObject;
    assertEquals("bar", firstPair.get(0));
    assertEquals(456, firstPair.get(1));
    assertEquals("foo", secondPair.get(0));
    assertEquals("1 2 3", secondPair.get(1));
  }

  @Test
  public void testDecodeXMLCDATA() {
    Object decodedObject = web.XMLTextDecode("<xml><![CDATA[foo < bar || bar > baz]]></xml>");
    assertTrue(decodedObject instanceof List);
    List<?> outerList = (List<?>) decodedObject;
    assertEquals(1, outerList.size());
    assertTrue(outerList.get(0) instanceof List);
    List<?> tagValuePair = (List<?>) ((List<?>) decodedObject).get(0);
    assertEquals(2, tagValuePair.size());
    // tag should be xml
    assertEquals("xml", tagValuePair.get(0));
    // value should be decoded CDATA
    assertEquals("foo < bar || bar > baz", tagValuePair.get(1));
  }

  @Test
  public void testDecodeXMLTextAsDictionary() {
    Object decodedObject = web.XMLTextDecodeAsDictionary("<foo>123</foo>");
    // Should be a dictionary mapping "foo" to 123
    assertTrue(decodedObject instanceof YailDictionary);
    YailDictionary outerList = (YailDictionary) decodedObject;
    // Dictionary has fields $tag, $namespaceUri, $localName, $namespace, $attributes, $content
    assertEquals(6, outerList.size());
    assertEquals("foo", outerList.get("$tag"));
    assertTrue(outerList.get("$content") instanceof List);
    assertEquals(YailList.makeList(new Object[] { "123" }), outerList.get("$content"));
  }

  @Test
  public void testDecodeXMLTextAsDictionary2() {
    Object decodedObject = web.XMLTextDecodeAsDictionary("<a><foo>1 2 3</foo><bar>456</bar></a>");
    // should be the list of one element, which is a pair of "a" and a list X.
    // X is a list two pairs.  The first pair is "bar" and 456 and the second pair is
    // "foo" and the string "1 2 3".
    // The order of these is bar before foo because it's alphabetical by according to
    // the tags.
    assertTrue(decodedObject instanceof YailDictionary);
    YailDictionary result  = (YailDictionary) decodedObject;
    assertEquals(8, result.size());
    assertTrue(result.containsKey("foo"));
    assertTrue(result.get("foo") instanceof YailList);
    assertTrue(result.containsKey("bar"));
    assertTrue(result.get("bar") instanceof YailList);
    assertEquals(2, ((YailList) result.get("$content")).size());
    assertEquals("1 2 3", result.getObjectAtKeyPath(Arrays.asList("foo", 1, "$content", 1)));
    assertEquals("456", result.getObjectAtKeyPath(Arrays.asList("bar", 1, "$content", 1)));
  }

  @Test
  public void testBuildRequestData() throws Exception {
    List<Object> list = new ArrayList<>();
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
