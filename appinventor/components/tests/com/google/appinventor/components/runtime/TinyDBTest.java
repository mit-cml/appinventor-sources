package com.google.appinventor.components.runtime;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for TinyDB component.
 */
public class TinyDBTest extends RobolectricTestBase {

  private TinyDB aTinyDB;

  @Before
  public void SetUp() {
    super.setUp();
    aTinyDB = new TinyDB(getForm());
    aTinyDB.StoreValue("test-tag-1", "test-value-1");
    aTinyDB.StoreValue("test-tag-2", "test-value-2");
  }

  @Test
  public void testTinyDBDefault() {
    assertEquals("Expected default TinyDB Namespace"+TinyDB.DEFAULT_NAMESPACE, TinyDB.DEFAULT_NAMESPACE, aTinyDB.Namespace());
  }

  @Test
  public void testNameSpace() {
    aTinyDB.Namespace("MIT TinyDB");
    assertEquals("Invalid NameSpace Name", "MIT TinyDB", aTinyDB.Namespace());
  }

  @Test
  public void testClearAll()  {
    List<String> keyList = new ArrayList<>();
    aTinyDB.ClearAll();
    assertEquals("Invalid TinyDB tags", keyList, aTinyDB.GetTags());
  }

  @Test
  public void testClearTag()  {
    aTinyDB.ClearTag("test-tag-1");
    assertEquals("Invalid TinyDB ClearTag", "tag-not-found", aTinyDB.GetValue("test-tag-1", "tag-not-found"));
    assertFalse(aTinyDB.GetTags().toString().contains("test-tag-1"));
  }

  @Test
  public void testGetTags()  {
    List<String> keyList = new ArrayList<>();
    keyList.add("test-tag-1");
    keyList.add("test-tag-2");
    assertEquals("Invalid TinyDB GetTags", keyList, aTinyDB.GetTags());
  }

  @Test
  public void testGetValue()  {
    assertEquals("Invalid TinyDB GetValue", "test-value-1", aTinyDB.GetValue("test-tag-1", "tag-not-found"));
    assertEquals("Invalid TinyDB GetValue", "test-value-2", aTinyDB.GetValue("test-tag-2", "tag-not-found"));
  }

  @Test
  public void testStoreValue()  {
    aTinyDB.StoreValue("test-tag-3", "test-value-3");
    assertTrue(aTinyDB.GetTags().toString().contains("test-tag-3"));
    assertEquals("Invalid TinyDB StoreValue","test-value-3", aTinyDB.GetValue("test-tag-3", "tag-not-found"));
  }
}
