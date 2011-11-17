// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

/**
 * Provides a checked exception for exceptions that occur during the workspace
 * undo/redo operations.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class UndoRedoException extends CodeblocksException {

  public UndoRedoException(String message) {
    super(message);
  }
  
  public UndoRedoException(String message, Throwable cause) {
    super(message, cause);
  }
}
