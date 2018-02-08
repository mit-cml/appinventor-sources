package com.google.appinventor.client.editor.youngandroid.events;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * EventHelper provides convenience methods for querying JavaScript events produced by Blockly.
 *
 * @author ewpatton
 *
 */
public final class EventHelper {
  private EventHelper() {
  }

  /**
   * Determine whether an event from the native JavaScript (e.g., Blockly) is transient.
   *
   * @param event Native JavaScript event
   * @return true if the event can be determined to be transient, otherwise false.
   */
  public static native boolean isTransient(JavaScriptObject event)/*-{
    if (!event) {
      // null events are transient
      return true;
    } else if (event.isTransient) {
      // transient events are transient
      return true;
    } else if (event.type == 'ui') {
      // Blockly ui events are transient if they are selection changes, clicks, opening of mutator
      // and warning bubbles.
      return event.element == 'selected' || event.element == 'click' ||
        event.element == 'mutatorOpen' || event.element == 'warningOpen';
    }
    return false;
  }-*/;

  /**
   * Determine whether an event from the native JavaScript (e.g., Blockly) is a UI event.
   *
   * @param event Native JavaScript event
   * @return true if the event can be determined to be user interface event, otherwise false.
   */
  public static native boolean isUi(JavaScriptObject event)/*-{
    return event && event.type === 'ui';
  }-*/;
}
