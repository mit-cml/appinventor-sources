/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.appengine.demos.taskengine.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.EventListener;

/**
 * Simple utility class for registering and unregistering an EventListener.
 */
public class DomUtils {

  /**
   * A simple Overlay type that allows us to unhook an event listener.
   */
  public static class EventRemover extends JavaScriptObject {
    protected EventRemover() {
    }

    /**
     * Simply calls remove, which should correspond to a function that unhooks
     * the event listener.
     */
    public final native void remove() /*-{
      this.remove()
    }-*/;
  }

  /**
   * A simple Overlay type for the Window object.
   */
  public static class Window extends JavaScriptObject {
    protected Window() {
    }

    /**
     * Adds a resize listener.
     * 
     * @param listener
     */
    public final void addResizeListener(EventListener listener) {
      DomUtils.addEventListener("resize", this, listener);
    }

    public final native void alert(String msg) /*-{
      this.alert(msg);
    }-*/;

    public final native boolean confirm(String msg) /*-{
      return this.confirm(msg);
    }-*/;

    /**
     * We subtract one to work around an issue on iphone where certain
     * dimensions seem to be special cased and break when set on resize.
     * 
     * @return the inner width of the window
     */
    public final native int getWidth() /*-{
      return this.innerWidth - 1;
    }-*/;;
  }

  /**
   * Adds an {@link EventListener} as the recipient of event dispatches for a
   * specific event type on a specific element.
   * 
   * @param type
   * @param elem
   * @param listener
   * @return
   */
  public static EventRemover addEventListener(String type,
      JavaScriptObject elem, EventListener listener) {
    return addEventListenerImpl(type, elem, listener);
  }

  /**
   * Helper function for getting access to the Window object.
   * 
   * @return the Window object
   */
  public static native Window getWindow() /*-{
    return $wnd;
  }-*/;

  /**
   * private implementation of adding an {@link EventListener} as the recipient
   * of event dispatches for a specific event type on a specific element.
   * 
   * @param type
   * @param sourceElem
   * @param listener
   * @return
   */
  private static native EventRemover addEventListenerImpl(String type,
      JavaScriptObject sourceElem, EventListener listener) /*-{
    var f = function(event) {
      listener.
      @com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)
      (event);
    }

    // Hang an expando that allows us to unhook this event
    f.remove = function() {
      sourceElem.removeEventListener(type,f,false);
    }

    // Register the event listener
    sourceElem.addEventListener(type,f,false);    
    return f;
  }-*/;
}
