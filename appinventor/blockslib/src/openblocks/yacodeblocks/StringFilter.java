// Copyright 2009 Google Inc. All Rights Reserved.

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
