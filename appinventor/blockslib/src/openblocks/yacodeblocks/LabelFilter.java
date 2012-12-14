// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;


/**
 * Provides an interface for filtering the string input into editable
 * labels.
 *
 * Note: LabelWidget.java has a private class called BlockLabelTextField
 * which implements input controls that only allow certain characters
 * to be input into a label at all.  It enforces this when keyboard events
 * fire, but they don't actually keep someone from copy/pasting in illegal
 * characters.
 *
 *
 */
public interface LabelFilter {

  /**
   * Returns whether or not the provided label is allowed using this filter.
   * @param label the candidate string.
   * @return true if the label matches the pattern associated with the
   * instance of LabelFilter.
   */
  public boolean isLegal(String label);

}
