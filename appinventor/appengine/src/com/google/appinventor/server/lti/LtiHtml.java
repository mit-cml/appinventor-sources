// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

/**
 * Shared markup helpers for the LTI servlets that build pages by hand. Escapes
 * the five characters that matter in element text and in single or double quoted
 * attribute values, and builds the common page chrome so every served page looks
 * the same.
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

  private static final String STYLE =
      "*{box-sizing:border-box}"
      + "body{margin:0;padding:1.5rem;background:#f1f3f4;color:#202124;"
      + "font:16px/1.55 -apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif}"
      + "main{max-width:560px;margin:1.5rem auto;padding:2rem;background:#fff;"
      + "border-radius:12px;box-shadow:0 1px 3px rgba(60,64,67,.2)}"
      + ".brand{margin:0 0 1rem;font-size:.75rem;font-weight:600;letter-spacing:.08em;"
      + "text-transform:uppercase;color:#1a73e8}"
      + "h1{margin:0 0 .75rem;font-size:1.375rem;font-weight:500;line-height:1.3;color:#0d47a1}"
      + "p{margin:.75rem 0;color:#3c4043}"
      + "ul{list-style:none;margin:1.25rem 0;padding:0;max-height:50vh;overflow:auto}"
      + "label.opt{display:flex;align-items:center;gap:.75rem;padding:.75rem .875rem;"
      + "margin:.5rem 0;border:1px solid #dadce0;border-radius:8px;cursor:pointer}"
      + "label.opt:hover{border-color:#1a73e8;background:#f8fbff}"
      + "label.opt:focus-within{border-color:#1a73e8;outline:3px solid #0d47a1}"
      + "input[type=radio]{flex:none;width:1.15rem;height:1.15rem;accent-color:#1a73e8}"
      + ".btn{appearance:none;margin-top:1rem;padding:.7rem 1.4rem;border:0;border-radius:8px;"
      + "background:#1a73e8;color:#fff;font-size:1rem;font-weight:500;cursor:pointer}"
      + ".btn:hover{background:#0d47a1}"
      + ".btn:focus-visible{outline:3px solid #0d47a1;outline-offset:2px}"
      + "@media(max-width:600px){body{padding:0}"
      + "main{margin:0;border-radius:0;min-height:100vh;box-shadow:none}}";

  /**
   * Opens a styled App Inventor page with a valid doctype, language, charset, and
   * mobile viewport, and one inline stylesheet so it stays self contained in the
   * sandboxed window an LMS opens. Pair every call with {@link #pageFoot()}.
   */
  static String pageHead(String title) {
    return pageHead(title, "");
  }

  /**
   * As {@link #pageHead(String)}, with trusted extra attributes on the body tag.
   */
  static String pageHead(String title, String bodyAttrs) {
    return "<!DOCTYPE html><html lang='en'><head><meta charset='utf-8'>"
        + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
        + "<title>" + escape(title) + "</title><style>" + STYLE + "</style></head><body"
        + (bodyAttrs.isEmpty() ? "" : " " + bodyAttrs) + "><main>"
        + "<p class='brand'>App Inventor</p>";
  }

  static String pageFoot() {
    return "</main></body></html>";
  }
}
