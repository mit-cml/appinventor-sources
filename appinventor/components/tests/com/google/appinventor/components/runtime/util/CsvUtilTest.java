// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.util.YailList;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Test CsvUtil methods (converting between YailLists and CSV-formatted strings.
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class CsvUtilTest extends TestCase {
  
  public void testFromCSVTableBasic() throws Exception {
    String testString =
      "column id,type,name\r\n" +
      "col0,animal,string\r\n" + 
      "col1,favorite_food,string\r\n" +
      "col4,population size,number\r\n" +
      "col5,location,location\r\n" +
      "col6,timestamp,datetime\r\n";
    YailList ylist = CsvUtil.fromCsvTable(testString);
    for (Object o : ylist.toArray()) {
      YailList row = (YailList) o;
      assertEquals(3, row.size());
    }
  }
  
  public void testFromCSVTableEmbeddedDoubleQuotes() throws Exception {
    String testString =
      "column id,type,\"nam\"\"ex\"\r\n" +
      "col0,animal,\"nam\"\"e1x\"\r\n"  +
      "col1,vegetable,\"nam\"\"e2x\"\r\n";
    YailList ylist = CsvUtil.fromCsvTable(testString);
    for (Object o : ylist.toArray()) {
      YailList row = (YailList) o;
      assertEquals(3, row.size());
      String field3 = row.getString(2);
      assertTrue(field3.startsWith("nam\"e")); // single "
      assertTrue(field3.endsWith("x"));
    }
  }
  
  // TODO(sharon): consider adding a test for multiple CRLF in sequence
  
  public void testFromCSVTableUnmatchedDoubleQuotes() {
    String testString = 
      "\"column id,type,name\r\n" +
      "col0,animal,string\r\n" + 
      "col1,favorite_food,string\r\n";
    try {
      YailList list = CsvUtil.fromCsvTable(testString);
      fail();
    } catch (Exception e) {
      // expected
    }
  }
  
  public void testFromCSVRowWithNewline() throws Exception {
    String testString = "column id,name,type\n";  // with newline
    YailList ylist = CsvUtil.fromCsvRow(testString);
    assertEquals(3, ylist.size());
  }
  
  public void testFromCSVRowNoNewline() throws Exception {
    String testString = "column id,name,type,value";  // no newline
    YailList ylist = CsvUtil.fromCsvRow(testString);
    assertEquals(4, ylist.size());
  }
  
  public void testFromCSVRowNewlineInQuotes() throws Exception {
    // newlines inside quotes don't terminate line
    String testString = "column id,name,type,\"line1\nline2\"";  
    YailList ylist = CsvUtil.fromCsvRow(testString);
    assertEquals(4, ylist.size());
  }
  
  public void testFromCSVRowMultiLineRowLF() {
    String testString =
      "column id,name,type\n" +
      "col0,animal,string\n";  // multi-line row should fail
    try {
      YailList list = CsvUtil.fromCsvRow(testString);
      fail();
    } catch (Exception e) {
      // expected
    }
  }
  
  public void testFromCSVMultiLineRowCRLF() {
   String testString =
      "column id,name,type\r\n" +
      "col0,animal,string";  // multi-line row should fail
    try {
      YailList list = CsvUtil.fromCsvRow(testString);
      fail();
    } catch (Exception e) {
      // expected
    }
  }
  
// Check that the elements are trimmed
  public void testFromCSVRowForTrimming() throws Exception {
   String testString =
     "  hello   ,   there     ,    sailor     ";
   YailList ylist = CsvUtil.fromCsvRow(testString);
   // for a Yail list accessed at this level, the 0th element is *list* 
   assertEquals(ylist.get(1), "hello");
   assertEquals(ylist.get(2), "there");
   assertEquals(ylist.get(3), "sailor");
  }

// check that all the elements of all the table rows are trimmed

  public void testFromCSVTableForTrimming() throws Exception {
   String testString =
     "  hello   ,   there     ,    sailor  \n  got  ,  a  , match    ";
   YailList ylist = CsvUtil.fromCsvTable(testString);
   // check rows at the string level rather than twists java's arm into
   // converting each row string into a list of words
   String row1 = ylist.get(1).toString();
   String row2 = ylist.get(2).toString();
   assertEquals(row1, "(hello there sailor)");
   assertEquals(row2, "(got a match)");
  }

  public void testToCSVTableBasic() {
    ArrayList<YailList> list = new ArrayList<YailList>();
    for (int i = 0; i < 5; ++i) {
      ArrayList<String> row = new ArrayList<String>();
      for (int j = 0; j < 3; ++j) {
        row.add("field" + i + j);
      }
      list.add(YailList.makeList(row));
    }
    String expectedCSVString = 
      "\"field00\",\"field01\",\"field02\"\r\n" +
      "\"field10\",\"field11\",\"field12\"\r\n" +
      "\"field20\",\"field21\",\"field22\"\r\n" +
      "\"field30\",\"field31\",\"field32\"\r\n" +
      "\"field40\",\"field41\",\"field42\"\r\n";
    assertEquals(expectedCSVString, CsvUtil.toCsvTable(YailList.makeList(list)));
  }    

  public void testToCSVTableSingleLevelList() {
    ArrayList<String> row = new ArrayList<String>();
    for (int j = 0; j < 3; ++j) {
      row.add("field" + j);
    }
    try {
      CsvUtil.toCsvTable(YailList.makeList(row));
      fail();
    } catch (Exception e) {
      // expected;
    }
  }
  
  public void testToCSVRowBasic() {
    ArrayList<String> row = new ArrayList<String>();
    for (int j = 0; j < 3; ++j) {
      row.add("field" + j);
    }
    String expectedCSVString = "\"field0\",\"field1\",\"field2\"";
    assertEquals(expectedCSVString, CsvUtil.toCsvRow(YailList.makeList(row)));
  }
}
