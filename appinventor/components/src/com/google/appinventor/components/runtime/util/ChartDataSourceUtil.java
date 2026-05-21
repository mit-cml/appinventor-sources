// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility functions for Chart Data Source related functionality,
 * such as converting columns to rows. Used primarily by Chart compatible
 * Data Sources.
 */
public class ChartDataSourceUtil {
  /**
   * Prevent instantiation.
   */
  private ChartDataSourceUtil() {
  }

  /**
   * Determines the maximum List entry size in the specified matrix.
   * The YailList is expected to contain List entries, and invalid
   * entries are simply skipped.
   *
   * @param matrix  Matrix to return maximum list size of
   * @return  Maximum List size of the matrix's nested Lists
   */
  public static int determineMaximumListSize(YailList matrix) {
    int entries = 0;

    // Matrix null case - return 0
    if (matrix == null) {
      return entries;
    }

    // Iterate through all the matrix's entries
    for (int i = 0; i < matrix.size(); ++i) {
      // Matrix entry is not of type YailList; Skip
      Object row = matrix.getObject(i);

      if (!(row instanceof List)) {
        continue;
      }

      List<?> list = (List<?>) row;

      // A list entry with a bigger size has been found; update
      // the value.
      if (list.size() > entries) {
        entries = list.size();
      }
    }

    return entries;
  }

  /**
   * Returns the transpose of the specified matrix (List of Lists)
   * The specified YailList parameter is expected to contain nested
   * YailList entries. Invalid entries are simply ignored.
   *
   * <p>The method is used to convert a List of rows to a List of
   * columns and vice versa (the transpose)</p>
   *
   * @param matrix  Matrix to return the transpose of
   * @return  Transpose of the specified matrix.
   */
  public static YailList getTranspose(YailList matrix) {
    // Determine the maximum entry count of the matrix
    int entries = determineMaximumListSize(matrix);

    List<YailList> result = new ArrayList<>();

    for (int i = 0; i < entries; ++i) {
      // Get the i-th transpose entry and add it to the
      // result List. This essentially constructs the
      // i-th column or row entry (depending on the List passed in)
      YailList listEntries = getTransposeEntry(matrix, i);
      result.add(listEntries);
    }

    // Convert the result to a YailList
    return YailList.makeList(result);
  }

  /**
   * Constructs and returns a transpose entry from the given matrix
   * with the given index.
   *
   * <p>The index represents the entry required. If the matrix is a List
   * of rows, the index represents the number of the column to return.
   * If the matrix is a List of columns, the index represents the
   * number of the row to return.</p>
   *
   * @param matrix  Matrix to return the transpose entry of
   * @param index  The index of the entry to return
   * @return  The index-th transpose entry of the matrix
   */
  private static YailList getTransposeEntry(YailList matrix, int index) {
    List<String> entries = new ArrayList<>();

    for (int i = 0; i < matrix.size(); ++i) {
      // Get the i-th matrix entry
      List<?> matrixEntry = (List<?>) matrix.getObject(i); // Safe cast

      // Ensure that the entry has the required index value
      // (this handles un-even list case)
      if (matrixEntry.size() > index) {
        // Each index-th element is added from all the matrix entries
        // to create the transpose entry
        if (matrixEntry instanceof YailList) {
          entries.add((((YailList) matrixEntry).getString(index)));
        } else {
          entries.add(matrixEntry.get(index).toString());
        }
      } else { // Entry does not exist
        // Add blank entry
        entries.add("");
      }
    }

    return YailList.makeList(entries);
  }
}
