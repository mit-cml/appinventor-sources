// Copyright 20011 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.util.CsvParser;
import com.google.common.annotations.VisibleForTesting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of a whitelist.  Note that it uses email addresses
 * (which can change), and not user ids (which are unique), because we
 * do not expect that the people creating the whitelist have access to
 * the user ids of users.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class Whitelist {
  private static final Logger LOG = Logger.getLogger(Whitelist.class.getName());
  private static final boolean DEBUG = true;

  // When running on appengine, the application is running in a way that
  // the rootPath should not be set to anything.  This flag needs to be
  // set for testing.
  @VisibleForTesting
  public static final Flag<String> rootPath = Flag.createFlag("root.path", "");

  String pathToWhitelist = rootPath.get() + "whitelist";
  boolean validWhitelist = false;

  private final Set<String> addresses = new HashSet<String>();

  Whitelist() {
    validWhitelist = false;
    try {
      parseToAddresses(new CsvParser(new FileInputStream(pathToWhitelist)));
      if (addresses.size() == 0) {
        LOG.severe("White file found with no entries.");
      } else {
        if (DEBUG) {
          logSuccesses();
        }
        validWhitelist = true;
      }
    } catch (FileNotFoundException e) {
      LOG.info("No whitelist found.");
    } catch (SecurityException e) {
      LOG.severe("Whitelist found, but wrong permission... ignoring.");
    } catch (IOException e) {
      LOG.severe("Unexpected whitelist error: " + e);
    }

  }

  // should throw something if not valid whitelist?
  public Boolean isInWhitelist(LocalUser user) {
    if (! validWhitelist) {
      // If we have not found a valid whitelist, default to using no
      // whitelist at all.
      return false;
    }

    return addresses.contains(user.getUserEmail());
  }

  private void logSuccesses() {
    final int INCR = 500;
    final int num = addresses.size();
    Iterator<String> iterator = addresses.iterator();
    int i = 0;
    String s = "";
    while (iterator.hasNext()) {
      s = s + iterator.next();
      if (i == INCR) {
        LOG.info("On whitelist: " + s);
        i = 0;
        s = "";
      } else {
        s += ",";
      }
    }
    if (s.length() > 0) {
      LOG.info("On whitelist: " + s);
    }
  }

  private void parseToAddresses(CsvParser parser) {
    /*
     * expected file format:
     * "emailaddress1"
     * "emailaddress2"
     *
     */
    final int expectedMinNumFields = 2; // for the case when each column is not just a single addr.
    Pattern patternGooglemail = Pattern.compile("([^@]*)@googlemail\\.com");
    Pattern patternPlus = Pattern.compile("([^+]*)(\\+[^@]*)(@.*)");

    Matcher matcher;

    while (parser.hasNext()) {
      List<String> line = parser.next();
      String[] fields = line.toArray(new String[line.size()]);
      String address = fields[0].trim();

      // Change googlemail.com to gmail.com (we expect this is necessary)
      matcher = patternGooglemail.matcher(address);
      if (matcher.matches()) {
        address = matcher.group(1) + "@gmail.com";
      }
      // Change foo+bar@gmail.com to foo@gmail.com
      matcher = patternPlus.matcher(address);
      if (matcher.matches()) {
        address = matcher.group(1) + matcher.group(3);
      }
      addresses.add(address);
    }
  }
}
