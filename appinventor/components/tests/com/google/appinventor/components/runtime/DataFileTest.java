// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DataFile component.
 */
public class DataFileTest extends FileTestBase {
  protected DataFile dataFile;

  protected static final String TARGET_CSV_FILE = "testCsv.csv";
  protected static final String TARGET_FILE_2_ROWS = "testCsv2Rows.csv";
  protected static final String TARGET_JSON_FILE = "testJson.json";
  protected static final String TARGET_FILE_READ = "testWriteCsv.txt";
  protected static final String DATA = "X,Y\n1,2\n"; // Same as testCsv2Rows.csv

  /**
   * Prepare the test.
   */
  @Before
  public void setUp() {
    super.setUp();
    dataFile = new DataFile(getForm());
    dataFile.DefaultScope(FileScope.Legacy);
  }

  /**
   * Test to ensure that instantiating a DataFile component sets the
   * proper default properties.
   */
  @Test
  public void testDefaults() {
    YailList emptyList = new YailList();

    assertEquals(emptyList, dataFile.Rows());
    assertEquals(emptyList, dataFile.Columns());
    assertEquals(emptyList, dataFile.ColumnNames());
  }

  /**
   * Test to ensure that the ColumnNames property getter returns
   * the correct result and the result is returned only after
   * the reading of the File is finished for the case of CSV.
   */
  @Test
  public void testGetColumnNamesCsv() {
    ArrayList<String> expectedValues = new ArrayList<String>() {{
        add("X");
        add("Y");
        add("Z");
      }};

    YailList expected = YailList.makeList(expectedValues);

    // Load the test CSV File & get the Column Names.
    // Since ColumnNames() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestCsvFile();
    YailList columnNames = dataFile.ColumnNames();

    // Assert that the expected result is obtained
    assertEquals(expected, columnNames);
  }

  /**
   * Test to ensure that the ColumnNames property getter returns
   * the correct result and the result is returned only after
   * the reading of the File is finished for the case ofJSON.
   */
  @Test
  public void testGetColumnNamesJson() {
    HashSet<String> expectedValues = new HashSet<String>() {{
        add("a");
        add("b");
        add("c");
        add("x");
        add("y");
        add("z");
      }};

    // Load the test JSON File & get the Column Names.
    // Since ColumnNames() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestJsonFile();
    YailList columnNames = dataFile.ColumnNames();

    // Since JSON key-value pairs are stored in HashMaps,
    // we cannot ensure deterministic ordering here. Instead,
    // check that all the expected values are contained.
    assertTrue(columnNames.containsAll(expectedValues));
  }

  /**
   * Test to ensure that the Rows property getter returns
   * the correct result and the result is returned only after
   * the reading of the File is finished. Test case for CSV
   * data.
   */
  @Test
  public void testGetRowsCsv() {
    ArrayList<YailList> expectedValues = new ArrayList<YailList>() {{
        add(YailList.makeList(Arrays.asList("X", "Y", "Z")));
        add(YailList.makeList(Arrays.asList("1", "2", "3")));
        add(YailList.makeList(Arrays.asList("2", "3", "4")));
        add(YailList.makeList(Arrays.asList("3", "4", "5")));
      }};

    YailList expected = YailList.makeList(expectedValues);

    // Load the test CSV File & get the Rows.
    // Since Rows() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestCsvFile();
    YailList rows = dataFile.Rows();

    // Assert that the expected result is obtained
    assertEquals(expected, rows);
  }

  /**
   * Test to ensure that the Rows property getter returns
   * the correct result and the result is returned only after
   * the reading of the File is finished. Test case for JSON
   * data.
   */
  @Test
  public void testGetRowsJson() {
    List<List<String>> expectedRows = new ArrayList<List<String>>() {{
        add(Arrays.asList("x", "y", "z", "a", "b", "c"));
        add(Arrays.asList("1", "2", "3", "1", "3", "value"));
        add(Arrays.asList("2", "3", "4", "2", "", ""));
        add(Arrays.asList("3", "4", "5", "", "", ""));
      }};

    // Load the test JSON File & get the Columns.
    // Since Columns() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestJsonFile();
    YailList rows = dataFile.Rows();

    // Assert that all the expected elements are contained
    // in each separate row. The reason for this loop is
    // due to the fact that JSON key-value pairs are
    // stored in HashMaps, and since the rows are
    // the transpose of the columns, a row-by-row
    // check is needed instead.
    for (int i = 0; i < expectedRows.size(); ++i) {
      YailList row = (YailList)rows.getObject(i);
      assertTrue(row.containsAll(expectedRows.get(i)));
    }
  }

