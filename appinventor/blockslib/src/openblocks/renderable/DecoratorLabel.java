// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.renderable;

import java.awt.Color;
import java.awt.Point;

/**
 * Provides a label which renders in the top left of a block to inform
 * users what the block type is without using the "label" of the block,
 * which is often user-editable.
 *
 *
 */
public class DecoratorLabel extends BlockLabel{
  private long blockId;

  /**
   * Creates a new DecoratorLabel to add to a Block.
   *
   * @param initLabelText the text of the decorator.
   * @param blockId the id of the Block that this decorator is being added to.
   */
  public DecoratorLabel(String initLabelText, long blockId) {
    super(initLabelText, BlockLabel.Type.DECORATOR_LABEL, false, blockId, true, Color.WHITE);
    this.blockId = blockId;
  }

  /**
   * Updates the location of the text in the block.
   */
  public void update() {
    RenderableBlock rb = RenderableBlock.getRenderableBlock(blockId);
    if (rb != null) {
      Point decoratorLocation = rb.getUnScaledDecoratorLabelLocation();
      setPixelLocation(rb.rescale(decoratorLocation.x), rb.rescale(decoratorLocation.y));
    }
  }
}
