// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime.util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;

/**
 * Helper methods for calling methods added in Gingerbread (2.3, API level 9).
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class GingerbreadUtil {
  private GingerbreadUtil() {
  }

  /**
   * Creates a new CookieManager instance.
   */
  public static CookieHandler newCookieManager() {
    return new CookieManager();
  }

  /**
   * Clears the cookies in the given cookie handler. Cookies can only be cleared if the
   * cookieHandler is a CookieManager with a non-null CookieStore.
   *
   * @param cookieHandler the cookie handler where cookies should be cleared
   * @return true if cookies were cleared; false otherwise
   */
  public static boolean clearCookies(CookieHandler cookieHandler) {
    if (cookieHandler instanceof CookieManager) {
      CookieManager cookieManager = (CookieManager) cookieHandler;
      CookieStore cookieStore = cookieManager.getCookieStore();
      if (cookieStore != null) {
        cookieStore.removeAll();
        return true;
      }
    }
    return false;
  }
}
