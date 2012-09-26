// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
