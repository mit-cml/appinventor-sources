// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Provides escape functions.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class Escapers {

  private Escapers() {
  }

  /**
   * Returns an escaped (safe for xml) version of the given string.
   */
  public static String escapeForXml(String s) {
    return s.replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");
  }

  /**
   * Returns an encoded version the given string by replacing special
   * characters with backslash escaped sequences.
   */
  public static String encodeInternationalCharacters(String s) {
    StringBuilder sb = new StringBuilder();
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
        case '"':
        case '/':
          sb.append('\\').append(c);
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < ' ' || c > '~') {
            // Replace any special chars with \u1234 unicode
            String hex = "000" + Integer.toHexString(c);
            hex = hex.substring(hex.length() - 4);
            sb.append("\\u" + hex);
          } else {
            sb.append(c);
          }
          break;
      }
    }
    return sb.toString();
  }

  /**
   * Returns a decoded version of the given String, assuming the given string
   * was previously encoded with encodeInternationalCharacters.
   */
  public static String decodeInternationalCharacters(String s) {
    try {
      // Make the JSON parser do the work for us.
      JSONObject o = new JSONObject("{\"Text\":\"" + s + "\"}");
      return o.getString("Text");
    } catch (JSONException e) {
      // Something went wrong. Just return the original text.
      return s;
    }
  }
}
