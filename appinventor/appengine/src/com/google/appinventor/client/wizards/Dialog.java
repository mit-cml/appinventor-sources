// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import java.util.Iterator;

/**
 * Accessible wrapper for the GWT DialogBox that provides ARIA support and
 * keyboard navigation following WCAG 2.1 guidelines.
 *
 * This class extends DialogBox with:
 * - ARIA attributes (role, aria-modal, aria-labelledby, aria-describedby)
 * - Global Escape key handling to close dialogs
 * - Focus management (auto-focus on open, restore on close)
 * - UIBinder support for all accessibility features
 */
public class Dialog extends DialogBox {

  private Caption caption;
  private String ariaRole = "dialog";  // Default role
  private com.google.gwt.dom.client.Element triggerElement = null;  // For focus restoration
  private boolean isModal = true;  // Track modal state
  private Event.NativePreviewHandler keyboardHandler;

  public Dialog() {
    super(false, true, new CaptionImpl());
    caption = getCaption();
    setGlassEnabled(true);
    setModal(false);

    // Set default ARIA role and attributes
    Roles.getDialogRole().set(getElement());
    getElement().setAttribute("aria-modal", "true");

    // Add global Escape key handler for accessibility
    keyboardHandler = new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        if (event.getTypeInt() == Event.ONKEYDOWN &&
            isShowing() &&
            event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
          event.getNativeEvent().preventDefault();
          event.getNativeEvent().stopPropagation();
          handleEscapeKey();
        }
      }
    };
    Event.addNativePreviewHandler(keyboardHandler);
  }

  /**
   * Sets the caption text for this dialog.
   *
   * @param text The caption text to display
   */
  public void setCaption(String text) {
    caption.setText(text);
  }

  /**
   * Sets the ARIA role for this dialog.
   * Called by UIBinder when role="..." attribute is present.
   *
   * @param role The ARIA role ("dialog" or "alertdialog")
   */
  public void setRole(String role) {
    this.ariaRole = role;
    if ("dialog".equals(role)) {
      Roles.getDialogRole().set(getElement());
    } else if ("alertdialog".equals(role)) {
      Roles.getAlertdialogRole().set(getElement());
    }
  }

  /**
   * Sets the aria-modal attribute.
   * Called by UIBinder when ariaModal="..." attribute is present.
   *
   * @param modal "true" or "false"
   */
  public void setAriaModal(String modal) {
    this.isModal = "true".equals(modal);
    getElement().setAttribute("aria-modal", modal);
  }

  /**
   * Sets the aria-labelledby attribute.
   * Called by UIBinder when ariaLabelledby="..." attribute is present.
   *
   * @param id The ID of the element that labels this dialog (usually title)
   */
  public void setAriaLabelledby(String id) {
    getElement().setAttribute("aria-labelledby", id);
  }

  /**
   * Sets the aria-describedby attribute.
   * Called by UIBinder when ariaDescribedby="..." attribute is present.
   *
   * @param id The ID of the element that describes this dialog
   */
  public void setAriaDescribedby(String id) {
    getElement().setAttribute("aria-describedby", id);
  }

  /**
   * Sets the aria-label attribute.
   * Called by UIBinder when ariaLabel="..." attribute is present.
   * Use when dialog has no visible title element.
   *
   * @param label The accessible label
   */
  public void setAriaLabel(String label) {
    getElement().setAttribute("aria-label", label);
  }

  /**
   * Configure ARIA attributes programmatically.
   * Use when creating dialogs in Java code (not UIBinder).
   *
   * @param titleText The dialog title text (can be null)
   */
  public void configureAria(String titleText) {
    setRole("dialog");
    setAriaModal("true");

    // If caption exists, link it with aria-labelledby
    if (getCaption() != null) {
      String titleId = Document.get().createUniqueId();
      getCaption().asWidget().getElement().setId(titleId);
      setAriaLabelledby(titleId);
    } else if (titleText != null) {
      setAriaLabel(titleText);
    }
  }

  /**
   * Shows this dialog with proper focus management.
   * Stores the currently focused element and moves focus to the first
   * focusable element within the dialog.
   */
  @Override
  public void show() {
    // Store current focus for restoration
    com.google.gwt.dom.client.Element activeElement = getFocusedElement();
    if (activeElement != null) {
      triggerElement = activeElement;
    }

    super.show();

    // Move focus to first focusable element in dialog (with a small delay to avoid
    // conflicts with explicit focus management in subclasses)
    com.google.gwt.core.client.Scheduler.get().scheduleDeferred(new com.google.gwt.core.client.Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        // Only auto-focus if nothing else has claimed focus
        com.google.gwt.dom.client.Element currentFocus = getFocusedElement();
        if (currentFocus == null || !getElement().isOrHasChild(currentFocus)) {
          focusFirstElement();
        }
      }
    });
  }

  /**
   * Hides this dialog and restores focus to the trigger element.
   */
  @Override
  public void hide() {
    super.hide();

    // Restore focus to trigger element (with a small delay to ensure dialog is fully hidden)
    final com.google.gwt.dom.client.Element elementToFocus = triggerElement;
    triggerElement = null;

    if (elementToFocus != null) {
      com.google.gwt.core.client.Scheduler.get().scheduleDeferred(new com.google.gwt.core.client.Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          try {
            // Only restore focus if the element is still in the document and visible
            if (isElementValid(elementToFocus)) {
              elementToFocus.focus();
            }
          } catch (Exception e) {
            // Ignore focus restoration errors - element might be gone
          }
        }
      });
    }
  }

  /**
   * Check if an element is still valid for focus restoration.
   */
  private native boolean isElementValid(com.google.gwt.dom.client.Element element) /*-{
    return element != null &&
           $doc.body.contains(element) &&
           element.offsetParent != null;
  }-*/;

  /**
   * Handle Escape key press.
   * Subclasses can override to customize behavior (e.g., prevent closing).
   */
  protected void handleEscapeKey() {
    hide();
  }

  /**
   * Focus the first focusable element in the dialog.
   */
  private void focusFirstElement() {
    Widget firstFocusable = findFirstFocusableWidget(this);
    if (firstFocusable instanceof FocusWidget) {
      ((FocusWidget) firstFocusable).setFocus(true);
    } else if (firstFocusable != null) {
      firstFocusable.getElement().focus();
    }
  }

  /**
   * Find first focusable widget in a widget tree.
   * Searches recursively through the widget hierarchy.
   *
   * @param widget The root widget to search from
   * @return The first focusable widget found, or null if none found
   */
  private Widget findFirstFocusableWidget(Widget widget) {
    // Check if current widget is focusable
    if (isFocusable(widget)) {
      return widget;
    }

    // If widget is a container, search its children
    if (widget instanceof HasWidgets) {
      Iterator<Widget> iterator = ((HasWidgets) widget).iterator();
      while (iterator.hasNext()) {
        Widget child = iterator.next();
        Widget focusable = findFirstFocusableWidget(child);
        if (focusable != null) {
          return focusable;
        }
      }
    }

    return null;
  }

  /**
   * Check if a widget is focusable.
   *
   * @param widget The widget to check
   * @return true if the widget can receive focus
   */
  private boolean isFocusable(Widget widget) {
    if (widget == null) {
      return false;
    }

    // Skip focus trap buttons - they're for Tab key containment, not initial focus
    com.google.gwt.dom.client.Element element = widget.getElement();
    if (element != null) {
      String className = element.getClassName();
      if (className != null && className.contains("FocusTrap")) {
        return false;
      }

      // Skip elements with tabindex="-1"
      int tabIndex = element.getTabIndex();
      if (tabIndex < 0) {
        return false;
      }
    }

    // Check if it's a FocusWidget (Button, TextBox, etc.)
    if (widget instanceof FocusWidget) {
      FocusWidget focusWidget = (FocusWidget) widget;
      return focusWidget.isEnabled();
    }

    // Check if the element has a positive tabindex
    if (element != null) {
      int tabIndex = element.getTabIndex();
      if (tabIndex >= 0) {
        return true;
      }

      // Check for naturally focusable elements
      String tagName = element.getTagName().toLowerCase();
      if ("a".equals(tagName) || "button".equals(tagName) ||
          "input".equals(tagName) || "select".equals(tagName) ||
          "textarea".equals(tagName)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Get currently focused element from DOM using JSNI.
   *
   * @return The currently focused element, or null if none
   */
  private native com.google.gwt.dom.client.Element getFocusedElement() /*-{
    return $doc.activeElement;
  }-*/;
}
