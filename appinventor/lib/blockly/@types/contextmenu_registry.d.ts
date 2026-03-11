/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Registry for context menu option items.
 *
 * @class
 */
import type { BlockSvg } from './block_svg.js';
import { RenderedWorkspaceComment } from './comments/rendered_workspace_comment.js';
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
import { Coordinate } from './utils/coordinate.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for the registry of context menu items. This is intended to be a
 * singleton. You should not create a new instance, and only access this class
 * from ContextMenuRegistry.registry.
 */
export declare class ContextMenuRegistry {
    static registry: ContextMenuRegistry;
    /** Registry of all registered RegistryItems, keyed by ID. */
    private registeredItems;
    /** Resets the existing singleton instance of ContextMenuRegistry. */
    constructor();
    /** Clear and recreate the registry. */
    reset(): void;
    /**
     * Registers a RegistryItem.
     *
     * @param item Context menu item to register.
     * @throws {Error} if an item with the given ID already exists.
     */
    register(item: RegistryItem): void;
    /**
     * Unregisters a RegistryItem with the given ID.
     *
     * @param id The ID of the RegistryItem to remove.
     * @throws {Error} if an item with the given ID does not exist.
     */
    unregister(id: string): void;
    /**
     * @param id The ID of the RegistryItem to get.
     * @returns RegistryItem or null if not found
     */
    getItem(id: string): RegistryItem | null;
    /**
     * Gets the valid context menu options for the given scope.
     * Options are only included if the preconditionFn shows
     * they should not be hidden.
     *
     * @param scope Current scope of context menu (i.e., the exact workspace or
     *     block being clicked on).
     * @param menuOpenEvent Event that caused the menu to open.
     * @returns the list of ContextMenuOptions
     */
    getContextMenuOptions(scope: Scope, menuOpenEvent: Event): ContextMenuOption[];
}
export declare namespace ContextMenuRegistry {
    /**
     * Where this menu item should be rendered. If the menu item should be
     * rendered in multiple scopes, e.g. on both a block and a workspace, it
     * should be registered for each scope.
     */
    export enum ScopeType {
        BLOCK = "block",
        WORKSPACE = "workspace",
        COMMENT = "comment"
    }
    /**
     * The actual workspace/block/focused object where the menu is being
     * rendered. This is passed to callback and displayText functions
     * that depend on this information.
     */
    export interface Scope {
        block?: BlockSvg;
        workspace?: WorkspaceSvg;
        comment?: RenderedWorkspaceComment;
        focusedNode?: IFocusableNode;
    }
    /**
     * Fields common to all context menu registry items.
     */
    interface CoreRegistryItem {
        scopeType?: ScopeType;
        weight: number;
        id: string;
        preconditionFn?: (p1: Scope, menuOpenEvent: Event) => string;
    }
    /**
     * A representation of a normal, clickable menu item in the registry.
     */
    interface ActionRegistryItem extends CoreRegistryItem {
        /**
         * @param scope Object that provides a reference to the thing that had its
         *     context menu opened.
         * @param menuOpenEvent The original event that triggered the context menu to open.
         * @param menuSelectEvent The event that triggered the option being selected.
         * @param location The location in screen coordinates where the menu was opened.
         */
        callback: (scope: Scope, menuOpenEvent: Event, menuSelectEvent: Event, location: Coordinate) => void;
        displayText: ((p1: Scope) => string | HTMLElement) | string | HTMLElement;
        separator?: never;
        preconditionFn: (p1: Scope, menuOpenEvent: Event) => string;
    }
    /**
     * A representation of a menu separator item in the registry.
     */
    interface SeparatorRegistryItem extends CoreRegistryItem {
        separator: true;
        callback?: never;
        displayText?: never;
    }
    /**
     * A menu item as entered in the registry.
     */
    export type RegistryItem = ActionRegistryItem | SeparatorRegistryItem;
    /**
     * Fields common to all context menu items as used by contextmenu.ts.
     */
    export interface CoreContextMenuOption {
        scope: Scope;
        weight: number;
    }
    /**
     * A representation of a normal, clickable menu item in contextmenu.ts.
     */
    export interface ActionContextMenuOption extends CoreContextMenuOption {
        text: string | HTMLElement;
        enabled: boolean;
        /**
         * @param scope Object that provides a reference to the thing that had its
         *     context menu opened.
         * @param menuOpenEvent The original event that triggered the context menu to open.
         * @param menuSelectEvent The event that triggered the option being selected.
         * @param location The location in screen coordinates where the menu was opened.
         */
        callback: (scope: Scope, menuOpenEvent: Event, menuSelectEvent: Event, location: Coordinate) => void;
        separator?: never;
    }
    /**
     * A representation of a menu separator item in contextmenu.ts.
     */
    export interface SeparatorContextMenuOption extends CoreContextMenuOption {
        separator: true;
        text?: never;
        enabled?: never;
        callback?: never;
    }
    /**
     * A menu item as presented to contextmenu.ts.
     */
    export type ContextMenuOption = ActionContextMenuOption | SeparatorContextMenuOption;
    /**
     * A subset of ContextMenuOption corresponding to what was publicly
     * documented.  ContextMenuOption should be preferred for new code.
     */
    export interface LegacyContextMenuOption {
        text: string;
        enabled: boolean;
        callback: (p1: Scope) => void;
        separator?: never;
    }
    export {};
}
export type ScopeType = ContextMenuRegistry.ScopeType;
export declare const ScopeType: typeof ContextMenuRegistry.ScopeType;
export type Scope = ContextMenuRegistry.Scope;
export type RegistryItem = ContextMenuRegistry.RegistryItem;
export type ContextMenuOption = ContextMenuRegistry.ContextMenuOption;
export type LegacyContextMenuOption = ContextMenuRegistry.LegacyContextMenuOption;
//# sourceMappingURL=contextmenu_registry.d.ts.map