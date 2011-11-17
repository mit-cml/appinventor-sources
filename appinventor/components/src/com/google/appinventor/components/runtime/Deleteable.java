// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

/**
 * Interface for components that need to do something when they are dynamically deleted (most
 * likely by the REPL)
 *
 * @author markf@google.com (Your Name Here)
 */
public interface Deleteable {
  void onDelete();
}
