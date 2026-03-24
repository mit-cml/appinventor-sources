/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Abstract } from './events/events_abstract.js';
import type { Field } from './field.js';
import type { Input } from './inputs/input.js';
import type { IConnectionChecker } from './interfaces/i_connection_checker.js';
import type { IConnectionPreviewer } from './interfaces/i_connection_previewer.js';
import type { ICopyable } from './interfaces/i_copyable.js';
import type { IDragger } from './interfaces/i_dragger.js';
import type { IFlyout } from './interfaces/i_flyout.js';
import type { IIcon } from './interfaces/i_icon.js';
import type { IMetricsManager } from './interfaces/i_metrics_manager.js';
import type { IPaster } from './interfaces/i_paster.js';
import type { ISerializer } from './interfaces/i_serializer.js';
import type { IToolbox } from './interfaces/i_toolbox.js';
import type { Cursor } from './keyboard_nav/cursor.js';
import type { Options } from './options.js';
import type { Renderer } from './renderers/common/renderer.js';
import type { Theme } from './theme.js';
import type { ToolboxItem } from './toolbox/toolbox_item.js';
export declare const TEST_ONLY: {
    typeMap: {
        [key: string]: {
            [key: string]: any;
        };
    };
};
/**
 * The string used to register the default class for a type of plugin.
 */
export declare const DEFAULT = "default";
/**
 * A name with the type of the element stored in the generic.
 */
export declare class Type<_T> {
    private readonly name;
    /** @param name The name of the registry type. */
    constructor(name: string);
    /**
     * Returns the name of the type.
     *
     * @returns The name.
     */
    toString(): string;
    static CONNECTION_CHECKER: Type<IConnectionChecker>;
    static CONNECTION_PREVIEWER: Type<IConnectionPreviewer>;
    static CURSOR: Type<Cursor>;
    static EVENT: Type<Abstract>;
    static FIELD: Type<Field<any>>;
    static INPUT: Type<Input>;
    static RENDERER: Type<Renderer>;
    static TOOLBOX: Type<IToolbox>;
    static THEME: Type<Theme>;
    static TOOLBOX_ITEM: Type<ToolboxItem>;
    static FLYOUTS_VERTICAL_TOOLBOX: Type<IFlyout>;
    static FLYOUTS_HORIZONTAL_TOOLBOX: Type<IFlyout>;
    static METRICS_MANAGER: Type<IMetricsManager>;
    /**
     * Type for an IDragger. Formerly behavior was mostly covered by
     * BlockDraggeers, which is why the name is inaccurate.
     */
    static BLOCK_DRAGGER: Type<IDragger>;
    /** @internal */
    static SERIALIZER: Type<ISerializer>;
    /** @internal */
    static ICON: Type<IIcon>;
    /** @internal */
    static PASTER: Type<IPaster<ICopyable.ICopyData, ICopyable<ICopyable.ICopyData>>>;
}
/**
 * Registers a class based on a type and name.
 *
 * @param type The type of the plugin.
 *     (e.g. Field, Renderer)
 * @param name The plugin's name. (Ex. field_angle, geras)
 * @param registryItem The class or object to register.
 * @param opt_allowOverrides True to prevent an error when overriding an already
 *     registered item.
 * @throws {Error} if the type or name is empty, a name with the given type has
 *     already been registered, or if the given class or object is not valid for
 *     its type.
 */
export declare function register<T>(type: string | Type<T>, name: string, registryItem: (new (...p1: any[]) => T) | null | any, opt_allowOverrides?: boolean): void;
/**
 * Unregisters the registry item with the given type and name.
 *
 * @param type The type of the plugin.
 *     (e.g. Field, Renderer)
 * @param name The plugin's name. (Ex. field_angle, geras)
 */
export declare function unregister<T>(type: string | Type<T>, name: string): void;
/**
 * Returns whether or not the registry contains an item with the given type and
 * name.
 *
 * @param type The type of the plugin.
 *     (e.g. Field, Renderer)
 * @param name The plugin's name. (Ex. field_angle, geras)
 * @returns True if the registry has an item with the given type and name, false
 *     otherwise.
 */
export declare function hasItem<T>(type: string | Type<T>, name: string): boolean;
/**
 * Gets the class for the given name and type.
 *
 * @param type The type of the plugin.
 *     (e.g. Field, Renderer)
 * @param name The plugin's name. (Ex. field_angle, geras)
 * @param opt_throwIfMissing Whether or not to throw an error if we are unable
 *     to find the plugin.
 * @returns The class with the given name and type or null if none exists.
 */
export declare function getClass<T>(type: string | Type<T>, name: string, opt_throwIfMissing?: boolean): (new (...p1: any[]) => T) | null;
/**
 * Gets the object for the given name and type.
 *
 * @param type The type of the plugin.
 *     (e.g. Category)
 * @param name The plugin's name. (Ex. logic_category)
 * @param opt_throwIfMissing Whether or not to throw an error if we are unable
 *     to find the object.
 * @returns The object with the given name and type or null if none exists.
 */
export declare function getObject<T>(type: string | Type<T>, name: string, opt_throwIfMissing?: boolean): T | null;
/**
 * Returns a map of items registered with the given type.
 *
 * @param type The type of the plugin. (e.g. Category)
 * @param opt_cased Whether or not to return a map with cased keys (rather than
 *     caseless keys). False by default.
 * @param opt_throwIfMissing Whether or not to throw an error if we are unable
 *     to find the object. False by default.
 * @returns A map of objects with the given type, or null if none exists.
 */
export declare function getAllItems<T>(type: string | Type<T>, opt_cased?: boolean, opt_throwIfMissing?: boolean): {
    [key: string]: T | null | (new (...p1: any[]) => T);
} | null;
/**
 * Gets the class from Blockly options for the given type.
 * This is used for plugins that override a built in feature. (e.g. Toolbox)
 *
 * @param type The type of the plugin.
 * @param options The option object to check for the given plugin.
 * @param opt_throwIfMissing Whether or not to throw an error if we are unable
 *     to find the plugin.
 * @returns The class for the plugin.
 */
export declare function getClassFromOptions<T>(type: Type<T>, options: Options, opt_throwIfMissing?: boolean): (new (...p1: any[]) => T) | null;
//# sourceMappingURL=registry.d.ts.map