  /**
   * Test to ensure that the Columns property getter returns
   * the correct result and the result is returned only after
   * the reading of the File is finished. Test case for CSV
   * data.
   */
  @Test
  public void testGetColumnsCsv() {
    ArrayList<YailList> expectedValues = new ArrayList<YailList>() {{
        add(YailList.makeList(Arrays.asList("X", "1", "2", "3")));
        add(YailList.makeList(Arrays.asList("Y", "2", "3", "4")));
        add(YailList.makeList(Arrays.asList("Z", "3", "4", "5")));
      }};

    YailList expected = YailList.makeList(expectedValues);

    // Load the test CSV File & get the Column Names.
    // Since ColumnNames() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestCsvFile();
    YailList columns = dataFile.Columns();

    // Assert that the expected result is obtained
    assertEquals(expected, columns);
  }

  /**
   * Test to ensure that the Columns property getter returns
   * the correct result and the result is returned only after
   * the reading of the File is finished. Test case for JSON
   * data.
   */
  @Test
  public void testGetColumnsJson() {
    HashSet<YailList> expectedValues = new HashSet<YailList>() {{
        add(YailList.makeList(Arrays.asList("x", "1", "2", "3")));
        add(YailList.makeList(Arrays.asList("y", "2", "3", "4")));
        add(YailList.makeList(Arrays.asList("z", "3", "4", "5")));
        add(YailList.makeList(Arrays.asList("a", "1", "2")));
        add(YailList.makeList(Arrays.asList("b", "3")));
        add(YailList.makeList(Arrays.asList("c", "value")));
      }};

    // Load the test JSON File & get the Columns.
    // Since Columns() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestJsonFile();
    YailList columns = dataFile.Columns();

    // Assert that all the expected columns are contained
    // in the result. We use contains instead of equals
    // since JSON key-value pairs are stored in HashMaps
    // and thus ordering cannot be ensured.
    assertTrue(columns.containsAll(expectedValues));
  }

  /**
   * Test to ensure that setting the Source File property
   * properly reads the specified file from the media path
   * and populates the DataFile with the correct properties
   * (only Rows is checked for simplicity).
   */
  @Test
  public void testSetSourceFile() {
    // Assert that rows are empty initially
    YailList emptyList = new YailList();
    assertEquals(emptyList, dataFile.Rows());

    // Grant permissions & update the Source File of the CSV File
    grantFilePermissions();
    dataFile.SourceFile(TARGET_FILE_2_ROWS);
    ShadowAsynchUtil.runAllPendingRunnables();

    // Get the expected value and assert that it is equal to the result
    YailList expected = expectedValues2Rows();
    assertEquals(expected, dataFile.Rows());
  }

  /**
   * Test to ensure that reading a file using a
   * relative path (no slash) correctly reads a
   * file from the correct directory and populates
   * the DataFile with the appropriate properties
   * (only Rows is checked for simplicity).
   */
  @Test
  public void testReadFileInternal() {
    testWriteAndReadFile(TARGET_FILE_READ, DATA, false);

    YailList expected = expectedValues2Rows();
    assertEquals(expected, dataFile.Rows());
  }

  /**
   * Test to ensure that reading a file using
   * an absolute path (single slash) correctly
   * reads the file from the correct directory and
   * populates the DataFile with the appropriate properties
   * (only Rows is checked for simplicity).
   */
  @Test
  public void testReadFileExternal() {
    testWriteAndReadFile("/" + TARGET_FILE_READ, DATA, true);

    YailList expected = expectedValues2Rows();
    assertEquals(expected, dataFile.Rows());
  }

  /**
   * Test to ensure that reading a file containing a single row
   * behaves correctly and sets the properties accordingly.
   */
  @Test
  public void testReadOneRow() {
    testWriteAndReadFile(TARGET_FILE_READ, "Y", false);

    YailList expectedColumnNames = YailList.makeList(Collections.singletonList("Y"));
    YailList expectedList = YailList.makeList(Collections.singletonList(expectedColumnNames));

    assertEquals(expectedColumnNames, dataFile.ColumnNames());
    assertEquals(expectedList, dataFile.Rows());
    assertEquals(expectedList, dataFile.Columns());
  }

