// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
