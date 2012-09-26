// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

/**
 * Interface indicating that this object can handle event dispatching.
 *
 * @author markf@google.com (Mark Friedman)
 */

public interface HandlesEventDispatching {
  public boolean canDispatchEvent(Component component, String eventName);

  public boolean dispatchEvent(Component component, String componentName, String eventName,
      Object[] args);
}
