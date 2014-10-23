// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
// This work is licensed under a Creative Commons Attribution 3.0 Unported License.

package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.util.CsvParser;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the fixid list.  Google stores e-mail addresses
 * in a case senstive way, yet in many instances displays them only in
 * lower case. We have at least one instance of a person somehow getting
 * their apps signed using the lower case version.
 *
 * This code permits us to establish a "fixid" list which maps one
 * Google ID, specified by their email address, into another.  This
 * facility can effectively permit one user to login as another. The
 * mapping is implemented as a file in the application instead of as a
 * table in the datastore in order to reduce the probability of it
 * being abused.
 *
 * The list itself is called "fixid" and it lives in the WEB-INF
 * directory.  Each line consists of two tuples separated by a comma.
 * entries may be quoted, but do not have to be.  The first is the
 * email address to map and the other is what it maps to.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 * Based on code from Whitelist.java authored by kerr@google.com (Debby Wallach)
 *
 */
public class IdMap {

  private static final Logger LOG = Logger.getLogger(IdMap.class.getName());
  private static final boolean DEBUG = true;

  // When running on appengine, the application is running in a way that
  // the rootPath should not be set to anything.  This flag needs to be
  // set for testing.
  @VisibleForTesting
  public static final Flag<String> rootPath = Flag.createFlag("root.path", "");

  private String pathTofixId = rootPath.get() + "WEB-INF/fixids";
  private boolean validfixId;

  private final HashMap<String,String> addresses = new HashMap<String,String>();

  private static IdMap INSTANCE = null;

  public static IdMap getInstance() {
    if (INSTANCE == null)
      INSTANCE = new IdMap();
    return INSTANCE;
  }

  private IdMap() {
    validfixId = false;
    try {
      FileInputStream mapfile = new FileInputStream(pathTofixId);
      parseToMap(new CsvParser(mapfile));
      mapfile.close();          // Avoid file descriptor leak, expicitly close the mapfile once we no longer need it.
      if (addresses.size() == 0) {
        LOG.severe("fixid list contained no entries.");
      } else {
        if (DEBUG) {
          logfixIdContents();
        }
        validfixId = true;
      }
    } catch (FileNotFoundException e) {
      LOG.severe("No fixid list found.");
    } catch (SecurityException e) {
      LOG.severe("Fixid list found, but wrong permission.");
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Unexpected fixid list error", e);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error parsing fixids file.", e);
    }
  }

  // If an entry doesn't exist, we are an identity function
  public String get(String email) {
    if (! validfixId) {
      if (DEBUG)
        LOG.info("IdMap.get called for " + email + " but no valid map exists.");
      // If we have not loaded a valid fixid list, no mappings
      return email;
    }

    String retval = addresses.get(email);
    if (DEBUG) {
      if (retval == null) {
        LOG.info("No mapping found for " + email);
      } else {
        LOG.info("Mapping " + email + " to " + retval);
      }
    }
    if (retval == null)
      return email;
    return retval;
  }

  private void logfixIdContents() {
    LOG.info("fixid list contains " + addresses.size() + " entries.");
    String delimiter = "";
    for (String key : addresses.keySet()) {
      LOG.info("Contents:  " + key + " => " + addresses.get(key));
    }
  }

  private void parseToMap(CsvParser parser) {
    /*
     * expected file format:
     * "emailaddress1","emailaddress2"
     *
     */

    while (parser.hasNext()) {
      List<String> line = parser.next();
      String key = line.get(0).trim();
      String value = line.get(1).trim();
      addresses.put(key, value);
    }
  }
}
