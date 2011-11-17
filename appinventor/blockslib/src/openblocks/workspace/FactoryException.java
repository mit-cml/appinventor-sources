// Copyright 2010, Google Inc. All Rights Reserved.

package openblocks.workspace;

/**
 * Provides a checked exception for errors that occur in the FactoryManager
 * operations
 * @author sharon@google.com (Sharon Perl)
 *
 */

public class FactoryException extends Exception {
  public FactoryException(String message){
    super(message);
  }
  
  public FactoryException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public FactoryException(Throwable cause) {
    super(cause);
  }
}
