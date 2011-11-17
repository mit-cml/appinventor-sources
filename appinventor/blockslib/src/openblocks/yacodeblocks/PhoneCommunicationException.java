// Copyright 2010 Google Inc. All Rights Reserved.

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
