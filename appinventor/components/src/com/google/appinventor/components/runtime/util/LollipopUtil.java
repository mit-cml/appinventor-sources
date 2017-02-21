// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.Locale;

import android.telephony.PhoneNumberUtils;

/**
 * Helper methods for calling APIs added in LOLLIPOP (5.0, API Level 21)
 *
 * @author Evan W. Patton (ewpatton@mit.edu)
 *
 */
public final class LollipopUtil {

  private LollipopUtil() {
  }

  /**
   * Format a phone number based on the number's country code, falling
   * back to the format defined by the user's current locale. This is
   * to replace calling {@link PhoneNumberUtils#formatNumber(String)},
   * which was deprecated in the LOLLIPOP release.
   *
   * @see PhoneNumberUtils#formatNumber(String, String)
   * @param number The phone number to be formatted
   * @return The phone number, formatted based on the country code or
   * user's locale.
   */
  public static String formatNumber(String number) {
    return PhoneNumberUtils.formatNumber(number, Locale.getDefault().getCountry());
  }
}
