/**
 * @license
 * Copyright 2016 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Gesture } from './gesture.js';
/**
 * Whether touch is enabled in the browser.
 * Copied from Closure's goog.events.BrowserFeature.TOUCH_ENABLED
 */
export declare const TOUCH_ENABLED: boolean;
/**
 * The TOUCH_MAP lookup dictionary specifies additional touch events to fire,
 * in conjunction with mouse events.
 */
export declare const TOUCH_MAP: {
    [key: string]: string[];
};
/**
 * Context menus on touch devices are activated using a long-press.
 * Unfortunately the contextmenu touch event is currently (2015) only supported
 * by Chrome.  This function is fired on any touchstart event, queues a task,
 * which after about a second opens the context menu.  The tasks is killed
 * if the touch event terminates early.
 *
 * @param e Touch start event.
 * @param gesture The gesture that triggered this longStart.
 * @internal
 */
export declare function longStart(e: PointerEvent, gesture: Gesture): void;
/**
 * Nope, that's not a long-press.  Either touchend or touchcancel was fired,
 * or a drag hath begun.  Kill the queued long-press task.
 *
 * @internal
 */
export declare function longStop(): void;
/**
 * Clear the touch identifier that tracks which touch stream to pay attention
 * to.  This ends the current drag/gesture and allows other pointers to be
 * captured.
 */
export declare function clearTouchIdentifier(): void;
/**
 * Decide whether Blockly should handle or ignore this event.
 * Mouse and touch events require special checks because we only want to deal
 * with one touch stream at a time.  All other events should always be handled.
 *
 * @param e The event to check.
 * @returns True if this event should be passed through to the registered
 *     handler; false if it should be blocked.
 */
export declare function shouldHandleEvent(e: Event): boolean;
/**
 * Get the pointer identifier from the given event.
 *
 * @param e Pointer event.
 * @returns The pointerId of the event.
 */
export declare function getTouchIdentifierFromEvent(e: PointerEvent): string;
/**
 * Check whether the pointer identifier on the event matches the current saved
 * identifier. If the current identifier was unset, save the identifier from
 * the event. This starts a drag/gesture, during which pointer events with
 * other identifiers will be silently ignored.
 *
 * @param e Pointer event.
 * @returns Whether the identifier on the event matches the current saved
 *     identifier.
 */
export declare function checkTouchIdentifier(e: PointerEvent): boolean;
//# sourceMappingURL=touch.d.ts.map