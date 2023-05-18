// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static com.google.appinventor.components.runtime.util.YailDictionary.ALL;
import static java.util.Arrays.asList;
import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.errors.DispatchableError;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import gnu.lists.FString;
import gnu.lists.LList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for the YailDictionary datatype.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest="tests/AndroidManifest.xml")
public class YailDictionaryTest {

  private static final String TEST_JSON = "{\"foo\":[{\"bar\":[0,{\"baz\":true}]}], \"num\": 1}";

  @Test
  public void testDictCopy() throws JSONException {
    YailDictionary dict1 = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    YailDictionary dict2 = new YailDictionary(dict1);
    assertNotSame(dict1, dict2);
    assertEquals(dict1, dict2);
    dict2.put("test", "value");
    //noinspection SimplifiableJUnitAssertion
    assertFalse(dict2.equals(dict1));
  }

  @Test
  public void testConstructors() {
    assertEquals(0, YailDictionary.makeDictionary().size());
    YailDictionary dict1 = YailDictionary.makeDictionary();
    YailDictionary dict2 = YailDictionary.makeDictionary(dict1);
    assertNotSame(dict2, dict1);
    assertEquals(dict2, dict1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMakerNeedsEvenNumberOfArguments() {
    YailDictionary.makeDictionary("foo");
  }

  @Test
  public void testDictListConstructor() {
    YailList data = getTestList();
    //noinspection unchecked
    YailDictionary dict = YailDictionary.makeDictionary(list(((LList) data.getCdr()).elements()));
    YailDictionary target = getTestDict();
    assertEquals(target, dict);
  }

  @Test
  public void testDictParsing() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
  }

  @Test
  public void testRecursiveGetSuccess() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    Object answer = dict.getObjectAtKeyPath(Arrays.asList((Object) "foo", 1, "bar", 2, "baz"));
    assertNotNull(answer);
    assertEquals(Boolean.TRUE, answer);
  }

