// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

/**
 *
 */
public class LabelFilterFactory {

  /**
   * A LabelFilter object which always returns true for calls to isLegal.
   */
  public static LabelFilter ALLOW_ALL = new LabelFilter() {
    @Override
    public boolean isLegal(String label) {
      return true;
    }
  };

  /**
   * Factory method for creating LabelFilter objects from their name prefix.
   * If filterName does not match "string", "identifier", "none" or "number"
   * this will throw an IllegalArgumentException
   * @param filterName the name of the filter.
   * @return an instance of the relevant subclass of StringFilter or throws
   * an exception if such a subclass does not exist.
   */
  public static LabelFilter valueOf(String filterName){
    if (filterName.equalsIgnoreCase("string")) {
      return StringFilter.STRING_FILTER;
    } else if (filterName.equalsIgnoreCase("identifier")) {
      return IdentifierFilter.IDENTIFIER_FILTER;
    } else if (filterName.equalsIgnoreCase("number")) {
      return NumberFilter.NUMBER_FILTER;
    } else if (filterName.equalsIgnoreCase("none")) {
      return ALLOW_ALL;
    }
    throw new IllegalArgumentException("Invalid filter name: " + filterName);
  }



}
