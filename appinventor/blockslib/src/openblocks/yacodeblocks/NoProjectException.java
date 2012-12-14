// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

/**
 * Exception thrown when there is no current project.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class NoProjectException extends Exception {

  public NoProjectException(String message) {
    super(message);
  }
}
