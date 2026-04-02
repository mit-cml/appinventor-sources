// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Static utility methods shared across context modules.
 */
public final class ContextUtils {

  private static final Logger LOG = Logger.getLogger(ContextUtils.class.getName());

  static final String RESOURCE_BASE =
      "/com/google/appinventor/server/aiagent/resources/";

  private ContextUtils() {
  }

  /**
   * Load a classpath resource as a string. If {@code name} starts with "/" it
   * is used as an absolute path; otherwise it is resolved relative to the
   * aiagent resources directory.
   */
  public static String loadResource(String name) {
    String path = name.startsWith("/") ? name : RESOURCE_BASE + name;
    try (InputStream is = ContextUtils.class.getResourceAsStream(path)) {
      if (is == null) {
        LOG.warning("Resource not found: " + path);
        return "(resource not available)";
      }
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(is, StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      return sb.toString();
    } catch (IOException e) {
      LOG.warning("Failed to load resource " + name + ": " + e.getMessage());
      return "(resource not available)";
    }
  }

  /**
   * Extract the JSON content from an SCM file (strips the
   * {@code #| $JSON ... |#} wrapper).
   */
  public static String extractScmJson(String scmContent) {
    if (scmContent == null || scmContent.isEmpty()) {
      return null;
    }
    int jsonStart = scmContent.indexOf("{");
    int jsonEnd = scmContent.lastIndexOf("}");
    if (jsonStart >= 0 && jsonEnd > jsonStart) {
      return scmContent.substring(jsonStart, jsonEnd + 1);
    }
    return null;
  }

  /**
   * Build an indented component tree from an SCM JSON Properties object.
   */
  public static String buildComponentTree(JSONObject properties, int depth) {
    StringBuilder sb = new StringBuilder();
    String indent = repeatIndent(depth);

    String type = properties.optString("$Type", "?");
    String name = properties.optString("$Name", "?");
    sb.append(indent).append(type).append(" (").append(name).append(")");

    // Collect non-default, non-internal properties
    List<String> propPairs = new ArrayList<>();
    for (Object keyObj : properties.keySet()) {
      String key = (String) keyObj;
      if (key.startsWith("$") || "Uuid".equals(key)) {
        continue;
      }
      propPairs.add(key + "=" + properties.get(key));
    }
    if (!propPairs.isEmpty()) {
      sb.append(" [").append(String.join(", ", propPairs)).append("]");
    }
    sb.append("\n");

    // Recurse into children
    JSONArray children = properties.optJSONArray("$Components");
    if (children != null) {
      for (int i = 0; i < children.length(); i++) {
        sb.append(buildComponentTree(children.getJSONObject(i), depth + 1));
      }
    }

    return sb.toString();
  }

  /**
   * Count the total number of components in an SCM Properties tree.
   */
  public static int countComponents(JSONObject properties) {
    int count = 1;
    JSONArray children = properties.optJSONArray("$Components");
    if (children != null) {
      for (int i = 0; i < children.length(); i++) {
        count += countComponents(children.getJSONObject(i));
      }
    }
    return count;
  }

  /**
   * Strip HTML tags, decode common entities, and collapse whitespace.
   */
  public static String stripHtml(String html) {
    if (html == null || html.isEmpty()) {
      return "";
    }
    String text = html.replaceAll("<[^>]+>", " ");
    text = text.replace("&amp;", "&")
               .replace("&lt;", "<")
               .replace("&gt;", ">")
               .replace("&quot;", "\"")
               .replace("&#39;", "'")
               .replace("&nbsp;", " ");
    text = text.replaceAll("\\s+", " ").trim();
    return text;
  }

  /**
   * Strip HTML for tutorial content extraction. Unlike {@link #stripHtml},
   * this method removes {@code <head>}, {@code <script>}, {@code <style>},
   * {@code <nav>}, {@code <footer>}, and {@code <header>} elements with
   * their contents, then strips remaining tags while preserving paragraph
   * structure.
   */
  public static String stripHtmlForTutorial(String html) {
    if (html == null || html.isEmpty()) {
      return "";
    }
    // 1. Remove elements that carry site chrome or non-content data.
    //    Pattern.DOTALL so . matches newlines within elements.
    String text = html;
    for (String tag : new String[]{"head", "script", "style", "nav", "footer", "header"}) {
      text = text.replaceAll("(?is)<" + tag + "[^>]*>.*?</" + tag + ">", "\n");
    }
    // 2. Replace block-level closing tags with newlines to preserve structure.
    text = text.replaceAll("(?i)</(p|div|li|tr|h[1-6]|blockquote|section|article|main)>", "\n");
    text = text.replaceAll("(?i)<br\\s*/?>", "\n");
    // 3. Remove all remaining HTML tags.
    text = text.replaceAll("<[^>]+>", " ");
    // 4. Decode common HTML entities.
    text = text.replace("&amp;", "&")
               .replace("&lt;", "<")
               .replace("&gt;", ">")
               .replace("&quot;", "\"")
               .replace("&#39;", "'")
               .replace("&nbsp;", " ");
    // 5. Collapse runs of whitespace within lines, then collapse 3+ newlines to 2.
    text = text.replaceAll("[ \\t]+", " ");
    text = text.replaceAll(" *\\n *", "\n");
    text = text.replaceAll("\\n{3,}", "\n\n");
    return text.trim();
  }

  /**
   * Truncate to the first sentence (period followed by whitespace or end),
   * capped at 150 characters.
   */
  public static String truncateDescription(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    int end = -1;
    for (int i = 0; i < text.length() - 1; i++) {
      if (text.charAt(i) == '.'
          && (i + 1 >= text.length() || Character.isWhitespace(text.charAt(i + 1)))) {
        end = i + 1;
        break;
      }
    }
    if (end < 0 && text.endsWith(".")) {
      end = text.length();
    }
    if (end < 0) {
      end = text.length();
    }
    String result = text.substring(0, end).trim();
    if (result.length() > 150) {
      result = result.substring(0, 147) + "...";
    }
    return result;
  }

  /**
   * Return an indentation string (two spaces per depth level).
   */
  public static String repeatIndent(int depth) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      sb.append("  ");
    }
    return sb.toString();
  }
}
