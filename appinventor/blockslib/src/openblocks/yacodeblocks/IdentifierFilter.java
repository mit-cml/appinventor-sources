// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

/**
 * Provides a filter that allows valid identifier String objects.  This is 
 * suitable for variable and method names.  It allows any String which begins
 * with a letter and is followed by zero or more word characters (alphanumeric
 * or underscores).
 * 
 *
 */
public class IdentifierFilter implements LabelFilter {

  public static final LabelFilter IDENTIFIER_FILTER = new IdentifierFilter();

  public boolean isLegal(String label) {
    return label.matches("^[a-zA-Z]\\w*$");
  }
}