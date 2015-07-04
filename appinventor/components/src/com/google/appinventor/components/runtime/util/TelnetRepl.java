// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import android.util.Log;

import gnu.expr.Language;
import gnu.mapping.Environment;
import gnu.mapping.OutPort;
import gnu.mapping.Procedure0;
import gnu.mapping.TtyInPort;
import gnu.mapping.Values;
import gnu.text.FilePath;

import kawa.Shell;
import kawa.Telnet;

/**
 * This is code for running Telnet-based Read-Eval-Print loops.  It's based on the code for
 * {@link kawa.TelnetRepl}
 */
public class TelnetRepl extends Procedure0 {
  // close when finished.
  java.net.Socket socket;

  Language language;
  private static final int REPL_STACK_SIZE = 256*1024;

  public TelnetRepl(Language language, java.net.Socket socket) {
    this.language = language;
    this.socket = socket;
  }

  public Object apply0 () {
    Thread thread = Thread.currentThread();
    ClassLoader contextClassLoader = thread.getContextClassLoader();
    if (contextClassLoader == null) {
      // TODO(markf): this is a hack to deal with calls to getContextClassLoader() within the
      // Shell.run() call below which return null on some older Android phones.
      thread.setContextClassLoader(Telnet.class.getClassLoader());
    }

    try {
      Shell.run(language, Environment.getCurrent());
      return Values.empty;
    } catch (RuntimeException e) {
      Log.d("TelnetRepl", "Repl is exiting with error " + e.getMessage());
      e.printStackTrace();
      throw e;
    } finally {
      try {
        socket.close();
      } catch (java.io.IOException ex) {
        // This ignoring of the exception was in the original version of the code - markf
      }
    }
  }


  /** Run a Kawa repl as a telnet server.
   *  @param client A client that has connected to us,
   *  and that wants to use the telnet protocol to talk to a
   *  Scheme read-eval-print-loop.
   */
  public static Thread serve (Language language, java.net.Socket client)
    throws java.io.IOException {
    Telnet conn = new Telnet(client, true);
    java.io.OutputStream sout = conn.getOutputStream();
    java.io.InputStream sin = conn.getInputStream();
    OutPort out = new OutPort(sout, FilePath.valueOf("/dev/stdout"));
    TtyInPort in = new TtyInPort(sin, FilePath.valueOf("/dev/stdin"), out);
    // The following was commented out in the original code - markf
    /*
    conn.request(Telnet.DO, Telnet.EOF);
    conn.request(Telnet.DO, Telnet.NAWS);
    conn.request(Telnet.DO, Telnet.TTYPE);
    conn.request(Telnet.DO, Telnet.LINEMODE);
    */

    Thread thread =
        new BiggerFuture(new TelnetRepl(language, client),
                         in, out, out, "Telnet Repl Thread", REPL_STACK_SIZE);
    thread.start();
    return thread;
  }
}
