// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/**
 * Unit tests for the ChartDataSourceUtil utility class.
 */
public class DataSourceUtilTest {
  /**
   * Test case to ensure that passing in a null List to the
   * determineMaximumListSize returns 0.
   */
  @Test
  public void testDetermineMaximumListSizeNull() {
    final int expected = 0;
    int result = ChartDataSourceUtil.determineMaximumListSize(null);

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that determining the maximum list size on
   * equally sized Lists returns the size of a single List.
   */
  @Test
  public void testDetermineMaximumListSizeEvenSizes() {
    YailList column1 = YailList.makeList(Arrays.asList("X", "1", "2", "3", "4"));
    YailList column2 = YailList.makeList(Arrays.asList("Y", "2", "3", "4", "5"));
    YailList column3 = YailList.makeList(Arrays.asList("Z", "3", "4", "5", "6"));
    YailList columns = YailList.makeList(Arrays.asList(column1, column2, column3));

    final int expected = 5;
    int result = ChartDataSourceUtil.determineMaximumListSize(columns);

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that determining the List size on
   * un-even sized Lists returns the maximum height
   * of the Lists.
   */
  @Test
  public void testDetermineMaximumListSizeUnevenSizes() {
    YailList column1 = YailList.makeList(Arrays.asList("X", "1"));
    YailList column2 = YailList.makeList(Arrays.asList("Y", "2", "3"));
    YailList column3 = YailList.makeList(Arrays.asList("Z", "3"));
    YailList columns = YailList.makeList(Arrays.asList(column1, column2, column3));

    final int expected = 3;
    int result = ChartDataSourceUtil.determineMaximumListSize(columns);

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that passing in a non-List entry to
   * the matrix does not affect the outcome of the result of
   * determineMaximumListSize.
   */
  @Test
  public void testDetermineRMaximumListSizeNonColumnEntry() {
    YailList column1 = YailList.makeList(Arrays.asList("X", "1"));
    YailList column2 = YailList.makeList(Arrays.asList("Y", "3", "5"));
    YailList columns = YailList.makeList(Arrays.asList(column1, "random-entry", column2));

    final int expected = 3;
    int result = ChartDataSourceUtil.determineMaximumListSize(columns);

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that determining the maximum list size of
   * lists which consists of only an empty list returns 0.
   */
  @Test
  public void testDetermineMaximumListSizeEmpty() {
    final int expected = 0;
    YailList columns = YailList.makeList(Collections.singletonList(new YailList()));

    int result = ChartDataSourceUtil.determineMaximumListSize(columns);

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that getting the transpose
   * of an empty List returns an empty List.
   */
  @Test
  public void testGetTransposeEmpty() {
    YailList rows = new YailList();
    YailList columns = ChartDataSourceUtil.getTranspose(rows);

    assertEquals(columns, rows);
  }

  /**
   * Test case to ensure that retrieving the transpose
   * from a matrix with a single entry only returns
   * the same result as the input.
   */
  @Test
  public void testGetTransposeSingleEntry() {
    YailList row = YailList.makeList(Collections.singletonList("1"));
    YailList rows = YailList.makeList(Collections.singletonList(row));
    YailList columns = ChartDataSourceUtil.getTranspose(rows);

    assertEquals(columns, rows);
  }

  /**
   * Test case to ensure that retrieving the transpose from
   * a matrix containing a single List returns the appropriate
   * transpose containing a List entry for each matrix entry.
   */
  @Test
  public void testGetTransposeSingleEntryMultipleColumns() {
    YailList row = YailList.makeList(Arrays.asList("X", "Y", "Z"));
    YailList rows = YailList.makeList(Collections.singletonList(row));
    YailList columns = ChartDataSourceUtil.getTranspose(rows);

    YailList column1 = YailList.makeList(Collections.singletonList("X"));
    YailList column2 = YailList.makeList(Collections.singletonList("Y"));
    YailList column3 = YailList.makeList(Collections.singletonList("Z"));
    YailList expected = YailList.makeList(Arrays.asList(column1, column2, column3));

    assertEquals(expected, columns);
  }

  /**
   * Test case to ensure that retrieving the transpose from
   * a matrix consisting of Lists with single entries only
   * returns a transposed matrix with a single List entry
   * containing all the entries of the matrix.
   */
  @Test
  public void testGetTransposeSingleColumn() {
    YailList row1 = YailList.makeList(Collections.singletonList("X"));
    YailList row2 = YailList.makeList(Collections.singletonList("1"));
    YailList row3 = YailList.makeList(Collections.singletonList("2"));
    YailList rows = YailList.makeList(Arrays.asList(row1, row2, row3));
    YailList columns = ChartDataSourceUtil.getTranspose(rows);

    YailList column = YailList.makeList(Arrays.asList("X", "1", "2"));
    YailList expected = YailList.makeList(Collections.singletonList(column));

    assertEquals(expected, columns);
  }

  /**
   * Test case to ensure that retrieving the transpose
   * from a matrix consisting of multiple List entries
   * which all have (equal amounts of) multiple entries
   * returns the appropriate transpose result.
   */
  @Test
  public void testGetTransposeMultipleColumns() {
    YailList row1 = YailList.makeList(Arrays.asList("X", "Y", "Z"));
    YailList row2 = YailList.makeList(Arrays.asList("1", "2", "3"));
    YailList row3 = YailList.makeList(Arrays.asList("2", "3", "4"));
    YailList row4 = YailList.makeList(Arrays.asList("3", "4", "5"));
    YailList rows = YailList.makeList(Arrays.asList(row1, row2, row3, row4));
    YailList columns = ChartDataSourceUtil.getTranspose(rows);

    YailList column1 = YailList.makeList(Arrays.asList("X", "1", "2", "3"));
    YailList column2 = YailList.makeList(Arrays.asList("Y", "2", "3", "4"));
    YailList column3 = YailList.makeList(Arrays.asList("Z", "3", "4", "5"));
    YailList expected = YailList.makeList(Arrays.asList(column1, column2, column3));

    assertEquals(expected, columns);
  }

  /**
   * Test case to ensure that retrieving the transpose
   * from a matrix consisting of uneven increasing size List entries
   * returns the appropriate transpose filling blank entries ("")
   * where appropriate.
   */
  @Test
  public void testGetTransposeUnevenSizesDecreasing() {
    YailList row1 = YailList.makeList(Arrays.asList("X", "Y", "Z"));
    YailList row2 = YailList.makeList(Arrays.asList("1", "2", "3"));
    YailList row3 = YailList.makeList(Arrays.asList("2", "3"));
    YailList row4 = YailList.makeList(Collections.singletonList("3"));
    YailList rows = YailList.makeList(Arrays.asList(row1, row2, row3, row4));
    YailList columns = ChartDataSourceUtil.getTranspose(rows);

    YailList column1 = YailList.makeList(Arrays.asList("X", "1", "2", "3"));
    YailList column2 = YailList.makeList(Arrays.asList("Y", "2", "3", ""));
    YailList column3 = YailList.makeList(Arrays.asList("Z", "3", "", ""));
    YailList expected = YailList.makeList(Arrays.asList(column1, column2, column3));

    assertEquals(expected, columns);
  }

  /**
   * Test case to ensure that retrieving the transpose
   * from a matrix consisting of decreasing size List
   * entries returns the appropriate transpose filling
   * blank entries ("") where appropriate.
   */
  @Test
  public void testGetTransposeUnevenSizesIncreasing() {
    YailList row1 = YailList.makeList(Arrays.asList("X"));
    YailList row2 = YailList.makeList(Arrays.asList("Y", "2"));
    YailList row3 = YailList.makeList(Arrays.asList("Z", "3", "4"));
    YailList rows = YailList.makeList(Arrays.asList(row1, row2, row3));
    YailList columns = ChartDataSourceUtil.getTranspose(rows);

    YailList column1 = YailList.makeList(Arrays.asList("X", "Y", "Z"));
    YailList column2 = YailList.makeList(Arrays.asList("", "2", "3"));
    YailList column3 = YailList.makeList(Arrays.asList("", "", "4"));
    YailList expected = YailList.makeList(Arrays.asList(column1, column2, column3));

    assertEquals(expected, columns);
  }

  /**
   * Test case to ensure that retrieving the transpose
   * from a matrix consisting of uneven sized List entries
   * with the largest one as the middle element
   * returns the appropriate transpose filling blank entries
   * ("") where appropriate.
   */
  @Test
  public void testGetTransposeUnevenSizeMiddle() {
    YailList row1 = YailList.makeList(Arrays.asList("X", "1", "2", "3"));
    YailList row2 = YailList.makeList(Arrays.asList("Y", "2", "3", "4", "5"));
    YailList row3 = YailList.makeList(Arrays.asList("Z", "3", "4"));
    YailList rows = YailList.makeList(Arrays.asList(row1, row2, row3));
    YailList columns = ChartDataSourceUtil.getTranspose(rows);

    YailList col1 = YailList.makeList(Arrays.asList("X", "Y", "Z"));
    YailList col2 = YailList.makeList(Arrays.asList("1", "2", "3"));
    YailList col3 = YailList.makeList(Arrays.asList("2", "3", "4"));
    YailList col4 = YailList.makeList(Arrays.asList("3", "4", ""));
    YailList col5 = YailList.makeList(Arrays.asList("", "5", ""));
    YailList expected = YailList.makeList(Arrays.asList(col1, col2, col3, col4, col5));

    assertEquals(expected, columns);
  }

  /**
   * Test to ensure that the transpose property is satisfied
   * (taking the transpose of the transpose of a matrix returns
   * the matrix itself).
   */
  @Test
  public void testTransposeProperty() {
    YailList row1 = YailList.makeList(Arrays.asList("X", "Y", "Z"));
    YailList row2 = YailList.makeList(Arrays.asList("5", "22", "73"));
    YailList row3 = YailList.makeList(Arrays.asList("7", "64", "34"));
    YailList row4 = YailList.makeList(Arrays.asList("9", "32", "51"));
    YailList row5 = YailList.makeList(Arrays.asList("11", "32", "9"));
    YailList rows = YailList.makeList(Arrays.asList(row1, row2, row3, row4, row5));
    YailList matrix = ChartDataSourceUtil.getTranspose(rows);

    YailList transposeMatrix = ChartDataSourceUtil.getTranspose(matrix);
    assertNotSame(matrix, transposeMatrix);

    YailList transposeTransposeMatrix = ChartDataSourceUtil.getTranspose(transposeMatrix);
    assertEquals(matrix, transposeTransposeMatrix);
  }
}
