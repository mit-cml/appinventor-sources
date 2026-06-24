/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Display/configuration options for a toast notification.
 */
export interface ToastOptions {
    /**
     * Toast ID. If set along with `oncePerSession`, will cause subsequent toasts
     * with this ID to not be shown.
     */
    id?: string;
    /**
     * Flag to show the toast once per session only.
     * Subsequent calls are ignored.
     */
    oncePerSession?: boolean;
    /**
     * Text of the message to display on the toast.
     */
    message: string;
    /**
     * Duration in seconds before the toast is removed. Defaults to 5.
     */
    duration?: number;
    /**
     * How prominently/interrupting the readout of the toast should be for
     * screenreaders. Corresponds to aria-live and defaults to polite.
     */
    assertiveness?: Toast.Assertiveness;
}
/**
 * Class that allows for showing and dismissing temporary notifications.
 */
export declare class Toast {
    /** IDs of toasts that have previously been shown. */
    private static shownIds;
    /**
     * Shows a toast notification.
     *
     * @param workspace The workspace to show the toast on.
     * @param options Configuration options for the toast message, duration, etc.
     */
    static show(workspace: WorkspaceSvg, options: ToastOptions): void;
    /**
     * Creates the DOM representation of a toast.
     *
     * @param workspace The workspace to inject the toast notification onto.
     * @param options Configuration options for the toast.
     * @returns The root DOM element of the toast.
     */
    protected static createDom(workspace: WorkspaceSvg, options: ToastOptions): HTMLDivElement;
    /**
     * Dismiss a toast, e.g. in response to a user action.
     *
     * @param workspace The workspace to dismiss a toast in.
     * @param id The toast ID, or undefined to clear any toast.
     */
    static hide(workspace: WorkspaceSvg, id?: string): void;
}
/**
 * Options for how aggressively toasts should be read out by screenreaders.
 * Values correspond to those for aria-live.
 */
export declare namespace Toast {
    enum Assertiveness {
        ASSERTIVE = "assertive",
        POLITE = "polite"
    }
}
//# sourceMappingURL=toast.d.ts.map