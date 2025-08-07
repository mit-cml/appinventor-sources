// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Return timestamp information for when this server was last deployed
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class BuildData {

  private static long timestamp = new Date().getTime();
  private static Date date = new Date(timestamp);

  /**
   * Returns the timestamp for when the app was deployed (as a Unix time,
   * in milliseconds, suitable for passing to the java.util.Date() constructor
   *
   */
  public static long getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the timestamp for when the app was deployed, formatted as
   * a String suitable for displaying to the user in the default locale.
   * TODO(user): I'm not sure if this uses the user's locale, or the
   * server's. And which do we want?
   *
   */
  public static String getTimestampAsString() {
    return new SimpleDateFormat("EEE, d MMM yyyy HH:mm z").format(date);
  }
}
