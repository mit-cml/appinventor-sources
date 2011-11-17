// Copyright 2010 Google Inc. All Rights Reserved.

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
