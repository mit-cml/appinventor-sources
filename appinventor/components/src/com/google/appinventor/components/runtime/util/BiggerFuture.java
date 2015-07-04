// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import gnu.mapping.InPort;
import gnu.mapping.OutPort;
import gnu.mapping.Procedure;
import gnu.mapping.RunnableClosure;

/**
 * A version of the {@link gnu.mapping.Future} class that can run with a larger stack size
 */
public class BiggerFuture extends Thread {
  public BiggerFuture(Procedure action,
                      InPort in, OutPort out, OutPort err, String threadName, long stackSize) {
    super(new ThreadGroup("biggerthreads"),
          new RunnableClosure (action, in, out, err),
          threadName, stackSize);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append ("#<future ");
    buf.append(getName());
    buf.append(">");
    return buf.toString();
  }
}
