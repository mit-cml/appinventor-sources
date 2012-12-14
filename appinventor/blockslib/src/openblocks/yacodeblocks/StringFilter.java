// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

/**
 * LabelFilter suitable for text fields.
 *
 *
 */
public class StringFilter implements LabelFilter {

  public static final LabelFilter STRING_FILTER = new StringFilter();

  public boolean isLegal(String label){
    boolean evenNoOfTrailingBackslashes = true;
    for (int i = label.length() - 1; i >= 0; i--) {
      if (label.charAt(i) == '\\') {
        evenNoOfTrailingBackslashes = !evenNoOfTrailingBackslashes;
      } else {
        break;
      }
    }
    return evenNoOfTrailingBackslashes;
  }

}