  /**
   * Test to ensure that using the getColumn method with
   * an existing column name returns the appropriate column.
   */
  @Test
  public void testGetColumnValid() {
    final String column = "Y";

    // Load the test CSV file
    loadTestCsvFile();

    // The Y column is the 2nd column of the test CSV data
    YailList expected = (YailList) dataFile.Columns().getObject(1);

    // Assert result equals to expected value
    YailList result = dataFile.getColumn(column);
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that using the getColumn method with
   * a non-existing column name returns an empty YailList.
   */
  @Test
  public void testGetColumnNonExistent() {
    final String column = "Random";

    // Load the test CSV file
    loadTestCsvFile();

    YailList expected = new YailList();

    // Assert result equals to expected value
    YailList result = dataFile.getColumn(column);
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that using the getColumn method with
   * an empty String as the name returns an empty YailList.
   */
  @Test
  public void testGetColumnEmpty() {
    final String column = "";

    // Load the test CSV file
    loadTestCsvFile();

    YailList expected = new YailList();

    // Assert result equals to expected value
    YailList result = dataFile.getColumn(column);
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that using the getColumn method with
   * null as the parameter returns an empty YailList.
   */
  @Test
  public void testGetColumnNull() {
    final String column = null;

    // Load the test CSV file
    loadTestCsvFile();

    YailList expected = new YailList();

    // Assert result equals to expected value
    YailList result = dataFile.getColumn(column);
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that using the getColumns method
   * with an empty YailList returns an empty YailList.
   */
  @Test
  public void testGetColumnsEmpty() throws Exception {
    YailList columns = new YailList();

    loadTestCsvFile();

    YailList expected = new YailList();

    // Use the helper method to assert the result
    testGetColumnsHelper(expected, columns);
  }

  /**
   * Test to ensure that using the getColumns method
   * with a list of a single (valid) value returns
   * a List of columns containing that single column.
   */
  @Test
  public void testGetSingleColumn() throws Exception {
    // Construct the argument containing a single column
    YailList columns = YailList.makeList(Collections.singleton("Z"));

    loadTestCsvFile();

    // Construct the expected value
    YailList xcolumn = (YailList) dataFile.Columns().getObject(2);
    YailList expected = YailList.makeList(Collections.singletonList(xcolumn));

    // Use the helper to assert the result
    testGetColumnsHelper(expected, columns);
  }

  /**
   * Test to ensure that using the getColumns method
   * with a list of a two (valid) columns returns
   * a List of columns containing those two columns.
   */
  @Test
  public void testGetTwoColumns() throws Exception {
    // Construct the argument with 2 columns
    YailList columns = YailList.makeList(Arrays.asList("X", "Z"));

    loadTestCsvFile();

    // Construct the expected value
    YailList xcolumn = (YailList) dataFile.Columns().getObject(0);
    YailList zcolumn = (YailList) dataFile.Columns().getObject(2);
    YailList expected = YailList.makeList(Arrays.asList(xcolumn, zcolumn));

    // Use the helper to assert the result
    testGetColumnsHelper(expected, columns);
  }

  /// Helper methods

  /**
   * Helper method to assert the expected and the resulting values of the
   * getColumns method.
   *
   * @param expected  Expected value
   * @param columns  List of columns argument
   */
  private void testGetColumnsHelper(YailList expected, YailList columns) throws Exception {
    YailList result = dataFile.getDataValue(columns).get();
    assertEquals(expected, result);
  }

  /**
   * Helper method to load a test CSV file for the tests.
   */
  private void loadTestCsvFile() {
    grantFilePermissions();
    dataFile.ReadFile("//" + TARGET_CSV_FILE);
    ShadowAsynchUtil.runAllPendingRunnables();
  }

  /**
   * Helper method to load a test JSON file for the tests.
   */
  private void loadTestJsonFile() {
    grantFilePermissions();
    dataFile.readFromFile("//" + TARGET_JSON_FILE);
    ShadowAsynchUtil.runAllPendingRunnables();
  }

  /**
   * Helper method to write the specified data to the target file, and then
   * read the file in the DataFile component.
   *
   * @param targetFile  Path to the file to read
   * @param data  Data to write
   * @param external  Write to external storage?
   */
  private void testWriteAndReadFile(String targetFile, String data, boolean external) {
    grantFilePermissions();
    writeTempFile(targetFile, data, external);
    dataFile.ReadFile(targetFile);
    ShadowAsynchUtil.runAllPendingRunnables();
  }

  /**
   * Returns the YailList of the expected values for the 2-row CSV test data.
   * The expected values are  ((X,Y), (1,2))
   * @return  YailList of expected values for the 2-row test CSV data
   */
  private YailList expectedValues2Rows() {
    ArrayList<YailList> expectedValues = new ArrayList<YailList>() {{
        add(YailList.makeList(Arrays.asList("X", "Y")));
        add(YailList.makeList(Arrays.asList("1", "2")));
      }};

    return YailList.makeList(expectedValues);
  }
}
