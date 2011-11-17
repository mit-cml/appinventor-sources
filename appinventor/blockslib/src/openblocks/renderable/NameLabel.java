package openblocks.renderable;

import java.awt.Color;
import java.awt.Point;

import openblocks.codeblocks.BlockConnectorShape;
import openblocks.codeblocks.BlockShape;

/**
 * NameLabel displays the name of a RenderableBlock
 */
class NameLabel extends BlockLabel{
  private long blockID;

  public NameLabel(String initLabelText, BlockLabel.Type labelType, boolean isEditable, 
      long blockID) {
    super(initLabelText, labelType, isEditable, blockID, true, Color.WHITE);
    this.blockID = blockID;
  }

  Point getUnscaledNameLabelLocation() {
    int x = 0;
    int y = 0;
    RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);
    if (rb != null) {
      Point labelLoc = rb.getUnscaledBlockLabelLocation();
      x = labelLoc.x;
      y = labelLoc.y;
      if (rb.getBlockWidget() == null  && rb.getAbstractBlockArea() != null) {
        y += rb.getAbstractBlockArea().getBounds().height / 2;
      } else {
        y += 12;
      }

      if (rb.getBlock().isCommandBlock()) {
        y -= 2;
      }
      if(rb.getBlock().hasPageLabel() && rb.getBlock().hasAfterConnector()) {
        y -= BlockConnectorShape.CONTROL_PLUG_HEIGHT;
      }
      if(!rb.getBlock().hasPageLabel()) {
        y -= getAbstractHeight() / 2;
      }
    }
    return new Point(x, y);
  }
  void update() {
    Point usp = getUnscaledNameLabelLocation();
    setPixelLocation(rescale(usp.x), rescale(usp.y));
  }
}
