package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class CSVFileTest extends FileTestBase {
  protected CSVFile csvFile;

  protected static final String TARGET_FILE = "testCsv.csv";
  protected static final String TARGET_FILE_2_ROWS = "testCsv2Rows.csv";
  protected static final String TARGET_FILE_READ = "testWriteCsv.txt";
  protected static final String DATA = "X,Y\n1,2\n"; // Same as testCsv2Rows.csv

  @Before
  public void setUp() {
    super.setUp();
    csvFile = new CSVFile(getForm());
  }

  @Test
  public void testDefaults() {
    YailList emptyList = new YailList();

    assertEquals(emptyList, csvFile.Rows());
    assertEquals(emptyList, csvFile.Columns());
    assertEquals(emptyList, csvFile.ColumnNames());
  }

  @Test
  public void testGetColumnNames() {
    ArrayList<String> expectedValues = new ArrayList<String>() {{
      add("X");
      add("Y");
      add("Z");
    }};

    YailList expected = YailList.makeList(expectedValues);

    // Load the test CSV File & get the Column Names.
    // Since ColumnNames() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestCSVFile();
    YailList columnNames = csvFile.ColumnNames();

    // Assert that the expected result is obtained
    assertEquals(expected, columnNames);
  }

  @Test
  public void testGetRows() {
    ArrayList<YailList> expectedValues = new ArrayList<YailList>() {{
      add(YailList.makeList(Arrays.asList("X", "Y", "Z")));
      add(YailList.makeList(Arrays.asList("1", "2", "3")));
      add(YailList.makeList(Arrays.asList("2", "3", "4")));
      add(YailList.makeList(Arrays.asList("3", "4", "5")));
    }};

    YailList expected = YailList.makeList(expectedValues);

    // Load the test CSV File & get the Column Names.
    // Since ColumnNames() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestCSVFile();
    YailList rows = csvFile.Rows();

    // Assert that the expected result is obtained
    assertEquals(expected, rows);
  }

  @Test
  public void testGetColumns() {
    ArrayList<YailList> expectedValues = new ArrayList<YailList>() {{
      add(YailList.makeList(Arrays.asList("X", "1", "2", "3")));
      add(YailList.makeList(Arrays.asList("Y", "2", "3", "4")));
      add(YailList.makeList(Arrays.asList("Z", "3", "4", "5")));
    }};

    YailList expected = YailList.makeList(expectedValues);

    // Load the test CSV File & get the Column Names.
    // Since ColumnNames() is blocking, we do not
    // need to worry about explicitly running async tasks
    loadTestCSVFile();
    YailList columns = csvFile.Columns();

    // Assert that the expected result is obtained
    assertEquals(expected, columns);
  }

  @Test
  public void testSetSourceFile() {
    // Assert that rows are empty initially
    YailList emptyList = new YailList();
    assertEquals(emptyList, csvFile.Rows());

    // Grant permissions & update the Source File of the CSV File
    grantFilePermissions();
    csvFile.SourceFile(TARGET_FILE_2_ROWS);

    // Get the expected value and assert that it is equal to the result
    YailList expected = expectedValues2Rows();
    assertEquals(expected, csvFile.Rows());
  }

  @Test
  public void testReadFileInternal() {
    grantFilePermissions();
    writeTempFile(TARGET_FILE_READ, DATA, false);
    csvFile.ReadFile(TARGET_FILE_READ);

    YailList expected = expectedValues2Rows();
    assertEquals(expected, csvFile.Rows());
  }

  @Test
  public void testReadFileExternal() {
    grantFilePermissions();
    writeTempFile(TARGET_FILE_READ, DATA, true);
    csvFile.ReadFile("/" + TARGET_FILE_READ);

    YailList expected = expectedValues2Rows();
    assertEquals(expected, csvFile.Rows());
  }

  @Test
  public void testGetColumnValid() {
    final String column = "Y";

    // Load the test CSV file
    loadTestCSVFile();

    // The Y column is the 2nd column of the test CSV data
    YailList expected = (YailList) csvFile.Columns().getObject(1);

    // Assert result equals to expected value
    YailList result = csvFile.getColumn(column);
    assertEquals(expected, result);
  }

  @Test
  public void testGetColumnNonExistent() {
    final String column = "Random";

    // Load the test CSV file
    loadTestCSVFile();

    YailList expected = new YailList();

    // Assert result equals to expected value
    YailList result = csvFile.getColumn(column);
    assertEquals(expected, result);
  }

  @Test
  public void testGetColumnEmpty() {
    final String column = "";

    // Load the test CSV file
    loadTestCSVFile();

    YailList expected = new YailList();

    // Assert result equals to expected value
    YailList result = csvFile.getColumn(column);
    assertEquals(expected, result);
  }

  @Test
  public void testGetColumnNull() {
    final String column = null;

    // Load the test CSV file
    loadTestCSVFile();

    YailList expected = new YailList();

    // Assert result equals to expected value
    YailList result = csvFile.getColumn(column);
    assertEquals(expected, result);
  }

  @Test
  public void testGetColumnsEmpty() {
    YailList columns = new YailList();

    loadTestCSVFile();

    YailList expected = new YailList();
    YailList result = null;

    try {
      result = csvFile.getColumns(columns).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
      fail("Exception thrown!");
    } catch (ExecutionException e) {
      e.printStackTrace();
      fail("Exception thrown!");
    }

    assertEquals(expected, result);
  }

  @Test
  public void testGetSingleColumn() {
    YailList columns = YailList.makeList(Collections.singleton("Z"));

    loadTestCSVFile();

    YailList xColumn = (YailList)csvFile.Columns().getObject(2);
    YailList expected = YailList.makeList(Collections.singletonList(xColumn));

    YailList result = null;

    try {
      result = csvFile.getColumns(columns).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
      fail("Exception thrown!");
    } catch (ExecutionException e) {
      e.printStackTrace();
      fail("Exception thrown!");
    }

    assertEquals(expected, result);
  }

  @Test
  public void testGetTwoColumns() {
    YailList columns = YailList.makeList(Arrays.asList("X", "Z"));

    loadTestCSVFile();

    YailList xColumn = (YailList) csvFile.Columns().getObject(0);
    YailList zColumn = (YailList) csvFile.Columns().getObject(2);

    YailList expected = YailList.makeList(Arrays.asList(xColumn, zColumn));
    YailList result = null;

    try {
      result = csvFile.getColumns(columns).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
      fail("Exception thrown!");
    } catch (ExecutionException e) {
      e.printStackTrace();
      fail("Exception thrown!");
    }

    assertEquals(expected, result);
  }

  /**
   * Helper method to load a test CSV file for the tests
   */
  private void loadTestCSVFile() {
    grantFilePermissions();
    csvFile.ReadFile("//" + TARGET_FILE);
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