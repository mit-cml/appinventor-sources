// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

// Note: This code is no longer used but is kept here as an example and in case we
//       wish to switch back to a internal file based version of a whitelist

package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.util.CsvParser;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
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
  /**
   * Class representing a case-insensitive email address.
   */
  private static class EmailAddress {
    private final String email;

    EmailAddress(String email) {
      Preconditions.checkNotNull(email);
      this.email = email;
    }

    @Override
    public String toString() {
      return email;
    }

    @Override
    public boolean equals(Object other) {
      boolean result = (other instanceof EmailAddress) &&
          ((EmailAddress) other).email.equalsIgnoreCase(email);
      return result;
    }

    @Override
    public int hashCode() {
      return email.toLowerCase().hashCode();
    }
  }

  private static final Logger LOG = Logger.getLogger(Whitelist.class.getName());
  private static final boolean DEBUG = false;

  // When running on appengine, the application is running in a way that
  // the rootPath should not be set to anything.  This flag needs to be
  // set for testing.
  @VisibleForTesting
  public static final Flag<String> rootPath = Flag.createFlag("root.path", "");

  private String pathToWhitelist = rootPath.get() + "WEB-INF/whitelist";
  private boolean validWhitelist;

  private final Set<EmailAddress> addresses = new HashSet<EmailAddress>();

  Whitelist() {
    validWhitelist = false;
    try {
      parseToAddresses(new CsvParser(new FileInputStream(pathToWhitelist)));
      if (addresses.size() == 0) {
        LOG.severe("Whitelist file contained no entries.");
      } else {
        if (DEBUG) {
          logWhitelistContents();
        }
        validWhitelist = true;
      }
    } catch (FileNotFoundException e) {
      LOG.severe("No whitelist found.");
    } catch (SecurityException e) {
      LOG.severe("Whitelist found, but wrong permission.");
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Unexpected whitelist error", e);
    }
  }

  // should throw something if not valid whitelist?
  public boolean isInWhitelist(LocalUser user) {
    if (! validWhitelist) {
      // If we have not loaded a valid whitelist, reject everyone.
      return false;
    }

    boolean found = addresses.contains(new EmailAddress(user.getUserEmail()));
    if (!found) {
      LOG.info("User with email address " + user.getUserEmail() +
          " was not found in the whitelist");
    }
    return found;
  }

  private void logWhitelistContents() {
    LOG.info("Whitelist contains " + addresses.size() + " addresses.");
    StringBuilder sb = new StringBuilder();
    String delimiter = "";
    for (EmailAddress address : addresses) {
      sb.append(delimiter).append(address);
      if (sb.length() > 70) {
        LOG.info("On whitelist: " + sb);
        sb = new StringBuilder();
        delimiter = "";
      } else {
        delimiter = ",";
      }
    }
    if (sb.length() > 0) {
      LOG.info("On whitelist: " + sb);
    }
  }

  private void parseToAddresses(CsvParser parser) {
    /*
     * expected file format:
     * "emailaddress1"
     * "emailaddress2"
     *
     */
    Pattern patternPlus = Pattern.compile("([^+]*)(\\+[^@]*)(@.*)");

    while (parser.hasNext()) {
      List<String> line = parser.next();
      String address = line.get(0).trim();

      // Change foo+bar@gmail.com to foo@gmail.com
      Matcher matcher = patternPlus.matcher(address);
      if (matcher.matches()) {
        address = matcher.group(1) + matcher.group(3);
      }

      addresses.add(new EmailAddress(address));
    }
  }
}
