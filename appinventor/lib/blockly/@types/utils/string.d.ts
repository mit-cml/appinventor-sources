/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Fast prefix-checker.
 * Copied from Closure's goog.string.startsWith.
 *
 * @param str The string to check.
 * @param prefix A string to look for at the start of `str`.
 * @returns True if `str` begins with `prefix`.
 * @deprecated Use built-in **string.startsWith** instead.
 */
export declare function startsWith(str: string, prefix: string): boolean;
/**
 * Given an array of strings, return the length of the shortest one.
 *
 * @param array Array of strings.
 * @returns Length of shortest string.
 */
export declare function shortestStringLength(array: string[]): number;
/**
 * Given an array of strings, return the length of the common prefix.
 * Words may not be split.  Any space after a word is included in the length.
 *
 * @param array Array of strings.
 * @param opt_shortest Length of shortest string.
 * @returns Length of common prefix.
 */
export declare function commonWordPrefix(array: string[], opt_shortest?: number): number;
/**
 * Given an array of strings, return the length of the common suffix.
 * Words may not be split.  Any space after a word is included in the length.
 *
 * @param array Array of strings.
 * @param opt_shortest Length of shortest string.
 * @returns Length of common suffix.
 */
export declare function commonWordSuffix(array: string[], opt_shortest?: number): number;
/**
 * Wrap text to the specified width.
 *
 * @param text Text to wrap.
 * @param limit Width to wrap each line.
 * @returns Wrapped text.
 */
export declare function wrap(text: string, limit: number): string;
/**
 * Is the given string a number (includes negative and decimals).
 *
 * @param str Input string.
 * @returns True if number, false otherwise.
 */
export declare function isNumber(str: string): boolean;
//# sourceMappingURL=string.d.ts.map