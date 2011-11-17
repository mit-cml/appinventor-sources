// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime.util;

import java.text.*;

public final class YailNumberToString {

  /**
   * Convert inexact numbers to strings for printing in App Inventor.
   * Kawa's default shows too many decimal places.
   * @author halabelson@google.com (Hal Abelson)
   */

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

  // TODO(halabelson): DecimalFormat scientific notation apparently does not provide a
  // way to specify the maximum number of digits in the mantissa. One consequence is that this
  // formatting method, when given floats of very large magnitude, will produce too many
  // places in the mantissa.  Consider post-processing the result of
  // DecimalFormat to remove these extra digits.
  private static final DecimalFormat formatterDec = new DecimalFormat(decPattern);
  private static final DecimalFormat formatterSci = new DecimalFormat(sciPattern);

  // This implementation assumes that Kawa inexact numbers can be passed to this routine
  // as doubles.
  public static String format(double number) {
    double mag = Math.abs(number);
    if (mag < BIGBOUND && mag > SMALLBOUND) {
      return formatterDec.format(number);
    } else {
      return formatterSci.format(number);
    }
  }
}
