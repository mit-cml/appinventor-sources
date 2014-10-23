// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import com.google.appengine.api.utils.SystemProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Return timestamp information for when this server was last deployed
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class BuildData {

  private static long timestamp = 0;
  private static Date date = null;

  /**
   * Returns the timestamp for when the app was deployed (as a Unix time,
   * in milliseconds, suitable for passing to the java.util.Date() constructor
   *
   */
  public static long getTimestamp() {
    if (timestamp == 0) {
      // Note: the applicationVersion string has the format version.timestamp
      // The timestamp currently (when this was written) needs to be divided
      // by 2^28 to get it into unix epoch seconds. This could change according
      // to comments I found on the web, but there doesn't seem
      // to be any more stable API to get this info.
      String applicationVersion = SystemProperty.applicationVersion.get();
      if (applicationVersion != null) {
        String parts[] = applicationVersion.split("\\.");
        timestamp = (Long.parseLong(parts[1]) >> 28) * 1000;
        date = new Date(timestamp);
      }
    }
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
    if (timestamp == 0) {
      getTimestamp();
    }
    if (date != null) {
      return new SimpleDateFormat("EEE, d MMM yyyy HH:mm z").format(date);
    }
    return "";
  }
}
