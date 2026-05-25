// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

/**
 * Extracts the canonical identity string (YAIL head tokens) from
 * {@code write_block} / {@code delete_block} payloads, so the server can
 * detect when multiple operations in one batch target the same top-level
 * block.
 *
 * <p>Identity strings mirror the client-side
 * {@code AI.YailToBlocks.getUpsertIdentifier_} and
 * {@code findBlockByIdentifier_} conventions:
 * <ul>
 *   <li>{@code "define-event ComponentName EventName"}</li>
 *   <li>{@code "define-generic-event TypeName EventName"}</li>
 *   <li>{@code "def g$VarName"}</li>
 *   <li>{@code "def p$ProcName"} (also for {@code def-return}; normalized
 *       to {@code "def"} since an identity collision between a procedure
 *       and a returnless def with the same name is still a conflict)</li>
 * </ul>
 *
 * <p>This is a pure-text extractor; it does not validate that the YAIL
 * parses or runs.  Unknown / malformed inputs produce {@code null}.
 */
public final class BlockIdentity {

  private BlockIdentity() {
    // Utility class.
  }

  /**
   * Extract the identity from a {@code write_block} YAIL string.
   *
   * @param yail the YAIL S-expression (may start with whitespace)
   * @return identity string, or {@code null} if the form is unrecognized
   */
  public static String fromWriteYail(String yail) {
    if (yail == null) {
      return null;
    }
    Tokenizer t = new Tokenizer(yail);
    if (!t.expect('(')) {
      return null;
    }
    String head = t.nextToken();
    if (head == null) {
      return null;
    }
    switch (head) {
      case "define-event":
      case "define-generic-event": {
        String component = t.nextToken();
        String event = t.nextToken();
        if (component == null || event == null) {
          return null;
        }
        return head + " " + component + " " + event;
      }
      case "def":
      case "def-return": {
        // Next item is either a bare g$ symbol (global) or a
        // parenthesized (p$ProcName ...) form (procedure).
        t.skipWhitespace();
        if (t.peek() == '(') {
          t.consume();
          String procName = t.nextToken();
          if (procName == null) {
            return null;
          }
          return "def " + procName;
        }
        String name = t.nextToken();
        if (name == null) {
          return null;
        }
        return "def " + name;
      }
      default:
        return null;
    }
  }

  /**
   * Normalize an identifier passed to {@code delete_block}.  The payload
   * already carries head tokens, so we only collapse whitespace and map
   * {@code def-return} -> {@code def} so a global/procedure identity
   * compares equal whether it was created via {@code def} or
   * {@code def-return}.
   *
   * @param blockId the raw {@code block} field from a DELETE_BLOCK payload
   * @return canonical identity string, or {@code null} if {@code blockId}
   *     is null/blank/unrecognized
   */
  public static String fromDeleteIdentifier(String blockId) {
    if (blockId == null) {
      return null;
    }
    String trimmed = blockId.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    String[] tokens = trimmed.split("\\s+");
    if (tokens.length == 0) {
      return null;
    }
    String head = tokens[0];
    switch (head) {
      case "define-event":
      case "define-generic-event":
        if (tokens.length < 3) {
          return null;
        }
        return head + " " + tokens[1] + " " + tokens[2];
      case "def":
      case "def-return":
        if (tokens.length < 2) {
          return null;
        }
        return "def " + tokens[1];
      default:
        return null;
    }
  }

  /** Minimal whitespace/paren-aware tokenizer over a YAIL source string. */
  private static final class Tokenizer {
    private final String src;
    private int pos;

    Tokenizer(String src) {
      this.src = src;
      this.pos = 0;
    }

    void skipWhitespace() {
      while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
        pos++;
      }
    }

    char peek() {
      skipWhitespace();
      return pos < src.length() ? src.charAt(pos) : '\0';
    }

    void consume() {
      pos++;
    }

    boolean expect(char ch) {
      skipWhitespace();
      if (pos < src.length() && src.charAt(pos) == ch) {
        pos++;
        return true;
      }
      return false;
    }

    /**
     * Read the next whitespace/paren-delimited token.  Returns {@code null}
     * at end of input or when a paren is encountered before any token
     * characters.
     */
    String nextToken() {
      skipWhitespace();
      int start = pos;
      while (pos < src.length()) {
        char c = src.charAt(pos);
        if (Character.isWhitespace(c) || c == '(' || c == ')') {
          break;
        }
        pos++;
      }
      if (pos == start) {
        return null;
      }
      return src.substring(start, pos);
    }
  }
}
