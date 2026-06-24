import type { IBoundedElement } from './interfaces/i_bounded_element.js';
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
/**
 * Representation of an item displayed in a flyout.
 */
export declare class FlyoutItem {
    private element;
    private type;
    /**
     * Creates a new FlyoutItem.
     *
     * @param element The element that will be displayed in the flyout.
     * @param type The type of element. Should correspond to the type of the
     *     flyout inflater that created this object.
     */
    constructor(element: IBoundedElement & IFocusableNode, type: string);
    /**
     * Returns the element displayed in the flyout.
     */
    getElement(): IBoundedElement & IFocusableNode;
    /**
     * Returns the type of flyout element this item represents.
     */
    getType(): string;
}
//# sourceMappingURL=flyout_item.d.ts.map