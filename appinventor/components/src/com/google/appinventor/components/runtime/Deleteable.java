// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
