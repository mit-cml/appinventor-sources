// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.context.ContextUtils;
import com.google.appinventor.server.storage.StorageIo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Fetches tutorial page HTML, strips it to plain text, and caches results
 * in memory with an 8-hour TTL. Max 100 entries with oldest-first eviction.
 *
 * <p>URLs are validated against the tutorial allowlist from
 * {@link StorageIo#getTutorialsUrlAllowed()} before any HTTP request is made.
 */
public class TutorialContentCache {

  private static final Logger LOG = Logger.getLogger(TutorialContentCache.class.getName());

  static final int CONNECT_TIMEOUT_MS = 5000;
  static final int READ_TIMEOUT_MS = 10000;
  static final long TTL_MS = 8 * 60 * 60 * 1000; // 8 hours
  static final int MAX_ENTRIES = 100;

  private final StorageIo storageIo;
  private final ConcurrentHashMap<String, CachedEntry> cache = new ConcurrentHashMap<>();

  static class CachedEntry {
    final String text;
    final long createdAt;

    CachedEntry(String text) {
      this.text = text;
      this.createdAt = System.currentTimeMillis();
    }

    boolean isExpired() {
      return System.currentTimeMillis() - createdAt > TTL_MS;
    }
  }

  public TutorialContentCache(StorageIo storageIo) {
    this.storageIo = storageIo;
  }

  /**
   * Returns plain text content for the tutorial URL, or {@code null} if
   * the URL is not in the allowlist, the fetch fails, or the URL is empty.
   */
  public String get(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }

    // Validate against allowlist
    List<String> allowed = storageIo.getTutorialsUrlAllowed();
    if (!isUrlAllowed(url, allowed)) {
      AIDebug.log(LOG, "Tutorial URL not in allowlist: " + url);
      return null;
    }

    // Check cache
    CachedEntry entry = cache.get(url);
    if (entry != null && !entry.isExpired()) {
      return entry.text;
    }

    // Fetch, strip, cache
    String html = fetchUrl(url);
    if (html == null) {
      return null;
    }

    String text = ContextUtils.stripHtmlForTutorial(html);
    evictIfNeeded();
    cache.put(url, new CachedEntry(text));
    return text;
  }

  /**
   * Checks whether a URL matches any of the allowed tutorial URL prefixes.
   */
  static boolean isUrlAllowed(String url, List<String> allowedPrefixes) {
    if (url == null || url.isEmpty() || allowedPrefixes == null) {
      return false;
    }
    for (String prefix : allowedPrefixes) {
      if (url.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Fetches the URL content as a string. Follows redirects. Returns
   * {@code null} on any failure.
   */
  String fetchUrl(String url) {
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
      conn.setReadTimeout(READ_TIMEOUT_MS);
      conn.setInstanceFollowRedirects(true);
      conn.setRequestProperty("User-Agent", "AppInventor-AIAgent/1.0");

      int status = conn.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        LOG.warning("Tutorial fetch failed: HTTP " + status + " for " + url);
        return null;
      }

      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int read;
        while ((read = reader.read(buf)) != -1) {
          sb.append(buf, 0, read);
        }
        return sb.toString();
      }
    } catch (IOException e) {
      LOG.warning("Tutorial fetch error for " + url + ": " + e.getMessage());
      return null;
    }
  }

  /**
   * Evicts the oldest entry if the cache has reached {@link #MAX_ENTRIES}.
   */
  private void evictIfNeeded() {
    if (cache.size() < MAX_ENTRIES) {
      return;
    }
    String oldestKey = null;
    long oldestTime = Long.MAX_VALUE;
    for (Map.Entry<String, CachedEntry> e : cache.entrySet()) {
      if (e.getValue().createdAt < oldestTime) {
        oldestTime = e.getValue().createdAt;
        oldestKey = e.getKey();
      }
    }
    if (oldestKey != null) {
      cache.remove(oldestKey);
    }
  }
}
