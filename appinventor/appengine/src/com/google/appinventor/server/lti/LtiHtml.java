// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

/**
 * Small HTML escaping helper shared by the LTI servlets that build markup by
 * hand (the template picker and the auto submitting Deep Linking response).
 * Escapes the five characters that matter in element text and in single or
 * double quoted attribute values.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiHtml {

  private LtiHtml() {}

  /** Escapes a string for safe inclusion in HTML text or a quoted attribute. */
  static String escape(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;");
  }
}
