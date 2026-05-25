// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.companion;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Scans YAIL source for symbol references a runtime-read snapshot
 * should query: component-property getters and global-variable getters.
 *
 * Used by the "Ask AI about this error" flow to enrich context with
 * live runtime state. The scan is regex-based and best-effort — callers
 * should cap results at a sensible bound.
 */
public final class BlockYailSymbolScanner {

  private BlockYailSymbolScanner() {}

  /**
   * Symbol kinds this scanner recognizes.
   */
  public enum Kind { PROPERTY, VARIABLE }

  /**
   * A single symbol reference found in YAIL.
   */
  public static final class Symbol {
    public final Kind kind;
    public final String componentOrVar;  // component name for PROPERTY, variable name for VARIABLE
    public final String propertyName;    // property name for PROPERTY, null for VARIABLE

    private Symbol(Kind kind, String a, String b) {
      this.kind = kind;
      this.componentOrVar = a;
      this.propertyName = b;
    }

    public static Symbol property(String component, String property) {
      return new Symbol(Kind.PROPERTY, component, property);
    }

    public static Symbol variable(String name) {
      return new Symbol(Kind.VARIABLE, name, null);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Symbol)) return false;
      Symbol s = (Symbol) o;
      if (kind != s.kind) return false;
      if (!componentOrVar.equals(s.componentOrVar)) return false;
      if (propertyName == null) return s.propertyName == null;
      return propertyName.equals(s.propertyName);
    }

    @Override
    public int hashCode() {
      int h = kind.hashCode();
      h = 31 * h + componentOrVar.hashCode();
      h = 31 * h + (propertyName == null ? 0 : propertyName.hashCode());
      return h;
    }

    @Override
    public String toString() {
      return kind == Kind.PROPERTY
          ? "PROPERTY(" + componentOrVar + "." + propertyName + ")"
          : "VARIABLE(" + componentOrVar + ")";
    }
  }

  // (get-property 'ComponentName 'PropertyName)
  private static final String GET_PROPERTY_PATTERN =
      "\\(get-property\\s+'([A-Za-z_][A-Za-z0-9_]*)\\s+'([A-Za-z_][A-Za-z0-9_]*)\\)";

  // (get-var g$VarName)   — must not match p$ (procedures) or other g$-prefixed things with hyphens.
  private static final String GET_VAR_PATTERN =
      "\\(get-var\\s+g\\$([A-Za-z_][A-Za-z0-9_]*)\\)";

  /**
   * Scan the given YAIL for property/variable references.
   *
   * <p>Uses {@link com.google.gwt.regexp.shared.RegExp} rather than
   * {@code java.util.regex.Pattern} so the class compiles under both the
   * JVM (for unit tests) and GWT (for production client code). The
   * patterns themselves are JavaScript-compatible.
   *
   * @param yail YAIL source (typically the current screen's blocksYail)
   * @param cap maximum number of unique symbols returned; once the cap is
   *            reached further matches are ignored (insertion order is
   *            preserved so a failing block's references tend to come first
   *            when callers pass per-block YAIL).
   * @return deduped list of symbols in insertion order, capped.
   */
  public static List<Symbol> scan(String yail, int cap) {
    if (yail == null || yail.isEmpty() || cap <= 0) {
      return new ArrayList<Symbol>();
    }
    Set<Symbol> seen = new LinkedHashSet<Symbol>();

    RegExp getProperty = RegExp.compile(GET_PROPERTY_PATTERN, "g");
    MatchResult m;
    while (seen.size() < cap && (m = getProperty.exec(yail)) != null) {
      seen.add(Symbol.property(m.getGroup(1), m.getGroup(2)));
    }

    if (seen.size() < cap) {
      RegExp getVar = RegExp.compile(GET_VAR_PATTERN, "g");
      while (seen.size() < cap && (m = getVar.exec(yail)) != null) {
        seen.add(Symbol.variable(m.getGroup(1)));
      }
    }

    return new ArrayList<Symbol>(seen);
  }
}
