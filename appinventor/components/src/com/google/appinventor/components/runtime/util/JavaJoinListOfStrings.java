// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2018  MIT, All rights reserve
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.List;
import android.util.Log;


// Implements the following operation

// (define join-strings (strings separator)
//    (JavaJoinListOfStrings:joinStrings strings separator))

// I'm writing this in Java, rather than using Kawa in runtime.scm
// because Kawa seems to blow out memory (or stack?) on small-memory systems 
// and large lists.


/**
 * Java implementation of join-strings since the Kawa version appears to run of space.
 * See runtime.scm
 *
 * @author halabelson@google.com (Hal Abelson)
 */
public final class JavaJoinListOfStrings {

private static final boolean DEBUG = false;

  public static final String LOG_TAG = "JavaJoinListOfStrings";

  // The elements in listOString are Kawa strings, but these are
  // not necessarily Java Strings.   They might be FStrings.   So we
  // accept a list of Objects and use toString to do a conversion.
  public static String joinStrings(List<Object> listOfStrings, String separator) {
    // We would use String.join, but that is Java 8
    if (DEBUG) {
      Log.i(LOG_TAG, "calling joinStrings");
    }
    return join(listOfStrings, separator);
  }
  
  private static String join(List<Object> list, String separator)
  {
     StringBuilder sb = new StringBuilder();
     boolean first = true;
     for (Object item : list)
     {
        if (first)
           first = false;
        else
           sb.append(separator);
        sb.append(item.toString());
     }
     return sb.toString();
  }

}