  @Test
  public void testRecursiveGetFail() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    Object answer = dict.getObjectAtKeyPath(singletonList((Object) "Not Present"));
    assertNull(answer);
  }

  @Test
  public void testRecursiveSetSuccess() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    dict.setValueForKeyPath(Arrays.asList((Object) "foo", 1, "bar", 2, "baz"), "passed");
    Object answer = dict.getObjectAtKeyPath(Arrays.asList((Object) "foo", 1, "bar", 2, "baz"));
    assertEquals("passed", answer);
    dict.setValueForKeyPath(Arrays.asList((Object) "foo", 1, "bar", 1), "passed");
    answer = dict.getObjectAtKeyPath(Arrays.asList((Object) "foo", 1, "bar", 1));
    assertEquals("passed", answer);
  }

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetObjectFallthrough() {
    // This test is really just for coverage purposes. The behavior that is tested in theory
    // should never happen (queue story about for(;;)).
    YailDictionary test = new YailDictionary() {
      @Override
      public int size() {
        return Integer.MAX_VALUE - 1;
      }
    };
    test.getObject(1);
  }

  @Test
  public void testLookupTargetForKeyPath() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    dict.getObjectAtKeyPath(Arrays.asList((Object) "num", 1));
  }

  @Test
  public void testRecursiveSetNoop() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    YailDictionary dict2 = YailDictionary.makeDictionary(dict);
    dict.setValueForKeyPath(Lists.newArrayList(), "foo");
    assertNotSame(dict2, dict);
    assertEquals(dict2, dict);
  }

  @Test(expected = DispatchableError.class)
  public void testRecursiveSetFail() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    dict.setValueForKeyPath(Arrays.asList((Object) "num", "bad"), true);
  }

  @Test(expected = DispatchableError.class)
  public void testRecursiveSetFail2() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    dict.setValueForKeyPath(Arrays.asList((Object) "foo", "bad"), true);
  }

  @Test(expected = DispatchableError.class)
  public void testRecursiveSetFailOutOfBounds() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    dict.setValueForKeyPath(Arrays.asList((Object) "foo", -1), false);
  }

  @Test
  public void testFString() {
    YailDictionary target = new YailDictionary();
    target.put("key", "value");
    target.put(new FString("fstringkey"), new FString("fstringvalue"));
    assertTrue(target.containsKey(new FString("key")));
    assertTrue(target.containsValue(new FString("value")));
    assertTrue(target.containsKey("fstringkey"));
    assertTrue(target.containsValue("fstringvalue"));
    assertEquals("value", target.get("key"));
    assertEquals("value", target.get(new FString("key")));
    assertEquals("fstringvalue", target.get("fstringkey"));
    assertEquals("fstringvalue", target.get(new FString("fstringkey")));
    target.remove(new FString("key"));
    target.remove(new FString("fstringkey"));
    assertTrue(target.isEmpty());
  }

  @Test
  public void testToString() {
    YailDictionary dict = YailDictionary.makeDictionary();
    assertEquals("{}", dict.toString());
    dict.put("foo", "bar");
    assertEquals("{\"foo\":\"bar\"}", dict.toString());
    dict.put("baz", "bop");
    // Since YailDictionary is a LinkedHashMap, it will remember the order in which
    // the keys have been added.
    assertEquals("{\"foo\":\"bar\",\"baz\":\"bop\"}", dict.toString());
  }

  @Test(expected = YailRuntimeError.class)
  public void testToStringThrows() {
    YailDictionary dict = new YailDictionary();
    dict.put("foo", Double.NaN);  // NaN isn't allowed in JSON so toString will throw
    System.err.println(dict.toString());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetObjectThrowsNegative() {
    YailDictionary dict = YailDictionary.makeDictionary();
    dict.getObject(-1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetObjectThrowsPositive() {
    YailDictionary dict = YailDictionary.makeDictionary();
    dict.getObject(1);
  }

  @Test
  public void testGetObject() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    assertEquals(Arrays.asList("num", 1), dict.getObject(1));
  }

  @Test
  public void testYailIterator() throws JSONException {
    YailDictionary dict = (YailDictionary) JsonUtil.getObjectFromJson(TEST_JSON, true);
    assertNotNull(dict);
    Iterator<YailList> it = dict.iterator();
    while (it.hasNext()) {
      YailList list = it.next();
      if (list.getObject(1).equals(1)) {
        it.remove();
      }
    }
    assertEquals(1, dict.size());
  }

  @Test
  public void testAlistToDictEmpty() {
    YailList list = YailList.makeEmptyList();
    assertEquals(new YailDictionary(), YailDictionary.alistToDict(list));
  }

  @Test
  public void testAlistToDict() {
    YailList list = getTestList();
    YailDictionary target = getTestDict();
    assertEquals(target, YailDictionary.alistToDict(list));
  }

  @Test
  public void testDictToAlist() {
    YailList target = getDictToListTestList();
    YailDictionary dict = getTestDict();
    assertEquals(target, YailDictionary.dictToAlist(dict));
  }

  @Test
  public void testSetList() {
    YailList pair = YailList.makeList(new Object[] { "a", "b" });
    YailDictionary dict = new YailDictionary();
    dict.setPair(pair);
    assertFalse(dict.isEmpty());
    assertEquals(1, dict.size());
    assertEquals("b", dict.get("a"));
  }

  @Test
  public void testRecursiveGet2() {
    YailDictionary dict = new YailDictionary();
    dict.put("foo", singletonList("bar"));
    assertEquals("bar", dict.getObjectAtKeyPath(asList((Object) "foo", new FString("1"))));
    assertEquals("bar", dict.getObjectAtKeyPath(asList((Object) "foo", "1")));
  }

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @Test(expected = YailRuntimeError.class)
  public void testRecursiveGetBadIndex() {
    YailDictionary dict = new YailDictionary();
    dict.put("foo", singletonList("bar"));
    dict.getObjectAtKeyPath(asList((Object) "foo", 2));
  }

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @Test(expected = YailRuntimeError.class)
  public void testRecursiveGetBadKey() {
    YailDictionary dict = new YailDictionary();
    dict.put("foo", singletonList("bar"));
    dict.getObjectAtKeyPath(asList((Object) "foo", "this is not a valid list index"));
  }

  @Test
  public void testRecursiveGetInvalidKeyType() {
    YailDictionary dict = new YailDictionary();
    dict.put("foo", singletonList("bar"));
    assertNull(dict.getObjectAtKeyPath(asList("foo", new Object())));
  }

  @Test
  public void testAlistInRecursiveGet() {
    YailDictionary dict = new YailDictionary();
    dict.put("foo", YailList.makeList(new Object[] {
        YailList.makeList(asList("a", 1)),
        YailList.makeList(asList("b", 2))
    }));
    assertEquals(1, dict.getObjectAtKeyPath(asList("foo", "a")));
    assertEquals(2, dict.getObjectAtKeyPath(asList("foo", "b")));
  }

  @Test
  public void testRecursiveGetList() {
    YailDictionary person1 = new YailDictionary();
    person1.put("first", "John");
    person1.put("middle", "Q");
    person1.put("last", "Smith");
    person1.put("address", YailDictionary.makeDictionary(
        "street", "Main Street",
        "city", "Cambridge",
        "state", "MA"
    ));
    YailDictionary person2 = new YailDictionary();
    person2.put("first", "Jane");
    person2.put("middle", "R");
    person2.put("last", "Smith");
    person2.put("address", YailDictionary.makeDictionary(
        "street", "Wall Street",
        "city", "New York",
        "state", "NY"
    ));
    YailDictionary person3 = new YailDictionary();
    person3.put("first", "John");
    person3.put("last", "Doe");
    person3.put("address", YailList.makeList(new Object[] {
        YailList.makeList(asList("street", "Market Street")),
        YailList.makeList(asList("city", "San Francisco")),
        YailList.makeList(asList("state", "CA"))
    }));
    YailList list = YailList.makeList(asList(person1, person2, person3));
    YailDictionary dict = new YailDictionary();
    dict.put("people", list);
    assertEquals(asList("John", "Jane", "John"),
        YailDictionary.walkKeyPath(dict, asList("people", ALL, "first")));
    assertEquals(asList("Q", "R"),
        YailDictionary.walkKeyPath(dict, asList("people", ALL, "middle")));
    assertEquals(asList("MA", "NY", "CA"),
        YailDictionary.walkKeyPath(dict, asList("people", ALL, "address", "state")));
    assertEquals(asList("Main Street", "Cambridge", "MA", "Wall Street", "New York", "NY",
        "Market Street", "San Francisco", "CA"),
        YailDictionary.walkKeyPath(dict, asList("people", ALL, "address", ALL)));
  }

  @Test
  public void testSetValueForKeyPath() {
    YailDictionary dict = getTestDict();
    dict.setValueForKeyPath(asList("list", 2), "passed");
    assertEquals("passed", dict.getObjectAtKeyPath(asList("list", 2)));
  }

  @Test(expected = DispatchableError.class)
  public void testSetValueForKeyPathFails() {
    YailDictionary dict = getTestDict();
    dict.setValueForKeyPath(asList("foo", "number", "invalid"), "fail");
  }

  @Test
  public void testRecursiveGetListEmpty() {
    YailDictionary dict = getTestDict();
    assertEquals(Collections.emptyList(), YailDictionary.walkKeyPath(dict, asList("bad", "path")));
  }

  private static YailList getTestList() {
    return YailList.makeList(new Object[] {
        YailList.makeList(new Object[] { "number", 1 }),
        YailList.makeList(new Object[] { "string", "foo" }),
        YailList.makeList(new Object[] { "empty-list", YailList.makeEmptyList() }),
        YailList.makeList(new Object[] { "list",
            YailList.makeList(new Object[] {
                YailList.makeList(new Object[] { "a", "b" }), 2, 3})}),
        YailList.makeList(new Object[] { "dict",
            YailDictionary.makeDictionary("a","b")}),
        YailList.makeList(new Object[] { "list-with-dict", 
            YailList.makeList(new Object[] {
                YailDictionary.makeDictionary("a","b")
            })})
    });
  }

  private static YailDictionary getTestDict() {
    YailList ablist = YailList.makeList(Arrays.asList("a", "b"));
    YailDictionary abdict = YailDictionary.makeDictionary("a", "b");
    YailDictionary target = new YailDictionary();
    target.put("number", 1);
    target.put("string", "foo");
    target.put("empty-list", YailList.makeEmptyList());
    target.put("list", YailList.makeList(asList(ablist, 2, 3)));
    target.put("dict", abdict);
    target.put("list-with-dict", YailList.makeList(singletonList(abdict)));
    return target;
  }

  private static YailList getDictToListTestList() {
    // Only the top level is converted to a list.
    YailList ablist = YailList.makeList(Arrays.asList("a", "b"));
    YailDictionary abdict = YailDictionary.makeDictionary("a", "b");
    return YailList.makeList(new Object[] {
         YailList.makeList(new Object[] { "number", 1 }),
         YailList.makeList(new Object[] { "string", "foo" }),
         YailList.makeList(new Object[] { "empty-list", YailList.makeEmptyList() }),
         YailList.makeList(new Object[] { "list",
             YailList.makeList(asList(ablist, 2, 3)) }),
         YailList.makeList(new Object[] { "dict", abdict }),
         YailList.makeList(new Object[] { "list-with-dict",
             YailList.makeList(singletonList(abdict)) })
    });
  }
}
