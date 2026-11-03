/**
 * theme-toggle.js
 * ─────────────────────────────────────────────────────────────────────────────
 * Copyright 2024 MIT, All Rights Reserved.
 * Released under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Manages the light / dark / system theme preference for MIT App Inventor.
 *
 * Preference priority (highest → lowest):
 *   1. Explicit user choice stored in localStorage ("light" | "dark")
 *   2. OS / browser prefers-color-scheme media query  (handled purely in CSS)
 *
 * The chosen value is written to  html[data-theme]  so CSS custom-property
 * overrides in theme.css can take effect immediately.
 * ─────────────────────────────────────────────────────────────────────────────
 */

(function () {
  'use strict';

  var STORAGE_KEY = 'ai-theme';        // localStorage key
  var LIGHT       = 'light';
  var DARK        = 'dark';
  var SYSTEM      = 'system';

  // ── Helpers ──────────────────────────────────────────────────────────────

  /** Return the raw stored preference, or null when none is saved. */
  function stored() {
    try { return localStorage.getItem(STORAGE_KEY); } catch (_) { return null; }
  }

  /** Persist the user's choice. Pass null to clear (revert to system). */
  function save(value) {
    try {
      if (value === null) {
        localStorage.removeItem(STORAGE_KEY);
      } else {
        localStorage.setItem(STORAGE_KEY, value);
      }
    } catch (_) { /* storage unavailable — degrade gracefully */ }
  }

  /**
   * Resolve the *effective* theme that should be applied right now.
   * Returns "light" or "dark".
   */
  function resolveTheme() {
    var pref = stored();
    if (pref === LIGHT || pref === DARK) return pref;
    // Fall back to OS preference
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return DARK;
    }
    return LIGHT;
  }

  /** Apply a theme string ("light" | "dark") to the <html> element. */
  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
  }

  /** Update every toggle button's icon and aria-label to match the theme. */
  function updateButtons(theme) {
    var buttons = document.querySelectorAll('#ai-theme-toggle');
    for (var i = 0; i < buttons.length; i++) {
      var btn  = buttons[i];
      var icon = btn.querySelector('.ai-theme-icon');
      if (theme === DARK) {
        if (icon) icon.textContent = '🌙';
        btn.setAttribute('aria-label', 'Switch to light theme');
        btn.setAttribute('title',      'Switch to light theme');
      } else {
        if (icon) icon.textContent = '☀️';
        btn.setAttribute('aria-label', 'Switch to dark theme');
        btn.setAttribute('title',      'Switch to dark theme');
      }
    }
  }

  // ── Core: apply on load ──────────────────────────────────────────────────

  var currentTheme = resolveTheme();
  applyTheme(currentTheme);

  // ── Toggle logic ─────────────────────────────────────────────────────────

  /**
   * Called when the user clicks a toggle button.
   * Flips between light and dark and saves the choice.
   */
  function handleToggle() {
    currentTheme = (currentTheme === DARK) ? LIGHT : DARK;
    save(currentTheme);
    applyTheme(currentTheme);
    updateButtons(currentTheme);
  }

  // ── Inject toggle button(s) once the DOM is ready ────────────────────────

  function createToggleButton() {
    var btn = document.createElement('button');
    btn.id = 'ai-theme-toggle';
    btn.setAttribute('type', 'button');

    var icon = document.createElement('span');
    icon.className = 'ai-theme-icon';
    icon.setAttribute('aria-hidden', 'true');
    btn.appendChild(icon);

    btn.addEventListener('click', handleToggle);
    return btn;
  }

  function injectToggle() {
    updateButtons(currentTheme); // update any server-rendered buttons

    // ── Inject into the IDE top toolbar (ode-TopToolbar) ──────────────────
    var toolbar = document.querySelector('.ode-TopToolbar') ||
                  document.querySelector('div.TopToolbar');
    if (toolbar) {
      var ideBtn = createToggleButton();
      ideBtn.style.marginLeft = 'auto';
      toolbar.appendChild(ideBtn);
      updateButtons(currentTheme);
    }

    // ── Inject into the public homepage navbar ────────────────────────────
    var navbar = document.querySelector('.mit_app_inventor .navbar') ||
                 document.querySelector('nav.navbar');
    if (navbar) {
      var navBtn = createToggleButton();
      navBtn.style.marginLeft = '1rem';
      navbar.appendChild(navBtn);
      updateButtons(currentTheme);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', injectToggle);
  } else {
    injectToggle();
  }

  // ── React to OS-level theme changes in real time ─────────────────────────

  if (window.matchMedia) {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function (e) {
      // Only honour OS change when the user has NOT pinned a preference
      if (!stored()) {
        currentTheme = e.matches ? DARK : LIGHT;
        applyTheme(currentTheme);
        updateButtons(currentTheme);
      }
    });
  }

  // ── Expose a small public API for GWT / other scripts ────────────────────
  window.AppInventorTheme = {
    /**
     * Set the theme explicitly.
     * @param {"light"|"dark"|"system"} value
     */
    set: function (value) {
      if (value === SYSTEM) {
        save(null);
        currentTheme = resolveTheme();
      } else {
        save(value);
        currentTheme = value;
      }
      applyTheme(currentTheme);
      updateButtons(currentTheme);
    },
    /** Return the currently active theme ("light" or "dark"). */
    get: function () { return currentTheme; }
  };

}());

