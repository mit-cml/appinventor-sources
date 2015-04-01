// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;


import java.text.*;
import java.util.Locale;

/**
 * Convert inexact numbers to strings for printing in App Inventor.
 * Kawa's default shows too many decimal places.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
public final class YailNumberToString {

  static final String LOG_TAG = "YailNumberToString";

  // format magnitudes larger than BIGBOUND in scientific notation
  private static final double BIGBOUND = 1.e6;
  // format magnitudes smaller than SMALLBOUND in scientific notation
  private static final double SMALLBOUND = 1.e-6;


  // TODO(halabelson): The Java documentation warns that formatters are
  // not thread-safe.  Is there any way that this can bite us?
  // format for decimal notation
  private static final String decPattern = "#####0.0####";
  // format for scientific notation
  private static final String sciPattern = "0.####E0";

  // TODO(hal): We are making the decimal separator be a period, regardless of
  // the locale of the phone.   We need to think about how to allow comma as decimal separator,
  // which will require updating number parsing and other places that transform numbers to strings,
  // such as FormatAsDecimal

  static Locale locale = Locale.US;
  static DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);

  static DecimalFormat decimalFormat = new DecimalFormat(decPattern, symbols);
  static DecimalFormat sciFormat = new DecimalFormat(sciPattern, symbols);


  // TODO(halabelson): DecimalFormat scientific notation apparently does not provide a
  // way to specify the maximum number of digits in the mantissa. One consequence is that this
  // formatting method, when given floats of very large magnitude, will produce too many
  // places in the mantissa.  Consider post-processing the result of
  // DecimalFormat to remove these extra digits.

  // This implementation assumes that Kawa inexact numbers are passed to this routine
  // as doubles.
  public static String format(double number) {
    // We will print integer values without a decimal point.
    if (number == Math.rint(number)) {
      return String.valueOf((int) number);
    } else {
      double mag = Math.abs(number);
      if (mag < BIGBOUND && mag > SMALLBOUND) {
        return decimalFormat.format(number);
      } else {
        return sciFormat.format(number);
      }
    }
  }
}
