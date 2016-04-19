// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// This module is an unreleased work.

package com.google.appinventor.client.admin;

import java.util.Comparator;
import java.util.Date;

import com.google.appinventor.shared.rpc.admin.AdminUser;


/**
 * Comparators for {@link AdminUsers}.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 * Based on the ProjectComparators written by lizlooney@google.com (Liz Looney)
 *
 */
public final class AdminComparators {
  private AdminComparators() {
  }

  public static final Comparator<AdminUser> COMPARE_BY_NAME_ASCENDING = new Comparator<AdminUser>() {
    @Override
    public int compare(AdminUser user1, AdminUser user2) {
      String user1Name = user1.getEmail();
      String user2Name = user2.getEmail();
      return user1Name.compareToIgnoreCase(user2Name); // ascending
    }
  };

  public static final Comparator<AdminUser> COMPARE_BY_NAME_DESCENDING = new Comparator<AdminUser>() {
    @Override
    public int compare(AdminUser user1, AdminUser user2) {
      String user1Name = user1.getEmail();
      String user2Name = user2.getEmail();
      return user2Name.compareToIgnoreCase(user1Name); // ascending
    }
  };

  public static final Comparator<AdminUser> COMPARE_BY_VISTED_DATE_ASCENDING = new Comparator<AdminUser>() {
    @Override
    public int compare(AdminUser user1, AdminUser user2) {
      Date date1 = user1.getVisited();
      Date date2 = user2.getVisited();
      if (date1 == null && date2 == null) {
        return 0;
      } else if (date1 == null) {
        return 1;
      } else if (date2 == null) {
        return -1;
      } else {
        return date1.compareTo(date2);
      }
    }
  };

  public static final Comparator<AdminUser> COMPARE_BY_VISTED_DATE_DESCENDING = new Comparator<AdminUser>() {
    @Override
    public int compare(AdminUser user1, AdminUser user2) {
      Date date1 = user1.getVisited();
      Date date2 = user2.getVisited();
      if (date1 == null && date2 == null) {
        return 0;
      } else if (date1 == null) {
        return -1;
      } else if (date2 == null) {
        return 1;
      } else {
        return date2.compareTo(date1);
      }
    }
  };

}
