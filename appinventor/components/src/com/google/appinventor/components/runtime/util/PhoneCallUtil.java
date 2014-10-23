// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Helper functions for making phone calls.
 * TODO(sharon): eventually support making conference calls when the Android
 * framework supports it via an API
 * 
 * @author sharon@google.com (Sharon Perl)
 */

public class PhoneCallUtil {

  private PhoneCallUtil() {
  }
  
  public static void makePhoneCall(Context context, String phoneNumber) {
    if (null != phoneNumber && phoneNumber.length() > 0) {
      Uri phoneUri = Uri.parse("tel:" + phoneNumber);   // Could also use Uri.Builder
      Intent intent = new Intent(Intent.ACTION_CALL, phoneUri);
      context.startActivity(intent);
    }
  }
}

