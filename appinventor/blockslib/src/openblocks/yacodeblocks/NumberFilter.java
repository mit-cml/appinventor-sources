// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Provides a filter that allows strings which represent positive and
 * negative integers and floating point numbers of any size.
 *
 *
 */
public class NumberFilter implements LabelFilter {

  public static final LabelFilter NUMBER_FILTER = new NumberFilter();

  private final String INT_REGEX = "^[-]?[0-9]+$";
  private final String FLOAT_REGEX = "^[-]?([0-9]*)((\\.[0-9]+)|[0-9]\\.)$";

  public boolean isLegal(String label) {
    return label.matches(INT_REGEX) || label.matches(FLOAT_REGEX);
  }
}
