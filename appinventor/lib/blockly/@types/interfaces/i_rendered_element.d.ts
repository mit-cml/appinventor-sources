/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
export interface IRenderedElement {
    /**
     * @returns The root SVG element of this rendered element.
     */
    getSvgRoot(): SVGElement;
}
/**
 * @returns True if the given object is an IRenderedElement.
 */
export declare function isRenderedElement(obj: any): obj is IRenderedElement;
//# sourceMappingURL=i_rendered_element.d.ts.map