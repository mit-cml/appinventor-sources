// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

/**
 * Provides a checked exception class for phone synchronization exceptions.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class PhoneCommunicationException extends Exception {
  public PhoneCommunicationException(String message){
    super(message);
  }

  public PhoneCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }

  public PhoneCommunicationException(Throwable cause) {
    super(cause);
  }
}
