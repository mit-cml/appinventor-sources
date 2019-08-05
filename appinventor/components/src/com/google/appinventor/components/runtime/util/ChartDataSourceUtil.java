package com.google.appinventor.components.runtime.util;

import java.util.ArrayList;
import java.util.List;

public class ChartDataSourceUtil {
  /**
   * Prevent instantiation.
   */
  private ChartDataSourceUtil() {
  }

  /**
   * Determines the total row count of the specified columns List. The
   * columns List is expected to be a List of Lists, and invalid entries
   * are simply skipped.
   *
   * In case of uneven rows, the maximum row count is returned.
   *
   * @param columns  Columns List to determine row count for
   * @return  row count of the columns
   */
  public static int determineRowCountInColumns(YailList columns) {
    int rows = 0;

    // Columns null case - return 0 rows
    if (columns == null) {
      return rows;
    }

    // Establish the row count of the specified columns
    for (int i = 0; i < columns.size(); ++i) {
      if (!(columns.getObject(i) instanceof YailList)) {
        continue;
      }

      YailList column = (YailList)columns.getObject(i);

      // Update rows variable if the column size is larger
      // than the current row count
      if (column.size() > rows) {
        rows = column.size();
      }
    }

    return rows;
  }

  /**
   * Constructs and returns a YailList of columns from the specified
   * rows YailList.
   *
   * @param rows  rows to construct columns from
   * @return  Columns representation of the specified rows
   */
  public static YailList getColumnsFromRows(YailList rows) {
    // Get the size of the first row, which indicates the number
    // of columns.
    int columnCount = ((YailList)rows.getObject(0)).size();

    // Construct each column separately, and add it
    // to the resulting list.
    ArrayList<YailList> columnList = new ArrayList<YailList>();

    for (int i = 0; i < columnCount; ++i) {
      // Add the i-th column from the rows list to the
      // resulting Columns List.
      columnList.add(getColumn(rows, i));
    }

    // Convert the result to a YailList
    return YailList.makeList(columnList);
  }

  /**
   * Constructs and returns a YailList of rows from the specified
   * columns YailList.
   *
   * @param columns  columns to construct rows from
   * @return  Rows representation of the specified columns
   */
  public static YailList getRowsFromColumns(YailList columns) {
    // First determine the total rows in the columns List
    int rowCount = determineRowCountInColumns(columns);

    List<YailList> rowResult = new ArrayList<YailList>();

    // A single iteration is needed for each row. The i-th index
    // will represent the current row.
    for (int i = 0; i < rowCount; ++i) {
      List<String> resultEntries = new ArrayList<String>();

      // Iterate over all the columns to get the entries for
      // the row.
      for (int j = 0; j < columns.size(); ++j) {
        // Columns is expected to contain only YailList entries
        YailList column = (YailList) columns.get(j);

        // If the index is smaller than the column size, then
        // the entry exists.
        if (column.size() > i) {
          // Add the i-th row entry from the column
          resultEntries.add(column.getString(i));
        } else { // Entry does not exist
          // Add blank entry
          resultEntries.add("");
        }
      }

      // Construct a row from the resulting entries, and
      // add it to the resulting List
      rowResult.add(YailList.makeList(resultEntries));
    }

    return YailList.makeList(rowResult);
  }

  /**
   * Constructs and returns a column from the specified rows,
   * given the index of the needed column.
   *
   * @param rows  rows to construct column from
   * @param index  the index of the column to construct from the rows
   * @return  YailList column representation of the specified index
   */
  private static YailList getColumn(YailList rows, int index) {
    List<String> entries = new ArrayList<String>();

    for (int i = 0; i < rows.size(); ++i) {
      // Get the i-th row
      YailList row = (YailList) rows.getObject(i); // Safe cast

      // index-th entry in the row is the required column value
      entries.add((row.getString(index)));
    }

    return YailList.makeList(entries);
  }
}
